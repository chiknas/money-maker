package httpclients.kraken;

import com.google.inject.Inject;
import httpclients.AbstractClient;
import httpclients.kraken.response.ticker.TickerPairResponse;

import java.net.http.HttpClient;
import java.util.Optional;
import java.util.logging.Logger;

public class KrakenClient extends AbstractClient {


    private final KrakenAuthentication krakenAuthentication;

    @Inject
    public KrakenClient(KrakenAuthentication krakenAuthentication, Logger logger, HttpClient httpClient) {
        super(logger, httpClient);
        this.krakenAuthentication = krakenAuthentication;
    }

    @Override
    protected String getURI() {
        return "https://api.kraken.com";
    }

    // https://docs.kraken.com/rest/#tag/Market-Data/operation/getTickerInformation
    public Optional<TickerPairResponse> getTickerInfo(String assetCode) {
        return getRequest("/0/public/Ticker?pair=" + assetCode).flatMap(request -> super.send(request, TickerPairResponse.class));
    }

    // https://docs.kraken.com/rest/#tag/User-Data/operation/getAccountBalance
    public Optional<String> getBalance() {
        String path = "/0/private/Balance";
        String nonce = krakenAuthentication.getNonce();
        String data = "nonce=" + nonce;
        return postRequest(data, path, krakenAuthentication.getSecurityHeaders(path, nonce, data)).flatMap(super::send);
    }
}
