public class MultiplyFunction extends GPFunction {
    public MultiplyFunction() {
        super("*", 2);
    }

    @Override
    public float apply(float[] args) {
        return args[0] * args[1];
    }
}