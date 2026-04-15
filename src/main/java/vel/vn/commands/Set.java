package vel.vn.commands;

import vel.vn.InMemoryStorage;
import vel.vn.resp.RespValue;

import java.util.List;

public class Set implements Command{
    @Override
    public RespValue execute(List<RespValue> args) {
        var storage = InMemoryStorage.getInstance();
        Long expiryMillis = null;
        if (args.size() > 2) {
            if(args.get(2) instanceof RespValue.BulkString) {
                var option = ((RespValue.BulkString) args.get(2)).value();
                switch (option.toUpperCase()) {
                    case "EX":
                        if (args.get(3) instanceof RespValue.Long) {
                            expiryMillis = ((RespValue.Long) args.get(3)).value() * 1000;
                        } else {
                            return new RespValue.Error("ERR", "after EX should be a number");
                        }
                        break;
                    case "PX":
                        if (args.get(3) instanceof RespValue.Long) {
                            expiryMillis = ((RespValue.Long) args.get(3)).value();
                        } else {
                            return new RespValue.Error("ERR", "after PX should be a number");
                        }
                        break;
                    default:
                        return new RespValue.Error("ERR", "Unknown option: "+ option);
                }
            } else {
                return new RespValue.Error("ERR", "Unknown 3rd argument of SET ");
            }
        }
        storage.put(args.getFirst().encode(), args.get(1), expiryMillis);
        return RespValue.OK;
    }
}
