package services.graphs;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import lombok.extern.slf4j.Slf4j;
import valueobjects.timeframe.Timeframe;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TimeframePlotService {

    public <T extends Number> void plot(Map<String, Timeframe<T>> timeFrames) {
        Plot plt = Plot.create();

        timeFrames.forEach((key, timeframe) -> {
            List<Double> x = timeframe.getTicks().stream().map(tick -> (double) tick.getTime().atZone(ZoneId.systemDefault()).toEpochSecond()).collect(Collectors.toList());
            List<Double> y = x.stream().map(xi -> timeframe.getTicks().stream().filter(tick -> tick.getTime().atZone(ZoneId.systemDefault()).toEpochSecond() == xi).findFirst().get().getValue().doubleValue()).collect(Collectors.toList());

            plt.plot().add(x, y).label(key);
        });

        plt.legend().loc("upper right");
        plt.title("scatter");

        try {
            plt.show();
        } catch (IOException | PythonExecutionException e) {
            log.error(e.getMessage(), e);
        }

    }
}
