package services.httpclients.kraken.response.trades;

import lombok.Getter;

import java.util.List;

@Getter
public class TradesResponse {
    Trades result;
    List<String> error;
}
