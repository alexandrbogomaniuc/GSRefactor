import SimpleXMLParser from '../../../xml/SimpleXMLParser';
import ColorDescriptor from '../ColorDescriptor';
import KeyColorDescriptor from './KeyColorDescriptor';

/**
 * Parser of key color descriptor in gradient fill.
 * @class
 * 
 * @example
 * Key color descriptor:
 * <key position="0" color="0xffffff"/>
 */
class KeyColorDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} keyColorDescriptorElement - Key color XML element.
	 */
	constructor(keyColorDescriptorElement)
	{
		this._keyColorDescriptor = null;

		this._initKeyColorDescriptorParser(keyColorDescriptorElement);
	}

	/**
	 * Key color descriptor.
	 * @type {KeyColorDescriptor}
	 */
	get keyColorDescriptor()
	{
		return this._keyColorDescriptor;
	}

	_initKeyColorDescriptorParser(keyColorDescriptorElement)
	{
		if (keyColorDescriptorElement)
		{
			this._parse(keyColorDescriptorElement);
		}
	}

	_parse(keyColorDescriptorElement)
	{
		var lPosition_num = Number(SimpleXMLParser.getAttributeValue(keyColorDescriptorElement, "position", true));
		var lColor_str = SimpleXMLParser.getAttributeValue(keyColorDescriptorElement, "color", true);
		var lColorDescr = new ColorDescriptor(Number(lColor_str));

		this._keyColorDescriptor = new KeyColorDescriptor(lPosition_num, lColorDescr);
	}
}

export default KeyColorDescriptorParser