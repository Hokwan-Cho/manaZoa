<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

	<div style="display: inline-block; margin: 0 5px;  float: right;">
		<button type="button" class="btn btn-warning" onclick="loadCreate('${manaDetail.bbsSn}');">수정</button>
		<button type="button" class="btn btn-danger">삭제</button>
	</div>
	<br/><br/>
	<table class="table" style="border-color:#3492bf; border: 1.5px solid #3492bf;">
		<colgroup>
			<col style="width:10%;" />
                   	<col style="width:40%;"/>
                   	<col style="width:10%;" />
                   	<col style="widht:40%;"/>
		</colgroup>
		<tbody>
			<tr>
				<th class="ac" style="background-color:#3492bf; color:#f7f36b;">제목</th>						
				<td>${manaDetail.sj}</td>	
				<th class="ac" style="background-color:#3492bf; color:#f7f36b;">책장 위치</th>
				<td> D-17 </td>
			</tr>
			<tr>					
				<th class="ac" style="background-color:#3492bf; color:#f7f36b;">내용</th>	
				<td colspan="3">${manaDetail.bbsContents}</td>					
			</tr>
		</tbody>
	</table>


