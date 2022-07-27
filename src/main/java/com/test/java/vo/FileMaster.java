package com.test.java.vo; 

import java.io.Serializable;

@SuppressWarnings("serial") 
public class FileMaster extends BaseVO implements Serializable { 
	/** 파일 아이디 */ 
	private long fileId=0L;

 	/** 사용여부 */ 
	private String useYn="";

 	/** 
	* fileId attribute 값을 설정한다 
	* @param  fileId 
	* 파일 아이디 
	*/ 
	public void setFileId(long fileId) { 
		this.fileId = fileId;
	}
	/** 
	* fileId attribute를 리턴한다 
	* @return the  fileId 
	* 파일 아이디 
	*/ 
	public long getFileId() { 
		return fileId;
	}

	/** 
	* useYn attribute 값을 설정한다 
	* @param  useYn 
	* 사용여부 
	*/ 
	public void setUseYn(String useYn) { 
		this.useYn = useYn;
	}
	/** 
	* useYn attribute를 리턴한다 
	* @return the  useYn 
	* 사용여부 
	*/ 
	public String getUseYn() { 
		return useYn;
	}


}
