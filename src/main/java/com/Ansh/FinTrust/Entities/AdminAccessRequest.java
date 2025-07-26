package com.Ansh.FinTrust.Entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "admin_requests")
public class AdminAccessRequest {

    @Id
    private String id;

    private String adminUsername;
    private String targetUsername;
    private String filename;
    private String operation;
    private String status;
    private Date requestedAt;
    private Date respondedAt;

}
