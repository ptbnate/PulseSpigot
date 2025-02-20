package xyz.krypton.spigot.util;

public class Pair<L, R> {

    private final L left;
    private final R right;

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return this.left;
    }

    public L getKey() {
        return this.left;
    }

    public R getRight() {
        return this.right;
    }

    public R getValue() {
        return this.right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

}
