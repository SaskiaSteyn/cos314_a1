import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class StockMLP {
    private static class Layer {
        double[][] weights;
        double[][] biases;
        
        public Layer(int inputSize, int outputSize, Random random, boolean isOutputLayer) {
            weights = new double[inputSize][outputSize];
            biases = new double[1][outputSize];
            
            // He initialization for hidden layers, Xavier for output
            double factor = isOutputLayer ? 1.0 / Math.sqrt(inputSize) : Math.sqrt(2.0 / inputSize);
            
            for (int i = 0; i < inputSize; i++) {
                for (int j = 0; j < outputSize; j++) {
                    weights[i][j] = random.nextGaussian() * factor;
                }
            }
        }
    }

    private List<Layer> layers;
    private List<double[][]> activations;
    private List<double[][]> layerOutputs;
    private Random random;
    
    public StockMLP(int inputSize, int[] hiddenLayers, int seed) {
        this.random = new Random(seed);
        this.layers = new ArrayList<>();
        
        // Create hidden layers
        int prevSize = inputSize;
        for (int size : hiddenLayers) {
            layers.add(new Layer(prevSize, size, random, false));
            prevSize = size;
        }
        
        // Add output layer
        layers.add(new Layer(prevSize, 1, random, true));
    }
    
    private double[][] relu(double[][] x) {
        double[][] result = new double[x.length][x[0].length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                result[i][j] = Math.max(0, x[i][j]);
            }
        }
        return result;
    }
    
    private double[][] reluDerivative(double[][] x) {
        double[][] result = new double[x.length][x[0].length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                result[i][j] = x[i][j] > 0 ? 1.0 : 0.0;
            }
        }
        return result;
    }
    
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    private double[][] sigmoid(double[][] x) {
        double[][] result = new double[x.length][x[0].length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                result[i][j] = sigmoid(x[i][j]);
            }
        }
        return result;
    }
    
    private double[][] dot(double[][] a, double[][] b) {
        double[][] result = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                for (int k = 0; k < a[0].length; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }
    
    private double[][] add(double[][] a, double[][] b) {
        double[][] result = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                result[i][j] = a[i][j] + b[0][j]; // Broadcasting bias
            }
        }
        return result;
    }
    
    private double[][] transpose(double[][] a) {
        double[][] result = new double[a[0].length][a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                result[j][i] = a[i][j];
            }
        }
        return result;
    }
    
    public double[][] forward(double[][] X) {
        activations = new ArrayList<>();
        layerOutputs = new ArrayList<>();
        
        activations.add(X);
        
        // Hidden layers
        for (int i = 0; i < layers.size() - 1; i++) {
            Layer layer = layers.get(i);
            double[][] z = add(dot(activations.get(i), layer.weights), layer.biases);
            layerOutputs.add(z);
            double[][] a = relu(z);
            activations.add(a);
        }
        
        // Output layer
        Layer outputLayer = layers.get(layers.size() - 1);
        double[][] z = add(dot(activations.get(activations.size() - 1), outputLayer.weights), outputLayer.biases);
        double[][] a = sigmoid(z);
        activations.add(a);
        
        return a;
    }
    
    public void backward(double[][] X, double[][] y, double learningRate) {
        int m = X.length;
        List<double[][]> gradients = new ArrayList<>();
        
        // Output layer gradient
        double[][] error = subtract(activations.get(activations.size() - 1), y);
        double[][] outputActivation = activations.get(activations.size() - 1);
        double[][] sigmoidDerivative = multiply(outputActivation, subtract(1.0, outputActivation));
        double[][] delta = multiply(error, sigmoidDerivative);
        gradients.add(0, delta);
        
        // Hidden layers gradients
        for (int i = layers.size() - 2; i >= 0; i--) {
            error = dot(gradients.get(0), transpose(layers.get(i + 1).weights));
            delta = multiply(error, reluDerivative(layerOutputs.get(i)));
            gradients.add(0, delta);
        }
        
        // Update weights and biases
        for (int i = 0; i < layers.size(); i++) {
            double[][] dw = dot(transpose(activations.get(i)), gradients.get(i));
            dw = multiply(dw, 1.0 / m);
            
            double[][] db = sum(gradients.get(i), 0);
            db = multiply(db, 1.0 / m);
            
            layers.get(i).weights = subtract(layers.get(i).weights, multiply(dw, learningRate));
            layers.get(i).biases = subtract(layers.get(i).biases, multiply(db, learningRate));
        }
    }
    
    // Helper matrix operations
    private double[][] subtract(double[][] a, double[][] b) {
        double[][] result = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        return result;
    }

    private double[][] subtract(double a, double[][] b) {
    double[][] result = new double[b.length][b[0].length];
    for (int i = 0; i < b.length; i++) {
        for (int j = 0; j < b[i].length; j++) {
            result[i][j] = a - b[i][j];
        }
    }
    return result;
}
    
    private double[][] multiply(double[][] a, double[][] b) {
        double[][] result = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                result[i][j] = a[i][j] * b[i][j];
            }
        }
        return result;
    }
    
    private double[][] multiply(double[][] a, double b) {
        double[][] result = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                result[i][j] = a[i][j] * b;
            }
        }
        return result;
    }
    
    private double[][] sum(double[][] a, int axis) {
        if (axis == 0) { // Sum along columns (keep rows)
            double[][] result = new double[1][a[0].length];
            for (int j = 0; j < a[0].length; j++) {
                for (int i = 0; i < a.length; i++) {
                    result[0][j] += a[i][j];
                }
            }
            return result;
        } else { // Sum along rows (keep columns)
            double[][] result = new double[a.length][1];
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a[i].length; j++) {
                    result[i][0] += a[i][j];
                }
            }
            return result;
        }
    }
    
    public void train(double[][] X_train, double[][] y_train, double[][] X_val, double[][] y_val,
                     int epochs, double learningRate, int batchSize, boolean verbose) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < X_train.length; i++) indices.add(i);
        
        for (int epoch = 0; epoch < epochs; epoch++) {
            Collections.shuffle(indices, random);
            
            for (int i = 0; i < X_train.length; i += batchSize) {
                int end = Math.min(i + batchSize, X_train.length);
                double[][] X_batch = new double[end - i][];
                double[][] y_batch = new double[end - i][];
                
                for (int j = i; j < end; j++) {
                    X_batch[j - i] = X_train[indices.get(j)];
                    y_batch[j - i] = y_train[indices.get(j)];
                }
                
                forward(X_batch);
                backward(X_batch, y_batch, learningRate);
            }
            
            if (verbose && (epoch % 100 == 0 || epoch == epochs - 1)) {
                int[] trainPred = predict(X_train);
                double trainAcc = accuracyScore(y_train, trainPred);
                String msg = String.format("Epoch %4d, Train Acc: %.4f", epoch, trainAcc);
                
                if (X_val != null) {
                    int[] valPred = predict(X_val);
                    double valAcc = accuracyScore(y_val, valPred);
                    msg += String.format(", Val Acc: %.4f", valAcc);
                }
                
                System.out.println(msg);
            }
        }
    }
    
    public int[] predict(double[][] X) {
        return predict(X, 0.5);
    }
    
    public int[] predict(double[][] X, double threshold) {
        double[][] output = forward(X);
        int[] predictions = new int[output.length];
        for (int i = 0; i < output.length; i++) {
            predictions[i] = output[i][0] > threshold ? 1 : 0;
        }
        return predictions;
    }
    
    public void evaluate(double[][] X, double[][] y) {
        int[] y_pred = predict(X);
        
        System.out.println("\nClassification Report:");
        classificationReport(y, y_pred);
        
        System.out.println("\nConfusion Matrix:");
        printConfusionMatrix(y, y_pred);
        
        double accuracy = accuracyScore(y, y_pred);
        System.out.printf("\nAccuracy: %.4f\n", accuracy);
    }
    
    // Evaluation metrics
    private double accuracyScore(double[][] y_true, int[] y_pred) {
        int correct = 0;
        for (int i = 0; i < y_true.length; i++) {
            if ((y_true[i][0] >= 0.5 && y_pred[i] == 1) || (y_true[i][0] < 0.5 && y_pred[i] == 0)) {
                correct++;
            }
        }
        return (double) correct / y_true.length;
    }
    
    private void classificationReport(double[][] y_true, int[] y_pred) {
        int tp = 0, fp = 0, fn = 0, tn = 0;
        
        for (int i = 0; i < y_true.length; i++) {
            int actual = y_true[i][0] >= 0.5 ? 1 : 0;
            int predicted = y_pred[i];
            
            if (actual == 1 && predicted == 1) tp++;
            else if (actual == 0 && predicted == 1) fp++;
            else if (actual == 1 && predicted == 0) fn++;
            else tn++;
        }
        
        double precision = (double) tp / (tp + fp);
        double recall = (double) tp / (tp + fn);
        double f1 = 2 * (precision * recall) / (precision + recall);
        
        System.out.printf("Precision: %.4f\n", precision);
        System.out.printf("Recall:    %.4f\n", recall);
        System.out.printf("F1 Score:  %.4f\n", f1);
    }
    
    private void printConfusionMatrix(double[][] y_true, int[] y_pred) {
        int[][] matrix = new int[2][2];
        
        for (int i = 0; i < y_true.length; i++) {
            int actual = y_true[i][0] >= 0.5 ? 1 : 0;
            matrix[actual][y_pred[i]]++;
        }
        
        System.out.println("Actual\\Predicted  0     1");
        System.out.printf("0               %4d  %4d\n", matrix[0][0], matrix[0][1]);
        System.out.printf("1               %4d  %4d\n", matrix[1][0], matrix[1][1]);
    }
    
    public static void main(String[] args) {
        System.out.println("Stock Purchase Classifier using MLP");
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.print("Enter random seed for reproducibility: ");
            int seed = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            System.out.print("Enter path to training data CSV: ");
            String trainPath = scanner.nextLine();
            
            System.out.print("Enter path to test data CSV: ");
            String testPath = scanner.nextLine();
            
            // Load and preprocess data
            double[][] X_train = null, y_train = null;
            double[] trainMean = null, trainStd = null;
            
            // Read training data
            try (BufferedReader br = new BufferedReader(new FileReader(trainPath))) {
                List<double[]> xList = new ArrayList<>();
                List<double[]> yList = new ArrayList<>();
                String line;
                
                br.readLine(); // Skip header
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    double[] x = new double[5]; // Open, High, Low, Close, Adj Close
                    for (int i = 0; i < 5; i++) {
                        x[i] = Double.parseDouble(values[i]);
                    }
                    double y = Double.parseDouble(values[5]); // Output
                    
                    xList.add(x);
                    yList.add(new double[]{y});
                }
                
                X_train = xList.toArray(new double[0][]);
                y_train = yList.toArray(new double[0][]);
                
                // Calculate mean and std
                trainMean = new double[5];
                trainStd = new double[5];
                
                for (int j = 0; j < 5; j++) {
                    double sum = 0;
                    for (double[] row : X_train) {
                        sum += row[j];
                    }
                    trainMean[j] = sum / X_train.length;
                    
                    double variance = 0;
                    for (double[] row : X_train) {
                        variance += Math.pow(row[j] - trainMean[j], 2);
                    }
                    trainStd[j] = Math.sqrt(variance / X_train.length);
                }
                
                // Normalize training data
                for (double[] row : X_train) {
                    for (int j = 0; j < 5; j++) {
                        row[j] = (row[j] - trainMean[j]) / trainStd[j];
                    }
                }
            }
            
            // Read and normalize test data
            double[][] X_test = null, y_test = null;
            try (BufferedReader br = new BufferedReader(new FileReader(testPath))) {
                List<double[]> xList = new ArrayList<>();
                List<double[]> yList = new ArrayList<>();
                String line;
                
                br.readLine(); // Skip header
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    double[] x = new double[5];
                    for (int i = 0; i < 5; i++) {
                        x[i] = Double.parseDouble(values[i]);
                    }
                    double y = Double.parseDouble(values[5]);
                    
                    // Normalize using training stats
                    for (int j = 0; j < 5; j++) {
                        x[j] = (x[j] - trainMean[j]) / trainStd[j];
                    }
                    
                    xList.add(x);
                    yList.add(new double[]{y});
                }
                
                X_test = xList.toArray(new double[0][]);
                y_test = yList.toArray(new double[0][]);
            }
            
            // Initialize MLP
            StockMLP mlp = new StockMLP(
                X_train[0].length,
                new int[]{64, 32}, // Two hidden layers with 64 and 32 neurons
                seed
            );
            
            // Train the model
            System.out.println("\nTraining MLP model...");
            mlp.train(
                X_train, y_train,
                X_test, y_test,
                1000, // epochs
                0.001, // learning rate
                64, // batch size
                true // verbose
            );
            
            // Evaluate on test set
            System.out.println("\nFinal Test Set Evaluation:");
            mlp.evaluate(X_test, y_test);
            
        } catch (Exception e) {
            System.out.println("\nError: " + e.getMessage());
            System.out.println("Please check your input files and try again.");
        } finally {
            scanner.close();
        }
    }
}