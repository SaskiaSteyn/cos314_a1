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

    public GP(List<GPFunction> functions, int size, int maxDepth, int maxGenerations, List<StockData> data) {
        this.functions = functions;
        this.terminals = Arrays.asList(
                Node.TerminalType.OPEN,
                Node.TerminalType.HIGH,
                Node.TerminalType.LOW,
                Node.TerminalType.CLOSE,
                Node.TerminalType.ADJCLOSE,
                Node.TerminalType.CONST
        );
        this.rand = new Random();

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
        if (depth == 0 || (depth > 1 && rand.nextDouble() < 0.3)) {
            // Create a terminal node
            Node.TerminalType term = terminals.get(rand.nextInt(terminals.size()));
            if (term == Node.TerminalType.CONST) {
                float value = rand.nextFloat() * 10 - 5; // Random constant between -5 and 5
                return new Node(term, value);
            } else {
                return new Node(term);
            }
        } else {
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
        for (Tree tree: population) {
            tree.setFitness(computeFitness(tree));
        }
    }

    private float computeFitness(Tree tree) {
        float errorSum = 0f;

        for (StockData data: dataset) {
            float predicted = tree.evaluate(data);
            float actual = data.getClose();
            float error = Math.abs(predicted - actual);

            errorSum += error;
        }

        return errorSum / dataset.size();
    }

    public void run() {
        createPopulation();

        for (int i = 0; i < maxGenerations; i++) {
            evaluatePopulation();

            List<Tree> nextGen = new ArrayList<>();

            population.sort(Comparator.comparingDouble(Tree::getFitness));
            nextGen.add(population.getFirst());

            while(nextGen.size() < population.size()) {
                Tree parent1 = tournamentSelect(5);
                Tree parent2 = tournamentSelect(5);

                Tree child = crossover(parent1, parent2);

                mutate(child);

                nextGen.add(child.copy());
            }

            population = nextGen;
        }
    }

    private Tree tournamentSelect(int tournamentSize) {
        Tree best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Tree candidate = population.get(rand.nextInt(population.size()));
            if (best == null || candidate.getFitness() < best.getFitness()) {
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
            return parent2;
        }
        if (nodes2.isEmpty() && !nodes1.isEmpty()) {
            return parent1;
        }

        Node random1 = nodes1.get(rand.nextInt(nodes1.size()));
        Node random2 = nodes2.get(rand.nextInt(nodes2.size()));

        Node copy = new Node(random2);

        random1.setLeftChild(copy.getLeftChild());
        random1.setRightChild(copy.getRightChild());

        if (random2.getType() == Node.NodeType.TERMINAL) {
            if (random2.getTerminalType() == Node.TerminalType.CONST) {
                random1.setTerminal(copy.getTerminalType(), copy.getValue());
            }
            else {
                random1.setTerminal(copy.getTerminalType());
            }
        }
        else {
            random1.setFunction(copy.getFunction());
        }

        return copy1;
    }

    public Tree mutate(Tree original) {

        int chance = rand.nextInt(0, 100);

        Tree copy = original.copy();
        List<Node> nodes = copy.getAllNodes();
        if (nodes.isEmpty()) {
            return copy;
        }

        Node target = nodes.get(rand.nextInt(nodes.size()));

        Node mutatedSubtree = generateRandomTree().getRoot();

        target.setFunction(mutatedSubtree.getFunction());
        if (mutatedSubtree.getTerminalType() == Node.TerminalType.CONST) {
            target.setTerminal(mutatedSubtree.getTerminalType(), mutatedSubtree.getValue());
        }
        else {
            target.setTerminal(mutatedSubtree.getTerminalType());
        }

        target.setLeftChild(mutatedSubtree.getLeftChild());
        target.setRightChild(mutatedSubtree.getRightChild());

        return copy;
    }

    public Tree getBestTree() {
        return population.stream()
                .min(Comparator.comparing(Tree::getFitness))
                .orElse(null);
    }
}