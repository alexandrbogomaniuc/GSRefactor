import Application from '../../../unified/controller/main/Application';
import GUSCurrencyInfo from '../../model/currency/GUSCurrencyInfo';
import GUSGameCommonAssetsController from '../preloading/GUSGameCommonAssetsController';
import GUSGamePendingOperationController from '../gameplay/GUSGamePendingOperationController';

class GUSGameApplication extends Application
{
	constructor(...args)
	{
		super(...args);

		//DEBUG FPS RAM INDICATOR...
		this._fRAMText_str = "";
		this._fFPSText_str = "";

		let l_html = window.document.createElement('div');
		l_html.style.color = "white";
		l_html.style.position = "absolute";
		l_html.style.left = "0px";
		l_html.style.top = "0px";
		l_html.style["padding-left"] = "10px";
		l_html.style["padding-right"] = "10px";
		l_html.style["background-color"] = "#ad0021";
		l_html.style["z-index"] = "999";

		l_html.style["text-align"] = "center";
		l_html.style["vertical-align"] = "middle";
		l_html.style["line-height"] = "50px";
		l_html.style["font-family"] = "calibri";
		l_html.style["font-weight"] = "bold";
		l_html.style["font-style"] = "italic";
		l_html.style["font-size"] = "40px";
		l_html.style["display"] = "none";


		l_html.style["-webkit-touch-callout"] = "none";
		l_html.style["-webkit-user-select"] = "none";
		l_html.style["-khtml-user-select"] = "none";
		l_html.style["-moz-user-select"] = "none";
		l_html.style["-ms-user-select"] = "none";
		l_html.style["user-select"] = "none";

		l_html.innerText = "";
		document.body.appendChild(l_html);

		this._fFPSRAMIndicator_html = l_html;
		//...DEBUG FPS RAM INDICATOR
	}

	__updateFPSDebug()
	{
		super.__updateFPSDebug();

		if (this.appParamsInfo.isFPSRAMDisplayRequired)
		{
			//RAM...
			if (performance.memory)
			{

				let lMemoryLimit_num = (performance.memory.jsHeapSizeLimit / 1024 / 1024).toFixed(2);
				let lUsedMemory_num = (performance.memory.usedJSHeapSize / 1024 / 1024).toFixed(2);

				this._fRAMText_str = "RAM: " + lUsedMemory_num + "mb";
			}
			//...RAM
			this._fFPSRAMIndicator_html.innerText = "FPS: " + this._lastFPS + " " + this._fRAMText_str;	
			this._fFPSRAMIndicator_html.style["display"] = "block";
		}
	}

	//CURRENCY INFO...
	__isCurrencyInfoSupported()
	{
		return true;
	}

	__generateCurrencyInfo()
	{
		return new GUSCurrencyInfo();
	}
	//...CURRENCY INFO

	//COMMON ASSETS...
	__provideCommonAssetsControllerInstance()
	{
		return new GUSGameCommonAssetsController();
	}
	//...COMMON ASSETS

	//PENDING OPERATION...
	get pendingOperationController()
	{
		return this._fPendingOperationController_lpoc || (this._fPendingOperationController_lpoc = this._initPendingOperationController());
	}

	_initPendingOperationController()
	{
		let l_lpoc = this._providePendingOperationControllerInstance();
		this._fPendingOperationController_lpoc = l_lpoc;

		l_lpoc.i_init();

		return l_lpoc;
	}

	_providePendingOperationControllerInstance()
	{
		return new GUSGamePendingOperationController();
	}
	//...PENDING OPERATION
}

export default GUSGameApplication;