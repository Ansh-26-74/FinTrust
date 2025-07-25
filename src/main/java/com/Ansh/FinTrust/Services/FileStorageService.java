package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.FileInfo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileStorageService {

    public String uploadFile(MultipartFile file, String username) throws IOException;
    InputStreamResource downloadFile(String filename, String username) throws Exception;
    public List<FileInfo> listFiles(String username);
    public List<FileInfo> listAllFiles() ;

}
