package trading.timeframe;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Keeps track of the specified object information over time. It holds a specified amount of ticks.
 * When a new ticker is inserted the oldest one is removed.
 */
public class Timeframe<T> {
    private final LinkedBlockingDeque<Tick<T>> timeFrame;

    public Timeframe(int ticksLimits) {
        timeFrame = new LinkedBlockingDeque<>(ticksLimits);
    }

    public LinkedList<Tick<T>> addTick(T value) {
        Tick<T> tick = new Tick<>(LocalDateTime.now(), value);
        if (timeFrame.remainingCapacity() == 0) {
            timeFrame.poll();
        }
        timeFrame.add(tick);

        return new LinkedList<>(timeFrame);
    }
}
