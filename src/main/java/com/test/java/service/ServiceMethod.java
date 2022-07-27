package com.test.java.service;

public class ServiceMethod {
	
	public ServiceMethod(String queryId) {
		this.queryId = queryId;		
	}
	
	public ServiceMethod(String queryId, Object queryParams) {
		this(queryId);
		this.queryParams = queryParams;
	}
	
	private String queryId;
	
	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public Object getQueryParams() {
		return queryParams;
	}

	public void setQueryParams(Object queryParams) {
		this.queryParams = queryParams;
	}	

	private Object queryParams;
	

}
