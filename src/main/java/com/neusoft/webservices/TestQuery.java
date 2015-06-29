package com.neusoft.webservices;

import com.neusoft.entity.*;

import javax.jws.WebService;
import java.util.List;

/**
 * Created by QPing on 2015/6/18.
 */
@WebService
public interface TestQuery {

    /**
     * 获取人员信息
     * @param name
     * @return
     */
    public List<Person> getPerson(String name);

}
