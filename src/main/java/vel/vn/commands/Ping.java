package vel.vn.commands;

import vel.vn.resp.RespValue;

import java.util.List;

public class Ping implements Command {
    public RespValue execute(List<RespValue> args) {
        if (args.isEmpty()) {
            return new RespValue.SimpleString("PONG");
        }
        return args.getFirst();
    }
}