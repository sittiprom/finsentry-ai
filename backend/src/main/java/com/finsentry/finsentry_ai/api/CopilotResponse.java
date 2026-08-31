package com.finsentry.finsentry_ai.api;

import java.util.List;

public record CopilotResponse(
        String answer,
        ResponseType responseType,
        TableData table,
        ReportData report
) {
    public enum ResponseType { TEXT, TABLE, REPORT }
    public record TableData(List<String> columns, List<List<String>> rows) {}
    public record ReportData(String title, List<Field> fields) {
        public record Field(String label, String value) {}
    }
}
