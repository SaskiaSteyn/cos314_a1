public class AddFunction extends GPFunction{
    public AddFunction() {
        super("+", 2);
    }

    @Override
    public float apply(float[] args) {
        return args[0] + args[1];
    }
}
