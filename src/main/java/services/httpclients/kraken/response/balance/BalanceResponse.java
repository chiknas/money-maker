package services.httpclients.kraken.response.balance;

import lombok.Getter;

import java.util.List;

@Getter
public class BalanceResponse {
    BalanceResult result;
    List<String> error;
}
