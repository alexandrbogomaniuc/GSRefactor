import SoundMetrics from "../../../../../common/PIXI/src/dgphoenix/unified/model/sounds/SoundMetrics";

class SoundSettingsInfo
{
	//IL INTERFACE...
	//VOLUME...
	i_setSoundsVolume(aSoundsVolume_num, aOptSoundType_str = null)
	{
		if (aOptSoundType_str)
		{
			let i = SoundMetrics.i_SUPPORTED_SOUND_TYPES.indexOf(aOptSoundType_str);
			this._fSoundsVolume_num_arr[i] = aSoundsVolume_num;
		}
	}

	i_getFxSoundsVolume()
	{
		return this.i_getSoundsVolume(SoundMetrics.i_SOUND_FX);
	}

	i_getBgSoundsVolume()
	{
		return this.i_getSoundsVolume(SoundMetrics.i_SOUND_MUSIC);
	}
	
	i_getSoundsVolume(aOptSoundType_str = null)
	{
		var lSoundsVolume_num = 0;
		if (aOptSoundType_str)
		{
			let i = SoundMetrics.i_SUPPORTED_SOUND_TYPES.indexOf(aOptSoundType_str);
			lSoundsVolume_num = this._fSoundsVolume_num_arr[i];
		}
		return lSoundsVolume_num;
	}

	i_updateSoundsVolumeBefore()
	{
		for (let i = 0; i < SoundMetrics.i_SUPPORTED_SOUND_TYPES.length; i++) 
		{
			this._fSoundsVolumeBefore_num_arr[i] = this._fSoundsVolume_num_arr[i];
		}
	}

	i_getFxSoundsVolumeBefore()
	{
		return this.i_getSoundsVolumeBefore(SoundMetrics.i_SOUND_FX);
	}

	i_getBgSoundsVolumeBefore()
	{
		return this.i_getSoundsVolumeBefore(SoundMetrics.i_SOUND_MUSIC);
	}

	i_getSoundsVolumeBefore(aSoundType_str)
	{
		var lSoundsVolume_num = 0;
		if (aSoundType_str)
		{
			let i = SoundMetrics.i_SUPPORTED_SOUND_TYPES.indexOf(aSoundType_str);
			lSoundsVolume_num = this._fSoundsVolumeBefore_num_arr[i];
		}
		return lSoundsVolume_num;
	}

	get isSoundsVolumeOn()
	{
		var lSoundsOn_bl = false;
		for (let i = 0; i < SoundMetrics.i_SUPPORTED_SOUND_TYPES.length; i++) 
		{
			lSoundsOn_bl = lSoundsOn_bl || !!this._fSoundsVolume_num_arr[i];
		}
		return lSoundsOn_bl;
	}
	//...VOLUME

	get soundsLoadingAvailable()
	{
		return this._fSoundsLoadingAvailable_bl;
	}

	set soundsLoadingAvailable(aValue_bl)
	{
		this._fSoundsLoadingAvailable_bl = !!aValue_bl;
	}

	get soundsMuted()
	{
		return this._fSoundsMuted_bl;
	}

	set soundsMuted(aValue_bl)
	{
		this._fSoundsMuted_bl = aValue_bl;
	}
	//...IL INTERFACE

	//IL CONSTRUCTION...
	constructor()
	{
		//IL IMPLEMENTATION...
		this._fSoundsLoadingAvailable_bl = true;
		this._fSoundsMuted_bl = true;

		this._fSoundsVolume_num_arr = [];
		this._fSoundsVolumeBefore_num_arr = [];

		for (let i = 0; i < SoundMetrics.i_SUPPORTED_SOUND_TYPES.length; i++) 
		{
			this._fSoundsVolume_num_arr.push(0);
			this._fSoundsVolumeBefore_num_arr.push(1);
		}

		this._initUSoundSettingsInfo();
	}
	//...IL CONSTRUCTION

	//ILI INIT...
	_initUSoundSettingsInfo()
	{
	}
	//...ILI INIT

	//IL IMPLEMENTATION...
	//...IL IMPLEMENTATION
}

export default SoundSettingsInfo;