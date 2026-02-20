import {Utils} from '../../Utils'

/**
 * Translatable assety descriptor.
 * @class
 */
class TranslatableAssetDescriptor
{
	static get ASSET_TYPE_TEXT_BASED () { return "txt_based"; }
	static get ASSET_TYPE_IMAGE_BASED () { return "img_based"; }
	static get ASSET_TYPE_VOID_BASED () { return "void_based"; }

	/**
	 * @constructor
	 * @param {string} aAssetId_str 
	 * @param {string} aAssetType_str 
	 * @param {TATextDescriptor} aTextDescriptor_utatd 
	 * @param {TAImageDescriptor} aImgDescriptor_utaid 
	 * @param {AreaInnerContentDescriptor} aOptAreaInnerContentDescriptor_uaicd 
	 * @param {boolean} aOptIsHiddenAsset_bl - Not supported param.
	 * @param {boolean} aOptIsVirtualAsset_bl - Not supported param.
	 * @param {boolean} aOptIsOverrideAsset_bl - Not supported param.
	 */
	constructor(aAssetId_str, aAssetType_str, aTextDescriptor_utatd, aImgDescriptor_utaid, aOptAreaInnerContentDescriptor_uaicd, aOptIsHiddenAsset_bl, aOptIsVirtualAsset_bl, aOptIsOverrideAsset_bl)
	{
		this._assetType = null;
		this._assetId = null;
		this._textDescriptor = null;
		this._imgDescriptor = null;
		this._areaInnerContentDescriptor = null;
		this._isHiddenAsset = false;
		this._isVirtualAsset = false;
		this._isOverrideAsset = false;

		this._initTranslatableAssetDescriptor(aAssetId_str, aAssetType_str, aTextDescriptor_utatd, aImgDescriptor_utaid, aOptAreaInnerContentDescriptor_uaicd, aOptIsHiddenAsset_bl, aOptIsVirtualAsset_bl, aOptIsOverrideAsset_bl);
	}

	/**
	 * Is text based asset.
	 * @type {boolean}
	 */
	get isTextBasedAsset ()
	{
		return this._assetType === TranslatableAssetDescriptor.ASSET_TYPE_TEXT_BASED;
	}

	/**
	 * Is image based asset.
	 * @type {boolean}
	 */
	get isImageBasedAsset ()
	{
		return this._assetType === TranslatableAssetDescriptor.ASSET_TYPE_IMAGE_BASED;
	}

	/**
	 * Is void based asset.
	 * @type {boolean}
	 */
	get isVoidBasedAsset ()
	{
		return this._assetType === TranslatableAssetDescriptor.ASSET_TYPE_VOID_BASED;
	}

	/**
	 * Asset type.
	 * @type {string}
	 */
	get assetType ()
	{
		return this._assetType;
	}

	/**
	 * Translatable asset area descriptor.
	 * @type {AreaInnerContentDescriptor}
	 */
	get areaInnerContentDescriptor ()
	{
		return this._areaInnerContentDescriptor;
	}

	/**
	 * Translatable asset text descriptor.
	 * @type {TATextDescriptor}
	 */
	get textDescriptor ()
	{
		return this._textDescriptor;
	}

	/**
	 * Translatable asset image descriptor.
	 * @type {TAImageDescriptor}
	 */
	get imageDescriptor ()
	{
		return this._imgDescriptor;
	}

	/**
	 * Asset id.
	 * @type {string}
	 */
	get assetId ()
	{
		return this._assetId;
	}

	/**
	 * @deprecated
	 */
	get isHiddenAsset ()
	{
		return this._isHiddenAsset;
	}

	/**
	 * @deprecated
	 */
	get isVirtualAsset ()
	{
		return this._isVirtualAsset;
	}

	/**
	 * @deprecated
	 */
	get isOverrideAsset ()
	{
		return this._isOverrideAsset;
	}

	_initTranslatableAssetDescriptor(aAssetId_str, aAssetType_str, aTextDescriptor_utatd, aImgDescriptor_utaid, aOptAreaInnerContentDescriptor_uaicd, aOptIsHiddenAsset_bl, aOptIsVirtualAsset_bl, aOptIsOverrideAsset_bl)
	{
		if (
				!(Utils.isBoolean(aOptIsVirtualAsset_bl))
				&& (aOptIsVirtualAsset_bl !== undefined)
			)
		{
			throw new Error("Invaid 'Is Virual' arg value: " + aOptIsVirtualAsset_bl);
		}
		if (
				!(Utils.isBoolean(aOptIsOverrideAsset_bl))
				&& (aOptIsOverrideAsset_bl !== undefined)
			)
		{
			throw new Error("Invalid 'Is Override' arg value: " + aOptIsOverrideAsset_bl);
		}

		if (
				(aOptIsVirtualAsset_bl)
				&& (aAssetType_str !== TranslatableAssetDescriptor.ASSET_TYPE_TEXT_BASED)
			)
		{
			throw new Error("text-based virtual assets are only supported currently: ASSET ID=" + aAssetId_str + ", ASSET TYPE=" + aAssetType_str);
		}

		if (
				(aOptIsOverrideAsset_bl)
				&& (aAssetType_str !== TranslatableAssetDescriptor.ASSET_TYPE_TEXT_BASED)
			)
		{
			throw new Error("text-based override assets are only supported currently: ASSET ID=" + aAssetId_str + ", ASSET TYPE=" + aAssetType_str);
		}

		this._assetType = aAssetType_str;
		this._assetId = aAssetId_str;
		this._textDescriptor = aTextDescriptor_utatd;
		this._imgDescriptor = aImgDescriptor_utaid;
		this._areaInnerContentDescriptor = aOptAreaInnerContentDescriptor_uaicd;
		this._isHiddenAsset = Boolean(aOptIsHiddenAsset_bl);
		this._isVirtualAsset = Boolean(aOptIsVirtualAsset_bl);
		this._isOverrideAsset = Boolean(aOptIsOverrideAsset_bl);
	}
}

export default TranslatableAssetDescriptor;