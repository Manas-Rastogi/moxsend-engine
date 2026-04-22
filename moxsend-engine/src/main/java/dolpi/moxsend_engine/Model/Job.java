package dolpi.moxsend_engine.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    private String id;

    private String status;        // PENDING and PROCESSING and COMPLETED and FAILED
    private int totalRows;
    private int successCount;
    private int failedCount;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}