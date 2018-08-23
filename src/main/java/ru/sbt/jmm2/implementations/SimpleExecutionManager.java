package ru.sbt.jmm2.implementations;

import ru.sbt.jmm2.Context;
import ru.sbt.jmm2.ExecutionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * принимает массив тасков, это задания которые ExecutionManager должен выполнять параллельно
 * (в вашей реализации пусть будет в своем пуле потоков). После завершения всех тасков должен
 * выполниться callback (ровно 1 раз). Метод execute – это неблокирующий метод, который сразу
 * возвращает объект Context.
 */
public class SimpleExecutionManager implements ExecutionManager {
    private final List<Thread> pool;
    private final int poolSize;
    private final Context context;


    public SimpleExecutionManager( int poolSize, int taskCount ) {
        this.pool = new ArrayList<>( poolSize );
        this.poolSize = poolSize;
        this.context = new AtomicContext( this.pool, taskCount );
    }

    @Override
    public Context execute( Runnable callback, Runnable... tasks ) {
        LinkedBlockingQueue<Runnable> taskList = new LinkedBlockingQueue<>( Arrays.asList( tasks ) );
        pool.addAll( IntStream.range( 0, poolSize )
                .mapToObj( i -> new Thread( new TaskExecutor( taskList, (AtomicContext) context, callback ) ) )
                .peek( Thread::start )
                .collect( Collectors.toList() ) );
        return context;
    }


}
