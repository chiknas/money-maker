package httpclients.kraken.deserializers;

import com.google.gson.*;
import httpclients.kraken.response.trades.Trades;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class RecentTradesDeserializer implements JsonDeserializer<Trades> {
    @Override
    public Trades deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String last = jsonObject.remove("last").getAsString();

        Map<String, List<List<Object>>> assetsTrades = new Gson().fromJson(jsonObject.toString(), Map.class);

        return new Trades(last, assetsTrades);
    }
}
