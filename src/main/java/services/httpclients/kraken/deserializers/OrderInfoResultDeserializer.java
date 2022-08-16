package services.httpclients.kraken.deserializers;

import com.google.gson.*;
import services.httpclients.kraken.response.orderinfo.OrderInfoDetails;
import services.httpclients.kraken.response.orderinfo.OrderInfoResult;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class OrderInfoResultDeserializer implements JsonDeserializer<OrderInfoResult> {
    @Override
    public OrderInfoResult deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Gson gson = new Gson();
        Map<String, Map<String, String>> orderInfoResultResponse = gson.fromJson(jsonObject.toString(), Map.class);

        HashMap<String, OrderInfoDetails> result = new HashMap<>();
        orderInfoResultResponse.forEach((key, value1) -> {
            OrderInfoDetails value = gson.fromJson(gson.toJson(value1), OrderInfoDetails.class);
            result.put(key, value);
        });

        return new OrderInfoResult(result);
    }
}