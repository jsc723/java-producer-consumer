import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class SimpleBlockedQueue<T> implements IBlockedQueue<T> {
    final int LENGTH = 10;
    Queue<T> queue = new ArrayDeque<>();
    int count = 0;
    int totalProduced = 0;
    int totalConsumed = 0;
    Controller c;

    public SimpleBlockedQueue(Controller c) {
        this.c = c;
    }

    public synchronized void enqueue(T item, Function<Integer, Void> onSuccess) throws InterruptedException {
        while (queue.size() == LENGTH && ! c.isDone()) {
            this.wait(); //wait for consumer
        }
        queue.add(item);
        this.notifyAll(); //notify consumers
        totalProduced++;
        if (onSuccess != null) {
            onSuccess.apply(this.queue.size());
        }
    }

    public synchronized T dequeue(Function<Integer, Void> onSuccess) throws InterruptedException {
        while (queue.size() == 0 && ! c.isDone()) {
            this.wait(); //wait for producer
        }
        if (queue.size() == 0 && c.isDone()) {
            return null;
        }
        T item = queue.remove();
        this.notifyAll(); //notify producers
        totalConsumed++;
        if (onSuccess != null) {
            onSuccess.apply(this.queue.size());
        }
        return item;
    }

    public int getTotalProduced() {
        return totalProduced;
    }

    public int getTotalConsumed() {
        return totalConsumed;
    }

}
