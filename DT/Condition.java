public class Condition {
    private Node successor;
    private int compare;

    public Condition(int value){
        this.compare = value;
    }

    public int getCompareValue(){
        return this.compare;
    }

    public boolean check(int value){
        return compare == value;
    }

    public void setSuccessor(Node successor){
        this.successor = successor;
    }

    public Node getSuccessor(){
        return this.successor;
    }
}