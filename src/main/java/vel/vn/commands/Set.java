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
                        switch (args.get(3)) {
                            case RespValue.Long number:
                                expiryMillis = number.value() * 1000;
                                break;
                            case RespValue.BulkString string:
                                expiryMillis = Long.parseLong(string.value()) * 1000;
                                break;
                            default:
                                return new RespValue.Error("ERR", "after EX should be a bulk string with number");
                        }
                        break;
                    case "PX":
                        switch (args.get(3)) {
                            case RespValue.Long number:
                                expiryMillis = number.value();
                                break;
                            case RespValue.BulkString string:
                                expiryMillis = Long.parseLong(string.value());
                                break;
                            default:
                                return new RespValue.Error("ERR", "after PX should be a bulk string with number");
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
