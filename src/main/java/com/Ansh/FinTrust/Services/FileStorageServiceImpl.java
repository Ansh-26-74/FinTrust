package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.FileInfo;
import com.Ansh.FinTrust.Entities.AdminAccessRequest;
import com.Ansh.FinTrust.Entities.User;
import com.Ansh.FinTrust.Repositories.AdminAccessRequestRepo;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService{

    private final GridFsTemplate gridFsTemplate;
    private final UserRepo userRepo;
    private final AdminRequestService adminRequestService;
    private final AdminAccessRequestRepo adminAccessRequestRepo;
    private final EmailService emailService;

    @Override
    public String uploadFile(MultipartFile file, String username) throws IOException{

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
        public InputStreamResource downloadFile(String filename, String username) throws Exception {

            Optional<User> userOpt = userRepo.findByUsername(username);
            if (userOpt.isEmpty()) throw new Exception("User not found");

            User user = userOpt.get();

            GridFSFile file = gridFsTemplate.findOne(
                    Query.query(Criteria.where("filename").is(filename))
            );

            if (file == null) {
                throw new Exception("File not found");
            }

            String fileOwner = file.getMetadata().getString("username");

            if(user.getRole().contains("ROLE_ADMIN")) {
                if (!fileOwner.equals(username)) {
                    boolean isApproved = adminRequestService.isAccessApproved(
                            username, fileOwner, filename, "DOWNLOAD"
                    );

                    if (!isApproved) {
                        Optional<AdminAccessRequest> existing = adminAccessRequestRepo
                                .findByAdminUsernameAndTargetUsernameAndFilenameAndOperationAndStatus(
                                        username, fileOwner, filename, "DOWNLOAD", "PENDING"
                                );

                        if (existing.isEmpty()) {
                            AdminAccessRequest request = new AdminAccessRequest();
                            request.setAdminUsername(username);
                            request.setTargetUsername(fileOwner);
                            request.setFilename(filename);
                            request.setOperation("DOWNLOAD");
                            request.setStatus("PENDING");
                            request.setRequestedAt(new Date());

                            adminAccessRequestRepo.save(request);

                            User targetUser = userRepo.findByUsername(fileOwner)
                                    .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

                            emailService.sendEmail(
                                    targetUser.getEmail(),
                                    "Admin Access Request - FinTrust",
                                    "Admin <b>" + username + "</b> is requesting to <b>" + "DOWNLOAD" +
                                            "</b> your files. Please login and approve or reject the request."
                            );
                        }

                        throw new Exception("Access denied. User approval required.");
                    }
                }
            }else {
                    if (!fileOwner.equals(username)) {
                        throw new Exception("Access denied. You can only download your own files.");
                    }
            }

            GridFsResource resource = gridFsTemplate.getResource(file); // Makes the chunks of file in DB back to og
            return new InputStreamResource(resource.getInputStream());
        }


    public List<FileInfo> listFiles(String username) {
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

    @Override
    public List<FileInfo> listAllFiles() {
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        List<FileInfo> files = new ArrayList<>();

        try {
            List<String> approvedUsernames = adminRequestService.getUsersThatApprovedAccess(
                    adminUsername, "LIST"
            );

            GridFSFindIterable results = gridFsTemplate.find(
                    Query.query(Criteria.where("metadata.username").in(approvedUsernames))
            );

            for (GridFSFile file : results) {
                String uploadedBy = "unknown";
                if (file.getMetadata() != null && file.getMetadata().getString("username") != null) {
                    uploadedBy = file.getMetadata().getString("username");
                }

                files.add(new FileInfo(
                        file.getFilename(),
                        file.getLength(),
                        file.getUploadDate(),
                        uploadedBy
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch accessible files: " + e.getMessage(), e);
        }

        return files;
    }

}
