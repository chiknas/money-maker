package services.httpclients.kraken.postrequestobjects.addorder;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import services.httpclients.kraken.postrequestobjects.PostRequestBody;

import java.math.BigDecimal;

@Getter
@SuperBuilder
public class AddOrderPostRequestBody extends PostRequestBody {

    private KrakenOrderType orderType;
    private KrakenOrderDirection orderDirection;

    // Order quantity in terms of the base asset.
    // Note: Volume can be specified as 0 for closing margin orders to automatically fill the requisite quantity.
    private BigDecimal volume;

    //Asset pair id or altname
    private String pair;

    // Validate inputs only. Do not submit order.
    private Boolean validate;

    /**
     * Kraken post request value input format.
     * TODO: refactor this into a common method somewhere (superclass maybe) so we dont have to override the toString
     * all the time
     */
    @Override
    public String toString() {
        return "nonce=" + getNonce() +
                "&ordertype=" + orderType.getOrderType() +
                "&type=" + orderDirection.getDirection() +
                "&volume=" + volume +
                "&pair=" + pair +
                "&validate=" + validate;
    }
}
