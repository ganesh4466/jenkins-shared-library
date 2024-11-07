static void ensureDirectoryExistsWithPermissions(String directoryPath, boolean isWindows) {
        File dir = new File(directoryPath)

        if (!dir.exists()) {
            if (isWindows) {
                dir.mkdirs()
                println("Created directory: ${directoryPath}")
            } else {
                def createDirCommand = "sudo mkdir -p ${directoryPath}"
                def setPermissionsCommand = "sudo chown -R jenkins:jenkins ${directoryPath} && sudo chmod -R 755 ${directoryPath}"

                executeShellCommand(createDirCommand)
                executeShellCommand(setPermissionsCommand)
                println("Created directory and set permissions for: ${directoryPath}")
            }
        } else {
            println("Directory already exists: ${directoryPath}")
        }
    }

    static void executeShellCommand(String command) {
        def proc = command.execute()
        proc.waitFor()
        if (proc.exitValue() != 0) {
            println("Error executing command: ${command}")
            println("Error: ${proc.err.text}")
        } else {
            println("Command executed successfully: ${command}")
        }
    }
