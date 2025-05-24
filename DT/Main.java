import java.util.*;

public class Main {
    public static void main(String [] args){
        final String TRAIN_DATA_PATH = "BTC_train.csv";
        Set<Stock> data = Read.readTrainData(TRAIN_DATA_PATH);
        String [] trainAttributes = {
                Attribute.OPEN,
                Attribute.HIGH,
                Attribute.LOW,
                Attribute.CLOSE,
                Attribute.ADJ_CLOSE,
        };
        String targetAttribute = Attribute.OUTPUT;

        DecisionTree decisionTree = new DecisionTree(targetAttribute, trainAttributes);
        decisionTree.train(data);

        final int NUMBER_OF_SPLITS = 10;

        Validate validator = new Validate(data, NUMBER_OF_SPLITS, trainAttributes, targetAttribute);

        final String TEST_DATA_PATH = "BTC_test.csv";

        Set<Stock> testDataWithResults = Read.readTrainData(TEST_DATA_PATH);
        Set<Stock> testData = Read.readTestData(TEST_DATA_PATH);
        validator.evaluateTestData(decisionTree, testData, testDataWithResults);
    }
}