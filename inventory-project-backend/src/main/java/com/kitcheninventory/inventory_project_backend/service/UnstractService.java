// src/main/java/com/kitcheninventory/inventory_project_backend/service/UnstractService.java
package com.kitcheninventory.inventory_project_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitcheninventory.inventory_project_backend.dto.UnstractItemDTO;
import com.kitcheninventory.inventory_project_backend.dto.UnstractResultDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UnstractService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${unstract.api-key}")
    private String apiKey;

    @Value("${unstract.debug-save:false}")
    private boolean debugSave;

    @Value("${unstract.debug-path:./unstract_debug}")
    private String debugPath;

    @Value("${unstract.base-url:https://us-central.unstract.com/deployment/api/org_hXGM7c45ddbBIVXU/InvProjParseReceipt/}")
    private String unstractURLString;

    public UnstractService() {
        //wrap the default factory so bodies are buffered for logging
        SimpleClientHttpRequestFactory baseFactory = new SimpleClientHttpRequestFactory();
        BufferingClientHttpRequestFactory bufferingFactory =
            new BufferingClientHttpRequestFactory(baseFactory);

        this.restTemplate = new RestTemplate(bufferingFactory);
        this.restTemplate.getInterceptors().add(new LoggingRequestInterceptor());
    }

    /**
     * Uploads the given PDF to Unstract and immediately returns
     * whatever JSON Unstract responds with.
     */
    public UnstractResultDTO uploadAndParse(MultipartFile multipartFile) throws Exception {
        
        String contentType = multipartFile.getContentType();
        if (contentType == null || !MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Only PDF files are accepted.");
        }

        
        ByteArrayResource byteResource = new ByteArrayResource(multipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return multipartFile.getOriginalFilename();
            }
        };

        
        LinkedMultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
        body.add("files", byteResource);
        body.add("timeout", "300");
        body.add("include_metadata", "False");
        body.add("include_metrics", "False");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String,Object>> requestEntity =
            new HttpEntity<>(body, headers);

        
        ResponseEntity<String> response =
            restTemplate.postForEntity(unstractURLString, requestEntity, String.class);

        
        String rawJson = response.getBody();
        if (debugSave && rawJson != null) {
            saveDebugFile(multipartFile.getOriginalFilename(), rawJson);
        }

        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode output = root
            .path("message")
            .path("result").get(0)
            .path("result")
            .path("output")
            .path("DB systems inventory proj_1"); //Should probably be in application.properties!
    

        List<UnstractItemDTO> items = StreamSupport.stream(output.path("Items").spliterator(), false)
            .map(item -> new UnstractItemDTO(
                normalize(item.path("Brand").asText()),
                item.path("Cost").asText(),
                item.path("ItemName").asText(),
                item.path("ProductCode").asText(),
                item.path("Quantity").asText(),
                normalize(item.path("Units").asText())
            ))
            .collect(Collectors.toList());

        return new UnstractResultDTO(
            output.path("Date").asText(),
            output.path("StoreName").asText(),
            output.path("Tax").asText(),
            output.path("TotalCost").asText(),
            items
        );
    }

    /**
     * Turn “!” or “?” or null into an empty‐string, otherwise trim
     */
    private String normalize(String val) {
        if (val == null || val.equals("!") || val.equals("?")) {
            return "";
        }
        return val.trim();
    }

    private void saveDebugFile(String identifier, String json) throws Exception {
        if (json == null) {
            return;
        }
        Files.createDirectories(Paths.get(debugPath));
        try (FileWriter writer = new FileWriter(new File(debugPath, identifier + ".json"))) {
            writer.write(json);
        }
    }
}
