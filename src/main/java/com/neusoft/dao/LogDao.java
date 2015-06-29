package com.neusoft.dao;

import com.neusoft.common.mybatis.MyBatisRepository;
import com.neusoft.entity.User;

import java.util.List;
import java.util.Map;

@MyBatisRepository
public interface LogDao {
    public void insertLog(Map params);
}
