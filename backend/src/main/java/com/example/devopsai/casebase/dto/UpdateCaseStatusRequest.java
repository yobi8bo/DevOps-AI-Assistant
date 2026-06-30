package com.example.devopsai.casebase.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCaseStatusRequest(@NotBlank String status) {
}
