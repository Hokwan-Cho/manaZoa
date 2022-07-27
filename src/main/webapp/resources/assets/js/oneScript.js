/* guide 0.1 (20.11.17) ***************
oneS.gnb() // 기본 메뉴 체계를 이루는 스크립트
oneS.subGnb() // Sub 관련 메뉴 스크립트
oneS.form() // 폼 관련 스크립트
oneS.popup(n,m) // n : open,close / m : jquery Eelement
**************************************/
oneS = {
  iframeH : function(n){
  	var fileForm= document.getElementById(n);
 	document.getElementById(n).style.height = fileForm.contentWindow.document.body.scrollHeight;
  },
  gnb : function(){//gnb script
    $(".oneGnbList > li > a").on("mouseenter",function(){
      $(".oneGnbList > li > ul").stop().slideUp();
      $(this).next("ul").stop().slideDown();
    });
    $(".oneGnbList").on("mouseleave",function(){
      $(".oneGnbList > li > ul").stop().slideUp();
    });
    if($("body").find(".oneSubLayout").length > 0){
      $(".oneHeadLayout").addClass("subPage");
    }
  },
  subGnb : function(){
    $(".oneSubNaviIn > ul > li").each(function(){
      if($(this).find("li").length > 0){
        $(this).find("> a").append('<span class="listIcon icon-down"></span>');
        $(this).find("> a").on("click",function(){
          if($(this).next("ul").css("display") === "none"){
            $(this).parent().addClass("on");
            $(this).next("ul").slideDown();
          }else{
            $(this).parent().removeClass("on");
            $(this).next("ul").slideUp();
          }
        });
      }
    });
  },
  form : function(){//form
    $("input").each(function(){
      if($(this).is("[placeholder]")){
        thisOffN = $(this).position();
        $(this).before("<div class='inputLable'>"+$(this).attr("placeholder")+"</div>");
        $(this).on("focus",function(){
          thisOffN = $(this).position();
          $(this).prev(".inputLable").css("top",thisOffN.top).css("left",thisOffN.left).addClass("on");
        });
        $(this).on("blur",function(){
          $(this).prev(".inputLable").removeClass("on");
        });
      }
    });
  },
  qnaS : function(){
    $(".qnaList > dl > dt > a").on("click",function(){
      thisN = $(this).parent();
      if(thisN.hasClass("on")){
        thisN.removeClass("on");
        thisN.next("dd").slideUp();
      } else{
        thisN.parent().find("dt").removeClass("on");
        thisN.addClass("on");
        thisN.parent().find("dd").slideUp();
        thisN.next("dd").slideDown();
      }
    });
  },
  popup : function(n,m){//popup
    if(n === "open"){
      $(m).addClass("on");
    }else if(n === "close"){
      $(m).removeClass("on");
    }
  },
  showHide : function(n,m){
    if($(n).css("display") === "none"){
      $(n).slideDown();
      $(m).addClass("on");
    }else{
      $(n).slideUp();
      $(m).removeClass("on");
    }
  },
  tableS : function(){
    $(".reShow").on("click",function(){
      if($(this).hasClass("on")){
        $(this).closest(".reListBottom").find(".reListBottomList").slideUp();
        $(this).removeClass("on");
      }else{
        $(this).closest(".reListBottom").find(".reListBottomList").slideDown();
        $(this).addClass("on");
      }
      
    });
  },
  sign : function(){
    var isDrawing = false;
    var x = 0;
    var y = 0;

    var canvas = document.getElementById("sign");
    var ctx = canvas.getContext("2d");

    canvas.addEventListener("mousedown", function (e) {
        x = e.offsetX;
        y = e.offsetY;
        isDrawing = true;
    });

    canvas.addEventListener("mousemove", function (e) {
        if (isDrawing) {
            drawSign(ctx, x, y, e.offsetX, e.offsetY);
            x = e.offsetX;
            y = e.offsetY;
        }
    });

    canvas.addEventListener("mouseup", function (e) {
        if (isDrawing) {
            drawSign(ctx, x, y, e.offsetX, e.offsetY);
            x = 0;
            y = 0;
            isDrawing = false;
        }
    });

    canvas.addEventListener("mouseleave", function (e) {
        x = 0;
        y = 0;
        isDrawing = false;
    });

    document.getElementById("erase").addEventListener("click", function (e) {
        if (ctx != null) {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
        }
    })

    function drawSign(ctx, x1, y1, x2, y2) {
        if (ctx != null) {
            ctx.save();
            ctx.beginPath();
            ctx.strokeStyle = "#000";
            ctx.lineWidth = 1;
            ctx.moveTo(x1, y1);
            ctx.lineTo(x2, y2);
            ctx.stroke();
            ctx.closePath();
            ctx.restore();
        }
    }
  }
}

$(window).scroll(function(){
  $(this).scrollTop() > 60 ? $("body").addClass("scroll") : $("body").removeClass("scroll");
});