package com.suitecrm.workflow.engine;

import com.suitecrm.workflow.entity.WorkflowCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class ConditionEvaluator {

    public boolean evaluate(List<WorkflowCondition> conditions, Map<String, Object> record, Map<String, Object> previousRecord) {
        if (conditions == null || conditions.isEmpty()) return true;
        boolean result = true;
        for (WorkflowCondition condition : conditions) {
            boolean condResult = evaluateSingle(condition, record, previousRecord);
            result = result && condResult;
        }
        return result;
    }

    private boolean evaluateSingle(WorkflowCondition condition, Map<String, Object> record, Map<String, Object> previousRecord) {
        String field = condition.getField();
        String operator = condition.getOperator();
        String expectedValue = condition.getValue();
        Object actualValue = record.get(field);
        Object previousValue = previousRecord != null ? previousRecord.get(field) : null;

        return switch (operator != null ? operator : "") {
            case "equals" -> Objects.equals(String.valueOf(actualValue), expectedValue);
            case "not_equals" -> !Objects.equals(String.valueOf(actualValue), expectedValue);
            case "is_empty" -> actualValue == null || String.valueOf(actualValue).isEmpty();
            case "is_not_empty" -> actualValue != null && !String.valueOf(actualValue).isEmpty();
            case "contains" -> actualValue != null && String.valueOf(actualValue).contains(expectedValue);
            case "starts_with" -> actualValue != null && String.valueOf(actualValue).startsWith(expectedValue);
            case "ends_with" -> actualValue != null && String.valueOf(actualValue).endsWith(expectedValue);
            case "changed" -> !Objects.equals(actualValue, previousValue);
            case "not_changed" -> Objects.equals(actualValue, previousValue);
            case "greater_than" -> compareNumeric(actualValue, expectedValue) > 0;
            case "less_than" -> compareNumeric(actualValue, expectedValue) < 0;
            case "greater_than_or_equals" -> compareNumeric(actualValue, expectedValue) >= 0;
            case "less_than_or_equals" -> compareNumeric(actualValue, expectedValue) <= 0;
            default -> {
                log.warn("Unknown operator: {}", operator);
                yield true;
            }
        };
    }

    private int compareNumeric(Object actual, String expected) {
        try {
            double a = actual != null ? Double.parseDouble(String.valueOf(actual)) : 0;
            double b = expected != null ? Double.parseDouble(expected) : 0;
            return Double.compare(a, b);
        } catch (NumberFormatException e) {
            return String.valueOf(actual).compareTo(expected != null ? expected : "");
        }
    }
}
