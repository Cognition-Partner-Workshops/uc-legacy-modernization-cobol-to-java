package com.suitecrm.auth.engine;

import com.suitecrm.auth.entity.ImportMap;
import com.suitecrm.auth.repository.ImportMapRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportEngine {

    private final ImportMapRepository importMapRepository;
    private final ObjectMapper objectMapper;

    public ImportResult importCsv(MultipartFile file, UUID importMapId) throws Exception {
        ImportMap importMap = importMapRepository.findById(importMapId)
            .orElseThrow(() -> new RuntimeException("Import map not found: " + importMapId));

        String delimiter = importMap.getDelimiter() != null ? importMap.getDelimiter() : ",";
        String enclosure = importMap.getEnclosure() != null ? importMap.getEnclosure() : "\"";
        boolean hasHeader = Boolean.TRUE.equals(importMap.getHasHeader());

        List<Map<String, Object>> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNum = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = null;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                String[] values = parseCsvLine(line, delimiter, enclosure);
                if (lineNum == 1 && hasHeader) {
                    headers = values;
                    continue;
                }
                if (headers != null && values.length == headers.length) {
                    Map<String, Object> record = new LinkedHashMap<>();
                    for (int i = 0; i < headers.length; i++) {
                        record.put(headers[i].trim(), values[i].trim());
                    }
                    records.add(record);
                } else {
                    errors.add("Line " + lineNum + ": column count mismatch");
                }
            }
        }

        ImportResult result = new ImportResult();
        result.setModule(importMap.getModule());
        result.setTotalRows(records.size());
        result.setImportedRows(records.size());
        result.setErrorRows(errors.size());
        result.setErrors(errors);
        result.setRecords(records);
        return result;
    }

    public String exportCsv(String module, List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        List<String> headers = new ArrayList<>(records.get(0).keySet());
        sb.append(String.join(",", headers)).append("\n");
        for (Map<String, Object> record : records) {
            List<String> values = new ArrayList<>();
            for (String header : headers) {
                Object val = record.get(header);
                String str = val != null ? val.toString().replace("\"", "\"\"") : "";
                values.add("\"" + str + "\"");
            }
            sb.append(String.join(",", values)).append("\n");
        }
        return sb.toString();
    }

    private String[] parseCsvLine(String line, String delimiter, String enclosure) {
        // Simple CSV parser - handles quoted fields
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == enclosure.charAt(0)) {
                inQuotes = !inQuotes;
            } else if (String.valueOf(c).equals(delimiter) && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
