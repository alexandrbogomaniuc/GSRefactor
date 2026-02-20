import SimpleXMLParser from '../../xml/SimpleXMLParser';
import ScaleDescriptor from './ScaleDescriptor';

/**
 * Parses scale descriptor (XML element).
 * @class
 */
class ScaleDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} scaleDescriptorElement - XML element.
	 */
	constructor(scaleDescriptorElement)
	{
		this._scaleDescriptor = null;

		this._initRectDescriptorParser(scaleDescriptorElement);
	}

	/**
	 * Scale descriptor instance.
	 * @type {ScaleDescriptor}
	 */
	get scaleDescriptor()
	{
		return this._scaleDescriptor;
	}

	_initRectDescriptorParser(scaleDescriptorElement)
	{
		if (scaleDescriptorElement)
		{
			this._parse(scaleDescriptorElement);
		}
	}

	_parse(scaleDescriptorElement)
	{
		var lXScale_obj = SimpleXMLParser.getAttributeValue(scaleDescriptorElement, "x", true);
		var lYScale_obj = SimpleXMLParser.getAttributeValue(scaleDescriptorElement, "y", true);
		var lPreventDistortion_bl = SimpleXMLParser.getAttributeValue(scaleDescriptorElement, "distortion", true) === "false";

		this._scaleDescriptor = new ScaleDescriptor(lXScale_obj, lYScale_obj, lPreventDistortion_bl);
	}

}

export default ScaleDescriptorParser;