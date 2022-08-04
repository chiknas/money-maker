package properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.Optional;

@Getter
@AllArgsConstructor
@PropertySuffix("strategy.3EmaCrossover")
public class ThreeEmaCrossoverStrategyProperties {

    @Getter(AccessLevel.NONE)
    private String periodLength;
    private Integer shortPeriod;
    private Integer mediumPeriod;
    private Integer longPeriod;

    public Duration getPeriodLength() {
        return Optional.ofNullable(this.periodLength)
                .map(Duration::parse)
                .orElse(Duration.ofSeconds(1));
    }
}
