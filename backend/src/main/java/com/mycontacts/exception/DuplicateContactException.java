package com.mycontacts.exception;

/**
 * Thrown when a duplicate contact (same name or phone number) is detected.
 */
public class DuplicateContactException extends RuntimeException {
    public DuplicateContactException(String message) {
        super(message);
    }
}
