package com.test.java.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.test.java.common.DataMap;
import com.test.java.common.RequestResolver;
import com.test.java.service.ContextUtil;
import com.test.java.service.FileUploadService;
import com.test.java.service.ServiceGateway;
import com.test.java.service.ServiceMethod;
import com.test.java.util.EgovNumberUtil;


@Controller
public class FileController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	@Value("#{config['file.uploadPath']}")
	protected String fileUploadPath;
	
	@Value("#{config['excel.templatePath']}")
	protected String excelTemplatePath;
	
	@Autowired
	protected FileUploadService fileUploadService;
	
	@Resource(name="serviceGateway")
	private ServiceGateway serviceGateway;

	@Autowired
	ContextUtil contextUtil;
	@RequestMapping(value = "/common/file/uploadProcess.do", produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> fileUploadProcess(RequestResolver resolver, MultipartHttpServletRequest multipartFile, HttpServletRequest request, HttpSession session) throws Exception{
		String jsonData = "";
		DataMap result = new DataMap();
		try {	
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			resolver.put("regusr", loginUser.getString("webId"));
			
			DataMap queryMap = resolver.getMap();
			List<DataMap> files = fileUploadService.uploadFile(multipartFile.getFileMap(), fileUploadPath);		
			if(resolver.getString("fileId").equals("") || resolver.getString("fileId").equals("0")) {
				serviceGateway.process(new ServiceMethod("file.insert", queryMap));
			}
			for(DataMap m : files) {
				m.putAll(queryMap);
				serviceGateway.process(new ServiceMethod("file.insertDetail", m));
				DataMap logMap = setFileLogMap(m, "COM_FILE", "업로드", "", request);
				serviceGateway.process(new ServiceMethod("accesLog.insertFileLog", logMap));
			}
			result.put("status", "success");
			result.put("fileList", files);
			result.put("fileId", queryMap.getString("fileId"));
		}catch(Exception ex) {
			ex.printStackTrace();
			result.put("status", "error");
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		try {
			jsonData = writer.writeValueAsString(result);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return new ResponseEntity<String>(jsonData, null, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/common/file/removeProcess.do", produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> removeProcess(RequestResolver resolver, HttpSession session) throws Exception{
		String jsonData = "";
		DataMap result = new DataMap();
		try {	
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			resolver.put("updusr", loginUser.getString("webId"));
			serviceGateway.process(new ServiceMethod("file.delete", resolver.getMap()));
			result.put("status", "success");			
		}catch(Exception ex) {
			ex.printStackTrace();
			result.put("status", "error");
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		try {
			jsonData = writer.writeValueAsString(result);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return new ResponseEntity<String>(jsonData, null, HttpStatus.OK);
	}
	
	@RequestMapping("/common/file/{method}fileDownload.do")
	public void commonfileDownload(@PathVariable String method, RequestResolver resolver, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (resolver.getLong("fileId") == 0) {
			return;
		}
		
		DataMap vo = null;
		if(method == null || method.equals("")) {
			vo = (DataMap)serviceGateway.getOne(new ServiceMethod("file.selectMap", resolver.getMap()));
		} else { // 전주기 파일 다운(병원공유 자료실)
			vo = (DataMap)serviceGateway.getOne(new ServiceMethod("file.selectMapHumid", resolver.getMap()));
		}
		File file = null;
		FileInputStream fis = null;
		
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		String fileMimetype = null;
		
		file = new File(vo.getString("fileStreCours"), vo.getString("streFileNm"));				
		long fSize = (long) file.length();
		String browser = getBrowser(request);
		
		System.out.println("###############fSize: " + fSize) ;
		System.out.println("###############fileStreCours: " + vo.getString("fileStreCours")) ;
		System.out.println("###############streFileNm: " + vo.getString("streFileNm")) ;
		System.out.println("###############browser: " + browser) ;
		
		
		if ("Safari".equals(browser)) {
			fileMimetype = "application/octet-stream";
		} else {
			fileMimetype = "application/x-msdownload";
		}
		
		if (fSize > 0) {
			response.setContentType(fileMimetype);
			System.out.println("mimetype--------->" + fileMimetype);
			System.out.println("getBrowser(request)--------->" + getBrowser(request));
			System.out.println("orignlFileNm--------->" + vo.getString("orignlFileNm"));
			setDisposition(vo.getString("orignlFileNm"), request, response);
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
			
			if (fSize <= Integer.MAX_VALUE)
				response.setContentLength((int)fSize);
			else
				response.addHeader("Content-Length", Long.toString(fSize));
			
			try {
				fis = new FileInputStream(file);
				System.out.println("file.getPath------> " + file.getPath());
				System.out.println("file.getAbsolutePath------> "+file.getAbsolutePath());
				System.out.println("file.getName------> "+file.getName());
				
				in = new BufferedInputStream(fis);
				out = new BufferedOutputStream(response.getOutputStream());
				FileCopyUtils.copy(in, out);
				out.flush();
				System.out.println("파일다운로드 정상작동");
			} catch (Exception ex) {
				logger.debug("IGNORED: " + ex.getMessage());
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());                   
					}
				}
				if(out != null) {
					try {
						out.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());                   
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());
					}
				}
			}
		} else {
			System.out.println("파일이 없습니다.");
			logger.debug(fileMimetype + " fileType is null.");
			response.sendRedirect("/common/noFile.do");
		}
		
		// 다운로드 횟수 증가 
		serviceGateway.process(new ServiceMethod("file.updateAtachDnCnt", vo));
		
		// 파일 로그 남기기 
		DataMap logMap = setFileLogMap(vo, "COM_FILE", "다운로드", "", request);
		serviceGateway.process(new ServiceMethod("accesLog.insertFileLog", logMap));
	}
	
	@RequestMapping(value = "/common/file/{method}list.do", produces = "application/json;charset=UTF-8")	
	public ResponseEntity<String> list(@PathVariable String method, RequestResolver resolver) {
		String jsonData = "";
		DataMap result = new DataMap();
		try {
			List<DataMap> fileList = null;
			if(method == null || method.equals("")) {
				
				
				System.out.println( resolver.getMap());
				
				
				fileList = serviceGateway.getList(new ServiceMethod("file.selectList", resolver.getMap()));
				
				
				
			} else { // 전주기 파일 목록 조회(병원공유 자료실)
				fileList = serviceGateway.getList(new ServiceMethod("file.selectListHumid", resolver.getMap()));
			}
			result.put("status", "success");
			result.put("fileList", fileList);
		} catch (Exception ex) {
			ex.printStackTrace();
			result.put("status", "error");
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		try {
			jsonData = writer.writeValueAsString(result);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(jsonData, null, HttpStatus.OK);
	}
	
	// 업로드양식 다운로드 
	@RequestMapping("/common/file/fileForm.do")
	public void fileFromDownload(RequestResolver resolver, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		File file = null;
		FileInputStream fis = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		String fileMimetype = null;
		
		System.out.println(excelTemplatePath + resolver.getString("streNm"));
		file = new File(excelTemplatePath + resolver.getString("streNm"));	
		
		long fSize = (long) file.length();
		String browser = getBrowser(request);
		
		System.out.println("###############fSize: " + fSize) ;
		System.out.println("###############browser: " + browser) ;
		
		
		if ("Safari".equals(browser)) {
			fileMimetype = "application/octet-stream";
		} else {
			fileMimetype = "application/x-msdownload";
		}
		
		if (fSize > 0) {
			response.setContentType(fileMimetype);
			System.out.println("mimetype--------->" + fileMimetype);
			System.out.println("getBrowser(request)--------->" + getBrowser(request));
			setDisposition(resolver.getString("orignlNm"), request, response);
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
					
			if (fSize <= Integer.MAX_VALUE)
			  response.setContentLength((int)fSize);
			else
			  response.addHeader("Content-Length", Long.toString(fSize));
			
			try {
				fis = new FileInputStream(file);
				System.out.println("file.getPath------> " + file.getPath());
				System.out.println("file.getAbsolutePath------> "+file.getAbsolutePath());
				System.out.println("file.getName------> "+file.getName());
			
				in = new BufferedInputStream(fis);
				out = new BufferedOutputStream(response.getOutputStream());
				
				FileCopyUtils.copy(in, out);
				out.flush();
				System.out.println("파일다운로드 정상작동");
				
			} catch (Exception ex) {
				logger.debug("IGNORED: " + ex.getMessage());
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());                   
					}
				}
				if(out != null) {
					try {
						out.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());                   
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());
					}
				}
			}
			
		} else {
			System.out.println("파일이 없습니다.");
			logger.debug(fileMimetype + " fileType is null.");
			response.sendRedirect("/common/noFile.do");
		}
		
	}
	
	// 스캔파일 아이디 조회 및 생성 
	@RequestMapping("/monitor/fileId/{method}.do")
	@ResponseBody
	public ResponseEntity<Object> fileIdProcess(RequestResolver resolver, @PathVariable String method, HttpSession session,
			ModelMap model) throws Exception{
		
		Object result = null;
		DataMap queryMap = resolver.getMap();
		if (method.equals("selectMonitorFileId")) {   // 스캔파일 파일 아이디 가져오기 
			result = serviceGateway.getOne(new ServiceMethod("file.selectMonitorFileId", queryMap));
			if(result == null) {
				//result = new DataMap().put("fileId", "0");
				result = resolver.getMap();
			}
		} else if (method.equals("selectMonitor")) {   
			result = serviceGateway.getOne(new ServiceMethod("file.selectMonitor", queryMap));
			if(result == null) {
				result = new DataMap();
			}
		}
		
		
		return new ResponseEntity<Object>(result, null, HttpStatus.OK);
	}
	

	// 스캔 파일 업로드 
	@RequestMapping(value = "/monitor/file/uploadProcess.do", produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> scanFileUploadProcess(RequestResolver resolver, MultipartHttpServletRequest multipartFile, HttpSession session, HttpServletRequest request) throws Exception{
		String jsonData = "";
		
		DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
		resolver.put("regusr", loginUser.getString("webId"));
		
		DataMap result = new DataMap();
		int fileSn = 0;
		try {	
			DataMap queryMap = resolver.getMap();
			List<DataMap> files = fileUploadService.uploadFile(multipartFile.getFileMap(), resolver.getString("folderRoot")+resolver.getString("folderStre"));		
			if(resolver.getString("fileId").equals("") || resolver.getString("fileId").equals("0")) {
				serviceGateway.process(new ServiceMethod("file.insertMonitorFileId", queryMap));
			}
			for(DataMap m : files) {
				m.putAll(queryMap);
				serviceGateway.process(new ServiceMethod("file.insertMonitorDetail", m));
				DataMap logMap = setFileLogMap(m, "ORG_DOC", "업로드", "", request);
				serviceGateway.process(new ServiceMethod("accesLog.insertFileLog", logMap));
			}
			result.put("status", "success");
			result.put("fileList", files);
			result.put("fileId", queryMap.getString("fileId"));
		}catch(Exception ex) {
			ex.printStackTrace();
			result.put("status", "error");
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		try {
			jsonData = writer.writeValueAsString(result);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return new ResponseEntity<String>(jsonData, null, HttpStatus.OK);
	}
	
	// 스캔 파일 삭제  
	@RequestMapping(value = "/monitor/file/removeProcess.do", produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> scanFileRemoveProcess(RequestResolver resolver, HttpSession session) throws Exception{
		String jsonData = "";
		DataMap result = new DataMap();
		try {	
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			resolver.put("updusr", loginUser.getString("webId"));	
			serviceGateway.process(new ServiceMethod("file.deleteFileMonitor", resolver.getMap()));
			result.put("status", "success");			
		}catch(Exception ex) {
			ex.printStackTrace();
			result.put("status", "error");
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		try {
			jsonData = writer.writeValueAsString(result);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return new ResponseEntity<String>(jsonData, null, HttpStatus.OK);
	}
	
	// 스캔 파일 리스트  
	@RequestMapping(value = "/monitor/file/list.do", produces = "application/json;charset=UTF-8")	
	public ResponseEntity<String> scanFilelist(RequestResolver resolver) {
		String jsonData = "";
		DataMap result = new DataMap();
		try {
			List<DataMap> fileList = null;
			if(resolver.get("target") != null && resolver.get("target") != "" && resolver.get("target").equals("regFile")) { // 모니터링 피해자별 등록파일 조회 리스트 (regFile.jsp)
				fileList = serviceGateway.getList(new ServiceMethod("file.selectMonitorList2", resolver.getMap()));
				this.setFileSize(fileList);
				Long totalCnt = (Long) serviceGateway.getOne(new ServiceMethod("file.selectMonitorListCount", resolver.getMap()));
				result.put("resultList", fileList);
				result.put("totalCnt", totalCnt);
			}else {  // 파일 컴포넌트 리스트 22
				fileList = serviceGateway.getList(new ServiceMethod("file.selectMonitorList", resolver.getMap()));
				this.setFileSize(fileList);
				result.put("status", "success");
				result.put("fileList", fileList);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			result.put("status", "error");
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		try {
			jsonData = writer.writeValueAsString(result);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return new ResponseEntity<String>(jsonData, null, HttpStatus.OK);
	}
	
	// 스캔 파일 다운로드 
	@RequestMapping("/monitor/file/fileDownload.do")
	public void fileDownload(RequestResolver resolver, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (resolver.getLong("fileId") == 0) {
			return;
		}
		
		if(!resolver.getMap().getString("type").equals("") && resolver.getMap().getString("type").equals("exp")) { // 환경노출조사 설문
			resolver.put("operCd", "KEITI");
		}
		DataMap vo = (DataMap)serviceGateway.getOne(new ServiceMethod("file.selectMonitor", resolver.getMap()));
		File file = null;
		FileInputStream fis = null;

		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		String fileMimetype = null;
		
		System.out.println(vo.getString("folderRoot") + vo.getString("folderStre") + vo.getString("streFileNm"));
		file = new File(vo.getString("folderRoot") + vo.getString("folderStre"), vo.getString("streFileNm"));				
		long fSize = (long) file.length();
		String browser = getBrowser(request);
		
		System.out.println("###############fSize: " + fSize) ;
		System.out.println("###############browser: " + browser) ;
		

		if ("Safari".equals(browser)) {
			fileMimetype = "application/octet-stream";
		} else {
			fileMimetype = "application/x-msdownload";
		}
		
		if (fSize > 0) {
			response.setContentType(fileMimetype);
			System.out.println("mimetype--------->" + fileMimetype);
			System.out.println("getBrowser(request)--------->" + getBrowser(request));
			System.out.println("orignlFileNm--------->" + vo.getString("orignlFileNm"));
			setDisposition(vo.getString("orignlFileNm"), request, response);
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
					
					if (fSize <= Integer.MAX_VALUE)
					  response.setContentLength((int)fSize);
					else
					  response.addHeader("Content-Length", Long.toString(fSize));
			try {
				fis = new FileInputStream(file);
				System.out.println("file.getPath------> " + file.getPath());
				System.out.println("file.getAbsolutePath------> "+file.getAbsolutePath());
				System.out.println("file.getName------> "+file.getName());
			
				in = new BufferedInputStream(fis);
				out = new BufferedOutputStream(response.getOutputStream());
				FileCopyUtils.copy(in, out);
				out.flush();
				System.out.println("파일다운로드 정상작동");
			} catch (Exception ex) {
				logger.debug("IGNORED: " + ex.getMessage());
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());                   
					}
				}
				if(out != null) {
					try {
						out.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());                   
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());
					}
				}
			}
		} else {
			System.out.println("파일이 없습니다.");
			logger.debug(fileMimetype + " fileType is null.");
			response.sendRedirect("/common/noFile.do");
		}
		
		// 다운로드 횟수 증가 
		serviceGateway.process(new ServiceMethod("file.updateDnCnt", vo));
		
		// 파일 로그 남기기 
		DataMap logMap = setFileLogMap(vo, "ORG_DOC", "다운로드", "", request);
		serviceGateway.process(new ServiceMethod("accesLog.insertFileLog", logMap));
	}
	
	// 스캔파일 압축 다운로드: Zip파일 생성 
	@RequestMapping("/monitor/file/generateZip.do")
	public Object generateZip(RequestResolver resolver, HttpSession session, ModelMap model) throws Exception{
		Object result = null;
		DataMap queryMap = resolver.getMap();
		
		try {
			String fileId = queryMap.getString("fileIds");
			String[] fileIds = fileId.split("/");
			
			DataMap param = new DataMap();
			param.put("fileIds", fileIds);
			List<DataMap> fileList = serviceGateway.getList(new ServiceMethod("file.selectMonitorMulti", param));
			
			if(fileList == null){
				fileList = new ArrayList<DataMap>();
			}
			
			DataMap file = fileUploadService.generateZipFile(fileIds, queryMap.getString("streFileNms").split("/"), fileUploadPath, fileList, queryMap);
			if(file == null){
				file = new DataMap();
			}
			result = file;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<Object>(result, null, HttpStatus.OK);
	}
	
	// 스캔파일 압축 다운로드: Zip파일 다운로드
	@RequestMapping("/monitor/file/downloadZip.do")
	public void downloadZip(HttpServletRequest request, HttpServletResponse response, RequestResolver resolver) throws Exception{
		DataMap queryMap = resolver.getMap();
		
		File file = new File(queryMap.getString("folderRoot") + queryMap.getString("folderStre"), queryMap.getString("streFileNm"));
		FileInputStream fis = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		String fileMimetype = null;
		
		long fSize = (long) file.length();
		String browser = getBrowser(request);
		
		System.out.println("###############fSize: " + fSize);
		System.out.println("###############browser: " + browser);
		
		if ("Safari".equals(browser)) {
			fileMimetype = "application/octet-stream";
		} else {
			fileMimetype = "application/x-msdownload";
		}
		
		if (fSize > 0) {
			response.setContentType(fileMimetype);
			System.out.println("mimetype--------->" + fileMimetype);
			System.out.println("getBrowser(request)--------->" + getBrowser(request));
			setDisposition(queryMap.getString("streFileNm"), request, response);
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
					
			if (fSize <= Integer.MAX_VALUE)
			  response.setContentLength((int)fSize);
			else
			  response.addHeader("Content-Length", Long.toString(fSize));
			
			try {
				fis = new FileInputStream(file);
				System.out.println("file.getPath------> " + file.getPath());
				System.out.println("file.getAbsolutePath------> "+file.getAbsolutePath());
				System.out.println("file.getName------> "+file.getName());
			
				in = new BufferedInputStream(fis);
				out = new BufferedOutputStream(response.getOutputStream());
				
				FileCopyUtils.copy(in, out);
				out.flush();
				System.out.println("파일다운로드 정상작동");
				
			} catch (Exception ex) {
				logger.debug("IGNORED: " + ex.getMessage());
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());                   
					}
				}
				if(out != null) {
					try {
						out.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());                   
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (Exception ignore) {
						logger.debug("IGNORE: " + ignore.getMessage());
					}
				}
			}
			
		} else {
			System.out.println("###############파일이 없습니다.");
			logger.debug(fileMimetype + " fileType is null.");
			response.sendRedirect("/common/noFile.do");
		}
		
	}
	
	// 파일 다운로드 disposition  설정 
	private void setDisposition(String filename, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String browser = getBrowser(request);

		String dispositionPrefix = "attachment; filename=";
		String encodedFilename = URLEncoder.encode(filename, "UTF-8");

		if ("Opera".equals(browser)) {
			response.setContentType("application/octet-stream;charset=UTF-8");
		}

		if (browser.equals("Safari")) {
			encodedFilename = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
		}

		response.setHeader("Content-Disposition", dispositionPrefix + "\"" + encodedFilename.replaceAll("\\+", "%20") + "\"");

	}
	
	// 사용자 브라우저 확인 메소드 
	private String getBrowser(HttpServletRequest request) {
		String header = request.getHeader("User-Agent");

		if (header.indexOf("MSIE") > -1 || header.indexOf("Trident") > -1) {
			return "MSIE";
		} else if (header.indexOf("Chrome") > -1) {
			return "Chrome";
		} else if (header.indexOf("Opera") > -1) {
			return "Opera";
		} else if (header.indexOf("Safari") > -1) {
			return "Safari";
		}
		
		return "Firefox";
	}
	
	// 파일 로그기록 남기기 위한  logMap 생성 메소드 (public static) 
	public static DataMap setFileLogMap(DataMap fileVo, String fileSe, String accessAction, String accessRm, HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null) ip = request.getRemoteAddr();
		String locale = request.getLocale().getDisplayName();
		String wbsr = request.getHeader("User-Agent");
		
		DataMap logMap = new DataMap();
		try{
			DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
			DataMap sufrerUser = (DataMap) session.getAttribute("SUFRER_USER"); // 홈페이지 대상자여부
			logMap.put("operCd", loginUser != null ? loginUser.getString("operCd") : sufrerUser.getString("operCd"));
			logMap.put("accesSe", "건강모니터링");
			logMap.put("fileSe", fileSe);
			logMap.put("fileId", fileVo.getString("fileId"));
			logMap.put("fileSn", fileVo.getString("fileSn"));
			logMap.put("orignlFileNm",  fileVo.getString("orignlFileNm"));
			logMap.put("accesIp", ip);
			logMap.put("locale", locale);
			logMap.put("accesWbsr", wbsr);
			logMap.put("accesLoginId", loginUser != null ? loginUser.getString("webId") : sufrerUser.getString("webId"));
			logMap.put("accesKorNm", loginUser != null ? loginUser.getString("korNm") : sufrerUser.getString("korNm"));
			logMap.put("accesAction", accessAction);
			logMap.put("accesRm", accessRm);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return logMap;
	}
	
	//엑셀 파일 다운로드 로그  Map  생성 메소드 (public static) 
		public static DataMap setExcelFileLogMap(String fileNm, String fileSe, String accessAction, String accessRm, HttpServletRequest request) {
			
			HttpSession session = request.getSession();
			String ip = request.getHeader("X-FORWARDED-FOR");
	        if (ip == null) ip = request.getRemoteAddr();
			String locale = request.getLocale().getDisplayName();
			String wbsr = request.getHeader("User-Agent");
			
			DataMap logMap = new DataMap();
			try{
				DataMap loginUser = (DataMap) session.getAttribute("LOGIN_USER");
				logMap.put("operCd", loginUser.getString("operCd"));
				logMap.put("accesSe", "건강모니터링");
				logMap.put("fileId", "0");
				logMap.put("fileSn", "0");
				logMap.put("fileSe", fileSe);
				logMap.put("orignlFileNm", fileNm);
				logMap.put("accesIp", ip);
				logMap.put("locale", locale);
				logMap.put("accesWbsr", wbsr);
				logMap.put("accesLoginId", loginUser.getString("webId"));
				logMap.put("accesKorNm", loginUser.getString("korNm"));
				logMap.put("accesAction", accessAction);
				logMap.put("accesRm", accessRm);
				
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
			return logMap;
		}
	
	// 엑셀 파일 다운로드를 위한 휴대폰 인증 메소드 
		
	@RequestMapping("/api/file/mbtlnoCheck.do")
	public Object mbtlnoCheck(RequestResolver resolver, HttpServletRequest request, HttpSession session, ModelMap model)
			throws Exception {
	
	//	String ip = request.getRemoteAddr();
		DataMap result = new DataMap();
		Object objectResult = null;
		
		if(resolver.getString("authNum").equals("") == false) { // 인증번호가 있으면, 					
		
			if(session.getAttribute("MBTLNO_AUTH_NO") != null) {
				String randomNo = session.getAttribute("MBTLNO_AUTH_NO").toString();
				if(randomNo.equals(resolver.getString("authNum"))) {
					//인증 통과
					result.put("resultCode", "AUTH_HP_MATCHED");
					session.removeAttribute("MBTLNO_AUTH_NO");
					
					return new ResponseEntity<Object>(result, null, HttpStatus.OK);
					
				}else {
					result.put("resultCode", "AUTH_HP_NOTMATCHED");
					return new ResponseEntity<Object>(result, null, HttpStatus.OK);
				}
			}else {
				result.put("resultCode", "AUTH_HP_EXPIRED");
				return new ResponseEntity<Object>(result, null, HttpStatus.OK);
			}
			
		}else {
				int randomNo = EgovNumberUtil.getRandomNum(123456,987654);
				//공통코드에서 대표발신번호 조회
				String callback = "";
				List<DataMap> callbackList = contextUtil.getCodeList("SMS_CALLBACK");
				for(DataMap m : callbackList) {
					if(m.getString("codeId").equals("CALLBACK_01")) {
						callback = m.getString("codeNm");
						break;
					}
				}
				System.out.println("###################### MBTLNO_AUTH_NO : " + randomNo);
				session.setAttribute("MBTLNO_AUTH_NO", randomNo);
				result.put("resultCode", "AUTH_HP");
				return new ResponseEntity<Object>(result, null, HttpStatus.OK);
		
		}
		
	}
	
		
		
		
	@RequestMapping("/api/file/{method}.do")
	public Object mber(RequestResolver resolver, @PathVariable String method, HttpSession session, ModelMap model)
			throws Exception {
		DataMap result = new DataMap();
		DataMap queryMap = resolver.getMap();
		if (method.equals("selectFileCtgryList")) { // column 조회
			List<DataMap> resultList = serviceGateway.getList(new ServiceMethod("file.selectFileCtgryList", queryMap));
			result.put("resultList", resultList);
		}else if (method.equals("selectFileCtgry2List")) {
			List<DataMap> resultList = serviceGateway.getList(new ServiceMethod("file.selectFileCtgry2List", queryMap));
			result.put("resultList", resultList);
		}else if (method.equals("selectAtachFileCtgryList")) {
			List<DataMap> resultList = serviceGateway.getList(new ServiceMethod("file.selectAtachFileCtgryList", queryMap));
			result.put("resultList", resultList);
		}
		return new ResponseEntity<Object>(result, null, HttpStatus.OK);
	}
	
	private void setFileSize(List<DataMap> dataList) {
		for(DataMap m : dataList) {
			System.out.println(m.getString("streFileNm") + m.getString("fileSize") + " : file size check" );
			if(m.getString("fileSize").equals("")) {
				this.setFileSize(m);
			}			
		}
	}
	
	private void setFileSize(DataMap vo) {
		File f = new File(vo.getString("folderRoot") + vo.getString("folderStre"), vo.getString("streFileNm"));
		if(f.exists()) {
			vo.put("fileSize", f.length());
		}else {
			System.out.println(vo.getString("folderRoot") + vo.getString("folderStre") + vo.getString("streFileNm") + " is not exists");
		}
	}
}
