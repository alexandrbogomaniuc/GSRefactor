import SimpleXMLParser from '../../xml/SimpleXMLParser';

/**
 * Parser of point from XML element.
 * @class
 */
class PointDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} pointDescriptorElement - XML element.
	 */
	constructor(pointDescriptorElement)
	{
		this._pointDescriptor = null;

		this._initTextFontDescriptorParser(pointDescriptorElement);
	}

	/**
	 * Point instance.
	 * @type {PIXI.Point}
	 */
	get pointDescriptor()
	{
		return this._pointDescriptor;
	}

	_initTextFontDescriptorParser(pointDescriptorElement)
	{
		if (pointDescriptorElement)
		{
			this._parse(pointDescriptorElement);
		}
	}

	_parse(pointDescriptorElement)
	{
		var lX_num = Number(SimpleXMLParser.getAttributeValue(pointDescriptorElement, "x", true));
		var lY_num = Number(SimpleXMLParser.getAttributeValue(pointDescriptorElement, "y", true));
		
		this._pointDescriptor = new PIXI.Point(lX_num, lY_num);
	}
}

export default PointDescriptorParser;