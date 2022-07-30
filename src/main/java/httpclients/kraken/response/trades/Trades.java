package httpclients.kraken.response.trades;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        long seconds = Long.parseLong(last) / 1_000_000_000;
        long nanos = Long.parseLong(last) % 1_000_000_000;

        return Instant.ofEpochSecond(seconds, nanos).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public List<TradeDetails> getTradeDetails(String assetCode) {
        return assetsMap.get(assetCode).stream()
                .map(details -> new TradeDetails((String) details.get(0), (String) details.get(1), (Double) details.get(2)))
                .collect(Collectors.toList());
    }
}
