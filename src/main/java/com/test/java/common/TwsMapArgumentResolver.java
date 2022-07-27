package com.test.java.common;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;



public class TwsMapArgumentResolver implements HandlerMethodArgumentResolver{

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return RequestResolver.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    	RequestResolver requestResolver = new RequestResolver();

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        Enumeration<?> enumeration = request.getParameterNames();

        String key = null;
        String[] values = null;


        while(enumeration.hasMoreElements()){
            key = (String) enumeration.nextElement();
            values = request.getParameterValues(key);
            if(values != null){
            	requestResolver.put(key, (values.length > 1) ? values:values[0] );
            }
        }

        if(requestResolver.getString("operCd").equals("")) {
        	
        	DataMap loginUser = (DataMap)request.getSession().getAttribute("LOGIN_USER");
        	if(loginUser != null) {
        		requestResolver.put("operCd", loginUser.getString("operCd"));
        	}
        	
        }

        if(requestResolver.containsKey("pageIndex") == false){
        	requestResolver.put("pageIndex", "1");
        }

        if(requestResolver.containsKey("itemCountPerPage") == false){
        	requestResolver.put("itemCountPerPage", "15");
        }else {
        	Long pageIndex = requestResolver.getLong("pageIndex");
        	Long itemCountPerPage = Long.parseLong(requestResolver.getString("itemCountPerPage"));
        	requestResolver.put("itemCountPerPage", itemCountPerPage);
        	requestResolver.put("limitStart", pageIndex * itemCountPerPage - itemCountPerPage);        	
        }
        return requestResolver;
    }
}
