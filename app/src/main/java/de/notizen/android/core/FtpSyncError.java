package de.notizen.android.core;

public final class FtpSyncError extends Exception {
    public FtpSyncError(String message) { super(message); }
    public FtpSyncError(String message, Throwable cause) { super(message, cause); }
}
