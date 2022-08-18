package services.trades;

import com.google.inject.Inject;
import database.daos.OrderDao;
import database.entities.TradeEntity;
import database.entities.TradeOrderStatus;
import services.httpclients.kraken.KrakenClient;
import services.httpclients.kraken.response.OrderStatus;
import services.httpclients.kraken.response.orderinfo.OrderInfoDetails;

public class OrderService {

    private final KrakenClient client;
    private final OrderDao orderDao;
    private final TradeService tradeService;

    @Inject
    public OrderService(OrderDao orderDao, KrakenClient client, TradeService tradeService) {
        this.orderDao = orderDao;
        this.client = client;
        this.tradeService = tradeService;
    }

    /**
     * Goes through the pending orders in the database and fires query to the API to get the most up to date data for them.
     * If the have been executed it will update the database with the values of the order fulfillment.
     */
    public void syncPendingOrders() {
        orderDao.findPendingOrders().forEach(order ->
                client.getOrderInfo(order.getOrderTransaction(), order.getOrderReference()).ifPresent(orderInfo -> {
                    OrderInfoDetails orderInfoDetails = orderInfo.getResult().getTransactionsMap().get(order.getOrderTransaction());
                    if (!OrderStatus.PENDING.equals(orderInfoDetails.getStatus()) && !OrderStatus.OPEN.equals(orderInfoDetails.getStatus())) {
                        order.setStatus(TradeOrderStatus.EXECUTED);
                        order.setPrice(orderInfoDetails.getPrice());
                        order.setCost(orderInfoDetails.getCost());
                        order.setFee(orderInfoDetails.getFee());
                        order.setVolumeExec(orderInfoDetails.getVolumeExec());

                        // if this was an order that closed a trade update the profit
                        tradeService.calculateTradeProfit(order.getTrade()).ifPresent(profit -> {
                            TradeEntity trade = order.getTrade();
                            trade.setProfit(profit);
                            tradeService.save(trade);
                        });

                        orderDao.save(order);
                    }
                }));
    }
}
