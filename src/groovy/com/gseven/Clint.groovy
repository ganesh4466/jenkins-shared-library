package com.gseven

import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClientBuilder

class Clint {

    public static void main(String[] args) {
        //def artifactUrl = 'https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/apache-groovy-binary-2.4.10.zip'
        def artifactUrl = 'https://demo.jfrog.io/ui/native/maven-virtual/org/jfrog/test/jfrog-maven-example/3.7-SNAPSHOT/jfrog-maven-example-3.7-20231003.160909-1.pom'
        def downloadFileName = 'downloaded-artifact.zip'
        def renamedFileName = 'new-name-for-artifact.zip'

// Function to download a file from a URL and save it locally

// Download and rename the file
        downloadFile(artifactUrl, downloadFileName)

// Rename the downloaded file
        File downloadedFile = new File(downloadFileName)
        File renamedFile = new File(renamedFileName)

        if (downloadedFile.renameTo(renamedFile)) {
            println "File renamed to: $renamedFileName"
        } else {
            println "Failed to rename file."
        }
    }
    static def downloadFile(String fileUrl, String fileName) {
        URL url = new URL(fileUrl)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        connection.setRequestMethod('GET')
        connection.connect()

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.inputStream
            FileOutputStream outputStream = new FileOutputStream(fileName)

            byte[] buffer = new byte[1024]
            int bytesRead
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()
            println "File downloaded as: $fileName"
        } else {
            println "Failed to download file. HTTP response code: ${connection.responseCode}"
        }
        connection.disconnect()
    }

}
