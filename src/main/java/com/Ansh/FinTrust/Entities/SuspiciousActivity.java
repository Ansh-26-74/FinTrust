package com.Ansh.FinTrust.Entities;

import com.Ansh.FinTrust.DTO.SuspiciousEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "suspicious_activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousActivity {

    @Id
    private String id;

    private String userId;

    private SuspiciousEventType eventType;

    private LocalDateTime timestamp;

    private String details;

}

