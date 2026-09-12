package br.com.fwnet.timetracking.dto.response;

import br.com.fwnet.timetracking.enums.TimeRecordType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminTimeRecordResponse(
        UUID id,
        UUID userId,
        String userFullName,
        String userEmail,
        LocalDate workDate,
        TimeRecordType recordType,
        OffsetDateTime recordedAt,
        String source,
        OffsetDateTime createdAt
) {
}