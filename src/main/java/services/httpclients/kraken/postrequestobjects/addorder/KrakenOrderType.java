package services.httpclients.kraken.postrequestobjects.addorder;

import lombok.Getter;

/**
 * Available orders in the kraken api when adding a new order.
 * <a href="https://docs.kraken.com/rest/#tag/User-Trading/operation/addOrder">Kraken order types</a>
 */
@Getter
public enum KrakenOrderType {
    MARKET("market"),
    LIMIT("limit"),
    TAKE_PROFIT("take-profit"),
    STOP_LOSS_LIMIT("stop-loss-limit"),
    TAKE_PROFIT_LIMIT("take-profit-limit"),
    SETTLE_POSITION("settle-position"),
    STOP_LOSS("stop-loss");

    private final String orderType;

    KrakenOrderType(String orderType) {
        this.orderType = orderType;
    }
}
