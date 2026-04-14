package vel.vn.commands;

import vel.vn.resp.RespValue;

import java.util.List;

public interface Command {

    RespValue execute(List<RespValue> args);
}
