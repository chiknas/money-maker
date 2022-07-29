package httpclients.kraken;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KrakenAuthentication {

    private final Logger logger;
    private final String apiPublicKey;
    private final String apiPrivateKey;

    @Inject
    public KrakenAuthentication(@Named("ApiPublicKey") String apiPublicKey, @Named("ApiPrivateKey") String apiPrivateKey, Logger logger) {
        this.apiPublicKey = apiPublicKey;
        this.apiPrivateKey = apiPrivateKey;
        this.logger = logger;
    }

    protected String getApiSignature(String path, String nonce, String data, String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((nonce + data).getBytes());
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(Base64.getDecoder().decode(secret.getBytes()), "HmacSHA512"));
            mac.update(path.getBytes());
            return new String(Base64.getEncoder().encode(mac.doFinal(md.digest())));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return "";
    }

    protected String[] getSecurityHeaders(String path, String nonce, String data) {
        return new String[]{
                "API-Key", apiPublicKey,
                "API-Sign", getApiSignature(path, nonce, data, apiPrivateKey),
                "Content-Type", "application/x-www-form-urlencoded; charset=utf-8"
        };
    }

    protected String getNonce() {
        return String.valueOf(System.currentTimeMillis());
    }
}
