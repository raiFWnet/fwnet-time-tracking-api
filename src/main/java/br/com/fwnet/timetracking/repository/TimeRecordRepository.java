package br.com.fwnet.timetracking.repository;

import br.com.fwnet.timetracking.entity.TimeRecord;
import br.com.fwnet.timetracking.enums.TimeRecordType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, UUID> {

    List<TimeRecord> findByUserIdAndWorkDateOrderByRecordedAtAsc(
            UUID userId,
            LocalDate workDate
    );

    boolean existsByUserIdAndWorkDateAndRecordType(
            UUID userId,
            LocalDate workDate,
            TimeRecordType recordType
    );

    List<TimeRecord> findByUserIdOrderByRecordedAtDesc(UUID userId);

    List<TimeRecord> findAllByOrderByRecordedAtDesc();
}