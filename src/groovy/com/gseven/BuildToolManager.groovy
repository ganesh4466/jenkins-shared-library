package com.gseven

import org.jfrog.artifactory.client.Artifactory

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.nio.file.Files
import java.nio.file.Paths

class BuildToolManager {

    static void main(String[] args) {
        String maven = BuildToolManager.setupBuildTool("maven", "3.4.1", "https://repo1.maven.org/maven2/org/apache/maven/plugins/maven-jar-plugin/3.4.1/maven-jar-plugin-3.4.1-source-release.zip", "\\maven-jar-plugin-3.7.1\\src\\main");
        System.out.println(maven)
    }
    static String setupBuildTool(String toolName, String toolVersion, String downloadUrl, String toolBinPath) {
        boolean isWindows = detectOS()
        String toolsDir = isWindows ? "C:\\Jenkins\\tools\\" : "/opt/jenkins/tools/"
        String toolDirPath = "${toolsDir}${toolName}-${toolVersion}"
        File toolDir = new File(toolDirPath)
        String toolPath = "${toolDirPath}${toolBinPath}"

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
        File toolZip = new File("${toolsDir}${toolName}-${toolVersion}.tgz")
        File toolDir = new File("${toolsDir}${toolName}-${toolVersion}")

        // Ensure tools directory exists
        toolDir.parentFile.mkdirs()

        // Download the tool
//        new URL(downloadUrl).withInputStream { i ->
//            toolZip.withOutputStream { it << i }
//        }
        URL url = new URL("https://demo.jfrog.io/artifactory/release-bundles-v2/release-bundle-example/1.0/artifacts/npm/jfrog-npm-example/-/jfrog-npm-example-1.0.0.tgz")
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        connection.setRequestMethod('GET')

        // Add Basic Authentication header
        def authString = "visitor_nag0914651:NaginNagi@1".bytes.encodeBase64().toString()
        connection.setRequestProperty('Authorization', "Basic ${authString}")

        connection.connect()

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.inputStream
            toolZip.withOutputStream {it<<inputStream}
            inputStream.close()
            println "File downloaded as: $toolZip"
        } else {
            println "Failed to download file. HTTP response code: ${connection.responseCode}"
        }
        connection.disconnect()
        println("Downloaded ${toolName} version ${toolVersion} to ${toolZip}")

        // Unzip the downloaded file
        if (isWindows) {
           // unzipWindows(toolZip, toolDir)
            extractTgz(toolZip, toolDir)
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

    static void extractTgz(File inputFile, File outputDir) {

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        // Open the tgz file
        inputFile.withInputStream { fileInputStream ->
            GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(fileInputStream)
            TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)

            def entry
            byte[] buffer = new byte[1024]
            while ((entry = tarIn.nextTarEntry) != null) {
                def outFile = new File(outputDir, entry.name)

                if (entry.isDirectory()) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile.mkdirs() // Ensure parent directories exist
                    outFile.withOutputStream { outputStream ->
                        int bytesRead
                        while ((bytesRead = tarIn.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }
                println "Extracted: ${outFile.absolutePath}"
            }

            tarIn.close()
            gzipIn.close()
        }
    }

}
