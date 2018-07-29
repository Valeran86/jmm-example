package ru.sbt.jmm;

public class TestExecutionanager {
    private static final int CALC_COUNT = 1000000;

    public static void main( String... args ) {
        int cores = 40 * Runtime.getRuntime().availableProcessors();System.out.println( "Cores * 4: " + cores );
        Runnable[] tasks=new Runnable[cores];
        for ( int i = 0; i < cores; i++ ) {
            final int numberTask = i;
            tasks[i]=()->heavyCalc(numberTask);
        }
        ExecutionManager executionManager=new ExecutionManagerImpl(3);
        Runnable callback=()-> System.out.println("CALLBACK");
        Context context = executionManager.execute(callback, tasks);

        int i=0;
        while(i<15){
            System.out.println(++i + ". Context  before interrupt:CompletedTaskCount="+context.getCompletedTaskCount()+
                    "; FailedTaskCount="+context.getFailedTaskCount()+
                    "; InterruptedTaskCount="+context.getInterruptedTaskCount());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        context.interrupt();

        i=0;
        while(!context.isFinished()){
            System.out.println(++i + ". Context not finished:CompletedTaskCount="+context.getCompletedTaskCount()+
                    "; FailedTaskCount="+context.getFailedTaskCount()+
                    "; InterruptedTaskCount="+context.getInterruptedTaskCount());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Context finished:CompletedTaskCount="+context.getCompletedTaskCount()+
                "; FailedTaskCount="+context.getFailedTaskCount()+
                "; InterruptedTaskCount="+context.getInterruptedTaskCount()+
                "; Finished="+context.isFinished());
    }

    private static void heavyCalc (int numberTask) {
        double a = 0;
        for ( int k = 0; k < CALC_COUNT; k++ ) {
            a += Math.tan( k );
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println( "task: " + numberTask + " a=" + a +
                " from thread:" + Thread.currentThread().getName() );
    }
}
