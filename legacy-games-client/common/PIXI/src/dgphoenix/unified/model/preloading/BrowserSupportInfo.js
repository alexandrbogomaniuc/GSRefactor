import SimpleInfo from '../base/SimpleInfo';
import WebSocketInteractionController from '../../controller/interaction/server/WebSocketInteractionController';
import { APP } from '../../controller/main/globals';

/**
 * Browser support info.
 * @class
 * @extends SimpleInfo
 */
class BrowserSupportInfo extends SimpleInfo
{
	//IL INTERFACE...
	//...IL INTERFACE

	//IL CONSTRUCTION...
	constructor(aOptId_obj, aOptParentInfo_usi)
	{
		//IL IMPLEMENTATION...
		super(aOptId_obj, aOptParentInfo_usi);

		this._initUBrowserSupportInfo();
	}
	//...IL CONSTRUCTION

	set platformInfo(aInfo_p)
	{
		this._fPlatform_p = aInfo_p;
	}

	/**
	 * Object containing information about browser prepared in validator.js.
	 * @type {Object}
	 */
	get platformInfo()
	{
		return this._fPlatform_p;
	}

	/**
	 * Is Browser supported by application.
	 * @type {Boolean}
	 */
	get confirmed()
	{
		return (this._fPlatform_p && this._fPlatform_p.supported);
	}

	set audioContextState(aState_str)
	{
		this._fAudioContextState_str = aState_str;
	}

	/** 
	 * Last saved browser's audio context state.
	 * @type {string}
	 */
	get audioContextState()
	{
		return this._fAudioContextState_str;
	}

	/**
	 * Checks if audio context state is "suspended".
	 */
	get isAudioContextSuspended()
	{
		if(
			APP.isDebugMode &&
			WebSocketInteractionController.STUBS_PATH
			)
		{
			return false;
		}

		return this._fAudioContextState_str === "suspended";
	}

	/** Indicates whether application audio is supported by browser or not. */
	get isSoundEnabled()
	{
		return (this._fPlatform_p && this._fPlatform_p.soundEnabled);
	}

	//ILI INIT...
	_initUBrowserSupportInfo()
	{
		this._fPlatform_p = null;
		this._fConfirmed_bl = false;
		this._fAudioContextState_str = undefined;
	}
	//...ILI INIT
}

export default BrowserSupportInfo;