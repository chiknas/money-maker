package valueobjects.timeframe;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Snapshot of state at a specific time for the given generic object.
 */
@Getter
public class Tick {
    private final LocalDateTime time;
    private final BigDecimal value;

    public Tick(LocalDateTime time, BigDecimal value) {
        this.time = time;
        this.value = value;
    }
}
