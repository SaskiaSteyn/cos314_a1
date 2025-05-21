public class DivideFunction extends GPFunction {
    public DivideFunction() {
        super("/", 2);
    }

    @Override
    public float apply(float[] args) {
        if (args[1] == 0) {
            return 1;
        }
        return args[0] / args[1];
    }
}