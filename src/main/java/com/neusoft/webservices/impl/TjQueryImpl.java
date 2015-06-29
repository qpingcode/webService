package com.neusoft.webservices.impl;

import com.neusoft.entity.Person;
import com.neusoft.webservices.TestQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by QPing on 2015/6/18.
 */
@WebService()
public class TjQueryImpl implements TestQuery {

    @Override
    public List<Person> getPerson(String name) {
        if((StringUtils.isBlank(name))){
            return null;
        }

        Person one = new Person();
        one.setName(name);
        one.setSex("男");
        one.setAge(18);

        List<Person> persons = new ArrayList<Person>();
        persons.add(one);

        return persons;
    }

}
