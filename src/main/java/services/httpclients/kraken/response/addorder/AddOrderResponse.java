package services.httpclients.kraken.response.addorder;

import lombok.Getter;

import java.util.List;

@Getter
public class AddOrderResponse {
    AddOrderResult result;
    List<String> error;
}
