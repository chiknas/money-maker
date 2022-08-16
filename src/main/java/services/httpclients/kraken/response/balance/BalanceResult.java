package services.httpclients.kraken.response.balance;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Builder
@AllArgsConstructor
public class BalanceResult {
    private final Map<String, BigDecimal> assetsMap;

    public BigDecimal getAssetBalance(String assetCode) {
        return assetsMap.get(assetCode);
    }
}
