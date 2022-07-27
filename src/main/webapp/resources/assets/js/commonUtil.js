


$.datepicker.setDefaults({
	dateFormat : 'yymmdd',
	prevText : '이전 달',
	nextText : '다음 달',
	monthNames : [ '1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월',
			'9월', '10월', '11월', '12월' ],
	monthNamesShort : [ '1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월',
			'9월', '10월', '11월', '12월' ],
	dayNames : [ '일', '월', '화', '수', '목', '금', '토' ],
	dayNamesShort : [ '일', '월', '화', '수', '목', '금', '토' ],
	dayNamesMin : [ '일', '월', '화', '수', '목', '금', '토' ],
	showMonthAfterYear : true,	
    showButtonPanel: true,
	changeYear : true,
	changeMonth : true,
	yearRange: "-100:+2",
	yearSuffix : '년'
});

function datePicker(){
	$("input.datePicker").datepicker({
		defaultDate : "+1w",
		changeYear : true,
		changeMonth : true,
		numberOfMonths : 1,
		showOn : 'button',
		buttonImage :  "/rpms/assets/img/admin/detepickerImg.png",
	});
}


var ajax = {};
ajax.startLoading = function(){
	$.blockUI({message : "<img src='/rpms/assets/img/loading3.gif' alt='처리중입니다.' width='70px' />"});
	setTimeout("console.log('loading 강제 종료');ajax.endLoading()", 1000 * 10); 
}
ajax.endLoading = function(){	
	$.unblockUI();
}

/**
 * html 을 response 받을때 사용
 */
ajax.getHtml = function(url, jsonData, callback){
	$.ajax({
		type: "POST",
		url: url,
		data: jsonData,
	  	dataType: "html",	  
	  	beforeSend : function(xhr){
	  		var token = $("meta[name='_csrf']").attr("content");
	    	var header = $("meta[name='_csrf_header']").attr("content");
			if(token && header){
				xhr.setRequestHeader(header, token);
	  			ajax.startLoading();	
			}	    	
	  	},
	  	error: function(request, status, error){
	  		ajax.endLoading();
	  		if(request.status == "401"){
	  			console.log("접근 권한 정보가 없습니다.");
	  		}
	  		console.log("code:"+request.status+"\n"+"error:"+error);
	  	},
		success: function(result)
		{
			callback(result);
			$.unblockUI();
		}
	});	
}


ajax.submit = function(url, jsonData, callback)
{
	$.ajax(
	{
		type: "POST",
		url: url,
		data: jsonData,
	  	dataType: "json",
	  	beforeSend : function(xhr){
	  		var token = $("meta[name='_csrf']").attr("content");
	    	var header = $("meta[name='_csrf_header']").attr("content");
			if(token && header){
	    		xhr.setRequestHeader(header, token);
	  			ajax.startLoading();
			}
	  	},
	  	error: function(request, status, error){
	  		ajax.endLoading();
	  		alert("시스템 실패입니다. 관리자에게 문의하시기 바랍니다.\n 에러 내용 : \n code:"+request.status+"\n"+"error:"+error);	
	  	},
		success: function(result){	
			var va = result.validationResponse; 
			if(va.processStatus === "VALIDATIONERROR"){
				alert("입력값이 없는 항목이 있거나 잘못된 데이터를 등록시도 하였습니다.");
			}else if (va.processStatus == "SYSTEMFAILED"){
				var errorMessageList = result.validationResponse.errorMessageList;
				if(errorMessageList.length > 0){
					var msg = "";
					for(i = 0; i < errorMessageList.length; i++){
						msg += errorMessageList[i] + "\n";
					}
					alert(msg);
				}else{
					alert("작업중 에러가 발생하였습니다.");
				}
			}else{
				callback(result);
			}		
			ajax.endLoading();
		}
	});
	try{initLogoutTimer()}catch(Exception){}
}

ajax.submitForm = function(url, formId, callback){
	ajax.submit(url, $("#" + formId).serialize(), callback)
}

/**
 * 쿠키 함수
 */
var cookie = {}
cookie.set = function( name, value, expiredays )
{
	var todayDate = new Date();
	todayDate.setDate( todayDate.getDate() + expiredays );
	todayDate.setHours(0, 0, -1);
	document.cookie = name + "=" + escape( value ) + "; path=/; expires=" + todayDate.toGMTString() + ";"
}

cookie.get = function(name)
{
	var nameOfCookie = name + '=';
	var x = 0;
	while ( x <= document.cookie.length )
	{
			var y = (x+nameOfCookie.length);
			if ( document.cookie.substring( x, y ) == nameOfCookie ) {
					if ( (endOfCookie=document.cookie.indexOf( ';', y )) == -1 )
							endOfCookie = document.cookie.length;
					return unescape( document.cookie.substring( y, endOfCookie ) );
			}
			x = document.cookie.indexOf( ' ', x ) + 1;
			if ( x == 0 )
					break;
	}
	return '';
}

/**
 * 팝업창
 */
var popup = {};
popup.open = function(url, w, h, title, dataParams, scroll)
{
	var left = $(window).width()/2 - w/2;
    var top = $(window).height()/2 - h/2;
    if(dataParams){
	    var win = window.open("", title, "location=no, directories=no, width="+w+",height="+h+",resizable=1,scrollbars="+scroll+", top=" + top + ", left=" + left);
	    if(!dataParams.formName){
	    	dataParams.formName = "popForm";
	    	$("#popForm").remove();
		    $("body").append("<form id='popForm' name='popForm' method='post' url='"+url+"'></form>");
		    $.each(dataParams, function (key, data) {
		       $('<input>').attr({
		    	    type: 'hidden',
		    	    id: key,
		    	    name: key,
		    	    value: data
		    	}).appendTo('#popForm');
		    });
	    }
	    $("#"+dataParams.formName).prop("target", title);
	    $("#"+dataParams.formName).prop("action", url);
	    $("#"+dataParams.formName).submit();
    }else{
    	var win = window.open(url, title, "location=no, directories=no, width="+w+",height="+h+",resizable=1,scrollbars="+scroll+", top=" + top + ", left=" + left);
    }
    return win;
}

popup.openScroll = function(url,w,h, windowName, dataParams) {
	return this.open(url, w, h, windowName, dataParams, "yes");
}

popup.openNoScroll = function (url,w,h, windowName, dataParams) {
	return this.open(url, w, h, windowName, dataParams, "no");
}

popup.openAutoScroll = function (url,w,h, windowName, dataParams) {
	return this.open(url, w, h, windowName, dataParams, "auto");
}

function pagination(blockId, page, totalCount, pageBlock, navigatorNum, functionCall) {
	page = parseInt(page, 10);
	totalCount = parseInt(totalCount, 10);
	pageBlock = parseInt(pageBlock, 10); // 페이지당 개시물 수
	navigatorNum = parseInt(navigatorNum, 10); //페이지 표시 개수
    var pagingHTML = "";    
    var firstPageNum = 1;
    var lastPageNum = Math.floor((totalCount - 1) / pageBlock) + 1;
    var prevPageNum = page == 1 ? 1 : page - 1;
    var nextPageNum = page == lastPageNum ? lastPageNum : page + 1;
    var indexNum = page <= navigatorNum ? 0 : parseInt((page - 1) / navigatorNum) * navigatorNum;



    if (totalCount >= 1) {
                	
        pagingHTML += "<a href=\"#none\" pageNum='" + prevPageNum + "' class=\"buBox\"><span class=\"icon-prev3\"></span></a>";
        
        for (var i = 1; i <= navigatorNum; i++) {
            var pageNum = i + indexNum;

            if (pageNum > lastPageNum)
                continue;

            if (pageNum == page)
                pagingHTML += "<strong>"+pageNum+"</strong>";
            else
                pagingHTML += "<a href='#none' pageNum='" + pageNum + "'><span>" + pageNum + "</span></a>";
        }

        
        pagingHTML += "<a href=\"#none\"  pageNum='" + nextPageNum + "' class=\"buBox\"><span class=\"icon-next3\"></span></a>";
        
    }
    $("#" + blockId).html(pagingHTML);    
    $("#" + blockId + " a").click(function (e) {
    functionCall($(this).attr('pageNum'));
    });
};

var modal = {
	open : function(modalId, url, modalWidth, frameHeight, frameScroll){
		$('#'+modalId).remove();
		var modalBox = "<div id='"+modalId+"' class='modal' style='-webkit-border-radius:0;padding:0;display:none;z-index:101;width:"+modalWidth+"px;'><div style='width:100%'>";
		modalBox += "<iframe width='100%' height='"+frameHeight+"px' src='"+url+"' frameborder='none' scrolling='"+frameScroll+"'></iframe>";
		modalBox += "<div class='buttonBoxR' style='margin:5px 2px'>";
		modalBox += "<button id='closeBtn' class=\"bu_s_gray\" onclick=\"$('#"+modalId+"').find('iframe').attr('src', 'about:blank');$('.modal_close').click();\">창 닫기</button>";
		modalBox += "<a href=\"#\" rel=\"modal:close\" class=\"modal_close\"></a>";
		modalBox += "</div></div>";
		console.log(modalBox)
		$("body").append(modalBox);
		$('#'+modalId).modal({
			escapeClose: false,
			clickClose: false,
			showClose: false
		});
	},
	juso : function(inputPrefix){
		modal.open("jusoPop", "/rpms/common/view/postSearch.do?inputPrefix="+inputPrefix, "700", "670", "no");		
	}
}

var Func = {
	getForm : function(formId){
		if(formId){
			return $('#' + formId);
		}else{
			return $('#searchFrm');
		}
	},
	search : function(pageIndex){
		try{
			event.preventDefault();	
		}catch(Exception){}
		if(pageIndex){
			Func.getForm().find("input[name='pageIndex']").val(pageIndex);
		}
		ajax.getHtml("list.do", Func.getForm().serialize(), function(res){
			$("#leftList").hide().html(res).fadeIn();
		})
	},
	downloadExcel : function(filename){
		try{
			event.preventDefault();	
		}catch(Exception){}
		alert("구현되어 있지 않음")
	},
	view : function(paramName, pkNumber){
		//상세보기 페이지 없음 ==> 수정페이지로 바로 이동	
		this.modify(paramName, pkNumber);
	},
	form : function(){
		try{
			event.preventDefault();	
		}catch(Exception){}
		ajax.getHtml("form.do", this.getForm().serializeArray(), function(res){
			$("#rightView").hide().html(res).fadeIn();
		})
	},
	modify : function(paramName, pkNumber){	
		try{
			event.preventDefault();	
		}catch(Exception){}
		
		var data = this.getForm().serializeArray(); 
		data.push({name: paramName, value: pkNumber});	
		ajax.getHtml("modify.do", $.param(data), function(res){
			$("#rightView").hide().html(res).fadeIn();
		})
	},
	regProcess : function(formId, callback){
		try{
			event.preventDefault();	
		}catch(Exception){}
		
		var data = $('#'+formId).serializeArray();
		ajax.submitForm("process.do", formId, function(res){				
			callback();
		})
	},
	initFormFieldRequired : function(){
		$("input.required, select.required").each(function(item, obj){
			var placeHolderText = $(obj).attr("placeholder");
			if(placeHolderText == null){
				placeHolderText = "";
			}
			$(obj).attr("placeholder", placeHolderText + "(필수)");
		});
	},
	initListClickEffect : function(pkParamName, tbodyClass, clickColor){
		if(!tbodyClass){
			tbodyClass = "dataListBody";
		}
		if(!clickColor){
			clickColor = "lightyellow";
		}
		$("."+tbodyClass+">tr").click(function(){
			$("."+tbodyClass+">tr").css("background-color", "#fff");
			$(this).css("background-color", clickColor);
			Func.view(pkParamName, $(this).attr("data-seq"));
		})
	}
}

var excel = {};
excel.downloadConfirm = function(addData, tableId, formId, filename){
	if(!tableId){
		tableId = "gridResultList";
	}
	if(!formId){
		formId = "moveFrm";
	}
	
	var tableSelector = "#" + tableId;
	var formSelector = "#" + formId;
	var fileName = "";
	var excelUrl = "";
	var columnList = [];
	
	fileName = filename;
	if(excelUrl != "stats06"){
		excelUrl = $(tableSelector).attr("excelUrl");
	}
	if(filename == ""){
		fileName = $(tableSelector).attr("excelFileName");
	}
	if(excelUrl == ""){
		alert("엑셀 다운로드 url 지정 오류");
		return;
	}

	if(addData){
		$(addData).each(function(index, item){			
			columnList.push( item );	
		});
	}
	
	$(formSelector+" input[name='gridConfigData']").remove();
	$(formSelector+" input[name='excelFileName']").remove();     
	$(formSelector+" input[name='mbtlno']").remove();     
    $(formSelector).append( $("<input type='hidden' name='gridConfigData' />").val( JSON.stringify(columnList) ) );
    $(formSelector).append( $("<input type='hidden' name='excelFileName' />").val(fileName) ) ;
    $(formSelector).append( $("<input type='hidden' name='currentUrl' />").val("/rest/grid/"+excelUrl+".do") ) ;
	$(formSelector).attr("method", "post");
	$(formSelector).attr("action", "/rest/grid/"+excelUrl+"/excel.do");
	
	var param = { formSelector: formSelector };
	
	popup.open("/excel/mbtlnoCheckPopup.do", "475", "270", "mbtlnoCheckPopup", param, "yes");
	
	
	//$(formSelector).submit();
	

	
}


excel.download = function(addData, tableId, formId, filename){
	if(!tableId){
		tableId = "gridResultList";
	}
	if(!formId){
		formId = "moveFrm";
	}
	
	var tableSelector = "#" + tableId;
	var formSelector = "#" + formId;
	var fileName = "";
	var excelUrl = "";
	var columnList = [];
	
	fileName = filename;
	if(excelUrl != "stats06"){
		excelUrl = $(tableSelector).attr("excelUrl");
	}
	if(filename == ""){
		fileName = $(tableSelector).attr("excelFileName");
	}
	if(excelUrl == ""){
		alert("엑셀 다운로드 url 지정 오류");
		return;
	}

	if(addData){
		$(addData).each(function(index, item){			
			columnList.push( item );	
		});
	}
	
	$(formSelector+" input[name='gridConfigData']").remove();
	$(formSelector+" input[name='excelFileName']").remove();     
	$(formSelector+" input[name='mbtlno']").remove();     
    $(formSelector).append( $("<input type='hidden' name='mbtlno' />").val("")) ;
    $(formSelector).append( $("<input type='hidden' name='gridConfigData' />").val( JSON.stringify(columnList) ) );
    $(formSelector).append( $("<input type='hidden' name='excelFileName' />").val(fileName ) ) ;
    $(formSelector).append( $("<input type='hidden' name='currentUrl' />").val("/rest/grid/"+excelUrl+".do") ) ;
	$(formSelector).attr("accept-charset", "UTF-8");
    
	$(formSelector).attr("method", "post");
	$(formSelector).attr("action", "/rest/grid/"+excelUrl+"/excel.do");
	
	$(formSelector).submit();
	
}


// replaceAll 추가 
String.prototype.replaceAll = function( searchStr, replaceStr )
{
	var temp = this;

	while( temp.indexOf( searchStr ) != -1 )
	{
		temp = temp.replace( searchStr, replaceStr );
	}

	return temp;
}



// replaceAt 추가 (원하는 문자열 index를 대체값으로 바꿔줌) 

String.prototype.replaceAt = function( index, character){
 return this.substr(0,index) + character + this.substr(index+character.length);
}


// datePicker 날짜선택 (1주일, 한달, 3개월, 초기화)  

$(document).ready(function(){
	$(".carlendar_btn").click(function(){		
		set_startEndDt($(this).attr("class"));
    });
	set_startEndDt("threemonth");
});


function nvl(value){

	if( value == null){
		return "";
	}else if (typeof(value) == "undefined"){
		return "";
	}else{
		return value;
	}

}

function set_startEndDt(cssClass){
	var format = "YYYY-MM-DD";
	var stDay = moment().format(format);
	var edDay = moment().format(format);
	
	if(cssClass.indexOf("aweek") >=0){
		stDay = moment().add(-1, "week").format(format);
	}else if(cssClass.indexOf("amonth") >=0){
		stDay = moment().add(-1, "month").format(format);
	}else if(cssClass.indexOf("threemonth") >=0){
		stDay = moment().add(-3, "month").format(format);
	}
	if(cssClass.indexOf("cancel") >=0){
		$(".startDt").val("");
		$(".endDt").val("");
	}else if(cssClass.indexOf("victimResultAll") >=0){
		$(".startDt").val("");
		$(".endDt").val("");
		$("#searchResultCnd").val("");
		$("#searchFetus").val("");
		$("#searchResultCnd").val("");
		$("#searchAsthma").val("");
		$("#searchWrd").val("");
		$("input[name=schReqstdocSe]").attr('checked',false);
	}else{
		$(".startDt").val(stDay);
		$(".endDt").val(edDay);
	}
}


//PDF 저장
function savePDF(formData){ 
	console.log('commonUtils savePDF', formData);
	
	var success = false; // pdf 저장 성공여부 
	$.ajax({
	    url: "/user/pdf/savePDF.do",
	    type: 'POST',
	    data: formData,  
	    processData: false,  // formData가 문자열로 변환되지 않기 위해 
	    contentType: false,  // multipart/form-data로 전송되게 boundary string 설정해줌  
	    async: false,
	    timeout: 10000000,
	    success: function(result){
			console.log('pdf저장완료', result.success); 
			success = result.success;
	    }
	});
	return success;
}

