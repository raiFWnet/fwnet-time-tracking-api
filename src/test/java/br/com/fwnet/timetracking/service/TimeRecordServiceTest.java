package br.com.fwnet.timetracking.service;

import br.com.fwnet.timetracking.dto.request.CreateTimeRecordRequest;
import br.com.fwnet.timetracking.dto.response.TimeRecordResponse;
import br.com.fwnet.timetracking.entity.TimeRecord;
import br.com.fwnet.timetracking.entity.User;
import br.com.fwnet.timetracking.enums.TimeRecordType;
import br.com.fwnet.timetracking.exception.DuplicateTimeRecordException;
import br.com.fwnet.timetracking.exception.InactiveUserException;
import br.com.fwnet.timetracking.exception.InvalidTimeRecordSequenceException;
import br.com.fwnet.timetracking.exception.UserNotFoundException;
import br.com.fwnet.timetracking.mapper.TimeRecordMapper;
import br.com.fwnet.timetracking.repository.TimeRecordRepository;
import br.com.fwnet.timetracking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeRecordServiceTest {

    private static final String EMAIL = "analyst@fwnet.com.br";

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TimeRecordMapper timeRecordMapper;

    private TimeRecordService timeRecordService;

    @BeforeEach
    void setUp() {
        timeRecordService = new TimeRecordService(
                timeRecordRepository,
                userRepository,
                timeRecordMapper
        );
    }

    @Test
    void shouldCreateClockInWhenUserHasNoRecordsForTheDay() {
        User user = createActiveUser();

        CreateTimeRecordRequest request =
                new CreateTimeRecordRequest(TimeRecordType.CLOCK_IN);

        TimeRecordResponse expectedResponse =
                createExpectedResponse(TimeRecordType.CLOCK_IN);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(timeRecordRepository.existsByUserIdAndWorkDateAndRecordType(
                any(UUID.class),
                any(LocalDate.class),
                any(TimeRecordType.class)
        )).thenReturn(false);

        when(timeRecordRepository.findByUserIdAndWorkDateOrderByRecordedAtAsc(
                any(UUID.class),
                any(LocalDate.class)
        )).thenReturn(Collections.emptyList());

        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(timeRecordMapper.toResponse(any(TimeRecord.class)))
                .thenReturn(expectedResponse);

        TimeRecordResponse response =
                timeRecordService.create(EMAIL, request);

        ArgumentCaptor<TimeRecord> timeRecordCaptor =
                ArgumentCaptor.forClass(TimeRecord.class);

        verify(timeRecordRepository).save(timeRecordCaptor.capture());

        TimeRecord savedTimeRecord = timeRecordCaptor.getValue();

        assertNotNull(savedTimeRecord.getId());
        assertEquals(user, savedTimeRecord.getUser());
        assertEquals(TimeRecordType.CLOCK_IN, savedTimeRecord.getRecordType());
        assertEquals("WEB", savedTimeRecord.getSource());
        assertNotNull(savedTimeRecord.getWorkDate());
        assertNotNull(savedTimeRecord.getRecordedAt());
        assertNotNull(savedTimeRecord.getCreatedAt());

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldRejectLunchOutWhenUserHasNoRecordsForTheDay() {
        User user = createActiveUser();

        CreateTimeRecordRequest request =
                new CreateTimeRecordRequest(TimeRecordType.LUNCH_OUT);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(timeRecordRepository.existsByUserIdAndWorkDateAndRecordType(
                any(UUID.class),
                any(LocalDate.class),
                any(TimeRecordType.class)
        )).thenReturn(false);

        when(timeRecordRepository.findByUserIdAndWorkDateOrderByRecordedAtAsc(
                any(UUID.class),
                any(LocalDate.class)
        )).thenReturn(Collections.emptyList());

        InvalidTimeRecordSequenceException exception = assertThrows(
                InvalidTimeRecordSequenceException.class,
                () -> timeRecordService.create(EMAIL, request)
        );

        assertEquals(
                "Sequência de marcação inválida. Próxima marcação permitida: CLOCK_IN.",
                exception.getMessage()
        );

        verify(timeRecordRepository, never())
                .save(any(TimeRecord.class));
    }

    @Test
    void shouldRejectClockOutWhenNextExpectedRecordIsLunchOut() {
        User user = createActiveUser();

        TimeRecord clockIn = createTimeRecord(
                user,
                TimeRecordType.CLOCK_IN,
                3
        );

        CreateTimeRecordRequest request =
                new CreateTimeRecordRequest(TimeRecordType.CLOCK_OUT);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(timeRecordRepository.existsByUserIdAndWorkDateAndRecordType(
                any(UUID.class),
                any(LocalDate.class),
                any(TimeRecordType.class)
        )).thenReturn(false);

        when(timeRecordRepository.findByUserIdAndWorkDateOrderByRecordedAtAsc(
                any(UUID.class),
                any(LocalDate.class)
        )).thenReturn(List.of(clockIn));

        InvalidTimeRecordSequenceException exception = assertThrows(
                InvalidTimeRecordSequenceException.class,
                () -> timeRecordService.create(EMAIL, request)
        );

        assertEquals(
                "Sequência de marcação inválida. Próxima marcação permitida: LUNCH_OUT.",
                exception.getMessage()
        );

        verify(timeRecordRepository, never())
                .save(any(TimeRecord.class));
    }

    @Test
    void shouldCreateLunchOutAfterClockIn() {
        User user = createActiveUser();

        TimeRecord clockIn = createTimeRecord(
                user,
                TimeRecordType.CLOCK_IN,
                3
        );

        CreateTimeRecordRequest request =
                new CreateTimeRecordRequest(TimeRecordType.LUNCH_OUT);

        TimeRecordResponse expectedResponse =
                createExpectedResponse(TimeRecordType.LUNCH_OUT);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(timeRecordRepository.existsByUserIdAndWorkDateAndRecordType(
                any(UUID.class),
                any(LocalDate.class),
                any(TimeRecordType.class)
        )).thenReturn(false);

        when(timeRecordRepository.findByUserIdAndWorkDateOrderByRecordedAtAsc(
                any(UUID.class),
                any(LocalDate.class)
        )).thenReturn(List.of(clockIn));

        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(timeRecordMapper.toResponse(any(TimeRecord.class)))
                .thenReturn(expectedResponse);

        TimeRecordResponse response =
                timeRecordService.create(EMAIL, request);

        ArgumentCaptor<TimeRecord> timeRecordCaptor =
                ArgumentCaptor.forClass(TimeRecord.class);

        verify(timeRecordRepository).save(timeRecordCaptor.capture());

        TimeRecord savedTimeRecord = timeRecordCaptor.getValue();

        assertEquals(TimeRecordType.LUNCH_OUT, savedTimeRecord.getRecordType());
        assertEquals(user, savedTimeRecord.getUser());
        assertEquals("WEB", savedTimeRecord.getSource());
        assertNotNull(savedTimeRecord.getRecordedAt());

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldCreateLunchInAfterLunchOut() {
        User user = createActiveUser();

        TimeRecord clockIn = createTimeRecord(
                user,
                TimeRecordType.CLOCK_IN,
                4
        );

        TimeRecord lunchOut = createTimeRecord(
                user,
                TimeRecordType.LUNCH_OUT,
                2
        );

        CreateTimeRecordRequest request =
                new CreateTimeRecordRequest(TimeRecordType.LUNCH_IN);

        TimeRecordResponse expectedResponse =
                createExpectedResponse(TimeRecordType.LUNCH_IN);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(timeRecordRepository.existsByUserIdAndWorkDateAndRecordType(
                any(UUID.class),
                any(LocalDate.class),
                any(TimeRecordType.class)
        )).thenReturn(false);

        when(timeRecordRepository.findByUserIdAndWorkDateOrderByRecordedAtAsc(
                any(UUID.class),
                any(LocalDate.class)
        )).thenReturn(List.of(clockIn, lunchOut));

        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(timeRecordMapper.toResponse(any(TimeRecord.class)))
                .thenReturn(expectedResponse);

        TimeRecordResponse response =
                timeRecordService.create(EMAIL, request);

        ArgumentCaptor<TimeRecord> timeRecordCaptor =
                ArgumentCaptor.forClass(TimeRecord.class);

        verify(timeRecordRepository).save(timeRecordCaptor.capture());

        TimeRecord savedTimeRecord = timeRecordCaptor.getValue();

        assertEquals(TimeRecordType.LUNCH_IN, savedTimeRecord.getRecordType());
        assertEquals(user, savedTimeRecord.getUser());
        assertEquals("WEB", savedTimeRecord.getSource());
        assertNotNull(savedTimeRecord.getRecordedAt());

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldCreateClockOutAfterLunchIn() {
        User user = createActiveUser();

        TimeRecord clockIn = createTimeRecord(
                user,
                TimeRecordType.CLOCK_IN,
                8
        );

        TimeRecord lunchOut = createTimeRecord(
                user,
                TimeRecordType.LUNCH_OUT,
                5
        );

        TimeRecord lunchIn = createTimeRecord(
                user,
                TimeRecordType.LUNCH_IN,
                4
        );

        CreateTimeRecordRequest request =
                new CreateTimeRecordRequest(TimeRecordType.CLOCK_OUT);

        TimeRecordResponse expectedResponse =
                createExpectedResponse(TimeRecordType.CLOCK_OUT);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(timeRecordRepository.existsByUserIdAndWorkDateAndRecordType(
                any(UUID.class),
                any(LocalDate.class),
                any(TimeRecordType.class)
        )).thenReturn(false);

        when(timeRecordRepository.findByUserIdAndWorkDateOrderByRecordedAtAsc(
                any(UUID.class),
                any(LocalDate.class)
        )).thenReturn(List.of(clockIn, lunchOut, lunchIn));

        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(timeRecordMapper.toResponse(any(TimeRecord.class)))
                .thenReturn(expectedResponse);

        TimeRecordResponse response =
                timeRecordService.create(EMAIL, request);

        ArgumentCaptor<TimeRecord> timeRecordCaptor =
                ArgumentCaptor.forClass(TimeRecord.class);

        verify(timeRecordRepository).save(timeRecordCaptor.capture());

        TimeRecord savedTimeRecord = timeRecordCaptor.getValue();

        assertEquals(TimeRecordType.CLOCK_OUT, savedTimeRecord.getRecordType());
        assertEquals(user, savedTimeRecord.getUser());
        assertEquals("WEB", savedTimeRecord.getSource());
        assertNotNull(savedTimeRecord.getRecordedAt());

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldRejectDuplicateTimeRecord() {
        User user = createActiveUser();

        CreateTimeRecordRequest request =
                new CreateTimeRecordRequest(TimeRecordType.CLOCK_IN);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(timeRecordRepository.existsByUserIdAndWorkDateAndRecordType(
                any(UUID.class),
                any(LocalDate.class),
                any(TimeRecordType.class)
        )).thenReturn(true);

        DuplicateTimeRecordException exception = assertThrows(
                DuplicateTimeRecordException.class,
                () -> timeRecordService.create(EMAIL, request)
        );

        assertEquals(
                "Esta marcação já foi registrada para a jornada de hoje.",
                exception.getMessage()
        );

        verify(timeRecordRepository, never())
                .findByUserIdAndWorkDateOrderByRecordedAtAsc(
                        any(UUID.class),
                        any(LocalDate.class)
                );

        verify(timeRecordRepository, never())
                .save(any(TimeRecord.class));

        verifyNoInteractions(timeRecordMapper);
    }

    @Test
    void shouldRejectTimeRecordWhenUserIsInactive() {
        User user = createActiveUser();
        user.setActive(false);

        CreateTimeRecordRequest request =
                new CreateTimeRecordRequest(TimeRecordType.CLOCK_IN);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        InactiveUserException exception = assertThrows(
                InactiveUserException.class,
                () -> timeRecordService.create(EMAIL, request)
        );

        assertEquals(
                "Usuário inativo não pode registrar ponto.",
                exception.getMessage()
        );

        verify(timeRecordRepository, never())
                .existsByUserIdAndWorkDateAndRecordType(
                        any(UUID.class),
                        any(LocalDate.class),
                        any(TimeRecordType.class)
                );

        verify(timeRecordRepository, never())
                .save(any(TimeRecord.class));

        verifyNoInteractions(timeRecordMapper);
    }

    @Test
    void shouldRejectTimeRecordWhenAuthenticatedUserIsNotFound() {
        CreateTimeRecordRequest request =
                new CreateTimeRecordRequest(TimeRecordType.CLOCK_IN);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> timeRecordService.create(EMAIL, request)
        );

        assertEquals(
                "Usuário autenticado não encontrado.",
                exception.getMessage()
        );

        verifyNoInteractions(
                timeRecordRepository,
                timeRecordMapper
        );
    }

    @Test
    void shouldReturnHistoryForAuthenticatedUser() {
        User user = createActiveUser();

        TimeRecord clockOut = createTimeRecord(
                user,
                TimeRecordType.CLOCK_OUT,
                1
        );

        TimeRecord lunchIn = createTimeRecord(
                user,
                TimeRecordType.LUNCH_IN,
                4
        );

        TimeRecordResponse clockOutResponse =
                createExpectedResponse(TimeRecordType.CLOCK_OUT);

        TimeRecordResponse lunchInResponse =
                createExpectedResponse(TimeRecordType.LUNCH_IN);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(timeRecordRepository.findByUserIdOrderByRecordedAtDesc(
                user.getId()
        )).thenReturn(List.of(clockOut, lunchIn));

        when(timeRecordMapper.toResponse(clockOut))
                .thenReturn(clockOutResponse);

        when(timeRecordMapper.toResponse(lunchIn))
                .thenReturn(lunchInResponse);

        List<TimeRecordResponse> history =
                timeRecordService.getHistory(EMAIL);

        assertEquals(2, history.size());
        assertEquals(clockOutResponse, history.get(0));
        assertEquals(lunchInResponse, history.get(1));

        verify(timeRecordRepository)
                .findByUserIdOrderByRecordedAtDesc(user.getId());
    }

    @Test
    void shouldRejectHistoryWhenAuthenticatedUserIsNotFound() {
        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> timeRecordService.getHistory(EMAIL)
        );

        assertEquals(
                "Usuário autenticado não encontrado.",
                exception.getMessage()
        );

        verifyNoInteractions(
                timeRecordRepository,
                timeRecordMapper
        );
    }

    private User createActiveUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(EMAIL);
        user.setActive(true);

        return user;
    }

    private TimeRecord createTimeRecord(
            User user,
            TimeRecordType recordType,
            long hoursAgo
    ) {
        OffsetDateTime dateTime =
                OffsetDateTime.now().minusHours(hoursAgo);

        TimeRecord timeRecord = new TimeRecord();
        timeRecord.setId(UUID.randomUUID());
        timeRecord.setUser(user);
        timeRecord.setWorkDate(LocalDate.now());
        timeRecord.setRecordType(recordType);
        timeRecord.setRecordedAt(dateTime);
        timeRecord.setSource("WEB");
        timeRecord.setCreatedAt(dateTime);

        return timeRecord;
    }

    private TimeRecordResponse createExpectedResponse(
            TimeRecordType recordType
    ) {
        OffsetDateTime now = OffsetDateTime.now();

        return new TimeRecordResponse(
                UUID.randomUUID(),
                LocalDate.now(),
                recordType,
                now,
                "WEB",
                now
        );
    }
}