package com.test.java.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.java.common.DataMap;



public class ExcelUtil {

private static final Log logger = LogFactory.getLog(ExcelUtil.class);
	
	static ObjectMapper objectMapper = null;;
	
	
	public static ObjectMapper getObjectMapper() {
		if(ExcelUtil.objectMapper == null) {
			ExcelUtil.objectMapper = new ObjectMapper();
			ExcelUtil.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}
		return ExcelUtil.objectMapper;
	}

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private ExcelUtil() {
	}

	/**
	 * 확장자에 의해서 엑셀파일을 파싱한다.
	 * 
	 * @param fileItem 파일아이템
	 * @return 데이터의 리스트
	 */
	public static List<?> parse(FileItem fileItem) {
		String ext = EgovFileTool.getFileExtension(fileItem.getName());
		InputStream is = null;
		try {
			is = fileItem.getInputStream();
			if ("xls".equalsIgnoreCase(ext)) {
				return parseExcel2003(is);
			} else if ("xlsx".equalsIgnoreCase(ext)) {
				return parseExcel2007(is);
			} else {
				throw new RuntimeException("지원하지 않는 파일포맷입니다.");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	/**
	 * 암호화된 엑셀파일을 파싱한다.
	 * 
	 * @param fileItem 파일아이템
	 * @param password 비밀번호
	 * @return 데이터의 리스트
	 */
	public static List<?> parse(FileItem fileItem, String password) {
		String ext = EgovFileTool.getFileExtension(fileItem.getName());
		InputStream is = null;
		try {
			is = fileItem.getInputStream();
			if ("xls".equalsIgnoreCase(ext)) {
				return parseExcel2003(is, password);
			} else if ("xlsx".equalsIgnoreCase(ext)) {
				return parseExcel2007(is, password);
			} else {
				throw new RuntimeException("지원하지 않는 파일포맷입니다.");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	/**
	 * 확장자에 의해서 엑셀파일을 파싱한다.
	 * 
	 * @param file 파일
	 * @return 데이터의 리스트
	 */
	public static List<?> parse(File file) {
		FileInputStream fis = null;
		try {
			String ext = EgovFileTool.getFileExtension(file);
			fis = new FileInputStream(file);
			if ("xls".equalsIgnoreCase(ext)) {
				return parseExcel2003(fis);
			} else if ("xlsx".equalsIgnoreCase(ext)) {
				return parseExcel2007(fis);
			} else {
				throw new RuntimeException("지원하지 않는 파일포맷입니다.");
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	/**
	 * 암호화된 엑셀파일을 파싱한다.
	 * 
	 * @param file     파일
	 * @param password 비밀번호
	 * @return 데이터의 리스트
	 */
	public static List<?> parse(File file, String password) {
		FileInputStream fis = null;
		try {
			String ext = EgovFileTool.getFileExtension(file);
			fis = new FileInputStream(file);
			if ("xls".equalsIgnoreCase(ext)) {
				return parseExcel2003(fis, password);
			} else if ("xlsx".equalsIgnoreCase(ext)) {
				return parseExcel2007(fis, password);
			} else {
				throw new RuntimeException("지원하지 않는 파일포맷입니다.");
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	

	/**
	 * List객체를 엑셀2003 형식으로 변환하여 응답객체로 전송한다.
	 * 
	 * @param response 응답 객체
	 * @param mapList  리스트 객체
	 * @param fileName 파일명
	 * @return 처리건수
	 */
	public static int renderExcel2003(HttpServletResponse response, List<?> mapList, String fileName) {
		return renderExcel2003(response, mapList, fileName, null);
	}

	/**
	 * List객체를 엑셀2003 형식으로 변환하여 응답객체로 전송한다.
	 * 
	 * @param response 응답 객체
	 * @param mapList  리스트 객체
	 * @param fileName 파일명
	 * @param header   헤더 배열
	 * @return 처리건수
	 */
	public static int renderExcel2003(HttpServletResponse response, List<?> mapList, String fileName,
			String[] header) {
		if (response == null || mapList == null || fileName == null) {
			return 0;
		}
		int rowCount = 0;
		try {
			setResponseHeaders(response, fileName);
			Workbook workbook = new HSSFWorkbook();
			rowCount = writeWorkbook(response.getOutputStream(), mapList, header, workbook);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * List객체를 엑셀2003 형식으로 변환하여 파일로 저장한다.
	 * 
	 * @param file    파일
	 * @param mapList 리스트 객체
	 * @return 처리건수
	 */
	public static int writeExcel2003(File file, List<?> mapList) {
		return writeExcel2003(file, mapList, null);
	}

	/**
	 * List객체를 엑셀2003 형식으로 변환하여 파일로 저장한다.
	 * 
	 * @param file    파일
	 * @param mapList 리스트 객체
	 * @param header  헤더 배열
	 * @return 처리건수
	 */
	public static int writeExcel2003(File file, List<?> mapList, String[] header) {
		if (file == null || mapList == null) {
			return 0;
		}
		int rowCount = 0;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			Workbook workbook = new HSSFWorkbook();
			rowCount = writeWorkbook(fos, mapList, header, workbook);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return rowCount;
	}

	/**
	 * List객체를 엑셀2007 형식으로 변환하여 응답객체로 전송한다.
	 * 
	 * @param response 응답 객체
	 * @param mapList  리스트 객체
	 * @param fileName 파일명
	 * @return 처리건수
	 */
	public static int renderExcel2007(HttpServletResponse response, List<?> mapList, String fileName) {
		return renderExcel2007(response, mapList, fileName, null, null);
	}

	/**
	 * List객체를 엑셀2007 형식으로 변환하여 응답객체로 전송한다.
	 * 
	 * @param response 응답 객체
	 * @param mapList  리스트 객체
	 * @param fileName 파일명
	 * @param header   헤더 배열
	 * @return 처리건수
	 */
	public static int renderExcel2007(HttpServletResponse response, List<?> mapList, String fileName,
			String[] header) {
		return renderExcel2007(response, mapList, fileName, header, null);
	}

	/**
	 * List객체를 엑셀2007 형식으로 변환하여 응답객체로 전송한다.
	 * 
	 * @param response 응답 객체
	 * @param mapList  리스트 객체
	 * @param fileName 파일명
	 * @param header   헤더 배열
	 * @param password 열기암호
	 * @return 처리건수
	 */
	public static int renderExcel2007(HttpServletResponse response, List<?> mapList, String fileName,
			String[] header, String password) {
		if (response == null || mapList == null || fileName == null) {
			return 0;
		}
		int rowCount = 0;
		try {
			setResponseHeaders(response, fileName);
			Workbook workbook = new XSSFWorkbook();
			rowCount = writeWorkbook(response.getOutputStream(), mapList, header, workbook);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * List객체를 엑셀2007 형식으로 변환하여 파일로 저장한다.
	 * 
	 * @param file    파일
	 * @param mapList 리스트 객체
	 * @return 처리건수
	 */
	public static int writeExcel2007(File file, List<?> mapList) {
		return writeExcel2007(file, mapList, null, null);
	}

	/**
	 * List객체를 엑셀2007 형식으로 변환하여 파일로 저장한다.
	 * 
	 * @param file    파일
	 * @param mapList 리스트 객체
	 * @param header  헤더 배열
	 * @return 처리건수
	 */
	public static int writeExcel2007(File file, List<?> mapList, String[] header) {
		return writeExcel2007(file, mapList, header, null);
	}

	/**
	 * List객체를 엑셀2007 형식으로 변환하여 파일로 저장한다.
	 * 
	 * @param file     파일
	 * @param mapList  리스트 객체
	 * @param header   헤더 배열
	 * @param password 읽기암호
	 * @return 처리건수
	 */
	public static int writeExcel2007(File file, List<?> mapList, String[] header, String password) {
		if (file == null || mapList == null) {
			return 0;
		}
		int rowCount = 0;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			Workbook workbook = new XSSFWorkbook();
			rowCount = writeWorkbook(fos, mapList, header, workbook);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return rowCount;
	}

	/**
	 * List객체를 엑셀2007 스트리밍 형식으로 변환하여 응답객체로 전송한다.
	 * 
	 * @param response 응답 객체
	 * @param mapList  리스트 객체
	 * @param fileName 파일명
	 * @return 처리건수
	 */
	public static int renderExcel2007S(HttpServletResponse response, List<?> mapList, String fileName) {
		return renderExcel2007S(response, mapList, fileName, null, null);
	}

	/**
	 * List객체를 엑셀2007 스트리밍 형식으로 변환하여 응답객체로 전송한다.
	 * 
	 * @param response 응답 객체
	 * @param mapList  리스트 객체
	 * @param fileName 파일명
	 * @param header   헤더 배열
	 * @return 처리건수
	 */
	public static int renderExcel2007S(HttpServletResponse response, List<?> mapList, String fileName,
			String[] header) {
		return renderExcel2007S(response, mapList, fileName, header, null);
	}

	/**
	 * List객체를 엑셀2007 스트리밍 형식으로 변환하여 응답객체로 전송한다.
	 * 
	 * @param response 응답 객체
	 * @param mapList  리스트 객체
	 * @param fileName 파일명
	 * @param header   헤더 배열
	 * @param password 열기암호
	 * @return 처리건수
	 */
	public static int renderExcel2007S(HttpServletResponse response, List<?> mapList, String fileName,
			String[] header, String password) {
		if (response == null || mapList == null || fileName == null) {
			return 0;
		}
		int rowCount = 0;
		try {
			setResponseHeaders(response, fileName);
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			workbook.setCompressTempFiles(true);
			rowCount = writeWorkbook(response.getOutputStream(), mapList, header, workbook);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * List객체를 엑셀2007 스트리밍 형식으로 변환하여 파일로 저장한다.
	 * 
	 * @param file    파일
	 * @param mapList 리스트 객체
	 * @return 처리건수
	 */
	public static int writeExcel2007S(File file, List<?> mapList) {
		return writeExcel2007S(file, mapList, null, null);
	}

	/**
	 * List객체를 엑셀2007 스트리밍 형식으로 변환하여 파일로 저장한다.
	 * 
	 * @param file    파일
	 * @param mapList 리스트 객체
	 * @param header  헤더 배열
	 * @return 처리건수
	 */
	public static int writeExcel2007S(File file, List<?> mapList, String[] header) {
		return writeExcel2007S(file, mapList, header, null);
	}

	/**
	 * List객체를 엑셀2007 스트리밍 형식으로 변환하여 파일로 저장한다.
	 * 
	 * @param file     파일
	 * @param mapList  리스트 객체
	 * @param header   헤더 배열
	 * @param password 열기암호
	 * @return 처리건수
	 */
	public static int writeExcel2007S(File file, List<?> mapList, String[] header, String password) {
		if (file == null || mapList == null) {
			return 0;
		}
		int rowCount = 0;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			workbook.setCompressTempFiles(true);
			rowCount = writeWorkbook(fos, mapList, header, workbook);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return rowCount;
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private
	////////////////////////////////////////////////////////////////////////////////////////// 메소드

	private static void appendHeader(Row row, String[] header, CellStyle cellStyle) {
		if (row == null || header == null || cellStyle == null) {
			return;
		}
		for (int c = 0; c < header.length; c++) {
			Cell cell = row.createCell(c);
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cell.setCellValue(header[c]);
			cell.setCellStyle(cellStyle);
		}
	}

		
	private static void appendRow(Row row, DataMap map, CellStyle cellStyle) {
		
		
		if (row == null || map == null || cellStyle == null) {
			return;
		}
		int c = 0;
		List<String> keys = map.keyList();
		for (String key : keys) {
			Object value = map.get(key);
			Cell cell = row.createCell(c++);
			if (value == null) {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				cell.setCellValue("");
			} else {
				if (value instanceof Number) {
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellValue(Double.valueOf(value.toString()));
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue(value.toString());
				}
			}
			cell.setCellStyle(cellStyle);
		}
	}

	private static List<?> parseExcel2003(InputStream is) {
		try {
			HSSFWorkbook workbook = new HSSFWorkbook(new POIFSFileSystem(is));
			return parseSheet(workbook.getSheetAt(0));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<DataMap> parseExcel2003(InputStream is, String password) {
		try {
			Biff8EncryptionKey.setCurrentUserPassword(password);
			HSSFWorkbook workbook = new HSSFWorkbook(new POIFSFileSystem(is));
			return parseSheet(workbook.getSheetAt(0));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			Biff8EncryptionKey.setCurrentUserPassword(null);
		}
	}

	private static List<DataMap> parseExcel2007(InputStream is) {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			return parseSheet(workbook.getSheetAt(0));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<DataMap> parseExcel2007(InputStream is, String password) {
		try {
			POIFSFileSystem fs = new POIFSFileSystem(is);
			EncryptionInfo info = new EncryptionInfo(fs);
			Decryptor d = Decryptor.getInstance(info);
			d.verifyPassword(password);
			XSSFWorkbook workbook = new XSSFWorkbook(d.getDataStream(fs));
			return parseSheet(workbook.getSheetAt(0));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 엑셀 시트의 데이터 파싱하여 맵의 리스트로 리턴
	 */
	private static List<DataMap> parseSheet(Sheet sheet) {
		List<DataMap> mapList = new ArrayList<DataMap>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int rowCount = sheet.getPhysicalNumberOfRows();
		int colCount = sheet.getRow(0).getPhysicalNumberOfCells();
		for (int i = 0; i < rowCount; i++) {
			Row row = sheet.getRow(i);
			if (row != null) {
				DataMap map = new DataMap();
				for (int j = 0; j < colCount; j++) {
					Cell cell = row.getCell(j);
					String item = "";
					if (cell != null) {
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_BOOLEAN:
						case Cell.CELL_TYPE_FORMULA:
						case Cell.CELL_TYPE_STRING:
							cell.setCellType(Cell.CELL_TYPE_STRING);
							item = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_NUMERIC:
							if (DateUtil.isCellDateFormatted(cell)) {
								Date date = cell.getDateCellValue();
								item = dateFormat.format(date);
							} else {
								cell.setCellType(Cell.CELL_TYPE_STRING);
								item = cell.getStringCellValue();
							}
							break;
						}
					}
					map.put(String.valueOf(j), item);
				}
				mapList.add(map);
			}
		}
		return mapList;
	}

	/**
	 * 헤더 셀 스타일 리턴
	 */
	private static CellStyle headerStyle(Workbook workbook) {
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 11);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setFontName("Dotum");
		font.setColor(HSSFColor.BLACK.index);
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFont(font);
		cellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cellStyle.setRightBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cellStyle.setTopBorderColor(HSSFColor.BLACK.index);
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		return cellStyle;
	}

	/**
	 * 로우 셀 스타일 리턴
	 */
	private static CellStyle rowStyle(Workbook workbook) {
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 11);
		font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
		font.setFontName("Dotum");
		font.setColor(HSSFColor.BLACK.index);
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFont(font);
		cellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cellStyle.setRightBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cellStyle.setTopBorderColor(HSSFColor.BLACK.index);
		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		return cellStyle;
	}

	/**
	 * 파일 다운로드 헤더 셋팅
	 */
	private static void setResponseHeaders(HttpServletResponse response, String fileName)
			throws UnsupportedEncodingException {
		if (response == null) {
			return;
		}
		response.reset();
		response.setContentType("application/octet-stream;");
		response.setHeader("Content-Disposition", (new StringBuilder("attachment; filename=\""))
				.append(new String(fileName.getBytes(), "ISO-8859-1")).append("\"").toString());
		response.setHeader("Pragma", "no-cache;");
		response.setHeader("Expires", "-1;");
	}

	
	/**
	 * List객체를 워크북으로 변환하여 스트림으로 전송한다.
	 */
	private static int writeWorkbook(OutputStream os, List<?> mapList, String[] header, Workbook workbook)
			throws IOException {
		if (os == null || workbook == null || mapList == null) {
			return 0;
		}
		int rowCount = 0;
		Sheet sheet = workbook.createSheet();
		if (header != null) {
			appendHeader(sheet.createRow(rowCount), header, headerStyle(workbook));
			rowCount++;
		}
		CellStyle cellStyle = rowStyle(workbook);
		DataMap dataEgovMap;
		for (Object map : mapList) {
			 if(map instanceof DataMap) {
             	dataEgovMap = (DataMap)map;
             }else {
             	dataEgovMap = ExcelUtil.getObjectMapper().convertValue(map, DataMap.class);
             }     
			appendRow(sheet.createRow(rowCount), dataEgovMap, cellStyle);
			rowCount++;
		}
		if (header != null) {
			for (int i = 0; i < header.length; i++) {
				sheet.autoSizeColumn(i);
				sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
			}
		}
		workbook.write(os);
		return rowCount;
	}
}
