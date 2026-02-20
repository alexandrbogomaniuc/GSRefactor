import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyBulletCostIndicatorView from './LobbyBulletCostIndicatorView';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import * as FEATURES from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';

class LobbyBulletRangeCostIndicatorView extends Sprite
{
	updateStake(aStake_num)
	{
		this._updateStake(aStake_num);
	}

	constructor()
	{
		super();

		this._initIndicatorView();
	}
	//INIT...
	_initIndicatorView()
	{
		this._fBulletMinCostIndicatorView_bciv = null;
		this._fBulletMaxCostIndicatorView_bciv = null;
		this._fDash_tf = null;
		this._fMaxBetLevel_num = null;

		this._initStateMinIndicator();
		this._initStateMaxIndicator();
		this._initDashTextField();

	}
	//...INIT

	_initDashTextField()
	{
		let lInputConfig_obj = {
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 18,
			dropShadow: true,
			dropShadowColor: 0xffffff,
			dropShadowAngle: Math.PI / 2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5
		}

		this._fDash_tf = this.addChild(new TextField(lInputConfig_obj));
		this._fDash_tf.text = '-';
		this._fDash_tf.position.set(-5, FEATURES.IE ? -13.5 : -15.5);
	}

	//VALUE...
	_initStateMinIndicator()
	{
		this._fBulletMinCostIndicatorView_bciv = this.addChild(new LobbyBulletCostIndicatorView());

		this._fBulletMinCostIndicatorView_bciv.position.set(-33, 0);

		return this._fBulletMinCostIndicatorView_bciv;
	}

	get _fStateMinIndicator()
	{
		return this._fBulletMinCostIndicatorView_bciv || (this._fBulletMinCostIndicatorView_bciv = this._initStateMinIndicator());
	}


	_initStateMaxIndicator()
	{
		this._fBulletMaxCostIndicatorView_bciv = this.addChild(new LobbyBulletCostIndicatorView());

		this._fBulletMaxCostIndicatorView_bciv.position.set(32, 0);

		return this._fBulletMaxCostIndicatorView_bciv;
	}

	get _fStateMaxIndicator()
	{
		return this._fBulletMaxCostIndicatorView_bciv || (this._fBulletMaxCostIndicatorView_bciv = this._initStateMaxIndicator());
	}

	_updateStake(aStake)
	{
		this._fStateMinIndicator.indicatorValue = aStake;
		this._fStateMaxIndicator.indicatorValue = aStake * this._fMaxBetLevel;
	}

	get _fMaxBetLevel()
	{
		return this._fMaxBetLevel_num || (this._fMaxBetLevel_num = this._initMaxBetLevel());
	}

	_initMaxBetLevel()
	{
		this._fMaxBetLevel_num = APP.playerController.info.possibleBetLevels ? Math.max.apply(null, APP.playerController.info.possibleBetLevels): 1;

		return this._fMaxBetLevel_num;
	}

	destroy()
	{
		this._fBackContainer_sprt = null;
		this._fBulletMinCostIndicatorView_bciv = null;
		this._fBulletMaxCostIndicatorView_bciv = null;
		this._fDash_tf = null;
		this._fMaxBetLevel_num = null;
	}
}

export default LobbyBulletRangeCostIndicatorView;