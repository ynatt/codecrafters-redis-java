package vel.vn.commands;

import vel.vn.InMemoryStorage;
import vel.vn.resp.RespValue;

import java.util.List;

public class Get implements Command{
    @Override
    public RespValue execute(List<RespValue> args) {
        return InMemoryStorage.getInstance().get(args.getFirst().encode());
    }
}
