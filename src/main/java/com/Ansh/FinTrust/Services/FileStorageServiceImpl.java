package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.FileInfo;
import com.Ansh.FinTrust.Entities.CommonUser;
import com.Ansh.FinTrust.Repositories.AdminRepo;
import com.Ansh.FinTrust.Repositories.UserRepo;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final GridFsTemplate gridFsTemplate;
    private final UserRepo userRepo;
    private final AdminRepo adminRepo;
    private final EmailService emailService;
    private final SessionPinService sessionPinService;

    @Override
    public CommonUser findUserOrAdmin(String username) throws Exception {
        return userRepo.findByUsername(username)
                .<CommonUser>map(user -> user)
                .or(() -> adminRepo.findByUsername(username).map(admin -> admin))
                .orElseThrow(() -> new Exception("User or Admin not found"));
    }

    @Override
    public String uploadFile(MultipartFile file, String username) throws IOException {
        var metadata = new Document();
        metadata.put("username", username);
        metadata.put("uploadDate", new Date());
        metadata.put("originalName", file.getOriginalFilename());

        ObjectId fileId = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                metadata
        );

        return fileId.toString();
    }

    @Override
    public InputStreamResource downloadFile(String filename, String username, String sessionPin) throws Exception {
        CommonUser user = findUserOrAdmin(username);

        GridFSFile file = gridFsTemplate.findOne(
                Query.query(Criteria.where("filename").is(filename))
        );
        if (file == null) {
            throw new Exception("File not found");
        }

        String fileOwner = file.getMetadata().getString("username");

        if (!sessionPinService.validatePin(username, sessionPin)) {
            throw new Exception("Invalid or expired session PIN");
        }

        if (!fileOwner.equals(username)) {
            throw new Exception("Access denied.");
        }

        GridFsResource resource = gridFsTemplate.getResource(file);
        return new InputStreamResource(resource.getInputStream());
    }

    @Override
    public List<FileInfo> listMyFiles() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<FileInfo> files = new ArrayList<>();

        GridFSFindIterable results = gridFsTemplate.find(
                Query.query(Criteria.where("metadata.username").is(username))
        );

        for (GridFSFile file : results) {
            files.add(new FileInfo(
                    file.getFilename(),
                    file.getLength(),
                    file.getUploadDate(),
                    username
            ));
        }

        return files;
    }

    public GridFSFile viewFileByFilename(String filename, String username, String sessionPin) throws Exception {

        if (!sessionPinService.validatePin(username, sessionPin)) {
            throw new Exception("Invalid or expired session PIN");
        }

        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("filename").is(filename)));

        if (file == null) {
            throw new Exception("File not found");
        }

        if (!file.getMetadata().getString("username").equals(username)) {
            throw new Exception("Access denied");
        }

        return file;
    }
}
