package properties;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@PropertySuffix("strategy.exit.TrailingStop")
public class TrailingStopExitStrategyProperties {
    private BigDecimal distance;
}
