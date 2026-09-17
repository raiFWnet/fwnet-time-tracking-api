package br.com.fwnet.timetracking.service;

import br.com.fwnet.timetracking.dto.request.CorrectTimeRecordRequest;
import br.com.fwnet.timetracking.dto.request.CreateTimeRecordRequest;
import br.com.fwnet.timetracking.dto.response.AdminTimeRecordCorrectionResponse;
import br.com.fwnet.timetracking.dto.response.AdminTimeRecordResponse;
import br.com.fwnet.timetracking.dto.response.TimeRecordResponse;
import br.com.fwnet.timetracking.entity.TimeRecord;
import br.com.fwnet.timetracking.entity.TimeRecordCorrection;
import br.com.fwnet.timetracking.entity.User;
import br.com.fwnet.timetracking.enums.TimeRecordType;
import br.com.fwnet.timetracking.exception.DuplicateTimeRecordException;
import br.com.fwnet.timetracking.exception.InactiveUserException;
import br.com.fwnet.timetracking.exception.InvalidTimeRecordSequenceException;
import br.com.fwnet.timetracking.exception.TimeRecordNotFoundException;
import br.com.fwnet.timetracking.exception.UserNotFoundException;
import br.com.fwnet.timetracking.mapper.TimeRecordCorrectionMapper;
import br.com.fwnet.timetracking.mapper.TimeRecordMapper;
import br.com.fwnet.timetracking.repository.TimeRecordCorrectionRepository;
import br.com.fwnet.timetracking.repository.TimeRecordRepository;
import br.com.fwnet.timetracking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TimeRecordService {

    private static final String WEB_SOURCE = "WEB";

    private final TimeRecordRepository timeRecordRepository;
    private final TimeRecordCorrectionRepository timeRecordCorrectionRepository;
    private final UserRepository userRepository;
    private final TimeRecordMapper timeRecordMapper;
    private final TimeRecordCorrectionMapper timeRecordCorrectionMapper;

    public TimeRecordService(
            TimeRecordRepository timeRecordRepository,
            TimeRecordCorrectionRepository timeRecordCorrectionRepository,
            UserRepository userRepository,
            TimeRecordMapper timeRecordMapper,
            TimeRecordCorrectionMapper timeRecordCorrectionMapper
    ) {
        this.timeRecordRepository = timeRecordRepository;
        this.timeRecordCorrectionRepository = timeRecordCorrectionRepository;
        this.userRepository = userRepository;
        this.timeRecordMapper = timeRecordMapper;
        this.timeRecordCorrectionMapper = timeRecordCorrectionMapper;
    }

    @Transactional
    public TimeRecordResponse create(
            String authenticatedEmail,
            CreateTimeRecordRequest request
    ) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Usuário autenticado não encontrado."
                        )
                );

        if (!user.isActive()) {
            throw new InactiveUserException(
                    "Usuário inativo não pode registrar ponto."
            );
        }

        OffsetDateTime now = OffsetDateTime.now();
        LocalDate workDate = now.toLocalDate();
        TimeRecordType requestedType = request.recordType();

        if (timeRecordRepository.existsByUserIdAndWorkDateAndRecordType(
                user.getId(),
                workDate,
                requestedType
        )) {
            throw new DuplicateTimeRecordException(
                    "Esta marcação já foi registrada para a jornada de hoje."
            );
        }

        List<TimeRecord> currentRecords =
                timeRecordRepository.findByUserIdAndWorkDateOrderByRecordedAtAsc(
                        user.getId(),
                        workDate
                );

        validateSequence(currentRecords, requestedType);

        TimeRecord timeRecord = new TimeRecord();
        timeRecord.setId(UUID.randomUUID());
        timeRecord.setUser(user);
        timeRecord.setWorkDate(workDate);
        timeRecord.setRecordType(requestedType);
        timeRecord.setRecordedAt(now);
        timeRecord.setSource(WEB_SOURCE);
        timeRecord.setCreatedAt(now);

        TimeRecord savedTimeRecord =
                timeRecordRepository.save(timeRecord);

        return timeRecordMapper.toResponse(savedTimeRecord);
    }

    @Transactional(readOnly = true)
    public List<TimeRecordResponse> getHistory(
            String authenticatedEmail
    ) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Usuário autenticado não encontrado."
                        )
                );

        return timeRecordRepository
                .findByUserIdOrderByRecordedAtDesc(user.getId())
                .stream()
                .map(timeRecordMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminTimeRecordResponse> getAdminHistory() {
        return timeRecordRepository
                .findAllByOrderByRecordedAtDesc()
                .stream()
                .map(timeRecordMapper::toAdminResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminTimeRecordCorrectionResponse> getAdminCorrectionLogs() {
        return timeRecordCorrectionRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(timeRecordCorrectionMapper::toAdminResponse)
                .toList();
    }

    @Transactional
    public AdminTimeRecordResponse correct(
            String authenticatedEmail,
            UUID timeRecordId,
            CorrectTimeRecordRequest request
    ) {
        User correctedByUser = userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Usuário autenticado não encontrado."
                        )
                );

        TimeRecord timeRecord = timeRecordRepository
                .findById(timeRecordId)
                .orElseThrow(() ->
                        new TimeRecordNotFoundException(
                                "Marcação de ponto não encontrada."
                        )
                );

        boolean duplicateExists =
                timeRecordRepository
                        .existsByUserIdAndWorkDateAndRecordTypeAndIdNot(
                                timeRecord.getUser().getId(),
                                request.workDate(),
                                timeRecord.getRecordType(),
                                timeRecord.getId()
                        );

        if (duplicateExists) {
            throw new DuplicateTimeRecordException(
                    "Já existe outra marcação deste tipo para o analista na jornada informada."
            );
        }

        LocalDate previousWorkDate =
                timeRecord.getWorkDate();

        OffsetDateTime previousRecordedAt =
                timeRecord.getRecordedAt();

        timeRecord.setWorkDate(request.workDate());
        timeRecord.setRecordedAt(request.recordedAt());

        TimeRecord savedTimeRecord =
                timeRecordRepository.save(timeRecord);

        TimeRecordCorrection correction =
                new TimeRecordCorrection();

        correction.setId(UUID.randomUUID());
        correction.setTimeRecord(savedTimeRecord);
        correction.setCorrectedByUser(correctedByUser);
        correction.setReason(request.reason().trim());
        correction.setPreviousWorkDate(previousWorkDate);
        correction.setNewWorkDate(savedTimeRecord.getWorkDate());
        correction.setPreviousRecordedAt(previousRecordedAt);
        correction.setNewRecordedAt(savedTimeRecord.getRecordedAt());
        correction.setCreatedAt(OffsetDateTime.now());

        timeRecordCorrectionRepository.save(correction);

        return timeRecordMapper.toAdminResponse(savedTimeRecord);
    }

    private void validateSequence(
            List<TimeRecord> currentRecords,
            TimeRecordType requestedType
    ) {
        TimeRecordType expectedType =
                determineExpectedType(currentRecords);

        if (expectedType == null) {
            throw new InvalidTimeRecordSequenceException(
                    "A jornada de hoje já foi encerrada."
            );
        }

        if (requestedType != expectedType) {
            throw new InvalidTimeRecordSequenceException(
                    "Sequência de marcação inválida. Próxima marcação permitida: "
                            + expectedType + "."
            );
        }
    }

    private TimeRecordType determineExpectedType(
            List<TimeRecord> currentRecords
    ) {
        if (currentRecords.isEmpty()) {
            return TimeRecordType.CLOCK_IN;
        }

        TimeRecordType lastRecordType =
                currentRecords
                        .get(currentRecords.size() - 1)
                        .getRecordType();

        return switch (lastRecordType) {
            case CLOCK_IN -> TimeRecordType.LUNCH_OUT;
            case LUNCH_OUT -> TimeRecordType.LUNCH_IN;
            case LUNCH_IN -> TimeRecordType.CLOCK_OUT;
            case CLOCK_OUT -> null;
        };
    }
}