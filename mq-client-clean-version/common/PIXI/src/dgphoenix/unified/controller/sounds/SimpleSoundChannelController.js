import EventDispatcher from '../events/EventDispatcher';
import SimpleSoundInfo from '../../model/sounds/SimpleSoundInfo'

import Timer from '../time/Timer';

/**
 * @class
 * @extends EventDispatcher
 * @classdesc Class handles sound instance playing.
 */
class SimpleSoundChannelController extends EventDispatcher 
{
	//CL INTERFACE...
	static i_EVENT_SOUND_PLAYING_STARTED 		= "onSoundPlayingStarted";
	static i_EVENT_SOUND_PLAYING_INTERRUPTED 	= "onSoundPlayingInterrupted";
	static i_EVENT_SOUND_PLAYING_COMPLETED 		= "onSoundPlayingCompleted";
	static i_EVENT_SOUND_PLAYING_LOOP 			= "onSoundPlayingLoop";

	/**
	 * Checks if sound is ready to play (loaded and registered).
	 * @param {string} soundName 
	 * @returns {boolean}
	 * @static
	 */
	static isSoundReady(soundName)
	{
		return createjs.Sound.loadComplete(soundName);
	}
	//...CL INTERFACE

	/**
	 * @constructor
	 * @param {SimpleSoundInfo} aSoundInfo_ussi 
	 */
	constructor (aSoundInfo_ussi)
	{
		super();

		//IL IMPLEMENTATION...
		this._fSoundInfo_ussi = null;
		this._fSound_cjs = null;
		this._fSoundPlayingEmulationTimer_t = null;
		this._fTickerPaused_bl = false;

		this._initUSimpleSoundChannelController(aSoundInfo_ussi);
	}
	
	//IL INTERFACE...
	/**
	 * Start sound instance playing.
	 */
	i_startPlaying()
	{
		this._startSoundPlaying();
	}

	/**
	 * Stop sound instance playing.
	 */
	i_stopPlaying()
	{
		this._stopSoundPlaying();
	}
	
	/**
	 * Set should sound instance playing be looped or not.
	 * @param {*} aIsLooped_bln 
	 */
	i_setLoop(aIsLooped_bln)
	{
		this._setLoop(aIsLooped_bln);
	}
	
	/**
	 * Pause sound instance playing.
	 */
	i_pausePlaying()
	{
		this._pausePlaying();
	}
	
	/**
	 * Resume sound instance playing.
	 */
	i_resumePlaying()
	{
		this._resumePlaying();
	}
	
	/**
	 * Gets position of the playhead in milliseconds.
	 * @returns {number}
	 */
	i_getPosition()
	{
		return this._getPosition();
	}
	
	/**
	 * Destroy sound channel instance.
	 */
	i_destroy()
	{
		this._destroy();
	}

	/**
	 * Sets is sound instanse volume is on or off.
	 * @param {boolean} aSoundOn_bl - Is sound volume on or not.
	 * @param {boolean} aOptPauseSound_bl - Pause sound instance playing or not.
	 */
	i_setSoundOn(aSoundOn_bl, aOptPauseSound_bl)
	{
		this._setSoundOn(aSoundOn_bl, aOptPauseSound_bl);
	}
	
	/**
	 * Set sound instance volume.
	 * @param {number} aVolume_num 
	 */
	i_setVolume(aVolume_num)
	{
		this._setVolume(aVolume_num);
	}

	/**
	 * Get sound info.
	 * @returns {SimpleSoundInfo}
	 */
	i_getInfo()
	{
		return this._fSoundInfo_ussi;
	}
	
	/**
	 * Checks if sound instance playing completed or not.
	 * @returns {boolean}
	 */
	i_isPlayingCompleted()
	{
		return this._isSoundPlayingCompleted();
	}
	
	/**
	 * Checks if sound instance is destroyed or is currently playing or paused.
	 * @returns {boolean}
	 */
	i_isPlayingSucceeded()
	{
		return this._isPlayingSucceeded();
	}

	/**
	 * Checks if sound instance is currently playing or paused.
	 * @returns {boolean}
	 */
	i_isSoundPlaying()
	{
		return this._isSoundPlaying();
	}

	/**
	 * Gets sound instance.
	 * @returns {createjs.Sound}
	 */
	i_getSound()
	{
		return this._fSound_cjs;
	}
	//...IL INTERFACE

	//ILI INIT...
	_initUSimpleSoundChannelController(aSoundInfo_ussi)
	{
		if (!(aSoundInfo_ussi instanceof SimpleSoundInfo))
		{
			throw new Error("Invalid argument: " + aSoundInfo_ussi);
		}
		this._fSoundInfo_ussi = aSoundInfo_ussi;
		this._initSoundInstance();
		this._initSoundPlayingEmulationTimer();
	}

	_initSoundInstance()
	{
		var lSoundInstance_cjs = new createjs.Sound.createInstance(this._fSoundInfo_ussi.i_getSoundName());
		var lSoundDescriptor_usms = this._fSoundInfo_ussi.i_getSoundDescriptor();
		
		lSoundInstance_cjs.soundName = this._fSoundInfo_ussi.i_getSoundName();
		
		if (this._fSoundInfo_ussi.i_isLoop())
		{
			lSoundInstance_cjs.loop = 0; //-1;
		}
		lSoundInstance_cjs.startTime = lSoundDescriptor_usms.i_getOffset();
		
		if (lSoundInstance_cjs.duration > 0)
		{
			lSoundInstance_cjs.duration = Math.min(lSoundDescriptor_usms.i_getLength(), (lSoundInstance_cjs.duration-lSoundInstance_cjs.startTime));
		}
		lSoundInstance_cjs.volume = this._fSoundInfo_ussi.i_getVolume() !== undefined 
									? this._fSoundInfo_ussi.i_getVolume() * lSoundDescriptor_usms.i_getDefaultVolume() * this._fSoundInfo_ussi.i_getIndependentVolumeFactor()
									: lSoundDescriptor_usms.i_getDefaultVolume();
		lSoundInstance_cjs.muted = !this._fSoundInfo_ussi.i_isSoundOn();
		
		lSoundInstance_cjs.addEventListener("succeeded", this._onSoundChannelPlayingStarted, false, 0, this);
		lSoundInstance_cjs.addEventListener("interrupted", this._onSoundChannelPlayingInterrupted, false, 0, this);
		lSoundInstance_cjs.addEventListener("complete", this._onSoundChannelPlayingCompleted, false, 0, this);
		lSoundInstance_cjs.addEventListener("loop", this._onSoundChannelPlayingLoop, false, 0, this);
		
		this._fSound_cjs = lSoundInstance_cjs;
	}
	//...ILI INIT

	//SOUND PLAYING CONTROL...
	_startSoundPlaying()
	{
		if (this._isSoundEmulationMode())
		{
			this._startSoundPlayingEmulation();
			this.emit(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_STARTED);
		}
		else
		{
			let lPanning_num = this._fSoundInfo_ussi.i_getSoundDescriptor().panning;
			this._fSound_cjs.play({pan: lPanning_num});
		}
	}

	_stopSoundPlaying()
	{
		if (this._isSoundEmulationMode())
		{
			this._stopSoundPlayingEmulation();
		}
		else if (this._fSound_cjs)
		{
			this._fSound_cjs.stop();
		}
	}
	
	_pausePlaying()
	{
		if (!createjs.Ticker.pause && this._fSoundPlayingEmulationTimer_t)
		{
			this._fSoundPlayingEmulationTimer_t.pause();
		}
		else
		{
			this._fTickerPaused_bl = true;
		}
		
		if (this._fSound_cjs)
		{
			this._fSound_cjs.paused = true;
		}
	}
	
	_resumePlaying()
	{
		if (!this._fTickerPaused_bl && this._fSoundPlayingEmulationTimer_t)
		{
			this._fSoundPlayingEmulationTimer_t.resume();
		}
		
		this._fTickerPaused_bl = createjs.Ticker.pause;
		
		if (this._fSound_cjs)
		{
			this._fSound_cjs.paused = false;
		}
	}
	
	_getPosition()
	{
		if (this._isSoundEmulationMode())
		{
			var lPosition_int = this._fSoundPlayingEmulationTimer_t.getElapsedTime();
			return lPosition_int;
		}

		if (this._fSound_cjs)
		{
			return this._fSound_cjs.position;
		}
		return 0;
	}
	
	_destroy()
	{
		this._stopSoundPlaying();
		
		if (this._fSound_cjs)
		{
			this._fSound_cjs.stop();
			this._fSound_cjs.destroy();
			this._fSound_cjs = null;
		}
		
		this._fSoundInfo_ussi = null;

		this._fSoundPlayingEmulationTimer_t && this._fSoundPlayingEmulationTimer_t.destructor();
		this._fSoundPlayingEmulationTimer_t = null;

		super.destructor();
	}
	
	_onSoundChannelPlayingStarted(aEvent_ue)
	{
		this.emit(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_STARTED);
	}
	
	_onSoundChannelPlayingInterrupted(aEvent_ue)
	{
		this.emit(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_INTERRUPTED);
	}
	
	_onSoundChannelPlayingCompleted(aEvent_ue)
	{
		if (this._fSoundInfo_ussi && this._fSoundInfo_ussi.i_isLoop())
		{
			this.emit(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_LOOP);
			if (this._fSound_cjs)
			{
				this._fSound_cjs.play();
			}
		}
		else
		{
			this.emit(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_COMPLETED);
		}
	}

	_onSoundChannelPlayingLoop()
	{
		this.emit(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_LOOP);
	}
	//...SOUND PLAYING CONTROL

	//SOUND ON...
	_setSoundOn(aSoundOn_bl, aOptPauseSound_bl)
	{
		aSoundOn_bl = Boolean(aSoundOn_bl);
		
		var lSound_cjs = this._fSound_cjs;
		if (lSound_cjs)
		{
			lSound_cjs.muted = !aSoundOn_bl;

			if (aOptPauseSound_bl !== undefined)
			{
				if (aOptPauseSound_bl)
				{
					this._pausePlaying();
				}
				else
				{
					this._resumePlaying();
				}
			}
		}
	}
	
	_setVolume(aVolume_num)
	{
		var lSound_cjs = this._fSound_cjs;
		if (lSound_cjs)
		{
			lSound_cjs.volume = aVolume_num * this._fSoundInfo_ussi.i_getSoundDescriptor().i_getDefaultVolume() * this._fSoundInfo_ussi.i_getIndependentVolumeFactor();
		}
	}

	_multiplyVolume(aMultiplier_num)
	{
		var lSound_cjs = this._fSound_cjs;
		this._fSoundInfo_ussi.i_multiplyVolume(aMultiplier_num);
		if (lSound_cjs)
		{
			lSound_cjs.volume = this._fSoundInfo_ussi.i_getVolume() * this._fSoundInfo_ussi.i_getSoundDescriptor().i_getDefaultVolume() * this._fSoundInfo_ussi.i_getIndependentVolumeFactor();
		}
	}
	//...SOUND ON
	
	//LOOP CONTROL...
	_setLoop(aIsLooped_bln)
	{
		this._fSound_cjs.loop = 0; //aIsLooped_bln ? -1 : 0;
	}
	//...LOOP CONTROL

	//SOUND PLAYING EMULATION...
	_startSoundPlayingEmulation()
	{
		var lSoundDescriptor_usms = this._fSoundInfo_ussi.i_getSoundDescriptor();
		var lRemainingTime_num = lSoundDescriptor_usms.i_getLength();
		lRemainingTime_num = lRemainingTime_num > 0 ? lRemainingTime_num : 1/*a hack to prevent synchronous timng due to sung is not supported by USounds implementation and can be quite unsafe*/;
		this._fSoundPlayingEmulationTimer_t.setTimeout(lRemainingTime_num);
		this._fSoundPlayingEmulationTimer_t.start();
	}

	_stopSoundPlayingEmulation()
	{
		if (this._fSoundPlayingEmulationTimer_t.isInProgress())
		{
			this.emit(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_INTERRUPTED);
		}
		this._fSoundPlayingEmulationTimer_t.pause();
	}

	_initSoundPlayingEmulationTimer()
	{
		var l_t = new Timer(this._onSoundChannelPlayingEmulationCompleted.bind(this), 0);
		l_t.pause();
		this._fSoundPlayingEmulationTimer_t = l_t;
	}

	_onSoundChannelPlayingEmulationCompleted()
	{
		if (this._fSoundInfo_ussi.i_isLoop())
		{
			this._startSoundPlayingEmulation();
			return;
		}
		
		this.emit(SimpleSoundChannelController.i_EVENT_SOUND_PLAYING_COMPLETED);
	}

	_isSoundEmulationMode()
	{
		if (!this._fSoundInfo_ussi)
		{
			return false;
		}
		return this._fSoundInfo_ussi.i_isSoundEmulationMode();
	}
	//...SOUND PLAYING EMULATION

	_isSoundPlayingCompleted()
	{
		var lSound_cjs = this._fSound_cjs;
		if (lSound_cjs === null /* sound was destroyed previously */
				||
				(
					this._isSoundEmulationMode() 
					&& this._fSoundPlayingEmulationTimer_t.destroy
				)
				|| 
				(
					!this._isSoundEmulationMode()
					&& (lSound_cjs.playState === createjs.Sound.PLAY_FINISHED || lSound_cjs.playState === createjs.Sound.PLAY_INTERRUPTED)
				)
			)
		{
			return true;
		}
		
		return false;
	}
	
	_isPlayingSucceeded()
	{
		var lSound_cjs = this._fSound_cjs;
		if (lSound_cjs === null /* sound was destroyed previously */
				||
				(
					this._isSoundEmulationMode() 
					&& this._fSoundPlayingEmulationTimer_t.isInProgress()
				)
				|| 
				(
					!this._isSoundEmulationMode()
					&& lSound_cjs.playState === createjs.Sound.PLAY_SUCCEEDED
				)
			)
		{
			return true;
		}
		
		return false;
	}

	_isSoundPlaying()
	{
		var lSound_cjs = this._fSound_cjs;
		if (lSound_cjs &&
				(
					(this._isSoundEmulationMode() && this._fSoundPlayingEmulationTimer_t && this._fSoundPlayingEmulationTimer_t.isInProgress()) ||
					(!this._isSoundEmulationMode() && lSound_cjs.playState === createjs.Sound.PLAY_SUCCEEDED)
				)
			)
		{
			return true;
		}
		
		return false;
	}
	//...IL IMPLEMENTATION
}

export default SimpleSoundChannelController;