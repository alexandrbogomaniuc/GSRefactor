import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class TransitionViewInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._fIsBattlegroundCountDownDialogActivated_bl = null;
		this._fIsCafRoomManagerRoundStartModeActivated_bl = null;
		this._fIsBattlegroundNoWeaponsFiredDialogExpected_bl = null;
		this._fisBattlegroundRoundResultExpected_bl = null;
		this._fBlockChangeState_bl = false;
	}

	get isFeatureActive()
	{
		return true;
	}

	get isBattlegroundCountDownDialogActivated()
	{
		return this._fIsBattlegroundCountDownDialogActivated_bl;
	}

	set isBattlegroundCountDownDialogActivated(aVal_bl)
	{
		this._fIsBattlegroundCountDownDialogActivated_bl = aVal_bl;
	}

	get isCafRoomManagerRoundStartModeActivated()
	{
		return this._fIsCafRoomManagerRoundStartModeActivated_bl;
	}

	set isCafRoomManagerRoundStartModeActivated(aVal_bl)
	{
		this._fIsCafRoomManagerRoundStartModeActivated_bl = aVal_bl;
	}

	get isBattlegroundNoWeaponsFiredDialogExpected()
	{
		return this._fIsBattlegroundNoWeaponsFiredDialogExpected_bl;
	}

	set isBattlegroundNoWeaponsFiredDialogExpected(aVal_bl)
	{
		this._fIsBattlegroundNoWeaponsFiredDialogExpected_bl = aVal_bl;
	}

	get isBattlegroundRoundResultExpected()
	{
		return this._fisBattlegroundRoundResultExpected_bl;
	}

	set isBattlegroundRoundResultExpected(aVal_bl)
	{
		this._fisBattlegroundRoundResultExpected_bl = aVal_bl;
	}

	get blockChangeState()
	{
		return this._fBlockChangeState_bl;
	}

	set blockChangeState(aVal_bl)
	{
		this._fBlockChangeState_bl = aVal_bl;
	}

	destroy()
	{
		super.destroy();
		
		this._fIsBattlegroundCountDownDialogActivated_bl = null;
		this._fIsCafRoomManagerRoundStartModeActivated_bl = null;
		this._fIsBattlegroundNoWeaponsFiredDialogExpected_bl = null;
		this._fisBattlegroundRoundResultExpected_bl = null;
		this._fBlockChangeState_bl = false;
	}
}

export default TransitionViewInfo;