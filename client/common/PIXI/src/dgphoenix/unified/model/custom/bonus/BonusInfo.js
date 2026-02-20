import SimpleInfo from '../../base/SimpleInfo';

/**
 * Base FRB/Cash bonus info
 * @class
 */
class BonusInfo extends SimpleInfo {

	static get MESSAGE_BONUS_EXPIRED() 			{ return 'MESSAGE_BONUS_EXPIRED'; }
	static get MESSAGE_BONUS_CANCELLED()		{ return 'MESSAGE_BONUS_CANCELLED'; }
	static get MESSAGE_BONUS_RELEASED()			{ return 'MESSAGE_BONUS_RELEASED'; }
	static get MESSAGE_FORCE_SIT_OUT()			{ return 'MESSAGE_FORCE_SIT_OUT'; } //[Y]TODO to remove?
	static get MESSAGE_BONUS_LOST()				{ return 'MESSAGE_BONUS_LOST'; }
	static get MESSAGE_BONUS_LOBBY_INTRO()		{ return 'MESSAGE_BONUS_LOBBY_INTRO'; }
	static get MESSAGE_BONUS_ROOM_INTRO()		{ return 'MESSAGE_BONUS_ROOM_INTRO'; }

	static get STATUS_ACTIVE() 			{ return 'ACTIVE'}//ACTIVE, RELEASED, LOST, CANCELLED, EXPIRED, CLOSED
	static get STATUS_RELEASED() 		{ return 'RELEASED'}
	static get STATUS_LOST() 			{ return 'LOST'}
	static get STATUS_CANCELLED() 		{ return 'CANCELLED'}
	static get STATUS_EXPIRED() 		{ return 'EXPIRED'}
	static get STATUS_CLOSED() 			{ return 'CLOSED'}

	static get TYPE_CASH_BONUS() 		{return 'CASHBONUS'; }
	static get TYPE_FRB() 				{return 'FRB'; }

	/**
	 * Possible bonus states.
	 * @static
	 */
	static get STATUSES() 	{
		return [
			BonusInfo.STATUS_ACTIVE,
			BonusInfo.STATUS_RELEASED,
			BonusInfo.STATUS_LOST,
			BonusInfo.STATUS_CANCELLED,
			BonusInfo.STATUS_EXPIRED,
			BonusInfo.STATUS_CLOSED
		]
	}

	constructor()
	{
		super();

		this._fId_num = undefined;
		this._fIsActivated_bl = false;
		this._fNextModeFRB_bl = undefined;

		this._fCurrentStatus_str = undefined;
		this._fCurrentBalance_num = undefined;
		this._fInitialBalance_num = undefined;
		this._fNextRoomId_num = undefined;

		this._fIsCleared_bl = false;

	}

	/** Clear bonus properties. */
	i_clearAll()
	{
		this._fId_num = undefined;
		this._fIsActivated_bl = false;
		this._fNextModeFRB_bl = undefined;

		this._fCurrentStatus_str = undefined;
		this._fCurrentBalance_num = undefined;
		this._fInitialBalance_num = undefined;
		this._fNextRoomId_num = undefined;

		this._fIsCleared_bl = true;
	}

	set id(aId_num)
	{
		this._fId_num = aId_num;
	}

	/** Bonus id. */
	get id()
	{
		return this._fId_num;
	}

	set isActivated(aValue_bl)
	{
		this._fIsActivated_bl = aValue_bl;
	}

	/** Indicates whether bonus is active or not. */
	get isActivated()
	{
		return this._fIsActivated_bl;
	}

	set nextModeFRB(aValue_bl)
	{
		this._fNextModeFRB_bl = aValue_bl;
	}

	/** Indicates whether game will be restarted to frb mode or not. */
	get nextModeFRB()
	{
		return this._fNextModeFRB_bl;
	}

	set nextRoomId(aValue_num)
	{
		this._fNextRoomId_num = aValue_num;
	}

	/** Room id to restart in. */
	get nextRoomId()
	{
		return this._fNextRoomId_num;
	}

	set currentStatus(aValue_str)
	{
		let lIndex_int = BonusInfo.STATUSES.indexOf(aValue_str);
		if (~lIndex_int)
		{
			this._fCurrentStatus_str = aValue_str;
		}
		else
		{
			throw new Error("Unknown Bonus status: " + aValue_str);
		}
	}

	/** Current bonus status. */
	get currentStatus()
	{
		return this._fCurrentStatus_str;
	}

	set winSum(aValue_num)
	{
		this._fWinSum_num = aValue_num;
	}

	/** Bonus win in cents. */
	get winSum()
	{
		return this._fWinSum_num;
	}

	get currentBalance()
	{
		return this._fCurrentBalance_num;
	}

	set currentBalance(aValue_num)
	{
		this._fCurrentBalance_num = aValue_num;
	}

	get initialBalance()
	{
		return this._fInitialBalance_num;
	}

	set initialBalance(aValue_num)
	{
		this._fInitialBalance_num = aValue_num;
	}

	get isCompleted()
	{
		return this.currentStatus !== BonusInfo.STATUS_ACTIVE;
	}

	/** Is bonus in active state or not. */
	get isActive()
	{
		return this.currentStatus === BonusInfo.STATUS_ACTIVE;
	}

	/** Is bonus info cleared. */
	get isCleared()
	{
		return this._fIsCleared_bl;
	}

	/**
	 * Indicates whether keep SW mode (save player's SW between rounds) is on or not.
	 * Currently keepSW mode is off on the server and not supported.
	 * @ignore
	*/
	get keepBonusSW()
	{
		return true;
	}
}

export default BonusInfo;