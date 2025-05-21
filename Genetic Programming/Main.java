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
            csvWriter.append("Run,Training Duration (ms),Training Accuracy (%),Test Accuracy (%),Seed,Correct/Total\n");

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

                int correct = 0;
                for (StockData data : testData) {
                    if (bestTree.evaluate(data) == data.getOutput()) {
                        correct++;
                    }
                }
                float testAccuracy = (float) correct / testData.size() * 100;

                // Write results to CSV
                csvWriter.append(String.format("%d,%d,%.2f,%.2f,%d,%d/%d\n",
                        run,
                        elapsedTime,
                        trainingAccuracy,
                        testAccuracy,
                        seed,
                        correct,
                        testData.size()));

                System.out.printf("Run %d completed - Test Accuracy: %.2f%%\n", run, testAccuracy);
            }

            System.out.println("\nAll 10 runs completed. Results saved to: " + outputCsvPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
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