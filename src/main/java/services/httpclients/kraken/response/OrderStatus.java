package services.httpclients.kraken.response;

import com.google.gson.annotations.SerializedName;

public enum OrderStatus {
    @SerializedName("pending")
    PENDING,

    @SerializedName("open ")
    OPEN,

    @SerializedName("closed")
    CLOSED,

    @SerializedName("canceled")
    CANCELED,

    @SerializedName("expired")
    EXPIRED;
}
