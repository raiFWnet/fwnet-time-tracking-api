package br.com.fwnet.timetracking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CorrectTimeRecordRequest(

        @NotNull
        LocalDate workDate,

        @NotNull
        OffsetDateTime recordedAt,

        @NotBlank
        @Size(max = 500)
        String reason

) {
}