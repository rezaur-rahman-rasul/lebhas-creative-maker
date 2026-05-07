package com.lebhas.creativesaas.gateway.interfaces;

import com.lebhas.creativesaas.gateway.config.AuthGatewayProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

@RestController
public class AuthGatewayController {

    private static final Set<String> EXCLUDED_REQUEST_HEADERS = Set.of(
            HttpHeaders.HOST.toLowerCase(),
            HttpHeaders.CONTENT_LENGTH.toLowerCase(),
            HttpHeaders.CONNECTION.toLowerCase(),
            HttpHeaders.EXPECT.toLowerCase(),
            HttpHeaders.UPGRADE.toLowerCase()
    );

    private static final Set<String> EXCLUDED_RESPONSE_HEADERS = Set.of(
            HttpHeaders.CONTENT_LENGTH.toLowerCase(),
            HttpHeaders.CONNECTION.toLowerCase(),
            HttpHeaders.TRANSFER_ENCODING.toLowerCase()
    );

    private final HttpClient httpClient;
    private final AuthGatewayProperties properties;

    public AuthGatewayController(HttpClient httpClient, AuthGatewayProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
    }

    @PostMapping(path = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout"
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> forwardPost(HttpServletRequest request, @RequestBody byte[] body)
            throws IOException, InterruptedException {
        return forward(request, body);
    }

    @GetMapping("/api/v1/auth/me")
    public ResponseEntity<byte[]> forwardGet(HttpServletRequest request) throws IOException, InterruptedException {
        return forward(request, null);
    }

    private ResponseEntity<byte[]> forward(HttpServletRequest incomingRequest, byte[] body)
            throws IOException, InterruptedException {
        HttpRequest.Builder outboundRequest = HttpRequest.newBuilder()
                .uri(resolveTargetUri(incomingRequest))
                .method(
                        incomingRequest.getMethod(),
                        body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body));

        copyRequestHeaders(incomingRequest, outboundRequest);

        HttpResponse<byte[]> downstreamResponse = httpClient.send(
                outboundRequest.build(),
                HttpResponse.BodyHandlers.ofByteArray());

        return ResponseEntity
                .status(HttpStatusCode.valueOf(downstreamResponse.statusCode()))
                .headers(copyResponseHeaders(downstreamResponse))
                .body(downstreamResponse.body());
    }

    private URI resolveTargetUri(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String path = request.getRequestURI();
        if (queryString != null && !queryString.isBlank()) {
            path = path + "?" + queryString;
        }
        return properties.getBaseUrl().resolve(path);
    }

    private void copyRequestHeaders(HttpServletRequest incomingRequest, HttpRequest.Builder outboundRequest) {
        Enumeration<String> headerNames = incomingRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (EXCLUDED_REQUEST_HEADERS.contains(headerName.toLowerCase())) {
                continue;
            }
            Enumeration<String> headerValues = incomingRequest.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                outboundRequest.header(headerName, headerValues.nextElement());
            }
        }

        if (incomingRequest.getHeader(HttpHeaders.CONTENT_TYPE) == null) {
            outboundRequest.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        }
    }

    private HttpHeaders copyResponseHeaders(HttpResponse<byte[]> downstreamResponse) {
        HttpHeaders headers = new HttpHeaders();
        downstreamResponse.headers().map().forEach((name, values) -> {
            if (!EXCLUDED_RESPONSE_HEADERS.contains(name.toLowerCase())) {
                headers.put(name, List.copyOf(values));
            }
        });
        return headers;
    }
}
