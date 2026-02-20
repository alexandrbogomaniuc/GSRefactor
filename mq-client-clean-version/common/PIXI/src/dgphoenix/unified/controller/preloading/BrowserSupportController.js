import SimpleInfo from '../../model/base/SimpleInfo';
import SimpleController from '../base/SimpleController';
import BrowserSupportInfo from '../../model/preloading/BrowserSupportInfo';
import { APP } from '../main/globals';
import Application from '../main/Application';

class BrowserSupportController extends SimpleController
{
	static get i_EVENT_BROWSER_SUPPORT_CONFIRMED() 	{return "onBrowserSupportConfirmed";}
	static get i_EVENT_BROWSER_SUPPORT_DENIED() 	{return "onBrowserSupportDenied";}
	static get EVENT_AUDIO_CONTEXT_STATE_CHANGED() 	{return "onAudioContextStateChanged";}

	//IL INTERFACE...
	unlockContext()
	{
		this._unlockContext();
	}
	//...IL INTERFACE

	//IL CONSTRUCTION...
	constructor()
	{
		super(new BrowserSupportInfo());

		//IL IMPLEMENTATION...
		this._initBrowserSupportController();
	}
	//...IL CONSTRUCTION

	//ILI INIT...
	_initBrowserSupportController()
	{
		this._fAudioContext_ac = null;
	}
	//...ILI INIT

	__initControlLevel()
	{
		super.__initControlLevel();

		this.info.platformInfo = window["getPlatformInfo"].apply(window);
		if (APP.isDebugMode)
		{
			console.log("[BrowserSupportController] : platformInfo = ", this.info.platformInfo);
		}

		this._checkAudioContext();
		this._checkBrowserVersion();
	}

	//CHECK BROWSER...
	_checkBrowserVersion()
	{
		if (this.info.confirmed)
		{
			this.emit(BrowserSupportController.i_EVENT_BROWSER_SUPPORT_CONFIRMED);
		}
		else
		{
			this.emit(BrowserSupportController.i_EVENT_BROWSER_SUPPORT_DENIED);
		}
	}
	//...CHECK BROWSER

	//CHECK AUDIOCONEXT...
	_getActualAudioContext()
	{
		return createjs.Sound.isReady()
				? createjs.Sound.activePlugin.context
				: window.AudioContext 
					? this._fAudioContext_ac || (this._fAudioContext_ac = new AudioContext())
					: null;
	}

	_checkAudioContext()
	{
		let lContext_ac = this._getActualAudioContext();
		if (lContext_ac && lContext_ac.state === "suspended")
		{
			this.info.audioContextState = lContext_ac.state;
			lContext_ac.onstatechange = this._onAudioContextChanged.bind(this);
			APP.once(Application.EVENT_ON_SOUND_CONTEXT_NEED_TO_BE_CHECKED, this._onSoundContextNeedToBeChecked, this);
		}
	}

	_onAudioContextChanged(e)
	{
		this.info.audioContextState = e.target.state;
		this._unlockContext();
	}

	_onSoundContextNeedToBeChecked(e)
	{
		this._unlockContext();
	}

	_unlockContext()
	{
		let lContext_ac = this._getActualAudioContext();
		if (lContext_ac && lContext_ac.state === "suspended" && lContext_ac.resume)
		{
			lContext_ac.resume();
			this.info.audioContextState = lContext_ac.state;
			if (lContext_ac.state === "running")
			{
				lContext_ac.onstatechange = null;
			}
		}
		this.emit(BrowserSupportController.EVENT_AUDIO_CONTEXT_STATE_CHANGED);
	}
	//...CHECK AUDIOCONEXT
}

export default BrowserSupportController;