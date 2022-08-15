package valueobjects.timeframe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * Keeps track of the specified object information over time. It holds a specified amount of ticks.
 * When a new ticker is inserted the oldest one is removed.
 */
public class Timeframe {
    private final LinkedBlockingDeque<Tick> timeFrame;

    public Timeframe(int ticksLimits) {
        timeFrame = new LinkedBlockingDeque<>(ticksLimits);
    }

    public Timeframe(int ticksLimits, Collection<Tick> values) {
        List<Tick> timeSortedTickers = values.stream()
                // sort tickers by time in case they come in in random order
                .sorted(Comparator.comparing(Tick::getTime))
                .collect(Collectors.toList());
        timeFrame = new LinkedBlockingDeque<>(ticksLimits);
        timeFrame.addAll(timeSortedTickers);
    }

    public LinkedList<Tick> addTick(BigDecimal value) {
        Tick tick = new Tick(LocalDateTime.now(), value);
        return addTick(tick);
    }

    public LinkedList<Tick> addTick(Tick tick) {
        if (timeFrame.remainingCapacity() == 0) {
            timeFrame.poll();
        }
        timeFrame.add(tick);

        return new LinkedList<>(timeFrame);
    }

    public LinkedList<Tick> getTicks() {
        return new LinkedList<>(timeFrame);
    }

    public int size() {
        return getTicks().size();
    }

    /**
     * Returns a boolean that specifies if the current timeframe is at max capacity.
     */
    public boolean isFull() {
        return timeFrame.remainingCapacity() == 0;
    }

    /**
     * Get the first portion of the timeframe. The size of the portion is the specified int.
     */
    public Timeframe subframe(int index) {
        return subframe(0, index);
    }

    /**
     * Returns a partial timeframe based on the specified indexes.
     */
    public Timeframe subframe(int fromIndex, int toIndex) {
        List<Tick> subTicks = this.getTicks().subList(fromIndex, toIndex);
        return new Timeframe(subTicks.size(), subTicks);
    }

    public int crossover(Timeframe timeframe) {
        // Last 2 prices of the current timeframe
        Iterator<Tick> aTickIterator = this.getTicks().descendingIterator();
        BigDecimal currentA = aTickIterator.next().getValue();
        BigDecimal previousA = aTickIterator.next().getValue();

        // Last 2 prices of the specified timeframe
        Iterator<Tick> bTickIterator = timeframe.getTicks().descendingIterator();
        BigDecimal currentB = bTickIterator.next().getValue();
        BigDecimal previousB = bTickIterator.next().getValue();

        int currentSignum = currentA.subtract(currentB).signum();
        int previousSignum = previousA.subtract(previousB).signum();

        if (currentSignum != previousSignum) {
            return currentSignum;
        }

        return 0;
    }
}
