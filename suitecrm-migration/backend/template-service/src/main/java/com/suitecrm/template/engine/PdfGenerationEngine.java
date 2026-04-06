package com.suitecrm.template.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationEngine {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$([a-zA-Z_][a-zA-Z0-9_]*)");

    public byte[] generatePdf(String templateContent, Map<String, Object> variables) {
        // Merge template with variables
        String mergedContent = mergeTemplate(templateContent, variables);

        // Convert HTML to a simple PDF representation
        // In production, integrate with OpenPDF, iText, or wkhtmltopdf
        return convertHtmlToPdf(mergedContent);
    }

    public String mergeTemplate(String template, Map<String, Object> variables) {
        if (template == null) return "";
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = variables.get(varName);
            matcher.appendReplacement(result, value != null ? Matcher.quoteReplacement(String.valueOf(value)) : "");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private byte[] convertHtmlToPdf(String html) {
        // Placeholder: returns HTML bytes
        // In production, use a library like OpenPDF, Flying Saucer, or wkhtmltopdf
        log.info("Generating PDF from HTML template ({} chars)", html.length());
        return html.getBytes(StandardCharsets.UTF_8);
    }
}
