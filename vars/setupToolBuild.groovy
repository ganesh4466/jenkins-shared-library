import com.gseven.BuildToolManager

/**
 * Shared library function to set up a build tool in Jenkins.
 *
 * @param toolName Name of the tool (e.g., 'maven')
 * @param toolVersion Version of the tool (e.g., '3.8.7')
 * @param downloadUrl URL for the tool download
 * @param toolBinPath Path within the tool's directory to the executable
 * @return Path to the tool's executable directory
 */
def call(String toolName, String toolVersion, String downloadUrl, String toolBinPath) {
    return BuildToolManager.setupBuildTool(toolName, toolVersion, downloadUrl, toolBinPath)
}
