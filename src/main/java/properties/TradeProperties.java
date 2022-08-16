package properties;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@PropertySuffix("trade")
public class TradeProperties {
    private String assetCode;
    private String detailAssetCode;
    // you can buy this assetCode pair with this asset code (use it to find balance)
    private String buyAssetCode;
    // you can sell this assetCode pair with this asset code (use it to find balance)
    private String sellAssetCode;
    private BigDecimal accountRisk;
}
