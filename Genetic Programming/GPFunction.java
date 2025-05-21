public abstract class GPFunction {
    private final String name;
    private final int arity;

    public GPFunction(String name, int arity) {
        this.name = name;
        this.arity = arity;
    }

    public String getName() {
        return name;
    }

    public int getArity() {
        return arity;
    }

    public abstract float apply(float[] args);
}

