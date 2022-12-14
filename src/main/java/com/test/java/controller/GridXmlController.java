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
			if (resolver.getMap().getString("operCd").equals("?????????")) {
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
			
			// POI ??????????????? ??????, xlsx ???????????? ??????
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
			if(resolver.getMap().getString("operCd").equals("?????????")) {
				resolver.remove("operCd");
			}
			resolver.put("itemCountPerPage", 0);
			resolver.put("pageIndex", 1);
			
			JSONArray gridConfigDataList = new JSONArray(gridConfigData); //?????? ?????????
			ObjectMapper mapper = new ObjectMapper();
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			
			if(methodName.equals("selectSurveyQnExcel")) {
				JSONArray surveyQnList = new JSONArray(surveyQnData); //???????????? ?????????
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
					
					resolver.put("workbookIndex", j); //?????? ???????????? dataList??? ???????????? ?????????
					List<DataMap> dataList = this.getDataList(className, methodName, gridConfigData, resolver.getMap());
					// POI ??????????????? ??????, xlsx ???????????? ??????
					//if(dataList != null && dataList.size() > 0) {//?????? ????????? ????????? ?????? ???????????? ?????? ??????
						workbook = excelDownComponent.makeExcelWorkbook(workbook, dataList, gridCellList, resolver.getMap());
						j++;					
					//}
				}
			}else {
				for(int i=0, j=0; i < gridConfigDataList.length(); i++) {
					List<DataMap> gridCellList = mapper.readValue(gridConfigDataList.get(i).toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, DataMap.class));
					resolver.put("sheetName", j); //?????? ??? ????????? ?????????
					resolver.put("workbookIndex", j); //?????? ???????????? dataList??? ???????????? ?????????
					List<DataMap> dataList = this.getDataList(className, methodName, gridConfigData, resolver.getMap());
					if(dataList != null && dataList.size() > 0) {//?????? ????????? ????????? ?????? ???????????? ?????? ??????
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

	/* ?????? ????????? ??????????????? ?????? ?????? ?????? */
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

	/* ?????? ????????? ???????????????(????????????) & ????????? */
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
				dataMap.put("operCd", list.get(i).get("????????????"));
				dataMap.put("assignSe", list.get(i).get("????????????"));
				dataMap.put("sufrerPin", list.get(i).get("????????? ????????????"));
				dataMap.put("sufrerNm", list.get(i).get("????????? ??????(??????)"));
				dataMap.put("hsptlId", list.get(i).get("??????????????????"));
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

	/* ???????????? ????????? ???????????? */
	@RequestMapping("/rest/grid/hsptlIdUploadProcess.do")
	@ResponseBody
	@Transactional
	public ResponseEntity<Map> hsptlIdUploadProcess(RequestResolver resolver, HttpSession session) throws Exception {

		List<DataMap> excelList = JsonConverter.getObjectList(resolver.getString("submitData"), DataMap.class);
		DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");

		int successCnt = 0;

		// update ?????? ?????????????????? serviceGateway.process ???????????????, dao ?????? ???????????? ??????

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

	/* ?????? ????????? ???????????????(SMS ?????? ??????) & ????????? */
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
				dataMap.put("sufrerPin", list.get(i).get("????????? ????????????"));
				dataMap.put("sufrerNm", list.get(i).get("????????? ??????"));
				dataMap.put("tranPhone", list.get(i).get("????????????"));
				dataMap.put("replTxt1", list.get(i).get("?????????1"));
				dataMap.put("replTxt2", list.get(i).get("?????????2"));
				dataMap.put("replTxt3", list.get(i).get("?????????3"));
				dataMap.put("replTxt4", list.get(i).get("?????????4"));
				dataMap.put("replTxt5", list.get(i).get("?????????5"));
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



	/* ?????? ????????? ???????????????(???????????????) & ????????? */
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
				dataMap.put("assignSe", list.get(i).get("????????????"));
				dataMap.put("sufrerPin", list.get(i).get("????????? ????????????"));
				dataMap.put("sufrerNm", list.get(i).get("????????? ??????"));
				dataMap.put("transFromOperCd", list.get(i).get("????????? ????????????"));
				dataMap.put("transToOperCd", list.get(i).get("????????? ????????????"));
				dataMap.put("transRm", list.get(i).get("????????????"));
				dataMap.put("rm01", list.get(i).get("??????1"));
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

	/* ??????????????? ????????? ???????????? */
	@RequestMapping("/rest/grid/transVictimListUploadProcess.do")
	@ResponseBody
	@Transactional
	public ResponseEntity<Map> transVictimListUploadProcess(RequestResolver resolver, HttpSession session)
			throws Exception {
		List<DataMap> excelList = JsonConverter.getObjectList(resolver.getString("excelList"), DataMap.class);
		DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
		int successCnt = 0;

		// update ?????? ?????????????????? serviceGateway.process ???????????????, dao ?????? ???????????? ??????
		List<DataMap> failList = new ArrayList<DataMap>();

		/*
		 * 1. ?????? ????????? operCd=????????????, transSe=???????????? update 2. ????????????????????? ????????? insert
		 */
		DataMap map = null;
		for (DataMap item : excelList) {

			System.out.println(item);

			int result = 0;

			// ????????? ????????????
			DataMap param = new DataMap();
			param.put("operCd", item.getString("transFromOperCd")); // operCd: ????????? ?????????????????? ??????
			param.put("assignSe", item.getString("assignSe"));
			param.put("sufrerPin", item.getString("sufrerPin"));
			DataMap mnt = (DataMap) serviceGateway.getOne(new ServiceMethod("listYear.select", param));

			if (mnt == null) {
				mnt = new DataMap();
				mnt.put("victimSeq", "0");
			}

			// ?????? ????????? update(operCd=????????????, transSe=??????)
			map = new DataMap();
			map.put("victimSeq", mnt.getString("victimSeq"));
			map.put("operCd", "????????????");
			map.put("transSe", "??????");
			map.put("transConfirmUsr", loginUser.getString("webId"));
			map.put("transFromUsr", loginUser.getString("webId"));
			// ?????? ????????? ??????
			map.put("transFromOperCd", item.getString("transFromOperCd"));
			map.put("transToOperCd", item.getString("transToOperCd"));
			map.put("transRm", item.getString("transRm"));
			map.put("rm01", item.getString("rm01"));
			map.put("updusr", loginUser.getString("webId"));
			//result += (int) serviceGatewayDao.update("listYear.updateTrans", map);

			// ???????????? ??? ????????? ????????? ?????????
			DataMap victim = (DataMap) serviceGateway.getOne(new ServiceMethod("listYear.select", map));

			if (victim != null) {
				// ??????????????? ?????? HH24:MI:SS.F ???????????? ???????????? ?????? .F ?????????
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

				// ????????? ?????????????????? ????????? ????????? insert
				map = new DataMap();
				map.putAll(victim);
				map.remove("orgCd");
				map.put("operCd", item.getString("transToOperCd")); // operCd: ????????? ?????????????????? ??????
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

	/* ?????? ????????? ???????????????(???????????????) & ????????? */
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
				dataMap.put("assignSe", list.get(i).get("????????????"));
				dataMap.put("sufrerPin", list.get(i).get("????????? ????????????"));
				dataMap.put("sufrerNm", list.get(i).get("????????? ??????"));
				dataMap.put("brthdy", list.get(i).get("????????????"));
				dataMap.put("operCd", list.get(i).get("?????? ????????????"));
				dataMap.put("rm01", list.get(i).get("??????1"));
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

	/* ??????????????? ????????? ???????????? */
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

			// ?????? ????????? ???????????? ????????? ??????
			DataMap param = new DataMap();
			param.put("operCd", item.getString("operCd"));
			param.put("assignSe", item.getString("assignSe"));
			param.put("sufrerPin", item.getString("sufrerPin"));
			DataMap assignVictim = (DataMap) serviceGateway.getOne(new ServiceMethod("listYear.select", param));

			if (assignVictim == null) { // ?????? ????????? ???????????? ???????????? ?????? ?????????

				// ??????????????? ??????????????? ????????? ????????? ???????????? ??????
				// ??????????????? ??????????????? ?????? ????????? ???????????? ???????????? insert ??? ??? ???????????? ????????? ??????
				DataMap victimInfo = (DataMap) serviceGateway
						.getOne(new ServiceMethod("victim.selectVictimInfo", item));

				if (victimInfo != null) {

					item.put("ctprvn", victimInfo.getString("ctprvn"));
					item.put("monitorPrio", "5");
					item.put("monitorSe", "??????");
					item.put("assignUsr", loginUser.getString("webId"));
					item.put("assignDe", today);
					item.put("regusr", loginUser.getString("webId"));
				//	result = (int) serviceGatewayDao.insert("listYear.insert", item);

					if (result > 0) {
						successCnt++;
					} else {
						item.put("failReason", "????????????(????????? ??? ??????)");
						failList.add(item);
					}

				} else {
					item.put("failReason", "???????????? ?????? ???????????? ?????????");
					failList.add(item);
				}
			} else {
				item.put("failReason", "?????? ????????? ????????????");
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

	/* ?????? ????????? ???????????????(????????? ????????????) & ????????? */
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
				dataMap.put("operCd", list.get(i).get("????????????"));
				dataMap.put("assignSe", list.get(i).get("????????????"));
				dataMap.put("sufrerPin", list.get(i).get("????????????"));
				dataMap.put("sufrerNm", list.get(i).get("????????? ??????"));
				dataMap.put("scSe", list.get(i).get("????????? ??????"));
				dataMap.put("scStartDe", list.get(i).get("????????????"));
				dataMap.put("scStartTm", list.get(i).get("????????????"));
				dataMap.put("scEndDe", list.get(i).get("????????????"));
				dataMap.put("scEndTm", list.get(i).get("????????????"));
				dataMap.put("scMngNm", list.get(i).get("?????????"));
				dataMap.put("scMemo", list.get(i).get("????????? ??????"));
				dataMap.put("smsCheckAt", list.get(i).get("???????????? ????????????"));
				dataMap.put("smsSe", list.get(i).get("????????? ??????"));
				dataMap.put("tranPhone", list.get(i).get("????????? ????????????"));
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


	/* ?????? ????????? ???????????????(????????? ????????????) & ????????? */
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
				dataMap.put("sufrerPin", ((HashMap) list.get(i)).get("????????????"));
				dataMap.put("assignSe", ((HashMap) list.get(i)).get("????????????"));
				dataMap.put("operCd", ((HashMap) list.get(i)).get("????????????"));

				System.out.println(dataMap);
				int result = 0;
				DataMap mnt = (DataMap) this.serviceGateway.getOne(new ServiceMethod("listYear.selectMnt", dataMap));
				if (mnt == null) {
					dataMap.put("insertPossibleAt", "????????? ??????");
				} else {
					dataMap.put("insertPossibleAt", "????????? ?????????");
				}
				dataMap.put("rowNum", Integer.valueOf(i));
				dataMap.put("operCd", list.get(i).get("????????????"));
				dataMap.put("assignSe", list.get(i).get("????????????"));
				dataMap.put("sufrerPin", list.get(i).get("????????????"));
				dataMap.put("sufrerNm", list.get(i).get("????????????"));
				dataMap.put("brthdy", list.get(i).get("????????????"));
				dataMap.put("sexdstn", list.get(i).get("??????"));
				dataMap.put("adultSe", list.get(i).get("????????????"));
				dataMap.put("healthExam1De", ((HashMap) list.get(i)).get("1??? ????????????"));
				dataMap.put("healthExam2De", ((HashMap) list.get(i)).get("2??? ????????????"));
				dataMap.put("healthExam3De", ((HashMap) list.get(i)).get("3??? ????????????"));
				dataMap.put("healthExam4De", ((HashMap) list.get(i)).get("4??? ????????????"));
				dataMap.put("healthExam5De", ((HashMap) list.get(i)).get("5??? ????????????"));
				dataMap.put("healthSurveyAt", ((HashMap) list.get(i)).get("???????????? ??????"));
				dataMap.put("healthSurvey1De", ((HashMap) list.get(i)).get("1??? ????????????"));
				dataMap.put("healthSurvey2De", ((HashMap) list.get(i)).get("2??? ????????????"));
				
				dataMap.put("testAt", ((HashMap) list.get(i)).get("?????? ??????"));
				dataMap.put("treatExamAt", ((HashMap) list.get(i)).get("??????(??????) ??????"));
				dataMap.put("treatExamDe", ((HashMap) list.get(i)).get("??????(??????) ??????"));
				dataMap.put("treatTelAt", ((HashMap) list.get(i)).get("???????????? ??????"));
				dataMap.put("treatTelDe", ((HashMap) list.get(i)).get("???????????? ??????"));
				dataMap.put("treatTelTypes", ((HashMap) list.get(i)).get("??????????????????"));
				dataMap.put("treatDenyAt", ((HashMap) list.get(i)).get("???????????? ??????"));
				dataMap.put("treatExamRm", ((HashMap) list.get(i)).get("?????? ??? ?????? ??????"));
				dataMap.put("commonExamAt", ((HashMap) list.get(i)).get("???????????? ??????"));
				dataMap.put("commonExcept", ((HashMap) list.get(i)).get("???????????? ?????? ??????"));
				dataMap.put("commonExamRm", ((HashMap) list.get(i)).get("???????????? ??????"));
				dataMap.put("customExams", ((HashMap) list.get(i)).get("???????????????"));
				dataMap.put("customExamsRm", ((HashMap) list.get(i)).get("??????????????? ??????"));
				dataMap.put("addExams", ((HashMap) list.get(i)).get("????????????"));
				dataMap.put("addTreat1", ((HashMap) list.get(i)).get("???????????? ??????1"));
				dataMap.put("addTreat2", ((HashMap) list.get(i)).get("???????????? ??????2"));
				dataMap.put("addTreat3", ((HashMap) list.get(i)).get("???????????? ??????3"));
				dataMap.put("addExamRm", ((HashMap) list.get(i)).get("???????????? ??????"));
				dataMap.put("otherExams", ((HashMap) list.get(i)).get("????????????"));
				dataMap.put("otherTreat1", ((HashMap) list.get(i)).get("?????? ?????? ??????1"));
				dataMap.put("otherTreat2", ((HashMap) list.get(i)).get("?????? ?????? ??????2"));
				dataMap.put("otherTreat3", ((HashMap) list.get(i)).get("?????? ?????? ??????3"));
				dataMap.put("otherTreatRm", ((HashMap) list.get(i)).get("?????? ?????? ??????"));
				
				dataMap.put("examReview1De", ((HashMap) list.get(i)).get("?????? ?????????1"));
				dataMap.put("examReview2De", ((HashMap) list.get(i)).get("?????? ?????????2"));
				dataMap.put("examReview3De", ((HashMap) list.get(i)).get("?????? ?????????3"));
				dataMap.put("examReviewRm", ((HashMap) list.get(i)).get("???????????? ??????"));
				dataMap.put("examReport1De", ((HashMap) list.get(i)).get("????????? ????????????1"));
				dataMap.put("examReport2De", ((HashMap) list.get(i)).get("????????? ????????????2"));
				dataMap.put("examReportRm", ((HashMap) list.get(i)).get("????????? ?????? ??????"));
				
				dataMap.put("examReportRceptDe", ((HashMap) list.get(i)).get("????????? ?????????"));
				dataMap.put("examReportPostnum", ((HashMap) list.get(i)).get("????????????"));
				
				dataMap.put("examDenialReasons", list.get(i).get("???????????? ??????"));
				dataMap.put("examDenialRm", list.get(i).get("???????????? ??????"));
				dataMap.put("examRm", ((HashMap) list.get(i)).get("?????? ????????????"));
				
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

	/* ????????? ???????????? ????????? ???????????? */
	@RequestMapping("/rest/grid/mntListUploadProcess.do")
	@ResponseBody
	@Transactional
	public ResponseEntity<Map> mntListUploadProcess(RequestResolver resolver, HttpSession session) throws Exception {
		List<DataMap> excelList = JsonConverter.getObjectList(resolver.getString("excelList"), DataMap.class);
		DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
		int successCnt = 0;

		// update ?????? ?????????????????? serviceGateway.process ???????????????, dao ?????? ???????????? ??????
		List<DataMap> failList = new ArrayList<DataMap>();

		for (DataMap item : excelList) {
			int result = 0;

			// ????????? ???????????? ??????
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

	/* ?????? ????????? ???????????????(??????????????????) & ????????? */
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
				if(list.get(i).get("????????????") == null)
					break;
				dataMap = new DataMap();
				dataMap.put("rowNum", i);
				dataMap.put("configVer", list.get(i).get("??????"));
				dataMap.put("operCd", list.get(i).get("????????????"));
				dataMap.put("configSe", list.get(i).get("??????"));
				dataMap.put("mainGroup", list.get(i).get("?????????"));
				dataMap.put("mainGroupSort", list.get(i).get("????????? ??????"));
				dataMap.put("itemGroup", list.get(i).get("????????????"));
				dataMap.put("itemGroupSort", list.get(i).get("???????????? ??????"));
				dataMap.put("item", list.get(i).get("??????"));
				dataMap.put("itemSort", list.get(i).get("?????? ??????"));
				dataMap.put("itemNameEn", list.get(i).get("?????????(ENG)"));
				dataMap.put("itemNameKo", list.get(i).get("?????????(KOR)"));
				dataMap.put("itemSe", list.get(i).get("????????????"));
				dataMap.put("itemType", list.get(i).get("???????????????"));
				dataMap.put("itemSelectString", list.get(i).get("??????????????? ?????????"));
				dataMap.put("itemUnit", list.get(i).get("??????"));
				dataMap.put("rangeMax", list.get(i).get("??????"));
				dataMap.put("rangeMin", list.get(i).get("??????"));
				dataMap.put("rangeIncrement", list.get(i).get("??????"));
				dataMap.put("rangeDecimal", list.get(i).get("??????????????????"));
				dataMap.put("rangeManLow", list.get(i).get("???????????? ?????????"));
				dataMap.put("rangeManHigh", list.get(i).get("???????????? ?????????"));
				dataMap.put("rangeWomanLow", list.get(i).get("???????????? ?????????"));
				dataMap.put("rangeWomanHigh", list.get(i).get("???????????? ?????????"));
				dataMap.put("rangeBoyLow", list.get(i).get("???????????? ?????????"));
				dataMap.put("rangeBoyHigh", list.get(i).get("???????????? ?????????"));
				dataMap.put("rangeGirlLow", list.get(i).get("???????????? ?????????"));
				dataMap.put("rangeGirlHigh", list.get(i).get("???????????? ?????????"));
				dataMap.put("rm", list.get(i).get("??????"));
				dataMap.put("viewAt", list.get(i).get("????????????"));
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