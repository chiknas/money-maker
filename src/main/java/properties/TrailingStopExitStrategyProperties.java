package properties;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@PropertySuffix("exit.strategy.TrailingStop")
public class TrailingStopExitStrategyProperties {
    private BigDecimal distance;
}
