package de.notizen.android.core;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Legacy TXT/RTF import helpers mirroring the PyQt import paths. */
public final class LegacyImporters {
    private LegacyImporters() {}

    public static String decodeTextBytes(byte[] data) {
        return decodeTextBytes(data, false);
    }

    public static String decodeTextBytes(byte[] data, boolean preferAnsi) {
        if (data == null || data.length == 0) return "";
        if (data.length >= 3 && (data[0] & 0xff) == 0xef && (data[1] & 0xff) == 0xbb && (data[2] & 0xff) == 0xbf) {
            return new String(data, 3, data.length - 3, StandardCharsets.UTF_8);
        }
        if (data.length >= 2 && (data[0] & 0xff) == 0xff && (data[1] & 0xff) == 0xfe) {
            return new String(data, 2, data.length - 2, StandardCharsets.UTF_16LE);
        }
        if (data.length >= 2 && (data[0] & 0xff) == 0xfe && (data[1] & 0xff) == 0xff) {
            return new String(data, 2, data.length - 2, StandardCharsets.UTF_16BE);
        }
        String head = new String(data, 0, Math.min(data.length, 4096), StandardCharsets.ISO_8859_1);
        Matcher m = Pattern.compile("(?is)charset\\s*=\\s*['\"]?([-_a-z0-9]+)").matcher(head);
        if (m.find()) {
            try { return new String(data, Charset.forName(m.group(1))); } catch (Exception ignored) {}
        }
        Charset ansi = Charset.forName("windows-1252");
        if (preferAnsi) return new String(data, ansi);
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(ByteBuffer.wrap(data)).toString();
        } catch (Exception ignored) {
            return new String(data, ansi);
        }
    }

    public static String txtBytesToRtf(byte[] data) {
        return RtfUtils.plainTextToRtf(decodeTextBytes(data, false));
    }

    public static String rtfBytesToRtf(byte[] data) {
        String text = decodeTextBytes(data, true);
        return RtfUtils.looksLikeRtf(text.trim()) ? text : RtfUtils.plainTextToRtf(text);
    }
}
