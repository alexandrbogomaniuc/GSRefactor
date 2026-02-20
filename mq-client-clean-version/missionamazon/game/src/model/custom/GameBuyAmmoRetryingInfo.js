import SimpleInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

class GameBuyAmmoRetryingInfo extends SimpleInfo 
{
	constructor()
	{
		super();

		this._fIsRetryDialogActive_bl = false;
	}

	get isRetryDialogActive()
	{
		return this._fIsRetryDialogActive_bl;
	}

	set isRetryDialogActive(value)
	{
		this._fIsRetryDialogActive_bl = value;
	}
}

export default GameBuyAmmoRetryingInfo;