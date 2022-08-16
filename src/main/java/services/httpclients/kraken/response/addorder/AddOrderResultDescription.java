package services.httpclients.kraken.response.addorder;

import lombok.Getter;

@Getter
public class AddOrderResultDescription {
    // Order description
    // ex.
    private String order;

    // Conditional close order description, if applicable
    private String close;
}
