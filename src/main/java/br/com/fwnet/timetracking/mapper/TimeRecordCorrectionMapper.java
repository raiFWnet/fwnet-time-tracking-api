package br.com.fwnet.timetracking.mapper;

import br.com.fwnet.timetracking.dto.response.AdminTimeRecordCorrectionResponse;
import br.com.fwnet.timetracking.entity.TimeRecordCorrection;
import org.springframework.stereotype.Component;

@Component
public class TimeRecordCorrectionMapper {

    public AdminTimeRecordCorrectionResponse toAdminResponse(
            TimeRecordCorrection correction
    ) {
        return new AdminTimeRecordCorrectionResponse(
                correction.getId(),
                correction.getTimeRecord().getId(),
                correction.getTimeRecord().getUser().getId(),
                correction.getTimeRecord().getUser().getFullName(),
                correction.getTimeRecord().getUser().getEmail(),
                correction.getTimeRecord().getRecordType(),
                correction.getPreviousWorkDate(),
                correction.getNewWorkDate(),
                correction.getPreviousRecordedAt(),
                correction.getNewRecordedAt(),
                correction.getCorrectedByUser().getId(),
                correction.getCorrectedByUser().getFullName(),
                correction.getCorrectedByUser().getEmail(),
                correction.getReason(),
                correction.getCreatedAt()
        );
    }
}