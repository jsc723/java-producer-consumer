import java.util.ArrayList;
import java.util.List;

public class Test {
    static Controller controller = new Controller();
    static final int PRODUCE_TIME = 10;
    static final int CONSUME_TIME = 20;
    static final int PRODUCER_COUNT = 8;
    static final int CONSUMER_COUNT = 2;
    public static void main(String[] args) throws InterruptedException {
        IBlockedQueue<Product> queue = new SimpleBlockedQueue<>(controller);
        List<Producer> producers = new ArrayList<>();
        List<Consumer> consumers = new ArrayList<>();
        for (int i = 0; i < PRODUCER_COUNT; i++) {
            Producer p = new Producer(i + 1, queue);
            p.start();
            producers.add(p);
        }
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            Consumer c = new Consumer(i + 1, queue);
            c.start();
            consumers.add(c);
        }
        for (Producer p: producers) {
            p.join();
        }
        System.out.println("--All products are produced--");
        controller.shutdown();
        for (Consumer c: consumers) {
            c.join();
        }
        System.out.println("--All products are consumed--");
        System.out.printf("Total produced: %d\n", queue.getTotalProduced());
        System.out.printf("Total consumed: %d\n", queue.getTotalConsumed());
    }
    public static class Producer extends Thread {
        public int id;
        public IBlockedQueue<Product> queue;

        public Producer(int id, IBlockedQueue<Product> queue) {
            super("Producer-" + id);
            this.id = id;
            this.queue = queue;
        }

        @Override
        public void run() {
            try{
                for (int i = 0; i < 100; i++) {
                    sleep(PRODUCE_TIME);
                    queue.enqueue(new Product(id * 1000 + i), size -> {
                        System.out.printf("Queue now has %d items\n", size);
                        return null;
                    });
                    System.out.printf("Producer %d produced item %d\n", id, id * 1000 + i);
                }
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }
    public static class Consumer extends Thread {
        public int id;
        public IBlockedQueue<Product> queue;
        private boolean isDone = false;

        public Consumer(int id, IBlockedQueue<Product> queue) {
            super("Consumer-" + id);
            this.id = id;
            this.queue = queue;
        }

        @Override
        public void run() {
            try{
                while(true) {
                    sleep(CONSUME_TIME);
                    Product p = queue.dequeue(size -> {
                        System.out.printf("Queue now has %d items\n", size);
                        return null;
                    });
                    if (p == null) { // no more products
                        break;
                    }
                    System.out.printf("Consumer %d consumed item %d\n", id, p.id);
                }
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }
    public static class Product {
        public Product(int id) {
            this.id = id;
        }
        public int id;
    }
}
