package trading.indicators;

import trading.timeframe.Tick;
import trading.timeframe.Timeframe;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.function.Function;

public class MovingAverageIndicator implements Function<Timeframe<BigDecimal>, Timeframe<BigDecimal>> {

    private final int period;

    public MovingAverageIndicator(int period) {
        this.period = period;
    }

    @Override
    public Timeframe<BigDecimal> apply(Timeframe<BigDecimal> timeframe) {

        LinkedList<Tick<BigDecimal>> ticks = timeframe.getTicks();
        Timeframe<BigDecimal> movingAverageTimeFrame = new Timeframe<>(timeframe.size());
        for (int i = 0; i < ticks.size(); i++) {
            int startingIndex = i < period ? 0 : (i + 1 - period);
            LinkedList<Tick<BigDecimal>> periodTicks = new LinkedList<>(ticks.subList(startingIndex, i + 1));
            movingAverageTimeFrame.addTick(movingAverage(periodTicks));
        }
        return movingAverageTimeFrame;
    }

    private Tick<BigDecimal> movingAverage(LinkedList<Tick<BigDecimal>> ticks) {
        BigDecimal value = ticks.stream().map(Tick::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(ticks.size()));
        return new Tick<>(ticks.getLast().getTime(), value);
    }
}
