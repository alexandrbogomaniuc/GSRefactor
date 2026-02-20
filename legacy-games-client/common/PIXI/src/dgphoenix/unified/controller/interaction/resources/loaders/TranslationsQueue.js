import Queue from './Queue';
import TranslationDescriptorLoader from './TranslationDescriptorLoader';
import { APP } from '../../../main/globals';
import I18 from '../../../translations/I18';

/**
 * A strings that describes descriptor type: app, preloader or paytable
 * @typedef {string} TRANSLATIONS_TYPES
 */
const TRANSLATIONS_TYPES = {
	APP: "app",
	PRELOADER: "preloader",
	PAYTABLE: "paytable"
}

/**
 * A strings that specifies application that contains descriptor
 * @typedef {string} APP_TYPES
 */
const APP_TYPES = {
	COMMON: "common",
	LOBBY: "lobby",
	GAME: "game"
}

/**
 * @class
 * @inheritDoc
 * @extends Queue
 * @classdesc Queue of translation descriptors
 */
class TranslationsQueue extends Queue 
{
	constructor() 
	{
		super();
		this._descriptors = [];
	}

	/**
	 * Adds descriptor to loader queue
	 * @param {TRANSLATIONS_TYPES} type - Descriptor type
	 * @param {APP_TYPES} appType - Descriptor owner type
	 * @param {Boolean} isSupportDesktopPaytable 
	 */
	addTranslationDescriptor(type=TRANSLATIONS_TYPES.APP, appType = undefined, isSupportDesktopPaytable = true)
	{
		let lBasePath_str = (appType === APP_TYPES.COMMON) ? APP.contentPathURLsProvider.commonCurrentLocaleTranslationsPath : APP.contentPathURLsProvider.currentLocaleTranslationsPath;
		let translationsDescriptorPath = `${lBasePath_str}/translation_descriptor${this._getDescriptorSuffix(type)}.xml`;
		this.add(new TranslationDescriptorLoader(translationsDescriptorPath));
		
		if (
				(appType === APP_TYPES.LOBBY || appType === APP_TYPES.GAME)
				&& type === TRANSLATIONS_TYPES.PAYTABLE
				&& (APP.isMobile || APP.isDebugMode || !isSupportDesktopPaytable)
			)
		{
			this._paytableDescriptorId = this._descriptors.length;
		}
	}

	/**
	 * Returns suffix for translation descriptor file name by its type
	 * @param {TRANSLATIONS_TYPES} type 
	 * @returns {string}
	 */
	_getDescriptorSuffix(type)
	{
		switch (type)
		{
			case TRANSLATIONS_TYPES.PRELOADER:
				return "_preloader";
			case TRANSLATIONS_TYPES.PAYTABLE:
				return "_paytable";
			default:
				return "";
		}
	}

	/**
	 * Gets loaded translation descriptors
	 * @returns {TranslationDescriptorLoader[]}
	 */
	get descriptors()
	{
		return this._descriptors;
	}

	/**
	 * Gets index of paytable descriptor in descriptors array
	 * @returns {number}
	 */
	get paytableDescriptorId()
	{
		if (this._paytableDescriptorId !== undefined)
		{
			return this._paytableDescriptorId;
		}
		else
		{
			return -1;
		}
	}

	get progressInfo() 
	{
		if (this.items.size == 1 && this.data.size == 0)
		{
			return undefined;
		}
		return super.progressInfo;
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
			if (loaderItem instanceof TranslationDescriptorLoader)
			{
				this.onTranslationDescriptorLoaded(loaderItem);
				super._handleProgress(evt);
				return;
			}
		}

		super._handleProgress(evt);
	}

	/**
	 * Registers loaded translation descriptor
	 * @param {TranslationDescriptorLoader} loaderItem 
	 */
	onTranslationDescriptorLoaded (loaderItem)
	{
		this._descriptors.push(loaderItem.translationDescriptor);
		I18.addTranslationDescriptor(loaderItem.translationDescriptor);
	}
}

export { TRANSLATIONS_TYPES, APP_TYPES };
export default TranslationsQueue;