package com.test.java.util;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.test.java.common.DataMap;
import com.test.java.controller.FileController;
import com.test.java.service.ServiceGateway;
import com.test.java.service.ServiceMethod;



@Component
public class ExcelDownloadUtil extends AbstractView{

	@Resource(name = "serviceGateway")
	private ServiceGateway serviceGateway;
	
	@Autowired FileController fileController;
	
    /**
     * 리스트를 간단한 엑셀 워크북 객체로 생성
     * @param workbook, list
     * @return 해당 워크북에 시트별 리스트 생성
     */
	
	public SXSSFWorkbook makeExcelWorkbook(SXSSFWorkbook workbook,List<DataMap> dataList, List<DataMap> gridCellList, DataMap resolver) {
		 // 시트 생성
        Sheet sheet = workbook.createSheet();
        sheet.setDefaultColumnWidth((short)16);
        sheet.setDefaultRowHeight((short)450);
        workbook.setSheetName(resolver.getInt("workbookIndex"), resolver.getString("sheetName"));
        // 행 생성
        Row headerRow = sheet.createRow(0);
        int rowIndex = 1; //바디행 번호
        Row bodyRow = sheet.createRow(rowIndex++);
        // 셀 생성
        Cell headerCell = null;
        Cell bodyCell = null;
        
        
        // 스타일 
        Font hf = workbook.createFont(); // 헤더 폰트 
        Font bf = workbook.createFont(); // 바디 폰트
        
        hf.setFontHeightInPoints((short)10); // 폰트 사이즈 
        hf.setFontName("Arial");
        hf.setBoldweight(Font.BOLDWEIGHT_BOLD);
        
        bf.setFontHeightInPoints((short)10);
        bf.setFontName("Arial");
        
        
        CellStyle headerStyle = workbook.createCellStyle(); // 헤더 스타일 
        CellStyle bodyStyle = workbook.createCellStyle(); // 바디 스타일
        
        headerStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        headerStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        //배경색
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        
        headerStyle.setFont(hf);
        
        bodyStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        bodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        bodyStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        bodyStyle.setFont(bf);

        
        
        // 셀에 들어갈 값 리스트 생성
        List<DataMap> valueList = makeValueList(dataList, gridCellList);
    	for(int i=0; i<valueList.size(); i++) {
    		DataMap map = valueList.get(i);
    		int cellIndex = 0; // 셀번호
    		
    		for(Object key: map.keySet()) {
    			if(i == 0) { // 헤더 
    				headerCell = headerRow.createCell(cellIndex++);
    				headerCell.setCellValue(map.getString(key));
    				headerCell.setCellStyle(headerStyle);
    			} else { // 바디
    				bodyCell = bodyRow.createCell(cellIndex++); 
    				if(map.getString(key) != null && !map.getString(key).equals("")  ) {
    					bodyCell.setCellValue(map.getString(key));
    				}
    				
    				bodyCell.setCellStyle(bodyStyle);
    			}
    			
    		}
    		
    		if(i != 0) {
    			bodyRow = sheet.createRow(rowIndex++); // 행 생성
    		}
    	}
    	
    	// 열 너비 지정
    	for(int i=0; i<gridCellList.size(); i++) {
    		DataMap map = gridCellList.get(i);
    		if(map.containsKey("width")) {
    			sheet.setColumnWidth(i, Integer.parseInt(map.getString("width")) * 30);
    		}
    	}
		return workbook;
	}
	
	/**
     * 리스트를 간단한 엑셀 워크북 객체로 생성
     * @param list
     * @return 생성된 워크북
     */
    public SXSSFWorkbook makeExcelWorkbook(List<DataMap> dataList, List<DataMap> gridCellList) {
    	
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        
        // 시트 생성
        Sheet sheet = workbook.createSheet();
        sheet.setDefaultColumnWidth((short)16);
        sheet.setDefaultRowHeight((short)450);
        
        // 행 생성
        Row headerRow = sheet.createRow(0);
        int rowIndex = 1; //바디행 번호
        Row bodyRow = sheet.createRow(rowIndex++);
        // 셀 생성
        Cell headerCell = null;
        Cell bodyCell = null;
        
        
        // 스타일 
        Font hf = workbook.createFont(); // 헤더 폰트 
        Font bf = workbook.createFont(); // 바디 폰트
        
        hf.setFontHeightInPoints((short)10); // 폰트 사이즈 
        hf.setFontName("Arial");
        hf.setBoldweight(Font.BOLDWEIGHT_BOLD);
        
        bf.setFontHeightInPoints((short)10);
        bf.setFontName("Arial");
        
        
        CellStyle headerStyle = workbook.createCellStyle(); // 헤더 스타일 
        CellStyle bodyStyle = workbook.createCellStyle(); // 바디 스타일
        
        headerStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        headerStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        //배경색
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        
        headerStyle.setFont(hf);
        
        bodyStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        bodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        bodyStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        bodyStyle.setFont(bf);

        
        
        // 셀에 들어갈 값 리스트 생성
        List<DataMap> valueList = makeValueList(dataList, gridCellList);
    	for(int i=0; i<valueList.size(); i++) {
    		DataMap map = valueList.get(i);
    		int cellIndex = 0; // 셀번호
    		
    		for(Object key: map.keySet()) {
    			if(i == 0) { // 헤더 
    				headerCell = headerRow.createCell(cellIndex++);
    				headerCell.setCellValue(map.getString(key));
    				headerCell.setCellStyle(headerStyle);
    			} else { // 바디
    				bodyCell = bodyRow.createCell(cellIndex++); 
    				if(map.getString(key) != null && !map.getString(key).equals("")  ) {
    					bodyCell.setCellValue(map.getString(key));
    				}	
    				bodyCell.setCellStyle(bodyStyle);
    			}
    		}
    		
    		if(i != 0) {
    			bodyRow = sheet.createRow(rowIndex++); // 행 생성
    		}
    	}
    	
    	// 열 너비 지정
    	for(int i=0; i<gridCellList.size(); i++) {
    		DataMap map = gridCellList.get(i);
    		if(map.containsKey("width")) {
    			sheet.setColumnWidth(i, Integer.parseInt(map.getString("width")) * 30);
    		}
    	}
    	
        return workbook;
    }
    
    /**
     * 생성한 엑셀 워크북을 컨트롤레에서 받게해줄 메소드
     * @param list
     * @return
     */
    public SXSSFWorkbook excelFileDownloadProcess(List<DataMap> dataList, List<DataMap> gridCellList) {
        return this.makeExcelWorkbook(dataList, gridCellList);
    }
    
    public List<DataMap> makeValueList(List<DataMap> dataList, List<DataMap> gridCellList) {
    	// dataList의 키가 gridCellList columnName 값과 같으면 wbList에 추가 
    	List<DataMap> valueList = new ArrayList<DataMap>();
    	DataMap header = new DataMap(); 
    	DataMap map = null;
    	
    	if(dataList.size() > 0) {
    		
    		for(int i =1 ; i <=dataList.size(); i++ ) {    		
    			dataList.get(i-1).put(0, "rowNum", i);
    		}
    		DataMap gridRownum = new DataMap();
    		gridRownum.put("columnName", "rowNum");
    		gridRownum.put("header", "순번");
    		gridCellList.add(0, gridRownum);
    		
    		
    		for(DataMap data: dataList) {
    			
    			map = new DataMap();
    			for(DataMap grid: gridCellList) {
    				
    				String key = grid.getString("columnName");
    				if(data.containsKey(key) && !grid.containsKey("hidden")) { 
    					if(valueList.isEmpty()) { // 헤더 셀 셋팅 
    						header.put(key, grid.getString("header"));
    					} 
    					
    					// sorting, format 설정
    					if(grid.containsKey("sorting") && 
    							(grid.getString("sorting").equals("date") || grid.getString("sorting").equals("datestr")) ) {
    						String sDate = "";
    						try{
    							if(grid.getString("sorting").equals("datestr")){
    								sDate = EgovDateUtil.formatDate(data.getString(key), "-");
    							}else{
    								sDate = EgovDateUtil.formatDatePattern((Date)data.get(key), grid.getString("format"));
    							}									
    						}catch(Exception ex){									
    						}
    						map.put(key, sDate);
    					} else {
    						map.put(key, data.getString(key));
    					}
    					
    					if(grid.containsKey("secretFormat")) {
    						map.put(key,  grid.getString("secretFormat"));
    					}
    				}
    			}
    			if(valueList.isEmpty()) {
    				valueList.add(header);
    			} 
    			valueList.add(map);
    		}
    		
    	} else { // 값 없는 경우 헤더만 추가 
    		for(DataMap grid: gridCellList) {
				String key = grid.getString("columnName");
				if(valueList.isEmpty()) { // 헤더 셀 셋팅 
					header.put(key, grid.getString("header"));
				} 
			}
			valueList.add(header);
    	}
    	
    	return valueList;
    }
    
    
    public List<DataMap> makeSurveyValueList(List<DataMap> dataList, List<DataMap> gridCellList) {
    	// dataList의 키가 gridCellList columnName 값과 같으면 wbList에 추가 
    	List<DataMap> valueList = new ArrayList<DataMap>();
    	DataMap header = new DataMap(); 
    	DataMap map = null;
    	
    	if(dataList.size() > 0) {
    		
    		for(int i =1 ; i <=dataList.size(); i++ ) {    		
    			dataList.get(i-1).put(0, "rowNum", i);
    		}
    		DataMap gridRownum = new DataMap();
    		gridRownum.put("columnName", "rowNum");
    		gridRownum.put("header", "순번");
    		gridCellList.add(0, gridRownum);
    		
    		
    		for(DataMap data: dataList) {
    			
    			map = new DataMap();
    			for(DataMap grid: gridCellList) {
    				
    				String key = grid.getString("columnName");
    				if(!grid.containsKey("hidden")) { 
    					if(valueList.isEmpty()) { // 헤더 셀 셋팅 
    						header.put(key, grid.getString("header"));
    					} 
    					
    					// sorting, format 설정
    					if(grid.containsKey("sorting") && 
    							(grid.getString("sorting").equals("date") || grid.getString("sorting").equals("datestr")) ) {
    						String sDate = "";
    						try{
    							if(grid.getString("sorting").equals("datestr")){
    								sDate = EgovDateUtil.formatDate(data.getString(key), "-");
    							}else{
    								sDate = EgovDateUtil.formatDatePattern((Date)data.get(key), grid.getString("format"));
    							}									
    						}catch(Exception ex){									
    						}
    						map.put(key, sDate);
    					} else {
    						map.put(key, data.getString(key));
    					}
    					
    					if(grid.containsKey("secretFormat")) {
    						map.put(key,  grid.getString("secretFormat"));
    					}
    				}
    			}
    			if(valueList.isEmpty()) {
    				valueList.add(header);
    			} 
    			valueList.add(map);
    		}
    		
    	} else { // 값 없는 경우 헤더만 추가 
    		for(DataMap grid: gridCellList) {
				String key = grid.getString("columnName");
				if(valueList.isEmpty()) { // 헤더 셀 셋팅 
					header.put(key, grid.getString("header"));
				} 
			}
			valueList.add(header);
    	}
    	
    	return valueList;
    }
    
    public void renderMergedOutputModel2(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response, String mbtlno)
            throws Exception {
        
        String workbookName = (String) model.get("workbookName");
        String encodedFilename = URLEncoder.encode(workbookName,"UTF-8");
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedFilename + ".xlsx");
        response.setHeader("Content-Transfer-Encoding", "binary");
        
        OutputStream os = null;
        SXSSFWorkbook workbook = (SXSSFWorkbook) model.get("workbook");
        
        try {
        	os = response.getOutputStream();
        	workbook.write(os);
        	
        	DataMap logMap = fileController.setExcelFileLogMap(workbookName + ".xlsx", "EXCEL_FILE", "다운로드", mbtlno, request);
			System.out.println("logMap: " + logMap);
        	
        	System.out.println(serviceGateway);
        	serviceGateway.process(new ServiceMethod("accesLog.insertFileLog", logMap));
			
        	
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	if(os != null) {
        		try {
        			os.close();
        		} catch (Exception e) {
                    e.printStackTrace();
                }
        	}
        }
    }

	@Override
	public void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	
	
	public SXSSFWorkbook makeSurveyExcelWorkbook(SXSSFWorkbook workbook,List<DataMap> dataList, List<DataMap> gridCellList, DataMap resolver) {
		 // 시트 생성
       Sheet sheet = workbook.createSheet();
       sheet.setDefaultColumnWidth((short)16);
       sheet.setDefaultRowHeight((short)450);
       workbook.setSheetName(resolver.getInt("workbookIndex"), resolver.getString("sheetName"));
       // 행 생성
       Row headerRow = sheet.createRow(0);
       int rowIndex = 1; //바디행 번호
       Row bodyRow = sheet.createRow(rowIndex++);
       // 셀 생성
       Cell headerCell = null;
       Cell bodyCell = null;
       
       
       // 스타일 
       Font hf = workbook.createFont(); // 헤더 폰트 
       Font bf = workbook.createFont(); // 바디 폰트
       
       hf.setFontHeightInPoints((short)10); // 폰트 사이즈 
       hf.setFontName("Arial");
       hf.setBoldweight(Font.BOLDWEIGHT_BOLD);
       
       bf.setFontHeightInPoints((short)10);
       bf.setFontName("Arial");
       
       
       CellStyle headerStyle = workbook.createCellStyle(); // 헤더 스타일 
       CellStyle bodyStyle = workbook.createCellStyle(); // 바디 스타일
       
       headerStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
       headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
       headerStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
       headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
       headerStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
       headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
       headerStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
       headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
       headerStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
       headerStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
       //배경색
       headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
       headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
       
       headerStyle.setFont(hf);
       
       bodyStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
       bodyStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
       bodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
       bodyStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
       bodyStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
       bodyStyle.setFont(bf);

       
       
       // 셀에 들어갈 값 리스트 생성
       List<DataMap> valueList = makeSurveyValueList(dataList, gridCellList);
       DataMap headerMap = valueList.get(0); // 
       /*
       System.out.println("@@@@@@@@@@@@@@@dataList: " + dataList);
       System.out.println("@@@@@@@@@@@@@@@gridCellList: " + gridCellList);
       System.out.println("@@@@@@@@@@@@@@@valueList: " + valueList);
       System.out.println("@@@@@@@@@@@@headerMap: " + headerMap);
       */
   	for(int i=0; i<valueList.size(); i++) {
   		int cellIndex = 0; // 셀번호
   		
		DataMap valueMap = valueList.get(i); // 
   		for(Object key: headerMap.keySet()) {
   			
   			if(i == 0) { // 헤더 
   				headerCell = headerRow.createCell(cellIndex++);
   				headerCell.setCellValue(headerMap.getString(key));
   				headerCell.setCellStyle(headerStyle);
   			} else { // 바디
   				bodyCell = bodyRow.createCell(cellIndex++); 
   				if(valueMap.getString(key) != null && !valueMap.getString(key).equals("")  ) {
   					bodyCell.setCellValue(valueMap.getString(key));
   				}
   				bodyCell.setCellStyle(bodyStyle);
   			}
   			
   		}
   		
   		if(i != 0) {
   			bodyRow = sheet.createRow(rowIndex++); // 행 생성
   		}
   	}
   	
   	// 열 너비 지정
   	for(int i=0; i<gridCellList.size(); i++) {
   		DataMap map = gridCellList.get(i);
   		if(map.containsKey("width")) {
   			sheet.setColumnWidth(i, Integer.parseInt(map.getString("width")) * 30);
   		}
   	}
		return workbook;
	}
	
	
	
	
	
}
