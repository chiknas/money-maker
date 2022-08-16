package services.httpclients.kraken.response.orderinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class OrderInfoResult {
    private Map<String, OrderInfoDetails> transactionsMap;
}
