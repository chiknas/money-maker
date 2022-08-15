package entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import services.TimeService;
import services.strategies.tradingstrategies.TradingStrategy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * Table to keep track of all the details of each trade the system makes.
 */
@Entity
@Getter
@Setter
@Table(name = "trade_order")
public class TradeOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private BigInteger id;

    // unique id returned by the api for this order
    @Column(name = "order_reference")
    private String orderReference;

    // the trade pair code ex. BTCUSD
    @Column(name = "asset_code")
    private String assetCode;

    // the type of trade (BUY or SELL)
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TradingStrategy.TradingSignal type;

    // if the order has been fulfilled or pending
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TradeOrderStatus status;

    // how much of the asset we order
    @Column(name = "volume")
    private BigDecimal volume;

    // the price of the pair we traded on
    @Column(name = "price")
    private BigDecimal price;

    // the cost this trade occurred to the account balance
    @Column(name = "cost")
    private BigDecimal cost;

    // the specific time this trade occurred
    @Column(name = "time")
    private BigInteger time;

    public LocalDateTime getTime() {
        return TimeService.getLocalDateTimeNano(String.valueOf(time));
    }

    public void setTime(LocalDateTime time) {
        this.time = BigInteger.valueOf(TimeService.getMilliSeconds(time));
    }
}
