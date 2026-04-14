package vel.vn.commands;

import vel.vn.resp.RespValue;

import java.util.List;

public class Echo implements Command{

    public RespValue execute(List<RespValue> args) {
        return args.getFirst();
    }
}
