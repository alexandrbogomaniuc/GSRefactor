import SimpleXMLParser from '../../../xml/SimpleXMLParser';
import TextShadowDescriptor from './TextShadowDescriptor';
import ColorDescriptor from '../../color/ColorDescriptor';

/**
 * Parses text shadow descriptor (XML element).
 * @class
 * 
 * @example
 * Example of shadow xml descriptor:
 * <shadow color="0x000000" distance="4" angle="90" alpha="0.8" blur="3"/>
 */
class TextShadowDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} textShadowDescriptorElement - XML element.
	 */
	constructor(textShadowDescriptorElement)
	{
		this._textShadowDescriptor = null;

		this._initTextShadowDescriptorParser(textShadowDescriptorElement);
	}

	/**
	 * Text shadow descriptor.
	 * @type {TextShadowDescriptor}
	 */
	get textShadowDescriptor()
	{
		return this._textShadowDescriptor;
	}

	_initTextShadowDescriptorParser(textShadowDescriptorElement)
	{
		if (textShadowDescriptorElement)
		{
			this._parse(textShadowDescriptorElement);
		}
	}

	_parse(textShadowDescriptorElement)
	{
		var lParsedColorValue_str = SimpleXMLParser.getAttributeValue(textShadowDescriptorElement, "color", true);
		var lColor_uc = new ColorDescriptor(this._getColorNumber(lParsedColorValue_str), this._getAlphaNumber(lParsedColorValue_str));

		var lBlurRadius_num = Number(SimpleXMLParser.getAttributeValue(textShadowDescriptorElement, "blur", false));
		var lDistance_num = Number(SimpleXMLParser.getAttributeValue(textShadowDescriptorElement, "distance", false));
		var lAngle_num = Number(SimpleXMLParser.getAttributeValue(textShadowDescriptorElement, "angle", false));
		var lAlpha_num = Number(SimpleXMLParser.getAttributeValue(textShadowDescriptorElement, "alpha", false));
		
		this._textShadowDescriptor = new TextShadowDescriptor(lColor_uc, lBlurRadius_num, lDistance_num, lAngle_num, lAlpha_num);
	}

	_getColorNumber (aColorCodeInHEX_str)
	{
		if (aColorCodeInHEX_str.length > 8)
		{
			return Number(aColorCodeInHEX_str.substr(0, 8));
		}
		return Number(aColorCodeInHEX_str);
	}
	
	_getAlphaNumber (aColorCodeInHEX_str)
	{
		if (aColorCodeInHEX_str.length > 8)
		{
			return parseInt(aColorCodeInHEX_str.substr(8), 16);
		}
		return 255;
	}
}

export default TextShadowDescriptorParser