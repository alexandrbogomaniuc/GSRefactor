import SimpleUIInfo from '../../../../../unified/model/uis/SimpleUIInfo';

class GUSLobbyTooltipsInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._fLobbyTipsEnabled_bln = null;
		this._fGameTipsEnabled_bln = null;
	}

	get tooltipsEnabled()
	{
		return this._fLobbyTipsEnabled_bln || this._fGameTipsEnabled_bln;
	}

	set lobbyTipsEnabled(aVal_bln)
	{
		this._fLobbyTipsEnabled_bln = aVal_bln;
	}

	get lobbyTipsEnabled()
	{
		return this._fLobbyTipsEnabled_bln;
	}

	set gameTipsEnabled(aVal_bln)
	{
		this._fGameTipsEnabled_bln = aVal_bln;
	}

	get gameTipsEnabled()
	{
		return this._fGameTipsEnabled_bln;
	}

	destroy()
	{
		super.destroy();

		this._fLobbyTipsEnabled_bln = null;
		this._fGameTipsEnabled_bln = null;
	}
}

export default GUSLobbyTooltipsInfo