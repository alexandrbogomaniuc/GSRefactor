function generateHttpRequestObject()
{
	try {return new XMLHttpRequest();}
		catch (error) {}
	try {return new ActiveXObject("Msxml2.XMLHTTP");}
		catch (error) {}
	try {return new ActiveXObject("Microsoft.XMLHTTP");}
		catch (error) {}
}

 /*TODO [os]: deprecated class to be removed*/
class ServerGetInteractionController
{
	static sendRequest(aURL_str, aSuccessCallBack_func, aOptErrorCallBack_func)
	{
		var lRequest_xhr = generateHttpRequestObject();

		if(!lRequest_xhr)
		{
			return;
		}

		lRequest_xhr.open("GET", aURL_str/* + "?" + GLOBALS.getCommonAssetsVersion()*/, true);
		lRequest_xhr.send(null);


		lRequest_xhr.onreadystatechange = function()
		{

			if (lRequest_xhr.readyState === 4)
			{

				let lResponse_str = lRequest_xhr.responseText;
				if(lResponse_str)
				{
					aSuccessCallBack_func(lRequest_xhr.responseText);
				}
				else if(aOptErrorCallBack_func)
				{
					aOptErrorCallBack_func();
				}
			}
		};
	}
}

export default ServerGetInteractionController;