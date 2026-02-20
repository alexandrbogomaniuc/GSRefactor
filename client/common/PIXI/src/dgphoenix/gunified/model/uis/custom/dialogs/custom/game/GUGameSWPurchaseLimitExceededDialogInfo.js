import GUGameBaseDialogInfo from './GUGameBaseDialogInfo';

class GUGameSWPurchaseLimitExceededDialogInfo extends GUGameBaseDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fIsGoBackToLobbyNeed_bl = null;
	}

	get isActivationOverHiddenGameAvailable()
	{
		return true;
	}

	set isGoBackToLobbyNeed(aValue_str)
	{
		this._fIsGoBackToLobbyNeed_bl = aValue_str;
	}

	get isGoBackToLobbyNeed()
	{
		return this._fIsGoBackToLobbyNeed_bl;
	}
}

export default GUGameSWPurchaseLimitExceededDialogInfo