package services.httpclients.kraken.postrequestobjects.addorder;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import services.httpclients.kraken.postrequestobjects.PostRequestBody;

import java.math.BigDecimal;

@Getter
@SuperBuilder
public class AddOrderPostRequestBody extends PostRequestBody {

    // Unique id generated by the system to identify this order
    private int orderReference;

    private KrakenOrderType orderType;
    private KrakenOrderDirection orderDirection;

    // Order quantity in terms of the base asset.
    // Note: Volume can be specified as 0 for closing margin orders to automatically fill the requisite quantity.
    private BigDecimal volume;

    //Asset pair id or altname
    private String pair;

    // Validate inputs only. Do not submit order.
    private Boolean validate;

    // Leverage to use for this trade. ex. 2:1 = 2x
    private String leverage;

    /**
     * Kraken post request value input format.
     * TODO: refactor this into a common method somewhere (superclass maybe) so we dont have to override the toString
     * all the time
     */
    @Override
    public String toString() {
        return "nonce=" + getNonce() +
                "&userref=" + orderReference +
                (leverage != null ? "&leverage=" + leverage : "") +
                "&ordertype=" + orderType.getOrderType() +
                "&type=" + orderDirection.getDirection() +
                "&volume=" + volume +
                "&pair=" + pair +
                "&validate=" + validate;
    }
}
