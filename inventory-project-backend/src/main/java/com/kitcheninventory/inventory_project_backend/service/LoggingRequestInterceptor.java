package com.kitcheninventory.inventory_project_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(
            org.springframework.http.HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(org.springframework.http.HttpRequest request, byte[] body) {
        logger.info("=== REQUEST BEGIN ===");
        logger.info("URI    : {}", request.getURI());
        logger.info("Method : {}", request.getMethod());
        logger.info("Headers: {}", request.getHeaders());
        // skip binary body dump
        // logger.info("Request body:\n{}", new String(body, StandardCharsets.UTF_8));
        logger.info("=== REQUEST END ===");
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        String body = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));

        logger.info("=== RESPONSE BEGIN ===");
        logger.info("Status code : {}", response.getStatusCode());
        logger.info("Status text : {}", response.getStatusText());
        logger.info("Headers     : {}", response.getHeaders());
        logger.info("Response body:\n{}", body);
        logger.info("=== RESPONSE END ===");
    }
}
