import java.util.concurrent.Callable;

public class Test {
    public static void main(String... args) {
        Thread[] threads = new Thread[5];
        Task<Integer> task = new Task<Integer>(Test::call);
        for (Thread thread : threads) {
            thread = new Thread(() -> task.get());
            thread.start();
        }
    }
    public static Integer call() {
        return 0;
    }
}