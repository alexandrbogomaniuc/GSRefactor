import GUDialogInfo from '../../GUDialogInfo';

class GUSGameBattlegroundNoWeaponsFiredDialogInfo extends GUDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fRefund_num = undefined;
	}

	setRefund(aRefund_num)
	{
		this._fRefund_num = aRefund_num;
	}

	getRefund()
	{
		return this._fRefund_num;
	}
}

export default GUSGameBattlegroundNoWeaponsFiredDialogInfo