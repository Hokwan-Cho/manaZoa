package com.test.java.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.test.java.common.DataMap;


@Component("fileUploadService")
public class FileUploadService {
	
	@Autowired
	ServletContext context;

	public static final int BUFF_SIZE = 2048;

	private static final Logger logger = (Logger) LoggerFactory.getLogger(ServiceGatewayImpl.class);

	public List<DataMap> uploadFile(Map<String, MultipartFile> files, String storePath) throws Exception {

		int fileKey = 0;

		File saveFolder = new File(storePath);

		if (!saveFolder.exists() || saveFolder.isFile()) {
			saveFolder.mkdirs();
		}

		Iterator<Entry<String, MultipartFile>> itr = files.entrySet()
				.iterator();
		MultipartFile file;
		String filePath = "";
		List<DataMap> result = new ArrayList<DataMap>();
		DataMap fvo;

		while (itr.hasNext()) {
			Entry<String, MultipartFile> entry = itr.next();

			file = entry.getValue();
			String orginFileName = file.getOriginalFilename();

			if ("".equals(orginFileName)) {
				continue;
			}

			int index = orginFileName.lastIndexOf(".");
			// String fileName = orginFileName.substring(0, index);
			String fileExt = index == -1 ? null : orginFileName.substring(index)+1;
			String newName = "FILE" + getTimeStamp() + fileKey;
			long _size = file.getSize();

			if (!"".equals(orginFileName)) {
				filePath = storePath + File.separator + newName;
				file.transferTo(new File(filePath));
			}
			fvo = new DataMap();
			fvo.put("name", orginFileName);
			fvo.put("fileExtsn", fileExt);
			fvo.put("fileStreCours", storePath);
			fvo.put("fileSize", _size);
			fvo.put("orignlFileNm", orginFileName);
			fvo.put("streFileNm", newName);
			fvo.put("delYn", "N");
			fvo.put("dnReadCnt", 0);
			fvo.put("mimetype", file.getContentType());


			result.add(fvo);

			fileKey++;
		}

		return result;
	}

	private static String getTimeStamp() {
		String rtnStr = null;
		String pattern = "yyyyMMddhhmmssSSS";
		try {
			SimpleDateFormat sdfCurrent = new SimpleDateFormat(pattern, Locale.KOREA);
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			rtnStr = sdfCurrent.format(ts.getTime());
		} catch (Exception e) {
			logger.debug("IGNORED: " + e.getMessage());
		}
		return rtnStr;
	}
	
	public DataMap generateZipFile(String[] fileIds, String[] streFileNms, String storePath, List<DataMap> fileList, DataMap queryMap) throws Exception {
		
		String sufrerPin = queryMap.getString("sufrerPin");
		String sufrerNm = queryMap.getString("sufrerNm");
		String assignSe = queryMap.getString("assignSe");
		String operCd = queryMap.getString("operCd");
		
		DataMap vo = new DataMap();
		String storeFileName = "";
		long fileSize = 0;

		fileIds = new HashSet<String>(Arrays.asList(fileIds)).toArray(new String[0]);

		if (!sufrerPin.equals("") && !sufrerNm.equals("")) {
			if(operCd.equals("KEITI")) { // 환경노출조사 설문
				storeFileName = "환경노출조사설문_" + sufrerPin + "_" + sufrerNm + "_" + getTimeStamp() + ".zip";
			} else {
				storeFileName = operCd + "_" + assignSe + "_" + sufrerPin + "_" + sufrerNm + "_" + getTimeStamp() + ".zip";
			}
		} else {
			storeFileName = "ZIP" + getTimeStamp() + ".zip";
		}
		
		String newStorePath = storePath + "zip";

		File dir = new File(newStorePath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		// 압축파일이름설정
		FileOutputStream fos = null;		
		BufferedOutputStream bos = null;		
		ZipArchiveOutputStream zos = null;		
		FileInputStream fis = null;

		try {
			fos = new FileOutputStream(newStorePath + File.separator + storeFileName);
			bos = new BufferedOutputStream(fos);
			zos = new ZipArchiveOutputStream(bos);
			
			zos.setEncoding("EUC-KR"); // 한글파일명 깨짐 수정
			zos.setLevel(0);

			for (int i = 0; i < streFileNms.length; i++) {
				BufferedInputStream bis = null;

				try {
					String fileName = streFileNms[i];
					String originalFileName = "";
					String path = "";

					for (DataMap dataMap : fileList) {
						String tempNm = dataMap.get("streFileNm").toString();
						if (fileName.equals(tempNm)) {
							originalFileName = dataMap.get("orignlFileNm").toString();
							path = dataMap.getString("folderRoot") + dataMap.getString("folderStre");
							break;
						}
					}
					File file = new File(path + File.separator + fileName);
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
					ZipArchiveEntry ze = new ZipArchiveEntry(originalFileName);

					ze.setTime(file.lastModified());
					fileSize += ze.getCompressedSize();
					zos.putArchiveEntry(ze);
					byte[] buffer = new byte[1024];
					int idx = 0;
					while ((idx = bis.read(buffer, 0, 1024)) != -1) {
						zos.write(buffer, 0, idx);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					zos.closeArchiveEntry();
					bis.close();
					if (fis != null) {
						try {
							fis.close();
						} catch (Exception ex) {
						}
					}
				}
			}
			zos.finish();
		}catch(Exception ex) {ex = ex = null;}
		finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (Exception ignore) {
					logger.debug("IGNORED: " + ignore.getMessage());
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (Exception ignore) {
					logger.debug("IGNORED: " + ignore.getMessage());
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception ignore) {
					logger.debug("IGNORED: " + ignore.getMessage());
				}
			}
		}
		
		vo.put("streFileNm", storeFileName);
		vo.put("orignlFileNm", storeFileName);
		vo.put("fileSize", fileSize);
		vo.put("folderRoot", storePath);
		vo.put("folderStre", "zip" + File.separator);

		return vo;
	}

}
