package com.test.java.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.test.java.common.DataMap;
import com.test.java.common.RequestResolver;
import com.test.java.dao.ServiceGatewayDao;
import com.test.java.service.ServiceGateway;
import com.test.java.service.ServiceMethod;
import com.test.java.util.EgovStringUtil;
import com.test.java.util.ExcelDownloadUtil;
import com.test.java.util.ExcelUploadUtil;
import com.test.java.util.GridXmlBuilder;
import com.test.java.util.JsonConverter;
import com.test.java.vo.FileDetail;
import org.json.JSONArray;

@Controller
public class GridXmlController {

	@Autowired
	HttpServletRequest request;

	@Autowired
	SqlSession sqlSession;

	@Autowired
	ExcelDownloadUtil excelDownComponent;
	
	@Resource(name = "serviceGateway")
	private ServiceGateway serviceGateway;

	@Resource(name = "ServiceGatewayDao")
	private ServiceGatewayDao serviceGatewayDao;

	@RequestMapping(value = "/rest/grid/init.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> initXml(@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam Map<String, Object> map) {
		try {
			String xmlValue = GridXmlBuilder.getCommonXmlString(gridConfigData, null, request, 0, 0);
			return new ResponseEntity<String>(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();

		}
		return new ResponseEntity<String>(null, null, HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = { "/rest/grid/{className}/{methodName}/excel.do", "/rest/treegrid/{className}/{methodName}/excel.do"}, produces="text/xml;charset=utf-8")
	public void largeExcelDown(RequestResolver resolver, @PathVariable("className") String className,
			@PathVariable("methodName") String methodName,
			@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam("excelFileName") String excelFileName, @RequestParam("mbtlno") String mbtlno, HttpServletResponse response, Model model) {

		//ExcelDownloadUtil excelDownComponent = new ExcelDownloadUtil();

		try {
			if (resolver.getMap().getString("operCd").equals("과학원")) {
				resolver.remove("operCd");
			}
			resolver.put("itemCountPerPage", 0);
			resolver.put("pageIndex", 1);

			List<DataMap> dataList = this.getDataList(className, methodName, gridConfigData, resolver.getMap());

			// convert String to List<DataMap>
			JSONArray jsonArray = new JSONArray(gridConfigData);
			ObjectMapper mapper = new ObjectMapper();
			
			List<DataMap> gridCellList = mapper.readValue(gridConfigData, TypeFactory.defaultInstance().constructCollectionType(List.class, DataMap.class));

			
			System.out.println(gridCellList);
			
			// POI 라이브러리 사용, xlsx 확장자로 다운
			SXSSFWorkbook workbook = excelDownComponent.excelFileDownloadProcess(dataList, gridCellList);
			model.addAttribute("workbook", workbook);
			model.addAttribute("workbookName", excelFileName);
			
			System.out.println("mbtlno: " + mbtlno);
			excelDownComponent.renderMergedOutputModel2((Map<String, Object>) model, request, response, mbtlno);

		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	@RequestMapping({ "/rest/grid/multiSheet/{className}/{methodName}/excel.do", "/rest/treegrid/multiSheet/{className}/{methodName}/excel.do" })
	public void surveyExcelDown(RequestResolver resolver, @PathVariable("className") String className,
			@PathVariable("methodName") String methodName,
			@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam(value = "surveyQnList") String surveyQnData,
			@RequestParam("excelFileName") String excelFileName, @RequestParam("mbtlno") String mbtlno, HttpServletResponse response, Model model) {
		try{			
			if(resolver.getMap().getString("operCd").equals("과학원")) {
				resolver.remove("operCd");
			}
			resolver.put("itemCountPerPage", 0);
			resolver.put("pageIndex", 1);
			
			JSONArray gridConfigDataList = new JSONArray(gridConfigData); //헤더 리스트
			ObjectMapper mapper = new ObjectMapper();
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			
			if(methodName.equals("selectSurveyQnExcel")) {
				JSONArray surveyQnList = new JSONArray(surveyQnData); //설문항목 리스트
				String[] surveySn = resolver.getString("surveySn").split("/");
				String[] surveyVer = resolver.getString("surveyVer").split("/");
				
				
				DataMap queryMap = new DataMap();
				queryMap.put("includeListAtN", "Y");
				List<DataMap> survey = serviceGateway.getList(new ServiceMethod("surveyNew.selectSurveyCdList", queryMap));
				
				
				for(int i=0, j=0; i < gridConfigDataList.length(); i++) {
					List<DataMap> gridCellList = mapper.readValue(gridConfigDataList.get(i).toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, DataMap.class));
					resolver.put("surveyQnList",JsonConverter.getObjectList(surveyQnList.get(i).toString(), String.class));
					resolver.put("surveySn", surveySn[i]);
					
					for(DataMap m : survey) {
						if(m.getString("surveySn").equals(surveySn[i])) {
							resolver.put("sheetName", m.getString("surveyCd") + "_" + surveyVer[i]);
							break;
						}
					}
					
					resolver.put("surveyVer", surveyVer[i]);
					
					resolver.put("workbookIndex", j); //시트 인덱스는 dataList가 있을때만 카운트
					List<DataMap> dataList = this.getDataList(className, methodName, gridConfigData, resolver.getMap());
					// POI 라이브러리 사용, xlsx 확장자로 다운
					//if(dataList != null && dataList.size() > 0) {//해당 데이터 로우가 있을 경우에만 시트 생성
						workbook = excelDownComponent.makeExcelWorkbook(workbook, dataList, gridCellList, resolver.getMap());
						j++;					
					//}
				}
			}else {
				for(int i=0, j=0; i < gridConfigDataList.length(); i++) {
					List<DataMap> gridCellList = mapper.readValue(gridConfigDataList.get(i).toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, DataMap.class));
					resolver.put("sheetName", j); //시트 명 세팅이 필요함
					resolver.put("workbookIndex", j); //시트 인덱스는 dataList가 있을때만 카운트
					List<DataMap> dataList = this.getDataList(className, methodName, gridConfigData, resolver.getMap());
					if(dataList != null && dataList.size() > 0) {//해당 데이터 로우가 있을 경우에만 시트 생성
						workbook = excelDownComponent.makeExcelWorkbook(workbook, dataList, gridCellList, resolver.getMap());
						j++;					
					}
				}
			}
			
			
			model.addAttribute("workbook", workbook);
			model.addAttribute("workbookName", excelFileName);
			
			excelDownComponent.renderMergedOutputModel2((Map<String, Object>) model, request, response, mbtlno);
		}catch(Exception ex){
			ex.printStackTrace();
			
		}
	}

	@RequestMapping(value = "/rest/grid/{className}/{methodName}.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> listXml(@PathVariable("className") String className,
			@PathVariable("methodName") String methodName,
			@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam(value = "pageIndex", required = false, defaultValue = "0") long pageIndex,
			@RequestParam(value = "recordCountPerPage", required = false, defaultValue = "0") long recordCountPerPage,
			RequestResolver resolver) {
		try {

			List<DataMap> dataList = this.getDataList(className, methodName, gridConfigData, resolver.getMap());
			String xmlValue = GridXmlBuilder.getCommonXmlString(gridConfigData, dataList, request, pageIndex,
					recordCountPerPage);
			return new ResponseEntity<String>(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(null, null, HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/rest/treegrid/{className}/{methodName}.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> treelistXml(@PathVariable("className") String className,
			@PathVariable("methodName") String methodName,
			@RequestParam(value = "gridConfigData") String gridConfigData, RequestResolver resolver) {
		try {

			List<DataMap> dataList = this.getDataList(className, methodName, gridConfigData, resolver.getMap());
			String xmlValue = GridXmlBuilder.getTreeXmlString(gridConfigData, dataList, request);
			return new ResponseEntity<String>(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(null, null, HttpStatus.BAD_REQUEST);
	}

	@SuppressWarnings("unchecked")
	private List<DataMap> getDataList(String className, String methodName, String gridConfigData, DataMap map)
			throws Exception {
		return (List<DataMap>) serviceGateway.getList(new ServiceMethod(className + "." + methodName, map));
	}

	/* 엑셀 업로드 파일업로드 초기 헤더 세팅 */
	@RequestMapping(value = "/rest/grid/showGridHeader.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> showGridHeader(@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam(value = "pageIndex", required = false, defaultValue = "0") long pageIndex,
			@RequestParam(value = "recordCountPerPage", required = false, defaultValue = "0") long recordCountPerPage,
			RequestResolver resolver, HttpSession session) {

		try {
			long totalCnt = 0L;
			String xmlValue = GridXmlBuilder.generateHeader(gridConfigData, request, totalCnt, pageIndex,
					recordCountPerPage);

			return new ResponseEntity<String>(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(null, null, HttpStatus.BAD_REQUEST);
	}

	/* 엑셀 업로드 파일업로드(환자번호) & 그리드 */
	@RequestMapping(value = "/rest/grid/hsptlIdUploadGrid.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> hsptlIdUploadGrid(@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam(value = "pageIndex", required = false, defaultValue = "0") long pageIndex,
			@RequestParam(value = "recordCountPerPage", required = false, defaultValue = "0") long recordCountPerPage,
			RequestResolver resolver, HttpSession session) {

		try {
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			FileDetail fileVO = (FileDetail) serviceGateway.getOne(new ServiceMethod("file.select", resolver.getMap()));
			List<DataMap> tempList = new ArrayList<DataMap>();

			System.out.println(fileVO.getFileStreCours() + fileVO.getStreFileNm());

			ExcelUploadUtil excelComponent = new ExcelUploadUtil();
			List<HashMap<String, Object>> list = excelComponent
					.excelProcess(fileVO.getFileStreCours() + fileVO.getStreFileNm());
			DataMap dataMap;
			for (int i = 0; i < list.size(); i++) {
				dataMap = new DataMap();
				dataMap.put("rowNum", i);
				dataMap.put("operCd", list.get(i).get("보건센터"));
				dataMap.put("assignSe", list.get(i).get("할당구분"));
				dataMap.put("sufrerPin", list.get(i).get("대상자 식별번호"));
				dataMap.put("sufrerNm", list.get(i).get("대상자 이름(참조)"));
				dataMap.put("hsptlId", list.get(i).get("병원환자번호"));
				dataMap.put("regusr", loginUser.getString("webId"));
				tempList.add(dataMap);
			}
			String xmlValue = GridXmlBuilder.getCommonXmlString(gridConfigData, tempList, request, pageIndex,
					recordCountPerPage);
			return new ResponseEntity<String>(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(null, null, HttpStatus.BAD_REQUEST);
	}

	/* 환자번호 업로드 프로세스 */
	@RequestMapping("/rest/grid/hsptlIdUploadProcess.do")
	@ResponseBody
	@Transactional
	public ResponseEntity<Map> hsptlIdUploadProcess(RequestResolver resolver, HttpSession session) throws Exception {

		List<DataMap> excelList = JsonConverter.getObjectList(resolver.getString("submitData"), DataMap.class);
		DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");

		int successCnt = 0;

		// update 결과 확인하기위해 serviceGateway.process 사용안하고, dao 직접 만들어서 사용

		List<DataMap> failList = new ArrayList<DataMap>();

		for (DataMap item : excelList) {

			item.put("updusr", loginUser.getString("webId"));

			int result= 0;
		//	int result = (int) serviceGatewayDao.update("victim.updateHsptlId", item);

			if (result > 0) {
				successCnt++;
			} else {
				failList.add(item);
			}

		}

		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		System.out.println("totalCnt: " + excelList.size());
		System.out.println("successCnt: " + successCnt);
		System.out.println("failList: " + failList);

		resolver.put("totalCnt", excelList.size());
		resolver.put("successCnt", successCnt);
		resolver.put("failList", failList);
		return new ResponseEntity<Map>(resolver.getMap(), null, HttpStatus.OK);
	}

	/* 엑셀 업로드 파일업로드(SMS 대량 전송) & 그리드 */
	@RequestMapping(value = "/rest/grid/cmpgnUploadGrid.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> cmpgnUploadGrid(@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam(value = "pageIndex", required = false, defaultValue = "0") long pageIndex,
			@RequestParam(value = "recordCountPerPage", required = false, defaultValue = "0") long recordCountPerPage,
			RequestResolver resolver, HttpSession session) {

		try {
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			FileDetail fileVO = (FileDetail) serviceGateway.getOne(new ServiceMethod("file.select", resolver.getMap()));
			List<DataMap> tempList = new ArrayList<DataMap>();

			System.out.println(fileVO.getFileStreCours() + fileVO.getStreFileNm());

			ExcelUploadUtil excelComponent = new ExcelUploadUtil();
			List<HashMap<String, Object>> list = excelComponent
					.excelProcess(fileVO.getFileStreCours() + fileVO.getStreFileNm());
			DataMap dataMap;
			for (int i = 0; i < list.size(); i++) {
				dataMap = new DataMap();
				dataMap.put("rowNum", i);
				dataMap.put("sufrerPin", list.get(i).get("대상자 식별번호"));
				dataMap.put("sufrerNm", list.get(i).get("대상자 성명"));
				dataMap.put("tranPhone", list.get(i).get("전화번호"));
				dataMap.put("replTxt1", list.get(i).get("대체값1"));
				dataMap.put("replTxt2", list.get(i).get("대체값2"));
				dataMap.put("replTxt3", list.get(i).get("대체값3"));
				dataMap.put("replTxt4", list.get(i).get("대체값4"));
				dataMap.put("replTxt5", list.get(i).get("대체값5"));
				dataMap.put("regusr", loginUser.getString("webId"));
				tempList.add(dataMap);
			}
			String xmlValue = GridXmlBuilder.getCommonXmlString(gridConfigData, tempList, request, pageIndex,
					recordCountPerPage);
			return new ResponseEntity<String>(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(null, null, HttpStatus.BAD_REQUEST);
	}



	/* 엑셀 업로드 파일업로드(이관대상자) & 그리드 */
	@RequestMapping(value = "/rest/grid/transVictimListUploadGrid.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> transVictimListUploadGrid(
			@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam(value = "pageIndex", required = false, defaultValue = "0") long pageIndex,
			@RequestParam(value = "recordCountPerPage", required = false, defaultValue = "0") long recordCountPerPage,
			RequestResolver resolver, HttpSession session) {

		try {
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			FileDetail fileVO = (FileDetail) serviceGateway.getOne(new ServiceMethod("file.select", resolver.getMap()));
			List<DataMap> tempList = new ArrayList<DataMap>();

			System.out.println(fileVO.getFileStreCours() + fileVO.getStreFileNm());

			ExcelUploadUtil excelComponent = new ExcelUploadUtil();
			List<HashMap<String, Object>> list = excelComponent
					.excelProcess(fileVO.getFileStreCours() + fileVO.getStreFileNm());
			DataMap dataMap;
			for (int i = 0; i < list.size(); i++) {
				dataMap = new DataMap();
				dataMap.put("rowNum", i);
				dataMap.put("assignSe", list.get(i).get("할당구분"));
				dataMap.put("sufrerPin", list.get(i).get("대상자 식별번호"));
				dataMap.put("sufrerNm", list.get(i).get("대상자 성명"));
				dataMap.put("transFromOperCd", list.get(i).get("이관전 보건센터"));
				dataMap.put("transToOperCd", list.get(i).get("이관후 보건센터"));
				dataMap.put("transRm", list.get(i).get("이관사유"));
				dataMap.put("rm01", list.get(i).get("비고1"));
				dataMap.put("regusr", loginUser.getString("webId"));
				tempList.add(dataMap);
			}
			String xmlValue = GridXmlBuilder.getCommonXmlString(gridConfigData, tempList, request, pageIndex,
					recordCountPerPage);
			return new ResponseEntity<String>(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(null, null, HttpStatus.BAD_REQUEST);
	}

	/* 이관대상자 업로드 프로세스 */
	@RequestMapping("/rest/grid/transVictimListUploadProcess.do")
	@ResponseBody
	@Transactional
	public ResponseEntity<Map> transVictimListUploadProcess(RequestResolver resolver, HttpSession session)
			throws Exception {
		List<DataMap> excelList = JsonConverter.getObjectList(resolver.getString("excelList"), DataMap.class);
		DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
		int successCnt = 0;

		// update 결과 확인하기위해 serviceGateway.process 사용안하고, dao 직접 만들어서 사용
		List<DataMap> failList = new ArrayList<DataMap>();

		/*
		 * 1. 기존 데이터 operCd=이관이력, transSe=승인으로 update 2. 이관후병원으로 데이터 insert
		 */
		DataMap map = null;
		for (DataMap item : excelList) {

			System.out.println(item);

			int result = 0;

			// 대상자 가져오기
			DataMap param = new DataMap();
			param.put("operCd", item.getString("transFromOperCd")); // operCd: 이관전 담당기관으로 셋팅
			param.put("assignSe", item.getString("assignSe"));
			param.put("sufrerPin", item.getString("sufrerPin"));
			DataMap mnt = (DataMap) serviceGateway.getOne(new ServiceMethod("listYear.select", param));

			if (mnt == null) {
				mnt = new DataMap();
				mnt.put("victimSeq", "0");
			}

			// 기존 데이터 update(operCd=이관이력, transSe=승인)
			map = new DataMap();
			map.put("victimSeq", mnt.getString("victimSeq"));
			map.put("operCd", "이관이력");
			map.put("transSe", "승인");
			map.put("transConfirmUsr", loginUser.getString("webId"));
			map.put("transFromUsr", loginUser.getString("webId"));
			// 엑셀 데이터 셋팅
			map.put("transFromOperCd", item.getString("transFromOperCd"));
			map.put("transToOperCd", item.getString("transToOperCd"));
			map.put("transRm", item.getString("transRm"));
			map.put("rm01", item.getString("rm01"));
			map.put("updusr", loginUser.getString("webId"));
			//result += (int) serviceGatewayDao.update("listYear.updateTrans", map);

			// 업데이트 후 대상자 데이터 가져옴
			DataMap victim = (DataMap) serviceGateway.getOne(new ServiceMethod("listYear.select", map));

			if (victim != null) {
				// 제우스에서 날짜 HH24:MI:SS.F 형식으로 변환되서 뒤에 .F 잘라냄
				if (!EgovStringUtil.isEmpty(victim.getString("transFromDt"))) {
					String dt = victim.getString("transFromDt");
					int index = victim.getString("transFromDt").indexOf(".");
					if (index >= 0) {
						victim.put("transFromDt", dt.substring(0, index));
					}
				}
				if (!EgovStringUtil.isEmpty(victim.getString("transConfirmDt"))) {
					String dt = victim.getString("transConfirmDt");
					int index = victim.getString("transConfirmDt").indexOf(".");
					if (index >= 0) {
						victim.put("transConfirmDt", dt.substring(0, index));
					}
				}

				// 이관후 담당기관으로 새롭게 데이터 insert
				map = new DataMap();
				map.putAll(victim);
				map.remove("orgCd");
				map.put("operCd", item.getString("transToOperCd")); // operCd: 이관후 담당기관으로 셋팅
				//result += (int) serviceGatewayDao.insert("listYear.insert", map);
			}

			if (result == 2) {
				successCnt++;
			} else {
				failList.add(item);
			}
		}

		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		System.out.println("totalCnt: " + excelList.size());
		System.out.println("successCnt: " + successCnt);
		System.out.println("failList: " + failList);

		resolver.put("totalCnt", excelList.size());
		resolver.put("successCnt", successCnt);
		resolver.put("failList", failList);
		return new ResponseEntity<Map>(resolver.getMap(), null, HttpStatus.OK);
	}

	/* 엑셀 업로드 파일업로드(배정대상자) & 그리드 */
	@RequestMapping(value = "/rest/grid/assignVictimListUploadGrid.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> assignVictimListUploadGrid(
			@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam(value = "pageIndex", required = false, defaultValue = "0") long pageIndex,
			@RequestParam(value = "recordCountPerPage", required = false, defaultValue = "0") long recordCountPerPage,
			RequestResolver resolver, HttpSession session) {

		try {
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			FileDetail fileVO = (FileDetail) serviceGateway.getOne(new ServiceMethod("file.select", resolver.getMap()));
			List<DataMap> tempList = new ArrayList<DataMap>();

			System.out.println(fileVO.getFileStreCours() + fileVO.getStreFileNm());

			ExcelUploadUtil excelComponent = new ExcelUploadUtil();
			List<HashMap<String, Object>> list = excelComponent
					.excelProcess(fileVO.getFileStreCours() + fileVO.getStreFileNm());
			DataMap dataMap;
			for (int i = 0; i < list.size(); i++) {
				dataMap = new DataMap();
				dataMap.put("rowNum", i);
				dataMap.put("assignSe", list.get(i).get("할당구분"));
				dataMap.put("sufrerPin", list.get(i).get("대상자 식별번호"));
				dataMap.put("sufrerNm", list.get(i).get("대상자 성명"));
				dataMap.put("brthdy", list.get(i).get("생년월일"));
				dataMap.put("operCd", list.get(i).get("배정 보건센터"));
				dataMap.put("rm01", list.get(i).get("비고1"));
				tempList.add(dataMap);
			}
			String xmlValue = GridXmlBuilder.getCommonXmlString(gridConfigData, tempList, request, pageIndex,
					recordCountPerPage);
			return new ResponseEntity<String>(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(null, null, HttpStatus.BAD_REQUEST);
	}

	/* 배정대상자 업로드 프로세스 */
	@RequestMapping("/rest/grid/assignVictimListUploadProcess.do")
	@ResponseBody
	@Transactional
	public ResponseEntity<Map> assignVictimListUploadProcess(RequestResolver resolver, HttpSession session)
			throws Exception {

		List<DataMap> excelList = JsonConverter.getObjectList(resolver.getString("excelList"), DataMap.class);
		DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");

		int successCnt = 0;
		List<DataMap> failList = new ArrayList<DataMap>();

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
		String today = sdf.format(date);

		for (DataMap item : excelList) {
			int result = 0;

			// 이미 배정된 대상자가 있는지 검색
			DataMap param = new DataMap();
			param.put("operCd", item.getString("operCd"));
			param.put("assignSe", item.getString("assignSe"));
			param.put("sufrerPin", item.getString("sufrerPin"));
			DataMap assignVictim = (DataMap) serviceGateway.getOne(new ServiceMethod("listYear.select", param));

			if (assignVictim == null) { // 기존 배정된 대상자가 없을때만 새로 업로드

				// 식별번호와 생년월일을 가지고 전주기 데이터와 비교
				// 식별번호와 생년월일이 모두 전주기 데이터와 같을때만 insert 둘 중 하나라도 틀리면 반환
				DataMap victimInfo = (DataMap) serviceGateway
						.getOne(new ServiceMethod("victim.selectVictimInfo", item));

				if (victimInfo != null) {

					item.put("ctprvn", victimInfo.getString("ctprvn"));
					item.put("monitorPrio", "5");
					item.put("monitorSe", "대상");
					item.put("assignUsr", loginUser.getString("webId"));
					item.put("assignDe", today);
					item.put("regusr", loginUser.getString("webId"));
				//	result = (int) serviceGatewayDao.insert("listYear.insert", item);

					if (result > 0) {
						successCnt++;
					} else {
						item.put("failReason", "삽입오류(데이터 값 오류)");
						failList.add(item);
					}

				} else {
					item.put("failReason", "식별번호 또는 생년월일 불일치");
					failList.add(item);
				}
			} else {
				item.put("failReason", "이미 배정된 식별번호");
				failList.add(item);
			}
		}

		serviceGateway.getOne(new ServiceMethod("victim.spInsertSurveySn"));

		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		System.out.println("totalCnt: " + excelList.size());
		System.out.println("successCnt: " + successCnt);
		System.out.println("failList: " + failList);

		resolver.put("totalCnt", excelList.size());
		resolver.put("successCnt", successCnt);
		resolver.put("failList", failList);
		return new ResponseEntity<Map>(resolver.getMap(), null, HttpStatus.OK);

	}

	/* 엑셀 업로드 파일업로드(스케줄 예약일정) & 그리드 */
	@RequestMapping(value = "/rest/grid/schListUploadGrid.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> schListUploadGrid(@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam(value = "pageIndex", required = false, defaultValue = "0") long pageIndex,
			@RequestParam(value = "recordCountPerPage", required = false, defaultValue = "0") long recordCountPerPage,
			RequestResolver resolver, HttpSession session) {

		try {
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			FileDetail fileVO = (FileDetail) serviceGateway.getOne(new ServiceMethod("file.select", resolver.getMap()));
			List<DataMap> tempList = new ArrayList<DataMap>();

			System.out.println(fileVO.getFileStreCours() + fileVO.getStreFileNm());

			ExcelUploadUtil excelComponent = new ExcelUploadUtil();
			List<HashMap<String, Object>> list = excelComponent
					.excelProcess(fileVO.getFileStreCours() + fileVO.getStreFileNm());
			DataMap dataMap;
			for (int i = 0; i < list.size(); i++) {
				dataMap = new DataMap();
				dataMap.put("rowNum", i);
				dataMap.put("operCd", list.get(i).get("보건센터"));
				dataMap.put("assignSe", list.get(i).get("할당구분"));
				dataMap.put("sufrerPin", list.get(i).get("식별번호"));
				dataMap.put("sufrerNm", list.get(i).get("대상자 성명"));
				dataMap.put("scSe", list.get(i).get("스케줄 유형"));
				dataMap.put("scStartDe", list.get(i).get("시작일자"));
				dataMap.put("scStartTm", list.get(i).get("시작시간"));
				dataMap.put("scEndDe", list.get(i).get("종료일자"));
				dataMap.put("scEndTm", list.get(i).get("종료시간"));
				dataMap.put("scMngNm", list.get(i).get("담당자"));
				dataMap.put("scMemo", list.get(i).get("스케줄 메모"));
				dataMap.put("smsCheckAt", list.get(i).get("문자발송 선택여부"));
				dataMap.put("smsSe", list.get(i).get("상용구 코드"));
				dataMap.put("tranPhone", list.get(i).get("수신자 전화번호"));
				tempList.add(dataMap);
			}
			String xmlValue = GridXmlBuilder.getCommonXmlString(gridConfigData, tempList, request, pageIndex,
					recordCountPerPage);
			return new ResponseEntity<String>(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(null, null, HttpStatus.BAD_REQUEST);
	}


	/* 엑셀 업로드 파일업로드(대상자 세부현황) & 그리드 */
	@RequestMapping(value = "/rest/grid/mntListUploadGrid.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> mntListUploadGrid(@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam(value = "pageIndex", required = false, defaultValue = "0") long pageIndex,
			@RequestParam(value = "recordCountPerPage", required = false, defaultValue = "0") long recordCountPerPage,
			RequestResolver resolver, HttpSession session) {
		try {
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			FileDetail fileVO = (FileDetail) this.serviceGateway
					.getOne(new ServiceMethod("file.select", resolver.getMap()));
			List<DataMap> tempList = new ArrayList<DataMap>();
			System.out.println(String.valueOf(fileVO.getFileStreCours()) + fileVO.getStreFileNm());
			ExcelUploadUtil excelComponent = new ExcelUploadUtil();
			List<HashMap<String, Object>> list = excelComponent
					.excelProcess(String.valueOf(fileVO.getFileStreCours()) + fileVO.getStreFileNm());

			System.out.println(list);
			for (int i = 0; i < list.size(); i++) {
				DataMap dataMap = new DataMap();
				dataMap.put("sufrerPin", ((HashMap) list.get(i)).get("식별번호"));
				dataMap.put("assignSe", ((HashMap) list.get(i)).get("할당구분"));
				dataMap.put("operCd", ((HashMap) list.get(i)).get("보건센터"));

				System.out.println(dataMap);
				int result = 0;
				DataMap mnt = (DataMap) this.serviceGateway.getOne(new ServiceMethod("listYear.selectMnt", dataMap));
				if (mnt == null) {
					dataMap.put("insertPossibleAt", "업로드 가능");
				} else {
					dataMap.put("insertPossibleAt", "업로드 불가능");
				}
				dataMap.put("rowNum", Integer.valueOf(i));
				dataMap.put("operCd", list.get(i).get("보건센터"));
				dataMap.put("assignSe", list.get(i).get("할당구분"));
				dataMap.put("sufrerPin", list.get(i).get("식별번호"));
				dataMap.put("sufrerNm", list.get(i).get("대상자명"));
				dataMap.put("brthdy", list.get(i).get("생년월일"));
				dataMap.put("sexdstn", list.get(i).get("성별"));
				dataMap.put("adultSe", list.get(i).get("성인구분"));
				dataMap.put("healthExam1De", ((HashMap) list.get(i)).get("1차 검진일자"));
				dataMap.put("healthExam2De", ((HashMap) list.get(i)).get("2차 검진일자"));
				dataMap.put("healthExam3De", ((HashMap) list.get(i)).get("3차 검진일자"));
				dataMap.put("healthExam4De", ((HashMap) list.get(i)).get("4차 검진일자"));
				dataMap.put("healthExam5De", ((HashMap) list.get(i)).get("5차 검진일자"));
				dataMap.put("healthSurveyAt", ((HashMap) list.get(i)).get("설문참여 여부"));
				dataMap.put("healthSurvey1De", ((HashMap) list.get(i)).get("1차 설문일자"));
				dataMap.put("healthSurvey2De", ((HashMap) list.get(i)).get("2차 설문일자"));
				
				dataMap.put("testAt", ((HashMap) list.get(i)).get("검사 여부"));
				dataMap.put("treatExamAt", ((HashMap) list.get(i)).get("진료(면담) 여부"));
				dataMap.put("treatExamDe", ((HashMap) list.get(i)).get("진료(면담) 일자"));
				dataMap.put("treatTelAt", ((HashMap) list.get(i)).get("전화진료 여부"));
				dataMap.put("treatTelDe", ((HashMap) list.get(i)).get("전화진료 일자"));
				dataMap.put("treatTelTypes", ((HashMap) list.get(i)).get("전화진료유형"));
				dataMap.put("treatDenyAt", ((HashMap) list.get(i)).get("진료거부 여부"));
				dataMap.put("treatExamRm", ((HashMap) list.get(i)).get("진료 및 검사 비고"));
				dataMap.put("commonExamAt", ((HashMap) list.get(i)).get("공통검사 여부"));
				dataMap.put("commonExcept", ((HashMap) list.get(i)).get("공통검사 제외 항목"));
				dataMap.put("commonExamRm", ((HashMap) list.get(i)).get("공통검사 비고"));
				dataMap.put("customExams", ((HashMap) list.get(i)).get("맞춤형검사"));
				dataMap.put("customExamsRm", ((HashMap) list.get(i)).get("맞춤형검사 비고"));
				dataMap.put("addExams", ((HashMap) list.get(i)).get("추가검사"));
				dataMap.put("addTreat1", ((HashMap) list.get(i)).get("추가검사 기타1"));
				dataMap.put("addTreat2", ((HashMap) list.get(i)).get("추가검사 기타2"));
				dataMap.put("addTreat3", ((HashMap) list.get(i)).get("추가검사 기타3"));
				dataMap.put("addExamRm", ((HashMap) list.get(i)).get("추가검사 사유"));
				dataMap.put("otherExams", ((HashMap) list.get(i)).get("타과진료"));
				dataMap.put("otherTreat1", ((HashMap) list.get(i)).get("타과 진료 기타1"));
				dataMap.put("otherTreat2", ((HashMap) list.get(i)).get("타과 진료 기타2"));
				dataMap.put("otherTreat3", ((HashMap) list.get(i)).get("타과 진료 기타3"));
				dataMap.put("otherTreatRm", ((HashMap) list.get(i)).get("타과 진료 사유"));
				
				dataMap.put("examReview1De", ((HashMap) list.get(i)).get("결과 상담일1"));
				dataMap.put("examReview2De", ((HashMap) list.get(i)).get("결과 상담일2"));
				dataMap.put("examReview3De", ((HashMap) list.get(i)).get("결과 상담일3"));
				dataMap.put("examReviewRm", ((HashMap) list.get(i)).get("결과상담 비고"));
				dataMap.put("examReport1De", ((HashMap) list.get(i)).get("결과지 송부일자1"));
				dataMap.put("examReport2De", ((HashMap) list.get(i)).get("결과지 송부일자2"));
				dataMap.put("examReportRm", ((HashMap) list.get(i)).get("결과지 송부 비고"));
				
				dataMap.put("examReportRceptDe", ((HashMap) list.get(i)).get("결과지 수령일"));
				dataMap.put("examReportPostnum", ((HashMap) list.get(i)).get("등기번호"));
				
				dataMap.put("examDenialReasons", list.get(i).get("참여거부 사유"));
				dataMap.put("examDenialRm", list.get(i).get("참여거부 비고"));
				dataMap.put("examRm", ((HashMap) list.get(i)).get("추가 기록사항"));
				
				dataMap.put("regusr", loginUser.getString("webId"));
				tempList.add(dataMap);
			}
			String xmlValue = GridXmlBuilder.getCommonXmlString(gridConfigData, tempList, this.request, pageIndex,
					recordCountPerPage);
			return new ResponseEntity(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
			return new ResponseEntity(null, null, HttpStatus.BAD_REQUEST);
		}
	}

	/* 대상자 세부현황 업로드 프로세스 */
	@RequestMapping("/rest/grid/mntListUploadProcess.do")
	@ResponseBody
	@Transactional
	public ResponseEntity<Map> mntListUploadProcess(RequestResolver resolver, HttpSession session) throws Exception {
		List<DataMap> excelList = JsonConverter.getObjectList(resolver.getString("excelList"), DataMap.class);
		DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
		int successCnt = 0;

		// update 결과 확인하기위해 serviceGateway.process 사용안하고, dao 직접 만들어서 사용
		List<DataMap> failList = new ArrayList<DataMap>();

		for (DataMap item : excelList) {
			int result = 0;

			// 데이터 존재여부 확인
			DataMap mnt = (DataMap) serviceGateway.getOne(new ServiceMethod("listYear.selectMnt", item));
			if (mnt == null) {
				item.put("regusr", loginUser.getString("webId"));
			//	result = (int) serviceGatewayDao.insert("listYear.insertMnt", item);
			}

			if (result > 0) {
				successCnt++;
			} else {
				failList.add(item);
			}
		}

		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		System.out.println("totalCnt: " + excelList.size());
		System.out.println("successCnt: " + successCnt);
		System.out.println("failList: " + failList);

		resolver.put("totalCnt", excelList.size());
		resolver.put("successCnt", successCnt);
		resolver.put("failList", failList);
		return new ResponseEntity<Map>(resolver.getMap(), null, HttpStatus.OK);
	}

	/* 엑셀 업로드 파일업로드(검진항목단위) & 그리드 */
	@RequestMapping(value = "/rest/grid/itemConfigUploadGrid.do", produces = "text/xml;charset=utf-8")
	public ResponseEntity<String> itemConfigUploadGrid(@RequestParam(value = "gridConfigData") String gridConfigData,
			@RequestParam(value = "pageIndex", required = false, defaultValue = "0") long pageIndex,
			@RequestParam(value = "recordCountPerPage", required = false, defaultValue = "0") long recordCountPerPage,
			RequestResolver resolver, HttpSession session) {

		try {
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			FileDetail fileVO = (FileDetail) serviceGateway.getOne(new ServiceMethod("file.select", resolver.getMap()));
			List<DataMap> tempList = new ArrayList<DataMap>();

			System.out.println(fileVO.getFileStreCours() + fileVO.getStreFileNm());

			ExcelUploadUtil excelComponent = new ExcelUploadUtil();
			List<HashMap<String, Object>> list = excelComponent
					.excelProcess(fileVO.getFileStreCours() + fileVO.getStreFileNm());
			DataMap dataMap;
			for (int i = 0; i < list.size(); i++) {
				if(list.get(i).get("운영기관") == null)
					break;
				dataMap = new DataMap();
				dataMap.put("rowNum", i);
				dataMap.put("configVer", list.get(i).get("버전"));
				dataMap.put("operCd", list.get(i).get("운영기관"));
				dataMap.put("configSe", list.get(i).get("구분"));
				dataMap.put("mainGroup", list.get(i).get("대분류"));
				dataMap.put("mainGroupSort", list.get(i).get("대분류 순서"));
				dataMap.put("itemGroup", list.get(i).get("항목그룹"));
				dataMap.put("itemGroupSort", list.get(i).get("항목그룹 순서"));
				dataMap.put("item", list.get(i).get("항목"));
				dataMap.put("itemSort", list.get(i).get("항목 순서"));
				dataMap.put("itemNameEn", list.get(i).get("항목명(ENG)"));
				dataMap.put("itemNameKo", list.get(i).get("항목명(KOR)"));
				dataMap.put("itemSe", list.get(i).get("검사구분"));
				dataMap.put("itemType", list.get(i).get("데이터형식"));
				dataMap.put("itemSelectString", list.get(i).get("선택아이템 문자열"));
				dataMap.put("itemUnit", list.get(i).get("단위"));
				dataMap.put("rangeMax", list.get(i).get("최대"));
				dataMap.put("rangeMin", list.get(i).get("최소"));
				dataMap.put("rangeIncrement", list.get(i).get("증감"));
				dataMap.put("rangeDecimal", list.get(i).get("소수점자리수"));
				dataMap.put("rangeManLow", list.get(i).get("성인남자 미만값"));
				dataMap.put("rangeManHigh", list.get(i).get("성인남자 초과값"));
				dataMap.put("rangeWomanLow", list.get(i).get("성인여자 미만값"));
				dataMap.put("rangeWomanHigh", list.get(i).get("성인여자 초과값"));
				dataMap.put("rangeBoyLow", list.get(i).get("소아남자 미만값"));
				dataMap.put("rangeBoyHigh", list.get(i).get("소아남자 초과값"));
				dataMap.put("rangeGirlLow", list.get(i).get("소아여자 미만값"));
				dataMap.put("rangeGirlHigh", list.get(i).get("소아여자 초과값"));
				dataMap.put("rm", list.get(i).get("비고"));
				dataMap.put("viewAt", list.get(i).get("게시여부"));
				dataMap.put("regusr", loginUser.getString("webId"));
				tempList.add(dataMap);
			}
			String xmlValue = GridXmlBuilder.getCommonXmlString(gridConfigData, tempList, request, pageIndex,
					recordCountPerPage);
			return new ResponseEntity<String>(xmlValue, null, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(null, null, HttpStatus.BAD_REQUEST);
	}


	
}