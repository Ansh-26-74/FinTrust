package com.Ansh.FinTrust.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class AdminRequest {

    private String requestId;
    private String adminUsername;
    private String filename;
    private String operation;
    private Date requestedAt;

}
