package com.neusoft.webservices.impl;

import com.neusoft.webservices.TestQuery;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;


/**
 * 带用户密码的 WebService 调用
 * 基于 WS-Security
 * Created by QPing on 2015/6/24.
 */
public class TjQueryImplAuthTest extends TestCase{

    TestQuery hw;

    @Override
    protected void setUp() throws Exception {
        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION,  WSHandlerConstants.USERNAME_TOKEN);
        outProps.put(WSHandlerConstants.USER, "test");   // 用户名
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        // 指定在调用远程ws之前触发的回调函数WsClinetAuthHandler，其实类似于一个拦截器
        outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, WsClinetAuthHandler.class.getName());

        ArrayList list = new ArrayList();

        list.add(new SAAJOutInterceptor());
        list.add(new WSS4JOutInterceptor(outProps));

        JaxWsProxyFactoryBean svr = new JaxWsProxyFactoryBean();
        svr.getOutInterceptors().addAll(list);
        svr.setServiceClass(TestQuery.class);
        //http://222.185.235.186:9002/tj/services/TjQuery?wsdl
        svr.setAddress("http://localhost:8080/tj/services/TjQuery");
        hw = (TestQuery) svr.create();
    }

    public void testGetPerson() throws Exception {
        System.out.println(hw.getPerson("320404194705061218"));
    }

}
