import java.util.*;

public class DecisionTree {

    private Node rootNode;
    private final String targetAttribute;
    private String [] trainAttributes;
    private Map<String, ArrayList<Integer>> possibleAttributeValues;

    public DecisionTree(String targetAttribute, String [] trainAttributes){
        this.rootNode = new Node();
        this.targetAttribute = targetAttribute;
        this.trainAttributes = trainAttributes;
    }

    public void train(Set<Stock> data){
        possibleAttributeValues = getPossibleAttributeValues(data);
        long timeStart = System.currentTimeMillis();
        train(this.rootNode, data, this.trainAttributes);
        long duration = System.currentTimeMillis() - timeStart;
        System.out.println("Duration of training: " + duration + " ms");
    }

    public void train(Node node, Set<Stock> stocks, String[] attributes){
        if(allPositive(stocks, this.targetAttribute)){
            node.setLabel("1");
            node.setLeaf(true);
            return;
        }
        else if(allNegative(stocks, this.targetAttribute)){
            node.setLabel("0");
            node.setLeaf(true);
            return;
        }
        else if(attributes.length == 0){
            node.setLabel(String.valueOf(mcv(stocks, this.targetAttribute)));
            node.setLeaf(true);
            return;
        }
        else{
            String bestAttribute = getBestSplitAttribute(stocks, attributes);
            node.setLabel(bestAttribute);
            ArrayList<Integer> possibleValues = possibleAttributeValues.get(bestAttribute);
            for(int possibleVal : possibleValues){
                Condition condition = new Condition(possibleVal);
                Node child = new Node();
                condition.setSuccessor(child);
                node.addCondition(condition);
                Set<Stock> subsetPV = createSubset(stocks, bestAttribute, possibleVal);
                if(subsetPV.isEmpty()){
                    child.setLabel(String.valueOf(mcv(stocks, this.targetAttribute)));
                    child.setLeaf(true);
                }
                else{
                    String [] remainingAttributes = removeAttribute(bestAttribute, attributes);
                    train(child, subsetPV, remainingAttributes);
                }
            }
        }
    }


    private boolean allPositive(Set<Stock> data, String attribute){
        Object [] dataArr = data.toArray();
        Stock stock;
        for (Object object : dataArr) {
            stock = (Stock) object;
            if (stock.getAttributeValue(attribute) == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean allNegative(Set<Stock> data, String attribute){
        Object [] dataArr = data.toArray();
        Stock stock;
        for (Object object : dataArr) {
            stock = (Stock) object;
            if (stock.getAttributeValue(attribute) == 1) {
                return false;
            }
        }
        return true;
    }

    private int mcv(Set<Stock> data, String attribute){
        Object[] dataArr = data.toArray();
        Map<Integer, Integer> valueCount = new HashMap<>();
        Stock stock;
        for (Object object : dataArr) {
            stock = (Stock) object;
            valueCount.merge(stock.getAttributeValue(attribute), 1, Integer::sum);
        }
        return Collections.max(valueCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private String getBestSplitAttribute(Set<Stock> data, String [] attributes){
        double [] gainResults = new double[attributes.length];

        for(int i = 0; i < attributes.length; i++){
            gainResults[i] = calcInformationGain(data, attributes[i]);
        }

        return attributes[getMaxPosition(gainResults)];
    }

    private int getMaxPosition(double[] arr){
        int index = 0;
        double max = Double.MIN_VALUE;
        for(int i = 0; i < arr.length; i++){
            if(arr[i] > max){
                max = arr[i];
                index = i;
            }
        }
        return index;
    }

    private double calculateEntropy(Set<Stock> data, String attribute){
        double entropy = 0.0;
        Set<Stock> subset;
        double stock;
        for(int possibleValue : possibleAttributeValues.get(attribute)){
            subset = createSubset(data, attribute, possibleValue);
            if(!subset.isEmpty()){
                stock = (double) subset.size() / (double) data.size();
                entropy -= stock * log2(stock);
            }
        }
        return entropy;
    }

    private double calcInformationGain(Set<Stock> data, String attribute){
        double postEntropy = 0.0;
        for(int possibleValue : possibleAttributeValues.get(attribute)){
            Set<Stock> subset_v = createSubset(data, attribute, possibleValue);
            double weighting = (double) subset_v.size() / (double) data.size();
            double subsetEntropy = calculateEntropy(subset_v, this.targetAttribute);
            postEntropy += weighting * subsetEntropy;
        }
        double preEntropy = calculateEntropy(data, this.targetAttribute); // entropy before splitting on attribute attribute
        return preEntropy - postEntropy;
    }

    private double log2(double x){
        return Math.log(x)/Math.log(2.0);
    }

    private Set<Stock> createSubset(Set<Stock> prevSet, String attribute, int value){
        Set<Stock> subset = new HashSet<>();
        Object[] dataArr = prevSet.toArray();
        Stock stock;
        for(Object object : dataArr){
            stock = (Stock) object;
            if(stock.getAttributeValue(attribute) == value){
                subset.add(stock);
            }
        }
        return subset;
    }

    private String[] removeAttribute(String attribute, String [] attributes){
        try{
            String [] remainingAttributes = new String[attributes.length-1];
            int j = 0;
            for (String s : attributes) {
                if (s.equals(attribute)) {
                    continue;
                }
                remainingAttributes[j] = s;
                j++;
            }
            return remainingAttributes;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private HashMap<String, ArrayList<Integer>> getPossibleAttributeValues(Set<Stock> data){
        HashMap<String, ArrayList<Integer>> possAttValues = new HashMap<>();
        for(String attribute : trainAttributes){
            possAttValues.put(attribute, new ArrayList<>());
        }
        Object [] dataArr = data.toArray();
        Stock p;
        for(Object o : dataArr){
            p = (Stock) o;
            for(String attribute : trainAttributes){
                if(!possAttValues.get(attribute).contains(p.getAttributeValue(attribute))){
                    possAttValues.get(attribute).add(p.getAttributeValue(attribute));
                }
            }
        }
        ArrayList<Integer> outputValues = new ArrayList<>();
        outputValues.add(0);
        outputValues.add(1);
        possAttValues.put(Attribute.OUTPUT, outputValues);
        return possAttValues;
    }

    public void print(){
        Queue<Node> q = new LinkedList();
        Queue<Node> q2 = new LinkedList();
        q.add(rootNode);
        Node curr;
        Node br = new Node();
        br.setLabel(" | ");
        List<Condition> branches;
        while(!q.isEmpty() || !q2.isEmpty()){
            while(!q.isEmpty()){
                curr = q.poll();
                branches = curr.getConditions();
                System.out.print(" " + curr.getLabel());
                for(Condition branch : branches){
                    q2.add(branch.getSuccessor());
                    System.out.print(branch.getCompareValue());
                }
                if(branches.size() != 0) q2.add(br);
            }
            System.out.println();
            while(!q2.isEmpty()){
                q.add(q2.poll());
            }

        }
    }

    public String classify(Stock passenger){
        Node curr = rootNode;
        List<Condition> branches;
        while(!curr.isLeaf()) {
            branches = curr.getConditions();
            int compareValue = passenger.getAttributeValue(curr.getLabel());
            Node next = null;
            for (Condition branch : branches) {
                if (branch.check(compareValue)) {
                    next = branch.getSuccessor();
                    break;
                }
            }
            if(next == null){
                return "2";
            }
            else{
                curr = next;
            }
        }
        return curr.getLabel();
    }
}