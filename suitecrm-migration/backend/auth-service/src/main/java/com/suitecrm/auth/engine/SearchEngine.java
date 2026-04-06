package com.suitecrm.auth.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchEngine {

    @Value("${opensearch.url:http://localhost:9200}")
    private String opensearchUrl;

    private final RestTemplate restTemplate;

    public Map<String, Object> search(String query, String module, int from, int size) {
        String index = module != null ? module.toLowerCase() : "_all";
        String url = opensearchUrl + "/" + index + "/_search";

        Map<String, Object> searchBody = new LinkedHashMap<>();
        searchBody.put("from", from);
        searchBody.put("size", size);

        Map<String, Object> queryBody = new LinkedHashMap<>();
        Map<String, Object> multiMatch = new LinkedHashMap<>();
        multiMatch.put("query", query);
        multiMatch.put("fields", List.of("name^3", "description", "email1", "phone_work", "account_name"));
        multiMatch.put("type", "best_fields");
        multiMatch.put("fuzziness", "AUTO");
        queryBody.put("multi_match", multiMatch);
        searchBody.put("query", queryBody);

        Map<String, Object> highlight = new LinkedHashMap<>();
        highlight.put("fields", Map.of("name", Map.of(), "description", Map.of(), "email1", Map.of()));
        searchBody.put("highlight", highlight);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(searchBody, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            return response != null ? response : Map.of();
        } catch (Exception e) {
            log.error("OpenSearch query failed: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    public void indexRecord(String module, UUID recordId, Map<String, Object> record) {
        String index = module.toLowerCase();
        String url = opensearchUrl + "/" + index + "/_doc/" + recordId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(record, headers);

        try {
            restTemplate.put(url, request);
            log.debug("Indexed record {}/{}", module, recordId);
        } catch (Exception e) {
            log.error("Failed to index record {}/{}: {}", module, recordId, e.getMessage());
        }
    }

    public void deleteRecord(String module, UUID recordId) {
        String index = module.toLowerCase();
        String url = opensearchUrl + "/" + index + "/_doc/" + recordId;

        try {
            restTemplate.delete(url);
        } catch (Exception e) {
            log.error("Failed to delete from index {}/{}: {}", module, recordId, e.getMessage());
        }
    }
}
