package commands;

import resp.RespValue;

import java.util.List;

public class Ping {
    public RespValue execute(List<RespValue> args) {
        if (args.isEmpty()) {
            return new RespValue.SimpleString("PONG");
        }
        return args.getFirst();
    }
}