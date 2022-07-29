package trading.timeframe;

import java.time.LocalDateTime;

/**
 * Snapshot of state at a specific time for the given generic object.
 */
public class Tick<T> {
    private final LocalDateTime time;
    private final T value;

    public Tick(LocalDateTime time, T value) {
        this.time = time;
        this.value = value;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public T getValue() {
        return value;
    }
}
