package br.com.fwnet.timetracking.repository;

import br.com.fwnet.timetracking.entity.TimeRecordCorrection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimeRecordCorrectionRepository
        extends JpaRepository<TimeRecordCorrection, UUID> {

    List<TimeRecordCorrection> findAllByOrderByCreatedAtDesc();
}