import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class CondBlockedQueue<T> implements IBlockedQueue<T>{
    final int LENGTH = 10;
    Queue<T> queue = new ArrayDeque<>();
    int count = 0;
    int totalProduced = 0;
    int totalConsumed = 0;
    Controller c;
    final Lock lock = new ReentrantLock();
    final Condition canProduce = lock.newCondition();
    final Condition canConsume = lock.newCondition();

    public CondBlockedQueue(Controller c) {
        this.c = c;
    }

    public void enqueue(T item, Function<Integer, Void> onSuccess) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == LENGTH && ! c.isDone()) {
                canProduce.await(); //wait for consumer
            }
            queue.add(item);
            canConsume.signalAll(); //notify consumers
            totalProduced++;
            if (onSuccess != null) {
                onSuccess.apply(this.queue.size());
            }
        } finally {
            lock.unlock();
        }
    }

    public T dequeue(Function<Integer, Void> onSuccess) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == 0 && ! c.isDone()) {
                canConsume.await(); //wait for producer
            }
            if (queue.size() == 0 && c.isDone()) {
                return null;
            }
            T item = queue.remove();
            canProduce.signalAll(); //notify producers
            totalConsumed++;
            if (onSuccess != null) {
                onSuccess.apply(this.queue.size());
            }
            return item;
        } finally {
            lock.unlock();
        }
    }

    public int getTotalProduced() {
        return totalProduced;
    }

    public int getTotalConsumed() {
        return totalConsumed;
    }

}
