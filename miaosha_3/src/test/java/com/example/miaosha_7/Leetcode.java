package com.example.miaosha_7;

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
}
