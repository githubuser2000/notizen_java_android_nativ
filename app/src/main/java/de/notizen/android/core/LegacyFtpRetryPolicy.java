package de.notizen.android.core;

/** Legacy ftpversuche decision around status/error code 7. */
public final class LegacyFtpRetryPolicy {
    public static final int LEGACY_CONNECTION_ERROR = 7;
    public static final int DEFAULT_ATTEMPTS = 3;
    public static final int MAX_ATTEMPTS = 10;

    public static final class Decision {
        public final boolean retry;
        public final int attemptsRemaining;
        public final String message;

        public Decision(boolean retry, int attemptsRemaining, String message) {
            this.retry = retry;
            this.attemptsRemaining = Math.max(0, attemptsRemaining);
            this.message = message == null ? "" : message;
        }
    }

    private LegacyFtpRetryPolicy() {}

    public static int normalizeAttempts(int attempts) {
        if (attempts <= 0) return 0;
        return Math.min(MAX_ATTEMPTS, attempts);
    }

    public static Decision afterFailure(int statusCode, int attemptsRemaining) {
        int left = normalizeAttempts(attemptsRemaining);
        if (statusCode == LEGACY_CONNECTION_ERROR && left > 0) {
            return new Decision(true, left - 1, "FTP-Verbindung fehlgeschlagen; erneuter Versuch.");
        }
        return new Decision(false, 0, "FTP-Verbindung fehlgeschlagen.");
    }
}
