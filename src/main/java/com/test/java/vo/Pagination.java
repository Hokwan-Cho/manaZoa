package com.test.java.vo;

public class Pagination {
	protected int sn;
	protected int start;
	protected int end;
	protected long totalCnt;
	protected int page;
	protected int totalPages;
	protected int pageSize;
	protected int listNum;


	public int getSn() {
		return sn;
	}

	public void setSn(int sn) {
		this.sn = sn;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public long getTotalCnt() {
		return totalCnt;
	}

	public void setTotalCnt(long totalCnt) {
		this.totalCnt = totalCnt;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getListNum() {
		return listNum;
	}

	public void setListNum(int listNum) {
		this.listNum = listNum;
	}
}
