package dolpi.moxsend_engine.Service;

import dolpi.moxsend_engine.DTO.GeneratedContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GroqService {

    @Value("${GROQ_API_KEY}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final WebClient webClient = WebClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeneratedContent generate(String name, String company,
                                     String industry, String city) {
        log.debug("Generating for: {} - {}", name, company);
        String prompt = buildPrompt(name, company, industry, city);
        String rawResponse = callGroq(prompt);
        return parseResponse(rawResponse, name);
    }

    private String buildPrompt(String name, String company,
                               String industry, String city) {
        return """
                You are a B2B email copywriter.
                Generate content for this lead:

                Name: %s
                Company: %s
                Industry: %s
                City: %s

                Return ONLY valid JSON, no extra text:
                {
                  "openingLine": "personalized opening line",
                  "subject1": "first subject line",
                  "subject2": "second subject line"
                }
                """.formatted(name, company, industry, city);
    }

    private String callGroq(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", "llama-3.1-8b-instant",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.7
        );

        try {
            Map response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            // Groq response parse karo
            List<Map> choices = (List<Map>) response.get("choices");
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Groq call failed: {}", e.getMessage());
            throw new RuntimeException("Groq error: " + e.getMessage());
        }
    }

    private GeneratedContent parseResponse(String rawText, String name) {
        try {
            String clean = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            // JSON part nikalo
            int start = clean.indexOf("{");
            int end = clean.lastIndexOf("}") + 1;
            if (start != -1 && end > start) {
                clean = clean.substring(start, end);
            }

            return objectMapper.readValue(clean, GeneratedContent.class);

        } catch (Exception e) {
            log.error("Parse failed for: {}", name);
            return GeneratedContent.builder()
                    .openingLine("Hi " + name + ", I came across your work and wanted to connect.")
                    .subject1("Quick question for " + name)
                    .subject2("Thought this might be valuable for you")
                    .build();
        }
    }
}
