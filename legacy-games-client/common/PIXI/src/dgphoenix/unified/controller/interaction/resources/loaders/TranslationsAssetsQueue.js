import ImageLoader from './ImageLoader';
import FontLoader from './FontLoader';
import Queue from './Queue';
import JSONLoader from './JSONLoader';
import AJAXLoader from './AJAXLoader';
import { createLoader } from './';
import TASpriteDescriptor from '../../../../model/translations/data/TASpriteDescriptor';
import { APP } from '../../../main/globals';
import I18 from '../../../translations/I18';
/**
 * @class
 * @inheritDoc
 * @extends Queue
  * @classdesc Queue of translation assets loaders
 */

 class TranslationsAssetsQueue extends Queue 
 {
    constructor (descriptor, isPaytable)
    {
        super();

        this._translationDescriptor = descriptor;
		this.addTranslationAssetsToQueue(isPaytable);
    }

    _handleProgress(evt)
	{
		if (this._loadingErrorOccured)
		{
			return;
		}

		if (evt && evt.target.complete)
		{
			let loaderItem = evt.target;

			if (loaderItem instanceof ImageLoader)
			{
				this._addTranslatedAsset(loaderItem);
			}
			else if (loaderItem instanceof FontLoader)
			{
				this._addFont(loaderItem);
			}
			else if (loaderItem instanceof JSONLoader)
			{
				this._addTranslatedSpriteAtlasConfig(loaderItem);
			}
			else if (loaderItem instanceof AJAXLoader)
			{
				this._addContentFile(loaderItem);
			}
		}

		super._handleProgress(evt);
	}

	/**
	 * Adds image asset to AssetsLibrary
	 * @param {*} loaderItem 
	 * @private
	 */
    _addTranslatedAsset(loaderItem)
	{
		let img = loaderItem.data;
		let src = loaderItem.key;
		let name = loaderItem.name;
		let asset = APP.library.addAsset(src, name);
		asset.bitmap = img;
	}

	/**
	 * Adds and registers font
	 * @param {*} loaderItem 
	 * @private
	 */
	_addFont(loaderItem)
	{
		APP.fonts.addFont(loaderItem);
	}

	/**
	 * Adds atlas config
	 * @param {*} loaderItem 
	 * @private
	 */
	_addTranslatedSpriteAtlasConfig(loaderItem)
	{
		this._translationDescriptor.translationAssetsDescriptor.getAssetDescriptor(loaderItem.name).imageDescriptor.atlasConfigs = loaderItem.data;
	}

	/**
	 * Adds content files data to library (paytable_content.html or paytable.css)
	 * @param {*} loaderItem 
	 * @private
	 */
	_addContentFile(loaderItem)
	{
		if (~loaderItem.name.indexOf(I18.getPaytableContentFileName(APP)))
		{
			I18.addContentFile(I18.getPaytableContentFileName(APP), loaderItem.data);
		}
		else if (~loaderItem.name.indexOf(I18.getPaytableStylesFileName(APP)))
		{
			I18.addContentFile(I18.getPaytableStylesFileName(APP), loaderItem.data);
		}
		else
		{
			throw new Error('Unexpected content file ' + loaderItem.name);
		}
	}

	/**
	 * Add assets from translation descriptor to loader queue
	 * @param {*} isPaytable 
	 */
	addTranslationAssetsToQueue(isPaytable)
	{
		let translDescriptor = this._translationDescriptor;

		//LOCALIZATION FONTS LOADING INITIATING...
		var lFontsDescriptor_utfsd = translDescriptor.fontsDescriptor;
		var lFontsCount_int = lFontsDescriptor_utfsd.fontsCount;
		for (var i = 0; i < lFontsCount_int; i++)
		{
			var lFontDescriptor_utfd = lFontsDescriptor_utfsd.getFontDescriptorByIntId(i);
			var lFontName_str = lFontDescriptor_utfd.fontName;

			this.addLoaderToQueue(lFontName_str, lFontDescriptor_utfd.url);
		}
		//...LOCALIZATION FONTS LOADING INITIATING

		//LOCALIZATION IMAGES LOADING INITIATING...
		var lTranslatableAssetsDescriptor_utasd = translDescriptor.translationAssetsDescriptor;

		var lAssetsCount_int = lTranslatableAssetsDescriptor_utasd.assetsCount;
		for (var i = 0; i < lAssetsCount_int; i++)
		{
			var lTranslatableAssetDescriptor_utad = lTranslatableAssetsDescriptor_utasd.getAssetDescriptorByIntId(i);
			if (!lTranslatableAssetDescriptor_utad.isImageBasedAsset)
			{
				continue;
			}
			var lAssetId_str = lTranslatableAssetDescriptor_utad.assetId;
			var lImgDescriptor_utaid = lTranslatableAssetDescriptor_utad.imageDescriptor;

			if (lImgDescriptor_utaid instanceof TASpriteDescriptor)
			{
				this.addLoaderToQueue(lAssetId_str, lImgDescriptor_utaid.altasConfigUrl);

				let spriteImagesUrls = lImgDescriptor_utaid.imagesUrls;
				for (var j=0; j<spriteImagesUrls.length; j++)
				{
					let spriteSheetImgAssetId = lAssetId_str + `_${j}`;
					this.addLoaderToQueue(spriteSheetImgAssetId, spriteImagesUrls[j]);
				}
			}
			else
			{
				this.addLoaderToQueue(lAssetId_str, lImgDescriptor_utaid.url);
			}

			if (isPaytable) 
			{
				this._addPaytableFilesToQueueIfNeeded();
			}
		}
		//...LOCALIZATION IMAGES LOADING INITIATING
	}

	_addPaytableFilesToQueueIfNeeded()
	{
		this.add(new AJAXLoader(`${APP.contentPathURLsProvider.currentLocaleTranslationsPath}/content/paytable/mobile/${I18.getPaytableContentFileName(APP)}`));
		this.add(new AJAXLoader(`${APP.contentPathURLsProvider.currentLocaleTranslationsPath}/content/paytable/mobile/${I18.getPaytableStylesFileName(APP)}`));
	}

	addLoaderToQueue(aAssetName_str, aURL_obj)
	{
		let assetLoader = createLoader(aURL_obj);
		assetLoader.name = aAssetName_str;

		this.add(assetLoader);
	}
 }

export default TranslationsAssetsQueue;