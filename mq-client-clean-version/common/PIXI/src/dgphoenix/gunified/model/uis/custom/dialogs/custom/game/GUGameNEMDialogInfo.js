import GUGameBaseDialogInfo from './GUGameBaseDialogInfo';

class GUGameNEMDialogInfo extends GUGameBaseDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._isRealMoneyMode = false;
		this._fIsBonusMode_bl = false;
		this._isFRBMode_bln = false;
	}

	get isActivationOverHiddenGameAvailable()
	{
		return true;
	}

	set isRealMoneyMode(value)
	{
		this._isRealMoneyMode = value;
	}

	get isRealMoneyMode()
	{
		return this._isRealMoneyMode;
	}

	get isFreeMoneyMode()
	{
		return !this._isRealMoneyMode;
	}

	get isBonusMode()
	{
		return this._fIsBonusMode_bl;
	}

	set isBonusMode(aValue_bl)
	{
		this._fIsBonusMode_bl = aValue_bl;
	}

	get isBonusModeNEMForRoom()
	{
		return this._fIsBonusModeNEMForRoom_bl;
	}

	set isBonusModeNEMForRoom(aValue_bl)
	{
		this._fIsBonusModeNEMForRoom_bl = aValue_bl;
	}
}

export default GUGameNEMDialogInfo