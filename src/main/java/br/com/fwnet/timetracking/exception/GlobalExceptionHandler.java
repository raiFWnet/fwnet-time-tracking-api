package br.com.fwnet.timetracking.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ProblemDetail handleEmailAlreadyRegistered(
            EmailAlreadyRegisteredException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problemDetail.setTitle("E-mail já cadastrado");
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }

    @ExceptionHandler(DuplicateTimeRecordException.class)
    public ProblemDetail handleDuplicateTimeRecord(
            DuplicateTimeRecordException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problemDetail.setTitle("Marcação duplicada");
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }

    @ExceptionHandler(InvalidTimeRecordSequenceException.class)
    public ProblemDetail handleInvalidTimeRecordSequence(
            InvalidTimeRecordSequenceException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problemDetail.setTitle("Sequência de marcação inválida");
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(
            UserNotFoundException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problemDetail.setTitle("Usuário não encontrado");
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }

    @ExceptionHandler(InactiveUserException.class)
    public ProblemDetail handleInactiveUser(
            InactiveUserException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );

        problemDetail.setTitle("Usuário inativo");
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }
}