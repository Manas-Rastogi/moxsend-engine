package dolpi.moxsend_engine.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "jobId_idx", def = "{'jobId': 1}")
public class Result {

    @Id
    private String id;

    @Indexed
    private String jobId;
    private String name;
    private String company;
    private String industry;
    private String city;

    private String openingLine;
    private String subject1;
    private String subject2;

    private String status;   // SUCCESS and FAILED
    private String error;

    private LocalDateTime createdAt;
}