import java.util.ArrayList;
import java.util.List;

public class Tree {
    private Node root;
    private float fitness;

    public Tree(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public List<Node> getAllNodes() {
        List<Node> nodeList = new ArrayList<>();
        traverse(root, nodeList);
        return nodeList;
    }

    private void traverse(Node current, List<Node> nodeList) {
        if (current == null) return;
        nodeList.add(current);
        traverse(current.getLeftChild(), nodeList);
        traverse(current.getRightChild(), nodeList);
    }


    public float evaluate(StockData data) {
        return root.evaluate(data);
    }

    public Tree copy() {
        Node rootCopy = copyNode(root);
        return new Tree(rootCopy);
    }

    public float getFitness() {
        return fitness;
    }

    public void setFitness(float f) {
        fitness = f;
    }

    private Node copyNode(Node original) {
        return new Node(original);
    }
}