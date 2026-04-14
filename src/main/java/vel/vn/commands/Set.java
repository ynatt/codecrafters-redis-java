package vel.vn.commands;

import vel.vn.InMemoryStorage;
import vel.vn.resp.RespValue;

import java.util.List;

public class Set implements Command{
    @Override
    public RespValue execute(List<RespValue> args) {
        var storage = InMemoryStorage.getInstance();
        storage.put(args.getFirst().encode(), args.get(1));
        return RespValue.OK;
    }
}
