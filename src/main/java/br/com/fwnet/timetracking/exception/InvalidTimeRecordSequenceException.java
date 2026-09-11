package br.com.fwnet.timetracking.exception;

public class InvalidTimeRecordSequenceException extends RuntimeException {

    public InvalidTimeRecordSequenceException(String message) {
        super(message);
    }
}