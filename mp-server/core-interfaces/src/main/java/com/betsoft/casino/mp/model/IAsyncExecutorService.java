package com.betsoft.casino.mp.model;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface IAsyncExecutorService {

  <T> Future<T> submit(Callable<T> task) ;

  void execute(Runnable task) ;

  void shutdown();

}
