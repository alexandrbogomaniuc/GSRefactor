import SimpleXMLParser from '../../xml/SimpleXMLParser';
import TranslationFontDescriptor from '../data/TranslationFontDescriptor'

/**
 * Translation font descriptor parser.
 * @class
 */
class TranslationFontDescriptorParser 
{
	/**
	 * @constructor
	 * @param {*} aFontNode_e - XML node.
	 */
	constructor(aFontNode_e)
	{
		var lFontName_str = SimpleXMLParser.getAttributeValue(aFontNode_e, "name", true);
		var lFontURL_str = SimpleXMLParser.getAttributeValue(aFontNode_e, "url", true);
		
		this._fontDescriptor = new TranslationFontDescriptor(lFontName_str, lFontURL_str);
	}

	/**
	 * @type {TranslationFontDescriptor}
	 */
	get fontDescriptor()
	{
		return this._fontDescriptor;
	}
}

export default TranslationFontDescriptorParser;