package com.test.java.common.interceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.test.java.service.ServiceGatewayImpl;



public class LoggingInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(ServiceGatewayImpl.class);
	
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		logger.info("====================Request Info====================");
		logger.info("request.getRequestURI() : " + request.getRequestURI());
		logger.info("request.getRequestURL() : " + request.getRequestURL());
		logger.info("request.getServletPath() : " + request.getServletPath());
		logger.info("request.getContextPath() : " + request.getContextPath());
		logger.info("request.getPathInfo() : " + request.getPathInfo());
		logger.info("request.getMethod() : " + request.getMethod());
		logger.info("this.getClass().getName() : " + handler.getClass().getName());
		logger.info("request.getRemoteAddr() : " + request.getRemoteAddr());
		

		Map<String, String> paramMap = new HashMap<String, String>(); 
		Enumeration enums = request.getParameterNames();
		while (enums.hasMoreElements()) {
			String paramName = (String) enums.nextElement();
			String[] parameters = request.getParameterValues(paramName);

			for (int i = 0; i < parameters.length; i++) {
				logger.info("parameter ::: [" + paramName + "] " + parameters[i]);
			}
		}
		logger.info("====================Request Info====================");
		return true;
	}
}
