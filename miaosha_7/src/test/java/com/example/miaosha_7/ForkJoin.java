package com.example.miaosha_7;

import com.sun.org.slf4j.internal.LoggerFactory;
import com.sun.org.slf4j.internal.Logger;

import java.util.concurrent.RecursiveTask;

/**
 * @author yuhao
 * @date: 2021/3/12
 * @description:
 */
public class ForkJoin extends RecursiveTask<Integer> {

	int n;
	Logger logger = (Logger) LoggerFactory.getLogger(ForkJoin.class);

	public ForkJoin(int n){
		this.n = n;
	}

	@Override
	public String toString() {
		return "{" + n + "}";
	}

	@Override
	protected Integer compute() {
		if (n == 1){
			logger.debug("join() {}",n);
			return n;
		}

		ForkJoin t1 = new ForkJoin(n - 1);
		t1.fork();
		logger.debug("fork() {} + {}",n,t1);

		int result = n + t1.join();
		logger.debug("join() {} + {} = {}",n,t1,result);
		return result;
	}
}
