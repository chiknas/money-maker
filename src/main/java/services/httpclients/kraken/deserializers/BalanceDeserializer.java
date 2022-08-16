package services.httpclients.kraken.deserializers;

import com.google.gson.*;
import services.httpclients.kraken.response.balance.BalanceResult;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BalanceDeserializer implements JsonDeserializer<BalanceResult> {
    @Override
    public BalanceResult deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Map<String, String> assetsBalancesResponse = new Gson().fromJson(jsonObject.toString(), Map.class);
        Map<String, BigDecimal> assetsBalances = new HashMap<>();
        assetsBalancesResponse.forEach((key, value) -> assetsBalances.put(key, new BigDecimal(value)));

        return new BalanceResult(assetsBalances);
    }
}
