package resp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public sealed interface RespValue
        permits RespValue.SimpleString,
        RespValue.Error,
        RespValue.Long,
        RespValue.BulkString,
        RespValue.Array,
        RespValue.Null {

    String CRLF = "\r\n";
    SimpleString OK = new SimpleString("OK");

    Null NULL = new Null();

    record SimpleString(String value) implements RespValue {}
    record Error(String errorType, String message) implements RespValue {}
    record Long(long value) implements RespValue {}
    record BulkString(String value) implements RespValue {}
    record Array(List<RespValue> elements) implements RespValue {}
    record Null() implements RespValue {}

    default String encode() {
        return switch (this) {
            case SimpleString(var v) -> "+" + v + CRLF;
            case Error(var e, var m) -> "-" + e + (m.isEmpty() ? "" : " " + m) + CRLF;
            case Long(var n) -> ":" + n + CRLF;
            case Null() -> "$-1\r\n";
            case BulkString(var v) -> "$" + v.length() + CRLF + v + CRLF;
            case Array(var elements) -> {
                var sb = new StringBuilder("*").append(elements.size()).append(CRLF);
                elements.forEach(e -> sb.append(e.encode()));
                yield sb.toString();
            }
        };
    }

    static RespValue decode(String input) {
        if (input == null || input.isBlank()) throw new IllegalArgumentException("Empty input");

        char prefix = input.charAt(0);
        String data = input.substring(1);

        return switch (prefix) {
            case '+' -> {
                int crlfIndex = data.indexOf(CRLF);
                String value = crlfIndex == -1 ? data : data.substring(0, crlfIndex);
                yield new SimpleString(value);
            }
            case '-' -> {
                int crlfIndex = data.indexOf(CRLF);
                String errorData = crlfIndex == -1 ? data : data.substring(0, crlfIndex);
                int spaceIndex = errorData.indexOf(' ');
                if (spaceIndex == -1) {
                    yield new Error(errorData, "");
                } else {
                    yield new Error(errorData.substring(0, spaceIndex), errorData.substring(spaceIndex + 1));
                }
            }
            case ':' -> {
                int crlfIndex = data.indexOf(CRLF);
                String numberStr = crlfIndex == -1 ? data : data.substring(0, crlfIndex);
                yield new Long(java.lang.Long.parseLong(numberStr));
            }
            case '$' -> decodeBulkString(data);
            case '*' -> decodeArray(data);
            default  -> throw new IllegalArgumentException("Unknown prefix: " + prefix);
        };
    }

    private static RespValue decodeBulkString(String data) {
        int crlfIndex = data.indexOf(CRLF);
        int length = java.lang.Integer.parseInt(data.substring(0, crlfIndex));
        if (length == -1) return new Null();

        int contentStart = crlfIndex + CRLF.length();
        String content = data.substring(contentStart, contentStart + length);
        return new BulkString(content);
    }

    private static RespValue decodeArray( String data) {
        int crlfIndex = data.indexOf(CRLF);
        int count = java.lang.Integer.parseInt(data.substring(0, crlfIndex));
        if (count == -1) return new Null();

        var elements = new ArrayList<RespValue>(count);
        int position = crlfIndex + CRLF.length();

        for (int i = 0; i < count; i++) {
            int elementEnd = findElementEnd(data, position);
            String element = data.substring(position, elementEnd);
            elements.add(decode(element));
            position = elementEnd;
        }
        return new Array(elements);
    }

    private static int findElementEnd(String data, int start) {
        char prefix = data.charAt(start);
        int pos = start + 1;

        return switch (prefix) {
            case '+', '-', ':' -> {
                int crlfPos = data.indexOf(CRLF, pos);
                yield crlfPos + CRLF.length();
            }
            case '$' -> {
                int crlfPos = data.indexOf(CRLF, pos);
                int length = java.lang.Integer.parseInt(data.substring(pos, crlfPos));
                if (length == -1) yield crlfPos + CRLF.length();
                yield crlfPos + CRLF.length() + length + CRLF.length();
            }
            case '*' -> {
                int crlfPos = data.indexOf(CRLF, pos);
                int count = java.lang.Integer.parseInt(data.substring(pos, crlfPos));
                if (count == -1) yield crlfPos + CRLF.length();

                int position = crlfPos + CRLF.length();
                for (int i = 0; i < count; i++) {
                    position = findElementEnd(data, position);
                }
                yield position;
            }
            default -> throw new IllegalArgumentException("Unknown prefix: " + prefix);
        };
    }
}
