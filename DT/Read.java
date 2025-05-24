import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Read {
    private static final String SEPARATOR = ",";

    public static Set<Stock> readTrainData(String filePath){
        Set<Stock> data = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // read over header line
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(SEPARATOR);
                try{
                    Stock p = new Stock(count, values[0], values[1], values[2], values[3], values[4], values[5]);
                    data.add(p);
                }catch(IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static Set<Stock> readTestData(String filePath){
        Set<Stock> data = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // skip header
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(SEPARATOR);
                try{
                    Stock p = new Stock(count, values[0], values[1], values[2], values[3], values[4]);
                    data.add(p);
                }catch(IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}