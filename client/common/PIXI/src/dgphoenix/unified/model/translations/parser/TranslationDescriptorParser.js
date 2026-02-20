import SimpleXMLParser from '../../xml/SimpleXMLParser';
import TranslationDescriptor from '../data/TranslationDescriptor';
import TranslationFontsDescriptorParser from './TranslationFontsDescriptorParser';
import TranslatableAssetsDescriptorParser from './TranslatableAssetsDescriptorParser';

/**
 * Translation descriptor parser.
 * @class
 */
class TranslationDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} translationDescriptor - XML document
	 */
	constructor(translationDescriptor)
	{
		this._translationDescriptor = null;

		this.initTranslationDescriptorParser(translationDescriptor);
	}

	initTranslationDescriptorParser(translationDescriptorXml)
	{
		let tdNode = translationDescriptorXml instanceof Element ? translationDescriptorXml : SimpleXMLParser.getTag(translationDescriptorXml.childNodes, "td", 0, true);
		
		var fontsNode = SimpleXMLParser.getTag(tdNode.childNodes, "fonts", 0, true);
		var translatableElementsDescriptorsNode = SimpleXMLParser.getTag(tdNode.childNodes, "tasds", 0, true);

		this._translationDescriptor = new TranslationDescriptor (new TranslationFontsDescriptorParser(fontsNode).fontsDescriptor,
																 new TranslatableAssetsDescriptorParser(translatableElementsDescriptorsNode).translatableAssetsDescriptor);
	}

	/**
	 * @type {TranslationDescriptor}
	 */
	get translationDescriptor()
	{
		return this._translationDescriptor;
	}
}

export default TranslationDescriptorParser;
