import { Sequence } from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation";
import TextField from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField";
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { APP } from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import SimpleUIView from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView";
import { FRAME_RATE } from "../../../../../shared/src/CommonConstants";
import { AtlasSprite, Sprite } from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import Counter from '../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/Counter';
import MTimeLine from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine";
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import AtlasConfig from "../../../config/AtlasConfig";

const NUMBER_FORMAT = {
	fontFamily: "fnt_nm_barlow_semibold",
	fontSize: 10,
	align: "center",
	dropShadow: true,
	dropShadowAlpha: .5,
	dropShadowAngle: Math.PI/3,
	dropShadowBlur: false,
	dropShadowDistance: 2,
	fill: 0xffffff
};

const NAME_FORMAT = {
	fontFamily: "fnt_nm_barlow_semibold",
	fontSize: 10,
	align: "left",
	fill: 0xffffff,
	wordWrapWidth: 20,
};

const BOSS_SCORE_FORMAT = {
	fontFamily: "fnt_nm_barlow_bold",
	fontSize: 16,
	align: "center",
	dropShadow: true,
	dropShadowAlpha: .3,
	dropShadowAngle: Math.PI/3,
	dropShadowBlur: true,
	dropShadowDistance: 4,
	fill: 0xffffff
};

let _win_coin_textures = null;
function _generate_win_coin_textures()
{
	if (_win_coin_textures !== null)
	{
		return _win_coin_textures;
	}
	return _win_coin_textures = AtlasSprite.getFrames([APP.library.getAsset("battleground/scoreboard/win_coin")], [AtlasConfig.BTGScoreboardWinCoin], "");
}

class ScoreboardItem extends SimpleUIView
{

	static get EVENT_ON_ADDITIONAL_CELL_APPEARED() 		{ return 'onAdditionalCellAppeared'; }
	static get EVENT_ON_ADDITIONAL_CELL_DISAPPEARED() 	{ return 'onAdditionalCellDisappeared'; }

	static i_getWinCountingDuration()
	{
		return 90*FRAME_RATE;
	}

	static i_getAdditionalCellAnimationDuration()
	{
		return 38*FRAME_RATE;
	}

	static i_getAdditionalCellBackgroundWidth()
	{
		return 70;
	}

	static i_getItemBackgroundWidth()
	{
		return 140;
	}

	static i_getItemBackgroundHeight()
	{
		return 25;
	}

	static i_getItemAlphaByPosition(aPlace_num)
	{
		return 1 - 0.12*(aPlace_num - 1);
	}

	static _getMaxPossibleWinLength()
	{
		return 5; // 5 - because is was discussed that coins(1-10000) will be used instead of different possible values from 0.000001 to 10000000.0
										
	}

	constructor()
	{
		super();

		this._fScore_num = null;
		this._fBossRoundScore_num = null;
		this._fBossRoundScoreStatic_num = null;
		this._fBackground_g = null;
		this._fPosition_tf = null;
		this._fNameField_tf = null;
		this._fScoreField_tf = null;
		this._fBossRoundScoreField_tf = null;
		this._fSeatId_num = null;
		this._fBetAmount_num = null;

		this._fCurrentPlace_num = 6;
		
		this._fScoreFieldPosition_obj = null;
		this._fMainContainer_srp = null;

		this._fAdditionalContainer_spr = null;
		this._fBossRoundWinCounter_c = null;
		this._fCoinsTimer_t = null;
		this._fIsCounting_bl = null;
		this._fIsBossMode_bl = null;

		this._fLightSweepAnimation_mtl = null;
		this._fLightSweep_spr = null;

		this._initView();
	}

	get currentPlace()
	{
		return this._fCurrentPlace_num;
	}

	get name()
	{
		return this._fNameField_tf.text;
	}

	set name(aValue_str)
	{
		this._fNameField_tf.text = aValue_str;
	}

	get score()
	{
		let l_num = Number(this._fScore_num);
		if (!isNaN(l_num))
		{
			return l_num;
		}
		
		return 0;
	}

	get bossRoundScore()
	{
		let l_num = Number(this._fBossRoundScore_num);
		if (!isNaN(l_num))
		{
			return l_num;
		}
		
		return 0;
	}

	get staticTotalScore()
	{
		if (this._fIsCounting_bl)
		{
			return;
		}
		
		if(this.bossRoundScore > 0)
		{
			return this.score + this._fBossRoundScoreStatic_num;
		}
		else
		{
			this._fBossRoundScoreStatic_num = 0;
			return this.score + this.bossRoundScore;
		}
	}

	get totalScore()
	{
		return this.score + this.bossRoundScore;
	}

	get betAmount()
	{
		return this._fBetAmount_num;
	}

	set betAmount(aValue_num)
	{
		this._fBetAmount_num = aValue_num;
	}

	get seatId()
	{
		return this._fSeatId_num;
	}

	set seatId(aValue_num)
	{
		if (aValue_num >= 0 && aValue_num < 6)
		{
			this._fSeatId_num = aValue_num;
		}

		if (this._fSeatId_num === APP.currentWindow.player.seatId
			|| (APP.gameScreen.room && this._fSeatId_num === APP.gameScreen.room.alreadySitInNumber))
		{
			this._fScoreField_tf.textFormat = { fontSize: 12 };
			this._fNameField_tf.textFormat = { fill: 0x000000 };
			this._mySpotBg = APP.library.getSprite("battleground/scoreboard/my_player_background");
            this._mySpotBg.anchor.set(0,0);
            this._mySpotBg.scale.set(1.145,1.08);
            this._mySpotBg.position.set(10,1);
            this._fBackground_g.addChild(this._mySpotBg);
			this._fBackground_g.alpha = this._getMainItemAlpha(this._fCurrentPlace_num);
		}
	}

	get targetBossMultiplierGlobalPosition()
	{
		let lLocal_p = new PIXI.Point;
		lLocal_p.x = ScoreboardItem.i_getItemBackgroundWidth() + ScoreboardItem.i_getAdditionalCellBackgroundWidth()/2;
		lLocal_p.y = this._fBackground_g.getBounds().height/2;

		return this.localToGlobal(lLocal_p.x, lLocal_p.y);
	}

	get isAnimationsPlaying()
	{
		if (
			Sequence.findByTarget(this).length > 0
			|| Sequence.findByTarget(this._fBackground_g).length > 0
			|| Sequence.findByTarget(this._fAdditionalContainer_spr).length > 0
			|| this._fIsCounting_bl
			|| this._fLightSweepAnimation_mtl && this._fLightSweepAnimation_mtl.isPlaying()
		)
		{
			return true;
		}

		return false;
	}

	i_showAdditionalCell(aOptDelay_num=0, aOptForce_bl=false)
	{
		if (this._fIsBossMode_bl)
		{
			return;
		}

		this._fBossRoundScore_num = 0;
		this._fBossRoundScoreStatic_num = 0;
		if (aOptForce_bl)
		{
			this._fAdditionalContainer_spr.position.x = ScoreboardItem.i_getItemBackgroundWidth();
			this._onAdditionalCellAppeared();
		}
		else
		{
			this._startAppearingAdditionalCellAnimation(aOptDelay_num);
		}
		this._setBossRoundScore(0); // for init the value
	}

	i_showAdditionalCellWinCounting()
	{
		if (this._fIsCounting_bl)
		{
			return;
		}

		this._fIsCounting_bl = true;
		let lScoreLength_num = (this.bossRoundScore).toString().length;
		let lCountingDuration_num = Math.min(	// normalize by the score and the possible stake
			ScoreboardItem.i_getWinCountingDuration(),
			lScoreLength_num / 5 * ScoreboardItem.i_getWinCountingDuration() // 5 - because is was discussed that coins(1-10000) will be used instead of different possible values from 0.000001 to 10000000.0
		)
		this._fBossRoundWinCounter_c.startCounting(0, lCountingDuration_num, null, this._onWinCountingCompleted.bind(this));

		this._fLightSweepAnimation_mtl.playSeveralTimes(2);

		this._fCoinsTimer_t = new Timer(()=>{
			this._generateCoinAnimation(this._fCurrentPlace_num*2*FRAME_RATE)
		}, 4*FRAME_RATE, true);
	}

	i_hideAdditionalCell(aOptDelay_num=0, aForce_bl)
	{
		if (aForce_bl)
		{
			Sequence.destroy(Sequence.findByTarget(this._fAdditionalContainer_spr));
			this._fAdditionalContainer_spr.position.x = this._getAdditionalCellInitPositionX();
			this._fIsCounting_bl = false;
			this._fLightSweepAnimation_mtl && this._fLightSweepAnimation_mtl.stop();
			this._fLightSweep_spr.visible = false;
			this._onAdditionalCellDisappeared();
		}
		else
		{
			this._startHidingAdditionalCellAnimation(aOptDelay_num);
		}
	}

	i_resetItem()
	{
		this._fBossRoundWinCounter_c && this._fBossRoundWinCounter_c.stopCounting();
		this._onWinCountingCompleted();

		this._fBossRoundScoreField_tf.text = '-';
		this._fScoreField_tf.text = '-';

		this._fScore_num = 0;
		this._fBossRoundScore_num = 0;
		this._fBossRoundScoreStatic_num = 0;
		this._fBetAmount_num = 0;
		this._fNameField_tf.text = '';

		this._fSeatId_num = null;
		
		this._fScoreField_tf.textFormat = NUMBER_FORMAT;
		this._fNameField_tf.textFormat = NAME_FORMAT;
		if(this._mySpotBg)
		{
			this._fBackground_g.removeChild(this._mySpotBg);
		}

		this._fBackground_g.clear();
		this._fBackground_g.beginFill(0x252525).drawRoundedRect(0, 0, ScoreboardItem.i_getItemBackgroundWidth(), ScoreboardItem.i_getItemBackgroundHeight(), 4);
		this._fBackground_g.alpha = this._getMainItemAlpha(this.currentPlace);
	}

	i_setPosition(aPlace_num, aOptSkipAnimation_bl)
	{
		this._setPosition(aPlace_num, aOptSkipAnimation_bl);
	}

	i_setScore(aScore_num, aOptForceSet_bl)
	{
		this._fScore_num = aScore_num;
		if (aScore_num !== 0 || this.betAmount || aOptForceSet_bl)
		{
			this._fScoreField_tf.text = APP.currencyInfo.i_formatNumber(aScore_num, false, false, 0);
		}
		else
		{
			this._fScoreField_tf.text = '-';
		}
	}

	i_setBossRoundScore(aScore_num, aSkipAnimation_bl)
	{
		if (aSkipAnimation_bl)
		{
			if (this._fIsCounting_bl)
			{
				this._fBossRoundWinCounter_c && this._fBossRoundWinCounter_c.finishCounting();
				this._onWinCountingCompleted();
			}
			this._setBossRoundScore(aScore_num);
			this._fBossRoundScoreStatic_num = aScore_num;
		}
		else
		{
			this._fBossRoundWinCounter_c.startCounting(aScore_num, ScoreboardItem.i_getWinCountingDuration()/2);
		}
	}

	i_incomeScore(aScore_num)
	{
		this.i_setScore(this.score + aScore_num, true);
	}
	
	i_incomeBossRoundScore(aScore_num)
	{
		this._fBossRoundWinCounter_c && this._fBossRoundWinCounter_c.finishCounting();
		this._fBossRoundWinCounter_c.startCounting(this.bossRoundScore+aScore_num, ScoreboardItem.i_getWinCountingDuration()/2);
		this._fBossRoundScoreStatic_num = this.bossRoundScore+aScore_num;
	}

	i_doubleUpBossScores()
	{
		this._fBossRoundWinCounter_c && this._fBossRoundWinCounter_c.finishCounting();
		this._setBossRoundScore(this.bossRoundScore*2);
		this._fBossRoundScoreStatic_num = this.bossRoundScore;
	}

	_initView()
	{
		this._fMainContainer_srp = this.addChild(new Sprite());
		this._fBackground_g = this._fMainContainer_srp.addChild(new PIXI.Graphics());
		let lBackgroundWidth_num = ScoreboardItem.i_getItemBackgroundWidth();
		this._fBackground_g.beginFill(0x252525).drawRoundedRect(0, 0, lBackgroundWidth_num, 25, 4);

		let lBGBounds_obj = this._fBackground_g.getBounds();

		this._fArrowPointer_spr = this._fBackground_g.addChild(APP.library.getSprite("battleground/scoreboard/scoreboard_item_arrow"));
		this._fArrowPointer_spr.anchor.set(0);
		this._fArrowPointer_spr.scale.set(1.25, 1.09);
		this._fArrowPointer_spr.position.y = 2;
		this._fArrowPointer_spr.position.x = 10;

		this._fPosition_tf = this._fMainContainer_srp.addChild(new TextField(NUMBER_FORMAT));
		this._fPosition_tf.position.set(lBGBounds_obj.x+20, lBGBounds_obj.height/2);
		this._fPosition_tf.anchor.set(.5, .5);

		this._fScoreField_tf = this._fMainContainer_srp.addChild(new TextField(NUMBER_FORMAT));

		this._fScoreFieldPosition_obj = {
			x: lBGBounds_obj.x + lBGBounds_obj.width - 20,
			y: lBGBounds_obj.height / 2
		};

		this._fScoreField_tf.position.set(this._fScoreFieldPosition_obj.x, this._fScoreFieldPosition_obj.y);
		this._fScoreField_tf.anchor.set(.5, .5);
		this._fScoreField_tf.text = '-';
		this._fScoreField_tf.maxWidth = ScoreboardItem._getMaxPossibleWinLength() * 5; //5 pixels for each possible symbol

		let lPlaceBounds_obj = this._fPosition_tf.getBounds();
		let lNamePosition_obj = {
			x: lPlaceBounds_obj.x + lPlaceBounds_obj.width + 10,
			y: lBGBounds_obj.height/2 - 2
		};
		let lMaxNameWidth_num = this._fScoreFieldPosition_obj.x - lNamePosition_obj.x - this._fScoreField_tf.maxWidth/2 - 5;

		this._fNameField_tf = this._fMainContainer_srp.addChild(new TextField(NAME_FORMAT));
		this._fNameField_tf.position.set(lNamePosition_obj.x, lNamePosition_obj.y);
		this._fNameField_tf.textFormat = { shortLength: lMaxNameWidth_num };
		this._fNameField_tf.anchor.set(0, .5);

		this._initAdditionalView();
	}

	// BOSS ROUND CELL ...
	_initAdditionalView()
	{
		let lCellWidthNum = ScoreboardItem.i_getAdditionalCellBackgroundWidth();
		let lAddCellMask_g = this.addChild(new PIXI.Graphics());
		lAddCellMask_g.beginFill(0x00ff00).drawRect(ScoreboardItem.i_getItemBackgroundWidth(), 0, lCellWidthNum+2, ScoreboardItem.i_getItemBackgroundHeight());
		let lAdditionalContainerPosX_num = ScoreboardItem.i_getItemBackgroundWidth() - lCellWidthNum - 20;

		this._fAdditionalContainer_spr = this.addChild(new Sprite());
		this._fAdditionalContainer_spr.position.set(lAdditionalContainerPosX_num, 0);
		this._fAdditionalContainer_spr.mask = lAddCellMask_g;

		this._fAdditionalCell_g = this._fAdditionalContainer_spr.addChild(new PIXI.Graphics());
		this._fAdditionalCell_g.beginFill(0x22673f).lineStyle(1,0x2d2d37,1,0).drawRoundedRect(0, 0, lCellWidthNum, 25, 4) ;

		let lCellBounds_obj = this._fAdditionalCell_g.getBounds();

		this._fBossRoundScoreField_tf = this._fAdditionalContainer_spr.addChild(new TextField(BOSS_SCORE_FORMAT));
		this._fBossRoundScoreField_tf.anchor.set(.5, .5);
		this._fBossRoundScoreField_tf.position.set(lCellBounds_obj.width/2, lCellBounds_obj.height/2);
		this._fBossRoundScoreField_tf.text = '-';
		this._fBossRoundScoreField_tf.maxWidth = ScoreboardItem._getMaxPossibleWinLength() * 8; //10 pixels for each possible symbol
		
		this._fBossRoundWinCounter_c = new Counter({callback: this._setBossRoundScore.bind(this)}, {callback: this._getBossRoundScore.bind(this)});

		//LIGHT SWEEP ...
		let lSweepMaskWidth = ScoreboardItem.i_getAdditionalCellBackgroundWidth() + ScoreboardItem.i_getItemBackgroundWidth();
		let lSweepMask_g = this.addChild(new PIXI.Graphics());
		lSweepMask_g.beginFill(0x00ff00).drawRoundedRect(0, 0, lSweepMaskWidth, 25, 4);

		let lInitSweepPositionX_num = ScoreboardItem.i_getItemBackgroundWidth() + ScoreboardItem.i_getAdditionalCellBackgroundWidth() + 50;

		this._fLightSweep_spr = this.addChild(APP.library.getSpriteFromAtlas('battleground/scoreboard/sweep'));
		this._fLightSweep_spr.position.set(lInitSweepPositionX_num, 15);
		this._fLightSweep_spr.scale.set(4);
		this._fLightSweep_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fLightSweep_spr.mask = lSweepMask_g;
		this._fLightSweep_spr.visible = false;

		let l_mtl = new MTimeLine();

		l_mtl.addAnimation(
			this._fLightSweep_spr,
			MTimeLine.SET_X,
			lInitSweepPositionX_num,
			[
				[lInitSweepPositionX_num, 15 + this._fCurrentPlace_num],
				[-50, 25],
			]);

		l_mtl.callFunctionAtFrame(this._fLightSweep_spr.show, 0, this._fLightSweep_spr);
		l_mtl.callFunctionOnFinish(this._fLightSweep_spr.hide, this._fLightSweep_spr);

		this._fLightSweepAnimation_mtl = l_mtl;
		//... LIGHT SWEEP
	}
	// ... BOSS ROUND CELL

	// BOSS ROUND CELL ANIMATIONS ...
	_startAppearingAdditionalCellAnimation(aOptDelay_num=0)
	{
		let l_seq = [
			{
				tweens: [
					{prop: 'position.x', to: ScoreboardItem.i_getItemBackgroundWidth(), ease: Easing.quadratic.easeInOut}
				],
				duration: ScoreboardItem.i_getAdditionalCellAnimationDuration(),
				onfinish: ()=>{
					Sequence.destroy(Sequence.findByTarget(this._fAdditionalContainer_spr));
					this._onAdditionalCellAppeared();
				}
			}
		];

		Sequence.start(this._fAdditionalContainer_spr, l_seq, aOptDelay_num);
	}

	_startHidingAdditionalCellAnimation(aOptDelay_num)
	{
		let lFinalPositionX_num = this._getAdditionalCellInitPositionX();
		let l_seq = [
			{
				tweens: [
					{prop: 'position.x', to: lFinalPositionX_num, ease: Easing.quadratic.easeInOut}
				],
				duration: ScoreboardItem.i_getAdditionalCellAnimationDuration(),
				onfinish: ()=>{
					this._onAdditionalCellDisappeared();
				}
			}
		];

		Sequence.start(this._fAdditionalContainer_spr, l_seq, aOptDelay_num);
	}
	// ... BOSS ROUND CELL ANIMATIONS

	// METHODS FOR BOSS ROUND CELLS COUNTERS...
	_setBossRoundScore(aValue_num)
	{
		if (aValue_num < this.bossRoundScore && this._fIsCounting_bl) // counting down. it means that the prize is counting. not income
		{
			let lDifference_num = this.bossRoundScore - aValue_num;
			this.i_incomeScore(lDifference_num);
		}

		this._fBossRoundScore_num = aValue_num;

		if (aValue_num)
		{
			this._fBossRoundScoreField_tf.text = APP.currencyInfo.i_formatNumber(aValue_num, false, false, 0);
		}
		else if (this.name !== '')
		{
			this._fBossRoundScoreField_tf.text = '0';
		}
		else
		{
			this._fBossRoundScoreField_tf.text = '-';
		}
	}

	_getBossRoundScore()
	{
		return this.bossRoundScore;
	}

	_onWinCountingCompleted()
	{
		this._fIsCounting_bl = false;
	}
	// ... METHODS FOR BOSS ROUND CELLS COUNTERS

	_onAdditionalCellAppeared()
	{
		this._fIsBossMode_bl = true;
		this.emit(ScoreboardItem.EVENT_ON_ADDITIONAL_CELL_APPEARED);
	}

	_onAdditionalCellDisappeared()
	{
		this._fIsBossMode_bl = false;
		this._onWinCountingCompleted();
		this.emit(ScoreboardItem.EVENT_ON_ADDITIONAL_CELL_DISAPPEARED);
	}

	_setPosition(aPlace_num, aOptSkipAnimation_bl=false)
	{
		this.zIndex = 10 - aPlace_num;
		this._fPosition_tf.text = (aPlace_num + 1).toString();

		if (this._fCurrentPlace_num !== aPlace_num)
		{
			if (aOptSkipAnimation_bl)
			{
				Sequence.destroy(Sequence.findByTarget(this));
				Sequence.destroy(Sequence.findByTarget(this._fBackground_g));
				Sequence.destroy(Sequence.findByTarget(this._fAdditionalCell_g));
	
				this.scale.set(1);
				this.position.y = this._getFinalContainerPositionY(aPlace_num);
				this._fBackground_g.alpha = this._getMainItemAlpha(aPlace_num);
				this._fAdditionalCell_g.alpha = 1;
			}
			else
			{
				this._startMovingAnimation(aPlace_num);
			}

			this._fCurrentPlace_num = aPlace_num;
		}
	}

	_startMovingAnimation(aPlace_num)
	{
		Sequence.destroy(Sequence.findByTarget(this));
		Sequence.destroy(Sequence.findByTarget(this._fBackground_g));
		Sequence.destroy(Sequence.findByTarget(this._fAdditionalCell_g));

		if (this._fCurrentPlace_num > aPlace_num)
		{
			this._moveUp();
		}
		else
		{
			this._moveDown();
		}

		let lFinalAlpha_num = this._getMainItemAlpha(aPlace_num);
		let lFinalContainerPositionY_num = this._getFinalContainerPositionY(aPlace_num);

		let lMainBGAlpha_seq = [
			{
				tweens: [{prop: 'alpha', to: lFinalAlpha_num}],
				duration: this._getMovingAnimationDuration()
			}
		];
		let lAdditionalBGAlpha_seq = [
			{
				tweens: [{prop: 'alpha', to: 1}],
				duration: this._getMovingAnimationDuration()
			}
		];

		let lPos_seq = [
			{
				tweens: [{prop: 'position.y', to: lFinalContainerPositionY_num}],
				duration: this._getMovingAnimationDuration()
			}
		];

		Sequence.start(this, lPos_seq);
		Sequence.start(this._fBackground_g, lMainBGAlpha_seq);
		Sequence.start(this._fAdditionalCell_g, lAdditionalBGAlpha_seq);
	}

	_moveDown()
	{
		let lAnimationDuration_num = this._getMovingAnimationDuration();

		let lScale_seq = [
			{
				tweens: [
					{prop: 'scale.x', to: .5},
					{prop: 'scale.y', to: .5}
				],
				duration: lAnimationDuration_num/2
			},
			{
				tweens: [
					{prop: 'scale.x', to: 1},
					{prop: 'scale.y', to: 1}
				],
				duration: lAnimationDuration_num/2
			}
		];
		Sequence.start(this, lScale_seq);
	}

	_moveUp()
	{
		let lAnimationDuration_num = this._getMovingAnimationDuration();

		let lScale_seq = [
			{
				tweens: [
					{prop: 'scale.x', to: 1.2},
					{prop: 'scale.y', to: 1.2}
				],
				duration: lAnimationDuration_num/2
			},
			{
				tweens: [
					{prop: 'scale.x', to: 1},
					{prop: 'scale.y', to: 1}
				],
				duration: lAnimationDuration_num/2
			}
		];
		Sequence.start(this, lScale_seq);
	}

	_generateCoinAnimation(aDelay_num)
	{
		if (this._fBossRoundScore_num > 0 && this._fIsCounting_bl)
		{
			let l_spr = this.addChild(new Sprite());
			l_spr.textures = _generate_win_coin_textures();
			l_spr.scale.set(1);

			let lAddCellBounds_obj = this._fAdditionalContainer_spr.getBounds();
			l_spr.position.set(lAddCellBounds_obj.x+lAddCellBounds_obj.width/2, lAddCellBounds_obj.height/2);

			let l_seq = [
				{
					tweens: [
						{prop: 'scale.x', to: 2},
						{prop: 'scale.y', to: 2},
						{prop: 'position.x', to: ScoreboardItem.i_getItemBackgroundWidth()},
						{prop: 'position.y', to: lAddCellBounds_obj.height/2 - 3},
					],
					duration: 3*FRAME_RATE
				},
				{
					tweens: [
						{prop: 'scale.x', to: 1},
						{prop: 'scale.y', to: 1},
						{prop: 'position.x', to: this._fScoreFieldPosition_obj.x},
						{prop: 'position.y', to: lAddCellBounds_obj.height/2},
					],
					duration: 3*FRAME_RATE,
					onfinish: ()=>{
						Sequence.destroy(Sequence.findByTarget(l_spr));
						l_spr && l_spr.destroy();
						l_spr = null;
					}
				}
			];

			l_spr.play();
			Sequence.start(l_spr, l_seq, aDelay_num);
		}
		else
		{
			this._fCoinsTimer_t && this._fCoinsTimer_t.destructor();
			this._fCoinsTimer_t = null;
		}
	}

	_getMainItemAlpha(aPlace_num)
	{
		if (this._fSeatId_num === APP.currentWindow.player.seatId)
		{
			return 1;
		}
		else
		{
			return ScoreboardItem.i_getItemAlphaByPosition(aPlace_num);
		}
	}

	_getFinalContainerPositionY(aPlace_num)
	{
		let l_num = Number(aPlace_num) || 0;
		return (ScoreboardItem.i_getItemBackgroundHeight() - 1) * l_num;
	}

	_getAdditionalCellInitPositionX()
	{
		return ScoreboardItem.i_getItemBackgroundWidth() - ScoreboardItem.i_getAdditionalCellBackgroundWidth() - 2;
	}

	_getMovingAnimationDuration()
	{
		return 20 * FRAME_RATE;
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));
		Sequence.destroy(Sequence.findByTarget(this._fBackground_g));
		
		this._fScore_num = null;
		this._fBossRoundScore_num = null;
		this._fBossRoundScoreStatic_num = null;
		this._fBackground_g = null;
		this._fPosition_tf = null;
		this._fNameField_tf = null;
		this._fScoreField_tf = null;
		this._fBossRoundScoreField_tf = null;
		this._fSeatId_num = null;
		this._fBetAmount_num = null;

		this._fCurrentPlace_num = null;
		
		this._fScoreFieldPosition_obj = null;
		this._fMainContainer_srp = null;

		this._fAdditionalContainer_spr = null;

		this._fBossRoundWinCounter_c && this._fBossRoundWinCounter_c.destroy();
		this._fBossRoundWinCounter_c = null;
		this._fCoinsTimer_t = null;
		this._fIsCounting_bl = null;
		this._fIsBossMode_bl = null;

		this._fLightSweepAnimation_mtl && this._fLightSweepAnimation_mtl.destroy();
		this._fLightSweep_spr = null;
	}
}

export default ScoreboardItem;