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
    <title>Cell RF Data Input</title>
    <style type="text/css">
        body,dl,dd,dt,p,h1,h2,h3,h4,h5,h6{ margin: 0;}
        ul,ol{margin: 0; list-style: none; padding: 0;}
        a{ text-decoration: none; color:inherit; }
        *{ margin: 0; padding: 0; }
        a img{ display:block; border:none;}
        .clearfix:after{ content:""; display: block; clear: both}
        .fl-l{ float: left;}
        .fl-r{ float: right;}

        .fileUpload {
            position: relative;
            overflow: hidden;
            margin: 10px;
        }

        .fileUpload input.upload {
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

        #uploadFile{display: block; width: 500px}
        #uploadFile{position:relative; left: 400px; border: none; background: white}
        .uploadButton{position: absolute; top: 150px}
        .submit{position: relative; top: 350px}
		#title{width: 400px; margin-left: 50px; margin-top: 50px; font: 40px/70px "Arial Black";  height: 100px}

    </style>

</head>

<body>
    <p id="title">Cell RF Data Input</p>
    <div class="uploadButton">
        <form action="UploadDataServlet" method="post" enctype="multipart/form-data">
            <div class="fileUpload btn btn-primary">
                <span>Upload</span>
                <input id="uploadBtn" type="file" class="upload" name="file">
            </div>
            <input id="uploadFile" placeholder="Selected: " disabled="disabled">
            <input type="submit" class="submit" value="Start Generation">
        </form>
    </div>

<script src="http://apps.bdimg.com/libs/jquery/2.1.4/jquery.js"></script>
<script>
    document.getElementById("uploadBtn").onchange = function () {
        var filePath = $(this).val();
        if(filePath.indexOf("xlsx")!==-1 || filePath.indexOf("xls")!==-1) {
            var arr = filePath.split('\\');
            var fileName = arr[arr.length-1];
            document.getElementById("uploadFile").value = "Selected: " + fileName;
        }
        else {
            document.getElementById("uploadFile").value = "type error"
        }
    }
</script>
</body>
</html>
