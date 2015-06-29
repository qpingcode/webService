package com.neusoft.webservices;

import com.neusoft.common.util.SpringContextHolder;
import com.neusoft.dao.UserDao;
import com.neusoft.entity.User;
import org.apache.commons.lang.StringUtils;
import org.apache.ws.security.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by QPing on 2015/6/24.
 */
public class ServerPasswordCallback  implements CallbackHandler {

    private HashMap<String, String> passwordsMap = new HashMap<String, String>();
    private UserDao userDao = SpringContextHolder.getBean("userDao");

    public ServerPasswordCallback(){
        // 加载用户名和密码
        addUserToMap();
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        for (int i = 0; i < callbacks.length; i++) {
            WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];

            String username = pc.getIdentifier();
            if(!StringUtils.isBlank(username)){
                pc.setPassword(passwordsMap.get(username));// ▲【这里非常重要】▲
            }
        }

    }

    private void addUserToMap(){
        List<User> users = userDao.get(null);
        for(int i = 0 ; i < users.size(); i++){
            User user = users.get(i);
            passwordsMap.put(user.getUsername(), user.getPassword());
        }
    }

}
