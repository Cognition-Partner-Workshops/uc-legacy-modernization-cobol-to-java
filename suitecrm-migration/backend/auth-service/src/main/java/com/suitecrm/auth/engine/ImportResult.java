package com.suitecrm.auth.engine;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ImportResult {
    private String module;
    private int totalRows;
    private int importedRows;
    private int errorRows;
    private List<String> errors;
    private List<Map<String, Object>> records;
}
