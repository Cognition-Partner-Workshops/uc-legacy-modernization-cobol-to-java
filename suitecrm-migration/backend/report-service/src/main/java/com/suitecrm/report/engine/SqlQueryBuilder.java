package com.suitecrm.report.engine;

import com.suitecrm.report.entity.Report;
import com.suitecrm.report.entity.ReportCondition;
import com.suitecrm.report.entity.ReportField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SqlQueryBuilder {

    public String buildQuery(Report report, List<ReportField> fields, List<ReportCondition> conditions) {
        StringBuilder sql = new StringBuilder("SELECT ");

        // Build SELECT clause
        String selectClause = fields.stream()
            .filter(f -> Boolean.TRUE.equals(f.getDisplay()))
            .map(this::buildFieldExpression)
            .collect(Collectors.joining(", "));
        sql.append(selectClause.isEmpty() ? "*" : selectClause);

        // Build FROM clause
        String tableName = moduleToTable(report.getReportModule());
        sql.append(" FROM ").append(tableName).append(" t");

        // Build WHERE clause
        if (!conditions.isEmpty()) {
            sql.append(" WHERE t.deleted = false");
            for (ReportCondition condition : conditions) {
                String logicOp = condition.getLogicOp() != null ? condition.getLogicOp() : "AND";
                sql.append(" ").append(logicOp).append(" ");
                sql.append(buildConditionExpression(condition));
            }
        } else {
            sql.append(" WHERE t.deleted = false");
        }

        // Build GROUP BY clause
        List<ReportField> groupFields = fields.stream()
            .filter(f -> Boolean.TRUE.equals(f.getGroupBy()))
            .toList();
        if (!groupFields.isEmpty()) {
            String groupClause = groupFields.stream()
                .map(f -> "t." + f.getField())
                .collect(Collectors.joining(", "));
            sql.append(" GROUP BY ").append(groupClause);
        }

        // Build ORDER BY clause
        List<ReportField> sortFields = fields.stream()
            .filter(f -> f.getSortBy() != null && !f.getSortBy().isEmpty())
            .toList();
        if (!sortFields.isEmpty()) {
            String orderClause = sortFields.stream()
                .map(f -> "t." + f.getField() + " " + f.getSortBy())
                .collect(Collectors.joining(", "));
            sql.append(" ORDER BY ").append(orderClause);
        }

        return sql.toString();
    }

    private String buildFieldExpression(ReportField field) {
        String expr = "t." + field.getField();
        if (field.getFieldFunction() != null && !field.getFieldFunction().isEmpty()) {
            expr = field.getFieldFunction() + "(" + expr + ")";
        }
        if (field.getLabel() != null) {
            expr += " AS \"" + field.getLabel() + "\"";
        }
        return expr;
    }

    private String buildConditionExpression(ReportCondition condition) {
        String field = "t." + condition.getField();
        String operator = condition.getOperator();
        String value = condition.getValue();

        return switch (operator != null ? operator : "equals") {
            case "equals" -> field + " = '" + escapeValue(value) + "'";
            case "not_equals" -> field + " != '" + escapeValue(value) + "'";
            case "contains" -> field + " LIKE '%" + escapeValue(value) + "%'";
            case "starts_with" -> field + " LIKE '" + escapeValue(value) + "%'";
            case "ends_with" -> field + " LIKE '%" + escapeValue(value) + "'";
            case "is_empty" -> "(" + field + " IS NULL OR " + field + " = '')";
            case "is_not_empty" -> field + " IS NOT NULL AND " + field + " != ''";
            case "greater_than" -> field + " > '" + escapeValue(value) + "'";
            case "less_than" -> field + " < '" + escapeValue(value) + "'";
            case "between" -> field + " BETWEEN '" + escapeValue(value.split(",")[0].trim()) + "' AND '" + escapeValue(value.split(",")[1].trim()) + "'";
            default -> field + " = '" + escapeValue(value) + "'";
        };
    }

    private String escapeValue(String value) {
        return value != null ? value.replace("'", "''") : "";
    }

    private String moduleToTable(String module) {
        if (module == null) return "records";
        return switch (module) {
            case "Accounts" -> "account_schema.accounts";
            case "Contacts" -> "contact_schema.contacts";
            case "Leads" -> "lead_schema.leads";
            case "Opportunities" -> "opportunity_schema.opportunities";
            case "Cases" -> "case_schema.cases";
            case "Campaigns" -> "campaign_schema.campaigns";
            case "Calls" -> "activity_schema.calls";
            case "Meetings" -> "activity_schema.meetings";
            case "Tasks" -> "activity_schema.tasks";
            case "Bugs" -> "case_schema.bugs";
            default -> module.toLowerCase();
        };
    }
}
