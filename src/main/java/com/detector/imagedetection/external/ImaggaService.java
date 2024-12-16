package com.detector.imagedetection.external;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ImaggaService {
    private final String endpoint;
    private final WebClient webClient;
    @Value("${imagga.api.key}")
    private String imaggaApiKeyString;

    @Value("${imagga.api.secret}")
    private String imaggaApiSecret;

    public ImaggaService(@Value("${imagga.api.url}") String endpoint,
    WebClient webClient) {
        this.endpoint = endpoint;
        this.webClient = webClient;
    }

    public ByteArrayInputStream getStsreamForImageUrl(String url) {
        String credentialsToEncode = imaggaApiKeyString + ":" + imaggaApiSecret;
        String basicAuth = Base64.getEncoder().encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8));
        String queryParameters = String.format("?image_url=%s", url);
        byte[] response = webClient.get()
        .uri(endpoint.concat(queryParameters))
        .headers(headers -> headers.setBasicAuth(basicAuth))
        .retrieve()
        .bodyToMono(byte[].class)
        .block();

        return new ByteArrayInputStream(response);
    }
}
