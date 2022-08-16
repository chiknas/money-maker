package services.httpclients.kraken.response.ticker;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class TickerPairResponse {
    Map<String, TickerPair> result;
    List<String> error;
}
