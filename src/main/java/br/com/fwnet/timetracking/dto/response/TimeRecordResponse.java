package br.com.fwnet.timetracking.dto.response;

import br.com.fwnet.timetracking.enums.TimeRecordType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TimeRecordResponse(
        UUID id,
        LocalDate workDate,
        TimeRecordType recordType,
        OffsetDateTime recordedAt,
        String source,
        OffsetDateTime createdAt
) {
}