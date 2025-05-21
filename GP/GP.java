import java.util.*;

public class GP {
    private List<GPFunction> functions;
    private List<Node.TerminalType> terminals;
    private Random rand;

    private List<Tree> population;
    private List<StockData> dataset;


    private int size;
    private int maxDepth;
    private int maxGenerations;

    public GP(List<GPFunction> functions, int size, int maxDepth, int maxGenerations, List<StockData> data, Random random) {
        this.functions = functions;
        this.terminals = Arrays.asList(
                Node.TerminalType.OPEN,
                Node.TerminalType.HIGH,
                Node.TerminalType.LOW,
                Node.TerminalType.CLOSE,
                Node.TerminalType.ADJCLOSE,
                Node.TerminalType.CONST
        );
        this.rand = random;

        // Population size
        this.size = size;

        // Max Depth of Tree
        this.maxDepth = maxDepth;

        // Max Generations of Population
        this.maxGenerations = maxGenerations;

        this.dataset = data;

        this.population = new ArrayList<>();
    }

    public Tree generateRandomTree() {
        Node root = generateNode(maxDepth);
        return new Tree(root);
    }

    private Node generateNode(int depth) {
        if (depth <= 1) {
            // Create a terminal node
            Node.TerminalType term = terminals.get(rand.nextInt(terminals.size()));
            if (term == Node.TerminalType.CONST) {
                float value = rand.nextFloat() * 10 - 5; // Random constant between -5 and 5
                return new Node(term, value);
            } else {
                return new Node(term);
            }
        }
        else {
            // Create Function node

            GPFunction function = functions.get(rand.nextInt(functions.size()));
            Node node = new Node(function);

            Node left = generateNode(depth - 1);
            Node right = generateNode(depth - 1);
            node.setLeftChild(left);
            node.setRightChild(right);
            return node;
        }
    }

    public void createPopulation() {
        for (int i = 0; i < size; i++) {
            population.add(generateRandomTree());
        }
    }

    public void evaluatePopulation() {
//        int number = 0;

        for (Tree tree: population) {

            float fitness = computeFitness(tree);
//            System.out.println(fitness);
            tree.setFitness(fitness);
//            number++;
        }
    }

    private float computeFitness(Tree tree) {
        int correct = 0;

//        System.out.println("Dataset size = " + dataset.size());

        for (int i = 0; i < dataset.size(); i++) {
            StockData data = dataset.get(i);
//            System.out.println("Row " + i);
            int predicted = tree.evaluate(data);
            int actual = data.getOutput();

            if (predicted == actual) {
                correct++;
            }
        }

        return (float) correct / dataset.size();
    }

    public void run() {
        createPopulation();

        for (int i = 0; i < maxGenerations; i++) {
            evaluatePopulation();

            List<Tree> nextGen = new ArrayList<>();

            population.sort(Comparator.comparingDouble(Tree::getFitness).reversed());
            nextGen.add(population.getFirst());

//            System.out.println("Generation " + i + ": " + nextGen.getFirst().getFitness());

            while(nextGen.size() < population.size()) {
                Tree parent1 = tournamentSelect(5);
                Tree parent2 = tournamentSelect(5);

                Tree child1 = crossover(parent1, parent2);
                Tree child2 = crossover(parent2, parent1);

                mutate(child1);
                mutate(child2);

                nextGen.add(child1.copy());
                nextGen.add(child2.copy());
            }

            population = nextGen;
        }
    }

    private Tree tournamentSelect(int tournamentSize) {
        Tree best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Tree candidate = population.get(rand.nextInt(population.size()));
            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        return best.copy();
    }

    private Tree crossover(Tree parent1, Tree parent2) {

        Tree copy1 = parent1.copy();
        Tree copy2 = parent2.copy();

        List<Node> nodes1 = copy1.getAllNodes();
        List<Node> nodes2 = copy2.getAllNodes();

        if (nodes1.isEmpty() && !nodes2.isEmpty()) {
            return parent2.copy();
        }
        if (nodes2.isEmpty() && !nodes1.isEmpty()) {
            return parent1.copy();
        }

        Node random1 = nodes1.get(rand.nextInt(nodes1.size()));
        Node random2 = nodes2.get(rand.nextInt(nodes2.size()));

        Node copy = new Node(random2);


        if (random1 == nodes1.getFirst()) {
            return copy2.copy();
        }

        Node parent = findParent(nodes1.getFirst(), random1);

        if (parent.getLeftChild() == random1) {
            parent.setLeftChild(copy);
        } else if (parent.getRightChild() == random1) {
            parent.setRightChild(copy);
        }

        if (getTreeDepth(copy1.getRoot()) > maxDepth) {
            return parent1.getFitness() > parent2.getFitness() ? parent1 : parent2;
        }

        return copy1;
    }

    private Node findParent(Node root, Node target) {
        if (root == null) return null;

        // Found target
        if (root.getLeftChild() == target || root.getRightChild() == target) {
            return root;
        }

        // Iterate through tree
        Node leftResult = findParent(root.getLeftChild(), target);
        if (leftResult != null) return leftResult;

        return findParent(root.getRightChild(), target);
    }

    public static int getTreeDepth(Node node) {
        if (node == null) {
            return -1;
        }

        // Leaf Node
        if (node.getFunction() == null) {
            return 0;
        }

        // Recursive calls
        int leftDepth = getTreeDepth(node.getLeftChild());
        int rightDepth = getTreeDepth(node.getRightChild());

        return 1 + Math.max(leftDepth, rightDepth);
    }

    private int findNodeDepth(Node current, Node target, int depth) {
        if (current == null) return -1;
        if (current == target) return depth;

        int leftDepth = findNodeDepth(current.getLeftChild(), target, depth + 1);
        if (leftDepth != -1) return leftDepth;

        return findNodeDepth(current.getRightChild(), target, depth + 1);
    }

    public Tree mutate(Tree original) {

        Tree copy = original.copy();
        List<Node> nodes = copy.getAllNodes();
        if (nodes.isEmpty()) {
            return copy;
        }

        Node target = nodes.get(rand.nextInt(nodes.size()));

        if (target == copy.getRoot()) {
            return new Tree(generateNode(maxDepth));
        }

        int targetDepth = findNodeDepth(copy.getRoot(), target, 0);
        int remainingDepth = maxDepth - targetDepth;

        if (remainingDepth <= 1) {
            remainingDepth = 1;
        }

        Node newSubtree = generateNode(remainingDepth);

        Node parent = findParent(copy.getRoot(), target);

        if (parent.getLeftChild() == target) {
            parent.setLeftChild(newSubtree);
        } else {
            parent.setRightChild(newSubtree);
        }

        if (getTreeDepth(copy.getRoot()) > maxDepth) {
            return original;
        }

        return copy;
    }

    public Tree getBestTree() {
        return population.stream()
                .max(Comparator.comparing(Tree::getFitness))
                .orElse(null);
    }
}