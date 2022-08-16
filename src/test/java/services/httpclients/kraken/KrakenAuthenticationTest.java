package services.httpclients.kraken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KrakenAuthenticationTest {
    KrakenAuthentication krakenAuthentication;
    String privateKey = "kQH5HW/8p1uGOVjbgWA7FunAmGO8lsSUXNsu3eow76sz84Q18fWxnyRzBHCd3pd5nE9qa99HAZtuZuj6F1huXg==";
    String publicKey = "bdfghdsryh354q4fgsdgfzasdfzdc";

    @BeforeEach
    public void setUp() {
        krakenAuthentication = new KrakenAuthentication(publicKey, privateKey);
    }

    @Test
    void getApiSignature() {

        String nonce = "1616492376594";
        String data = "nonce=1616492376594&ordertype=limit&pair=XBTUSD&price=37500&type=buy&volume=1.25";
        String uriPath = "/0/private/AddOrder";

        String expectedSignature = "4/dpxb3iT4tp/ZCVEwSnEsLxx0bqyhLpdfOpc6fn7OR8+UClSV5n9E6aSS8MPtnRfp32bAb0nmbRn6H8ndwLUQ==";

        // thats how the api signature needs to be calculated
        // https://docs.kraken.com/rest/#section/Authentication/Headers-and-Signature
        assertEquals(expectedSignature, krakenAuthentication.getApiSignature(uriPath, nonce, data, privateKey));
    }

    @Test
    void getSecurityHeaders() {
        String path = "/security/headers";
        String nonce = "1616492376594";
        String data = "data";
        String[] securityHeaders = krakenAuthentication.getSecurityHeaders(path, nonce, data);

        // thats what every request to private Kraken API endpoints should have
        // https://docs.kraken.com/rest/#section/Authentication
        String[] expectedResponse = new String[]{
                "API-Key", publicKey,
                "API-Sign", "AS0SuSeYYJ9lyx38VSsF+6tqUzR+aiyHBgswvrdnPa1LZFVPJNGnWDX1nCuEDc4NJSjFT+YFtwm+N6zIsqeKAA==",
                "Content-Type", "application/x-www-form-urlencoded; charset=utf-8"
        };

        assertArrayEquals(expectedResponse, securityHeaders);
    }

    @Test
    void getNonce() {
        // The Kraken nonce is an incremental number (goes up every time we fire a request)
        // So in this impl we are using the system time in millis.
        long nonce = Long.parseLong(krakenAuthentication.getNonce());
        long currentMillis = System.currentTimeMillis();

        // test the 2 numbers are close. Give it a 100ms buffer in case it runs on a slow system
        assertTrue(Math.abs(nonce - currentMillis) < 100);
    }
}