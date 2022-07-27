package com.test.java.vo; 

import java.io.Serializable;

@SuppressWarnings("serial") 
public class FileDetail extends BaseVO implements Serializable { 
	
	private String operCd = "";
	
	public String getOperCd() {
		return operCd;
	}
	public void setOperCd(String operCd) {
		this.operCd = operCd;
	}
	
	// get방식에선 operCd 대신 orgAbrv 사용 
	private String orgAbrv = "";
	
	public String getOrbAbrv() {
		return orgAbrv;
	}
	public void setOrbAbrv(String orgAbrv) {
		this.orgAbrv = orgAbrv;
	}
	
	/** 파일마스터 파일아이디 */ 
	private long fileId=0L;

 	/** 일련번호 */ 
	private long fileSn=0L;

 	/** 카테고리 */ 
	private String fileCtgry="";

 	/** 서버저장경로 */ 
	private String fileStreCours="";

 	/** 서버저장파일명 */ 
	private String streFileNm="";

 	/** 원본파일명 */ 
	private String orignlFileNm="";

 	/** 확장자 */ 
	private String fileExtsn="";

 	/** 파일내용 */ 
	private String fileCn="";

 	/** 파일용량 */ 
	private long fileSize=0L;

 	/** MIME TYPE */ 
	private String mimetype="";

 	/** 다운로드수 */ 
	private long dnCnt=0L;

 	/** 사용여부 */ 
	private String useAt="";

 	/** 삭제여부 */ 
	private String delYn="";
	
	private String regusr = "";
	private String updusr = "";
	
	

 	public String getRegusr() {
		return regusr;
	}
	public void setRegusr(String regusr) {
		this.regusr = regusr;
	}
	public String getUpdusr() {
		return updusr;
	}
	public void setUpdusr(String updusr) {
		this.updusr = updusr;
	}
	/** 
	* fileId attribute 값을 설정한다 
	* @param  fileId 
	* 파일마스터 파일아이디 
	*/ 
	public void setFileId(long fileId) { 
		this.fileId = fileId;
	}
	/** 
	* fileId attribute를 리턴한다 
	* @return the  fileId 
	* 파일마스터 파일아이디 
	*/ 
	public long getFileId() { 
		return fileId;
	}

	/** 
	* fileSn attribute 값을 설정한다 
	* @param  fileSn 
	* 일련번호 
	*/ 
	public void setFileSn(long fileSn) { 
		this.fileSn = fileSn;
	}
	/** 
	* fileSn attribute를 리턴한다 
	* @return the  fileSn 
	* 일련번호 
	*/ 
	public long getFileSn() { 
		return fileSn;
	}

	/** 
	* fileCategory attribute 값을 설정한다 
	* @param  fileCategory 
	* 카테고리 
	*/ 
	public void setFileCtgry(String fileCtgry) { 
		this.fileCtgry = fileCtgry;
	}
	/** 
	* fileCategory attribute를 리턴한다 
	* @return the  fileCategory 
	* 카테고리 
	*/ 
	public String getFileCtgry() { 
		return fileCtgry;
	}

	/** 
	* fileStreCours attribute 값을 설정한다 
	* @param  fileStreCours 
	* 서버저장경로 
	*/ 
	public void setFileStreCours(String fileStreCours) { 
		this.fileStreCours = fileStreCours;
	}
	/** 
	* fileStreCours attribute를 리턴한다 
	* @return the  fileStreCours 
	* 서버저장경로 
	*/ 
	public String getFileStreCours() { 
		return fileStreCours;
	}

	/** 
	* streFileNm attribute 값을 설정한다 
	* @param  streFileNm 
	* 서버저장파일명 
	*/ 
	public void setStreFileNm(String streFileNm) { 
		this.streFileNm = streFileNm;
	}
	/** 
	* streFileNm attribute를 리턴한다 
	* @return the  streFileNm 
	* 서버저장파일명 
	*/ 
	public String getStreFileNm() { 
		return streFileNm;
	}

	/** 
	* orignlFileNm attribute 값을 설정한다 
	* @param  orignlFileNm 
	* 원본파일명 
	*/ 
	public void setOrignlFileNm(String orignlFileNm) { 
		this.orignlFileNm = orignlFileNm;
	}
	/** 
	* orignlFileNm attribute를 리턴한다 
	* @return the  orignlFileNm 
	* 원본파일명 
	*/ 
	public String getOrignlFileNm() { 
		return orignlFileNm;
	}

	/** 
	* fileExtsn attribute 값을 설정한다 
	* @param  fileExtsn 
	* 확장자 
	*/ 
	public void setFileExtsn(String fileExtsn) { 
		this.fileExtsn = fileExtsn;
	}
	/** 
	* fileExtsn attribute를 리턴한다 
	* @return the  fileExtsn 
	* 확장자 
	*/ 
	public String getFileExtsn() { 
		return fileExtsn;
	}

	/** 
	* fileCn attribute 값을 설정한다 
	* @param  fileCn 
	* 파일내용 
	*/ 
	public void setFileCn(String fileCn) { 
		this.fileCn = fileCn;
	}
	/** 
	* fileCn attribute를 리턴한다 
	* @return the  fileCn 
	* 파일내용 
	*/ 
	public String getFileCn() { 
		return fileCn;
	}

	/** 
	* fileSize attribute 값을 설정한다 
	* @param  fileSize 
	* 파일용량 
	*/ 
	public void setFileSize(long fileSize) { 
		this.fileSize = fileSize;
	}
	/** 
	* fileSize attribute를 리턴한다 
	* @return the  fileSize 
	* 파일용량 
	*/ 
	public long getFileSize() { 
		return fileSize;
	}

	/** 
	* mimetype attribute 값을 설정한다 
	* @param  mimetype 
	* MIME TYPE 
	*/ 
	public void setMimetype(String mimetype) { 
		this.mimetype = mimetype;
	}
	/** 
	* mimetype attribute를 리턴한다 
	* @return the  mimetype 
	* MIME TYPE 
	*/ 
	public String getMimetype() { 
		return mimetype;
	}

	/** 
	* dnReadCnt attribute 값을 설정한다 
	* @param  dnReadCnt 
	* 다운로드수 
	*/ 
	public void setDnCnt(long dnCnt) { 
		this.dnCnt = dnCnt;
	}
	/** 
	* dnReadCnt attribute를 리턴한다 
	* @return the  dnReadCnt 
	* 다운로드수 
	*/ 
	public long getDnCnt() { 
		return dnCnt;
	}

	

	public String getUseAt() {
		return useAt;
	}
	public void setUseAt(String useAt) {
		this.useAt = useAt;
	}
	/** 
	* delYn attribute 값을 설정한다 
	* @param  delYn 
	* 삭제여부 
	*/ 
	public void setDelYn(String delYn) { 
		this.delYn = delYn;
	}
	/** 
	* delYn attribute를 리턴한다 
	* @return the  delYn 
	* 삭제여부 
	*/ 
	public String getDelYn() { 
		return delYn;
	}


}
