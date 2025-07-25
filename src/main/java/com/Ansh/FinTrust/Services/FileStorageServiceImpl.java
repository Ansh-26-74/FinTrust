package com.Ansh.FinTrust.Services;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService{

    private final GridFsTemplate gridFsTemplate;

    @Override
    public String uploadFile(MultipartFile file, String username) throws IOException{

        var metadata = new org.bson.Document();
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
        GridFSFile file = gridFsTemplate.findOne(
                Query.query(Criteria.where("filename").is(filename).and("metadata.username").is(username))
        );

        if (file == null) {
            throw new Exception("File not found or access denied");
        }

        GridFsResource resource = gridFsTemplate.getResource(file); // Makes the chunks of file in DB back to og
        return new InputStreamResource(resource.getInputStream());
    }

    public GridFSFile getFileById(String id) {
        return gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(id))));
    }

    public GridFSFile getFileByUsername(String username) {
        return gridFsTemplate.findOne(Query.query(Criteria.where("metadata.username").is(username)));
    }
}
