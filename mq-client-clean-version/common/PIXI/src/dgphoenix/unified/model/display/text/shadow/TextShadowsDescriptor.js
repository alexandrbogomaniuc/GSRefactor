import TextShadowDescriptor from './TextShadowDescriptor';

/**
 * Storage of text shadow descriptors.
 * @class
 */
class TextShadowsDescriptor
{
	/**
	 * @constructor
	 * @param {TextShadowDescriptor[]} [aOptTextShadowDescriptors_utsd_arr] 
	 */
	constructor(aOptTextShadowDescriptors_utsd_arr)
	{
		this._textShadowDescriptors_arr = null;
		this._textShadowsCacheScale = undefined;

		this._initTextShadowsDescriptor(aOptTextShadowDescriptors_utsd_arr);
	}

	/** Add text shadow descriptor to storage. */
	addTextShadowDescriptor (aTextShadowDescriptor_utsd)
	{
		this._addTextShadowDescriptor(aTextShadowDescriptor_utsd);
	}

	/** Get text shadow descriptor by index. */
	getTextShadowDescriptor (aIntId_int)
	{
		return this._textShadowDescriptors_arr[aIntId_int];
	}

	/** Amount of text shadow descriptors in storage. */
	get textShadowDescriptorsCount ()
	{
		return this._textShadowDescriptors_arr.length;
	}
	
	/** Clone storage instrance. */
	clone ()
	{
		var l_utshsd = new TextShadowsDescriptor();
		var lShadowDescriptorsCount_int = this._textShadowDescriptors_arr.length;
		for (var i = 0; i < lShadowDescriptorsCount_int; i++)
		{
			l_utshsd._addTextShadowDescriptor(this._textShadowDescriptors_arr[i].clone());
		}
		return l_utshsd;
	}
	
	_initTextShadowsDescriptor (aOptTextShadowDescriptors_utsd_arr)
	{
		if (aOptTextShadowDescriptors_utsd_arr
			&& !(aOptTextShadowDescriptors_utsd_arr instanceof Array))
		{
			throw new Error ("Invalid text shadow descriptors array argument: " + aOptTextShadowDescriptors_utsd_arr);
		}

		this._textShadowDescriptors_arr = new Array();

		var lCount_int = 0;
		if (aOptTextShadowDescriptors_utsd_arr
			&& (lCount_int = aOptTextShadowDescriptors_utsd_arr.length))
		{
			for (var i = 0; i < lCount_int; i++)
			{
				this._addTextShadowDescriptor(aOptTextShadowDescriptors_utsd_arr[i]);
			}
		}
	}

	_addTextShadowDescriptor (aTextShadowDescriptor_utsd)
	{
		if (!(aTextShadowDescriptor_utsd instanceof TextShadowDescriptor))
		{
			throw new Error("Invalid Text Shadow Descriptor argument: " + aTextShadowDescriptor_utsd);
		}

		this._textShadowDescriptors_arr.push(aTextShadowDescriptor_utsd);
	}
}

export default TextShadowsDescriptor