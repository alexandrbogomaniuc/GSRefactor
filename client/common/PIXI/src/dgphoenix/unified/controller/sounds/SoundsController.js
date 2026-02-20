import SimpleController from '../base/SimpleController';

import SoundsInfo from '../../model/sounds/SoundsInfo';
import SimpleSoundInfo from '../../model/sounds/SimpleSoundInfo';
import SimpleSoundController from './SimpleSoundController';
import SimpleSoundLoopController from './SimpleSoundLoopController';
import SoundMetrics from '../../model/sounds/SoundMetrics';

import { APP } from '../main/globals';
import { createLoader } from '../interaction/resources/loaders';
import Queue from '../interaction/resources/loaders/Queue';
import { Utils } from '../../model/Utils';

/**
 * @class
 * @extends SimpleController
 * @classdesc Base sounds controller. Provides 2 types of sounds: FX sounds and music (bg sounds).
 */
class SoundsController extends SimpleController
{
	//CL INTERFACE...
	static i_EVENT_SOUND_PLAYING_STARTED 		= SimpleSoundController.i_EVENT_SOUND_PLAYING_STARTED;
	static i_EVENT_SOUND_PLAYING_COMPLETED 		= SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETED;
	static i_EVENT_SOUND_PLAYING_COMPLETION 	= SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETION;
	static i_EVENT_SOUND_PLAYING_LOOP 			= SimpleSoundController.i_EVENT_SOUND_PLAYING_LOOP;
	//...CL INTERFACE

	//IL INTERFACE...
	/**
	 * Creats sounds queue for loader
	 * @param assets - sound assets (from descriptor)
	 * @param [path] - base path to sound assets folder
	 * @returns {Queue}
	 */
	createLoaderQueue(assets, path = APP.contentPathURLsProvider.soundsPath) 
	{
		let queue = new Queue();
		let subdir = this.__getInfo().soundsStereoMode ? 'stereo' : 'mono';
		if (this.__getInfo().soundsLoadingAvailable)
		{
			for (let asset of assets) 
			{
				if (asset.isMobile !== undefined)
				{
					if (APP.isMobile && !asset.isMobile) continue;
					if (!APP.isMobile && asset.isMobile) continue;
				}

				//check vfx
				if (asset.vfx !== undefined && APP.profilingController.info.isVfxProfileValueLessThan(asset.vfx) )
				{
					continue;
				}

				let src = asset.src;
				if (Array.isArray(src)) 
				{
					for (let i = 0; i < src.length; i++) 
					{
						src[i] = `${path}/${subdir}/${src[i]}`;
					}
				}
				else if (Utils.isString(src))
				{
					if (src.includes("/") || src.includes(".")) // a path or an extension
					{
						console.error(`[file: ${asset.name}] ${src} looks like here must be parent. The parent's name can't contain '/' or '.' signs.`);

						// [owl] TODO: delete this after converting all games' sounds
						src = `${path}/${subdir}/${src}`;
					}
					else
					{
						let lParent_obj = assets.find(el => el.name === src);
						if (lParent_obj && Array.isArray(lParent_obj.src))
						{
							src = lParent_obj.src;
						}
						else
						{
							console.error(`Parent of ${asset.name} is not defined or it's src is not an array`);
						}
					}
				}

				let loader = createLoader(src);
				loader.name = asset.name || null;
				queue.add(loader);
			}
		}
		return queue;
	}

	/**
	 * Checks whether FX sound is playing or not.
	 * @param {string} aSoundId_str - Target sound name
	 * @returns {boolean}
	 */
	isSoundPlaying(aSoundId_str)
	{
		var lSound_ussc = this._fFxSounds_ussc_obj[aSoundId_str];
		if (!lSound_ussc)
		{
			return false;
		}
		return lSound_ussc.i_isSoundPlaying();

	}

	/**
	 * Checks whether music is playing or not.
	 * @param {string} aSoundId_str - Target sound name
	 * @returns {boolean}
	 */
	isBGSoundPlaying(aSoundId_str)
	{
		var lSound_ussc = this._fMusicSounds_usslc_obj[aSoundId_str];
		if (!lSound_ussc)
		{
			return false;
		}
		return lSound_ussc.i_isSoundPlaying();

	}

	/**
	 * Sets additional multiplier for sound volume.
	 * @param {string} aSoundId_str 
	 * @param {number} aFadeMultiplier 
	 */
	setFadeMultiplier(aSoundId_str, aFadeMultiplier)
	{
		this._setFadeMultiplier(aSoundId_str, aFadeMultiplier);
	}

	//[E] TODO: temporary ...
	/**
	 * Start sound playing
	 * @param {string} aSoundId_str 
	 * @param {boolean} aOptLoop_bl 
	 * @param {number} aOptVolumeMultiplier_num 
	 * @param {boolean} aOptIsCoop_bl - True for co-player's sound
	 */
	play(aSoundId_str, aOptLoop_bl, aOptVolumeMultiplier_num = 1, aOptIsCoop_bl = false)
	{
		if (aOptIsCoop_bl)
		{
			aSoundId_str += '_coop';
		}

		return this.__playSound(aSoundId_str, aOptLoop_bl, aOptVolumeMultiplier_num);
	}

	/**
	 * Stop sound playing
	 * @param {string} aSoundId_str 
	 */
	stop(aSoundId_str)
	{
		this.__stopSound(aSoundId_str);
	}
	//[E] TODO: ... temporary

	/**
	 * Resets volume for all sounds (to 1)
	 */
	resetAllSoundsVolume()
	{
		this._resetAllSoundsVolume();
	}
	
	/**
	 * Smoothly set expected sound volume for a certain time.
	 * @param {string} aSoundName_str - Sound name.
	 * @param {number} aTargetVolume_num - Expected sound volume.
	 * @param {number} aTimeInMs_int - Volume change duration.
	 * @param {Function} aEase_fnc - Easing function.
	 * @param {Function} aOptOnComplete_fnc - Callback for volume change completion.
	 */
	setVolumeSmoothly(aSoundName_str, aTargetVolume_num, aTimeInMs_int, aEase_fnc, aOptOnComplete_fnc)
	{
		this._setVolumeSmoothly(aSoundName_str, aTargetVolume_num, aTimeInMs_int, aEase_fnc, aOptOnComplete_fnc);
	}

	init(aIsSoundsStereoMode_bl = true, aIsSoundsLoadingAvailable_bl = true)
	{
		super.init();

		let info = this.__getInfo();
		info.soundsStereoMode = aIsSoundsStereoMode_bl;
		info.soundsLoadingAvailable = aIsSoundsLoadingAvailable_bl;
		info.soundsAllowedToPlay = aIsSoundsLoadingAvailable_bl;
	}

	/**
	 * Returns sound controller for specified sound.
	 * @param {string} aSoundName_str
	 * @param {boolean} aOptLoop_bl
	 * @returns {SimpleSoundController}
	 */
	i_getSoundController(aSoundName_str, aOptLoop_bl)
	{
		var lSoundDescription_obj = this.__getSoundDescription(aSoundName_str);

		if (!lSoundDescription_obj)
		{
			return;
		}
		
		var l_ussc;
		switch (lSoundDescription_obj.i_getType())
		{
			case SoundMetrics.i_SOUND_MUSIC:
				l_ussc = this.__getMusicSoundController(aSoundName_str, aOptLoop_bl);
				break;
			case SoundMetrics.i_SOUND_FX:
				l_ussc = this.__getFxSoundController(aSoundName_str, aOptLoop_bl);
				break;
		}
		return l_ussc;
	}

	/**
	 * Set expected sound volume.
	 * @param {string} aSoundName_str 
	 * @param {number} aTargetVolume_num 
	 */
	i_setVolume(aSoundName_str, aTargetVolume_num)
	{
		this._setVolume(aSoundName_str, aTargetVolume_num);
	}
	//...IL INTERFACE

	//IL CONSTRUCTION...
	constructor(aOptInfo_ussi)
	{
		super(aOptInfo_ussi ? aOptInfo_ussi : new SoundsInfo());
		
		//IL IMPLEMENTATION...
		/**
		 * FX sounds
		 * @ignore
		 */
		this._fFxSounds_ussc_obj = null;

		/**
		 * Music (bg) sounds
		 * @ignore
		 */
		this._fMusicSounds_usslc_obj = null;

		this._initUSoundsController();
	}
	//...IL CONSTRUCTION

	//ILI INIT...
	_initUSoundsController()
	{
		this._fFxSounds_ussc_obj = {};
		this._fMusicSounds_usslc_obj = {};

		createjs.Sound.initializeDefaultPlugins();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on("onSoundsPaused", this.__disableAllSounds, this);
		APP.on("onSoundsResumed", this.__enableAllSounds, this);

		if (!APP.tickerAllowed)
		{
			this.__disableAllSounds();
		}

		APP.on("ready", this._onApplicationReady, this);
	}

	_onApplicationReady(event)
	{
		APP.layout.on("hidewindow", this._onHideWindow, this);
		APP.layout.on("showwindow", this._onShowWindow, this);
	}

	/*handler on EVENT_ON_SOUND_SETTINGS_CHANGED - should be assigned in inherited class*/
	__updateSoundSettings(event)
	{
		var lInfo_ussi = this.__getInfo();

		if (event.loadingAvailable !== undefined && event.loadingAvailable !== lInfo_ussi.soundsLoadingAvailable)
		{
			lInfo_ussi.soundsLoadingAvailable = event.loadingAvailable;
		}

		if (event.muted !== undefined && event.muted !== lInfo_ussi.soundsMuted)
		{
			lInfo_ussi.soundsMuted = event.muted;

			lInfo_ussi.soundsMusicMuted = event.muted;
			lInfo_ussi.soundsFxMuted = event.muted;
			
			event.muted && this._resetAllSoundsVolume();
		}

		lInfo_ussi.soundsMusicMuted = event.musicVolume == 0 ? true : false;
		lInfo_ussi.soundsFxMuted = event.fxVolume == 0 ? true : false;

		lInfo_ussi.i_setSoundsVolume(event.fxVolume, SoundMetrics.i_SOUND_FX);
		lInfo_ussi.i_setSoundsVolume(event.musicVolume, SoundMetrics.i_SOUND_MUSIC);

		this._applySoundsVolumeToAllExistentSounds();
	}
	//...ILI INIT

	//ALL SOUNDS...
	_onHideWindow(event)
	{
		this.__stopAllFxSounds();
	}

	_onShowWindow(event)
	{
		this.__startFxSounds();
	}

	_resetAllSoundsVolume()
	{
		this._resetMusicVolume();
		this._resetFxVolume();
	}

	_resetMusicVolume()
	{
		for (let l_ussc in this._fMusicSounds_usslc_obj)
		{
			this._fMusicSounds_usslc_obj[l_ussc].i_resetVolume();
		}
	}

	_resetFxVolume()
	{
		for (let l_ussc in this._fFxSounds_ussc_obj)
		{
			this._fFxSounds_ussc_obj[l_ussc].i_resetVolume();
		}
	}

	__disableAllSounds(event)
	{
		this.__getInfo().enabled = false;
		this._applySoundsVolumeToAllExistentSounds();
	}

	__enableAllSounds(event)
	{
		this.__getInfo().enabled = true;
		this._applySoundsVolumeToAllExistentSounds();
	}

	__playSound(aSoundName_str, aOptLoop_bl)
	{
		if (!this.__getInfo().soundsAllowedToPlay)
		{
			return;
		}
		
		var lSound_ussc = this.i_getSoundController(aSoundName_str, aOptLoop_bl);

		lSound_ussc && lSound_ussc.i_startPlaying();
		return lSound_ussc;
	}
	
	__stopSound(aSoundName_str)
	{
		var lSound_uss = this.i_getSoundController(aSoundName_str);
		lSound_uss && lSound_uss.i_stopPlaying(this._isMuted(aSoundName_str));
	}

	_isMuted(aSoundName_str)
	{
		let lSoundDescription_obj = this.__getSoundDescription(aSoundName_str);

		if (!lSoundDescription_obj)
		{
			return true;
		}

		let lisMuted_bl;
		switch (lSoundDescription_obj.i_getType())
		{
			case SoundMetrics.i_SOUND_MUSIC:
				lisMuted_bl = this.i_getInfo().soundsMusicMuted;
				break;
			case SoundMetrics.i_SOUND_FX:
				lisMuted_bl = this.i_getInfo().soundsFxMuted;
				break;
		}

		return lisMuted_bl;
	}

	_setVolume(aSoundName_str, aTargetVolume_num)
	{
		let lSound_ussc = this.i_getSoundController(aSoundName_str);

		if (!lSound_ussc)
		{
			return;
		}

		if(this._isMuted(aSoundName_str))
		{
			return;
		}
		else
		{
			let absoluteVolume = this._getAbsoluteVolume(aSoundName_str);
			let relativeVolume = aTargetVolume_num * absoluteVolume;

			lSound_ussc.i_setVolume(relativeVolume);
		}
	}

	_setFadeMultiplier(aSoundId_str, aFadeMultiplier)
	{
		let lSound_ussc = this.i_getSoundController(aSoundId_str);

		if(lSound_ussc)
		{
			lSound_ussc.setFadeMultiplier(aFadeMultiplier);
		}
	}

	_setVolumeSmoothly(aSoundName_str, aTargetVolume_num, aTimeInMs_int, aEase_fnc, aOptOnComplete_fnc)
	{
		let lSound_ussc = this.i_getSoundController(aSoundName_str);
		if(lSound_ussc)
		{
			lSound_ussc.i_setVolumeSmoothly(aTargetVolume_num, aTimeInMs_int, aEase_fnc, aOptOnComplete_fnc)
		}
	}

	_getAbsoluteVolume(aSoundName_str)
	{
		let lSoundDescription_obj = this.__getSoundDescription(aSoundName_str);

		if (!lSoundDescription_obj)
		{
			return 0;
		}

		let lAbsoluteVolume;
		switch (lSoundDescription_obj.i_getType())
		{
			case SoundMetrics.i_SOUND_MUSIC:
				lAbsoluteVolume = this.i_getInfo().i_getBgSoundsVolume();
				break;
			case SoundMetrics.i_SOUND_FX:
				lAbsoluteVolume = this.i_getInfo().i_getFxSoundsVolume();
				break;
		}

		return lAbsoluteVolume;
	}

	/*reduction of volume independently from common (in Options) and private (in sound descriptor) volume*/
	_multiplySoundVolume(aSoundNames_str_arr, aVolumeMultiplier_num)
	{
		for (var i = 0; i < aSoundNames_str_arr.length; i++) 
		{
			let lSound_ussc = this.i_getSoundController(aSoundNames_str_arr[i]);
			lSound_ussc && lSound_ussc.i_multiplyVolume(aVolumeMultiplier_num);
		}
	}

	_applySoundsVolumeToAllExistentSounds()
	{
		this._applySoundsVolumeToSounds(this._fMusicSounds_usslc_obj, this.__getInfo().i_getBgSoundsVolume());
		this._applySoundsVolumeToSounds(this._fFxSounds_ussc_obj,	 this.__getInfo().i_getFxSoundsVolume());
	}

	_applySoundsVolumeToSounds(aSounds_obj, aVolume_num)
	{
		for (let lSoundName_str in aSounds_obj)
		{
			this.__applySoundsVolumeToSound(aSounds_obj[lSoundName_str], aVolume_num);
		}
	}

	__applySoundsVolumeToSound(aSound_ussc, aVolume_num)
	{
		if(!this.__getInfo().enabled && this._isSoundIncludedInDisableExceptionList(aSound_ussc.i_getInfo().i_getSoundName()))
		{
			return;
		}

		aSound_ussc.i_setVolume(aVolume_num);
	}

	_isSoundIncludedInDisableExceptionList(aSoundName_str)
	{
		let lIsExeption_bl = false;

		let lDisableExceptionList_arr = this.__getDisableExceptionList()
		for (let i = 0; i < lDisableExceptionList_arr.length; i++)
		{
			if(lDisableExceptionList_arr[i] == aSoundName_str)
			{
				lIsExeption_bl = true;
			}
		}

		return lIsExeption_bl;
	}

	__getDisableExceptionList()
	{
		return [];
	}
	//...ALL SOUNDS

	__getFxSoundController(aSoundName_str, aOptLoop_bl = false)
	{
		var lSound_ussc = this._fFxSounds_ussc_obj[aSoundName_str];
		if (!lSound_ussc)
		{
			lSound_ussc = this.__initSoundController(aSoundName_str, aOptLoop_bl, this.__getInfo().i_getFxSoundsVolume());
			if (lSound_ussc)
			{
				this._fFxSounds_ussc_obj[aSoundName_str] = lSound_ussc;
			}
		}
		return lSound_ussc;
	}

	__stopAllFxSounds()
	{
		for (let lSoundName_str in this._fFxSounds_ussc_obj)
		{
			this.__stopSound(lSoundName_str);
		}
	}

	__startFxSounds()
	{
	}

	__getMusicSoundController(aSoundName_str, aOptLoop_bl = true)
	{
		var lSound_ussc = this._fMusicSounds_usslc_obj[aSoundName_str];
		if (!lSound_ussc)
		{
			lSound_ussc = this.__initSoundController(aSoundName_str, aOptLoop_bl, this.__getInfo().i_getBgSoundsVolume());
			if (lSound_ussc)
			{
				this._fMusicSounds_usslc_obj[aSoundName_str] = lSound_ussc;
			}
		}
		return lSound_ussc;
	}

	__initSoundController(aSoundName_str, aOptLoop_bl, aOptDefaultVolume_num = 1)
	{
		let lSound_ussc;
		let lSoundDescription_obj = this.__getSoundDescription(aSoundName_str);

		if (!lSoundDescription_obj)
		{
			return;
		}

		let l_ussi = new SimpleSoundInfo(lSoundDescription_obj, aSoundName_str, !!aOptLoop_bl);

		if (aOptLoop_bl)
		{
			lSound_ussc = new SimpleSoundLoopController(l_ussi);
			lSound_ussc.on(SimpleSoundController.i_EVENT_SOUND_PLAYING_LOOP, this.__onSoundPlayingLoop, this);
		}
		else
		{
			lSound_ussc = new SimpleSoundController(l_ussi);
			lSound_ussc.on(SimpleSoundController.i_EVENT_SOUND_PLAYING_STARTING, this.__onSoundPlayingStarting, this);
			lSound_ussc.on(SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETION, this.__onSoundPlayingCompletion, this);
			lSound_ussc.on(SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETED, this.__onSoundPlayingCompleted, this);
		}

		lSound_ussc.once(SimpleSoundController.i_EVENT_SOUND_DESTROYING, this.__onSoundDestroying, this);

		lSound_ussc.i_setVolume(aOptDefaultVolume_num);
		return lSound_ussc;
	}

	__onSoundDestroying(event)
	{
		let soundName = event.soundName;

		if (this._fMusicSounds_usslc_obj && this._fMusicSounds_usslc_obj[soundName])
		{
			delete this._fMusicSounds_usslc_obj[soundName];
		}

		if (this._fFxSounds_ussc_obj && this._fFxSounds_ussc_obj[soundName])
		{
			delete this._fFxSounds_ussc_obj[soundName];
		}
	}
	
	__getSoundDescription(aSoundName_str)
	{
		try
		{
			return this.__getInfo().i_getSoundDescription(aSoundName_str);
		}
		catch(e)
		{
			APP.logger.i_pushWarning(`SoundController. Failed to get sound descriptor for sound: ${aSoundName_str}`);
			console.error("FAILED TO GET SOUND DESCRIPTOR FOR SOUND >> " + aSoundName_str);
		}
	}

	//NON_LOOPED...
	__onSoundPlayingStarting(event)
	{
		this.emit(event);
	}

	__onSoundPlayingCompletion(event)
	{
		this.emit(event);
	}

	__onSoundPlayingCompleted(event)
	{
		this.emit(event);
	}

	__onSoundPlayingLoop()
	{
	}
	//...NON_LOOPED

	//LOOPED...	
	__disposeLoopSound(aSoundName_str)
	{
		if (this._fMusicSounds_usslc_obj && this._fMusicSounds_usslc_obj[aSoundName_str])
		{
			var lSoundLoop_ussc = this._fMusicSounds_usslc_obj[aSoundName_str]
			lSoundLoop_ussc.i_destroy();
			
			delete this._fMusicSounds_usslc_obj[aSoundName_str];
		}
	}
	
	__isLoopSoundExist(aSoundName_str)
	{
		return this._fMusicSounds_usslc_obj && this._fMusicSounds_usslc_obj[aSoundName_str] !== null && this._fMusicSounds_usslc_obj[aSoundName_str] !== undefined;
	}
}

export default SoundsController;