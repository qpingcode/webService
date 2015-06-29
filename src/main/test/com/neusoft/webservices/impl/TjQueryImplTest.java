package com.neusoft.webservices.impl;

import com.neusoft.webservices.TestQuery;
import junit.framework.TestCase;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

/**
 * WebService调用
 */
public class TjQueryImplTest extends TestCase {


    TestQuery hw;

    public void setUp() throws Exception {
        super.setUp();
        JaxWsProxyFactoryBean svr = new JaxWsProxyFactoryBean();
        svr.setServiceClass(TestQuery.class);
        svr.setAddress("http://localhost:8080/tj/services/TjQuery");
        hw = (TestQuery) svr.create();
    }

    public void testGetPerson() throws Exception {
            System.out.println(hw.getPerson("小明"));
    }

}