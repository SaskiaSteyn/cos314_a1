public class SubtractFunction extends GPFunction{
    public SubtractFunction() {
        super("-", 2);
    }

    @Override
    public float apply(float[] args) {
        return args[0] - args[1];
    }
}
