package com.test.java.util;

public class GridCell {
	private String columnName = "";
	private Boolean isKey = false;
	private String header = "";
	private String width = "100";
	private String align = "left";
	private String valign = "center";
	private String type = "ro";
	private String sorting = "str"; // 헤더 클릭시 정렬 기능(비활성 기본)
	private String format = "";
	private Boolean hidden = false;
	private String cspan = "";
	private String rspan = "";
	private String color = "";
	private String validate = "";
	private String groupCode = "";
	private String comboListStr = "";
	private Boolean castNLTOBR = false;
	private Boolean toolTies = false;
	private Boolean displayZero = false;

	public String getComboListStr() {
		return comboListStr;
	}

	public void setComboListStr(String comboListStr) {
		this.comboListStr = comboListStr;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Boolean getIsKey() {
		return isKey;
	}

	public void setIsKey(Boolean isKey) {
		this.isKey = isKey;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getAlign() {
		String temp = align;
		if (type.equals("ron") || type.equals("edn")) {
			temp = "right";
		}
		return temp;
	}

	public void setAlign(String align) {
		this.align = align;
	}

	public String getValign() {
		return valign;
	}

	public void setValign(String valign) {
		this.valign = valign;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSorting() {
		return sorting;
	}

	public void setSorting(String sorting) {
		this.sorting = sorting;
	}

	public String getFormat() {
		String temp = format;
		if (format.equals("") && (type.equals("ron") || type.equals("edn"))) {
			temp = "0,000";
		}
		return temp;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public String getCspan() {
		return cspan;
	}

	public void setCspan(String cspan) {
		this.cspan = cspan;
	}

	public String getRspan() {
		return rspan;
	}

	public void setRspan(String rspan) {
		this.rspan = rspan;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getValidate() {
		return validate;
	}

	public void setValidate(String validate) {
		this.validate = validate;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(String groupCode) {
		this.groupCode = groupCode;
	}

	public Boolean getCastNLTOBR() {
		return castNLTOBR;
	}

	public void setCastNLTOBR(Boolean castNLTOBR) {
		this.castNLTOBR = castNLTOBR;
	}

	public Boolean getToolTies() {
		return toolTies;
	}

	public void setToolTies(Boolean toolTies) {
		this.toolTies = toolTies;
	}

	public Boolean getDisplayZero() {
		return displayZero;
	}

	public void setDisplayZero(Boolean displayZero) {
		this.displayZero = displayZero;
	}
}
