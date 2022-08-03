package properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.Optional;

@Getter
@AllArgsConstructor
@PropertySuffix("strategy.goldenCross")
public class GoldenCrossStrategyProperties {

    @Getter(AccessLevel.NONE)
    private String periodLength;
    private Integer shortPeriod;
    private Integer longPeriod;

    public Duration getPeriodLength() {
        return Optional.ofNullable(this.periodLength)
                .map(Duration::parse)
                .orElse(Duration.ofSeconds(1));
    }
}
