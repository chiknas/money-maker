package entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import services.TimeService;
import services.strategies.TradingStrategy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Table to keep track of all the details of each trade the system makes.
 */
@Entity
@Getter
@Setter
@Table(name = "trade_transaction")
public class TradeTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private BigInteger id;

    // the strategy algorithm used to execute these trade
    @Column(name = "strategy")
    private String strategy;

    // the trade pair code ex. BTCUSD
    @Column(name = "asset_code")
    private String assetCode;

    // the type of trade (BUY or SELL)
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TradingStrategy.TradingSignal type;

    // the price of the pair we traded on
    @Column(name = "price")
    private BigDecimal price;

    // the cost this trade occurred to the account balance
    @Column(name = "cost")
    private BigDecimal cost;

    // the specific time this trade occurred
    @Column(name = "time")
    private BigInteger time;

    // the period time length for each candlestick
    @Column(name = "period_length")
    private String periodLength;

    public LocalDateTime getTime() {
        return TimeService.getLocalDateTimeNano(String.valueOf(time));
    }

    public void setTime(LocalDateTime time) {
        this.time = BigInteger.valueOf(TimeService.getMilliSeconds(time));
    }

    public Duration getPeriodLength() {
        return Duration.parse(periodLength);
    }

    public void setPeriodLength(Duration periodLength) {
        this.periodLength = periodLength.toString();
    }
}
