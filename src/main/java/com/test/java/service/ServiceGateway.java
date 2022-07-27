package com.test.java.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.test.java.common.DataMap;


public interface ServiceGateway {
	
	List<DataMap> getList(ServiceMethod method) throws Exception;
	
	Object getOne(ServiceMethod method) throws Exception;

	@Transactional
	void process(ServiceMethod method) throws Exception;
}
