<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.stream.JsonReader" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="Keywords" content="">
    <meta name="Description" content="">
    <meta name="Author" content="Yuance">
    <meta name="Copyright" content="">
    <title>Template Selection</title>
    <style type="text/css">
        body,dl,dd,dt,p,h1,h2,h3,h4,h5,h6{ margin: 0;}
        ul,ol{margin: 0; list-style: none; padding: 0;}
        a{ text-decoration: none; color:inherit; }
        *{ margin: 0; padding: 0; }
        a img{ display:block; border:none;}
        .clearfix:after{ content:""; display: block; clear: both;}
        .fl-l{ float: left;}
        .fl-r{ float: right;}

        input[type="button"]{
            background-color: #4CAF50;
            border: none;
            border-radius: 10px;
            color: white;
            padding: 15px 32px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;
        }

        #setting .nextButton{position: absolute; top:650px; left: 1130px}
        #setting .deleteTemplate{position: absolute; top: 350px; left:1000px}


        /*Upload Button*/
        .fileUpload {
            position: relative;
            overflow: hidden;
            margin: 10px;
        }

        .upload {
            position: absolute;
            top: 0;
            right: 0;
            margin: 0;
            padding: 0;
            font-size: 20px;
            cursor: pointer;
            opacity: 0;
            filter: alpha(opacity=0);
        }
        .fileUpload, .submit{
            background-color: #4CAF50;
            border: none;
            border-radius: 10px;
            color: white;
            padding: 15px 32px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;
        }

        #uploadFile{display: block; width: 500px; border:none; background: white}
        .uploadButton{position: relative; top: 300px; left: 1000px;}


        #title{width: 400px; margin-left: 50px; margin-top: 50px; font: 40px/70px "Arial Black";  height: 100px}
        #table{width:700px; margin-left: 100px; margin-top: 50px; border: 2px solid #CCCCCC}

        #header{height: 40px; color: cornflowerblue; border-bottom: 2px solid #CCCCCC}
        #header p{width:170px; font:20px/40px "Arial"; margin: auto;}

        .unit {height: 30px; font: 20px/30px ""; color: #666666; margin-bottom:3px; cursor: pointer}
        .unit a{padding-left: 10px}
        .unit div:hover{background: #ddeeff}
        .list ul li{height:30px; overflow: hidden; margin-bottom: 5px; transition:0.7s;}
        .list ul li dt:hover{background: #8dff8e;}
        .list ul li dt{margin-bottom: 2px;}
        .list ul li dt{background: #ffffff; font:20px/30px ""; display: block; text-align: center;
            color:#666666}
    </style>

</head>

<body>
<div id="main">
    <p id="title">Current Template</p>
    <div id="setting">
        <div class="deleteTemplate"><input type="button" value="Delete All Current Templates" onclick="deleteTemplate()"></div>
        <div class="nextButton"><input type="button" value="Proceed To Template CellGroup Mapping" onclick="templateSetting()"></div>
        <div class="uploadButton">
            <form method="post" action="UploadTemplateServlet" enctype="multipart/form-data">
                <div class="fileUpload">
                    <span>Upload</span>
                    <input id="uploadBtn" type="file" class="upload" name="file">
                </div>
                <input id="uploadFile" placeholder="Choose File" disabled="disabled">
                <input type="submit" class="submit" value="Submit">
            </form>
        </div>
    </div>
    <div id="table">
        <div id="header">
            <p>Saved Templates</p>
        </div>
        <div class="list">
            <ul>
            </ul>
        </div>
    </div>
</div>

<script src="http://apps.bdimg.com/libs/jquery/2.1.4/jquery.js"></script>

<script>
    document.getElementById("uploadBtn").onchange = function () {
        var filePath = $(this).val();
        if(filePath.indexOf("xlsx")!==-1 || filePath.indexOf("xls")!==-1) {
            var arr = filePath.split('\\');
            var fileName = arr[arr.length-1];
            document.getElementById("uploadFile").value = fileName;
        }
        else {
            document.getElementById("uploadFile").value = "type error"
        }
    };
</script>

<script>

    /*     loadexcel(); */

    loadexcel();

    function templateSetting(){
        //save the current choice of the sheet and the excel
        siteSheetName = "";
        cellSheetNames = [];
        excelName="";
        oli = document.querySelectorAll(".list ul li");
        for (i=0; i<oli.length; i++){
            if (oli[i].firstElementChild.style.backgroundColor.indexOf("rgb(221, 238, 255)") !== -1){
                //get the excel name
                excelName = oli[i].firstElementChild.firstElementChild.innerHTML;

                odt = oli[i].lastElementChild.firstElementChild;
                do{
                    if (odt.style.backgroundColor.indexOf("rgb(141, 255, 142)") !== -1){
                        cellSheetNames.push(odt.innerHTML)
                    }
                    if (odt.style.backgroundColor.indexOf("rgb(255, 107, 131)") !== -1){
                        siteSheetName = odt.innerHTML
                    }
                    odt = odt.nextElementSibling
                } while(odt)
            }
        }

        setting = {"excelName" : excelName, "siteSheetName": siteSheetName, "cellSheetNames": cellSheetNames};
        if (setting.excelName === "") {
            alert("Have not selected an excel to process..")
            return
        }
        setting = JSON.stringify(setting);
        console.log(setting);

        $.ajax({
            type:"post",
            url:"saveJsonServlet",
            data:{"setting" : setting, "location": "template"},
            beforeSend:function(){},
            success:function(msg){
            	console.log(msg);
                if (msg === "success"){
                    alert("Successfully passed the selected value, redirect to the cellGroup Checking...");
                       window.location.href = "cellGroupMapping.html"
                }
                else{
                    alert("Error while processing the selected data")
                }
            }
        })
    }

        function deleteTemplate(){
            $.ajax({
                type:"post",
                url:"delTempServlet",
                data:{},
                beforeSend:function(){},
                success:function(data){
                    if (data === "success")
                        window.location.href="currentTemplate.jsp";
                    else console.log("fail on initializing Template...")
                }
            });
        }

        function addListEvent(){

            oAL = document.querySelectorAll(".unit div a");
            oLi = document.querySelectorAll(".unit");
            console.log(oLi.length);
            var listLength = oLi.length;
            //        console.log(oAL.length);
            for (var i = 0; i < oAL.length; i++) {
                var oA = oAL[i];
                oA.flag = false;
                oA.index = i;
                oA.dtLength = oA.parentNode.nextElementSibling.childElementCount * 32 + 32;

                oA.parentNode.onclick = function () {
                    if (!this.firstElementChild.flag) {
                        for (var i = 0; i < listLength; i++) {
                            oLi[i].style.height = "30px";
                            oAL[i].parentNode.style.background = "";
                        }
                        oLi[this.firstElementChild.index].style.height = this.firstElementChild.dtLength + "px";
                        this.firstElementChild.parentNode.style.background = "#ddeeff";
                        this.firstElementChild.flag = true;
                    }
                    else {
                        for (var i = 0; i < listLength; i++) {
                            oLi[i].style.height = "30px";
                            oAL[i].parentNode.style.background = "";
                        }
                        this.firstElementChild.flag = false;
                    }
                }

            }
        }

        function addClickEvent(){

            odt = document.querySelectorAll(".list ul li dt");
            console.log(odt.length);

            for (i=0; i<odt.length; i++){
                odt[i].onclick = function(){

                    console.log(this.style.backgroundColor + ", " + window.getComputedStyle(this).backgroundColor);

                    if (this.innerHTML.indexOf("Nodeb") !== -1) {
                        this.style.backgroundColor = "#FF6B83";
                        return
                    }

                    //rgb(255, 107, 131) <=> #FF6B83
                    //rgb(141, 255, 142) <=> #8DFF8E
                    //rgb(255, 255, 255) <=> #FFFFFF
                    if (this.style.backgroundColor === "rgb(141, 255, 142)")
                        this.style.backgroundColor = "#FFFFFF";
                    else this.style.backgroundColor = "#8DFF8E";

                }
            }
        }
//    function addClickSheetEvent(json){
//        odt = document.querySelectorAll(".list ul li dt");
//        console.log(odt.length);
//        for(let i=0; i<odt.length; i++) {
//            odt[i].
//            json.siteSheetName
//        }
//    }
//    function loadCurrentChoice(){
//        $.ajax({
//            type:"post",
//            url:"loadJsonServlet",
//            data:{},
//            beforeSend:function{},
//            success:function(data){
//                json = JSON.parse(data);
//                console.log(json);
//                addClickSheetEvent(json)
//            }
//        })
//    }


        function loadexcel(){
            $.ajax({
                type:"post",
                url:"loadJsonServlet",
                data:{},
                beforeSend:function(){},
                success:function(data){
                    json = JSON.parse(data);
                    console.log(json);
                    loadTemplate(json)
                }
            });
        }
        function loadTemplate(json){
            oul = document.querySelector('.list ul');
            excelnum = json.excels.length;
            for(i=0; i<excelnum; i++){
                oli = document.createElement('li');
                oli.className = "unit";
                oli.appendChild(document.createElement('div'));
                div = oli.firstElementChild;
                div.appendChild(document.createElement('A'));
                oa = div.firstElementChild;
                oa.className = "hyperLink";
                oa.href = "javascript:";
                oa.innerHTML = json.excels[i].name;

                oli.appendChild(document.createElement('dl'));
                odl = div.nextElementSibling;
                sheetnum = json.excels[i].sheets.length;
                for(j=0; j<sheetnum; j++){
                    odl.appendChild(document.createElement('dt'));
                    odl.lastElementChild.innerHTML = json.excels[i].sheets[j];
                    console.log(encodeURIComponent(json.excels[i].sheets[j]) + ", " + encodeURIComponent(json.sheetNames.siteSheetName));
                    if(encodeURIComponent(json.excels[i].sheets[j]) === encodeURIComponent(json.sheetNames.siteSheetName))
                        odl.lastElementChild.style.backgroundColor = "#FF6B83";
                    for (k=0; k<json.sheetNames.cellSheetNames.length; k++)
                        if(encodeURIComponent(json.excels[i].sheets[j]) === encodeURIComponent(json.sheetNames.cellSheetNames[k]))
                            odl.lastElementChild.style.backgroundColor = "#8DFF8E"
                }
                oul.appendChild(oli);
            }
            addListEvent();
            addClickEvent()
        }

</script>
</body>
</html>