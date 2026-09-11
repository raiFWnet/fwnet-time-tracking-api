package br.com.fwnet.timetracking.mapper;

import br.com.fwnet.timetracking.dto.response.TimeRecordResponse;
import br.com.fwnet.timetracking.entity.TimeRecord;
import org.springframework.stereotype.Component;

@Component
public class TimeRecordMapper {

    public TimeRecordResponse toResponse(TimeRecord timeRecord) {
        return new TimeRecordResponse(
                timeRecord.getId(),
                timeRecord.getWorkDate(),
                timeRecord.getRecordType(),
                timeRecord.getRecordedAt(),
                timeRecord.getSource(),
                timeRecord.getCreatedAt()
        );
    }
}