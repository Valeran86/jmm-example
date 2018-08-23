package ru.sbt.jmm2.implementations;

import ru.sbt.jmm2.Context;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicContext implements Context {
    private final AtomicInteger remaining;
    private final int initialCount;

    private final List<Thread> pool;

    private final AtomicInteger completed = new AtomicInteger( 0 );
    private final AtomicInteger failed = new AtomicInteger( 0 );
    private final AtomicInteger interrupted = new AtomicInteger( 0 );

    AtomicContext( List<Thread> pool, int initialCount ) {
        this.pool = pool;
        this.initialCount = initialCount;
        this.remaining = new AtomicInteger( initialCount );
    }

    @Override
    public int getCompletedTaskCount( ) {
        return completed.get();
    }

    @Override
    public int getFailedTaskCount( ) {
        return failed.get();
    }

    @Override
    public int getInterruptedTaskCount( ) {
        return interrupted.get();
    }

    @Override
    public void interrupt( ) {
        pool.forEach( Thread::interrupt );
        interrupted.set( remaining.get() );
    }

    @Override
    public boolean isFinished( ) {
        return initialCount - completed.get() - failed.get() - interrupted.get() == 0;
    }

    void incCompleted( ) {
        completed.incrementAndGet();
    }

    void incFailed( ) {
        failed.incrementAndGet();
    }

    void decRemaining( ) {
        remaining.decrementAndGet();
    }
}
