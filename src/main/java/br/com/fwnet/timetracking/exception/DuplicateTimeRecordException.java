package br.com.fwnet.timetracking.exception;

public class DuplicateTimeRecordException extends RuntimeException {

    public DuplicateTimeRecordException(String message) {
        super(message);
    }
}