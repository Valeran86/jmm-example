package ru.sbt.jmm;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionManagerImplAtomic implements ExecutionManager {
    private AtomicInteger completedTaskCount = new AtomicInteger(0);
    private AtomicInteger failedTaskCount = new AtomicInteger(0);
    private AtomicInteger interruptedTaskCount= new AtomicInteger(0);
    private AtomicBoolean isInterrupt = new AtomicBoolean(false);
    private final int countThread;

    ExecutionManagerImplAtomic(int countThread){
        this.countThread=countThread;
    }

    @Override
    public Context execute(Runnable callback, Runnable... tasks) {
        ExecutorService executorService = Executors.newFixedThreadPool(countThread);
        int countTasks=tasks.length;

        for(Runnable task:tasks){
            Runnable runnable=()-> {
                if (isInterrupt.get()) {
                    interruptedTaskCount.getAndIncrement();
                }else {
                    try {
                        task.run();
                        completedTaskCount.getAndIncrement();
                    } catch (Exception e) {
                        failedTaskCount.getAndIncrement();
                    }
                }
                if(countTasks==(interruptedTaskCount.get()+completedTaskCount.get()+failedTaskCount.get())){
                    executorService.shutdown();
                    callback.run();
                }
            };
            executorService.submit(runnable);
        }
//Возвращаемый объект Context.
        return new Context(){

            /**
             * @return возвращает количество тасков, которые на текущий момент успешно выполнились
             */
            @Override
            public int getCompletedTaskCount() {
                return completedTaskCount.get();
            }

            /**
             * @return возвращает количество тасков, при выполнении которых произошел Exception
             */
            @Override
            public int getFailedTaskCount() {
                return failedTaskCount.get();
            }

            /**
             * @return возвращает количество тасков, которые не были выполены из-за отмены
             */
            @Override
            public int getInterruptedTaskCount() {
                return interruptedTaskCount.get();
            }

            /**
             * отменяет выполнения тасков, которые еще не начали выполняться
             */
            @Override
            public void interrupt() {
                isInterrupt.set(true);
            }

            /**
             * @return вернет true, если все таски были выполнены или отменены, false в противном случае
             */
            @Override
            public boolean isFinished() {
                return executorService.isShutdown();
            }
        };
    }
}
