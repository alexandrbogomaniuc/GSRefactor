import { Utils } from '../Utils';

/**
 * Sound metrics descriptor.
 * @class
 */
class SoundMetrics
{
	//CL INTERFACE...
	static i_MIN_SUPPORTED_SOUND_LENGTH = 0.001;
	
	static i_SOUND_MUSIC = "bg";
	static i_SOUND_FX = "fx";
	static i_SUPPORTED_SOUND_TYPES = [SoundMetrics.i_SOUND_MUSIC, SoundMetrics.i_SOUND_FX];
	//...CL INTERFACE

	//IL CONSTRUCTION...
	/**
	 * @constructor
	 * @param {number} aLength_num - Sound duration.
	 * @param {string} aType_str - Sound type (FX or music).
	 * @param {number} aOptOffset_num - Sound playing start offset (in millisecinds).
	 * @param {number} aOptVolume_num - Sound default volume.
	 * @param {number} aOptPanning_num - The pan of the sound.
	 */
	constructor(aLength_num, aType_str, aOptOffset_num, aOptVolume_num, aOptPanning_num)
	{
		//IL IMPLEMENTATION...
		this._fOffset_num = 0;
		this._fLength_num = 0;
		this._fDefaultVolume_num = 1;
		this._fType_str = -1;
		this._fPanning_num = 0;

		this._initUSoundMetrics(aLength_num, aType_str, aOptOffset_num, aOptVolume_num, aOptPanning_num);
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...
	/**
	 * Sound playing start offset (in millisecinds).
	 * @returns {number}
	 */
	i_getOffset()
	{
		return this._fOffset_num;
	}

	/**
	 * Sound duration.
	 * @returns {number}
	 */
	i_getLength()
	{
		return this._fLength_num;
	}
	
	/**
	 * Sound default volume.
	 * @returns {number}
	 */
	i_getDefaultVolume()
	{
		return this._fDefaultVolume_num;
	}

	/**
	 * Sound type (FX or music).
	 * @returns {string}
	 */
	i_getType()
	{
		return this._fType_str;
	}

	/**
	 * The pan of the sound.
	 * @type {number}
	 */
	get panning()
	{
		return this._fPanning_num;
	}
	//...IL INTERFACE

	//ILI INIT...
	_initUSoundMetrics(aLength_num, aType_str, aOptOffset_num, aOptVolume_num, aOptPanning_num)
	{
		if (!Utils.isNumber(aOptOffset_num))
		{
			aOptOffset_num = 0;
		}
		
		if (!Utils.isNumber(aOptVolume_num))
		{
			aOptVolume_num = 1;
		}

		if (!Utils.isNumber(aOptPanning_num))
		{
			aOptPanning_num = 0;
		}
		
		if (
				!Utils.isNumber(aLength_num)
				|| aOptOffset_num < 0
				|| aOptOffset_num >= aLength_num
				|| aLength_num < SoundMetrics.i_MIN_SUPPORTED_SOUND_LENGTH
			)
		{
			throw new Error("Unsupported metrics: " + aLength_num + "/" + aOptOffset_num);
		}

		if (!Utils.isString(aType_str) || SoundMetrics.i_SUPPORTED_SOUND_TYPES.indexOf(aType_str) === -1)
		{
			throw new Error("Unsupported type metric: " + aType_str);
		}
		
		this._fLength_num = aLength_num;
		this._fOffset_num = aOptOffset_num;
		this._fDefaultVolume_num = aOptVolume_num;
		this._fType_str = aType_str;
		this._fPanning_num = aOptPanning_num;
	}
	//...ILI INIT
}

export default SoundMetrics;