package commands;

import resp.RespValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CommandReader {

    private final InputStream in;

    public CommandReader(InputStream in) {
        this.in = in;
    }

    /** Reads next RESP command from stream. Returns null on EOF. */
    public RespValue readCommand() throws IOException {
        return readValue();
    }

    public RespValue handle(RespValue value) {
        if (!(value instanceof RespValue.Array array)) {
            return new RespValue.Error("ERR", "expected array command");
        }

        List<RespValue> elements = array.elements();
        if (elements.isEmpty()) {
            return new RespValue.Error("ERR", "empty command");
        }
        if (!(elements.getFirst() instanceof RespValue.BulkString(var name))) {
            return new RespValue.Error("ERR", "command name must be bulk string");
        }

        List<RespValue> args = elements.subList(1, elements.size());
        return switch (name.toUpperCase()) {
            case "ECHO" -> new Echo().execute(args);
            case "PING" -> new Ping().execute(args);
            default -> new RespValue.Error("ERR", "unknown command '" + name + "'");
        };
    }

    private RespValue readValue() throws IOException {
        int prefix = in.read();
        if (prefix == -1) return null;

        return switch ((char) prefix) {
            case '+' -> new RespValue.SimpleString(readLine());
            case '-' -> {
                String line = readLine();
                int space = line.indexOf(' ');
                yield space == -1
                        ? new RespValue.Error(line, "")
                        : new RespValue.Error(line.substring(0, space), line.substring(space + 1));
            }
            case ':' -> new RespValue.Long(java.lang.Long.parseLong(readLine()));
            case '$' -> {
                int length = Integer.parseInt(readLine());
                if (length == -1) yield new RespValue.Null();
                byte[] bytes = in.readNBytes(length);
                in.readNBytes(2); // consume trailing CRLF
                yield new RespValue.BulkString(new String(bytes));
            }
            case '*' -> {
                int count = Integer.parseInt(readLine());
                if (count == -1) yield new RespValue.Null();
                var elements = new ArrayList<RespValue>(count);
                for (int i = 0; i < count; i++) {
                    elements.add(readValue());
                }
                yield new RespValue.Array(elements);
            }
            default -> throw new IllegalArgumentException("Unknown RESP prefix: " + (char) prefix);
        };
    }

    /** Reads bytes until CRLF, returns content without CRLF. */
    private String readLine() throws IOException {
        var sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                in.read(); // consume \n
                break;
            }
            sb.append((char) b);
        }
        return sb.toString();
    }
}