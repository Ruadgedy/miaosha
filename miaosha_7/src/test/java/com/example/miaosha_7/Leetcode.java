package com.example.miaosha_7;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * @author yuhao
 * @date: 2021/3/9
 * @description:
 */
public class Leetcode {

	public int longestValidParentheses(String s) {
		char[] chs = (" "+s).toCharArray();
		Stack<Integer> st = new Stack<>();
		st.push(0);
		int res = 0;
		for (int i = 1; i < chs.length; i++) {
			if(chs[i] == '('){
				st.push(i);
			}else{
				st.pop();
				if (st.isEmpty()){
					st.push(i);
				}else{
					res = Math.max(res, i-st.peek());
				}
			}
		}
		return res;
	}

	public List<String> midTo(String s){
		Stack<Character> st = new Stack<>();
		List<String> op = new ArrayList<>();
		char[] chs = s.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < chs.length; i++) {
			if (Character.isDigit(chs[i])){
				sb.append(chs[i]);
			}else{
				op.add(sb.toString());
				sb = new StringBuilder();
				if (chs[i] == '('){
					st.push(chs[i]);
					continue;
				}
				while (chs[i] == '+' || chs[i] == '-'){
					if (st.isEmpty() || st.peek() == '('){
						st.push(chs[i]);
						break;
					}else{
						op.add(String.valueOf(chs[i]));
					}
				}
				if (chs[i] == ')'){
					while (!st.isEmpty() && st.peek() != '('){
						op.add(String.valueOf(st.pop()));
					}
					if (!st.isEmpty()) st.pop();
				}
			}
		}
		while (!st.isEmpty()){
			op.add(String.valueOf(st.pop()));
		}
		return op;
	}

	@Test
	public void testMidTo(){
		System.out.println(midTo("3+(2+1)-(2+3)"));
	}

	public int calculate(String s){
		char[] chs = s.toCharArray();
		int res = 0;
		int sign = 1;  // 操作符
		int num = 0;
		Stack<Integer> st = new Stack<>(); // 保存中间结果
		for (int i = 0; i < chs.length; i++) {
			if (chs[i] == ' '){
				continue;
			}
			if (Character.isDigit(chs[i])){
				num = num * 10 + (chs[i] - '0');
				if(i < chs.length-1 && Character.isDigit(chs[i+1]))
					continue;
			}else if(chs[i] == '+' || chs[i] == '-'){
				sign = chs[i] == '+' ? 1 : -1;
//				num = 0;
			}else if (chs[i] == '('){
				st.push(res);
				st.push(sign);
				res = 0;
				sign = 1;
			}else if (chs[i] == ')'){
				num = res;
				sign = st.pop();
				res = st.pop();
			}
			res = res + num * sign;
			num = 0;
		}
		return res;
	}

	public int calculate2(String s){
		char[] chs = s.trim().toCharArray();
		return recur(chs, 0);
	}

	private int recur(char[] chs, int idx){
		Stack<Integer> st = new Stack<>();
		int num = 0;
		char sign = '+';
		for (int i = idx; i < chs.length; i++) {
			if (chs[i] == ' ') continue;
			if (Character.isDigit(chs[i])){
				num = num * 10 + (chs[i]-'0');
			}
			if (!Character.isDigit(chs[i]) || i == chs.length-1){
				if (chs[i] == '('){
					switch (sign){
						case '+':
							st.push(recur(chs,i+1));
							break;
						case '-':
							st.push(-recur(chs,i+1));
							break;
						case '*':
							st.push(recur(chs,i+1) * st.pop());
							break;
						case '/':
							st.push(st.pop() / recur(chs,i+1));
							break;
					}
					num = 0;
					sign = chs[i+1];
					continue;
				}
				if (chs[i] == ')'){
					return calStack(st);
				}
				switch (sign){
					case '+':
						st.push(num);
						break;
					case '-':
						st.push(-num);
						break;
					case '*':
						st.push(st.pop() * num);
						break;
					case '/':
						st.push(st.pop() / num);
						break;
				}
				sign = chs[i];
				num = 0;
			}
		}
		return calStack(st);
	}

	private int calStack(Stack<Integer> st){
		int res = 0;
		while (!st.isEmpty()){
			res += st.pop();
		}
		return res;
	}

	@Test
	public void testCalculate(){
		System.out.println(calculate2("3*(4+5)"));
	}

	public int largestRectangleArea(int[] heights){
		int res = 0;
		for (int i = 0; i < heights.length; i++) {
			int left = i;
			// 尽可能向左边走
			while(left >= 0 && heights[left] >= heights[i]){
				left--;
			}
			int right = i;
			// 尽可能向右边走
			while (right < heights.length && heights[right] >= heights[i]){
				right++;
			}
			int width = right - left - 1;
			res = Math.max(res,width*heights[i]);
		}
		return res;
	}

	// 给定数组nums，返回res. res[i]存放的是第一个比nums[i]大的数字，如果不存在这样的数字，返回-1
	public int[] monoStack(int[] nums){
		int[] res = new int[nums.length];
		Stack<Integer> st = new Stack<>();
		for (int i = nums.length-1; i >= 0; i--) {
			while (!st.isEmpty() && st.peek() <= nums[i]){
				st.pop();
			}
			res[i] = st.isEmpty() ? -1 : st.peek();
			st.push(nums[i]);
		}
		return res;
	}

	@Test
	public void testMonoStack(){
		System.out.println(Arrays.toString(monoStack(new int[]{2, 1, 2, 4, 3})));
		System.out.println(Arrays.toString(monoStack(new int[]{3, 1, 2, 4, 2, 1, 5})));
	}

	// 给定一个数组，数组中存放的是最近几天的温度，请返回res数组。计算对于每一天，你还要等至少多少天才能到一个更暖和的温度。如果不存在这样的一天，返回0
	public int[] monoStack2(int[] nums){
		int[] res = new int[nums.length];
		Stack<Integer> st = new Stack<>();
		for (int i = nums.length-1; i >= 0; i--) {
			while (!st.isEmpty() && nums[i] >= nums[st.peek()]){
				st.pop();
			}
			res[i] = st.isEmpty() ? 0 : st.peek() - i;
			st.push(i);
		}
		return res;
	}

	@Test
	public void testMonoStack2(){
		System.out.println(Arrays.toString(monoStack2(new int[]{73, 74, 75, 71, 69, 72, 76, 73})));
	}

	/**
	 * 环形数组，返回某一个元素后面更大的元素
	 * @return
	 */
	public int[] monoStack3(int[] nums){
		int[] res = new int[nums.length];
		Stack<Integer> st = new Stack<>();
		for (int i = nums.length*2-1; i >= 0; i--) {
			while(!st.isEmpty() && nums[i%nums.length] >= st.peek()){
				st.pop();
			}
			res[i%nums.length] = st.isEmpty() ? -1 : st.peek();
			st.push(nums[i%nums.length]);
		}
		return res;
	}

	@Test
	public void testMonoStack3(){
		System.out.println(Arrays.toString(monoStack3(new int[]{2, 1, 2, 4, 3})));
	}
}
