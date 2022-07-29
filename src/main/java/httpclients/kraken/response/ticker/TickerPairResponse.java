package httpclients.kraken.response.ticker;

import java.util.List;
import java.util.Map;

public class TickerPairResponse {
    Map<String, TickerPair> result;
    List<String> error;

    public Map<String, TickerPair> getResult() {
        return result;
    }
}
