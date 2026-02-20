import SimpleXMLParser from '../../xml/SimpleXMLParser';
import ColorDescriptor from './ColorDescriptor';
import StrokeDescriptor from './StrokeDescriptor';

/**
 * Parser of stroke descriptor (XML element).
 * @class
 * @example
 * Stroke descriptor XML element:
 * <stroke color="0x303030" thickness="1"/>
 */
class StrokeDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} strokeDescriptorElement - Stroke XML element.
	 */
	constructor(strokeDescriptorElement)
	{
		this._strokeDescriptor = null;

		this._initStrokeDescriptorParser(strokeDescriptorElement);
	}

	/**
	 * Stroke descriptor instance.
	 * @type {StrokeDescriptor}
	 */
	get strokeDescriptor()
	{
		return this._strokeDescriptor;
	}

	_initStrokeDescriptorParser(strokeDescriptorElement)
	{
		if (strokeDescriptorElement)
		{
			this._parse(strokeDescriptorElement);
		}
	}

	_parse(strokeDescriptorElement)
	{
		var lColor_str = SimpleXMLParser.getAttributeValue(strokeDescriptorElement, "color", true);
		var colorDescriptor = new ColorDescriptor(Number(lColor_str));

		var tickness = +SimpleXMLParser.getAttributeValue(strokeDescriptorElement, "thickness", true);

		this._strokeDescriptor = new StrokeDescriptor(colorDescriptor, tickness);
	}
}

export default StrokeDescriptorParser;