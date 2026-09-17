package br.com.fwnet.timetracking.dto.response;

import br.com.fwnet.timetracking.enums.TimeRecordType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminTimeRecordCorrectionResponse(
        UUID id,
        UUID timeRecordId,
        UUID analystId,
        String analystFullName,
        String analystEmail,
        TimeRecordType recordType,
        LocalDate previousWorkDate,
        LocalDate newWorkDate,
        OffsetDateTime previousRecordedAt,
        OffsetDateTime newRecordedAt,
        UUID correctedByUserId,
        String correctedByUserFullName,
        String correctedByUserEmail,
        String reason,
        OffsetDateTime createdAt
) {
}