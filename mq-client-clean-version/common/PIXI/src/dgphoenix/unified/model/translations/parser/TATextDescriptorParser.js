import SimpleXMLParser from '../../xml/SimpleXMLParser';
import TATextDescriptor from '../data/TATextDescriptor'
import TextFontDescriptorParser from '../../display/text/font/TextFontDescriptorParser';
import TextShadowsDescriptorParser from '../../display/text/shadow/TextShadowsDescriptorParser';

/**
 * Translatable asset text descriptor parser.
 * @class
 */
class TATextDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} textDescriptorElement - XML element
	 */
	constructor(textDescriptorElement)
	{
		this._textDescriptor = null;

		this._initTATextDescriptorParser(textDescriptorElement);
	}

	/**
	 * @type {TATextDescriptor}
	 */
	get textDescriptor()
	{
		return this._textDescriptor;
	}

	_initTATextDescriptorParser(textDescriptorElement)
	{
		var lContentDescriptor_e = SimpleXMLParser.getTag(textDescriptorElement.childNodes, "content", 0, true);
		var lAssetText_str = SimpleXMLParser.getInnerXMLText(lContentDescriptor_e);
		var lAssetContentOverride_bl = String(SimpleXMLParser.getAttributeValue(lContentDescriptor_e, "override", false)).toLowerCase() !== "false";
		var lAssetContent_obj = {};
		lAssetContent_obj.text = SimpleXMLParser.decodeXMLTextStrongEntities(lAssetText_str);
		lAssetContent_obj.override = lAssetContentOverride_bl;

		var lAssetWidth_obj = SimpleXMLParser.getAttributeValue(textDescriptorElement, "width", true);
		if (lAssetWidth_obj !== TATextDescriptor.WIDTH_AUTO)
		{
			lAssetWidth_obj = Number(lAssetWidth_obj);
		}

		var lAssetHeight_obj = SimpleXMLParser.getAttributeValue(textDescriptorElement, "height", false);
		lAssetHeight_obj = lAssetHeight_obj === undefined ? TATextDescriptor.HEIGHT_AUTO : lAssetHeight_obj;
		if (lAssetHeight_obj !== TATextDescriptor.HEIGHT_AUTO
			&& lAssetHeight_obj !== TATextDescriptor.HEIGHT_EXPLICIT)
		{
			lAssetHeight_obj = Number(lAssetHeight_obj);
		}

		var lLineSpacing_obj = SimpleXMLParser.getAttributeValue(textDescriptorElement, "linespacing", false);
		lLineSpacing_obj = lLineSpacing_obj === undefined ? TATextDescriptor.LINE_SPACING_AUTO : lLineSpacing_obj;
		if (lLineSpacing_obj !== TATextDescriptor.LINE_SPACING_AUTO)
		{
			lLineSpacing_obj = Number(lLineSpacing_obj);
		}

		var lLetterSpacing_obj = SimpleXMLParser.getAttributeValue(textDescriptorElement, "letterspacing", false);
		lLetterSpacing_obj = lLetterSpacing_obj === undefined ? TATextDescriptor.LETTER_SPACING_AUTO : lLetterSpacing_obj;
		if (lLetterSpacing_obj !== TATextDescriptor.LETTER_SPACING_AUTO)
		{
			lLetterSpacing_obj = Number(lLetterSpacing_obj);
		}

		var lAlpha_obj = SimpleXMLParser.getAttributeValue(textDescriptorElement, "alpha", false);
		lAlpha_obj = lAlpha_obj === undefined ? TATextDescriptor.ALPHA_AUTO : lAlpha_obj;
		if (lAlpha_obj !== TATextDescriptor.ALPHA_AUTO)
		{
			lAlpha_obj = Number(lAlpha_obj);
		}

		var lPadding_obj = SimpleXMLParser.getAttributeValue(textDescriptorElement, "padding", false);
		lPadding_obj = lPadding_obj === undefined ? TATextDescriptor.PADDING_DEFAULT : lPadding_obj;
		lPadding_obj = Number(lPadding_obj);

		var lAutoWrapMode_bl = String(SimpleXMLParser.getAttributeValue(textDescriptorElement, "auto_wrap", false)).toLowerCase() === "true";

		var lFontDescriptor_utfd = new TextFontDescriptorParser(SimpleXMLParser.getTag(textDescriptorElement.childNodes, "font", 0, true)).fontDescriptor;

		var lFiltersDescriptor_e = SimpleXMLParser.getTag(textDescriptorElement.childNodes, "filters", 0, false);
		var lShadowsDescriptor_utshsd = null;
		if (lFiltersDescriptor_e)
		{
			var lShadowsDescriptor_e = SimpleXMLParser.getTag(lFiltersDescriptor_e.childNodes, "shadows", 0, false);
			if (lShadowsDescriptor_e)
			{
				lShadowsDescriptor_utshsd = new TextShadowsDescriptorParser(lShadowsDescriptor_e).shadowsDescriptor;
			}
		}

		this._textDescriptor = new TATextDescriptor(lAssetContent_obj, lFontDescriptor_utfd, lShadowsDescriptor_utshsd, lAssetWidth_obj, lAssetHeight_obj, lAutoWrapMode_bl, lLineSpacing_obj, lPadding_obj, lLetterSpacing_obj, lAlpha_obj);
	}
}

export default TATextDescriptorParser;