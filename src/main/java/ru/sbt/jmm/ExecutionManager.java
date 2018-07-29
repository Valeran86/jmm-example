package ru.sbt.jmm;


public interface ExecutionManager {

    Context execute(Runnable callback, Runnable... tasks);
}

