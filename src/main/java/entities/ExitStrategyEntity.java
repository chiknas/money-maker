package entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import services.TimeService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "exit_strategy")
public class ExitStrategyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private BigInteger id;

    // the name of the exit strategy used
    @Column(name = "name")
    private String name;

    // the price of the asset when this exit price was calculated
    @Column(name = "current_price")
    private BigDecimal currentPrice;

    // the price where you should exit the market
    @Column(name = "exit_price")
    private BigDecimal exitPrice;

    // the specific time this exit strategy was updated
    @Column(name = "last_update")
    private BigInteger lastUpdate;

    public LocalDateTime getLastUpdate() {
        return TimeService.getLocalDateTimeNano(String.valueOf(lastUpdate));
    }

    public void setLastUpdate(LocalDateTime time) {
        this.lastUpdate = BigInteger.valueOf(TimeService.getMilliSeconds(time));
    }
}
