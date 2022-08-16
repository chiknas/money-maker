package services.httpclients.kraken.postrequestobjects;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PostRequestBody {
    private String nonce;
}
