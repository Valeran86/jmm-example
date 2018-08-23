package ru.sbt.jmm2.implementations;


import org.apache.log4j.Logger;

import java.util.Random;

public class Task implements Runnable {
    private final static Logger logger = Logger.getLogger( Task.class );

    private int number;

    public Task( int n ) {
        this.number = n;
    }

    @Override
    public void run( ) {
        double a = 0;
        for ( int k = 0; k < 10000000; k++ ) {
            a += Math.tan( k );
        }
        int[] t = { 0 };
        //random exception
        try {
            Random random = new Random();
            int mult = random.nextInt( 10 );
            a = a + ( random.nextInt( 10 ) < 2 ? t[1] : 100500 * mult );
        } catch ( IndexOutOfBoundsException e ) {
            logger.error( "Task " + number +
                    ", executing by " + Thread.currentThread().getName() +
                    ", threw exception " + e + ". result=" + a );
            throw e;
        }
        logger.info( "Task " + number + " executed by " + Thread.currentThread().getName() + ". result=" + a );
    }
}
