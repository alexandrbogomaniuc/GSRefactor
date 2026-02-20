var req;
var req1;
var isIE;
var b;
var xml;
var cellCount=0;
var destServletUrl="";
var iServletUrl = "";
function initRequest(){
	        	var xmlHttp;
	    		try{  
	    			xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");    						
	    			}catch (e){
	    				try{
	    					xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");    							
	    				}catch (e){
	    					try{
	    						xmlHttp = new XMLHttpRequest();   							    						
	    						}catch (e){
	    							alert("Your browser does not support AJAX!");
	    							return false;
	    						}
	    				}
	    			}
	    		return xmlHttp;
	        }

function init2(a,c,d) {

	b=a;
	destServletUrl="http://"+c+"/SlotTestServlet";
	iServletUrl = d+"/SlotTestIntermediateServlet";
//	alert(destServletUrl);
//	alert(iServletUrl);
	init();


}



function init() {

	
	var data="destServletUrl="+destServletUrl+"&CMD=GETGAMEINFO&SID=" + b;
	
	req = initRequest();
	
	var str = document.getElementById("gamename-field").innerHTML;
	if(str.indexOf("Not any response")!=-1)
			data+="&required=true"
        req.open("POST", iServletUrl);
//	alert(data);
	req.onreadystatechange = callback;
	req.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
        req.send(data);
	
    
}

function sendData() {
    var x=document.getElementById("available-states");
	if(x.selectedIndex!=null && x.selectedIndex >= 0) {
	var data = "destServletUrl="+destServletUrl+"&CMD=POSTSTATE&SID=" + b + "&STATE=" + x.options[x.selectedIndex].text;
	req1 = initRequest();
	req1.onreadystatechange = checkResult;
    req1.open("POST", iServletUrl);
	req1.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    req1.send(data);
	} else alert("No any selected items");
	
}

function callback() {
    if (req.readyState == 4) {
        if (req.status == 200) {
//			alert("callback");
			document.getElementById("result").innerHTML="   ";
			if(document.getElementById("targetText"))
			document.getElementById("targetText").innerHTML=" ";
            parseMessages(req.responseXML);
			init();
        } 

    }
	
	
}
function checkResult() {
    if (req1.readyState == 4) {
        if (req1.status == 200) {
            document.getElementById("result").innerHTML=req1.responseText; 
        } 
    }
	
	
}


function parseMessages(responseXML) {
    removeAllOptions();
    var info = responseXML.getElementsByTagName("gameinfo")[0];
	if(info !=null) {
	var name=info.getElementsByTagName("name")[0];
	if(name!=null) {
	document.getElementById("gamename-field").innerHTML=name.childNodes[0].nodeValue;
	}
	var states=info.getElementsByTagName("states")[0];

	if (states != null ) {    

		 for (loop = 0; loop < states.childNodes.length; loop++) {
        var state = states.childNodes[loop];
		insertOption(state.childNodes[0].nodeValue);        
    }
 }
}
	parseReels(responseXML);
}

/*

<reels>
	<reel>
		<cell>
			<icon>WARRIOR</icon>
			<icon>FEMALE_SH</icon>
			<icon>FEMALE_CH</icon>
			<icon>JAGUAR</icon>
			<icon>PYRAMID</icon>
			<icon>EMERALD_GEM</icon>
			<icon>RUBY_GEM</icon>
			<icon>GOLDEN_BOWL</icon>
			<icon>GECKO</icon>
			<icon>LOVE_HUT</icon>
			<icon>GEMSTONE</icon>
			<icon>SPEARMAN</icon>
			<icon>CALENDAR</icon>
		</cell>
	</reel>
</reels>

*/
function parseReels(xml) {
	cellCount=0;
	var reels,reel,cell,icon,iconValue;
	reels=xml.getElementsByTagName("reels")[0];
	if(reels!=null) {
		var resultTable = document.createElement("table");
		resultTable.setAttribute("border","1");
		resultTable.setAttribute("id","reels");
		
		//var line = document.createElement("tr");
		//var tableCell = document.createElement("td");
		//var text = document.createTextNode("success");
		//tableCell.appendChild(text);
		//line.appendChild(tableCell);
		//resultTable.appendChild(line);
		
		var lines=new Array();
		var rowsCount = reels.firstChild.childNodes.length;
		var z=0;
		var resetButtons=document.createElement("tr");
		for(z=0;z<rowsCount;z++) {
			var row=document.createElement("tr");
			
			//document.getElementById("debugDiv").innerHTML="Out of the cycle step1";
			
			var loopIndex1=0;
			for(loopIndex1=0;loopIndex1<reels.childNodes.length;loopIndex1++) {   //length=5;
				if(z==0) {
					var cellForButton = document.createElement("td");
					resetButtons.appendChild(cellForButton);
					var button = document.createElement("input");
					button.setAttribute("type","button");
					button.setAttribute("value","reset reel");
					button.setAttribute("onclick","clearReel('"+loopIndex1+"','"+rowsCount+"')");
					button.setAttribute("style","width:120px");
					cellForButton.appendChild(button);
				}
				cellCount++;
				var tableCell = document.createElement("td");
				var select = document.createElement("select");
				var cellIndex=z+loopIndex1*rowsCount;
				select.setAttribute("id","cell"+cellIndex);
				select.setAttribute("onchange","showDebugText()");
				select.setAttribute("style","width:120px");
				//var someNode = document.createTextNode("cell"+cellIndex);
				//document.getElementById("debugDiv").appendChild(someNode);
				tableCell.appendChild(select);
				row.appendChild(tableCell);
				//document.getElementById("debugDiv").innerHTML="In first cycle";
				reel=reels.childNodes[loopIndex1];
				if(reel!=null) {
					
					 
						cell=reel.childNodes[z];
						if(cell!=null) {
								if(cell.childNodes.length!=1) {
									var option = document.createElement("option");
									var text = document.createTextNode("noSelect");
									option.appendChild(text);
									select.appendChild(option);
								}
							var loopIndex3=0;
							for(loopIndex3=0;loopIndex3<cell.childNodes.length;loopIndex3++) {
								//document.getElementById("debugDiv").innerHTML="In third cycle";
								icon=cell.childNodes[loopIndex3];
								option = document.createElement("option");
								text = document.createTextNode(icon.firstChild.nodeValue);
								option.appendChild(text);
								select.appendChild(option);
								
							}
						}
					
				}
			}
			resultTable.appendChild(row);
		}
		resultTable.appendChild(resetButtons);
		var targetTable = document.getElementById("targetTable");
		var oldP = document.getElementById("reels");
		if(oldP) {
	       targetTable.replaceChild(resultTable,oldP);
		   }
		document.getElementById("targetButton").innerHTML="<input type=\"button\" value=\"Post Reels\" onclick=\"postReels()\"/>";
		
	}
	
}

function showDebugText() {
	
	
	var data="destServletUrl="+destServletUrl+"&CMD=GETREELTABLE&SID="+b+"&REELS="+getIcons();
	
	//document.getElementById("targetText").innerHTML=data;
	var request = initRequest();

	request.onreadystatechange = function() {
		 if (request.readyState == 4) {
			 if (request.status == 200) {
				parseReels(request.responseXML);
			 }
		}
	}
    request.open("POST", iServletUrl);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    request.send(data);
}
function postReels() {
	var data="destServletUrl="+destServletUrl+"&CMD=POSTREELS&SID="+b+"&REELS="+getIcons();
	var request = initRequest();
    request.open("POST", iServletUrl);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
    request.send(data);
}

function getIcons() {
var loop=0;
var data="";
for(loop=0;loop<cellCount;loop++) {
		var cell=document.getElementById("cell"+loop);
		data+=cell.options[cell.selectedIndex].text;
		if(loop!=cellCount-1)
			data+="|";
	}
	return data;
}


function insertOption(itext)
{
var y=document.createElement('option');
y.text=itext;
var x=document.getElementById("available-states");
try
  {
  x.add(y,null); // standards compliant
  }
catch(ex)
  {
  x.add(y); // IE only
  }
}


function removeAllOptions()
  {
  var x=document.getElementById("available-states")
  while (x.options.length>1) {
  x.options[1] = null;
}
  }
  
 function clearReel(index,rowsCount) {
	var loop=0;
	for(loop=0;loop<rowsCount;loop++) {
		var ind = index*rowsCount+loop;
		var cell = document.getElementById("cell"+ind);
		cell.options[0].text="notIcon";
	}
	showDebugText();
 }
