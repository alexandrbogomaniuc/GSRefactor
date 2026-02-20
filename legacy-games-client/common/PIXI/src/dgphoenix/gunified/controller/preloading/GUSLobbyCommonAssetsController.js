import CommonAssetsController from '../../../unified/controller/preloading/CommonAssetsController';
import GUSLobbyCommonAssetsInfo from '../../model/preloading/GUSLobbyCommonAssetsInfo';
import { createLoader } from '../../../unified/controller/interaction/resources/loaders';
import { APP } from '../../../unified/controller/main/globals';

class GUSLobbyCommonAssetsController extends CommonAssetsController
{
	static get EVENT_ON_BTG_RULES_HTML_READY() 		{return "EVENT_ON_BTG_RULES_HTML_READY";}

	loadBattlgroundRulesHtml()
	{
		let lBattlegroundRulesURL_str = APP.contentPathURLsProvider.commonCurrentLocaleTranslationsPath + "/battleground/html/rules.html?version=" + this.info.commonAssetsVersion;
		
		let lLoader = createLoader(lBattlegroundRulesURL_str);
		lLoader.once('complete', this._onBattlegroundRulesHtmlLoadingCompleted, this);
		lLoader.once('error', this._onBattlegroundRulesHtmlLoadingError, this);

		lLoader.load();
	}

	constructor(aOptInfo_si)
	{
		super(aOptInfo_si || new GUSLobbyCommonAssetsInfo());
	}

	_onBattlegroundRulesHtmlLoadingCompleted(event)
	{
		this.emit(GUSLobbyCommonAssetsController.EVENT_ON_BTG_RULES_HTML_READY, {htmlData: event.target.data});
	}

	_onBattlegroundRulesHtmlLoadingError(event)
	{
		throw new Error("Common assets version descriptor loading error");
	}
}

export default GUSLobbyCommonAssetsController