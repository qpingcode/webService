import com.neusoft.webservices.impl.TjQueryImpl;

import javax.xml.ws.Endpoint;

/**
 * Created by QPing on 2015/6/19.
 */
public class TestServer {

    public static void main(String[] args) {
        System.out.println("web service start");
        TjQueryImpl implementor= new TjQueryImpl();
        String address="http://localhost:8080/helloWorld";
        Endpoint.publish(address, implementor);
        System.out.println("web service started");

        //  访问 http://localhost:8080/helloWorld?wsdl 查看是否生效
    }

}