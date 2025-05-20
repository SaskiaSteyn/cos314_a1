import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filePath = "./BTC_train.csv";
        List<StockData> trainData = readStockData(filePath);

//        System.out.println(trainData.size());

        if (trainData.isEmpty()) {
            System.out.println("No data loaded from train data.");
            return;
        }

        List<GPFunction> functions = new ArrayList<>();
        functions.add(new AddFunction());
        functions.add(new SubtractFunction());
        functions.add(new MultiplyFunction());
        functions.add(new DivideFunction());
        GP gp = new GP(functions, 100, 5, 100, trainData);

        long startTime = System.currentTimeMillis();

        gp.run();

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;



        Tree bestTree = gp.getBestTree();

        String filePath2 = "./BTC_test.csv";
        List<StockData> testData = readStockData(filePath2);

        if (testData.isEmpty()) {
            System.out.println("No data loaded from test data.");
            return;
        }

        int correct = 0;

        for (StockData data: testData) {
            float predicted = bestTree.evaluate(data);
            float actual = data.getAdjclose();

            if (Math.abs(predicted - actual) <= 0.5) {
                correct++;
            }
        }

        float accuracy = correct / testData.size();

        System.out.println("Best fitness: " + bestTree.getFitness());
        System.out.println("Accuracy (within ±0.5 of adj close): " + String.format("%.2f", accuracy));
    }

    private static List<StockData> readStockData(String filePath) {
        List<StockData> dataList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header line

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 6) continue;

                float open = Float.parseFloat(parts[0]);
                float high = Float.parseFloat(parts[1]);
                float low = Float.parseFloat(parts[2]);
                float close = Float.parseFloat(parts[3]);
                float volume = Float.parseFloat(parts[4]);
                float output = Float.parseFloat(parts[5]);

                dataList.add(new StockData(open, high, low, close, volume, output));
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return dataList;
    }
}
