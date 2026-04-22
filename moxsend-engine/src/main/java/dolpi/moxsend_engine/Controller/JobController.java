package dolpi.moxsend_engine.Controller;

import dolpi.moxsend_engine.DTO.CsvRow;
import dolpi.moxsend_engine.Model.Job;
import dolpi.moxsend_engine.Model.Result;
import dolpi.moxsend_engine.Repository.JobRepository;
import dolpi.moxsend_engine.Repository.ResultRepository;
import dolpi.moxsend_engine.Service.CsvProcessingService;
import lombok.extern.slf4j.Slf4j;
import dolpi.moxsend_engine.Exception.ResourcesNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class JobController {

    @Autowired
    private CsvProcessingService csvProcessingService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ResultRepository resultRepository;

    // CSV Upload API Call
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            // Check Validate file
            if (file.isEmpty()) {
                throw new ResourcesNotFound("File Is Null ");
            }

            if (!file.getOriginalFilename().endsWith(".csv")) {
                throw new ResourcesNotFound("Allowed Only CSV file!");
            }

            // Pass CSV
            List<CsvRow> rows = csvProcessingService.parseCsv(file);

            // Create Job
            Job job = Job.builder()
                    .status("PENDING")
                    .totalRows(rows.size())
                    .successCount(0)
                    .failedCount(0)
                    .createdAt(LocalDateTime.now())
                    .build();

            Job savedJob = jobRepository.save(job);

            // Run Process In The Backgroud
            csvProcessingService.processRows(savedJob.getId(), rows);

            //return data
            return ResponseEntity.ok(Map.of(
                    "jobId", savedJob.getId(),
                    "status", "PENDING",
                    "totalRows", rows.size(),
                    "message", "Processing in background!"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Result fetch API
    @GetMapping("/result/{jobId}")
    public ResponseEntity<?> getResult(@PathVariable String jobId) {
        // Job status
        Job job = jobRepository.findById(jobId)
                .orElse(null);

        if (job == null) {
            throw new ResourcesNotFound("Not Found");
        }

        // Results
        List<Result> results = resultRepository.findByJobId(jobId);

        return ResponseEntity.ok(Map.of(
                "jobId", jobId,
                "status", job.getStatus(),
                "totalRows", job.getTotalRows(),
                "successCount", job.getSuccessCount(),
                "failedCount", job.getFailedCount(),
                "results", results
        ));
    }
}