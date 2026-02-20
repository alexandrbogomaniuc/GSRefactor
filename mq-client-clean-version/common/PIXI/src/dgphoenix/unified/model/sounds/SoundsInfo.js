import SoundMetrics from "./SoundMetrics";

/**
 * Sounds info.
 * @class
 */
class SoundsInfo
{
	static _SOUND_PROPERTY_LENGTH 	= "length";
	static _SOUND_PROPERTY_TYPE 	= "type";

	//IL CONSTRUCTION...
	constructor()
	{
		//IL IMPLEMENTATION...
		this._fSoundsLoadingAvailable_bl = true;
		this._fSoundsStereoMode_bl = true;

		this._fSoundsVolume_num_arr = [];
		this._fSoundsMuted_bl = true;

		this._fSoundsMusicMuted_bl = true;
		this._fSoundsFxMuted_bl = true;

		this._fSoundsEnabled_bl = true;
		this._fSoundsAllowedToPlay_bl = true;

		for (let i = 0; i < SoundMetrics.i_SUPPORTED_SOUND_TYPES.length; i++) 
		{
			this._fSoundsVolume_num_arr.push(0);
		}

		this._fSoundsMetrics_usms_obj = {};

		this._initUSoundsInfo();
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...
	/**
	 * Init sounds metrics from sounds descriptor.
	 * @param {*} aSoundsMetrics_obj - Sounds descriptor.
	 */
	i_initSoundsMetrics(aSoundsMetrics_obj)
	{
		this._initSoundsMetrics(aSoundsMetrics_obj);
	}

	/**
	 * Get metrics of the sound.
	 * @param {string} aSoundName_str - Target sound name.
	 * @returns {SoundMetrics}
	 */
	i_getSoundDescription(aSoundName_str)
	{
		if (!aSoundName_str || !this._fSoundsMetrics_usms_obj[aSoundName_str])
		{
			throw new Error("Unregistered sound name: " + aSoundName_str);
		}
		
		return this._fSoundsMetrics_usms_obj[aSoundName_str];
	}

	/**
	 * Set volume for all sounds or only for specific sounds type.
	 * @param {*} aSoundsVolume_num 
	 * @param {string} [aOptSoundType_str=null] - Sounds type.
	 */
	i_setSoundsVolume(aSoundsVolume_num, aOptSoundType_str = null)
	{
		if (aOptSoundType_str)
		{
			let i = SoundMetrics.i_SUPPORTED_SOUND_TYPES.indexOf(aOptSoundType_str);
			this._fSoundsVolume_num_arr[i] = aSoundsVolume_num;
		}
		else
		{
			for (let i = 0; i < SoundMetrics.i_SUPPORTED_SOUND_TYPES.length; i++)
			{
				this._fSoundsVolume_num_arr[i] = aSoundsVolume_num;
			}
		}
	}

	/**
	 * Gets volume of FX sounds.
	 * @returns {number}
	 */
	i_getFxSoundsVolume()
	{
		return this.i_getSoundsVolume(SoundMetrics.i_SOUND_FX);
	}

	/**
	 * Gets volume of music sounds.
	 * @returns {number}
	 */
	i_getBgSoundsVolume()
	{
		return this.i_getSoundsVolume(SoundMetrics.i_SOUND_MUSIC);
	}
	
	/**
	 * Gets volume of specific sounds type.
	 * @param {string} aOptSoundType_str - Sounds type (fx or music).
	 * @param {boolean} [aIgnoreEnabledState_bl=false] - Should sounds enable state be ignored or not.
	 * @returns {number}
	 */
	i_getSoundsVolume(aOptSoundType_str = null, aIgnoreEnabledState_bl = false)
	{
		var lSoundsVolume_num = 0;
		if (aOptSoundType_str)
		{
			let i = SoundMetrics.i_SUPPORTED_SOUND_TYPES.indexOf(aOptSoundType_str);
			lSoundsVolume_num = this._fSoundsVolume_num_arr[i];
		}

		if (!aIgnoreEnabledState_bl && !this.enabled)
		{
			return 0;
		}
		return lSoundsVolume_num;
	}

	/**
	 * Checks if sounds volume is on at least for one sounds type.
	 * @type {boolean}
	 */
	get isSoundsVolumeOn()
	{
		var lSoundsOn_bl = false;
		for (let i = 0; i < SoundMetrics.i_SUPPORTED_SOUND_TYPES.length; i++) 
		{
			lSoundsOn_bl = lSoundsOn_bl || !!this._fSoundsVolume_num_arr[i];
		}
		return lSoundsOn_bl;
	}

	/**
	 * Checks if sounds loading available.
	 * @type {boolean}
	 */
	get soundsLoadingAvailable()
	{
		return this._fSoundsLoadingAvailable_bl;
	}

	/**
	 * Sets sounds loading available state.
	 * @param {boolean} aValue_bl
	 */
	set soundsLoadingAvailable(aValue_bl)
	{
		this._fSoundsLoadingAvailable_bl = !!aValue_bl;
	}

	/**
	 * Is sounds stereo mode on or not.
	 * @type {boolean}
	 */
	get soundsStereoMode()
	{
		return this._fSoundsStereoMode_bl;
	}

	/**
	 * Sets sounds stereo mode on or off.
	 * @param {boolean} aValue_bl
	 */
	set soundsStereoMode(aValue_bl)
	{
		this._fSoundsStereoMode_bl = aValue_bl;
	}

	/**
	 * Are sounds allowed to play.
	 * @type {boolean}
	 */
	get soundsAllowedToPlay()
	{
		return this._fSoundsAllowedToPlay_bl;
	}

	/**
	 * Sets sounds allowed to play mode.
	 */
	set soundsAllowedToPlay(aValue_bl)
	{
		this._fSoundsAllowedToPlay_bl = aValue_bl;
	}

	/**
	 * Are sounds muted or not.
	 * @type {boolean}
	 */
	get soundsMuted()
	{
		return this._fSoundsMuted_bl;
	}

	/**
	 * Sets sounds mute mode.
	 * @param {boolean} aValue_bl
	 */
	set soundsMuted(aValue_bl)
	{
		this._fSoundsMuted_bl = aValue_bl;
	}

	/**
	 * Is sounds playing enabled or not.
	 * @type {boolean}
	 */
	get enabled()
	{
		return this._fSoundsEnabled_bl;
	}

	/**
	 * Update sounds enabled state.
	 * @param {boolean} aEnabled_bl
	 */
	set enabled(aEnabled_bl)
	{
		this._fSoundsEnabled_bl = aEnabled_bl;
	}

	/**
	 * Gets mute state of music sounds type.
	 * @type {boolean}
	 */
	get soundsMusicMuted()
	{
		return this._fSoundsMusicMuted_bl;
	}

	/**
	 * Set mute state for music sounds type.
	 * @param {boolean} aValue_bl
	 */
	set soundsMusicMuted(aValue_bl)
	{
		this._fSoundsMusicMuted_bl = aValue_bl;
	}

	/**
	 * Gets mute state of FX sounds type.
	 * @type {boolean}
	 */
	get soundsFxMuted()
	{
		return this._fSoundsFxMuted_bl;
	}

	/**
	 * Set mute state for FX sounds type.
	 * @param {boolean} aValue_bl
	 */
	set soundsFxMuted(aValue_bl)
	{
		this._fSoundsFxMuted_bl = aValue_bl;
	}
	//...IL INTERFACE

	//ILI INIT...
	_initUSoundsInfo()
	{
		this.__fillSoundsMetrics();
	}
	//...ILI INIT

	//IL IMPLEMENTATION...
	__fillSoundsMetrics()
	{
	}
	
	_initSoundsMetrics(aSoundsMetrics_obj)
	{
		var lLength_str = SoundsInfo._SOUND_PROPERTY_LENGTH;
		var lType_str = SoundsInfo._SOUND_PROPERTY_TYPE;

		for (let asset of aSoundsMetrics_obj) 
		{
			let lSoundName_str = asset.name;
			let lMetrics_obj = asset.descr;
			if (lMetrics_obj) 
			{
				if (lMetrics_obj[lLength_str] && lMetrics_obj[lType_str])
				{
					this.__addSoundMetrics(lSoundName_str, lMetrics_obj[lLength_str], lMetrics_obj[lType_str], lMetrics_obj.offset, lMetrics_obj.volume, lMetrics_obj.panning);
				}
				else
				{
					throw new Error("Property 'length' and 'type' must be declared in " + lSoundName_str);
				}
			}
			else
			{
				throw new Error("Sound metrics is not defined for sound " + lSoundName_str);
			}
		}
	}

	__addSoundMetrics(aSoundName_str, aLength_num, aType_str, aOptOffset_num, aOptVolume_num, aOptPanning_num)
	{
		this._fSoundsMetrics_usms_obj[aSoundName_str] = new SoundMetrics(aLength_num, aType_str, aOptOffset_num, aOptVolume_num, aOptPanning_num);
	}
	//...IL IMPLEMENTATION
}

export default SoundsInfo;