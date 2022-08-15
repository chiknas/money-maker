package entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;

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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "entry_order_id", referencedColumnName = "id", nullable = false)
    private TradeOrderEntity entryOrder;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "exit_order_id", referencedColumnName = "id")
    private TradeOrderEntity exitOrder;

    @Column(name = "profit")
    private BigDecimal profit;

    public Duration getPeriodLength() {
        return Duration.parse(periodLength);
    }

    public void setPeriodLength(Duration periodLength) {
        this.periodLength = periodLength.toString();
    }
}
