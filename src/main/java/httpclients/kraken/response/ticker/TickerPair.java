package httpclients.kraken.response.ticker;

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

    public List<String> getAskArray() {
        return this.a;
    }
}
