package com.test.java.vo;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class BaseVO {
	
	private String formMode;

	private String registId;

	private String updateId;

	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mi:ss")
	private Date registDt;

	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mi:ss")
	private Date updateDt;

	public String getRegistId() {
		return registId;
	}

	public void setRegistId(String registId) {
		this.registId = registId;
	}

	public String getUpdateId() {
		return updateId;
	}

	public void setUpdateId(String updateId) {
		this.updateId = updateId;
	}

	public Date getRegistDt() {
		return registDt;
	}

	public String getFormMode() {
		return formMode;
	}

	public void setFormMode(String formMode) {
		this.formMode = formMode;
	}

	public void setRegistDt(Date registDt) {
		this.registDt = registDt;
	}

	public Date getUpdateDt() {
		return updateDt;
	}

	public void setUpdateDt(Date updateDt) {
		this.updateDt = updateDt;
	}

}
