package com.test.java.util;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

import com.test.java.common.DataMap;



public class GridXmlBuilder {

	public static String getCommonXmlString(String gridConfigData, List<DataMap> dataList, HttpServletRequest request, long pageIndex, long recordCountPerPage) throws Exception{
		StringBuffer sb = new StringBuffer();

		long totalCnt = 0L;
		if(dataList != null && dataList.size() > 0 && dataList.get(0).get("totalCnt") != null){
			totalCnt = Long.parseLong(dataList.get(0).get("totalCnt").toString());
		}
		sb.append(GridXmlBuilder.generateHeader(gridConfigData, request, totalCnt, pageIndex, recordCountPerPage));
		sb.append(GridXmlBuilder.generateCommonBody(gridConfigData, dataList));
		return sb.toString();
	}

	public static String getTreeXmlString(String gridConfigData, List<DataMap> dataList, HttpServletRequest request) throws Exception{
		StringBuffer sb = new StringBuffer();
		sb.append(GridXmlBuilder.generateHeader(gridConfigData, request, 0, 0, 0));
		sb.append(GridXmlBuilder.generateTreeBody(gridConfigData, dataList));
		return sb.toString();
	}

	public static List<GridCell> getGridCellList(String gridConfigData) throws Exception{
		ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
	    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper.readValue(gridConfigData, TypeFactory.defaultInstance().constructCollectionType(List.class, GridCell.class));
	}

	public static String generateHeader(String gridConfigData, HttpServletRequest request, long totalCnt, long pageIndex, long recordCountPerPage) throws Exception{
		List<GridCell> gridCellList = GridXmlBuilder.getGridCellList(gridConfigData);

		/** 헤더가 멀티로우인지 체크한다, 3개인지만 체크 */
		int headerRowCount = 1;
		if(gridCellList.get(0).getHeader().split("//").length > 1){
			headerRowCount = gridCellList.get(0).getHeader().split("//").length;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		if(totalCnt >0){ /* 페이징인 경우에만*/
			sb.append("<rows total_count='"+totalCnt+"' pos='"+ ( (pageIndex -1) * recordCountPerPage)+"'>\n");
		}else{
			sb.append("<rows>\n");
		}

		sb.append("\t<head>\n");
		for(GridCell cell : gridCellList){
			String headerText = "";
			if(headerRowCount > 1){
				headerText = cell.getHeader().split("//")[0];
			}else{
				headerText = cell.getHeader();
			}
			if(cell.getComboListStr().equals("") == false){
				headerText += cell.getComboListStr();
			}
			String etcConfig = "";
			/*
			if(cell.getType().equals("co") || cell.getType().equals("coro") || cell.getType().equals("combo") || cell.getType().equals("clist")){
				List<ComCode> codeList = ComCodeController.getCodeList(request, cell.getGroupCode(), "");
				for(ComCode code : codeList){
					headerText += String.format("<option value='%s'>%s</option>", code.getCode(), code.getCodeNm());
				}
				etcConfig = "filter='true' xmlcontent='1'";
			}	*/

			if(cell.getType().equals("ron") || cell.getType().equals("edn")){
				if(cell.getSorting().equals("na") == false){
					cell.setSorting("int");
				}
			}

			sb.append(String.format("\t\t<column  type='%s' width='%s' id='%s' align='%s' valign='%s' sort='%s' format='%s' hidden='%s' color='%s' %s>%s</column>\n"
				,cell.getType()
				,cell.getWidth()
				,cell.getColumnName()
				,cell.getAlign()
				,cell.getValign()
				,cell.getSorting()
				,cell.getFormat()
				,cell.getHidden()
				,cell.getColor()
				,etcConfig
				,headerText
				));
		}


		sb.append("\t\t<beforeInit>\n");
		sb.append("\t\t<call command=\"clearAll\"></call>\n");
		sb.append("\t\t</beforeInit>\n");

		sb.append("\t\t<afterInit>\n");

		if(headerRowCount > 1){
			sb.append("\t\t<call command=\"attachHeader\"><param>");

			String headerStrings = "";
			for(GridCell cell : gridCellList){
				String headerText = cell.getHeader().split("//")[1];
				if(headerStrings.equals("")){
					headerStrings = headerText;
				}else{
					headerStrings += "," + headerText;
				}
			}
			sb.append(headerStrings +"</param></call>");

		}
		if(headerRowCount == 3){
			sb.append("\t\t<call command=\"attachHeader\"><param>");

			String headerStrings = "";
			for(GridCell cell : gridCellList){
				String headerText = cell.getHeader().split("//")[2];
				if(headerStrings.equals("")){
					headerStrings = headerText;
				}else{
					headerStrings += "," + headerText;
				}
			}
			sb.append(headerStrings +"</param></call>");

		}
		sb.append("\t\t</afterInit>\n");
		sb.append("\t</head>\n");

		return sb.toString();
	}

	public static String generateCommonBody(String gridConfigData, List<DataMap> dataList) throws Exception{
		List<GridCell> gridCellList = GridXmlBuilder.getGridCellList(gridConfigData);

		String[] keys = GridXmlBuilder.getKeys(gridCellList);
		StringBuffer sb = new StringBuffer();
		if(dataList != null){
			for(DataMap data : dataList){
				sb.append("\t<row id=\"");
				for(int i = 0; i < keys.length; i++){
					if(i > 0){
						sb.append("+");
					}
					sb.append(data.get(keys[i]));
				}
				sb.append("\">\n");

				sb.append(GridXmlBuilder.generateXmlCell(gridCellList, data));

				sb.append("\t</row>\n");
			}
		}
		sb.append("</rows>\n");

		return sb.toString();
	}

	public static String generateTreeBody(String gridConfigData, List<DataMap> dataList) throws Exception{
		List<GridCell> gridCellList = GridXmlBuilder.getGridCellList(gridConfigData);
		String[] keys = GridXmlBuilder.getKeys(gridCellList);
		String currentId = "";
		int currentLevel = 1;
		StringBuffer sb = new StringBuffer();
		if(dataList != null){
			for(DataMap data : dataList){
				currentId =  data.get("currentCd").toString();
				currentLevel = Integer.parseInt(data.get("currentLevel").toString());
				if(currentLevel== 1 ){
					sb.append("\t<row id=\"");
					for(int i = 0; i < keys.length; i++){
						if(i > 0){
							sb.append("+");
						}
						sb.append(data.get(keys[i]));
					}
					sb.append("\">\n");

					sb.append(GridXmlBuilder.generateXmlCell(gridCellList, data));
					if(data.get("isLeaf").toString().equals("0")){//하위노드가 있으면
						sb.append(GridXmlBuilder.getLevelXmlString(gridCellList, dataList, currentId, currentLevel+1, keys));
					}
					sb.append("\t</row>\n");
				}
			}
		}
		sb.append("</rows>\n");

		return sb.toString();
	}

	public static String getLevelXmlString(List<GridCell> gridCellList, List<DataMap> dataList, String parentId, int level, String[] keys) throws Exception{
		StringBuffer sb = new StringBuffer();

		String currentId = "";
		for(DataMap data : dataList){
			if(data.get("parentCd") != null && data.get("parentCd").toString().equals(parentId) && Integer.parseInt(data.get("currentLevel").toString()) == level){
				currentId =  data.get("currentCd").toString();
				sb.append("\t<row id=\"");
				for(int i = 0; i < keys.length; i++){
					if(i > 0){
						sb.append("+");
					}
					sb.append(data.get(keys[i]));
				}
				sb.append("\">\n");
				sb.append(GridXmlBuilder.generateXmlCell(gridCellList, data));

				if(data.get("isLeaf").toString().equals("0")){//하위노드가 있으면
					sb.append(GridXmlBuilder.getLevelXmlString(gridCellList, dataList, currentId, level+1, keys));
				}

				sb.append("\n</row>");
			}

		}
		return sb.toString();

	}

	public static String[] getKeys(List<GridCell> gridCellList) throws Exception{
		String keyElement = "";
		for(GridCell cell : gridCellList){
			if(cell.getIsKey()){
				if(keyElement.equals("")){
					keyElement = cell.getColumnName();
				}else{
					keyElement += "," +cell.getColumnName();
				}
			}
		}
		return keyElement.split(",");
	}



	public static String generateXmlCell(List<GridCell> gridCellList, DataMap data) throws Exception{
		String validateConfig = "";
		StringBuffer sb = new StringBuffer();
		for(GridCell cell : gridCellList){
			String xmlValue = data.getString(cell.getColumnName());
			validateConfig = "";
			if(cell.getValidate().equals("") == false){
				validateConfig = " validate='"+cell.getValidate()+"'";
			}
			if(xmlValue != null){
				if(cell.getType().equals("ch") || cell.getType().equals("ra") || cell.getType().equals("ra_str")){
					if(xmlValue.equals("0") == false && xmlValue.equals("1") == false){
						if(xmlValue.toString().equals("") || xmlValue.toString().equals("N")){
							xmlValue = "0";
						}else{
							xmlValue = "1";
						}
					}
				}else if(cell.getType().equals("co") || cell.getType().equals("coro") || cell.getType().equals("combo")){
					validateConfig += " xmlcontent='true'";
					xmlValue += cell.getComboListStr();
				}else if(cell.getType().equals("edn") || cell.getType().equals("ron")){
					if(xmlValue.equals("0") && cell.getDisplayZero() == false){
						xmlValue = "";
					}
				}else{
					xmlValue = "<![CDATA["+xmlValue.toString() + "]]>";
				}
				/*
				if(cell.getCastNLTOBR() == true){
					xmlValue = EgovStringUtil.changeQuotation(xmlValue);
				}
				*/
//				String addStyle = "";
//				String[] level = {"grade1", "grade2","grade3","grade4","gradeNo"};
//				for(int i=0; i<5; i++){
//					if(cell.getColumnName().equals(level[i])){
//						addStyle = "style='background-color:yellow;'";
//					}
//				}
				sb.append("\t\t<cell"+validateConfig+">"+xmlValue.toString() + "</cell>\n");
            }else{
            	sb.append("\t\t<cell"+validateConfig+"></cell>\n");
            }
		}
		return sb.toString();
	}


	public static String getExcelTemplateXml(String gridConfigData, List<DataMap> dataList) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		List<GridCell> gridCellList = mapper.readValue(gridConfigData, TypeFactory.defaultInstance().constructCollectionType(List.class, GridCell.class));


		/** 헤더가 멀티로우인지 체크한다, 2개인지만 체크 */
		int headerRowCount = 1;
		headerRowCount = gridCellList.get(0).getHeader().split("//").length;


		StringBuffer sb = new StringBuffer();

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<rows profile='color'>\n");
		sb.append("\t<head>\n");
		sb.append("\t\t<columns>\n");
		for(GridCell cell : gridCellList){

			String cellMerge = "";
			String headerText = "";
			if(headerRowCount > 1){
				headerText = cell.getHeader().split("//")[0];
			}else{
				headerText = cell.getHeader();
			}
			if(headerText.equals("#cspan") || headerText.equals("#rspan")){
				headerText = "";
			}
			if(!cell.getRspan().equals("")){
				cellMerge = "rowspan=\""+cell.getRspan()+"\"";
			}else if(!cell.getCspan().equals("")){
				cellMerge = "colspan=\""+cell.getCspan()+"\"";
			}


			if(cell.getSorting().equals("int")){
				cell.setAlign("right");
			}

			/* 가변 폭인경우 300 으로 고정 */
			if(cell.getWidth().equals("*")){
				cell.setWidth("300");
			}
			if(cell.getHidden() == false){
			sb.append(String.format("\t\t\t<column %s type='%s' width='%s' id='%s' align='%s' valign='%s' sort='%s' format='%s' hidden='false'><![CDATA[%s]]></column>\n"
				,cellMerge
				,cell.getType()
				,cell.getWidth()
				,cell.getColumnName()
				,cell.getAlign()
				,cell.getValign()
				,cell.getSorting()
				,cell.getFormat()
				,headerText
				));
			}

		}
		sb.append("\n\t\t</columns>\n");

		if(headerRowCount > 1){
			sb.append("\t\t<columns>\n");
			for(GridCell cell : gridCellList){
				String headerText = "";
				if(headerRowCount > 1){
					headerText = cell.getHeader().split("//")[1];
				}else{
					headerText = cell.getHeader();
				}
				if(headerText.equals("#cspan") || headerText.equals("#rspan")){
					headerText = "";
				}
				sb.append(String.format("\t\t\t<column>%s</column>\n" ,headerText ));
			}
			sb.append("\n\t\t</columns>\n");
		}

		if(headerRowCount ==3 ){
			sb.append("\t\t<columns>\n");
			for(GridCell cell : gridCellList){
				String headerText = "";
				if(headerRowCount == 2){
					headerText = cell.getHeader().split("//")[2];
				}else{
					headerText = cell.getHeader();
				}
				if(headerText.equals("#cspan") || headerText.equals("#rspan")){
					headerText = "";
				}
				sb.append(String.format("\t\t\t<column>%s</column>\n" ,headerText ));
			}
			sb.append("\n\t\t</columns>\n");
		}


		sb.append("\t</head>\n");
		if(dataList != null){
			for(DataMap code : dataList){
				sb.append("\t<row>");
				for(GridCell cell : gridCellList){
					if(cell.getHidden() == false){
						Object xmlValue = code.get(cell.getColumnName());
						if(xmlValue != null){
							if(cell.getSorting().equals("int")){
								sb.append("\t\t<cell>"+xmlValue.toString() + "</cell>\n");
							}else if(cell.getSorting().equals("date") || cell.getSorting().equals("datestr")){
								String sDate = xmlValue.toString();								
								try{
									if(cell.getSorting().equals("datestr")){
										sDate = EgovDateUtil.formatDate(xmlValue.toString(), "-");
									}else{
										sDate = EgovDateUtil.formatDatePattern((Date)xmlValue, cell.getFormat());
									}									
								}catch(Exception ex){									
								}								
								sb.append("\t\t<cell><![CDATA["+sDate + "]]></cell>\n");
							}else{
								sb.append("\t\t<cell><![CDATA["+xmlValue.toString() + "]]></cell>\n");
							}
		                }else{
		                	sb.append("\t\t<cell> </cell>\n");
		                }
					}

				}
				sb.append("\t</row>\n");
			}
		}
		sb.append("</rows>\n");

		return sb.toString();
	}

	public static String getDataProcessorXml(List<DataMap> dataList) throws Exception{

		StringBuffer sb = new StringBuffer();
		String keyName = "";
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("\t<data>");
		for(DataMap data : dataList){

			sb.append(String.format("\n\t\t<action sid='%s' type='%s' tid='%s' errorMessage='%s' >",
					data.get("sid"), data.get("type"), data.get("tid"), data.get("errorMessage")));

			Iterator it = data.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        keyName = pairs.getKey().toString();
		        if(keyName.equals("editCol") == false && keyName.equals("!nativeeditorStatus") == false )
                	sb.append("\t\t\t\n<" + keyName + "><![CDATA["+pairs.getValue().toString()+"]]></"+keyName+">");
		        it.remove();
		    }
			sb.append("\n\t\t</action>");
		}
		sb.append("\n\t</data>");
		return sb.toString();
	}

}