package commands;

import resp.RespValue;

import java.util.List;

public class Echo {

    public RespValue execute(List<RespValue> args) {
        return args.getFirst();
    }
}
