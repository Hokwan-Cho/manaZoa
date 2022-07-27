package com.test.java.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Component;



public class ExcelUploadUtil {

	public List<HashMap<String, Object>> excelProcess(String filePath) throws IOException {

		List<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>(); // 최종 맵
		XSSFRow row;
		XSSFCell cell;

		try {
			// load
			FileInputStream file = new FileInputStream(new File(filePath));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			// sheet수 취득
			int sheetCn = workbook.getNumberOfSheets();
			//System.out.println("sheet수 : " + sheetCn);

			for (int cn = 0; cn < sheetCn; cn++) {
				//System.out.println("취득하는 sheet 이름 : " + workbook.getSheetName(cn));
				//System.out.println(workbook.getSheetName(cn) + " sheet 데이터 취득 시작");
				// 0번째 sheet 정보 취득
				XSSFSheet sheet = workbook.getSheetAt(cn);

				// 취득된 sheet에서 rows수 취득
				int rows = sheet.getPhysicalNumberOfRows();
				//System.out.println(workbook.getSheetName(cn) + " sheet의 row수 : " + rows);
				// 취득된 row에서 취득대상 cell수 취득
				int cells = sheet.getRow(cn).getPhysicalNumberOfCells(); //
				//System.out.println(workbook.getSheetName(cn) + " sheet의 row에 취득대상 cell수 : " + cells);
				
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				DecimalFormat df = new DecimalFormat();
				
				String HeaderName[] = new String[cells];

				for (int r = 0; r < rows; r++) {
					row = sheet.getRow(r); // row 가져오기
					HashMap<String, Object> map = new HashMap<String, Object>(); // 행 데이터 삽입
					if (row != null) {
						for (int c = 0; c < cells; c++) {
							cell = row.getCell(c);
							if (cell != null) {
						
								String value = null;
								switch (cell.getCellType()) {
								case XSSFCell.CELL_TYPE_FORMULA:   // 엑셀 형식이 수식일때, 
									if(!(cell.toString()=="") ){
    									if (evaluator.evaluateFormulaCell(cell) == XSSFCell.CELL_TYPE_NUMERIC) {
    											double fddata = cell.getNumericCellValue();         
    											value = df.format(fddata);
    									} else if (evaluator.evaluateFormulaCell(cell) == XSSFCell.CELL_TYPE_STRING) {
    										value = cell.getStringCellValue();
    									} else if (evaluator.evaluateFormulaCell(cell) == XSSFCell.CELL_TYPE_BOOLEAN) {
    										boolean fbdata = cell.getBooleanCellValue();         
    										value = String.valueOf(fbdata);         
    									} else {
    										value = EgovStringUtil.nullToString(cell.getCellFormula());
    									}
    								} 
									break;
								case XSSFCell.CELL_TYPE_NUMERIC:
									if (HSSFDateUtil.isCellDateFormatted(cell)) {
										SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
										value = formatter.format(cell.getDateCellValue());
									} else {
										double ddata = cell.getNumericCellValue();
										value = EgovStringUtil.nullToString(df.format(ddata).replace(",",""));
									}
									break;
								case XSSFCell.CELL_TYPE_STRING:
									value = ""+ EgovStringUtil.nullToString(cell.getStringCellValue());
									break;
								case XSSFCell.CELL_TYPE_BLANK:
									value = "";
									break;
								case XSSFCell.CELL_TYPE_ERROR:
									value = "" + cell.getErrorCellValue();
									break;
								default:
									value = EgovStringUtil.nullToString(value);
								}
								if (r == 0) {
									//HeaderName[c] = value.split(" ")[0];
									HeaderName[c] = value;
									
								} else {
									map.put(HeaderName[c], value);
								}
								//System.out.print(value + "\t");
							} else {
								//System.out.print("[null]\t");
							}
						} // for(c) 문
						if (r > 0) {
							int valueCheck = 0;
							for (int c = 0; c < cells; c++) {
								if (map.get(HeaderName[c]) != null
										&& map.get(HeaderName[c]) != "") {
									valueCheck++;
								}
							}
							if (valueCheck == 0) {
								break;
							}
							mapList.add(map);
						}
						//System.out.print("\n");
					}
				} // for(r) 문
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapList;
	}
	
	// 위 함수 포뮬러 형식 지정 안해도 되도록 오버로딩, 또한 업로드시 날짜 포맷 형식 바꿈 
	public List<HashMap<String, Object>> excelProcess(String filePath, String formulaMode ) throws IOException {

		List<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>(); // 최종 맵
		XSSFRow row;
		XSSFCell cell;

		try {
			// load
			FileInputStream file = new FileInputStream(new File(filePath));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			// sheet수 취득
			int sheetCn = workbook.getNumberOfSheets();
			//System.out.println("sheet수 : " + sheetCn);

			for (int cn = 0; cn < sheetCn; cn++) {
				//System.out.println("취득하는 sheet 이름 : " + workbook.getSheetName(cn));
				//System.out.println(workbook.getSheetName(cn) + " sheet 데이터 취득 시작");
				// 0번째 sheet 정보 취득
				XSSFSheet sheet = workbook.getSheetAt(cn);

				// 취득된 sheet에서 rows수 취득
				int rows = sheet.getPhysicalNumberOfRows();
				//System.out.println(workbook.getSheetName(cn) + " sheet의 row수 : " + rows);
				// 취득된 row에서 취득대상 cell수 취득
				int cells = sheet.getRow(cn).getPhysicalNumberOfCells(); //
				//System.out.println(workbook.getSheetName(cn) + " sheet의 row에 취득대상 cell수 : " + cells);
				
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				DecimalFormat df = new DecimalFormat();
				
				String HeaderName[] = new String[cells];

				for (int r = 0; r < rows; r++) {
					row = sheet.getRow(r); // row 가져오기
					HashMap<String, Object> map = new HashMap<String, Object>(); // 행 데이터 삽입
					if (row != null) {
						for (int c = 0; c < cells; c++) {
							cell = row.getCell(c);
							if (cell != null) {
						
								String value = null;
								switch (cell.getCellType()) {
								case XSSFCell.CELL_TYPE_FORMULA:   // 엑셀 형식이 수식일때, 
									if(!(cell.toString()=="") ){
    									if (evaluator.evaluateFormulaCell(cell) == XSSFCell.CELL_TYPE_NUMERIC) {
    										
    										//20.06.10 수정 jhk (예약전송 업로드에서 날짜 수식 써서 수정) 
    										/// 엑셀 형식에 수식들어가면 날짜형식으로  반환하기 위한것 
    										if( formulaMode.equals("datetimeFormula")) {
    											Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
        										SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:ss");
        										value = formatter.format(date);		
    										}else {
    											double fddata = cell.getNumericCellValue();         
    											value = df.format(fddata);
    										}
    										
    									} else if (evaluator.evaluateFormulaCell(cell) == XSSFCell.CELL_TYPE_STRING) {
    										value = cell.getStringCellValue();
    									} else if (evaluator.evaluateFormulaCell(cell) == XSSFCell.CELL_TYPE_BOOLEAN) {
    										boolean fbdata = cell.getBooleanCellValue();         
    										value = String.valueOf(fbdata);         
    									} else {
    										value = EgovStringUtil.nullToString(cell.getCellFormula());
    									}
    								} 
									break;
								case XSSFCell.CELL_TYPE_NUMERIC:
									if (HSSFDateUtil.isCellDateFormatted(cell)) {
										SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:ss");  // 엑셀 업로드 기본 설정 시간 초로 바꿈, 만약 시간 초 필요없으면 컨틀로러 단에서 조절  
										value = formatter.format(cell.getDateCellValue());
									} else {
										double ddata = cell.getNumericCellValue();
										value = EgovStringUtil.nullToString(df.format(ddata).replace(",",""));
									}
									break;
								case XSSFCell.CELL_TYPE_STRING:
									value = ""+ EgovStringUtil.nullToString(cell.getStringCellValue());
									break;
								case XSSFCell.CELL_TYPE_BLANK:
									value = "";
									break;
								case XSSFCell.CELL_TYPE_ERROR:
									value = "" + cell.getErrorCellValue();
									break;
								default:
									value = EgovStringUtil.nullToString(value);
								}
								if (r == 0) {
									//HeaderName[c] = value.split(" ")[0];
									HeaderName[c] = value;
									
								} else {
									map.put(HeaderName[c], value);
								}
								//System.out.print(value + "\t");
							} else {
								//System.out.print("[null]\t");
							}
						} // for(c) 문
						if (r > 0) {
							int valueCheck = 0;
							for (int c = 0; c < cells; c++) {
								if (map.get(HeaderName[c]) != null
										&& map.get(HeaderName[c]) != "") {
									valueCheck++;
								}
							}
							if (valueCheck == 0) {
								break;
							}
							mapList.add(map);
						}
						//System.out.print("\n");
					}
				} // for(r) 문
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapList;
	}
	
	public List<XSSFSheet> getExcelSheet(String filePath) throws Exception {
		
		List<XSSFSheet> sheetList = new ArrayList<XSSFSheet>();
		
		FileInputStream fis = new FileInputStream(new File(filePath));
		XSSFWorkbook wb = new XSSFWorkbook(fis);
		
		for (XSSFSheet s : wb) {
			sheetList.add(s);
		}
		
		fis.close();
		
		return sheetList;
	}
	
	public String getCellValue(XSSFCell cell) throws Exception {
		
		String value = "";
		
		switch (cell.getCellType()) {
		case XSSFCell.CELL_TYPE_FORMULA:
			value = EgovStringUtil.nullToString(cell.getCellFormula());
			break;
		case XSSFCell.CELL_TYPE_NUMERIC:
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				value = formatter.format(cell.getDateCellValue());
			} else {
				DecimalFormat df = new DecimalFormat();
				double ddata = cell.getNumericCellValue();
				value = EgovStringUtil.nullToString(df.format(ddata));
			}
			break;
		case XSSFCell.CELL_TYPE_STRING:
			value = ""+ EgovStringUtil.nullToString(cell.getStringCellValue());
			break;
		case XSSFCell.CELL_TYPE_BLANK:
			value = "";
			break;
		case XSSFCell.CELL_TYPE_ERROR:
			value = "" + cell.getErrorCellValue();
			break;
		default:
			value = EgovStringUtil.nullToString(value);
		}
		
		return value;
	}

}
