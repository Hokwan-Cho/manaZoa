<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
 <head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
	<link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/resources/img/logo/favicon.png">

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
	
<link href="${pageContext.request.contextPath}/resources/assets/css/oneAdmin.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/resources/assets/jquery/jquery-3.6.0.min.js"></script>
<script src="${pageContext.request.contextPath}/resources/assets/jquery/jquery-ui-1.12.1/jquery-ui.js"></script>
<script src="${pageContext.request.contextPath}/resources/assets/js/oneAdmin.js"></script>
<script src="${pageContext.request.contextPath}/resources/assets/js/moment.js"></script>
<script src="${pageContext.request.contextPath}/resources/assets/js/sweetAlert.js"></script>
<script src="${pageContext.request.contextPath}/resources/assets/js/commonUtil.js"></script>

    <title>MANAZOA</title>

  </head>


	<style>
		.container {
  max-width: 960px;
}

/*
 * Custom translucent site header
 */
/*rgb(247, 243, 107);*/
.site-header {
  background-color:rgb(42, 118, 154);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  backdrop-filter: saturate(180%) blur(20px);
}
.site-header a {
  color: rgb(247, 243, 107) !important;
  transition: color .15s ease-in-out;
}
.site-header a:hover {
  color: #fff;
  text-decoration: none;
}

/*
 * Dummy devices (replace them with your own or something else entirely!)
 */

.product-device {
  position: absolute;
  right: 10%;
  bottom: -30%;
  width: 300px;
  height: 540px;
  background-color: #333;
  border-radius: 21px;
  transform: rotate(30deg);
}

.product-device::before {
  position: absolute;
  top: 10%;
  right: 10px;
  bottom: 10%;
  left: 10px;
  content: "";
  background-color: rgba(255, 255, 255, .1);
  border-radius: 5px;
}

.product-device-2 {
  top: -25%;
  right: auto;
  bottom: 0;
  left: 5%;
  background-color: #e5e5e5;
}


/*
 * Extra utilities
 */

.flex-equal > * {
  flex: 1;
}
@media (min-width: 768px) {
  .flex-md-equal > * {
    flex: 1;
  }
}
		
.dropdown-item:focus, .dropdown-item:hover {
    color: #1e2125;
    background-color: rgb(52 146 191);
}
li {
    list-style-type: none;
}	
		
.logo-box {border-radius:3px;;width:200px;display: inline-block;text-align:center; color:rgb(247, 243, 107);}
.logo-box img {height:40px}		
		
		

/*특정 부분 스크롤바 없애기*/
.adminConLeft{-ms-overflow-style: none;}
.adminConLeft::-webkit-scrollbar{display:none;}
#detailView{   -ms-overflow-style: none;}
#detailView{-ms-overflow-style: none;}


.table>:not(:first-child) {
    border-top: none;
}


.page-item.active .page-link {
    z-index: 3;
    color: #f7f36b;
    background-color: #3492bf;
    border-color: #3492bf;
}
.page-link {
	color: #2a769a;
}

.table{
  width: -webkit-fill-available;
}


</style>


<script>

$(function() {
	try{
		oneS.adminGnb(); //레이아웃을 위한 최초 스크립트
		oneS.tabS(".lineTabBox", ".lineTabCon", "aaa");// tabS(텝엘리먼트,콘텐츠엘리먼트,외부스크립트명(index)) 텝 스크립트 필요시에만 사용
		$('.tableWrap').scrollbar();
		$('.adminConLeft').scrollbar();
		$('.rightCon').scrollbar();
	}catch(Exception){}
});

</script>
 <body>
	<%@ include file="/WEB-INF/views/gnb.jsp" %>

	
	<!-- content -->
		<div class="adminContetLayout on2" id="container">
			<!-- v2_left -->
			<div class="adminConLeft" style="overflow-y:scroll; padding:15px;" >
			
			
				<jsp:include page="/WEB-INF/views/mana/${viewPath}.jsp"></jsp:include>
			
			
				<div class="widthChange_opacity"></div>
					<button href="javascript:;" class="widthChange">
						<span class="icon-prev"></span>
					</button>
				<div class="adminConLeftIn"></div>
			</div>
			
			<!-- v2_right -->
			<div class="rightCon">
				<div class="adminConRight">
				
					<div class="widthChange2_opacity"> </div>
					<button href="javascript:;" class="widthChange2">
						<span class="icon-next"></span>
					</button>
					<div class="adminConRightIn" id="detailView"  style="overflow-y:scroll;">
						
						
					
							
					      
					</div>
				</div>
			</div>
		</div>
    <!-- Optional JavaScript; choose one of the two! -->

    <!-- Option 1: Bootstrap Bundle with Popper -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>

    <!-- Option 2: Separate Popper and Bootstrap JS -->
    <!--
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.10.2/dist/umd/popper.min.js" integrity="sha384-7+zCNj/IqJ95wo16oMtfsKbZ9ccEh31eOz1HGyDuCQ6wgnyJNSYdrPa03rtR1zdB" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.min.js" integrity="sha384-QJHtvGhmr9XOIpI6YVutG+2QOK9T+ZnN4kzFN1RtK3zEFEIsxhlmWl5/YESvpZ13" crossorigin="anonymous"></script>
    -->
    
   <footer class="footer" style="background-color: lightgray; height:50px;">
	    <div style="height:50px;">
    	   <div style="margin-left:auto;margin-right:auto; width: fit-content; position:relative; top:13px; font-size:14px;" >
		    	Copyright © MANAZOA All rights reserved.<br/>
		    </div>
	    </div>
  </footer>
    
  </body>
 
</html>