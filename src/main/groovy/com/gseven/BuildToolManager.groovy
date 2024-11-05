package com.gseven

import groovy.io.FileType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class BuildToolManager {

    static String setupBuildTool(String toolName, String toolVersion, String downloadUrl, String toolBinPath) {
        boolean isWindows = detectOS()
        String toolsDir = isWindows ? "C:\\Jenkins\\tools\\" : "/opt/jenkins/tools/"
        String toolDirPath = "${toolsDir}${toolName}-${toolVersion}"
        File toolDir = new File(toolDirPath)
        String toolPath = "${toolDirPath}/${toolBinPath}"

        // Ensure tool is available or download and install it
        if (!toolExists(toolPath)) {
            downloadAndInstallTool(toolName, toolVersion, downloadUrl, toolsDir, isWindows)
        } else {
            println("Tool ${toolName} version ${toolVersion} already exists at ${toolPath}.")
        }

        // Create symlink in workspace and configure PATH
        configureWorkspaceSymlink(toolDir, isWindows)
        updateEnvironmentPath(toolPath, isWindows)

        // Return tool path for further use
        return toolPath
    }

    static boolean detectOS() {
        return System.getProperty("os.name").toLowerCase().contains("windows")
    }

    static boolean toolExists(String toolPath) {
        return new File(toolPath).exists()
    }

    static void downloadAndInstallTool(String toolName, String toolVersion, String downloadUrl, String toolsDir, boolean isWindows) {
        File toolZip = new File("${toolsDir}${toolName}-${toolVersion}.zip")
        File toolDir = new File("${toolsDir}${toolName}-${toolVersion}")

        // Ensure tools directory exists
        toolDir.parentFile.mkdirs()

        // Download the tool
        new URL(downloadUrl).withInputStream { i ->
            toolZip.withOutputStream { it << i }
        }
        println("Downloaded ${toolName} version ${toolVersion} to ${toolZip}")

        // Unzip the downloaded file
        if (isWindows) {
            unzipWindows(toolZip, toolDir)
        } else {
            unzipLinux(toolZip, toolDir)
        }
    }

    static void unzipWindows(File zipFile, File destDir) {
        def zip = new java.util.zip.ZipFile(zipFile)
        zip.entries().each { entry ->
            File entryFile = new File(destDir, entry.name)
            if (entry.isDirectory()) {
                entryFile.mkdirs()
            } else {
                entryFile.parentFile.mkdirs()
                entryFile.withOutputStream { out ->
                    zip.getInputStream(entry).withStream { it -> out << it }
                }
            }
        }
        zip.close()
        println("Unzipped ${zipFile} to ${destDir}")
    }

    static void unzipLinux(File zipFile, File destDir) {
        def ant = new AntBuilder()
        ant.unzip(src: zipFile.absolutePath, dest: destDir.absolutePath, overwrite: "true")
        println("Unzipped ${zipFile} to ${destDir}")
    }

    static void configureWorkspaceSymlink(File toolDir, boolean isWindows) {
        File workspaceToolDir = isWindows ? new File("C:\\Jenkins\\workspace\\tools") : new File("${System.getenv('WORKSPACE')}/tools")
        File symlinkPath = new File(workspaceToolDir, toolDir.name)

        workspaceToolDir.mkdirs()

        if (!symlinkPath.exists()) {
            if (isWindows) {
                def command = ["cmd", "/c", "mklink", "/D", symlinkPath.toString(), toolDir.toString()]
                def proc = command.execute()
                proc.waitFor()
                println("Created symlink at ${symlinkPath} for ${toolDir}")
            } else {
                Files.createSymbolicLink(Paths.get(symlinkPath.toString()), Paths.get(toolDir.toString()))
                println("Created symlink at ${symlinkPath} for ${toolDir}")
            }
        }
    }

    static void updateEnvironmentPath(String toolPath, boolean isWindows) {
        String currentPath = System.getenv("PATH")
        System.setProperty("env.PATH", isWindows ? "C:\\Jenkins\\workspace\\tools\\${toolPath};${currentPath}" : "${toolPath}:${currentPath}")
    }
}
