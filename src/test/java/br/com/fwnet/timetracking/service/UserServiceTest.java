package br.com.fwnet.timetracking.service;

import br.com.fwnet.timetracking.dto.request.UpdateUserRequest;
import br.com.fwnet.timetracking.dto.response.UserResponse;
import br.com.fwnet.timetracking.entity.User;
import br.com.fwnet.timetracking.enums.Role;
import br.com.fwnet.timetracking.exception.EmailAlreadyRegisteredException;
import br.com.fwnet.timetracking.exception.UserNotFoundException;
import br.com.fwnet.timetracking.mapper.UserMapper;
import br.com.fwnet.timetracking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                new UserMapper(),
                passwordEncoder
        );

        OffsetDateTime createdAt =
                OffsetDateTime.parse("2026-09-01T08:00:00-03:00");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Nome Original");
        user.setEmail("original@example.com");
        user.setPasswordHash("original-password-hash");
        user.setRole(Role.ANALYST);
        user.setActive(false);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(createdAt);
    }

    @Test
    void shouldUpdateUserAndEncodeNewPassword() {
        OffsetDateTime originalCreatedAt = user.getCreatedAt();

        UpdateUserRequest request = new UpdateUserRequest(
                "Nome Atualizado",
                "updated@example.com",
                "NewPassword123!",
                Role.ADMIN
        );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot(
                request.email(), user.getId()
        )).thenReturn(false);
        when(passwordEncoder.encode(request.password()))
                .thenReturn("new-password-hash");
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.update(user.getId(), request);

        assertEquals(user.getId(), response.id());
        assertEquals("Nome Atualizado", response.fullName());
        assertEquals("updated@example.com", response.email());
        assertEquals(Role.ADMIN, response.role());
        assertEquals("new-password-hash", user.getPasswordHash());
        assertFalse(response.active());
        assertEquals(originalCreatedAt, response.createdAt());
        assertTrue(response.updatedAt().isAfter(originalCreatedAt));

        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(user);
    }

    @Test
    void shouldKeepPasswordWhenNotProvided() {
        assertPasswordIsPreserved(null);
    }

    @Test
    void shouldKeepPasswordWhenEmpty() {
        assertPasswordIsPreserved("");
    }

    @Test
    void shouldKeepPasswordWhenBlank() {
        assertPasswordIsPreserved("   ");
    }

    @Test
    void shouldRejectEmailAlreadyUsedByAnotherUser() {
        UpdateUserRequest request = new UpdateUserRequest(
                "Nome Atualizado",
                "existing@example.com",
                "NewPassword123!",
                Role.ADMIN
        );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot(
                request.email(), user.getId()
        )).thenReturn(true);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> userService.update(user.getId(), request)
        );

        assertEquals("Nome Original", user.getFullName());
        assertEquals("original@example.com", user.getEmail());
        assertEquals(Role.ANALYST, user.getRole());
        assertEquals("original-password-hash", user.getPasswordHash());

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectUpdateWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();

        UpdateUserRequest request = new UpdateUserRequest(
                "Nome Atualizado",
                "updated@example.com",
                null,
                Role.ANALYST
        );

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.update(id, request)
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }
    @Test
    void shouldListAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> response = userService.findAll();

        assertEquals(1, response.size());
        assertEquals(user.getId(), response.getFirst().id());
        assertEquals(user.getFullName(), response.getFirst().fullName());
        assertEquals(user.getEmail(), response.getFirst().email());
        assertEquals(user.getRole(), response.getFirst().role());
        assertEquals(user.isActive(), response.getFirst().active());

        verify(userRepository).findAll();
    }
    private void assertPasswordIsPreserved(String password) {
        UpdateUserRequest request = new UpdateUserRequest(
                "Nome Atualizado",
                user.getEmail(),
                password,
                Role.ANALYST
        );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot(
                request.email(), user.getId()
        )).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.update(user.getId(), request);

        assertEquals("Nome Atualizado", response.fullName());
        assertEquals("original@example.com", response.email());
        assertEquals("original-password-hash", user.getPasswordHash());

        verify(userRepository).existsByEmailAndIdNot(
                request.email(), user.getId()
        );
        verify(userRepository).save(user);
        verifyNoInteractions(passwordEncoder);
    }
}