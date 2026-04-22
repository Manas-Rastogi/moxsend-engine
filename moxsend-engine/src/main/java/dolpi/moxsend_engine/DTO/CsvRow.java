package dolpi.moxsend_engine.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvRow {
    private String name;
    private String company;
    private String industry;
    private String city;
}