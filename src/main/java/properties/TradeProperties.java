package properties;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@PropertySuffix("trade")
public class TradeProperties {

    public final static String LEVERAGE_SEPARATOR = ":";

    private String assetCode;
    private String detailAssetCode;
    // you can buy this assetCode pair with this asset code (use it to find balance)
    private String buyAssetCode;
    // you can sell this assetCode pair with this asset code (use it to find balance)
    private String sellAssetCode;
    private BigDecimal accountRisk;
    private Boolean paperTrading;
    private String leverage;

    public boolean usesLeverage() {
        return !this.leverage.split(LEVERAGE_SEPARATOR)[0].equals("1");
    }
}
