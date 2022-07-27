package com.test.java.service;

import java.util.List;

import javax.annotation.Resource;

//import org.apache.log4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.test.java.common.DataMap;
import com.test.java.dao.AbstractDAO;
import com.test.java.dao.ServiceGatewayDao;
import com.test.java.util.JsonConverter;



@Service("serviceGateway")
public class ServiceGatewayImpl implements ServiceGateway {
	
	//Logger log = Logger.getLogger(this.getClass());
	private static final Logger logger = (Logger) LoggerFactory.getLogger(ServiceGatewayImpl.class);
	
	@Resource(name = "ServiceGatewayDao")
	private ServiceGatewayDao serviceGatewayDao;
	
	
	private AbstractDAO getServiceDao(String queryId) {
//		if(queryId.equals("message.insertSms")
//				|| queryId.equals("message.insertLms")
//				|| queryId.startsWith("board.")
//				|| queryId.equals("message.selectSmsHisList")
//				|| queryId.equals("message.selectSmsHisListCount")
//				|| queryId.equals("message.deleteSmsHis")
//				){
//			return this.serviceGatewayDaoSms;
//		}else {
//			return this.serviceGatewayDao;
//		}
		return this.serviceGatewayDao;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<DataMap> getList(ServiceMethod method) throws Exception{
		return getServiceDao(method.getQueryId()).selectList(method.getQueryId(), method.getQueryParams());
	}
	
	public Object getOne(ServiceMethod method) throws Exception{	
		return getServiceDao(method.getQueryId()).selectOne(method.getQueryId(), method.getQueryParams());		
	}
	
	public void process(ServiceMethod method) throws Exception{		
		DataMap queryParams = (DataMap)method.getQueryParams();
		if(queryParams.get("jsonList") != null) {			
			List<DataMap> jsonList = JsonConverter.getObjectList(queryParams.getString("jsonList"), DataMap.class);
			for(DataMap dataMap : jsonList) {
				queryParams.putAll(dataMap);
				getServiceDao(method.getQueryId()).update(method.getQueryId(), queryParams);
			}
		}else {
			getServiceDao(method.getQueryId()).update(method.getQueryId(), queryParams);
		}		
	}

}
