package br.com.fwnet.timetracking.exception;

public class TimeRecordNotFoundException extends RuntimeException {

    public TimeRecordNotFoundException(String message) {
        super(message);
    }
}