package ru.sbt.jmm2;

import ru.sbt.jmm2.implementations.SimpleExecutionManager;
import ru.sbt.jmm2.implementations.Task;

import org.apache.log4j.Logger;

import static java.lang.Thread.sleep;

public class JmmExample {
    private final static Logger logger = Logger.getLogger( JmmExample.class );

    public static void main( String[] args ) throws InterruptedException {

        Runnable[] tasks = new Runnable[50];
        for ( int i = 0; i < tasks.length; i++ ) {
            tasks[i] = new Task( i );
        }

        SimpleExecutionManager manager = new SimpleExecutionManager( 20, tasks.length );

        Context context = manager.execute( ( ) -> logger.warn( "All tasks are completed !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" ), tasks );


        for ( int i = 0; i < 4; i++ ) {
            sleep( 2000 );
            printResult( context );
            logger.warn( "INTERRUPT!!!" );
            context.interrupt();
        }

    }

    private static void printResult( Context context ) {
        logger.info( "Completed " + context.getCompletedTaskCount() +
                ", Failed " + context.getFailedTaskCount() +
                ", Interrupted " + context.getInterruptedTaskCount() +
                ", Finished " + context.isFinished() );
    }
}
