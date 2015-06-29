package com.neusoft.webservices;

import com.neusoft.common.util.SpringContextHolder;
import com.neusoft.dao.LogDao;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ErrorHandlerInterceptor extends AbstractSoapInterceptor {

	private LogDao logDao = SpringContextHolder.getBean("logDao");

	public ErrorHandlerInterceptor() {
		super(Phase.MARSHAL);
	}

	public void handleMessage(SoapMessage message) throws Fault {
		// url与uri
		String url = null;
		String uri = null;
		// 客户端ip
		String clientIp = null;
		// 用户名
		String username = null;
		// 密码
		String password = null;
		// 服务中的方法
		String methodName = null;
		// 参数名
		String paramNames = "";
		// 参数值
		String paramValues = "";
		// xml
		String xml = "";

		String flag = "";
		String errorMessage = null;
		Throwable cause = null;

		try{
			// 错误原因
			Fault fault = (Fault) message.getContent(Exception.class);
			// 错误信息

			if (fault != null) {
				errorMessage = fault.getMessage();
				cause = fault.getCause();
			}
			// 标志
			flag = cause == null ? "success" : "error";

			Exchange exchange = message.getExchange();

			if (exchange != null) {

				Message inMessage = exchange.getInMessage();
				if (inMessage != null) {
					HttpServletRequest req = (HttpServletRequest) inMessage.get("HTTP.REQUEST");
					clientIp = getIpAddr(req);
					url = (String) inMessage.get(Message.REQUEST_URL);
					uri = (String) inMessage.get(Message.REQUEST_URI);
				}

				W3CDOMStreamWriter w3CDOMStreamWriter = (W3CDOMStreamWriter) inMessage.get(W3CDOMStreamWriter.class.getName());

				HashMap<String,String> prefix2uri = new HashMap<String,String>();
				prefix2uri.put("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
				prefix2uri.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
				prefix2uri.put("neu", "http://webservices.neusoft.com/");

				org.dom4j.io.DOMReader reader = new org.dom4j.io.DOMReader();
				reader.getDocumentFactory().setXPathNamespaceURIs(prefix2uri);
				Document document = reader.read(w3CDOMStreamWriter.getDocument());

				xml = document.asXML();

				Element usernameNode = (Element)document.selectSingleNode("//wsse:Username");
				Element passwordNode = (Element)document.selectSingleNode("//wsse:Password");
				Element bodyNode = (Element)document.selectSingleNode("//soap:Body");

				if (usernameNode != null) {
					username = usernameNode.getText();
				}

				if(passwordNode != null){
					password = passwordNode.getText();
				}

				if(bodyNode != null){
					Iterator it = bodyNode.elementIterator();
					while(it.hasNext()){
						Element method = (Element) it.next();
						methodName = method.getName();

						Iterator args = method.elementIterator();


						while(args.hasNext()){
							Element arg = (Element) args.next();
							paramNames += arg.getName() + "|";
							paramValues += arg.getText() + "|";
						}
					}
				}
			}

		}catch(Exception ex){
			System.out.println("访问日志记录失败！\n " + xml);
		}

		HashMap<String, String> map = new HashMap<String, String>();

		map.put("flag", flag);
		map.put("cause", parseNull(cause));
		map.put("errorMsg", parseNull(errorMessage));
		map.put("url", parseNull(url));
		map.put("uri", parseNull(uri));
		map.put("clientIp", parseNull(clientIp));
		map.put("username", parseNull(username));
		map.put("password", parseNull(password));
		map.put("methodName", parseNull(methodName));
		map.put("paramNames", parseNull(paramNames));
		map.put("paramValues", parseNull(paramValues));

		logDao.insertLog(map);

	}

	private String parseNull(Object value){
		return value == null ? "" : value.toString();
	}

	private String getIpAddr(HttpServletRequest request) {
		// 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址

		String ip = request.getHeader("X-Forwarded-For");

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
		} else if (ip.length() > 15) {
			String[] ips = ip.split(",");
			for (int index = 0; index < ips.length; index++) {
				String strIp = (String) ips[index];
				if (!("unknown".equalsIgnoreCase(strIp))) {
					ip = strIp;
					break;
				}
			}
		}
		return ip;
	}
}