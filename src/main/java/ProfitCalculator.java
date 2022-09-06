import com.google.inject.Guice;
import com.google.inject.Injector;
import database.DatabaseModule;
import database.entities.TradeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import services.httpclients.HttpClientModule;
import services.httpclients.kraken.KrakenModule;
import services.strategies.TradingStrategiesModule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class ProfitCalculator {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule(), new DatabaseModule(), new TradingStrategiesModule());
        EntityManager entityManager = injector.getInstance(EntityManager.class);

        String hql = "FROM TradeEntity t";
        Query query = entityManager.createQuery(hql);
        List<TradeEntity> trades = ((List<TradeEntity>) query.getResultList()).stream().filter(trade -> trade.getProfit() != null).collect(Collectors.toList());

        BigDecimal initialInvestment = new BigDecimal("1000");
        BigDecimal feePercentage = new BigDecimal("0.005");

        for (TradeEntity trade : trades) {
            initialInvestment = initialInvestment.add(initialInvestment.multiply(trade.getProfit().divide(BigDecimal.valueOf(100L), RoundingMode.HALF_EVEN)));
            BigDecimal fee = initialInvestment.multiply(feePercentage);
            initialInvestment = initialInvestment.subtract(fee);
        }

        System.out.println(initialInvestment);
    }
}
