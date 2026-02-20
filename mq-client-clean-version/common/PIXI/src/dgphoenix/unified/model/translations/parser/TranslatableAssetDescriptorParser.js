import SimpleXMLParser from '../../xml/SimpleXMLParser';
import TranslatableAssetDescriptor from '../data/TranslatableAssetDescriptor';
import AreaInnerContentDescriptorParser from '../../display/area/AreaInnerContentDescriptorParser';
import TATextDescriptorParser from './TATextDescriptorParser';
import TASpriteDescriptorParser from './TASpriteDescriptorParser';
import TAImageDescriptorParser from './TAImageDescriptorParser';

/**
 * Translatable asset descriptor parser.
 * @class
 */
class TranslatableAssetDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} translatableAssetElement - XML element
	 */
	constructor(translatableAssetElement)
	{
		this._translatableAssetDescriptor = null;
		
		this._initTranslatableAssetDescriptorParser(translatableAssetElement);
	}

	_initTranslatableAssetDescriptorParser(translatableAssetElement)
	{
		var lAssetId_str = SimpleXMLParser.getAttributeValue(translatableAssetElement, "id", true);
		var lAssetType_str = SimpleXMLParser.getAttributeValue(translatableAssetElement, "type", true);
		var lIsHiddenAsset_bl = SimpleXMLParser.getAttributeValue(translatableAssetElement, "hidden", false) === "true"; //for some translations it may be helpfull to have ability to hide some unnecessary assets
		var lIsVirtualAsset_bl = SimpleXMLParser.getAttributeValue(translatableAssetElement, "virtual", false) === "true";
		var lIsOverrideAsset_bl = SimpleXMLParser.getAttributeValue(translatableAssetElement, "override", false) === "true";

		var lTextDescriptor_utatd = null;
		var lImageDescriptor_utaid = null;

		var lAreaDescriptor_uaicd = new AreaInnerContentDescriptorParser(SimpleXMLParser.getTag(translatableAssetElement.childNodes, "area", 0, true)).areaInnerContentDescriptor;
		if (lAssetType_str === TranslatableAssetDescriptor.ASSET_TYPE_TEXT_BASED)
		{
			lTextDescriptor_utatd = new TATextDescriptorParser(SimpleXMLParser.getTag(translatableAssetElement.childNodes, "txt", 0, true)).textDescriptor;
		}
		else if (lAssetType_str === TranslatableAssetDescriptor.ASSET_TYPE_IMAGE_BASED)
		{
			var lSpriteDescriptor_e = SimpleXMLParser.getTag(translatableAssetElement.childNodes, "sprite", 0, false);
			if (lSpriteDescriptor_e)
			{
				lImageDescriptor_utaid = new TASpriteDescriptorParser(lSpriteDescriptor_e).spriteDescriptor;
			}
			else
			{
				lImageDescriptor_utaid = new TAImageDescriptorParser(SimpleXMLParser.getTag(translatableAssetElement.childNodes, "img", 0, true)).imageDescriptor;
			}
		}
		else if (lAssetType_str === TranslatableAssetDescriptor.ASSET_TYPE_VOID_BASED)
		{
			//void based assets are used to define some area only that is related to translations specific
		}
		else
		{
			throw new Error(`Unsupported asset type: '${lAssetType_str}'; `);
		}

		this._translatableAssetDescriptor = new TranslatableAssetDescriptor(lAssetId_str, lAssetType_str, lTextDescriptor_utatd, lImageDescriptor_utaid, lAreaDescriptor_uaicd, lIsHiddenAsset_bl, lIsVirtualAsset_bl, lIsOverrideAsset_bl);
	}

	/**
	 * @type {TranslatableAssetDescriptor}
	 */
	get assetDescriptor()
	{
		return this._translatableAssetDescriptor;
	}
}

export default TranslatableAssetDescriptorParser;
