package services.httpclients.kraken.response.ticker;

import java.math.BigDecimal;
import java.util.List;

public class TickerPair {
    private List<String> a;
    private List<String> b;
    private List<String> c;
    private List<String> v;
    private List<String> p;
    private List<String> t;
    private List<String> l;
    private List<String> h;
    private String o;

    public BigDecimal getCurrentPrice() {
        return new BigDecimal(this.c.get(0));
    }
}
