import SimpleUIInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class GameBaseModel extends SimpleUIInfo
{
	get gameplayInfo()
	{
		return this._fGameplayInfo_gpi || (this._fGameplayInfo_gpi = this.__generateGameplayInfo());
	}

	get roundDetailsInfo()
	{
		return this._fRoundDetailsInfo_rdsi || (this._fRoundDetailsInfo_rdsi = this.__generateRoundDetailsInfo());
	}

	get betsListInfo()
	{
		return this._fBetsListInfo_bsli || (this._fBetsListInfo_bsli = this.__generateBetsListInfo());
	}

	get placeBetsInfo()
	{
		return this._fPlaceBetsInfo_pbsi || (this._fPlaceBetsInfo_pbsi = this.__generatePlaceBetsInfo());
	}

	constructor()
	{
		super();

		this._fGameplayInfo_gpi = null;
		this._fRoundDetailsInfo_rdsi = null;
		this._fBetsListInfo_bsli = null;
		this._fPlaceBetsInfo_pbsi = null;
	}

	destroy()
	{
		super.destroy();
	}

	__generateGameplayInfo()
	{
		// should be overridden
		return null;
	}

	__generateRoundDetailsInfo()
	{
		// should be overridden
		return null;
	}

	__generateBetsListInfo()
	{
		// should be overridden
		return null;
	}

	__generatePlaceBetsInfo()
	{
		// should be overridden
		return null;
	}
}
export default GameBaseModel;