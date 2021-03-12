package com.example.miaosha_7;

import org.junit.Test;

import java.util.ArrayList;
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
		Stack<Integer> st = new Stack<>();
		int num = 0;
		char sign = '+';
		int pre = 0;
		for (int i = 0; i < chs.length; i++) {
			if (chs[i] == ' ') continue;
			if (Character.isDigit(chs[i])){
				num = num * 10 + (chs[i] - '0');
			}
			if (!Character.isDigit(chs[i]) || i == chs.length-1){
				switch (sign){
					case '+':
						st.push(num);break;
					case '-':
						st.push(-num);break;
					case '*':
						pre = st.pop();
						st.push(pre * num);
						break;
					case '/':
						pre = st.pop();
						st.push(pre /num);
						break;
				}
				num = 0;
				sign = chs[i];
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
		System.out.println(calculate2("3*4+5"));
	}
}
