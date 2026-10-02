package com.hsf302.ch4.dto;

public record StudentCreditDTO(String studentCode, String fullName,
                               Long courseCount, Long totalCredits) {
}
