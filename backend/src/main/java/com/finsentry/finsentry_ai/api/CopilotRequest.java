package com.finsentry.finsentry_ai.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopilotRequest {
    private String question;
    private Long caseId;
}
