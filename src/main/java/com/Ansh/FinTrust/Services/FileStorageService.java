package com.Ansh.FinTrust.Services;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    public String uploadFile(MultipartFile file, String username) throws IOException;
    InputStreamResource downloadFile(String filename, String username) throws Exception;
}
