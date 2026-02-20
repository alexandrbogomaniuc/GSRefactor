import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasConfig from '../../../../config/AtlasConfig';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import { COINS_ANIMATIONS_PARAMS } from '../CoinsAward';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import ProfilingInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

const COIN_FLY_DURATION			= 9*FRAME_RATE;

class BigWinCoinsAward extends Sprite
{

	static get EVENT_ON_COIN_LANDED() 		{ return "EVENT_ON_COIN_LANDED"; }
	static get EVENT_AWARDING_COMPLETED() 	{ return "EVENT_AWARDING_COMPLETED"; }

	i_startAnimation(aCoinsNumber_int = 15)
	{
		this._startAnimation(aCoinsNumber_int);
	}

	get totalWin()
	{
		return this._fTotalWin_num;
	}

	constructor(aTotalWin_num, aStartPos_pt, aEndPos_pt, lOrigPos_pt = null)
	{
		super();

		this._fTotalWin_num = aTotalWin_num;
		this._fStartPosition_pt = aStartPos_pt;
		this._fEndPosition_pt = aEndPos_pt || this._defaultPoint;

		this._fGenerateId_num = 0;
		this._fSpeed_num = 1;
		this._fBundleCoinsTimer_t = null;
		this._fBundlePayValue_num = null;
		this._fBundleCoins_sprt_arr = null;
		this._fBundleCoinsCounter_int = 0;
		this._fBundleCoinsMoneyStep_num = null;
		this._fBundleCoinsMoneyCounter_num = null;
		this._fBundleDropPaths_arr_arr = null;
		this._fOrigPos_pt = lOrigPos_pt;
	}

	get _defaultPoint()
	{
		return new PIXI.Point(480, 540);
	}

	get _masterSeatId()
	{
		return APP.playerController.info.seatId;
	}

	get _targetSeatId()
	{
		return this._masterSeatId;
	}

	_startAnimation(aCoinsNumber_int)
	{
		this._fBundleCoinsCounter_int = aCoinsNumber_int;

		this._fBundleCoins_sprt_arr = [];
		for (let i = 0; i < this._fBundleCoinsCounter_int; i++)
		{
			this._fBundleCoins_sprt_arr.push(this.addChild(this._generateCoin()));
		}

		this._fBundlePayValue_num = this._fTotalWin_num;
		this._fBundleCoinsMoneyStep_num = Math.ceil(this._fBundlePayValue_num/this._fBundleCoinsCounter_int);
		this._fBundleCoinsMoneyCounter_num = 0;

		let lX_num = this._fStartPosition_pt.x;
		let lY_num = this._fStartPosition_pt.y;
		this._fillBundleDropPath(this._fStartPosition_pt, {x: lX_num, y: lY_num + 100}, this._fSpeed_num);

		this._onBundleCoinFlyInTime();

	}

	_onBundleCoinFlyInTime(aCoinIndex_int = 0)
	{
		this._fBundleCoinsTimer_t && this._fBundleCoinsTimer_t.destructor();

		let lCoin_sprt = this._fBundleCoins_sprt_arr[aCoinIndex_int];
		if (lCoin_sprt)
		{
			let lStartPosition_pt = this._fBundleDropPaths_arr_arr[aCoinIndex_int][0];

			let lStartScale_num = 0.71;

			lCoin_sprt.scale.set(lStartScale_num);
			lCoin_sprt.scale.x *= -1;
			lCoin_sprt.position.set(lStartPosition_pt.x, lStartPosition_pt.y);
			lCoin_sprt.visible = true;
			lCoin_sprt.play();

			let lOrigPos_pt = this._fOrigPos_pt ? this._fOrigPos_pt : {x: APP.config.size.width/2, y: APP.config.size.height/2};
			lCoin_sprt.position.set(lOrigPos_pt.x, lOrigPos_pt.y - 40);

			let lSequence_s = this._fBundleDropPaths_arr_arr[aCoinIndex_int][1];
			Sequence.start(lCoin_sprt, lSequence_s).once(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, () => {
				this._startBundleCoinFlyOut();
			});
		}

		if (aCoinIndex_int < this._fBundleCoins_sprt_arr.length - 1)
		{
			this._fBundleCoinsTimer_t = new Timer(this._onBundleCoinFlyInTime.bind(this, aCoinIndex_int + 1), 6*Math.random());
		}
	}

	_startBundleCoinFlyOut()
	{
		if (this._fBundleCoinsCounter_int > 0 && this._fBundleCoins_sprt_arr[this._fBundleCoinsCounter_int-1])
		{
			let lIndex_int = this._fBundleCoins_sprt_arr.length - this._fBundleCoinsCounter_int--;
			let lOutCoin_sprt = this._fBundleCoins_sprt_arr[lIndex_int];
			lOutCoin_sprt.position.x = this._fBundleCoins_sprt_arr[lIndex_int].position.x;
			lOutCoin_sprt.position.y = this._fBundleCoins_sprt_arr[lIndex_int].position.y;

			this._onBundleCoinFlyOutTime(lOutCoin_sprt);
		}
		else
		{
			this._completeAwarding();
		}
	}

	_onBundleCoinFlyOutTime(aCoin_sprt)
	{
		let lRestCoinsAmount_int = this._fBundleCoinsCounter_int;
		let lCoinFlyDuration_num = COIN_FLY_DURATION / this._fSpeed_num;

		if (aCoin_sprt)
		{
			aCoin_sprt.visible = true;
			aCoin_sprt.play();
			aCoin_sprt.moveTo(this._fEndPosition_pt.x, this._fEndPosition_pt.y, lCoinFlyDuration_num, null, () => {
				aCoin_sprt.destroy();

				this._playCoinDropSoundSuspicion();

				if (!this._fBundlePayValue_num)
				{
					this._fBundleCoinsMoneyStep_num = 0;
				}
				else if ((this._fBundleCoinsMoneyCounter_num + this._fBundleCoinsMoneyStep_num) <= this._fBundlePayValue_num)
				{
					this._fBundleCoinsMoneyCounter_num += Number(this._fBundleCoinsMoneyStep_num);
				}
				else
				{
					this._fBundleCoinsMoneyStep_num = Number(this._fBundlePayValue_num) - this._fBundleCoinsMoneyCounter_num;
					this._fBundlePayValue_num = 0;
				}

				this.emit(BigWinCoinsAward.EVENT_ON_COIN_LANDED, {money: this._fBundleCoinsMoneyStep_num, seatId: this._targetSeatId});

				if (lRestCoinsAmount_int === 0)
				{
					this._completeAwarding();
				}

			});
		}
	}

	_fillBundleDropPath(aStart_obj={x: 0, y: 0}, startOffset, aSpeed_num)
	{
		let lOrigStartX_num = aStart_obj.x + startOffset.x;
		let lOrigStartY_num = aStart_obj.y + startOffset.y;

		this._fBundleDropPaths_arr_arr = [];

		for (let i = 0; i < this._fBundleCoinsCounter_int; ++i)
		{
			let lCoinParams_obj = COINS_ANIMATIONS_PARAMS[i];
			let lFinishX_num = lCoinParams_obj.x/2;
			let lFinishY_num = lCoinParams_obj.y/2;
			let lStartX_num = lOrigStartX_num;
			let lStartY_num = lOrigStartY_num + 20;
			let lWiggleYOffset_num = 25;

			lFinishX_num += lStartX_num;
			lFinishY_num += lStartY_num;

			let seq = [
				{tweens: [], duration: lCoinParams_obj.startFrame + FRAME_RATE},
				{tweens: [	{prop:"position.x",	to: lFinishX_num}, {prop:"position.y", to: lFinishY_num},{prop: 'scale.x', to: -1}, {prop: 'scale.y', to: 1}],
				duration: 6 * FRAME_RATE / aSpeed_num,ease: Easing.sine.easeOut},
				{tweens: [{ prop: 'position.y', to: lFinishY_num + lWiggleYOffset_num}],duration: 6 * FRAME_RATE / aSpeed_num, ease: Easing.sine.easeOut},
				{tweens: [{ prop: 'position.y', to: lFinishY_num}],duration: 6 * FRAME_RATE / aSpeed_num,ease: Easing.sine.easeOut}
			];

			seq = [
				{tweens: [	{prop:"position.x",	to: lFinishX_num}, {prop:"position.y", to: lFinishY_num},{prop: 'scale.x', to: -1}, {prop: 'scale.y', to: 1}],
				duration: 2 * FRAME_RATE / aSpeed_num,ease: Easing.cubic.easeOut},
				{tweens: [{ prop: 'position.y', to: lFinishY_num + lWiggleYOffset_num}],duration: 6 * FRAME_RATE / aSpeed_num, ease: Easing.sine.easeOut},
				{tweens: [],duration: 4 * FRAME_RATE / aSpeed_num},
				{tweens: [{ prop: 'position.y', to: lFinishY_num}],duration: 6 * FRAME_RATE / aSpeed_num,ease: Easing.sine.easeOut},
			];

			this._fBundleDropPaths_arr_arr.push([new PIXI.Point(lFinishX_num - Math.floor(2+Math.random()*3), lFinishY_num - Math.floor(2+Math.random()*3)), seq]);
		}
	}

	_playCoinDropSoundSuspicion()
	{
		APP.soundsController.play('wins_coincollect');
	}

	_generateCoin()
	{
		let lCoin_sprt = new Sprite();
		lCoin_sprt.textures = this._coinTextures;

		++this._fGenerateId_num;
		if (this._fGenerateId_num >= lCoin_sprt.textures.length) this._fGenerateId_num = 0;
		let lStartFrameIndex_num = ++this._fGenerateId_num;

		lCoin_sprt.anchor.set(0.5);
		lCoin_sprt.gotoAndStop(lStartFrameIndex_num);
		lCoin_sprt.visible = false;

		return lCoin_sprt;
	}

	get _coinTextures()
	{
		return AtlasSprite.getFrames([APP.library.getAsset("common/coin_spin")], AtlasConfig.WinCoin, "");
	}

	_completeAwarding()
	{
		this.emit(BigWinCoinsAward.EVENT_AWARDING_COMPLETED);
	}

	destroy()
	{
		if (this._fBundleCoins_sprt_arr && this._fBundleCoins_sprt_arr.length > 0)
		{
			for (let lCoin_sprt of this._fBundleCoins_sprt_arr)
			{
				Sequence.destroy(Sequence.findByTarget(lCoin_sprt));
			}
			this._fBundleCoins_sprt_arr = null;
		}

		this._fBundleCoinsTimer_t && this._fBundleCoinsTimer_t.destructor();
		this._fBundleCoinsTimer_t = null;

		super.destroy();

		this._fOrigPos_pt = null;
	}
}

export default BigWinCoinsAward;