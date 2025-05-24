import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String filePath = "./BTC_train.csv";
        String filePath2 = "./BTC_test.csv";
        String outputCsvPath = "./GP.csv";

        List<StockData> trainData = readStockData(filePath);
        List<StockData> testData = readStockData(filePath2);

        if (trainData.isEmpty() || testData.isEmpty()) {
            System.out.println("No data loaded from train or test files.");
            return;
        }

        List<GPFunction> functions = new ArrayList<>();
        functions.add(new AddFunction());
        functions.add(new SubtractFunction());
        functions.add(new MultiplyFunction());
        functions.add(new DivideFunction());

        try (FileWriter csvWriter = new FileWriter(outputCsvPath)) {
            // Write CSV header
            csvWriter.append("Seed Value,Duration (ms),Training Accuracy (%),Training F1,Test Accuracy (%),Test F1,Test Correct/Total\n");

            // Run GP 10 times
            for (int run = 1; run <= 10; run++) {
                long seed = System.currentTimeMillis();
                Random random = new Random(seed);
                GP gp = new GP(functions, 100, 5, 100, trainData, random);

                long startTime = System.currentTimeMillis();
                gp.run();
                long elapsedTime = System.currentTimeMillis() - startTime;

                Tree bestTree = gp.getBestTree();
                float trainingAccuracy = bestTree.getFitness() * 100;

                float trainF1 = computeF1Score(bestTree, trainData);

                int correct = 0;
                for (StockData data : testData) {
                    if (bestTree.evaluate(data) == data.getOutput()) {
                        correct++;
                    }
                }
                float testAccuracy = (float) correct / testData.size() * 100;

                float testF1 = computeF1Score(bestTree, testData);

                // Write results to CSV
                csvWriter.append(String.format("%d,%.2f,%.2f,%.2f,%.2f,%.2f,%s\n",
                        seed,
                        elapsedTime / 1000.0,  // Convert to seconds with 2 decimals
                        trainingAccuracy,
                        trainF1,
                        testAccuracy,
                        testF1,
                        String.format("%d/%d", correct, testData.size())));

                System.out.printf("Run %d completed - Test Accuracy: %.2f%%\n", run, testAccuracy);
            }

            System.out.println("\nAll 10 runs completed. Results saved to: " + outputCsvPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static float computeF1Score(Tree tree, List<StockData> testData) {

        // True Positive
        int TP = 0;

        // False Positive
        int FP = 0;

        // False Negative
        int FN = 0;

        for (StockData data : testData) {
            int predicted = tree.evaluate(data);
            int actual = data.getOutput();

            if (predicted == 1 && actual == 1) {
                TP++;
            }
            else if (predicted == 1 && actual == 0) {
                FP++;
            }
            else if (predicted == 0 && actual == 1) {
                FN++;
            }
        }

        float precision = (TP + FP == 0) ? 0 : (float) TP / (TP + FP);
        float recall = (TP + FN == 0) ? 0 : (float) TP / (TP + FN);
        float f1 = (precision + recall == 0) ? 0 : 2 * (precision * recall) / (precision + recall);

        return f1;
    }

    private static List<StockData> readStockData(String filePath) {
        List<StockData> dataList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();

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