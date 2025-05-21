import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

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

        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        GP gp = new GP(functions, 100, 5, 100, trainData, random);


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

//        System.out.println(bestTree.getFitness() * 100);

        int correct = 0;

        for (int i = 0; i < testData.size(); i++) {
            StockData data = testData.get(i);
            int predicted = bestTree.evaluate(data);
            int actual = data.getOutput();

            if (predicted == actual) {
                correct++;
            }

//            System.out.println("Test " + i + ": ");
//            System.out.println("Predicted: " + predicted);
//            System.out.println("Actual: " + actual);
        }

        float accuracy = correct / testData.size() * 100;

        System.out.println("Duration of training: " + elapsedTime + "ms");
        System.out.println("Accuracy against training data: " + String.format("%.2f", bestTree.getFitness() * 100) + "%");
        System.out.println("Seed value: " + seed);
        System.out.println("Correctly Classified: " + correct + "/" + testData.size());
        System.out.println("Accuracy against test data: " + String.format("%.2f", accuracy) + "%");
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
                int output = Integer.parseInt(parts[5]);

                dataList.add(new StockData(open, high, low, close, volume, output));
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return dataList;
    }
}
