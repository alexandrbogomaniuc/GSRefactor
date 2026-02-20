import SimpleInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

class BalanceInfo extends SimpleInfo
{
	constructor()
	{
		super();

		this._serverBalanceValue = undefined;
		this._balanceValue = undefined;
		this._fIsAddBetsAllowed_bl = true;
	}

	set balance(value)
	{
		this._balanceValue = value;
	}

	get balance()
	{
		return this._balanceValue;
	}

	get isBalanceValueDefined()
	{
		return this._balanceValue !== undefined;
	}

	isBalanceEnoughForBet(aBetValue_num)
	{
		return this.isBalanceValueDefined && this.balance >= aBetValue_num;
	}

	set serverBalance(value)
	{
		this._serverBalanceValue = value;
	}

	get serverBalance()
	{
		return this._serverBalanceValue;
	}

	set isAddBetsAllowed(value)
	{
		this._fIsAddBetsAllowed_bl = value;
	}

	get isAddBetsAllowed()
	{
		return this._fIsAddBetsAllowed_bl;
	}

	destroy()
	{
		this._balanceValue = undefined;

		super.destroy();
	}
}
export default BalanceInfo;