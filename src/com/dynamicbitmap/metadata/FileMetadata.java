package com.dynamicbitmap.metadata;

import java.io.Serializable;

public class FileMetadata implements Serializable {

    private String fileId;

    private String fileName;

    private int totalChunks;

    private long originalSize;

    //  tamaño cifrado real
    private long encryptedSize;

    public FileMetadata(
            String fileId,
            String fileName,
            int totalChunks,
            long originalSize,
            long encryptedSize
    ) {

        this.fileId = fileId;

        this.fileName = fileName;

        this.totalChunks = totalChunks;

        this.originalSize = originalSize;

        this.encryptedSize = encryptedSize;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public long getOriginalSize() {
        return originalSize;
    }

    public long getEncryptedSize() {
        return encryptedSize;
    }
}