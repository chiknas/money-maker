package services.httpclients.kraken.response.orderinfo;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderInfoResponse {
    private OrderInfoResult result;
    private List<String> error;
}
