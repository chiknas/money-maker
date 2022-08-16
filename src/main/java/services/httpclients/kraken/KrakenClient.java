package services.httpclients.kraken;

import com.google.gson.Gson;
import com.google.inject.Inject;
import services.httpclients.AbstractClient;
import services.httpclients.kraken.postrequestobjects.addorder.AddOrderPostRequestBody;
import services.httpclients.kraken.postrequestobjects.addorder.KrakenOrderDirection;
import services.httpclients.kraken.postrequestobjects.addorder.KrakenOrderType;
import services.httpclients.kraken.response.addorder.AddOrderResponse;
import services.httpclients.kraken.response.ticker.TickerPairResponse;
import services.httpclients.kraken.response.trades.TradesResponse;
import services.strategies.tradingstrategies.TradingStrategy;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

public class KrakenClient extends AbstractClient {

    private final KrakenAuthentication krakenAuthentication;

    @Inject
    public KrakenClient(KrakenAuthentication krakenAuthentication, HttpClient httpClient, Gson gson) {
        super(httpClient, gson);
        this.krakenAuthentication = krakenAuthentication;
    }

    @Override
    protected String getURI() {
        return "https://api.kraken.com";
    }

    // https://docs.kraken.com/rest/#tag/Market-Data/operation/getTickerInformation
    public Optional<TickerPairResponse> getTickerInfo(String assetCode) {
        Optional<TickerPairResponse> tickerPairResponse = getRequest("/0/public/Ticker?pair=" + assetCode).flatMap(request -> super.send(request, TickerPairResponse.class));
        tickerPairResponse.ifPresent(response -> logErrors(response.getError()));
        return tickerPairResponse;
    }

    // https://docs.kraken.com/rest/#tag/User-Data/operation/getAccountBalance
    public Optional<String> getBalance() {
        String path = "/0/private/Balance";
        String nonce = krakenAuthentication.getNonce();
        String data = "nonce=" + nonce;
        return postRequest(data, path, krakenAuthentication.getSecurityHeaders(path, nonce, data)).flatMap(super::send);
    }

    // https://docs.kraken.com/rest/#tag/Market-Data/operation/getOHLCData
    public Optional<TradesResponse> getHistoricData(String assetCode) {
        return getHistoricData(assetCode, null);
    }

    /**
     * Returns historic data for the specified asset and for the specified timeframe/period.
     * period can be one of the following values:
     * 1 5 15 30 60 240 1440 10080 21600
     * https://docs.kraken.com/rest/#tag/Market-Data/operation/getOHLCData
     */
    public Optional<TradesResponse> getHistoricData(String assetCode, Duration period) {
        String intervalParam = Optional.ofNullable(period)
                .map(periodDuration -> "&interval=" + (periodDuration.toMinutes() > 0 ? periodDuration.toMinutes() : "1"))
                .orElse("");
        String requestUrl = "/0/public/OHLC?pair=" + assetCode + intervalParam;

        Optional<TradesResponse> tradesResponse = getRequest(requestUrl).flatMap(request -> super.send(request, TradesResponse.class));
        tradesResponse.ifPresent(response -> logErrors(response.getError()));
        return tradesResponse;
    }

    /**
     * Add a new order of type 'market'. A market order is designed to be executed immediately.
     * https://docs.kraken.com/rest/#tag/User-Trading/operation/addOrder
     */
    public Optional<AddOrderResponse> postMarketOrder(String assetCode, BigDecimal volume, TradingStrategy.TradingSignal tradingSignal) {
        String path = "/0/private/AddOrder";
        String nonce = krakenAuthentication.getNonce();
        KrakenOrderDirection orderDirection = TradingStrategy.TradingSignal.BUY.equals(tradingSignal) ? KrakenOrderDirection.BUY : KrakenOrderDirection.SELL;
        AddOrderPostRequestBody postRequestBody = AddOrderPostRequestBody.builder()
                .nonce(nonce)
                .pair(assetCode)
                .orderType(KrakenOrderType.MARKET)
                .orderDirection(orderDirection)
                .volume(volume)
                .validate(true)
                .build();
        String data = postRequestBody.toString();

        Optional<AddOrderResponse> addOrderResponse = postRequest(data, path, krakenAuthentication.getSecurityHeaders(path, nonce, data))
                .flatMap(request -> super.send(request, AddOrderResponse.class));
        addOrderResponse.ifPresent(response -> logErrors(response.getError()));
        return addOrderResponse;
    }
}
