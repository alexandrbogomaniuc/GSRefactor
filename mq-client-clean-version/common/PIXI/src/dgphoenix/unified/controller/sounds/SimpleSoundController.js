import EventDispatcher from '../events/EventDispatcher';

import SimpleSoundInfo from '../../model/sounds/SimpleSoundInfo';
import SimpleSoundChannelController from './SimpleSoundChannelController';

import Counter from '../../view/custom/Counter';
import { APP } from '../main/globals';

/**
 * @class
 * @extends EventDispatcher
 * @classdesc Handles sound playing.
 */
class SimpleSoundController extends EventDispatcher
{
	//CL INTERFACE...
	static i_EVENT_SOUND_PLAYING_STARTING = "onSoundPlayingStarting";
	static i_EVENT_SOUND_PLAYING_COMPLETION = "onSoundPlayingCompletion";
	static i_EVENT_SOUND_PLAYING_STARTED = SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_STARTED;
	static i_EVENT_SOUND_PLAYING_COMPLETED = SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_COMPLETED;
	static i_EVENT_SOUND_PLAYING_LOOP = SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_LOOP;
	
	static i_EVENT_SOUND_VOLUME_CHANGED = "onSoundVolumeChanged";
	static i_EVENT_SOUND_DESTROYING = "onSoundDestroying";
	//...CL INTERFACE

	//IL CONSTRUCTION...
	/**
	 * @constructor
	 * @param {SimpleSoundInfo} aSoundInfo_ussi - Sound descriptor
	 */
	constructor(aSoundInfo_ussi)
	{
		super();

		//IL IMPLEMENTATION...
		this._fSoundInfo_ussi = null;
		this._fChannels_usschc_arr = null;
		this._fLastActivatedChannel_usschc = null;
		this._fVolumeValueCounter_guvc = null;

		this._initUSimpleSoundController(aSoundInfo_ussi);
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...

	/**
	 * Sets multiplier to fade volume.
	 * @param {number} aFadeMultiplier 
	 */
	setFadeMultiplier(aFadeMultiplier)
	{
		this._setFadeMultiplier(aFadeMultiplier);
	}

	/**
	 * Starts new sound instance playing
	 */
	i_startPlaying()
	{
		this.__startSoundPlaying();
	}

	/**
	 * Stops all sound instances playing
	 */
	i_stopPlaying(aIsMuted)
	{
		this.__stopAllSoundChannelsPlaying(aIsMuted);
	}

	/**
	 * Checks if sound is currently playing or not.
	 * @returns {boolean}
	 */
	i_isSoundPlaying()
	{
		return this._fLastActivatedChannel_usschc ? this._fLastActivatedChannel_usschc.i_isSoundPlaying() : false;
	}
	
	/**
	 * Set should sound playing be looped or not.
	 * @param {aIsLooped_bln}
	 */
	i_setLoop(aIsLooped_bln)
	{
		this._setLoop(aIsLooped_bln);
	}
	
	/**
	 * Pause sound playing.
	 */
	i_pausePlaying()
	{
		this._pausePlaying();
	}
	
	/**
	 * Resume sound playing.
	 */
	i_resumePlaying()
	{
		this._resumePlaying();
	}
	
	/**
	 * Set sound volume.
	 * @param {number} aTargetVolume_num 
	 */
	i_setVolume(aTargetVolume_num)
	{
		this.__setVolume(aTargetVolume_num);
	}
	
	/**
	 * Set expected sound volume smoothly.
	 * @param {number} aTargetVolume_num - Expected sound volume.
	 * @param {number} aTimeInMs_int - Volume change duration (milliseconds).
	 * @param {Function} aEase_fnc - Easing function.
	 * @param {Function} aOptOnComplete_fnc - Callback for volume change completion.
	 */
	i_setVolumeSmoothly(aTargetVolume_num, aTimeInMs_int, aEase_fnc, aOptOnComplete_fnc)
	{
		this._setVolumeSmoothly(aTargetVolume_num, aTimeInMs_int, aEase_fnc, aOptOnComplete_fnc)
	}

	/**
	 * Set additional sound volume multiplier.
	 * @param {*} aMultiplier_num 
	 */
	i_multiplyVolume(aMultiplier_num)
	{
		this.__multiplyVolume(aMultiplier_num);
	}
	
	/**
	 * Resets sound volume (to 1).
	 */
	i_resetVolume()
	{
		this.__resetVolume();
	}
	
	/**
	 * Destroy sound controller.
	 */
	i_destroy()
	{
		let lSoundName_str = this._fSoundInfo_ussi ? this._fSoundInfo_ussi.i_getSoundName() : undefined;
		this.emit(SimpleSoundController.i_EVENT_SOUND_DESTROYING, {soundName: lSoundName_str});

		this.__destroyChannels();
		this._fChannels_usschc_arr = null;

		this._fSoundInfo_ussi && this._fSoundInfo_ussi.i_destroy();
		this._fSoundInfo_ussi = null;

		this._fLastActivatedChannel_usschc = null;
		
		this._fVolumeValueCounter_guvc && this._fVolumeValueCounter_guvc.destroy();
		this._fVolumeValueCounter_guvc = null;

		this._fOnVolumeChangingCompleteHandler_func = null;		

		super.destructor();
	}

	/**
	 * Set sound volume on or off.
	 * @param {boolean} aSoundOn_bl - Is sound volume on or not.
	 * @param {boolean} aOptPauseSound_bl - Should sound playing be paused or not.
	 */
	i_setSoundOn(aSoundOn_bl, aOptPauseSound_bl)
	{
		this._setSoundOn(aSoundOn_bl, aOptPauseSound_bl);
	}

	/**
	 * Gets sound info.
	 * @returns {SimpleSoundInfo}
	 */
	i_getInfo()
	{
		return this.__getInfo();
	}
	
	/**
	 * Gets position of the last channel in milliseconds.
	 * @returns {number}
	 */
	i_getPosition()
	{
		var lLastActivatedChannel_usschc = this._fLastActivatedChannel_usschc;
		if (!lLastActivatedChannel_usschc)
		{
			return 0;
		}
		
		return lLastActivatedChannel_usschc.i_getPosition();
	}
	
	/**
	 * Save sound settings before change of page visibility.
	 */
	i_preserveSettingsBeforeChangeVisibility()
	{
		this._preserveSettingsBeforeChangeVisibility();
	}
	//...IL INTERFACE

	//ILI INIT...
	_initUSimpleSoundController(aSoundInfo_ussi)
	{
		this._fSoundInfo_ussi = aSoundInfo_ussi;
		this._fChannels_usschc_arr = [];

		let lIsSoundReady = this.__isSoundReady;
		this._fSoundInfo_ussi.i_setSoundEmulationMode(!lIsSoundReady);

		if (!lIsSoundReady)
		{
			this._startListenSoundReadyState();
		}
	}
	//...ILI INIT

	get __isSoundReady()
	{
		return SimpleSoundChannelController.isSoundReady(this._fSoundInfo_ussi.i_getSoundName());;
	}

	__getInfo()
	{
		return this._fSoundInfo_ussi;
	}
	
	__getChannels()
	{
		return this._fChannels_usschc_arr;
	}

	//SOUND PLAYING CONTROL...
	__startSoundPlaying()
	{
		var lChannel_usschc = this.__generateNewSoundChannelController();
		this._fLastActivatedChannel_usschc = lChannel_usschc;
		lChannel_usschc.i_startPlaying();
	}
	//...SOUND PLAYING CONTROL

	//SOUND CHANNELS...
	__generateNewSoundChannelController()
	{
		if (this._isMaximumChannelsAmountDefined() && this._fChannels_usschc_arr.length >= this.__maximumChannelsAmount())
		{
			throw new Error("Channel's stack overflow ex: " + this._fChannels_usschc_arr.length + "/" + this.__maximumChannelsAmount());
		}
		
		var lChannel_usschc = new SimpleSoundChannelController(this._fSoundInfo_ussi);
		lChannel_usschc.i_setSoundOn(this._fSoundInfo_ussi.i_isSoundOn());
		this._fChannels_usschc_arr.push(lChannel_usschc);
		
		lChannel_usschc.on(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_STARTED, this.__onSoundChannelPlayingStarted, this);
		lChannel_usschc.on(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_INTERRUPTED, this.__onSoundChannelPlayingInterrupted, this);
		lChannel_usschc.on(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_COMPLETED, this.__onSoundChannelPlayingCompleted, this);
		lChannel_usschc.on(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_LOOP, this.emit, this);
		
		return lChannel_usschc;
	}
	
	__maximumChannelsAmount()
	{
		return undefined;
	}
	
	_isMaximumChannelsAmountDefined()
	{
		return !isNaN(this.__maximumChannelsAmount());
	}
	
	__onSoundChannelPlayingStarted(aEvent_ue)
	{
		var lInfo_ussi = this._fSoundInfo_ussi;
		
		if (lInfo_ussi.i_isPlayingStatePlaying() || lInfo_ussi.i_isPlayingStatePaused())
		{
			return;
		}
		
		if (
				lInfo_ussi.i_isPlayingStateCompletion()
				|| lInfo_ussi.i_isPlayingStateInterrupted()
				|| lInfo_ussi.i_isPlayingStateInitial()
				|| lInfo_ussi.i_isPlayingStateCompleted()
			)
		{
			this._setSoundState(SimpleSoundInfo.i_PLAYING_STATE_ID_STARTING);
			this._setSoundState(SimpleSoundInfo.i_PLAYING_STATE_ID_PLAYING);
		}
		else
		{
			throw new Error("ILE: " + lInfo_ussi.i_getPlayingStateId());
		}
	}
	
	__onSoundChannelPlayingInterrupted(aEvent_ue)
	{
		this.__resetVolume();
	}

	__onSoundChannelPlayingCompleted(aEvent_ue)
	{
		this.__disposeCurrentSoundChannel(aEvent_ue.target);

		if (this.__checkAllSoundChanelCompleted())
		{
			this._setSoundState(SimpleSoundInfo.i_PLAYING_STATE_ID_COMPLETION);
			this._setSoundState(SimpleSoundInfo.i_PLAYING_STATE_ID_COMPLETED);
		}
	}

	__checkAllSoundChanelCompleted()
	{
		for (var i = 0; i < this._fChannels_usschc_arr.length; i++)
		{
			var lChannel_usschc = this._fChannels_usschc_arr[i];
			if (lChannel_usschc.i_isPlayingSucceeded())
			{
				return false;
			}
		}
		return true;
	}

	__disposeCurrentSoundChannel(aChannels_usschc)
	{
		if (this._fChannels_usschc_arr)
		{
			let lSoundIndex_int = this._fChannels_usschc_arr.indexOf(aChannels_usschc);
			if (lSoundIndex_int >= 0)
			{
				this._fChannels_usschc_arr.splice(lSoundIndex_int, 1);
			}
		}
		
		aChannels_usschc && aChannels_usschc.i_destroy();
	}
	
	__stopAllSoundChannelsPlaying(aIsMuted)
	{
		if (this._fChannels_usschc_arr)
		{
			while (this._fChannels_usschc_arr.length > 0)
			{
				var lChannel_usschc = this._fChannels_usschc_arr.pop();
				lChannel_usschc.i_destroy();
			}
		}

		var lInfo_ussi = this.__getInfo();
		if (lInfo_ussi)
		{
			if (this.__getInfo().i_isPlayingStatePlaying() || this.__getInfo().i_isPlayingStatePaused()
				)
			{
				this._setSoundState(SimpleSoundInfo.i_PLAYING_STATE_ID_INTERRUPTED);
			}

			!aIsMuted && this.__resetVolume();
		}
	}
	
	_setLoop(aIsLooped_bln)
	{
		if (this._fSoundInfo_ussi)
		{
			this._fSoundInfo_ussi.i_setLoop(aIsLooped_bln);
		}
		
		var lChannels_usschc_arr = this._fChannels_usschc_arr.slice(0);
		var lChannelsCount_int = lChannels_usschc_arr.length;
		for (var i = 0 ; i < lChannelsCount_int; i++)
		{
			var lChannel_usschc = lChannels_usschc_arr[i];
			lChannel_usschc.i_setLoop(aIsLooped_bln);
		}
	}
	
	_pausePlaying()
	{
		var lInfo_ussi = this._fSoundInfo_ussi;
		if (!lInfo_ussi || !lInfo_ussi.i_isPlayingStatePlaying())
		{
			return;
		}
		var lChannels_usschc_arr = this._fChannels_usschc_arr.slice(0);
		var lChannelsCount_int = lChannels_usschc_arr.length;
		for (var i = 0 ; i < lChannelsCount_int; i++)
		{
			var lChannel_usschc = lChannels_usschc_arr[i];
			lChannel_usschc.i_pausePlaying();
		}
		
		this._setSoundState(SimpleSoundInfo.i_PLAYING_STATE_ID_PAUSED);
	}
	
	_resumePlaying()
	{
		var lInfo_ussi = this._fSoundInfo_ussi;
		if (!lInfo_ussi || !lInfo_ussi.i_isPlayingStatePaused())
		{
			return;
		}
		var lChannels_usschc_arr = this._fChannels_usschc_arr.slice(0);
		var lChannelsCount_int = lChannels_usschc_arr.length;
		for (var i = 0 ; i < lChannelsCount_int; i++)
		{
			var lChannel_usschc = lChannels_usschc_arr[i];
			lChannel_usschc.i_resumePlaying();
		}
		
		this._setSoundState(SimpleSoundInfo.i_PLAYING_STATE_ID_PLAYING);
	}
	
	__setVolume(aVolume_num)
	{	
		if (isNaN(aVolume_num) || aVolume_num < 0)
		{
			throw new Error("Incorrect volume value: " + aVolume_num);
		}

		let lVolume_num = aVolume_num;

		if (aVolume_num > 1)
		{
			console.error("Incorrect volume value: " + aVolume_num+". The volume value will be set to 1.");
			lVolume_num = 1;
		}

		if (!this._fSoundInfo_ussi || lVolume_num === this._fSoundInfo_ussi.i_getVolume())
		{
			return;
		}
		
		var lChannels_usschc_arr = this._fChannels_usschc_arr.slice(0);
		var lChannelsCount_int = lChannels_usschc_arr.length;
		for (var i = 0 ; i < lChannelsCount_int; i++)
		{
			var lChannel_usschc = lChannels_usschc_arr[i];
			lChannel_usschc.i_setVolume(lVolume_num);
		}
		
		this._fSoundInfo_ussi.i_setVolume(lVolume_num);
		this.emit(SimpleSoundController.i_EVENT_SOUND_VOLUME_CHANGED);
	}

	__getVolume()
	{
		return this.__getInfo() ? this.__getInfo().i_getVolume() : null;
	}

	__multiplyVolume(aMultiplier_num)
	{
		if (isNaN(aMultiplier_num) || aMultiplier_num < 0 || aMultiplier_num > 1)
		{
			throw new Error("Incorrect multiplier value: " + aMultiplier_num);
		}
		
		if (!this._fSoundInfo_ussi || aMultiplier_num === this._fSoundInfo_ussi.i_getIndependentVolumeFactor())
		{
			return;
		}
		
		var lChannels_usschc_arr = this._fChannels_usschc_arr.slice(0);
		var lChannelsCount_int = lChannels_usschc_arr.length;
		for (var i = 0 ; i < lChannelsCount_int; i++)
		{
			var lChannel_usschc = lChannels_usschc_arr[i];
			lChannel_usschc._multiplyVolume(aMultiplier_num);
		}
		
		this._fSoundInfo_ussi.i_multiplyVolume(aMultiplier_num);
		this.emit(SimpleSoundController.i_EVENT_SOUND_VOLUME_CHANGED);
	}
	
	__resetVolume()
	{
		this._stopVolumeChanging();
		this.__multiplyVolume(1);

		if (this._fSoundInfo_ussi)
		{
			this._fSoundInfo_ussi.i_multiplyVolume(1);
		}

		// Causes sound bugs
		// this.__setVolume(this._fSoundInfo_ussi.i_getSoundDescriptor().i_getDefaultVolume());
	}
	
	__destroyChannels()
	{
		if (!this._fChannels_usschc_arr)
		{
			return;
		}
		
		var lChannels_usschc_arr = this._fChannels_usschc_arr.slice(0);
		var lChannelsCount_int = lChannels_usschc_arr.length;
		for (var i = 0 ; i < lChannelsCount_int; i++)
		{
			var lChannel_usschc = lChannels_usschc_arr[i];
			lChannel_usschc.off(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_STARTED, this.__onSoundChannelPlayingStarted, this);
			lChannel_usschc.off(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_INTERRUPTED, this.__onSoundChannelPlayingInterrupted, this);
			lChannel_usschc.off(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_COMPLETED, this.__onSoundChannelPlayingCompleted, this);
			
			lChannel_usschc.i_destroy();
		}
		
		this._fChannels_usschc_arr = [];
	}
	//...SOUND CHANNELS

	//SOUND ON...
	_setSoundOn(aSoundOn_bl, aOptPauseSound_bl)
	{
		aSoundOn_bl = !!aSoundOn_bl;
		
		if (!this._fSoundInfo_ussi || (this._fSoundInfo_ussi.i_isSoundOn() === aSoundOn_bl))
		{
			return;
		}
		this._fSoundInfo_ussi.i_setSoundOn(aSoundOn_bl);
		var lChannelsCount_int = this._fChannels_usschc_arr.length;
		for (var i = 0; i < lChannelsCount_int; i++)
		{
			this._fChannels_usschc_arr[i].i_setSoundOn(aSoundOn_bl, aOptPauseSound_bl);
		}
	}
	//...SOUND ON

	//SOUND STATE...
	_setSoundState(aSoundStateId_int)
	{
		var lInfo_ussi = this._fSoundInfo_ussi;

		if (lInfo_ussi)
		{
			var lPrevSoundStateId_int = lInfo_ussi.i_getPlayingStateId();
			if (lPrevSoundStateId_int === aSoundStateId_int)
			{
				return true;
			}
			lInfo_ussi.i_setPlayingStateId(aSoundStateId_int);
			//TOGO: it seems state changing must be protected also like the sound channel implmentation; but possibly this is not required with current logic implementation
			this._onPlayingStateChanged(aSoundStateId_int, lPrevSoundStateId_int);
		}
	}

	_onPlayingStateChanged(aNewSoundStateId_int, aPrevSoundStateId_int)
	{
		if (aNewSoundStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_STARTING)
		{
			this.emit(SimpleSoundController.i_EVENT_SOUND_PLAYING_STARTING);
		}
		else if (aNewSoundStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_PLAYING)
		{
			if (aPrevSoundStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_STARTING)
			{
				this.emit(SimpleSoundController.i_EVENT_SOUND_PLAYING_STARTED);
			}
		}
		else if (aNewSoundStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_COMPLETION)
		{
			this.emit(SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETION);
		}
		else if (aNewSoundStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_COMPLETED)
		{
			this.emit(SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETED);
		}
	}
	//...SOUND STATE

	//VOLUME CONTROL ...
	_getVolumeValueCounter()
	{
		return this._fVolumeValueCounter_guvc || (this._fVolumeValueCounter_guvc = this._initVolumeValueCounter());
	}

	_initVolumeValueCounter()
	{
		let lVolumeValueCounter_c = new Counter({ target: this.__getInfo(), method: "fadeMultiplier" }, null, true);
		
		return lVolumeValueCounter_c;
	}
	
	_setVolumeSmoothly(aTargetVolume_num, aTimeInMs_int, aEase_fnc, aOptOnComplete_fnc)
	{
		if (isNaN(aTargetVolume_num) || aTargetVolume_num < 0 || aTargetVolume_num > 1)
		{
			throw new Error("Incorrect volume target value: " + aTargetVolume_num);
		}
		
		if (aTimeInMs_int > 0)
		{
			this._fOnVolumeChangingCompleteHandler_func = aOptOnComplete_fnc;
			this._getVolumeValueCounter().startCounting(aTargetVolume_num, aTimeInMs_int, aEase_fnc, this._onVolumeValueCountingCompleted.bind(this), this._onVolumeValueCountingStep.bind(this));
		}
		else
		{
			aOptOnComplete_fnc && aOptOnComplete_fnc();
		}
	}

	_stopVolumeChanging()
	{
		this._fOnVolumeChangingCompleteHandler_func = null;
		
		this._getVolumeValueCounter().stopCounting();
	}
	
	_onVolumeValueCountingStep()
	{
		let lInfo_ussi = this.__getInfo();

		if (lInfo_ussi)
		{
			this.__multiplyVolume(lInfo_ussi.fadeMultiplier);
			this.__setVolume(lInfo_ussi.i_getVolume());
		}
	}
	
	_onVolumeValueCountingCompleted()
	{
		this._fOnVolumeChangingCompleteHandler_func && this._fOnVolumeChangingCompleteHandler_func();
		this._fOnVolumeChangingCompleteHandler_func = null;
	}

	_setFadeMultiplier(aFadeMultiplier)
	{
		var lInfo_ussi = this.__getInfo();
		this.i_multiplyVolume(aFadeMultiplier);

		if (lInfo_ussi)
		{
			this.__getInfo().fadeMultiplier = aFadeMultiplier;
		}
	}
	//...VOLUME CONTROL 

	//PRESERVING SETTINGS...
	_preserveSettingsBeforeChangeVisibility()
	{
		var lInfo_ussi = this.__getInfo();
		if (lInfo_ussi)
		{
			var lIsPaused_bl = lInfo_ussi.i_isPlayingStatePaused();
			lInfo_ussi.i_setWasPausedBeforeChangeVisibility(lIsPaused_bl);
		}
	}
	//...PRESERVING SETTINGS

	//SWITCH FROM EMULATION TO REAL PLAYING...
	_startListenSoundReadyState()
	{
		if (this.__isSoundReady)
		{
			this.__cancelEmulationMode();
		}
		else
		{
			if (APP.soundsController.info.soundsLoadingAvailable)
			{
				createjs.Sound.on("fileload", this._onAnySoundLoaded, this);
			}
			
		}
	}

	_onAnySoundLoaded(event)
	{
		var lInfo_ussi = this.__getInfo();
		if (
				lInfo_ussi
				&& event.id === lInfo_ussi.i_getSoundName()
				&& this.__isSoundReady
			)
		{
			this.__cancelEmulationMode();
		}
	}
		
	__cancelEmulationModeIfRequired()
	{
		var lInfo_ussi = this.__getInfo();
		if (lInfo_ussi && lInfo_ussi.i_isSoundEmulationMode() && this.__isSoundReady)
		{
			this.__cancelEmulationMode();
		}
	}

	__cancelEmulationMode()
	{
		this._fSoundInfo_ussi.i_setSoundEmulationMode(false);
	}
	//...SWITCH FROM EMULATION TO REAL PLAYING
}

export default SimpleSoundController;