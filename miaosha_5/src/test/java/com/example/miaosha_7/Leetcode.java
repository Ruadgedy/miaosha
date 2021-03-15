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

	public int calculate(String s) {
		s = s.replace(" ","");
		return 0;
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

	public int[][] reconstructQueue(int[][] people){
		Arrays.sort(people, (a, b) -> {
			if (a[0] == b[0]){
				return a[1]-b[1];
			}
			return b[0]-a[0];
		});
		int[][] res = new int[people.length][2];
		for (int i = 0; i < people.length; i++) {
			if (people[i][1] >= i){
				res[i] = people[i];
			}else{
				int target = people[i][1];
				for (int j = i; j > target; j--) {
					res[j] = res[j-1];
				}
				res[target] = people[i];
			}
		}
		return res;
	}
}
