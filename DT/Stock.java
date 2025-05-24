import java.util.HashMap;
import java.util.Map;

public class Stock {
    private int id;
    private Map<String, Integer> attributes;

    public Stock(int id, String open, String high, String low, String close, String adj_close, String output){
        this.id = id;
        attributes = new HashMap<>();
        attributes.put(Attribute.OPEN, double_to_category(open));
        attributes.put(Attribute.HIGH, double_to_category(high));
        attributes.put(Attribute.LOW, double_to_category(low));
        attributes.put(Attribute.CLOSE, double_to_category(close));
        attributes.put(Attribute.ADJ_CLOSE, double_to_category(adj_close));
        attributes.put(Attribute.OUTPUT, Integer.parseInt(output));
    }

    //test stock
    public Stock(int id, String open, String high, String low, String close, String adj_close){
        this.id = id;
        attributes = new HashMap<>();
        attributes.put(Attribute.OPEN, double_to_category(open));
        attributes.put(Attribute.HIGH, double_to_category(high));
        attributes.put(Attribute.LOW, double_to_category(low));
        attributes.put(Attribute.CLOSE, double_to_category(close));
        attributes.put(Attribute.ADJ_CLOSE, double_to_category(adj_close));
    }

    private int double_to_category(String input){
        double number = Double.parseDouble(input);
        if (number <= -2) return 0;
        else if (number <= -1) return 1;
        else if (number <= 0) return 2;
        else if (number <= 1) return 3;
        else if (number <= 2) return 4;
        else return 5;
    }

    public int getAttributeValue(String attribute){
        return attributes.get(attribute);
    }

    public int getId(){
        return this.id;
    }
}