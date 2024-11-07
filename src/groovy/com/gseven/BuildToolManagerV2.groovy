package com.gseven

import org.jfrog.artifactory.client.ArtifactoryClientBuilder

import java.nio.file.Files
import java.nio.file.Paths

class BuildToolManagerV2 {

    static void main(String[] args) {
        String maven = BuildToolManagerV2.setupBuildTool("maven", "3.4.1","\\groovy-4.0.23\\bin\\","");
        System.out.println(maven)
    }

    static String setupBuildTool(String toolName, String toolVersion, String toolBinPath, String url) {
        boolean isWindows = detectOS()
        String toolsDir = isWindows ? "C:\\Jenkins\\tools\\" : "/opt/jenkins/tools/"
        String toolDirPath = "${toolsDir}${toolName}-${toolVersion}"
        File toolDir = new File(toolDirPath)
        String toolPath = "${toolDirPath}${toolBinPath}"

        if (!toolExists(toolPath)) {
            downloadAndInstallTool(toolName, toolVersion, toolsDir, url, isWindows)
        } else {
            println("Tool ${toolName} version ${toolVersion} already exists at ${toolPath}.")
        }

        configureWorkspaceSymlink(toolDir, isWindows)
        updateEnvironmentPath(toolPath, isWindows)

        return toolPath
    }

    static boolean detectOS() {
        return System.getProperty("os.name").toLowerCase().contains("windows")
    }

    static boolean toolExists(String toolPath) {
        return new File(toolPath).exists()
    }

    static void downloadAndInstallTool(String toolName, String toolVersion, String toolsDir, String url, boolean isWindows) {

            def artifactory = ArtifactoryClientBuilder.create()
                    .setUrl("https://demo.jfrog.io/ui/native/")
                    .setUsername("visitor_nag0914651")
                    .setPassword("NaginNagi@1")
                    .addInterceptorLast({ request, httpContext ->
                        System.out.println("Artifactory request: " + request.getRequestLine());
                    })
                    .build()
           // def artifactPath = "${artifactoryRepo}/${toolName}-${toolVersion}.zip"
            def toolZip = new File("${toolsDir}${toolName}-${toolVersion}.zip")
            //def artifact = artifactory.repository("dist-release-local").download("/groovy-zips/apache-groovy-binary-4.0.23.zip").doDownload();
        InputStream iStream = artifactory.repository("maven-virtual")
                .download("org/jfrog/test/jfrog-maven-example/3.7-SNAPSHOT/jfrog-maven-example-3.7-20231003.160909-1.pom")
                .doDownload();
        println("Downloaded ${toolName} version ${toolVersion} locally.")

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

