
function updateFormAction(form) {
    currentAction=form.action;

    if (currentAction.indexOf('://') != -1)
        return;

    form.action = 'http://' + location.host + currentAction;
}

function writeOb(src){
    document.write(src);
}

function hidescrollbar(){	
	var agent = navigator.userAgent;		
	if (agent.indexOf("MSIE") != -1){			
		document.body.scroll = "no";	
	} else{			
		document.documentElement.style.overflow = 'hidden';
	}
}

function openWnd(url, width, height, name, isfullscreen){
	var optionstr = 'menubar=no';
	optionstr += ', location=yes';
	optionstr += ', toolbar=no';
	optionstr += ', status=no';
	optionstr += ', personalbar=no';
	optionstr += ', scrollbars=no';
	optionstr += ', resizable=yes';
	if (isfullscreen == "yes"){
		optionstr  += ', width=' + screen.width;
		optionstr  += ', height=' + screen.height; 
		optionstr += ', fullscreen=yes';
	} else{
		optionstr  += ', width=' + width;
		optionstr  += ', height=' + height;
		optionstr += ', fullscreen=no';
	}	
	var wnd = window.open(url,name,optionstr);
	wnd.focus();
	return wnd;
}