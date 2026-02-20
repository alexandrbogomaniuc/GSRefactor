import SimpleInfo from '../../../unified/model/base/SimpleInfo';

class GUSLobbyStateInfo extends SimpleInfo
{
	static get SCREEN_PAYTABLE()				{ return "paytableScreen"; }
	static get SCREEN_SETTINGS()				{ return "settingsScreen"; }
	static get SCREEN_PROFILE()					{ return "profileScreen"; }
	static get SCREEN_NONE()					{ return "none"; }

	constructor()
	{
		super();

		this._fSecondaryScreenState_str = GUSLobbyStateInfo.SCREEN_NONE;
		this._fIsLobbyScreenVisible_bln = null;
		this._fDialogVisible_bln = null;
		this._fPreloaderVisible_bln = null;
	}

	set lobbyScreenVisible(isVisible_bln)
	{
		this._fIsLobbyScreenVisible_bln = isVisible_bln;
	}

	get lobbyScreenVisible()
	{
		return this._fIsLobbyScreenVisible_bln;
	}

	set secondaryScreenState(screen_str)
	{
		this._fSecondaryScreenState_str = screen_str;
	}

	get secondaryScreenState()
	{
		return this._fSecondaryScreenState_str;
	}

	get secondaryScreenVisible()
	{
		return Boolean(this._fSecondaryScreenState_str !== GUSLobbyStateInfo.SCREEN_NONE);
	}

	set dialogVisible(aVal_bln)
	{
		this._fDialogVisible_bln = aVal_bln;
	}

	get dialogVisible()
	{
		return this._fDialogVisible_bln;
	}

	set preloaderVisible(aVal_bln)
	{
		this._fPreloaderVisible_bln = aVal_bln;
	}

	get preloaderVisible()
	{
		return this._fPreloaderVisible_bln;
	}

	destroy()
	{
		super.destroy();

		this._fIsLobbyScreenVisible_bln = null;
		this._fSecondaryScreenState_str = null;
		this._fDialogVisible_bln = null;
		this._fPreloaderVisible_bln = null;
	}
}

export default GUSLobbyStateInfo;