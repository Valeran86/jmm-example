//Ваша задача реализовать класс Task имеющий один метод get():

import java.util.concurrent.Callable;

public class Task<T> {

    private volatile boolean isCalling;
    private volatile boolean isCalled;
    private volatile T result;
    private final Callable<? extends T> task;
    private static final Object lock = new Object();
    public Task(Callable<? extends T> callable) {
        task = callable;
    }

    public T get() {
        if (isCalled) {
            System.out.println(Thread.currentThread().getName() + "получил сразу");
            return result;
        }
        else {
            if (isCalling) {
                try {
                    synchronized (lock) {
                        System.out.println(Thread.currentThread().getName() + "жду");
                        lock.wait();
                        System.out.println(Thread.currentThread().getName() + "получил потом");
                        return result;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                isCalling = true;
                try {
                    synchronized (lock) {
                        System.out.println(Thread.currentThread().getName() + "работаю");
                        result = task.call();
                        lock.notifyAll();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
            isCalled = true;
            System.out.println(Thread.currentThread().getName() + "получил потом");
            return result;
        }
    }
}
//Данный класс в конструкторе принимает экземпляр java.util.concurrent.Callable.
//Callable похож на Runnuble, но результатом его работы является объект (а не void).
//Ваша задача реализовать метод get() который возвращает результат работы Callable.
// Выполнение callable должен начинать тот поток, который первый вызвал метод get().
// Если несколько потоков одновременно вызывают этот метод, то выполнение должно начаться только в одном потоке,
// а остальные должны ожидать конца выполнения (не нагружая процессор).
// Если при вызове get() результат уже просчитан, то он должен вернуться сразу, (даже без задержек на вход в синхронизированную область).
// Если при просчете результата произошел Exception, то всем потокам при вызове get(), надо кидать этот Exception,
// обернутый в ваш RuntimeException (подходящее название своему ексепшену придумайте сами).