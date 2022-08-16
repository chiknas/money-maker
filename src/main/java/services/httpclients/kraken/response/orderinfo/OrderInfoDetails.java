package services.httpclients.kraken.response.orderinfo;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import services.httpclients.kraken.response.OrderStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Getter
public class OrderInfoDetails {
    private String refid;
    @Getter(AccessLevel.NONE)
    private String userref;
    private OrderStatus status;
    private BigDecimal price;
    private BigDecimal cost;
    private BigDecimal fee;
    // the coin volume we asked to trade
    @SerializedName("vol")
    private BigDecimal volume;
    // the coin volume we actually traded
    @SerializedName("vol_exec")
    private BigDecimal volumeExec;

    public Optional<UUID> getOrderReference() {
        return userref.length() == 32 ? Optional.of(UUID.fromString(userref)) : Optional.empty();
    }
}
