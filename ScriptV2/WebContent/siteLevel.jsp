<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.huawei.bean.SiteLevelRule" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.stream.JsonReader" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="Keywords" content="">
    <meta name="Description" content="">
    <meta name="Author" content="Yuance">
    <meta name="Copyright" content="">
    <title>Cell Group Rule</title>
    <style type="text/css">
        body,dl,dd,dt,p,h1,h2,h3,h4,h5,h6{ margin: 0;}
        ul,ol{margin: 0; list-style: none; padding: 0;}
        a{ text-decoration: none; color:inherit; }
        *{ margin: 0; padding: 0; }
        a img{ display:block; border:none;}
        .clearfix:after{content:""; display: block; clear: both;}
        .fl-l{ float: left;}
        .fl-r{ float: right;}

        /*nav button*/
        .nav,.addbutton, .delvalbutton, .delattbutton{background-color: #4CAF50;
            border: none;
            border-radius: 10px;
            color: white;
            padding: 15px 32px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 26px;
            margin: 4px 2px;
            cursor: pointer;}

        #main{position: relative; margin: auto 50px}
        #title{width: 400px; margin-left: 50px; margin-top: 50px; font: 40px/70px "Arial Black"; height: 100px}
        /*setting block*/
        #setting .set{margin-left: 50px; padding: 30px}
        #setting .set .body div{margin-right: 30px;}
        #setting .set .body .value{margin-top: 30px; font: 20px/30px ""}
        #setting .set .body p{font:20px/30px ""; padding: 3px;}
        #setting .set .body input{width: 200px;  height:30px; padding: 5px; text-align: center; font:20px/30px ""}
        #setting .set .footer input{margin: 30px 30px 0 0}


        .addbutton, .delvalbutton, .delattbutton{padding: 10px 22px; font-size: 16px}

        #main #nav{margin-left: 50px;}
        #main #nav button{margin-left: 20px; margin-top: 100px}

    </style>

</head>
<body>
	
	<% 
		response.setHeader("Cache-Control","no-cache"); //HTTP 1.1 
		response.setHeader("Pragma","no-cache"); //HTTP 1.0 
		response.setDateHeader ("Expires", 1); //prevents caching at the proxy server 
	%> 
	
	
    <div id="main">
        <p id="title">Cell Group Rule</p>

        <!--Setting Block-->
        <div id="setting">
            


            <!--Add more sets here-->
            <div class="footer">
                <input type="button" class="addbutton addattbutton" value="Add Attribute">
            </div>

        </div>
        <div id="nav">
            <button class="nav" onclick="enableinput()">Edit</button>
            <button class="nav" onclick="saveconfig()">Save</button>
            <button class="nav" onclick="jumpNext()">Proceed To Cell Level Rule Setting</button>
        </div>

    </div>
    
<script src="http://apps.bdimg.com/libs/jquery/2.1.4/jquery.js"></script>    
<script>
    //dynamic selector
    addvaluebutton = document.querySelectorAll(".set .footer .addbutton");
    deleteValue = document.querySelectorAll(".set .footer .delvalbutton");
    deleteAttribute = document.querySelectorAll(".set .footer .delattbutton");
    
    addAttribute = document.querySelector(".addattbutton");


//    console.log(addvaluebutton.length);
//    console.log(deleteValue.length);
    //html code for a set
      html = 
            '    <div class="body">'
            +'            <div class="col fl-l">'
            +'                <p>Import From Column</p>'
            +'                <input type="text" name="col">'
            +'            </div>'
            +'            <div class="Attri fl-l">'
            +'                <p>Attribute Description</p>'
            +'                <input type="text" name="attri">'
            +'            </div>'
            +'            <div class="keyValue clearfix">'
            +'                <p>Key Value</p>'
            +'                <input type="text" name="keyVal">'
            +'            </div>'
            +'            <div class="value">'
            +'                Cell: <input type="text" name="val">'
    +'                        belongs to Group 1'
            +'            </div>'
            +'    </div>'
            +'    <div class="footer">'
            +'        <input class="addbutton fl-l" type="button" value="Add Group">'
            +'        <input class="delvalbutton fl-l" type="button" value="Delete Group">'
            +'        <input class="delattbutton clearfix" type="button" value="Delete Attribute">'
            +'    </div>';

    /* var json;
    var client = new XMLHttpRequest();
    client.open('GET', 'siteLevelRule.json');
    client.responseType(')
    client.send();
   	json = JSON.parse(client.responseText);
   	console.log(json); */
   	
    //Get Json from json file
    window.onload = load();
    
   	function jumpNext(){
   		window.location.href="cellLevel.jsp"
   	}
   	
    function load(){
        xml = new XMLHttpRequest();
   	
	    xml.open("GET", "RuleFiles/siteLevelRule.json?cb=" + Date.now(), true);
	
	    xml.onreadystatechange = function() {
	      if (xml.readyState !== 4)  { return; }
	      response = JSON.parse(xml.responseText);
		  console.log(response);
		  //initialization
		  loadSetting(response);
		  //disable the input
	      disableinput();
	    };
	    xml.send();
   	}
    
    //add and remove value button, deleteAttributeButton Initialization
    for(i=0; i<addvaluebutton.length; i++){
        addRemoveValueEvent(addvaluebutton[i], deleteValue[i]);
        deleteAttEvent(deleteAttribute[i])
    }

    //addAttribute button
    addAttribute.addEventListener('click',function(){
        let setting = this.parentNode;
        let div = document.createElement('div');
        div.className = "set";
        div.innerHTML = html;
        div = setting.insertBefore(div, setting.lastElementChild);
        //re-tie the events
        let footer = div.lastElementChild;
        addRemoveValueEvent(footer.firstElementChild, footer.firstElementChild.nextElementSibling);
        deleteAttEvent(footer.lastElementChild);
    });

    var addAttributeEvent = new Event('click');

    function saveconfig(){
        //Get the data String
        param = getOnPageData();
        console.log(param.setting);
        //using ajax to pass to servelet
        $.ajax({
            type:"post",
            url:"saveJsonServlet",
            data:{"setting": param.setting, "location": "site"},
            beforeSend:function(){},
            success:function(data){
            	console.log(data);
            	if (data === "success")
            		alert("The config has been updated!")
            }
        });
        disableinput()
    }

    function getOnPageData(){
        param = {};
        bodys = $(".body");
        jsonArr = [];
        for(i=0; i<bodys.length; i++){
            div = bodys[i].firstElementChild;
            json = {};
             //col
            json.column = div.lastElementChild.value;
            //attri
            div = div.nextElementSibling;
            json.attribute = div.lastElementChild.value;
            //keyVal
            div = div.nextElementSibling;
            json.KeyValue = div.lastElementChild.value;
            //values
            valuelen = bodys[i].childElementCount - 3;
            json.determinants = [];
            for(j=0; j<valuelen; j++){
                 div = div.nextElementSibling;
                 json.determinants.push(div.lastElementChild.value)
            }
            jsonArr.push(json);
        }
        param.setting = JSON.stringify(jsonArr);
        return param
    }
    
    function enableinput(){
        console.log("Enter event");
        inputs = document.querySelectorAll('input');
        for(i=0;i<inputs.length;i++)
            inputs[i].disabled = false;
        console.log("changed")
    }
    
    function disableinput(){
        inputs = document.querySelectorAll('input');
        for(i=0;i<inputs.length;i++)
            inputs[i].disabled = true
    }

    function addRemoveValueEvent(add, remove){
        //addEvent for add button
        add.addEventListener('click', function() {
                let curset = this.parentNode.parentNode;
                let div = curset.firstElementChild.lastElementChild;
                div.className = "value";
                let body = div.parentNode;
                let len = body.childElementCount;
                newValue = document.createElement('div');
                newValue.style.marginTop = "30px";
                newValue.style.font = '20px/30px ""';
                newValue.className = "value";
                newValue.innerHTML = 'Cell: <input type="text" width="50px"> belongs to Group' + (len - 2);
                body.appendChild(newValue)
            }
        );
        //addEvent for remove button
        remove.addEventListener('click', function(){
                let curset = this.parentNode.parentNode;
                let body = curset.firstElementChild;
                let div = body.lastElementChild;
                let len = body.childElementCount;
                if (len<5) alert('At Least One Value of One Attribute!');
                else {
                    div.previousElementSibling.className = "value clearfix";
                    body.removeChild(div);
                }
            }
        )
    }

    function deleteAttEvent(del){
        del.addEventListener('click', function(){
                let set = this.parentNode.parentNode;
                let setting = set.parentNode;
                setting.removeChild(set);
            }
        )
    }


    function loadSetting(json){
        len = json.setting.length;
        json = json.setting;
        for (i=0; i<len; i++) {
        	//initiate add attribute
            addAttribute.dispatchEvent(addAttributeEvent);

           	let sets = document.querySelectorAll('.set');
            let len = sets.length;
            let set = sets[len-1];
            let body = set.firstElementChild;
            let tag = body.firstElementChild;
            //Column
            tag.lastElementChild.value = json[i].column;
            tag = tag.nextElementSibling;
            //Attribute
            tag.lastElementChild.value = json[i].attribute;
            tag = tag.nextElementSibling;
            //KeyValue
            tag.lastElementChild.value = json[i].KeyValue;
            
            //loop the values
            curvalue = tag.nextElementSibling;
            arr = json[i].determinants;
            len = arr.length;
            curvalue.lastElementChild.value = arr[0];
            footer = set.lastElementChild;
            addbutton = footer.firstElementChild;
            for (j=1; j<len; j++) {
                addbutton.dispatchEvent(addAttributeEvent);
                curvalue = curvalue.nextElementSibling;
                curvalue.lastElementChild.value = arr[j];
            }
        }
    }


</script>
</body>
</html>
