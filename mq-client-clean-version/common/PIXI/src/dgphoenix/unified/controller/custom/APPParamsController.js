import SimpleController from '../base/SimpleController';
import APPParamsInfo from '../../model/custom/APPParamsInfo';

/**
 * @class
 * @classdesc Application window params controller
 */
class APPParamsController extends SimpleController
{
	//INIT...
	constructor()
	{
		super(new APPParamsInfo());
		
		this._initAPPParamsController();
	}

	_initAPPParamsController()
	{
		if (window[APPParamsInfo.FUNC_GET_LOBBY_PATH])
		{
			this.info.lobbyPath = window[APPParamsInfo.FUNC_GET_LOBBY_PATH].apply(window);
		}

		if (window[APPParamsInfo.FUNC_GET_GAME_PATH])
		{
			this.info.gamePath = window[APPParamsInfo.FUNC_GET_GAME_PATH].apply(window);
		}

		if (window[APPParamsInfo.FUNC_GET_PARAMS])
		{
			this.info.params = window[APPParamsInfo.FUNC_GET_PARAMS].apply(window);
		}

		if (window[APPParamsInfo.FUNC_GET_CUSTOMERSPEC_DESCRIPTOR_URL])
		{
			this.info.customerspecDescriptorUrl = window[APPParamsInfo.FUNC_GET_CUSTOMERSPEC_DESCRIPTOR_URL].apply(window);
		}

		//DEBUG PARAMETERS...
		let lURLParams_str_arr = window.location.search.split("&");
		
		//STUBS TRACKING PARAMETER...
		if(lURLParams_str_arr.indexOf(APPParamsInfo.DEBUG_PARAM_TRACK_STUBS) !== -1)
		{
			this.info.isStubsTrackingRequired = true;
		}
		//...STUBS TRACKING PARAMETER

		//FPS RAM DISPLAY PARAMETER...
		if(lURLParams_str_arr.indexOf(APPParamsInfo.DEBUG_PARAM_FPS_RAM) !== -1)
		{
			this.info.isFPSRAMDisplayRequired = true;
		}
		//...FPS RAM DISPLAY PARAMETER

		//...DEBUG PARAMETERS
	}
	//...INIT
}

export default APPParamsController;