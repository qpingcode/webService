package com.neusoft.dao;

import com.neusoft.common.mybatis.MyBatisRepository;
import com.neusoft.entity.Person;
import com.neusoft.entity.User;

import java.util.List;
import java.util.Map;

@MyBatisRepository
public interface UserDao {
    public List<User> get(Map params);
}
