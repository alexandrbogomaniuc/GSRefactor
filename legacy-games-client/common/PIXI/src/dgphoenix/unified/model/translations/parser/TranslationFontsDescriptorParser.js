import SimpleXMLParser from '../../xml/SimpleXMLParser';
import TranslationFontsDescriptor from '../data/TranslationFontsDescriptor';
import TranslationFontDescriptorParser from './TranslationFontDescriptorParser';

/**
 * Translation fonts descriptor parser.
 * @class
 */
class TranslationFontsDescriptorParser 
{
	/**
	 * @constructor
	 * @param {*} aFontsNode_e - XML node.
	 */
	constructor(aFontsNode_e)
	{
		var l_utfsd = new TranslationFontsDescriptor();
		this._fontsDescriptor = l_utfsd;

		var lFontsElements_e_arr = SimpleXMLParser.getTags(aFontsNode_e.childNodes, "font");
		var lFontsCount_int = lFontsElements_e_arr.length;
		for (var i = 0; i < lFontsCount_int; i++)
		{
			l_utfsd.addFontDescriptor(new TranslationFontDescriptorParser(lFontsElements_e_arr[i]).fontDescriptor);
		}

	}

	/**
	 * @type {TranslationFontsDescriptor}
	 */
	get fontsDescriptor()
	{
		return this._fontsDescriptor;
	}
}

export default TranslationFontsDescriptorParser;