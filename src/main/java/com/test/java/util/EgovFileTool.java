package com.test.java.util;

import java.io.File;

public class EgovFileTool {

	public static String getFileExtension(File file) {
		return EgovFileTool.getFileExtension(file.toString());
	}

	public static String getFileExtension(String filePath) {
		return filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
	}

}
