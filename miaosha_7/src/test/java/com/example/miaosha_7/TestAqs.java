package com.example.miaosha_7;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author yuhao
 * @date: 2021/3/12
 * @description:
 */
@Slf4j(topic = "c.TestAqs")
public class TestAqs {
}

// 自定义锁，不可重入锁
class MyLock implements Lock{

	// 独占锁
	class MySync extends AbstractQueuedSynchronizer{
		protected MySync() {
			super();
		}

		@Override
		protected boolean tryAcquire(int arg) {
			if (compareAndSetState(0,1)){
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
			return false;
		}

		@Override
		protected boolean tryRelease(int arg) {
			setExclusiveOwnerThread(null);
			setState(0);
			return true;
		}

		@Override
		protected int tryAcquireShared(int arg) {
			return super.tryAcquireShared(arg);
		}

		@Override
		protected boolean tryReleaseShared(int arg) {
			return super.tryReleaseShared(arg);
		}

		@Override
		protected boolean isHeldExclusively() {
			return getState() == 1;
		}

		@Override
		public String toString() {
			return super.toString();
		}

		public Condition newCondition(){
			return new ConditionObject();
		}
	}

	private MySync sync = new MySync();

	@Override
	public void lock() {
		sync.tryAcquire(1);
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}

	@Override
	public boolean tryLock() {
		return sync.tryAcquire(1);
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireNanos(1,unit.toNanos(1));
	}

	@Override
	public void unlock() {
		sync.release(1);
	}

	@Override
	public Condition newCondition() {
		return sync.newCondition();
	}
}
