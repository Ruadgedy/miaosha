package com.example.miaosha_7.dao;

import com.example.miaosha_7.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author yuhao
 * @date: 2021/3/8
 * @description:
 */
@Mapper
public interface UserDao {

	@Select("select * from user where id=#{id}")
	public User getById(@Param("id") int id);

	@Insert("insert into user(id, name) VALUES (#{id},#{name})")
	public int insert(User user);

}
