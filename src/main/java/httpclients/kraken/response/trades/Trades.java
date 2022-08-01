package httpclients.kraken.response.trades;

import services.TimeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Trades {
    private final String last;
    private final Map<String, List<List<Object>>> assetsMap;

    public Trades(String last, Map<String, List<List<Object>>> assetsMap) {
        this.last = last;
        this.assetsMap = assetsMap;
    }

    // Api returns the last trade timestamp in nano seconds
    public LocalDateTime getLast() {
        return TimeService.getLocalDateTimeNano(last);
    }

    public List<TradeDetails> getTradeDetails(String assetCode) {
        return assetsMap.get(assetCode).stream()
                .map(details -> new TradeDetails((String) details.get(4), (String) details.get(6), (Double) details.get(0)))
                .collect(Collectors.toList());
    }
}
