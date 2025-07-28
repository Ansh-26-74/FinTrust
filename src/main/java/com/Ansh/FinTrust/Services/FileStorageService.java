package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.FileInfo;
import com.Ansh.FinTrust.Entities.CommonUser;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileStorageService {

    CommonUser findUserOrAdmin(String username) throws Exception;
    String uploadFile(MultipartFile file, String username) throws IOException;
    InputStreamResource downloadFile(String filename, String username, String pin) throws Exception;
    GridFSFile viewFileByFilename(String filename, String username, String sessionPin) throws Exception;
    public List<FileInfo> listMyFiles() ;
    void deleteFile(String filename, String pin, String username) throws Exception;

}
