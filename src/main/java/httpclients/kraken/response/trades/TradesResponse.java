package httpclients.kraken.response.trades;

import java.util.List;

public class TradesResponse {
    Trades result;
    List<String> error;

    public Trades getResult() {
        return result;
    }
}
