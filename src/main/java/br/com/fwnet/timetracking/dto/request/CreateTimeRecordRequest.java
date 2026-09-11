package br.com.fwnet.timetracking.dto.request;

import br.com.fwnet.timetracking.enums.TimeRecordType;
import jakarta.validation.constraints.NotNull;

public record CreateTimeRecordRequest(

        @NotNull
        TimeRecordType recordType

) {
}