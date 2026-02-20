import GameBaseDialogInfo from './GameBaseDialogInfo';

class GameForceSitOutDialogInfo extends GameBaseDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fRoomId_int = 0;
		this._fCompensateSpecialWeapons_num = 0;
		this._fTotalReturnedSpecialWeapons_num = 0;
		this._fResitOutInProcess_bl = false;
	}

	get roomId()
	{
		return this._fRoomId_int;
	}

	set roomId(aValue_int)
	{
		this._fRoomId_int = aValue_int;
	}

	get compensateSpecialWeapons()
	{
		return this._fCompensateSpecialWeapons_num;
	}

	set compensateSpecialWeapons(aValue_num)
	{
		this._fCompensateSpecialWeapons_num = aValue_num;
	}

	get totalReturnedSpecialWeapons()
	{
		return this._fTotalReturnedSpecialWeapons_num;
	}

	set totalReturnedSpecialWeapons(aValue_num)
	{
		this._fTotalReturnedSpecialWeapons_num = aValue_num;
	}

	set resitOutInProcess(aValue_bl)
	{
		this._fResitOutInProcess_bl = aValue_bl;
	}

	get isActivationOverHiddenGameAvailable()
	{
		return this._fResitOutInProcess_bl;
	}
}

export default GameForceSitOutDialogInfo