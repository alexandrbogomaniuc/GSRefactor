import XMLLoader from './XMLLoader';
import TranslationDescriptorParser from '../../../../model/translations/parser/TranslationDescriptorParser';
import TASpriteDescriptor from '../../../../model/translations/data/TASpriteDescriptor';
import { APP } from '../../../main/globals';
import TranslationDescriptor from '../../../../model/translations/data/TranslationDescriptor';

/**
 * @class
 * @inheritDoc
 * @classdesc Translations descriptor xml loader
 */
class TranslationDescriptorLoader extends XMLLoader 
{
	constructor(src) 
	{
		super(src);

		this._translationDescriptor = null;
	}

	parseData()
	{
		let translationDescr = new TranslationDescriptorParser(this.data).translationDescriptor;
		this._resolveTranslationDescriptorURLs(translationDescr);

		this.translationDescriptor = translationDescr;
	}

	get translationDescriptor()
	{
		return this._translationDescriptor;
	}

	set translationDescriptor(val)
	{
		this._translationDescriptor = val;
	}

	/**
	 * Resolves URLs - replace placeholders with url parts
	 * @param {TranslationDescriptor} a_utd 
	 * @param {*} aPreferredResolvingContext_gucpsurlsp 
	 * @private
	 */
	_resolveTranslationDescriptorURLs (a_utd, aPreferredResolvingContext_gucpsurlsp)
	{
		var lTranslationFontsDescriptor_utfsd = a_utd.fontsDescriptor;
		var lTranslatableAssetsDescriptor_utasd = a_utd.translationAssetsDescriptor;
		var lContentPathsURLsProvider_gucpsurlsp = APP.contentPathURLsProvider;

		var lFontsCount_int = lTranslationFontsDescriptor_utfsd.fontsCount;
		var lCurrentFontDescriptor_utfd;
		for (var i = 0; i < lFontsCount_int; i++)
		{
			lCurrentFontDescriptor_utfd = lTranslationFontsDescriptor_utfsd.getFontDescriptorByIntId(i);
			lCurrentFontDescriptor_utfd.updateURL(lContentPathsURLsProvider_gucpsurlsp.resolvePathPlaceholders(lCurrentFontDescriptor_utfd.url));
		}

		var lAssetsCount_int = lTranslatableAssetsDescriptor_utasd.assetsCount;
		var lCurrentAssetDescriptor_utad;
		var lCurrentAssetImageDescriptor_utaid;
		for (var i = 0; i < lAssetsCount_int; i++)
		{
			lCurrentAssetDescriptor_utad = lTranslatableAssetsDescriptor_utasd.getAssetDescriptorByIntId(i);
			if (lCurrentAssetDescriptor_utad.isImageBasedAsset)
			{
				lCurrentAssetImageDescriptor_utaid = lCurrentAssetDescriptor_utad.imageDescriptor;
				if (lCurrentAssetImageDescriptor_utaid instanceof TASpriteDescriptor)
				{
					lCurrentAssetImageDescriptor_utaid.updateAtlasConfigUrl(lContentPathsURLsProvider_gucpsurlsp.resolvePathPlaceholders(lCurrentAssetImageDescriptor_utaid.altasConfigUrl));

					let spriteImagesUrls = lCurrentAssetImageDescriptor_utaid.imagesUrls;
					for (var j = 0; j < spriteImagesUrls.length; j++)
					{
						lCurrentAssetImageDescriptor_utaid.updateImageURL(lContentPathsURLsProvider_gucpsurlsp.resolvePathPlaceholders(spriteImagesUrls[j]), j);
					}
				}
				else
				{
					lCurrentAssetImageDescriptor_utaid.updateURL(lContentPathsURLsProvider_gucpsurlsp.resolvePathPlaceholders(lCurrentAssetImageDescriptor_utaid.url));
				}
			}
		}
	}

}

export default TranslationDescriptorLoader;