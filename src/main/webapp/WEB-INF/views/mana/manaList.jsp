<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


	
		<div class="ct_area">
			<div class="adminpageGnb" style="background-color:#2a769a;">
				<ul>
					<li><a href="#"><span class="icon-home"></span></a></li>
					<li><a href="#">예산</a></li>				
					<li class="on"><a href="#">예산사업 관리</a></li>
				</ul>
			</div>		
			
			<div id="contentInfo" class="adminpageHead">
				<h2 class="left_path">
					만화책 목록 
				</h2>
				
				<div  class="right_path">
					<button type="button" class="btn btn-success" onclick="fn_excel();">Excel</button>
		
					<button type="button" class="btn btn-secondary">Excel Upload</button>
					<button type="button" class="btn btn-danger">Danger</button>
					<button type="button" class="btn btn-warning">Warning</button>
					<button type="button" class="btn btn-info">Info</button>
					<button type="button" class="btn btn-light">Light</button>
					<button type="button" class="btn btn-dark">Dark</button>
				</div>				
			</div>
			<!-- search box -->
			<form id="searchFrm"  method="post" action="/"  accept-charset = "UTF-8">
			
				<!-- paging 관련 변수-->
				<input type="hidden" id="pageIndex" name="pageIndex" value="${searchMap.pageIndex}"> 
				<input type="hidden" id="itemCountPerPage" name="itemCountPerPage" value="${searchMap.itemCountPerPage}"> 
				<input type="hidden" id="totalCnt" name="totalCnt" value="${searchMap.totalCnt}"> 
			
				<table class="table" style="border-color:#3492bf; border: 1.5px solid #3492bf;">
					<colgroup>
						<col style="width:10%" />
	                      	<col/>
	                      	<col style="width:10%" />
	                      	<col/>
					</colgroup>
					<tr>
						<th class="ac" style="background-color:#3492bf; color:#f7f36b;">제목</th>						
						<td ></td>						
						<th class="ac" style="background-color:#3492bf; color:#f7f36b;">작가</th>	
						<td></td>					
					</tr>
				</table>
			</form>
		</div>	
		<div>
			<button type="button" class="btn btn-primary" onclick="document.getElementById('searchFrm').submit();">검색</button>
		</div>			
	<table class="table table-hover" style="table-layout:fixed" id="gridResultList" excelUrl="board/selectList">
			<colgroup>
				<col style="width:10%" />
				<col style="width:10%" />
				<col style="width:*" />
				<col style="width:15%" />
			</colgroup>	
		  <tbody>
		   		<tr style="background-color:#3492bf; color:#f7f36b;">
			      <th class="ac" style="color:#f7f36b; font-weight:400;">No</th>
			      <th class="ac" style="color:#f7f36b;font-weight:400;">SN</th>
			      <th class="ac" style="color:#f7f36b; font-weight:400;">제목</th>
			      <th class="ac" style="color:#f7f36b; font-weight:400;">작성자</th>
			     <tr>
		  		<c:choose>
				<c:when test="${fn:length(boardList) <= 0 }">
					<tr>
						<td colspan="10" class="ac">데이터가 없습니다.</td>
					</tr>
				</c:when>
				<c:otherwise>
					<c:forEach var="board" items="${boardList}" varStatus="sttus">
						<tr>
							<td class="ac">${searchMap.totalCnt + 1 - ((searchMap.pageIndex-1) * searchMap.itemCountPerPage + sttus.index +1 )}</td>
							<td class="ac">${board.bbsSn}</td>
							<td class="ac" style="cursor:pointer;" onclick="loadDetail('${board.bbsSn}')">${board.sj}</td>
							<td class="ac">${board.korNm}</td>
						</tr>
					</c:forEach>
				</c:otherwise>
			</c:choose>
		  </tbody>
	</table>
		<div id="pagination">
			<!--  <nav style="width: fit-content; margin-left: auto; margin-right: auto;">
			  <ul class="pagination">
			    <li class="page-item disabled "><a class="page-link" href="#">&lt;</a></li>
			     <li class="page-item disabled"><a class="page-link" href="#">&lt;&lt;</a></li>
			    
			    <li class="page-item active"><a class="page-link" href="#">1</a></li>
			    <li class="page-item"><a class="page-link" href="#">2</a></li>
			    <li class="page-item"><a class="page-link" href="#">3</a></li>
			    
			    <li class="page-item"><a class="page-link" href="#">&gt;</a></li>
			    <li class="page-item"><a class="page-link" href="#">&gt;&gt;</a></li>
			  </ul>
			</nav>-->
		</div>
<script>
$(function(){
	pagination();
});


function loadDetail(bbsSn){
	
  $.ajax({
	    url: '/mana/manaDetail.do',
		async: true,
	    type: 'post',
	    data: { bbsSn: bbsSn},
	    dataType: 'html',
	    success: function(data) {
	    	console.log(data);
	    	oneS.adminGnb.layout == 1;
			$("#container").removeClass("on2");
			$("#container").addClass("on1");
	    	$('#detailView').html(data);
	    }
	  });
}

function loadCreate(bbsSn){
	  $.ajax({
		    url: '/mana/manaCreate.do',
			async: true,
		    type: 'post',
		    data: { bbsSn: bbsSn},
		    dataType: 'html',
		    success: function(data) {
		    	console.log(data);
		    	oneS.adminGnb.layout == 1;
				$("#container").removeClass("on2");
				$("#container").addClass("on1");
		    	$('#detailView').html(data);
		    }
		  });
}


function fn_excel(){
	
	var addData= [
		{ columnName : 'regDt', header: '등록일자', sorting: 'date', format : 'yyyy-MM-dd HH:mm:ss'}
	]
	
	excel.download(addData,'gridResultList','searchFrm',"테스트");
	
}

function search(){
	$("#searchFrm").submit();
}

function goToPage(pageIndex){
	$("#pageIndex").val(pageIndex);
	$("#searchFrm").submit();
}

function pagination(){
	var navigatorNum = 10 
	var pageIndex = parseInt($("#pageIndex").val());
	var totalCnt =  parseInt($("#totalCnt").val());
	var itemCountPerPage = parseInt($("#itemCountPerPage").val());
	var firstPageNum = 1;
	var prevPageNum = (pageIndex == 1 ? 1 : pageIndex - 1);
	var lastPageNum = Math.floor((totalCnt - 1) / itemCountPerPage) + 1;
	var nextPageNum = (pageIndex == lastPageNum ? lastPageNum : pageIndex + 1);
	var indexNum = (pageIndex <= navigatorNum ? 0 : parseInt((pageIndex - 1) / navigatorNum) * navigatorNum);
	
   var pagingHTML = "";    
	
   if(totalCnt >= 1){
	 
	   pagingHTML += '<nav style="width: fit-content; margin-left: auto; margin-right: auto;">';
 	   pagingHTML += '<ul class="pagination">';

	   if(pageIndex == firstPageNum){
			pagingHTML += '<li class="page-item disabled"><a class="page-link" href="#" onclick="goToPage('+firstPageNum+')">&lt;&lt;</a></li>';
			pagingHTML += '<li class="page-item disabled"><a class="page-link" href="#" onclick="goToPage('+prevPageNum+')">&lt;</a></li>';
	   }else{
		    pagingHTML += '<li class="page-item"><a class="page-link" href="#" onclick="goToPage('+firstPageNum+')">&lt;&lt;</a></li>';
		    pagingHTML += '<li class="page-item"><a class="page-link" href="#" onclick="goToPage('+prevPageNum+')">&lt;</a></li>';
	   }
 	
	   for (var i = 1; i <= navigatorNum; i++) {
           var pageNum = i + indexNum;

           if (pageNum > lastPageNum)
               continue;

           if (pageNum == pageIndex)
               pagingHTML +='<li class="page-item active"><a class="page-link" href="#" onclick="goToPage('+pageNum+')">'+pageNum+'</a></li>'
           else
        	   pagingHTML +='<li class="page-item"><a class="page-link" href="#" onclick="goToPage('+pageNum+')">'+pageNum+'</a></li>';
       }
	   
	   if(pageIndex == lastPageNum){
			pagingHTML += '<li class="page-item disabled"><a class="page-link" href="#" onclick="goToPage('+nextPageNum+')">&gt;</a></li>';
			pagingHTML += '<li class="page-item disabled"><a class="page-link" href="#" onclick="goToPage('+lastPageNum+')">&gt;&gt;</a></li>';
	   }else{
		   pagingHTML += '<li class="page-item"><a class="page-link" href="#" onclick="goToPage('+nextPageNum+')">&gt;</a></li>';
		   pagingHTML += '<li class="page-item"><a class="page-link" href="#" onclick="goToPage('+lastPageNum+')">&gt;&gt;</a></li>';
	   }
   }
   
   $("#pagination").html(pagingHTML);
   
}




</script>