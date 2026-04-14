package vel.vn.commands;

import vel.vn.resp.RespValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandReader {

    private Map<String, Command> commands;

    public CommandReader() {
        commands = new HashMap<>();
        commands.put("PING", new Ping());
        commands.put("ECHO", new Echo());
        commands.put("SET", new Set());
        commands.put("GET", new Get());
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
        Command command = commands.get(name.toUpperCase());
        if (command == null) return new RespValue.Error("ERR", "unknown command '" + name + "'");
        return command.execute(args);
    }
}