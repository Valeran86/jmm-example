package ru.sbt.jmm;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutionManagerImpl implements ExecutionManager {
    private volatile int completedTaskCount=0;
    private volatile int failedTaskCount=0;
    private volatile int interruptedTaskCount=0;
    private volatile boolean isInterrupt=false;
    private final int countThread;

    ExecutionManagerImpl(int countThread){
        this.countThread=countThread;
    }

    @Override
    public Context execute(Runnable callback, Runnable... tasks) {
        ExecutorService executorService = Executors.newFixedThreadPool(countThread);
        int countTasks=tasks.length;

        for(Runnable task:tasks){
            Runnable runnable=()-> {
                if (isInterrupt) {
                    interruptedTaskCount++;
                }else {
                    try {
                        task.run();
                        completedTaskCount++;
                    } catch (Exception e) {
                        failedTaskCount++;
                    }
                }
                if(countTasks==(interruptedTaskCount+completedTaskCount+failedTaskCount)){
                    executorService.shutdown();
                    callback.run();
                }
            };
            executorService.submit(runnable);
        }

        return new Context(){

            /**
             * @return возвращает количество тасков, которые на текущий момент успешно выполнились
             */
            @Override
            public int getCompletedTaskCount() {
                return completedTaskCount;
            }

            /**
             * @return возвращает количество тасков, при выполнении которых произошел Exception
             */
            @Override
            public int getFailedTaskCount() {
                return failedTaskCount;
            }

            /**
             * @return возвращает количество тасков, которые не были выполены из-за отмены
             */
            @Override
            public int getInterruptedTaskCount() {
                return interruptedTaskCount;
            }

            /**
             * отменяет выполнения тасков, которые еще не начали выполняться
             */
            @Override
            public void interrupt() {
                isInterrupt=true;
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
