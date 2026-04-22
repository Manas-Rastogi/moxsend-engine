package dolpi.moxsend_engine.Service;

import dolpi.moxsend_engine.DTO.CsvRow;
import dolpi.moxsend_engine.DTO.GeneratedContent;
import dolpi.moxsend_engine.Model.Result;
import dolpi.moxsend_engine.Repository.JobRepository;
import dolpi.moxsend_engine.Repository.ResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CsvProcessingService {

    @Autowired
    private GroqService groqService;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private JobRepository jobRepository;

    public List<CsvRow> parseCsv(MultipartFile file) throws Exception {
        List<CsvRow> rows = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()));

        String line;
        boolean firstLine = true;

        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }

            String[] parts = line.split(",");

            if (parts.length < 4) continue;

            rows.add(CsvRow.builder()
                    .name(parts[0].trim())
                    .company(parts[1].trim())
                    .industry(parts[2].trim())
                    .city(parts[3].trim())
                    .build());
        }

        if (rows.isEmpty()) {
            throw new RuntimeException("CSV empty hai ya format galat hai!");
        }

        return rows;
    }

    @Async("llmTaskExecutor")
    public void processRows(String jobId, List<CsvRow> rows) {
        log.info("Processing shuru — JobId: {}, Rows: {}", jobId, rows.size());

        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus("PROCESSING");
            jobRepository.save(job);
        });

        int success = 0;
        int failed = 0;

        for (CsvRow row : rows) {
            try {
                GeneratedContent content = groqService.generate(
                        row.getName(),
                        row.getCompany(),
                        row.getIndustry(),
                        row.getCity()
                );

                resultRepository.save(Result.builder()
                        .jobId(jobId)
                        .name(row.getName())
                        .company(row.getCompany())
                        .industry(row.getIndustry())
                        .city(row.getCity())
                        .openingLine(content.getOpeningLine())
                        .subject1(content.getSubject1())
                        .subject2(content.getSubject2())
                        .status("SUCCESS")
                        .createdAt(LocalDateTime.now())
                        .build());

                success++;
                log.info("✅ Done: {}", row.getName());

            } catch (Exception e) {
                resultRepository.save(Result.builder()
                        .jobId(jobId)
                        .name(row.getName())
                        .status("FAILED")
                        .error(e.getMessage())
                        .createdAt(LocalDateTime.now())
                        .build());

                failed++;
                log.error("Failed: {} — {}", row.getName(), e.getMessage());
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        int finalSuccess = success;
        int finalFailed = failed;

        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus("COMPLETED");
            job.setSuccessCount(finalSuccess);
            job.setFailedCount(finalFailed);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            log.info("COMPLETED — Success: {}, Failed: {}", finalSuccess, finalFailed);
        });
    }
}