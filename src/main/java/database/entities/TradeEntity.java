package database.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@Table(name = "trade")
public class TradeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private BigInteger id;

    @Column(name = "entry_strategy", nullable = false)
    private String entryStrategy;

    @Column(name = "period_length", nullable = false)
    private String periodLength;

    @Column(name = "exit_strategy", nullable = false)
    private String exitStrategy;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "trade")
    private List<TradeOrderEntity> orders = new ArrayList<>();

    @Column(name = "profit")
    private BigDecimal profit;

    public Duration getPeriodLength() {
        return Duration.parse(periodLength);
    }

    public void setPeriodLength(Duration periodLength) {
        this.periodLength = periodLength.toString();
    }

    public List<TradeOrderEntity> addOrder(TradeOrderEntity order) {
        order.setTrade(this);
        this.orders.add(order);
        return this.orders;
    }

    public Optional<TradeOrderEntity> getExitOrder() {
        return orders.stream().filter(order -> TradeOrderType.EXIT.equals(order.getType())).findFirst();
    }

    public TradeOrderEntity getEntryOrder() {
        return orders.stream().filter(order -> TradeOrderType.ENTRY.equals(order.getType()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Trade with id: " + this.id + " , was found without an entry order."));
    }
}
