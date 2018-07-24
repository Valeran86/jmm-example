package ru.sbt.jmm;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

// Данный класс в конструкторе принимает экземпляр
// java.util.concurrent.Callable. Callable похож на Runnable,
// но результатом его работы является объект (а не void).
//
// Ваша задача реализовать метод get(), который возвращает
// результат работы Callable. Выполнение callable должен
// начинать тот поток, который первый вызвал метод get().
// Если несколько потоков одновременно вызывают этот метод,
// то выполнение должно начаться только в одном потоке,
// а остальные должны ожидать конца выполнения (не нагружая
// процессор).

// Если при вызове get() результат уже просчитан, то он
// должен вернуться сразу, (даже без задержек на вход в
// синхронизированную область).

// Если при просчете результата произошел Exception, то
// всем потокам при вызове get(), надо кидать этот Exception,
// обернутый в ваш RuntimeException (подходящее название
// своему ексепшену придумайте сами).


class Task<T> {
    private final Callable<? extends T> callable;

    private volatile T callResult;
    private volatile RuntimeException exception;

    public Task( Callable<? extends T> callable ) {
        this.callable = callable;
    }

    public T get() throws CallResultUnavailableException {
        if ( exception != null )
            throw new CallResultUnavailableException( exception );

        if ( callResult == null ) {
            synchronized ( this ) {
                if ( callResult == null ) {
                    try {
                        callResult = callable.call();
                    } catch ( Exception e ) {
                        exception = new RuntimeException( e );
                        throw new CallResultUnavailableException( exception );
                    }
                }
            }
        }

        return callResult;
    }
}

public class JmmExample {
    public static void main( String... args ) {
        Callable<Double> callable = new Callable<Double>() {
            public Double call() throws Exception {
                System.out.println( " in call " + Thread.currentThread().getName() );
                double a = 0;
                for ( int i = 0; i < 1000000; i++ )
                    a += Math.tan( i );
                return a;
            }
        };

        final Task<Double> task = new Task<Double>( callable );

        Runnable runnable = () -> {
            try {
                LocalDateTime dt = LocalDateTime.now();
                Double result = task.get();
                Duration d = Duration.between( LocalDateTime.now(), dt );
                System.out.println( Thread.currentThread().getName()
                        + ": " + result + " period: " + d );
            } catch ( CallResultUnavailableException e ) {
                e.printStackTrace( System.out );
            }
        };
        Stream.of(
                new Thread( runnable )
                , new Thread( runnable )
                , new Thread( runnable )
                , new Thread( runnable )
                , new Thread( runnable )
                , new Thread( runnable )
        ).forEach( t -> t.start() );
    }
}
