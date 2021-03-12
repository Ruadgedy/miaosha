package com.example.miaosha_7;

import org.junit.Test;
import org.yaml.snakeyaml.events.Event;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author yuhao
 * @date: 2021/3/11
 * @description:
 */
public class Calculate {

	public int calculate(String s){
		char[] chs = s.trim().toCharArray();
		Deque<Character> dq = new LinkedList<>();
		for (char c : chs) {
			dq.add(c);
		}
		return recur(dq);
	}

	private int recur(Deque<Character> dq){
		Stack<Integer> st = new Stack<>();
		char sign = '+';
		int num = 0;

		while (!dq.isEmpty()){
			char c = dq.pop();
			if (c == ' '){
				continue;
			}
			if (Character.isDigit(c)){
				num = num * 10 + (c-'0');
			}
			if (c =='('){
				num = recur(dq);
			}

			if (!Character.isDigit(c) || dq.isEmpty()){
				switch (sign){
					case '+': st.push(num);break;
					case '-': st.push(-num);break;
					case '*': st.push(st.pop() * num);break;
					case '/': st.push(st.pop() / num);break;
				}
				sign = c;
				num = 0;
			}

			if (c == ')'){
				break;
			}
		}
		int res = 0;
		while (!st.isEmpty()){
			res += st.pop();
		}
		return res;
	}

	@Test
	public void testCalculate(){
		LockSupport.park();
		System.out.println(calculate("1+(2*3+4)"));
	}

	static int count = 0;
	static final Object room = new Object();

	@Test
	public void testSyn() throws InterruptedException {
		Thread t1 = new Thread(() -> {
			for (int i = 0; i < 5000; i++) {
				synchronized (room){
					count++;
				}
			}
		});

		Thread t2 = new Thread(()->{
			for (int i = 0; i < 5000; i++) {
				synchronized (room){
					count--;
				}
			}
		});

		t1.start();
		t2.start();
		t1.join();
		t2.join();
		System.out.println(count);
	}

	private static <T> void demo(
			Supplier<T> arraySupplier,
			Function<T,Integer> lengthFun,
			BiConsumer<T,Integer> putConsumer,
			Consumer<T> printConsumer
	){
		List<Thread> ts = new ArrayList<>();
		T array = arraySupplier.get();
		int length = lengthFun.apply(array);
		for (int i = 0; i < length; i++) {
			ts.add(new Thread(()->{
				for (int j = 0; j < 10000; j++) {
					putConsumer.accept(array,j%length);
				}
			}));
		}
		ts.forEach(Thread::start);
		ts.forEach(t ->{
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		printConsumer.accept(array);
	}

	@Test
	public void testDemo(){
		demo(
				()-> new int[10],
				(arr) -> arr.length,
				(arr, idx) -> arr[idx]++,
				(arr) -> System.out.println(Arrays.toString(arr))
		);

		demo(
				() -> new AtomicIntegerArray(10),
				(arr) -> arr.length(),
				(arr,idx) -> arr.getAndIncrement(idx),
				(arr) -> System.out.println(arr)
		);
	}

	/**
	 * 测试原子累加器(LongAdder) 和普通原子类AtomicLong累加性能比较
	 * @param <T>
	 */
	private static <T> void addDemo(
			Supplier<T> addSupplier,
			Consumer<T> action
	){
		T adder = addSupplier.get();
		long start = System.currentTimeMillis();
		List<Thread> ts = new ArrayList<>();

		for (int i = 0; i < 40; i++) {
			ts.add(new Thread(()->{
				for (int j = 0; j < 500000; j++) {
					action.accept(adder);
				}
			}));
		}

		ts.forEach(Thread::start);
		ts.forEach(t -> {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});

		long end = System.currentTimeMillis();
		System.out.println(adder + " cost " + (end - start) + "seconds");
	}

	@Test
	public void addTest(){
		for (int i = 0; i < 5; i++) {
			addDemo(
					()-> new LongAdder(),
					(add) -> add.increment()
			);
		}

		for (int i = 0; i < 5; i++) {
			addDemo(
					()->new AtomicLong(),
					(al)->al.incrementAndGet()
			);
		}
	}

	public boolean isValidSerialization(String preorder) {
		String[] ss = preorder.split(",");
		if(ss.length == 1 &&  ss[0].equals("#")) return true;
		Deque<Character> dq = new LinkedList<Character>(){{add('a');add('a');}};
		for (int i = 1; i < ss.length; i++) {
			if (dq.isEmpty()) return false;
			if (ss[i].equals("#")) dq.pop();
			else {
				dq.add('a');
			}
		}
		return dq.isEmpty();
	}

	public static <T> void show(
			Supplier<T> arraySupplier,
			Function<T,Integer> lengthFunc,
			BiConsumer<T,Integer> putConsumer,
			Consumer<T> printConsumer
	){
		List<Thread> ts = new ArrayList<>();
		T array = arraySupplier.get();
		Integer length = lengthFunc.apply(array);
		for (int i = 0; i < length; i++) {
			ts.add(new Thread(()->{
				for (int j = 0; j < 10000; j++) {
					putConsumer.accept(array,j % length);
				}
			}));
		}
		ts.forEach(Thread::start);
		ts.forEach(thread -> {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		printConsumer.accept(array);
	}

	@Test
	public void testShow(){
		show(
				() -> new int[10],
				arr -> arr.length,
				(arr, idx) -> arr[idx]++,
				arr -> System.out.println(Arrays.toString(arr))
		);
	}

	@Test
	public void testForkJoin(){
		ForkJoinPool pool = new ForkJoinPool(4);
		System.out.println(pool.invoke(new ForkJoin(5)));
	}
}
