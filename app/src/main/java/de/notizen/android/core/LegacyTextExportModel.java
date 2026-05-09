package de.notizen.android.core;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** TXT export paths matching the old ANSI/UTF-8/Unicode choices. */
public final class LegacyTextExportModel {
    private LegacyTextExportModel() {}

    public enum Mode { ANSI, UTF8, UNICODE }

    public static final class Result {
        public final Mode mode;
        public final String text;
        public final byte[] bytes;
        public final LegacyFileDialogModel.Spec dialogSpec;

        Result(Mode mode, String text, byte[] bytes, LegacyFileDialogModel.Spec dialogSpec) {
            this.mode = mode;
            this.text = text == null ? "" : text;
            this.bytes = bytes == null ? new byte[0] : bytes;
            this.dialogSpec = dialogSpec;
        }
    }

    public static Result forTree(NoteNode root, Mode mode, String directory) {
        Mode m = mode == null ? Mode.UTF8 : mode;
        String text = normalizeCrLf(Exporters.treeToPlainText(root, null));
        return new Result(m, text, encode(text, m), specForMode(m, directory));
    }

    public static LegacyFileDialogModel.Spec specForMode(Mode mode, String directory) {
        Mode m = mode == null ? Mode.UTF8 : mode;
        if (m == Mode.ANSI) return LegacyFileDialogModel.exportTxtAnsi(directory);
        if (m == Mode.UNICODE) return LegacyFileDialogModel.exportTxtUnicode(directory);
        return LegacyFileDialogModel.exportTxtUtf8(directory);
    }

    public static byte[] encode(String text, Mode mode) {
        String s = normalizeCrLf(text);
        Mode m = mode == null ? Mode.UTF8 : mode;
        if (m == Mode.ANSI) return s.getBytes(Charset.forName("windows-1252"));
        if (m == Mode.UNICODE) {
            byte[] raw = s.getBytes(StandardCharsets.UTF_16LE);
            ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length + 2);
            out.write(0xff);
            out.write(0xfe);
            out.write(raw, 0, raw.length);
            return out.toByteArray();
        }
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String normalizeCrLf(String text) {
        String s = text == null ? "" : text;
        s = s.replace("\r\n", "\n").replace('\r', '\n');
        return s.replace("\n", "\r\n");
    }

    public static String status(Mode mode) {
        Mode m = mode == null ? Mode.UTF8 : mode;
        if (m == Mode.ANSI) return "ANSI-TXT exportiert";
        if (m == Mode.UNICODE) return "Unicode-TXT exportiert";
        return "TXT exportiert";
    }
}
