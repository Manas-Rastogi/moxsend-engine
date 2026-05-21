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

    @Value("${groq.api.key:${GROQ_API_KEY:}}")
    private String apiKey;

   @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
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
               Write cold email content for this B2B lead:

                - First Name: %s
                - Company: %s
                - Industry: %s
                - City: %s

                Deliverables:

                1. openingLine
                   - Exactly 1 sentence (max 20 words)
                   - Reference their industry or city in a natural, non-forced way
                   - Must feel personally researched, not templated
                   - Never start with "I", "We", or the prospect's name
                   - No compliments, no "I came across your profile"

                2. subject1 (Curiosity-based)
                   - Under 7 words
                   - Creates intrigue without being clickbait
                   - Lowercase preferred (feels more human)
                   - No emojis, no question marks

                3. subject2 (Value-based)
                   - Under 7 words
                   - Specific to their industry or role
                   - Implies a clear benefit or outcome
                   - No emojis

                Return ONLY this JSON structure, nothing else:
                {
                  "openingLine": "...",
                  "subject1": "...",
                  "subject2": "..."
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
