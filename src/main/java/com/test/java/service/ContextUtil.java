package com.test.java.service;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.test.java.common.DataMap;


@Component
public class ContextUtil {
	
	@Autowired
	ServletContext context;
		
	@Resource(name="serviceGateway")
	private ServiceGateway serviceGateway;
	
	@Autowired
	HttpServletRequest request;
	
	public static final String APP_CODE = "APP_CODE_LIST";
	public static final String APP_MENU = "APP_MENU_LIST";
	public static final String APP_BOARD = "APP_BOARD_LIST";
	
	
	public List<DataMap> getCodeList(){
		return (List<DataMap>)context.getAttribute(APP_CODE);
	}
	
	public List<DataMap> getMenuList(){
		return (List<DataMap>)context.getAttribute(APP_MENU);
	}
	
	public List<DataMap> getBoardList(){
		return (List<DataMap>)context.getAttribute(APP_BOARD);
	}

	
	public void initStartData() throws Exception{
		List<DataMap> menuList = (List<DataMap>)serviceGateway.getList((new ServiceMethod("menuinfo.selectList", null)));
		context.setAttribute(APP_MENU, menuList);

		
		List<DataMap> codeList = (List<DataMap>)serviceGateway.getList((new ServiceMethod("codeinfo.selectList", null)));
		context.setAttribute(APP_CODE, codeList);

		List<DataMap> boardConfigList = (List<DataMap>)serviceGateway.getList((new ServiceMethod("boardConfig.selectList", null)));
		context.setAttribute(APP_BOARD, boardConfigList);
		
		/**
		 * parentCodeId 별로 메모리에 탑재
		 */
		DataMap queryMap = new DataMap();
		queryMap.put("parentCodeId", "ROOT");
		List<DataMap> groupCodeList = (List<DataMap>)serviceGateway.getList((new ServiceMethod("codeinfo.selectList", queryMap)));
		if(groupCodeList != null && groupCodeList.size() > 0) {
			
			String parentCodeId = "";
			List<DataMap> childCodeList = null;
			
			/* 검색 속도를 빨리하기 위해 기존 배열을 복사해서 매칭될때마다 해당 데이터 삭제 처리 */
			List<DataMap> codeListSearch = new ArrayList();
			codeListSearch.addAll(codeList);
			
			for(DataMap m : groupCodeList) {
				parentCodeId = m.getString("codeId");
				childCodeList = new ArrayList();
				for(DataMap c : codeListSearch) {
					if(c.getString("parentCodeId").equals(parentCodeId)) {
						childCodeList.add(c);
					}
				}
				codeListSearch.removeAll(childCodeList);					
				context.setAttribute("CODE_" + parentCodeId.toUpperCase(), childCodeList);
			}
		}
	}
	
	public DataMap getBoardConfig(String boardPath) throws Exception{
		DataMap boardConfig = new DataMap();
		List<DataMap> boardList =this.getBoardList();
		
		for(DataMap data : boardList){
			if(data.getString("bbsPathPrefix").equals(boardPath)){
				boardConfig = data;
			}
		}

		return boardConfig;
	}
	
	public List<DataMap> getCodeList(String groupCodeId) throws Exception{
		
		List<DataMap> codeList = new ArrayList<DataMap>();
		
		List<DataMap> allList =this.getCodeList();
		for(DataMap m : allList){
			if(m.getString("groupCodeId").equals(groupCodeId)){
				codeList.add(m);
			}
		}
		return codeList;
	}
	
	public List<DataMap> getMenuList(String menuId) throws Exception{
		
		List<DataMap> menuList = new ArrayList<DataMap>();
		if(menuId.equals("")) {
			return menuList;
		}
		
		List<DataMap> allList =this.getMenuList();
		for(DataMap m : allList){
			if(m.getString("parentMenuId").equals(menuId)){
				menuList.add(m);
			}
		}
		return menuList;
	}
	
	public DataMap getMenu(String url){
		if(url.equals("")) {
			return null;
		}
		System.out.println("$$$$$$$$$$$$ url " + url);
		List<DataMap> allList =this.getMenuList();
		DataMap menuInfo = null;
		for(DataMap m : allList){
			if(m.getString("menuUrl").equals(url)){
				menuInfo = m;
				System.out.println("$$$$$$$$$$$$ current menu Is " + menuInfo.getString("menuNm"));
				break;
			}
		}
		return menuInfo;
	}	
	
	public DataMap getMenuFromPath(String requestUri){
		List<DataMap> allList =this.getMenuList();
		DataMap menuInfo = null;		
		System.out.println("$$$$$$$$$$$$ parameter " + requestUri);		
		String menuUrl;
		for(DataMap m : allList){
			menuUrl = m.getString("menuUrl");
			if(menuUrl.indexOf("/") >= 0) {
				menuUrl = menuUrl.substring(0, menuUrl.lastIndexOf("/") + 1);
				if(requestUri.startsWith(menuUrl) && m.getString("isLeaf").equals("1")) {
					menuInfo = m;
					System.out.println("$$$$$$$$$$$$ current menu Is " + m.getString("menuNm"));
					break;
				}
			}
			
		}
		return menuInfo;
	}
	
	public String setLoginUserMenu(HttpSession session, String mberNo) throws Exception{
		DataMap queryMap = new DataMap();
		queryMap.put("mberNo", mberNo);
		queryMap.put("parentMenuId", "A");
		queryMap.put("roleMenuSe", "MENU");
		
		String enterPage = "";
		List<DataMap> adminMenuList = (List<DataMap>)serviceGateway.getList((new ServiceMethod("menuinfo.selectMberMenuList", queryMap)));
		
		List<DataMap> userMenuList = new ArrayList<DataMap>();
		List<DataMap> subMenuList = null;
		List<DataMap> leafMenuList = null;
		for(DataMap m : adminMenuList) {
			if(m.getString("currentLevel").equals("2") && m.getString("parentMenuId").equals("A")) {								
				if(enterPage.equals("")){
					enterPage = m.getString("menuUrl");
				}
				
				subMenuList = new ArrayList<DataMap>();
				for(DataMap m2 : adminMenuList) {
					if(m2.getString("parentMenuId").equals(m.getString("menuId"))) {
						subMenuList.add(m2);
						leafMenuList = new ArrayList<DataMap>();
						for(DataMap m3 : adminMenuList) {
							if(m3.getString("parentMenuId").equals(m2.getString("menuId"))) {
								leafMenuList.add(m3);
							}
						}
						if(leafMenuList.size() > 0) {
							m.put("leafMenu", "OK");
						}
						m2.put("subMenuList", leafMenuList);
					}
				}
				m.put("subMenuList", subMenuList);
				userMenuList.add(m);
			}
			
		}
		//현재는 유저별 권한 없고 무조건 전체메뉴를 리턴함
		session.setAttribute("userMenuList", adminMenuList);
		return enterPage;
	}
	
	public DataMap getRequestLoggingMap(HttpServletRequest request) {
		String ip = request.getRemoteAddr();
		String userAgent = request.getHeader("User-Agent");
		String url = request.getRequestURI();
		
		DataMap loginUser = (DataMap)request.getSession().getAttribute("LOGIN_USER");
		if(loginUser == null) {
			loginUser = new DataMap();
		}
		        
        DataMap param = new DataMap();

        param.put("accesSe", "MNT");
        param.put("accesUrl", url);
        
        DataMap pageMenu = null;
    	if(!url.endsWith(".jsp") && !url.endsWith("/file/download.do")) {    	
    		pageMenu = this.getMenu(url);
    		//pageMenu = this.getMenuFromPath(request.getRequestURI());
    	}
    	if(pageMenu == null) {
    		pageMenu = new DataMap();
    	}
    	
    	if(pageMenu.isEmpty()) {
    		// 설문 & 동의서
    		if(url.equals("/user/agreementForm/process.do")) {
    			pageMenu.put("menuNm", "동의서 저장");
    			pageMenu.put("accesAction", "저장");
    		} else if(url.equals("/user/agreementForm/update.do")) {
    			pageMenu.put("menuNm", "동의서 수정");
    			pageMenu.put("accesAction", "수정");
    		} else if(url.equals("/user/agreementForm/registSignature.do")) {
    			pageMenu.put("menuNm", "온라인서명 저장");
    			pageMenu.put("accesAction", "저장");
    		} else if(url.equals("/user/survey/denyProcess.do")) {
    			pageMenu.put("menuNm", "건강영향설문 거부");
    			pageMenu.put("accesAction", "저장");
    		} else if(url.equals("/user/survey/write.do")) {
    			pageMenu.put("menuNm", "건강영향설문 편집");
    			pageMenu.put("accesAction", "편집");
    		} else if(url.equals("/user/survey/update.do")) {
        		pageMenu.put("menuNm", "건강영향설문 저장");
        		pageMenu.put("accesAction", "저장");
        	} else if(url.equals("/user/survey/editSurvey.do")) {
        		pageMenu.put("menuNm", "건강영향설문 수정");
        		pageMenu.put("accesAction", "수정");
        	} else if(url.equals("/user/pdf/savePDF.do")) {
        		pageMenu.put("menuNm", "PDF 저장");
        		pageMenu.put("accesAction", "저장");
        	} else if(url.equals("/api/survey/surveyCdList.do")) {
        		pageMenu.put("menuNm", "설문코드 목록 조회");
        		pageMenu.put("accesAction", "목록 조회");
        	} else if(url.equals("/api/survey/surveyQnList.do")) {
        		pageMenu.put("menuNm", "설문항목 조회");
        		pageMenu.put("accesAction", "조회");
        	} 
    		
    		// 대상자
    		if(url.equals("/api/victim/list.do")) {
    			pageMenu.put("menuNm", "대상자목록 조회");
    			pageMenu.put("accesAction", "목록조회");
    		} else if(url.equals("/api/listYear/newTransListYear.do")) {
    			pageMenu.put("menuNm", "신규 이관자 목록");
    			pageMenu.put("accesAction", "목록조회");
    		} else if(url.equals("/api/listYear/newListYear.do")) {
    			pageMenu.put("menuNm", "신규 배정자 목록");
    			pageMenu.put("accesAction", "목록조회");
    		} 
    		
    		// 변경이력
    		if(url.indexOf("/api/his/") >= 0) {
				pageMenu.put("menuNm", "정보변경 이력");
			} 
    		
    		// 검진 
    		if(url.indexOf("/api/hsptTest/") >= 0) {
    			pageMenu.put("menuNm", "건강검진 조회");
    		}
    		if(url.equals("/api/hsptTest/saveItemConfigOne.do") || url.equals("/api/hsptTest/saveItemConfigList.do")) {
    			pageMenu.put("menuNm", "검진항목 저장");
    			pageMenu.put("accesAction", "저장");
    		}
    		
    		// IP 허용 
    		if(url.equals("/acceptIp/applcntPopup.do")) {
    			pageMenu.put("menuNm", "IP 허용 신청");
    			pageMenu.put("accesAction", "화면조회");
    		} else if(url.equals("/api/acceptIp/saveAcceptIp.do")) {
    			pageMenu.put("menuNm", "IP 허용 신청");
    			pageMenu.put("accesAction", "IP 허용 신청");
    		} else if(url.equals("/api/acceptIp/select.do")) {
    			pageMenu.put("menuNm", "허용IP 조회");
    			pageMenu.put("accesAction", "상세조회");
    		}
    		
    		// SMS
			if(url.equals("/api/smsdb/cmpgnSmsSendProcess.do")) {
    			pageMenu.put("menuNm", "SMS 대량전송");
    			pageMenu.put("accesAction", "SMS 전송");
    		} else if(url.equals("/api/smsdb/sendProcess.do")) {
    			pageMenu.put("menuNm", "SMS 전송");
    			pageMenu.put("accesAction", "SMS 전송");
    		}
    		
    		// 엑셀 업로드 & 다운 
    		if(url.equals("/rest/grid/hsptlIdUploadGrid.do") || url.equals("/rest/grid/hsptlIdUploadProcess.do")) {
    			pageMenu.put("menuNm", "환자번호 업로드");
    			pageMenu.put("accesAction", "엑셀 업로드");
    		} else if(url.equals("/rest/grid/cmpgnUploadGrid.do") || url.equals("/rest/grid/hsptlIdUploadProcess.do")) {
    			pageMenu.put("menuNm", "SMS 대량전송 업로드");
    			pageMenu.put("accesAction", "엑셀 업로드");
    		} else if(url.equals("/rest/grid/cmpgnReservedUploadGrid.do") || url.equals("/rest/grid/cmpgnReservedUploadProcess.do")) {
    			pageMenu.put("menuNm", "SMS 대량전송예약 업로드");
    			pageMenu.put("accesAction", "엑셀 업로드");
    		} else if(url.equals("/rest/grid/victimListUploadGrid.do") || url.equals("/rest/grid/victimListUploadProcess.do")) {
    			pageMenu.put("menuNm", "대상자목록 업로드");
    			pageMenu.put("accesAction", "엑셀 업로드");
    		} else if(url.equals("/rest/grid/itemConfigUploadGrid.do") || url.equals("/rest/grid/itemConfigUploadProcess.do")) {
    			pageMenu.put("menuNm", "검진항목단위 업로드");
    			pageMenu.put("accesAction", "엑셀 업로드");
    		} else if(url.startsWith("/rest/grid/") && ( url.endsWith("/excel.do") || url.endsWith("/Excel.do"))) {
    			pageMenu.put("menuNm", "엑셀 다운로드");
    			pageMenu.put("accesAction", "엑셀 다운로드");
    		}
    		
    		// 파일 & 게시판 
    		if(url.endsWith("/file/fileDownload.do")) {
    			pageMenu.put("menuNm", "파일 다운로드");
    			pageMenu.put("accesAction", "파일 다운로드");
    		} else if(url.endsWith("/file/fileForm.do")) {
    			pageMenu.put("menuNm", "업로드양식 다운로드");
    			pageMenu.put("accesAction", "파일 다운로드");
    		} else if(url.endsWith("/file/removeProcess.do")) {
    			pageMenu.put("menuNm", "파일 삭제");
    			pageMenu.put("accesAction", "파일 삭제");
    		} else if(url.endsWith("/file/uploadProcess.do")) {
    			pageMenu.put("menuNm", "파일 업로드");
    			pageMenu.put("accesAction", "파일 업로드");
    		} else if(url.equals("/file/common/image.do")) {
    			pageMenu.put("menuNm", "이미지 조회");
    			pageMenu.put("accesAction", "이미지 조회");
    		} else if(url.equals("/api/board/selectBoardListTop5.do")) {
    			pageMenu.put("menuNm", "최근 게시글 목록");
    			pageMenu.put("accesAction", "목록조회");
    		} else if(url.indexOf("scan") >= 0) {
				pageMenu.put("menuNm", "스캔파일");
			}
    		
    		// 기타 
    		if(url.equals("/user/login.do")) {
    			pageMenu.put("menuNm", "홈페이지 메인");
    			pageMenu.put("accesAction", "홈페이지 메인");
    		} else if(url.equals("/api/mber/loginProcess.do")) {
    			pageMenu.put("menuNm", "로그인");
    			pageMenu.put("accesAction", "로그인");
    		} else if(url.equals("/api/mber/logoutProcess.do")) {
    			pageMenu.put("menuNm", "로그아웃");
    			pageMenu.put("accesAction", "로그아웃");
    		} else if(url.equals("/api/mber/insertMber.do")) {
    			pageMenu.put("menuNm", "회원가입");
    			pageMenu.put("accesAction", "회원가입");
    		} else if(url.equals("/api/org/list.do")) {
    			pageMenu.put("menuNm", "보건센터 목록");
    			pageMenu.put("accesAction", "목록조회");
    		} else if(url.equals("/api/code/list.do")) {
    			pageMenu.put("menuNm", "공통코드 목록");
    			pageMenu.put("accesAction", "목록조회");
    		} else if(url.equals("/api/org/operView.do")) {
    			pageMenu.put("menuNm", "보건센터");
    			pageMenu.put("accesAction", "상세조회");
    		} else if(url.indexOf("/api/log/") >= 0) {
				pageMenu.put("menuNm", "접속로그");
			} else if(url.indexOf("/home/") >= 0) {
				pageMenu.put("menuNm", "홈");
			}
    		
    	}
    	
    	param.put("operCd", loginUser.getString("operCd"));
		param.put("accesMenuNm", pageMenu.getString("menuNm"));
        param.put("accesIp", ip);
        param.put("locale", request.getLocale().toLanguageTag());
        param.put("accesWbsr", userAgent);
        param.put("accesWebId", loginUser.getString("webId"));
        param.put("accesKorNm", loginUser.getString("korNm"));
        
        if(pageMenu.getString("accesAction") == null || pageMenu.getString("accesAction").equals("")) {
        	url = url.toLowerCase();
        	if(url.endsWith("list.do")) {
        		param.put("accesAction", "목록조회");			
        	}else if(url.endsWith("view.do") || url.endsWith("form.do")) {
        		param.put("accesAction", "상세조회");
        	}else if(url.endsWith("process.do")) {
        		String formMode = request.getParameter("formMode");
        		if(formMode.equals("insert") || formMode.equals("add")) {
        			param.put("accesAction", "데이터등록");
        		}else if(formMode.equals("update") || formMode.equals("edit")) {
        			param.put("accesAction", "데이터변경");
        		}else if(formMode.equals("delete") || formMode.equals("remove")) {
        			param.put("accesAction", "데이터삭제");
        		}else {
        			param.put("accesAction", "데이터처리");
        		}
        	}else if(url.endsWith("download.do")){
        		param.put("accesAction", "파일다운로드");
        	}else {
        		param.put("accesAction", "화면조회");
        	}
        } else {
        	param.put("accesAction", pageMenu.getString("accesAction"));	
        }
		//param.put("accesRm", sb.toString());
		
		return param;
	}
	
	public HttpSession getSession() throws Exception{
		return request.getSession();
	}

}