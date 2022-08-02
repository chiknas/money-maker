package entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import services.TimeService;
import services.strategies.TradingStrategy;

import java.math.BigDecimal;
import java.math.BigInteger;
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

    // the trade pair code ex. BTCUSD
    @Column(name = "asset_code", nullable = false)
    private String assetCode;

    // the type of trade (BUY or SELL)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TradingStrategy.TradingSignal type;

    // the price of the pair we traded on
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    // the cost this trade occurred to the account balance
    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    // the specific time this trade occurred
    @Column(name = "time", nullable = false)
    private BigInteger time;

    public LocalDateTime getTime() {
        return TimeService.getLocalDateTimeNano(String.valueOf(time));
    }

    public void setTime(LocalDateTime time) {
        this.time = BigInteger.valueOf(TimeService.getMilliSeconds(time));
    }
}