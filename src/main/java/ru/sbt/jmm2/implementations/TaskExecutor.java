package ru.sbt.jmm2.implementations;

import org.apache.log4j.Logger;

import java.util.Queue;

public class TaskExecutor implements Runnable {
    private final static Logger logger = Logger.getLogger( TaskExecutor.class );

    private static volatile boolean callbackCalled = false;

    private final Queue<Runnable> tasks;
    private final AtomicContext context;
    private final Runnable callback;

    private final Object lock = new Object();

    public TaskExecutor( Queue<Runnable> tasks, AtomicContext context, Runnable callback ) {
        this.tasks = tasks;
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void run( ) {
        Thread currentThread = Thread.currentThread();
        while ( !currentThread.isInterrupted() ) {
            Runnable task = tasks.poll();
            if ( task != null ) {
                context.decRemaining();
                try {
                    task.run();
                    context.incCompleted();
                } catch ( RuntimeException e ) {
                    context.incFailed();
                }
            } else {
                if ( !callbackCalled ) {
                    synchronized ( lock ) {
                        if ( !callbackCalled ) {
                            callback.run();
                            callbackCalled = true;
                        }
                    }
                }
                logger.info( currentThread.getName() + " has no more tasks. break" );
                break;
            }
        }
        if ( currentThread.isInterrupted() )
            logger.error( currentThread.getName() + " interrupted" );
        else
            logger.info( currentThread.getName() + " completed correctly" );
    }
}
