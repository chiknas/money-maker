package services.httpclients.kraken.response.balance;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
public class AccountBalanceResponse {
    Map<String, BigDecimal> result;
    List<String> error;
    
    public BigDecimal getAccountBalance() {
        return result.get("eb");
    }
}
