import TranslationFontsDescriptor from './TranslationFontsDescriptor';
import TranslatableAssetsDescriptor from './TranslatableAssetsDescriptor';

/**
 * Translation descriptor.
 * @class
 */
class TranslationDescriptor
{
	/**
	 * @constructor
	 * @param {TranslationFontsDescriptor} translFontsDescriptor 
	 * @param {TranslatableAssetsDescriptor} translAssetsDescriptor 
	 */
	constructor (translFontsDescriptor, translAssetsDescriptor)
	{
		this._translationFontsDescriptor = null;
		this._translationAssetsDescriptor = null;

		this._initTranslationDescriptor(translFontsDescriptor, translAssetsDescriptor);
	}

	/**
	 * Translation fonts descriptor.
	 * @type {TranslationFontsDescriptor}
	 */
	get fontsDescriptor()
	{
		return this._translationFontsDescriptor;
	}

	/**
	 * Translatable assets descriptor.
	 * @type {TranslatableAssetsDescriptor}
	 */
	get translationAssetsDescriptor()
	{
		return this._translationAssetsDescriptor;
	}

	_initTranslationDescriptor(translFontsDescriptor, translAssetsDescriptor)
	{
		if (!(translFontsDescriptor instanceof TranslationFontsDescriptor))
		{
			throw new Error(`Invalid Translation Font Descriptor arg: '${translFontsDescriptor}'; `);
		}
		if (!(translAssetsDescriptor instanceof TranslatableAssetsDescriptor))
		{
			throw new Error(`Invalid Translatable Assets Descriptor arg: '${translAssetsDescriptor}'; `);
		}
		
		this._translationFontsDescriptor = translFontsDescriptor;
		this._translationAssetsDescriptor = translAssetsDescriptor;
	}
}

export default TranslationDescriptor;