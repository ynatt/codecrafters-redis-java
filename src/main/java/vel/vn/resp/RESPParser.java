package vel.vn.resp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RESPParser {
    public static RespValue parse(BufferedInputStream in) throws IOException {
        int prefix = in.read();
        if (prefix == -1) return null;

        return switch ((char) prefix) {
            case '+' -> new RespValue.SimpleString(readLine(in));
            case '-' -> {
                String line = readLine(in);
                int space = line.indexOf(' ');
                yield space == -1
                        ? new RespValue.Error(line, "")
                        : new RespValue.Error(line.substring(0, space), line.substring(space + 1));
            }
            case ':' -> new RespValue.Long(java.lang.Long.parseLong(readLine(in)));
            case '$' -> {
                int length = Integer.parseInt(readLine(in));
                if (length == -1) yield new RespValue.Null();
                byte[] bytes = in.readNBytes(length);
                in.skipNBytes(2);
                yield new RespValue.BulkString(new String(bytes));
            }
            case '*' -> {
                int count = Integer.parseInt(readLine(in));
                if (count == -1) yield new RespValue.Null();
                var elements = new ArrayList<RespValue>(count);
                for (int i = 0; i < count; i++) {
                    elements.add(parse(in));
                }
                yield new RespValue.Array(elements);
            }
            default -> throw new IllegalArgumentException("Unknown RESP prefix: " + (char) prefix);
        };
    }

    /** Reads bytes until CRLF, returns content without CRLF. */
    private static String readLine(BufferedInputStream in) throws IOException {
        var sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                in.skipNBytes(1);
                break;
            }
            sb.append((char) b);
        }
        return sb.toString();
    }
}
