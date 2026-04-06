package com.suitecrm.workflow.engine;

import com.suitecrm.workflow.entity.WorkflowAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActionExecutor {

    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public void execute(WorkflowAction action, Map<String, Object> record) {
        String actionType = action.getAction();
        log.info("Executing workflow action: type={}, id={}", actionType, action.getId());

        switch (actionType != null ? actionType : "") {
            case "SetField" -> executeSetField(action, record);
            case "SendEmail" -> executeSendEmail(action, record);
            case "CreateRecord" -> executeCreateRecord(action, record);
            case "CalculateField" -> executeCalculateField(action, record);
            case "ModifyRecord" -> executeModifyRecord(action, record);
            default -> log.warn("Unknown action type: {}", actionType);
        }
    }

    private void executeSetField(WorkflowAction action, Map<String, Object> record) {
        try {
            Map<String, Object> params = parseParameters(action.getParameters());
            String fieldName = (String) params.get("field");
            Object fieldValue = params.get("value");
            if (fieldName != null) {
                record.put(fieldName, fieldValue);
                log.info("Set field {} = {}", fieldName, fieldValue);
            }
        } catch (Exception e) {
            log.error("SetField action failed", e);
        }
    }

    private void executeSendEmail(WorkflowAction action, Map<String, Object> record) {
        log.info("SendEmail action triggered for record: {}", record.get("id"));
        // Delegates to email-service via REST or message queue
    }

    private void executeCreateRecord(WorkflowAction action, Map<String, Object> record) {
        log.info("CreateRecord action triggered");
        // Creates a new record in the target module via REST
    }

    private void executeCalculateField(WorkflowAction action, Map<String, Object> record) {
        log.info("CalculateField action triggered");
    }

    private void executeModifyRecord(WorkflowAction action, Map<String, Object> record) {
        log.info("ModifyRecord action triggered");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseParameters(String parameters) {
        try {
            if (parameters == null || parameters.isBlank()) return Map.of();
            return objectMapper.readValue(parameters, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse action parameters", e);
            return Map.of();
        }
    }
}
