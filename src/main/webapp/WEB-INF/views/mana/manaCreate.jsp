<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<style>
.upload-box {
  width: calc(50% - 15px);
  box-sizing: border-box;
  margin-right: 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.upload-box .drag-file {
  width: 100%;
  height: 360px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: 3px dashed #dbdbdb;
}
.upload-box .drag-file.highlight {
  border: 3px dashed red;
}
.upload-box .drag-file .image {
  width: 40px;
}
.upload-box .drag-file .message {
  margin-bottom: 0;
}


</style>

				
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
				<td><input name="sj" value="${manaCreate.sj}" /></td>	
				<th class="ac" style="background-color:#3492bf; color:#f7f36b;">책장 위치</th>
				<td> D-17 </td>
			</tr>
			<tr>					
				<th class="ac" style="background-color:#3492bf; color:#f7f36b;">내용</th>	
				<td colspan="3"><textarea>${manaCreate.bbsContents}</textarea></td>					
			</tr>
			<tr>
				<th class="ac" style="background-color:#3492bf; color:#f7f36b;">첨부파일</th>
				<td>
					<section id="ex9">
			        <div class="upload-box">
			            <button class="btn-upload" onclick="document.getElementById('fileBtn').click();">파일선택</button>
			            <input type="file" id="fileBtn" class="btn-file"  multiple > <!--파일 input box 형태-->     
			        </div>
			    </section>
					
				
				</td>
			</tr>
	
		</tbody>
	</table>

<script>
var sec9 = document.querySelector('#ex9');
var btnUpload = sec9.querySelector('.btn-upload');
var inputFile = sec9.querySelector('input[type="file"]');
var uploadBox = sec9.querySelector('.upload-box');

/* 박스 안에 Drag 들어왔을 때 */
uploadBox.addEventListener('dragenter', function(e) {
    console.log('dragenter');
});

/* 박스 안에 Drag를 하고 있을 때 */
uploadBox.addEventListener('dragover', function(e) {
    e.preventDefault();
    console.log('dragover');

    this.style.backgroundColor = 'green';
});

/* 박스 밖으로 Drag가 나갈 때 */
uploadBox.addEventListener('dragleave', function(e) {
    console.log('dragleave');

    this.style.backgroundColor = 'white';
});

/* 박스 안에서 Drag를 Drop했을 때 */
uploadBox.addEventListener('drop', function(e) {
    e.preventDefault();

    console.log('drop');
    this.style.backgroundColor = 'white';
});


uploadBox.addEventListener('drop', function(e) {
    e.preventDefault();

    console.log('drop');
    this.style.backgroundColor = 'white';
    
    console.dir(e.dataTransfer);
	var fileList = e.dataTransfer.files; 
	console.dir(fileList);
    var data = e.dataTransfer.files[0];
    console.dir(data);        
});


</script>
