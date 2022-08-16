package services.httpclients.kraken.response.addorder;

import lombok.Getter;

import java.util.List;

@Getter
public class AddOrderResult {
    // Transaction IDs for order (if order was added successfully)
    private List<String> txid;

    // Order description info
    private AddOrderResultDescription descr;
}
