public class Controller {
    private boolean done = false;
    public boolean isDone() {
        return done;
    }
    public void shutdown() {
        done = true;
    }
}
