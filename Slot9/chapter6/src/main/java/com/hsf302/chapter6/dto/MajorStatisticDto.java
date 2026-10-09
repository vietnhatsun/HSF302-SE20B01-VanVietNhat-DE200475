package com.hsf302.chapter6.dto;

public record MajorStatisticDto(
        String major,
        Long totalStudents,
        Double avgGpa,
        Double maxGpa,
        Double minGpa
) {
}
