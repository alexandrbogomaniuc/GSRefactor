import Sprite from '../../../../unified/view/base/display/Sprite';
import { APP } from '../../../../unified/controller/main/globals';
import GUSLobbyBulletCostIndicatorView from './GUSLobbyBulletCostIndicatorView';
import TextField from '../../../../unified/view/base/display/TextField';

class GUSLobbyBulletRangeCostIndicatorView extends Sprite
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
		this.__addBack();
		
		this._fBulletMinCostIndicatorView_bciv = null;
		this._fBulletMaxCostIndicatorView_bciv = null;
		this._fDash_tf = null;
		this._fMaxBetLevel_num = null;

		this._initMinCostIndicator();
		this._initMaxCostIndicator();
		this._initDashTextField();
	}

	__addBack()
	{
	}
	//...INIT

	_initDashTextField()
	{
		let lStyle_obj = this.__dashTFStyle;
		let lPos_obj = this.__dashTFPosition;

		this._fDash_tf = this.addChild(new TextField(lStyle_obj));
		this._fDash_tf.position.set(lPos_obj.x, lPos_obj.y);

		this._fDash_tf.text = '-';
		
	}

	get __dashTFStyle()
	{
		let lInputConfig_obj = {
			fontFamily: "sans-serif",
			fontSize: 14,
			align: "center"
		}

		return lInputConfig_obj;
	}

	get __dashTFPosition()
	{
		return {x: 0, y: -10};
	}

	//VALUE...
	__provideBulletCostIndicatorInstance()
	{
		return new GUSLobbyBulletCostIndicatorView();
	}

	_initMinCostIndicator()
	{
		this._fBulletMinCostIndicatorView_bciv = this.addChild(this.__provideBulletCostIndicatorInstance());

		let lPos_obj = this.__minCostIndicatorPosition;
		this._fBulletMinCostIndicatorView_bciv.position.set(lPos_obj.x, lPos_obj.y);

		return this._fBulletMinCostIndicatorView_bciv;
	}

	get __minCostIndicatorPosition()
	{
		return {x: -20, y: 0};
	}

	get minCostIndicator()
	{
		return this._fBulletMinCostIndicatorView_bciv || (this._fBulletMinCostIndicatorView_bciv = this._initMinCostIndicator());
	}

	_initMaxCostIndicator()
	{
		this._fBulletMaxCostIndicatorView_bciv = this.addChild(this.__provideBulletCostIndicatorInstance());
		
		let lPos_obj = this.__maxCostIndicatorPosition;
		this._fBulletMaxCostIndicatorView_bciv.position.set(lPos_obj.x, lPos_obj.y);

		return this._fBulletMaxCostIndicatorView_bciv;
	}

	get __maxCostIndicatorPosition()
	{
		return {x: 20, y: 0};
	}

	get maxCostIndicator()
	{
		return this._fBulletMaxCostIndicatorView_bciv || (this._fBulletMaxCostIndicatorView_bciv = this._initMaxCostIndicator());
	}

	_updateStake(aStake)
	{
		this.minCostIndicator.indicatorValue = aStake;
		this.maxCostIndicator.indicatorValue = aStake * this.maxBetLevel;
	}

	get maxBetLevel()
	{
		return this._fMaxBetLevel_num || (this._fMaxBetLevel_num = this._initMaxBetLevel());
	}

	_initMaxBetLevel()
	{
		this._fMaxBetLevel_num = APP.playerController.info.possibleBetLevels ? Math.max.apply(null, APP.playerController.info.possibleBetLevels) : 1;

		return this._fMaxBetLevel_num;
	}

	destroy()
	{
		this._fBulletMinCostIndicatorView_bciv = null;
		this._fBulletMaxCostIndicatorView_bciv = null;
		this._fDash_tf = null;
		this._fMaxBetLevel_num = null;

		super.destroy();
	}
}

export default GUSLobbyBulletRangeCostIndicatorView;