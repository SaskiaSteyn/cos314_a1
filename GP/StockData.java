public class StockData {
    float open;
    float high;
    float low;
    float close;
    float adjclose;
    float output;

    StockData(float o, float h, float l, float c, float v, float out) {
        open = o;
        high = h;
        low = l;
        close = c;
        adjclose = v;
        output = out;
    }

    float getOpen() {
        return open;
    }

    float getHigh() {
        return high;
    }

    float getLow() {
        return low;
    }

    float getClose() {
        return close;
    }

    float getAdjclose() {
        return adjclose;
    }
}
