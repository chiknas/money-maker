package services.httpclients.kraken.postrequestobjects.addorder;

import lombok.Getter;

/**
 * Available directions in the kraken api when adding a new order.
 * <a href="https://docs.kraken.com/rest/#tag/User-Trading/operation/addOrder">Kraken order directions</a>
 */
@Getter
public enum KrakenOrderDirection {

    BUY("buy"),
    SELL("sell");

    private final String direction;

    KrakenOrderDirection(String direction) {
        this.direction = direction;
    }
}
