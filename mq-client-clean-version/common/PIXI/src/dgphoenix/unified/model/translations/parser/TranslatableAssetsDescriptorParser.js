import SimpleXMLParser from '../../xml/SimpleXMLParser';
import TranslatableAssetDescriptorParser from './TranslatableAssetDescriptorParser';
import TranslatableAssetsDescriptor from '../data/TranslatableAssetsDescriptor';

/**
 * Translatable assets descriptor parser.
 * @class
 */
class TranslatableAssetsDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} translatableElementsDescriptorsNode - XML element
	 */
	constructor(translatableElementsDescriptorsNode)
	{
		this._translatableAssetsDescriptor = null;

		this._initTranslatableAssetsDescriptorParser(translatableElementsDescriptorsNode);
	}

	_initTranslatableAssetsDescriptorParser(translatableElementsDescriptorsNode)
	{
		var l_utasd = new TranslatableAssetsDescriptor();
		this._translatableAssetsDescriptor = l_utasd;

		var lTranslatableAssetsElements_e_arr = SimpleXMLParser.getTags(translatableElementsDescriptorsNode.childNodes, "tad");
		var lCount_int = lTranslatableAssetsElements_e_arr.length;
		for (var i = 0; i < lCount_int; i++)
		{
			l_utasd.addAssetDescriptor(new TranslatableAssetDescriptorParser(lTranslatableAssetsElements_e_arr[i]).assetDescriptor);
		}
	}

	/**
	 * @type {TranslatableAssetsDescriptor}
	 */
	get translatableAssetsDescriptor()
	{
		return this._translatableAssetsDescriptor;
	}
}

export default TranslatableAssetsDescriptorParser;
