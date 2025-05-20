import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class Validate {

    private Set<Stock> [] splitData;
    private final int NUMBER_OF_SPLITS;
    private String[] trainAttributes;
    private String targetAttribute;

    public Validate(Set<Stock> data, int numberOfSplits, String [] trainAttributes, String targetAttribute){
        this.splitData = splitData(data, numberOfSplits);
        this.NUMBER_OF_SPLITS = numberOfSplits;
        this.trainAttributes = trainAttributes;
        this.targetAttribute = targetAttribute;
    }
    public void evaluateTestData(DecisionTree decisionTree, Set<Stock> testData, Set<Stock> testDataWithResults){
        int correct = 0;
        try {
            PrintWriter writer = new PrintWriter("result.csv");
            writer.write("StockID,Output\n");
            for(Stock p : testData){
                String line = String.valueOf(p.getId()) + ",";
                Integer value = Integer.parseInt(decisionTree.classify(p));
                Stock sWithResult = testDataWithResults.stream()
                        .filter(stock -> stock.getId() == p.getId())
                        .findFirst()
                        .orElse(null);
                if(value == sWithResult.getAttributeValue(Attribute.OUTPUT)){
                    correct++;
                }
                line += decisionTree.classify(p);
                line += "\n";
                writer.write(line);
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Correctly classified: " + correct + "/" + testData.size());
        System.out.println("Evaluation: " + String.format("%.2f", ((double) correct / testData.size()) * 100) + "%");
    }

    private Set<Stock>[] splitData(Set<Stock> data, int numSplits){
        Set<Stock> [] splitSets = new Set[numSplits];
        for(int i = 0; i < splitSets.length ; i++){
            splitSets[i] = new HashSet<>();
        }
        Object [] data_arr = data.toArray();
        Stock stock;
        int i = 0;
        for(Object object : data_arr){
            stock = (Stock) object;
            splitSets[i].add(stock);
            i++;
            if(i >= numSplits){
                i = 0;
            }
        }
        return splitSets;
    }
}