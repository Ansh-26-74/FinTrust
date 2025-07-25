package com.Ansh.FinTrust.DTO;

import lombok.Data;

import java.util.Date;

@Data
public class FileInfo {

    private String filename;
    private long size;
    private Date uploadDate;
    private String uploadedBy;

    public FileInfo(String filename, long size, Date uploadDate, String uploadedBy) {
        this.filename = filename;
        this.size = size;
        this.uploadDate = uploadDate;
        this.uploadedBy = uploadedBy;
    }

}
