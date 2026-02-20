import SimpleXMLParser from '../../../xml/SimpleXMLParser';
import FillDescriptorParser from '../../color/FillDescriptorParser';
import ColorDescriptor from '../../color/ColorDescriptor';
import TextFontDescriptor from './TextFontDescriptor';
import StrokeDescriptorParser from '../../color/StrokeDescriptorParser';

/**
 * Parses text font descriptor (from XML).
 * @class
 * 
 * @example
 * Font descriptor XML element example:
 * <font name="fnt_nm_barlow" size="8" color="0xffffff"></font>
 */
class TextFontDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} textFontDescriptorElement - XML element.
	 */
	constructor(textFontDescriptorElement)
	{
		this._fontDescriptor = null;

		this._initTextFontDescriptorParser(textFontDescriptorElement);
	}

	/**
	 * Font descriptor.
	 * @type {TextFontDescriptor}
	 */
	get fontDescriptor()
	{
		return this._fontDescriptor;
	}

	_initTextFontDescriptorParser(textFontDescriptorElement)
	{
		if (textFontDescriptorElement)
		{
			this._parse(textFontDescriptorElement);
		}
	}

	_parse(textFontDescriptorElement)
	{
		var lFontName_str = SimpleXMLParser.getAttributeValue(textFontDescriptorElement, "name", true);

		var lFontSize_num = Number(SimpleXMLParser.getAttributeValue(textFontDescriptorElement, "size", true));

		var lBold_str = SimpleXMLParser.getAttributeValue(textFontDescriptorElement, "bold", false);
		var lBold_bl = lBold_str ? lBold_str.toLowerCase() === "true" : false;

		var lColor_str = SimpleXMLParser.getAttributeValue(textFontDescriptorElement, "color", true);
		var lColor_obj;
		var lFillDescriptor_obj;
		if (lColor_str === "non-uniform")
		{
			lColor_obj = new FillDescriptorParser(SimpleXMLParser.getTag(textFontDescriptorElement, "fill", 0, true)).fillDescriptor;
		}
		else
		{
			lColor_obj = new ColorDescriptor(Number(lColor_str));
		}

		var lStroke_obj;
		var lStrokeDescriptor_e = SimpleXMLParser.getTag(textFontDescriptorElement.childNodes, "stroke", 0, false);
		if (lStrokeDescriptor_e)
		{
			lStroke_obj = new StrokeDescriptorParser(lStrokeDescriptor_e).strokeDescriptor;
		}

		var lFontStyle_str = SimpleXMLParser.getAttributeValue(textFontDescriptorElement, "style", false);

		this._fontDescriptor = new TextFontDescriptor(lFontName_str, lFontSize_num, lColor_obj, lBold_bl, lStroke_obj, lFontStyle_str);
	}
}

export default TextFontDescriptorParser;