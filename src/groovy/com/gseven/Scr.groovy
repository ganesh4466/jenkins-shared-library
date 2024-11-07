package com.gseven
@Grab(group='org.apache.commons', module='commons-compress', version='1.21')

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

// Function to extract .tgz file
def extractTgz(String tgzFilePath, String outputDirPath) {
    File inputFile = new File(tgzFilePath)
    File outputDir = new File(outputDirPath)

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
            def outFile = Paths.get(outputDirPath, entry.name).toFile()

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

// Usage example
def tgzFilePath = "/path/to/your-archive.tgz"
def outputDirPath = "/path/to/output/directory"
extractTgz(tgzFilePath, outputDirPath)
