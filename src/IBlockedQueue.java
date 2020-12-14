import java.util.function.Function;

public interface IBlockedQueue <T> {
    void enqueue(T item, Function<Integer, Void> onSuccess) throws InterruptedException;

    T dequeue(Function<Integer, Void> onSuccess) throws InterruptedException;

    int getTotalProduced();

    int getTotalConsumed();
}
