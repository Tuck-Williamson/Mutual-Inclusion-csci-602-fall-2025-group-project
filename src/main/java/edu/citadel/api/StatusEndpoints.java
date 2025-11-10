package edu.citadel.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint for determining the status and API information.
 */
@RestController
@RequestMapping("/server")
public class StatusEndpoints {

    @Value("${info.app.name}")
    private String applicationName;

    @Value("${info.app.description}")
    private String applicationDescription;

    @Value("${info.app.version}")
    private String applicationVersion;

    private final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    @GetMapping(value = "health", produces = MediaType.APPLICATION_JSON_VALUE)
    public String health() throws JsonProcessingException {
        Map<String, String> healthStatus = new HashMap<>();
        healthStatus.put("status", "ok");
        return objectWriter.writeValueAsString(healthStatus);
    }

    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_VALUE)
    public String info() throws JsonProcessingException {
        Map<String, String> info = new HashMap<>();
        info.put("name", applicationName);
        info.put("version", applicationVersion);
        info.put("description", applicationDescription);
        return objectWriter.writeValueAsString(info);
    }

    @GetMapping("status")
    public String status(){
        return "Hello World";
    }

    // Additional redirect for the Swagger UI
    @Hidden
    @GetMapping("dev")
    public ResponseEntity<?> redirectToSwaggerUI() {
        return ResponseEntity.status(302)
                .header("Location", "/swagger-ui/index.html")
                .build();
    }
}


