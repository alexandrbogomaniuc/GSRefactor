import DialogInfo from '../../DialogInfo';

class GameBattlegroundNoWeaponsFiredDialogInfo extends DialogInfo
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

export default GameBattlegroundNoWeaponsFiredDialogInfo