package com.neusoft.webservices.impl;

import org.apache.ws.security.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * Created by QPing on 2015/6/24.
 */
public class WsClinetAuthHandler implements CallbackHandler {
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        System.out.println("this is wsClientAuthHandlerss");
        for (int i = 0; i < callbacks.length; i++) {
            WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
            System.out.println("identifier: " + pc.getIdentifier());
            pc.setPassword("test"); // 密码
        }
    }
}
