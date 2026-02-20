import SimpleXMLParser from '../../xml/SimpleXMLParser';
import AlignDescriptor from './AlignDescriptor';

/**
 * Parser of align descriptor in XML format.
 * @class
 * 
 * @example
 * Align descriptor:
 * <align h="center" v="middle"/>
 */
class AlignDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} alignDescriptorElement - XML element
	 */
	constructor(alignDescriptorElement)
	{
		this._alignDescriptor = null;

		this._initAlignDescriptorParser(alignDescriptorElement);
	}

	/**
	 * Align descriptor instance.
	 * @type {AlignDescriptor}
	 * @readonly
	 */
	get alignDescriptor()
	{
		return this._alignDescriptor;
	}

	_initAlignDescriptorParser(alignDescriptorElement)
	{
		if (alignDescriptorElement)
		{
			this._parse(alignDescriptorElement);
		}
	}

	_parse(alignDescriptorElement)
	{
		var lHAlign_obj = SimpleXMLParser.getAttributeValue(alignDescriptorElement, "h", true);
		var lVAlign_obj = SimpleXMLParser.getAttributeValue(alignDescriptorElement, "v", true);
		
		this._alignDescriptor = new AlignDescriptor(lHAlign_obj, lVAlign_obj);
	}

}

export default AlignDescriptorParser;