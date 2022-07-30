package httpclients.kraken.response.trades;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TradeDetails {
    private final String price;
    private final String volume;
    private final Double time;

    public TradeDetails(String price, String volume, Double time) {
        this.price = price;
        this.volume = volume;
        this.time = time;
    }

    public BigDecimal getPrice() {
        return new BigDecimal(price);
    }

    public BigDecimal getVolume() {
        return new BigDecimal(volume);
    }

    public LocalDateTime getTime() {
        return LocalDateTime.ofEpochSecond(
                time.longValue(), 0, ZoneOffset.UTC);
    }
}
