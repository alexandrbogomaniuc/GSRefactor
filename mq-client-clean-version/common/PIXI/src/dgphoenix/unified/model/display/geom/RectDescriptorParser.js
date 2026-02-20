import SimpleXMLParser from '../../xml/SimpleXMLParser';
import { APP } from '../../../controller/main/globals';

/**
 * Parses rect from XML element.
 * @class
 */
class RectDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} rectDescriptorElement - XML element.
	 */
	constructor(rectDescriptorElement)
	{
		this._rectDescriptor = null;

		this._initRectDescriptorParser(rectDescriptorElement);
	}

	/**
	 * Rectangle instance.
	 * @type {PIXI.Rectangle}
	 */
	get rectDescriptor()
	{
		return this._rectDescriptor;
	}

	_initRectDescriptorParser(rectDescriptorElement)
	{
		if (rectDescriptorElement)
		{
			this._parse(rectDescriptorElement);
		}
	}

	_parse(rectDescriptorElement)
	{
		let rectScale = APP.layout.bitmapScale;

		var lX_num = Number(SimpleXMLParser.getAttributeValue(rectDescriptorElement, "x", true))//*rectScale;
		var lY_num = Number(SimpleXMLParser.getAttributeValue(rectDescriptorElement, "y", true))//*rectScale;
		var lW_num = Number(SimpleXMLParser.getAttributeValue(rectDescriptorElement, "w", true))//*rectScale;
		var lH_num = Number(SimpleXMLParser.getAttributeValue(rectDescriptorElement, "h", true))//*rectScale;
		
		this._rectDescriptor = new PIXI.Rectangle(lX_num, lY_num, lW_num, lH_num);
	}

}

export default RectDescriptorParser;