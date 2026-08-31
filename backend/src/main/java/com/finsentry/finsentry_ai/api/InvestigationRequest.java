package com.finsentry.finsentry_ai.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class InvestigationRequest {
    private Long transactionId;
    private Long caseId;

}
