package de.notizen.android.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SimpleFtpClient {
    private static final Pattern PASV_PATTERN = Pattern.compile(".*\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)\\).*");

    private SimpleFtpClient() {}

    public static byte[] download(FtpTarget target) throws FtpSyncError {
        Session session = new Session(target);
        try {
            session.open();
            session.login();
            session.prepareBinary();
            session.cwd(target.directory());
            Socket data = session.openPassiveDataSocket();
            session.command("RETR " + target.filename(), 125, 150);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(data.getInputStream(), out);
            closeQuietly(data);
            session.expect(226, 250);
            session.quit();
            return out.toByteArray();
        } catch (IOException e) {
            throw new FtpSyncError("FTP-Download fehlgeschlagen: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public static void upload(FtpTarget target, byte[] payload) throws FtpSyncError {
        Session session = new Session(target);
        try {
            session.open();
            session.login();
            session.prepareBinary();
            session.cwd(target.directory());
            Socket data = session.openPassiveDataSocket();
            session.command("STOR " + target.filename(), 125, 150);
            OutputStream out = new BufferedOutputStream(data.getOutputStream());
            out.write(payload == null ? new byte[0] : payload);
            out.flush();
            data.shutdownOutput();
            closeQuietly(data);
            session.expect(226, 250);
            session.quit();
        } catch (IOException e) {
            throw new FtpSyncError("FTP-Upload fehlgeschlagen: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        byte[] buf = new byte[8192];
        int n;
        while ((n = bin.read(buf)) != -1) out.write(buf, 0, n);
    }

    private static void closeQuietly(Socket socket) {
        if (socket == null) return;
        try { socket.close(); } catch (IOException ignored) {}
    }

    private static final class Session {
        final FtpTarget target;
        Socket control;
        BufferedReader reader;
        OutputStream writer;

        Session(FtpTarget target) { this.target = target; }

        void open() throws IOException, FtpSyncError {
            control = new Socket();
            control.connect(new InetSocketAddress(target.host, target.port), target.timeoutMillis);
            control.setSoTimeout(target.timeoutMillis);
            reader = new BufferedReader(new InputStreamReader(control.getInputStream(), StandardCharsets.ISO_8859_1));
            writer = new BufferedOutputStream(control.getOutputStream());
            expect(220);
        }

        void login() throws IOException, FtpSyncError {
            String user = target.username.isEmpty() ? "anonymous" : target.username;
            Reply userReply = commandRaw("USER " + user);
            if (userReply.code == 331) {
                String pass = target.username.isEmpty() ? "anonymous@" : target.password;
                command("PASS " + pass, 230, 202);
            } else if (userReply.code != 230 && userReply.code != 202) {
                throw new FtpSyncError("FTP-Login fehlgeschlagen: " + userReply.line);
            }
        }

        void prepareBinary() throws IOException, FtpSyncError {
            command("TYPE I", 200);
        }

        void cwd(String directory) throws IOException, FtpSyncError {
            if (directory == null || directory.isEmpty() || "/".equals(directory)) return;
            command("CWD " + directory, 250);
        }

        Socket openPassiveDataSocket() throws IOException, FtpSyncError {
            if (!target.passive) throw new FtpSyncError("Nur passives FTP wird im Android-Port unterstützt.");
            Reply reply = command("PASV", 227);
            Matcher m = PASV_PATTERN.matcher(reply.line);
            if (!m.matches()) throw new FtpSyncError("FTP-PASV-Antwort konnte nicht gelesen werden: " + reply.line);
            String host = m.group(1) + "." + m.group(2) + "." + m.group(3) + "." + m.group(4);
            int port = Integer.parseInt(m.group(5)) * 256 + Integer.parseInt(m.group(6));
            Socket data = new Socket();
            data.connect(new InetSocketAddress(host, port), target.timeoutMillis);
            data.setSoTimeout(target.timeoutMillis);
            return data;
        }

        Reply command(String command, int... expected) throws IOException, FtpSyncError {
            Reply reply = commandRaw(command);
            for (int e : expected) if (reply.code == e) return reply;
            throw new FtpSyncError("FTP-Befehl fehlgeschlagen (" + command + "): " + reply.line);
        }

        Reply commandRaw(String command) throws IOException, FtpSyncError {
            writer.write((command + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
            writer.flush();
            return readReply();
        }

        Reply expect(int... expected) throws IOException, FtpSyncError {
            Reply reply = readReply();
            for (int e : expected) if (reply.code == e) return reply;
            throw new FtpSyncError("FTP-Antwort unerwartet: " + reply.line);
        }

        Reply readReply() throws IOException, FtpSyncError {
            String first = reader.readLine();
            if (first == null) throw new FtpSyncError("FTP-Verbindung wurde geschlossen.");
            if (first.length() < 3) throw new FtpSyncError("FTP-Antwort unlesbar: " + first);
            int code;
            try { code = Integer.parseInt(first.substring(0, 3)); }
            catch (NumberFormatException e) { throw new FtpSyncError("FTP-Antwort unlesbar: " + first, e); }
            String line = first;
            if (first.length() > 3 && first.charAt(3) == '-') {
                String prefix = first.substring(0, 3) + " ";
                String next;
                while ((next = reader.readLine()) != null) {
                    line = next;
                    if (next.startsWith(prefix)) break;
                }
            }
            return new Reply(code, line);
        }

        void quit() {
            try { commandRaw("QUIT"); } catch (Exception ignored) {}
        }

        void close() { closeQuietly(control); }
    }

    private static final class Reply {
        final int code;
        final String line;
        Reply(int code, String line) { this.code = code; this.line = line == null ? "" : line; }
    }
}
