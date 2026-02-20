import { createLoader } from '../interaction/resources/loaders';
import Queue from '../interaction/resources/loaders/Queue';
import TranslationsQueue, { TRANSLATIONS_TYPES } from '../interaction/resources/loaders/TranslationsQueue';
import TranslationsAssetsQueue from '../interaction/resources/loaders/TranslationsAssetsQueue';
import { APP } from '../main/globals';
import CTranslatableAsset from '../../view/translations/CTranslatableAsset';
import { createEnding } from '../../model/translations/parser/utils/NounEnding';
import { Utils } from '../../model/Utils';

var _currentLocale = "en";
var _supportedLanguages = ["en", "de", "dk", "el", "es", "fi", "fr", "it", "jp", "ko", "nl", "no", "pt", "pt-eu", "ru", "se", "th", "tr", "vi", "zh", "zh-cn", "bg", "cz", "sk", "ro", "pl", "hu", "id", "dk-sp", "km"];
var _slavonicLanguages = ["ru", "cz", "sk", "pl"];
var _translationsDescriptors = [];
var _contentFiles = [];

export const MOBILE_PAYTABLE_CONTENT_FILE_NAME = "paytable_content.html";
export const MOBILE_PAYTABLE_CSS_FILE_NAME = "paytable.css";

export const BATTLEGROUND_PAYTABLE_CONTENT_FILE_NAME = "battleground/paytable_content.html";
export const BATTLEGROUND_PAYTABLE_CSS_FILE_NAME = "battleground/paytable.css";

export const TRIPLE_MAX_BLAST_PAYTABLE_CONTENT_FILE_NAME = "triplemaxblast/paytable_content.html";
export const TRIPLE_MAX_BLAST_PAYTABLE_CSS_FILE_NAME = "triplemaxblast/paytable.css";

/**
 * @class
 * @description Localisations
 */
class I18 {

	/** current localization */
	static get currentLocale() {
		return _currentLocale;
	}
	static set currentLocale(val) {
		_currentLocale = val;
	}

	/** array of slavonic localizations */
	static get slavonicLocales() {
		return _slavonicLanguages;
	}

	/** array of supported localizations */
	static get supportedLanguages() {
		return _supportedLanguages;
	}
	static set supportedLanguages(val) {
		_supportedLanguages = val;
	}

	/** array of translation descriptors */
	static get translationsDescriptors() {
		return _translationsDescriptors;
	}
	static set translationsDescriptors(val) {
		_translationsDescriptors = val;
	}

	/** Array of files with localizes content*/
	static get contentFiles() {
		return _contentFiles;
	}
	static set contentFiles(val) {
		_contentFiles = val;
	}

	//deprecated, kept for compatibility
	static get paytableContentFileName()
	{
		return MOBILE_PAYTABLE_CONTENT_FILE_NAME;
	}

	//deprecated, kept for compatibility
	static get paytableStylesFileName()
	{
		return MOBILE_PAYTABLE_CSS_FILE_NAME;
	}

	/**
	 * Returns relative path of paytable content file by application type.
	 * @param {Application} aApplication_a 
	 * @returns {string}
	 */
	static getPaytableContentFileName(aApplication_a)
	{
		if (aApplication_a.isBattlegroundGame)
		{
			return BATTLEGROUND_PAYTABLE_CONTENT_FILE_NAME;
		}
		
		if (aApplication_a && aApplication_a.tripleMaxBlastModeController && aApplication_a.tripleMaxBlastModeController.info && aApplication_a.tripleMaxBlastModeController.info.isTripleMaxBlastMode)
		{
			return TRIPLE_MAX_BLAST_PAYTABLE_CONTENT_FILE_NAME;
		}

		return MOBILE_PAYTABLE_CONTENT_FILE_NAME;
	}

	/**
	 * Returns relative path of paytable css file by application type.
	 * @param {Application} aApplication_a 
	 * @returns {string}
	 */
	static getPaytableStylesFileName(aApplication_a)
	{
		if (aApplication_a.isBattlegroundGame)
		{
			return BATTLEGROUND_PAYTABLE_CSS_FILE_NAME;
		}

		if (aApplication_a && aApplication_a.tripleMaxBlastModeController && aApplication_a.tripleMaxBlastModeController.info && aApplication_a.tripleMaxBlastModeController.info.isTripleMaxBlastMode)
		{
			return TRIPLE_MAX_BLAST_PAYTABLE_CSS_FILE_NAME;
		}

		return MOBILE_PAYTABLE_CSS_FILE_NAME;
	}


	//deprecated, kept for compatibility
	static get mobilePaytableContent()
	{
		return I18.getContentFileData(I18.paytableContentFileName);
	}

	//deprecated, kept for compatibility
	static get mobilePaytableStylesheet()
	{
		return I18.getContentFileData(I18.paytableStylesFileName);
	}

	/** Gets data from paytable content file. */
	static getMobilePaytableContent(aApplication_a)
	{
		return I18.getContentFileData(I18.getPaytableContentFileName(aApplication_a));
	}

	/** Gets data from paytable css file. */
	static getMobilePaytableStylesheet(aApplication_a)
	{
		return I18.getContentFileData(I18.getPaytableStylesFileName(aApplication_a));
	}

	 /*TODO [os]: to be removed ...*/
	static getCommonTranslatableAssetsRelativePath()
	{
		return "assets/translations/"+ I18.currentLocale + "/"
	}
	/*TODO [os]: ... to be removed*/

	/**
	 * @description Initialization
	 * @param {String} [locale=null] locale - will be defined automatically if null value provided
	 */
	static init(locale=null) 
	{
		if (!locale)
		{
			locale = APP.urlBasedParams.LANG || APP.urlBasedParams.lang || I18.supportedLanguages[0];
		}

		if(I18.supportedLanguages.indexOf(locale) < 0) locale = I18.supportedLanguages[0];
		
		I18.currentLocale = locale;
	}

	/**
	 * Creates loader queue for translation descriptors.
	 * @param {Object} params - Queue params
	 * @property {TranslationsQueue.TRANSLATIONS_TYPES} params.type
	 * @property {TranslationsQueue.APP_TYPES} params.appType
	 * @property {Boolean} params.isSupportDesktopPaytable
	 * @returns {TranslationsQueue}
	 */
	static createLoaderQueue(params)
	{
		let queue = new TranslationsQueue();

		if (params) 
		{
			queue.addTranslationDescriptor(params.type, params.appType, params.isSupportDesktopPaytable)
		}

		return queue;;
	}

	/**
	 * Creates loader queue for translation assets.
	 * @param {TranslationsQueue} translationsQueue 
	 * @returns {Queue}
	 */
	static createAssetsLoaderQueue(translationsQueue)
	{
		let queue = new Queue;
		let isPaytable = false;
		
		for (let descriptorId in translationsQueue.descriptors)
				{
					if (descriptorId == translationsQueue.paytableDescriptorId)
					{
						isPaytable = true;
					}
					queue.add
					(
						new TranslationsAssetsQueue(translationsQueue.descriptors[descriptorId], isPaytable)
					);
				}
		
		return queue;
	}

	/**
	 * Adds translation descriptor to library
	 * @param {TranslationDescriptor} translationDescriptor 
	 */
	static addTranslationDescriptor(translationDescriptor)
	{
		I18.translationsDescriptors.push(translationDescriptor);
	}

	/**
	 * Checks if translation descriptor for preloader assets is ready.
	 */
	static get isPreloaderTranslationDescriptorReady()
	{
		return I18.translationsDescriptors.length > 0;
	}

	/**
	 * Ads content file to library.
	 * @param {String} contentFileName 
	 * @param {*} contentFileData 
	 */
	static addContentFile(contentFileName, contentFileData)
	{
		I18.contentFiles.push({name: contentFileName, data: contentFileData});
	}

	/**
	 * Returns content file data by file name.
	 * @param {string} contentFileName 
	 * @returns {*}
	 */
	static getContentFileData(contentFileName)
	{
		for (let i=0; i<I18.contentFiles.length; i++)
		{
			let lContentFile_obj = I18.contentFiles[i];
			if (lContentFile_obj.name === contentFileName)
			{
				return lContentFile_obj.data;
			}
		}
		return null;
	}

	/**
	 * @description Generate translatable asset view
	 * @param {String} assetId - translatable assets id
	 * @returns {String}
	 */
 
	static generateNewCTranslatableAsset(assetId)
	{
		var assetDescriptor = I18.getTranslatableAssetDescriptor(assetId);
		return new CTranslatableAsset(assetDescriptor);
	}

	/**
	 * Finds translatable asset descriptor by asset id.
	 * @param {string} assetId 
	 * @returns {TranslatableAssetDescriptor}
	 */
	static getTranslatableAssetDescriptor(assetId)
	{
		var assetDescriptor = null;
		if (assetId)
		{
			for (let i=0; i<I18.translationsDescriptors.length; i++)
			{
				let l_utd = I18.translationsDescriptors[i];
				assetDescriptor = l_utd.translationAssetsDescriptor.getAssetDescriptor(assetId, true);
				if (!!assetDescriptor)
				{
					break;
				}
			}
		}

		return assetDescriptor;
	}

	/**
	 * Selects a noun with proper ending from template according to an amount.
	 * @example 'Please see N #examples:EXAMPLE,EXAMPLES#'
	 * @param {string} totalString - Source string containing noun-ending template.
	 * @param {string} subString - Key of non-ending template ('#examples:' in this example).
	 * @param {number} count - Amount for proper ending.
	 * @returns {string}
	 */
	static prepareNumberPoweredMessage(totalString, subString, count)
	{		
		if (!Utils.isString(totalString))
		{
			throw new Error(`Invalid message arg: ${totalString}`);
		}

		var startIndex = totalString.indexOf(subString);
		if (startIndex < 0)
		{
			return totalString;
		}
		var stopIndex = totalString.indexOf("#", startIndex + 1);
		var sEndingForms = totalString.substring(startIndex + subString.length, stopIndex);
		var arEndingForms = sEndingForms.split(",");
		var cutString = totalString.substring(startIndex, stopIndex + 1);			
		var pasteString = createEnding(count, I18.currentLocale, arEndingForms);
		
		var sResult = totalString.replace(cutString, pasteString);
		
		return sResult;
	}

}

export default I18;