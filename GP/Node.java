public class Node {

    public enum NodeType { FUNCTION, TERMINAL }

    public enum TerminalType {
        OPEN, HIGH, LOW, CLOSE, ADJCLOSE, CONST
    }

    public interface Function {
        float apply(float a, float b);
        String getName();
    }

    private NodeType type;
    private TerminalType terminalType;
    private float value;
    private GPFunction function;
    private int arity;
    private Node left;
    private Node right;

    // Terminal constructor (non-CONSTANT)
    public Node(TerminalType terminalType) {
        this.type = NodeType.TERMINAL;
        this.terminalType = terminalType;
    }

    // Terminal constructor (CONSTANT with value)
    public Node(TerminalType terminalType, float value) {
        this(terminalType);
        this.value = value;
    }

    public Node(GPFunction functionType) {
        this.type = NodeType.FUNCTION;
        this.function = functionType;
        this.arity = functionType.getArity();
    }

    // Copy constructor
    public Node(Node other) {
        this.type = other.type;

        if (type == NodeType.TERMINAL) {
            this.terminalType = other.terminalType;
            this.value = other.value;
        } else {
            this.function = other.function;
        }

        if (other.left != null)
            this.left = new Node(other.left);
        if (other.right != null)
            this.right = new Node(other.right);
    }

    public Node getLeftChild() {
        return left;
    }

    public void setLeftChild(Node leftChild) {
        this.left = leftChild;
    }

    public Node getRightChild() {
        return right;
    }

    public void setRightChild(Node rightChild) {
        this.right = rightChild;
    }

    public NodeType getType() {
        return type;
    }

    public TerminalType getTerminalType() {
        return terminalType;
    }

    public float getValue() {
        if (type == NodeType.TERMINAL) {
            if (terminalType == TerminalType.CONST) {
                return value;
            } else {
                throw new UnsupportedOperationException("getValue() called on a non CONST node");
            }
        } else {
            throw new UnsupportedOperationException("getValue() called on a function node");
        }
    }

    public GPFunction getFunction() {
        if (type == NodeType.FUNCTION) {
            return function;
        } else {
            return null;
        }
    }

    public void setFunction(GPFunction nfunction) {
        this.function = nfunction;
    }

    public void setTerminal(TerminalType type) {
        terminalType = type;
    }

    public void setTerminal(TerminalType type, float nValue) {
        terminalType = type;
        if (terminalType == TerminalType.CONST) {
            value = nValue;
        }
    }


    public float evaluate(StockData data) {
        if (type == NodeType.TERMINAL) {
            switch (terminalType) {
                case OPEN: return data.getOpen();
                case HIGH: return data.getHigh();
                case LOW: return data.getLow();
                case CLOSE: return data.getClose();
                case ADJCLOSE: return data.getAdjclose();
                case CONST: return value;
                default: throw new RuntimeException("Unknown terminal type");
            }
        } else {
            float[] inputs;
            if (function.getArity() == 2) {
                inputs = new float[] { left.evaluate(data), right.evaluate(data) };
            } else if (function.getArity() == 1) {
                inputs = new float[] { left.evaluate(data) };
            } else {
                inputs = new float[0];
            }
            return function.apply(inputs);
        }
    }

    @Override
    public String toString() {
        if (type == NodeType.TERMINAL) {
            if (terminalType == TerminalType.CONST) {
                return String.valueOf(value);
            } else {
                return terminalType.name();
            }
        } else {
            if (function.getArity() == 2) {
                return "(" + left.toString() + " " + function.getName() + " " + right.toString() + ")";
            } else if (function.getArity() == 1) {
                return function.getName() + "(" + left.toString() + ")";
            } else {
                return function.getName();
            }
        }
    }
}
