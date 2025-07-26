package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.FileInfo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileStorageService {

    String uploadFile(MultipartFile file, String pin, String username) throws IOException;
    InputStreamResource downloadFile(String filename, String username, String pin) throws Exception;
    public List<FileInfo> listFiles(String username);
    public List<FileInfo> listAllFiles() ;

}
