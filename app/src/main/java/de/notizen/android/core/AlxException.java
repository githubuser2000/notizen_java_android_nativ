package de.notizen.android.core;

public class AlxException extends Exception {
    public AlxException(String message) { super(message); }
    public AlxException(String message, Throwable cause) { super(message, cause); }

    public static final class PasswordRequired extends AlxException {
        public PasswordRequired(String message) { super(message); }
    }

    public static final class InvalidPassword extends AlxException {
        public InvalidPassword(String message, Throwable cause) { super(message, cause); }
    }
}
