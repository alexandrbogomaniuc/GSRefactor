import { Sprite } from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import { APP } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import BattlegroundCoinBurstsAnimation from "./BattlegroundCoinBurstsAnimation";
import BattlegroundCoinRainsAnimation from "./BattlegroundCoinRainsAnimation";
import BattlegroundConfettisAnimation from "./BattlegroundConfettisAnimation";
import MTimeLine from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine";
import I18 from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18";
import TextField from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField";
import { GAME_VIEW_SETTINGS } from "../../../main/GameBaseView";

const YOU_WON_ANIMATION_START_DELAY = 750;
const LINE_PARAMS = { x: -77, y: 17, width: 150, height: 2 };


class BattlegroundYouWonView extends Sprite
{
	static get EVENT_ON_YOU_WON_ANIMATION_STARTED()			{return "EVENT_ON_YOU_WON_ANIMATION_STARTED";}
	static get EVENT_ON_YOU_WON_ANIMATION_COMPLETED()		{return "EVENT_ON_YOU_WON_ANIMATION_COMPLETED";}
	static get EVENT_ON_WIN_CROWN_ANIMATION_STARTED()		{return "EVENT_ON_WIN_CROWN_ANIMATION_STARTED";}
	static get EVENT_ON_YOU_WIN_INTERRUPT_ANIMATION()		{return "EVENT_ON_YOU_WIN_INTERRUPT_ANIMATION";}

	get isAnimationInProgress()
	{
		return this._fIsInProgress_bl;
	}

	constructor()
	{
		super();

		this._fPrizeSweepFinishPoint_num = 100;
		this._fSweepStartX_num = 0;
		this._fBucksSweepStartX_num = 0;
		this._fPrizeStartPozition_num = 0;
		this._fPrizeEndPozition_num = 0;
		this._fSmokesStartBaseX_num = 0;
		this._fIsInProgress_bl = false;
		this._fIsCompleted_bl = false;
		this._fCurFlashAlpha_num = undefined;
		
		this._fCoinsBurstAnimation_bcbsa = this.addChild(new BattlegroundCoinBurstsAnimation());
		
		this._fConfettisAnimation_bcsa = this.addChild(new BattlegroundConfettisAnimation());

		//YELLOW FLARE BACKGROUND...
		let lYfBackground_spr = this._fYfBackground_spr = this.addChild(APP.library.getSprite("game/battleground/yellow_bg"));
		lYfBackground_spr.alpha = 0.5;
		lYfBackground_spr.scale.set(2.09, 2.023);
		lYfBackground_spr.blendMode = PIXI.BLEND_MODES.ADD;
		//...YELLOW FLARE BACKGROUND

		//SCREEN FLASH...
		let lFlash_gr = this._fFlash_gr = this.addChild(new PIXI.Graphics);
		lFlash_gr.blendMode = PIXI.BLEND_MODES.ADD;
		//...SCREEN FLASH

		//FLASH STATIC...
		let lF_sprt = this._fF_sprt = this.addChild(APP.library.getSprite("game/battleground/ship_explosion/light5_add"));
		lF_sprt.position.set(10, -34);
		lF_sprt.scale.set(5);
		lF_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		//...FLASH STATIC

		//FLASH ROTATABLE...
		let lFR_sprt = this._fFR_sprt = this.addChild(APP.library.getSprite("game/battleground/ship_explosion/light5_add"));
		lFR_sprt.position.set(10, -34);
		lFR_sprt.scale.set(5);
		lFR_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		//...FLASH ROTATABLE
		
		//REDGLOW...
		let lRG_sprt = this._fRG_sprt = this.addChild(APP.library.getSprite("game/battleground/redglow_add"));
		lRG_sprt.position.set(155, 110);
		lRG_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		//...REDGLOW

		this._fYouWonPanelContainer_sprt = this.addChild(new Sprite());

		//YOU WON...
		let lYouWon_ta = this._fYouWon_ta = this._fYouWonPanelContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TABattlegroundYouWon"));
		let lYouWonBounds_r = lYouWon_ta.assetContent.getBounds();
		let lTextWidth_num = this._fTextWidth_num = lYouWonBounds_r.width;
		let lTextHeight_num = this._fTextHeight_num = lYouWonBounds_r.height;
		//...YOU WON

		//YOU WON SWEEP...
		this._fSweep_spr = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/text_highlight"));
		this._fSweep_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fSweep_spr.scale.set(1.2);

		let lMask_ta = lYouWon_ta.addChild(I18.generateNewCTranslatableAsset("TABattlegroundYouWon"));
		this._fSweep_spr.mask = lMask_ta.assetContent;

		let lStartSweepX_num = -lTextWidth_num/2 - this._fSweep_spr.width/2;
		this._fSweep_spr.position.set(lStartSweepX_num, 0);
		//...YOU WON SWEEP

		//STREAK FLARE...
		let lSF_sprt = this._fSF_sprt = this.addChild(APP.library.getSprite("game/battleground/streak_flare"));
		lSF_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lSF_sprt.position.set(this._fTextWidth_num/7, -lTextHeight_num-7);
		//...STREAK FLARE

		//BUCKS TF...
		let lPrize_spr = this._fPrizeContainer_spr = this._fYouWonPanelContainer_sprt.addChild(new Sprite());

		this._fPrize_tf = lPrize_spr.addChild(new TextField(this._getPrizeTextFormat()));
		this._fPrize_tf.text = 'PRIZE';
		this._fPrize_tf.maxWidth = 220;
		this._fPrize_tf.anchor.set(0.5, 0.5);
		this._fPrize_tf.position.y = 52;
		//...BUCKS TF

		//BUCKS SWEEP...
		this._fSweepBucks_spr = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/text_highlight"));
		this._fSweepBucks_spr.blendMode = PIXI.BLEND_MODES.ADD;

		this._fBucksMask_tf = lPrize_spr.addChild(new TextField(this._getPrizeTextFormat()));
		this._fBucksMask_tf.maxWidth = 220;
		this._fBucksMask_tf.anchor.set(0.5, 0.5);
		this._fBucksMask_tf.position.y = 52;

		this._fBucksMask_tf.text = this._fPrize_tf.text;
		this._fSweepBucks_spr.mask = this._fBucksMask_tf;
		//...BUCKS SWEEP		
		
		//COIN RAIN CONTAINER...
		this._fCoinsRainContainerAnimations_arr = [];
		
		this._fCoinRainsAnimation_bcrsa = this.addChild(new BattlegroundCoinRainsAnimation());
		//...COIN RAIN CONTAINER

		//LIGHT_PARTICLE...
		let lLP_sprt = this._fLP_sprt = this.addChild(APP.library.getSprite("game/battleground/light_particle"));
		lLP_sprt.scale.set(23.77);
		lLP_sprt.position.set(10, -50);
		lLP_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		//...LIGHT_PARTICLE

		//LINE...
		this._fResultPanelLine_gr = this._fYouWonPanelContainer_sprt.addChild(new PIXI.Graphics)
																						.beginFill(0xc2aa40, 1)
																						.drawRoundedRect(LINE_PARAMS.x, LINE_PARAMS.y, LINE_PARAMS.width, LINE_PARAMS.height)
																						.endFill();
		//...LINE

		//PLAYER NAME...
		let lUsername_tf = this._fUsername_tf = this._fYouWonPanelContainer_sprt.addChild(new TextField(this._getUsernameTextFormat()));
		lUsername_tf.text = 'USERNAME';
		lUsername_tf.position.set(-41, 27);
		lUsername_tf.maxWidth = 110;
		//...PLAYER NAME

		//CROWN...
		let lCrown_spr = this._fCrown_spr = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/crown"));
		lCrown_spr.scale.set(1.1);
		lCrown_spr.position.set(-63, 38);
		//...CROWN

		//CROWN SWEEP...
		this._fCrownSweep_spr = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/text_highlight"));
		this._fCrownSweep_spr.blendMode = PIXI.BLEND_MODES.ADD;

		let lCrownSweepMask_spr = lCrown_spr.addChild(lCrown_spr.addChild(APP.library.getSprite("game/crown")));
		this._fCrownSweep_spr.mask = lCrownSweepMask_spr;
		this._fCrownSweep_spr.position.set(-130, 38);
		//...CROWN SWEEP

		//CROWN FLASH...
		let lCrFlash_sprt = this._fCrownFlash_sprt = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/battleground/ship_explosion/light5_add"));
		lCrFlash_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lCrFlash_sprt.position.set(-63, 38);

		let lCrLightParticle_spr = this._fCrownParticle_sprt = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/battleground/light_particle"));
		lCrLightParticle_spr.blendMode = PIXI.BLEND_MODES.ADD;
		lCrLightParticle_spr.position.set(-63, 38);
		//...CROWN FLASH

		//ORANGE SMOKE 1...
		let lOS1_spr = this._fOrangeSmoke1_spr = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/battleground/orange_smoke"));
		lOS1_spr.blendMode = PIXI.BLEND_MODES.ADD;
		//...ORANGE SMOKE 1

		//ORANGE SMOKE 2...
		let lOS2_spr = this._fOrangeSmoke2_spr = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/battleground/orange_smoke"));
		lOS2_spr.blendMode = PIXI.BLEND_MODES.ADD;
		//...ORANGE SMOKE 2

		//ORANGE SMOKE 3...
		let lOS3_spr = this._fOrangeSmoke3_spr = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/battleground/orange_smoke"));
		lOS3_spr.blendMode = PIXI.BLEND_MODES.ADD;
		//...ORANGE SMOKE 3

		//CROWN HIGHLIGHTS...
		let lCrHighligh1_spr = this._fCrHighlight1_spr = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/battleground/star_glow")); //TOP
		lCrHighligh1_spr.position.set(-57, 37);
		lCrHighligh1_spr.scale.set(0.44);

		let lCrHighlight2_spr = this._fCrHighlight2_spr = this._fYouWonPanelContainer_sprt.addChild(APP.library.getSprite("game/battleground/star_glow")); // DOWN
		lCrHighlight2_spr.position.set(-71, 45);
		lCrHighlight2_spr.scale.set(0.44);
		//...CROWN HIGHLIGHTS

		this._addAnimation();

		this._fYouWonPanelContainer_sprt.scale.set(1.5);

		this.visible = false;
	}

	_addAnimation()
	{
		let l_ywtl = new MTimeLine();

		let lYfBackground_spr = this._fYfBackground_spr;
		let lF_sprt = this._fF_sprt;
		let lFR_sprt = this._fFR_sprt;
		let lLP_sprt = this._fLP_sprt;
		let lSF_sprt = this._fSF_sprt;
		let lRG_sprt = this._fRG_sprt;
		let lYouWon_ta = this._fYouWon_ta;
		let lPrize_spr = this._fPrizeContainer_spr;
		let lLine_gr = this._fResultPanelLine_gr;
		let lUsername_tf = this._fUsername_tf;
		let lCrown_spr = this._fCrown_spr;
		let lOS1_spr = this._fOrangeSmoke1_spr;
		let lOS2_spr = this._fOrangeSmoke2_spr;
		let lOS3_spr = this._fOrangeSmoke3_spr;
		let lCrFlash_sprt = this._fCrownFlash_sprt;
		let lCrLightParticle_spr = this._fCrownParticle_sprt;
		let lCrHighligh1_spr = this._fCrHighlight1_spr;
		let lCrHighlight2_spr = this._fCrHighlight2_spr;

		//YELLOW FLARE BACKGROUND ANIMATION...
		l_ywtl.addAnimation(
			lYfBackground_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				[0.1, 1],
				[0.91, 6],
				[0.72, 12],
				[1, 8],
				[0.92, 13],
				[0.48, 8],
				[0, 54]
			]);
		//...YELLOW FLARE BACKGROUND ANIMATION

		//FLASH ANIMATION...
		l_ywtl.addAnimation(
			lF_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				[0.16, 1],
				[1, 4],
				[0, 9, MTimeLine.EASE_OUT]
			]);

		l_ywtl.addAnimation(
			lF_sprt,
			MTimeLine.SET_SCALE,
			3.95,
			[
				[6.21, 12],
			]);

		l_ywtl.addAnimation(
			lFR_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				9,
				[0.63, 6],
				[0.23, 18],
				54,
				[0, 13, MTimeLine.EASE_OUT],
			]);
		
		l_ywtl.addAnimation(
			lFR_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			139.3,
			[
				9,
				[143.3, 6],
				[155.3, 18],
				[184.7, 54],
				[193.3, 13]
			]);
		//...FLASH ANIMATION

		//SCREEN FLASH ANIMATION...
		l_ywtl.addAnimation(
			this._updateFlashView,
			MTimeLine.EXECUTE_METHOD,
			0.91,
			[
				[0, 9],
				10
			],
			this);
		//...SCREEN FLASH ANIMATION
		
		//LIGHT PARTICLE ANIMATION...
		l_ywtl.addAnimation(
			lLP_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				4,
				[0.78, 0],
				[0.79, 8],
				[0.19, 91, MTimeLine.EASE_OUT],
				[0.25, 83],
				[0, 30]
			]);
		//...LIGHT PARTICLE ANIMATION

		//STREAK FLARE ANIMATION...
		l_ywtl.addAnimation(
			lSF_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				50,
				[1, 11, MTimeLine.EASE_IN_OUT],
				[0, 16, MTimeLine.EASE_IN_OUT]
			]);
		//...STREAK FLARE ANIMATION

		//REDGLOW ANIMATION...
		l_ywtl.addAnimation(
			lRG_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				2,
				[0.5, 0],
				[0.7, 5],
				[0, 5]
			]);

		l_ywtl.addAnimation(
			lRG_sprt,
			MTimeLine.SET_SCALE,
			1.9,
			[
				2,
				[11.03, 10, MTimeLine.EASE_IN]
			]);
		//...REDGLOW ANIMATION

		//YOU WON ANIMATION...
		l_ywtl.addAnimation(
			lYouWon_ta,
			MTimeLine.SET_SCALE,
			0,
			[
				7,
				[0.647, 0],
				[1.2, 4],
				[1.279, 7],
				[0.96, 5],
				[1.012, 8],
				43,
				[1.002, 8],
				[1.086, 6],
				[0.684, 22]
			]);

		l_ywtl.addAnimation(
			lYouWon_ta,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			-3.1,
			[
				8,
				[1.8, 4],
				[-0.2, 6],
				[1, 5],
				[0, 12],
			]);

		l_ywtl.addAnimation(
			lYouWon_ta,
			MTimeLine.SET_X,
			0,
			[
				[-4, 22]
			]);

		l_ywtl.addAnimation(
			lYouWon_ta,
			MTimeLine.SET_Y,
			0,
			[
				85,
				[-46, 22]
			]);
		//...YOU WON ANIMATION

		//BUCKS ANIMATION...
		l_ywtl.addAnimation(
			lPrize_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				13,
				[0.647, 0],
				[1.2, 4],
				[1.279, 7],
				[0.96, 5],
				[1.012, 8],
				43,
				[1.002, 5],
				[1.086, 6],
				[0.651, 21]
			]);

		l_ywtl.addAnimation(
			lPrize_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			-3.1,
			[
				12,
				[1.8, 4],
				[-0.2, 6],
				[1, 5],
				[0, 12],
			]);

		l_ywtl.addAnimation(
			lPrize_spr,
			MTimeLine.SET_Y,
			-22,
			[
				85,
				[-48.2, 22]
			]);
		//...BUCKS ANIMATION

		//LINE ANIMATION...
		l_ywtl.addAnimation(
			lLine_gr,
			MTimeLine.SET_ALPHA,
			0,
			[
				108,
				[1, 17]
			]);
		//...LINE ANIMATION

		//PLAYER ANIMATION...
		l_ywtl.addAnimation(
			lUsername_tf,
			MTimeLine.SET_ALPHA,
			0,
			[
				108,
				[1, 17]
			]);
		//...PLAYER ANIMATION

		//CROWN ANIMATION...
		l_ywtl.addAnimation(
			lCrown_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				129,
				[0.66, 0],
				[1.556, 4],
				[1.761, 5],
				[0.823, 5],
				[0.946, 3],
				[0.823, 3]
			]);

		l_ywtl.addAnimation(
			lCrown_spr,
			MTimeLine.SET_Y,
			36,
			[
				129,
				[1, 4],
				[-3, 5],
				[38, 5, MTimeLine.EASE_IN]
			]);
			
		l_ywtl.addAnimation(
			lCrown_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			-12.1,
			[
				129,
				[2.7, 4],
				[-5.7, 5],
				[-0.1, 5],
				[-5.2, 2],
				[4.2, 3],
				[-3.4, 2],
				[0, 3]
			]);
		//...CROWN ANIMATION

		//CROWN SMOKE ANIMATION...
		l_ywtl.addAnimation(
			lOS1_spr,
			MTimeLine.SET_Y,
			58,
			[
				124,
				[56, 27]
			]);

		l_ywtl.addAnimation(
			lOS1_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				124,
				[0.28, 0],
				[0.64, 4],
				[0, 23]
			]);

		l_ywtl.addAnimation(
			lOS1_spr,
			MTimeLine.SET_SCALE,
			0.342,
			[
				124,
				[0.571, 27]
			]);

		l_ywtl.addAnimation(
			lOS1_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				124,
				[-31.8, 27]
			]);

		l_ywtl.addAnimation(
			lOS2_spr,
			MTimeLine.SET_Y,
			44,
			[
				126,
				[45, 27]
			]);

		l_ywtl.addAnimation(
			lOS2_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				126,
				[0.28, 0],
				[0.41, 4],
				[0, 34]
			]);

		l_ywtl.addAnimation(
			lOS2_spr,
			MTimeLine.SET_SCALE,
			0.372,
			[
				126,
				[0.601, 38]
			]);

		l_ywtl.addAnimation(
			lOS2_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			-79,
			[
				126,
				[-110.8, 38]
			]);

		l_ywtl.addAnimation(
			lOS3_spr,
			MTimeLine.SET_Y,
			58,
			[
				126,
				[65, 27]
			]);

		l_ywtl.addAnimation(
			lOS3_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				126,
				[0.28, 0],
				[0.41, 4],
				[0, 27]
			]);

		l_ywtl.addAnimation(
			lOS3_spr,
			MTimeLine.SET_SCALE,
			0.372,
			[
				126,
				[0.601, 38]
			]);

		l_ywtl.addAnimation(
			lOS3_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			-28,
			[
				126,
				[-31.8, 31]
			]);
		//...CROWN SMOKE ANIMATION

		//CROWN FLASH ANIMATION...
		l_ywtl.addAnimation(
			lCrFlash_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				125,
				[1, 3],
				[0, 15]
			]);

		l_ywtl.addAnimation(
			lCrFlash_sprt,
			MTimeLine.SET_SCALE,
			0.3,
			[
				125,
				[0.96, 3],
				[0, 15]
			]);

		l_ywtl.addAnimation(
			lCrLightParticle_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				125,
				[1, 3],
				[0, 23]
			]);

		l_ywtl.addAnimation(
			lCrLightParticle_spr,
			MTimeLine.SET_SCALE,
			1.5,
			[
				124,
				[2.76, 3],
				[1.58, 23]
			]);
		//...CROWN FLASH ANIMATION

		//CROWN HIGHLIGH...
		l_ywtl.addAnimation(
			lCrHighligh1_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				161, //322
				[0.44, 5], //10
				[0, 6] //12
			]);
		
		l_ywtl.addAnimation(
			lCrHighligh1_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				161,
				[32, 11]
			]);

		l_ywtl.addAnimation(
			lCrHighlight2_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				183,
				[0.44, 5],
				[0, 6]
			]);

		l_ywtl.addAnimation(
			lCrHighlight2_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				183,
				[32, 11]
			]);
		//...CROWN HIGHLIGHT

		//SWEEP...
		l_ywtl.addAnimation(
			this._updateSweepMovePosition,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				43,
				[1, 32],
				10
			],
			this);
		//...SWEEP

		//BUCKS SWEEP...
		l_ywtl.addAnimation(
			this._updateBucksSweepMovePosition,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				61,
				[1, 18],
				10
			],
			this);
		//...BUCKS SWEEP

		//CROWN SWEEP...
		l_ywtl.addAnimation(
			this._updateCrownSweepMovePosition,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				163,
				[1, 55],
				10
			],
			this);
		//...CROWN SWEEP

		//PRIZE MOVE...
		l_ywtl.addAnimation(
			this._updatePrizeMovePosition,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				85,
				[1, 22],
				10
			],
			this);
		//...PRIZE MOVE

		//SMOKE1 MOVE...
		l_ywtl.addAnimation(
			this._updateFirstSmokeMovePosition,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				124,
				[1, 27],
				10
			],
			this);
		//...SMOKE1 MOVE

		//SMOKE2 MOVE...
		l_ywtl.addAnimation(
			this._updateSecondSmokeMovePosition,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				126,
				[1, 27],
				10
			],
			this);
		//...SMOKE2 MOVE

		//SMOKE3 MOVE...
		l_ywtl.addAnimation(
			this._updateThirdSmokeMovePosition,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				126,
				[1, 27],
				10
			],
			this);
		//...SMOKE3 MOVE

		l_ywtl.callFunctionAtFrame(this._onAnimationStarted, 1, this);
		l_ywtl.callFunctionAtFrame(this._crownAnimationStarted, 129, this);
		l_ywtl.callFunctionOnFinish(this._onTimelineAnimationsCompleted, this);
		
		this._fWonAnimtion_watl = l_ywtl;
		this._fWonAnimtionDuration_num = this._fWonAnimtion_watl.getTotalDurationInMilliseconds();
	}

	get _isMasterPlayerWon()
	{
		let lIsPlayerWon_bl = false;

		let l_gpi = APP.gameController.gameplayController.info;
		let l_ri = l_gpi.roundInfo;
		let lRoundWinners_arr = l_ri.battlegroundRoundWinners;

		if (!l_ri.isRoundPlayActive && !!lRoundWinners_arr && !!lRoundWinners_arr.length)
		{
			let lMasterNickName_str = l_gpi.gamePlayersInfo.observerId;
			for (let i=0; i<lRoundWinners_arr.length; i++)
			{
				if (lRoundWinners_arr[i] === lMasterNickName_str)
				{
					lIsPlayerWon_bl = true;
					break;
				}
			}
		}

		return lIsPlayerWon_bl;
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_ri = l_gpi.roundInfo;

		if (l_ri.isBattlegroundRoundEndTimeDefined && this._isMasterPlayerWon)
		{
			if (this._fIsCompleted_bl)
			{
				this.visible = false;
			}
			else
			{
				this._fIsInProgress_bl = true;

				let lAnimStartTime_num = l_ri.battlegroundRoundEndTime + YOU_WON_ANIMATION_START_DELAY;
				let lCurGameplayTime_num = l_gpi.gameplayTime;

				let lTotalYouWonDuration_num = Math.max(this._fWonAnimtionDuration_num,
														this._fCoinsBurstAnimation_bcbsa.getTotalDuration(),
														this._fCoinRainsAnimation_bcrsa.getTotalDuration(),
														this._fConfettisAnimation_bcsa.getTotalDuration()
														);
				
				this.visible = lCurGameplayTime_num >= lAnimStartTime_num && lCurGameplayTime_num <= (lAnimStartTime_num + lTotalYouWonDuration_num);

				if (this.visible)
				{
					this._fUsername_tf.text = l_gpi.gamePlayersInfo.observerId;
					this._updateUsernamePosition();

					// [OWL] TODO: apply changes for alll systems without any conditions
					if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
					{
						this._fPrize_tf.text = APP.currencyInfo.i_formatNumber(l_ri.battlegroundRoundWinValue, true, APP.isBattlegroundGame, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
					}
					else
					{
						this._fPrize_tf.text = APP.currencyInfo.i_formatNumber(l_ri.battlegroundRoundWinValue, true, APP.isBattlegroundGame, 2, undefined, false);
					}
					this._updateSweeps();
					this._updatePrizePosition();

					if (!this._fWonAnimtion_watl.isPlaying() && !this._fWonAnimtion_watl.isCompleted())
					{
						this._fWonAnimtion_watl.ccc = true;
						this._fWonAnimtion_watl.playFromMillisecond(lCurGameplayTime_num-lAnimStartTime_num);
					}

					this._fCoinsBurstAnimation_bcbsa.adjust(lAnimStartTime_num);
					this._fCoinRainsAnimation_bcrsa.adjust(lAnimStartTime_num+340);
					this._fConfettisAnimation_bcsa.adjust(lAnimStartTime_num+370);
				}
				else if ( lCurGameplayTime_num > (lAnimStartTime_num + lTotalYouWonDuration_num) )
				{
					this._fIsCompleted_bl = false;
					this._fIsInProgress_bl = false;
					this._dropAnimation();
				}
			}
		}
		else
		{
			this._fIsCompleted_bl = false;
			this._fIsInProgress_bl = false;
			this._dropAnimation();
		}
	}

	drop()
	{  	
		this._dropAnimation();
	}

	_dropAnimation()
	{
		this.visible = false;

		this._fWonAnimtion_watl.reset();

		this._fCoinsBurstAnimation_bcbsa.drop();
		this._fCoinRainsAnimation_bcrsa.drop();
		this._fConfettisAnimation_bcsa.drop();

		this.emit(BattlegroundYouWonView.EVENT_ON_YOU_WIN_INTERRUPT_ANIMATION);
	}

	_animationCompleted()
	{
		this._fIsCompleted_bl = true;
		this._fIsInProgress_bl = false;
		this._dropAnimation();

		this.emit(BattlegroundYouWonView.EVENT_ON_YOU_WON_ANIMATION_COMPLETED);
	}

	_crownAnimationStarted()
	{
		this.emit(BattlegroundYouWonView.EVENT_ON_WIN_CROWN_ANIMATION_STARTED);
	}

	_updateSweeps()
	{
		//BUCKS SWEEP...
		this._fBucksMask_tf.text = this._fPrize_tf.text;
		//...BUCKS SWEEP

		//YOU WON SWEEP...
		let lStartSweepX_num = this._fSweepStartX_num = -this._fTextWidth_num/2 - this._fSweep_spr.width/2;
		this._fSweep_spr.position.set(lStartSweepX_num, 0);
		//...YOU WON SWEEP
	}

	_updatePrizePosition()
	{
		let lSideMargin_num = (LINE_PARAMS.width - this._fPrizeContainer_spr.width)/2;
		let lStartSideMargin_num = 0 - GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width/2 + (GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width - this._fPrizeContainer_spr.width)/2; //0 - GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width/
		
		this._fPrizeContainer_spr.x = this._fPrizeStartPozition_num = lStartSideMargin_num;
		this._fPrizeEndPozition_num = LINE_PARAMS.x + lSideMargin_num;

		let l_r = this._fPrizeContainer_spr.getLocalBounds();
		this._fSweepBucks_spr.position.x = this._fBucksSweepStartX_num = l_r.x - 40;
		this._fPrizeSweepFinishPoint_num = l_r.x + l_r.width + 60;
	}

	_updateUsernamePosition()
	{
		if (this._fUsername_tf.width < 110)
		{
			let l_r = this._fCrown_spr.getLocalBounds();
			let lTotalLength_num = l_r.width + 20 + this._fUsername_tf.width;
			let lSideMargin_num = (LINE_PARAMS.width - lTotalLength_num)/2;

			this._fCrown_spr.position.x = LINE_PARAMS.x + lSideMargin_num;
			this._fUsername_tf.position.x = LINE_PARAMS.x + lSideMargin_num + 20;

			this._fSmokesStartBaseX_num = this._fCrown_spr.position.x;

			this._fCrownFlash_sprt.position.x = this._fCrown_spr.position.x;
			this._fCrownParticle_sprt.position.x = this._fCrown_spr.position.x;
	
			this._fCrHighlight1_spr.position.x = this._fCrown_spr.position.x + 6;
			this._fCrHighlight2_spr.position.x = this._fCrown_spr.position.x - 8;
		}
		else
		{
			this._fSmokesStartBaseX_num = -63;
		}
	}

	_updateFlashView(aAlhpa_num=0)
	{
		if (this._fCurFlashAlpha_num === aAlhpa_num)
		{
			return;
		}

		this._fCurFlashAlpha_num = aAlhpa_num;

		let lFlash_gr = this._fFlash_gr;
		this._fFlash_gr.clear();

		if (aAlhpa_num > 0)
		{
			lFlash_gr.beginFill(0xfdf6cd, aAlhpa_num).drawRect(0 - GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width/2, 0 - GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height/2, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height).endFill();
		}
	}

	_onAnimationStarted()
	{
		this.emit(BattlegroundYouWonView.EVENT_ON_YOU_WON_ANIMATION_STARTED);

	}

	_onTimelineAnimationsCompleted()
	{
		this._animationCompleted();
	}

	_updateFirstSmokeMovePosition(aPercent_num)
	{
		let lStartBaseX_num = this._fSmokesStartBaseX_num;
		let lEndX_num = this._fSmokesStartBaseX_num + 2;

		let lDistance_num = Math.abs(lStartBaseX_num - lEndX_num);

		this._fOrangeSmoke1_spr.position.x = lStartBaseX_num + aPercent_num*lDistance_num;
	}

	_updateSecondSmokeMovePosition(aPercent_num)
	{
		let lStartBaseX_num = this._fSmokesStartBaseX_num+13;
		let lEndX_num = this._fSmokesStartBaseX_num + 7;

		let lDistance_num = Math.abs(lStartBaseX_num - lEndX_num);

		this._fOrangeSmoke2_spr.position.x = lStartBaseX_num + aPercent_num*lDistance_num;
	}

	_updateThirdSmokeMovePosition(aPercent_num)
	{
		let lStartBaseX_num = this._fSmokesStartBaseX_num+18;
		let lEndX_num = this._fSmokesStartBaseX_num + 21;

		let lDistance_num = Math.abs(lStartBaseX_num - lEndX_num);

		this._fOrangeSmoke3_spr.position.x = lStartBaseX_num + aPercent_num*lDistance_num;
	}

	_updatePrizeMovePosition(aPercent_num)
	{
		let lStartX_num = this._fPrizeStartPozition_num;
		let lEndX_num = this._fPrizeEndPozition_num;

		let lDistance_num = Math.abs(lStartX_num - lEndX_num);

		this._fPrizeContainer_spr.position.x = lStartX_num + aPercent_num*lDistance_num;
	}

	_updateSweepMovePosition(aPercent_num)
	{
		let lStartX_num = this._fSweepStartX_num;
		let lEndX_num = this._fTextWidth_num/2;

		let lDistance_num = Math.abs(lStartX_num - lEndX_num);

		this._fSweep_spr.position.x = lStartX_num + aPercent_num*lDistance_num;
	}

	_updateBucksSweepMovePosition(aPercent_num)
	{
		let lStartX_num = this._fBucksSweepStartX_num;
		let lEndX_num = this._fPrizeSweepFinishPoint_num;

		let lDistance_num = Math.abs(lStartX_num - lEndX_num);

		this._fSweepBucks_spr.position.x = lStartX_num + aPercent_num*lDistance_num;
	}

	_updateCrownSweepMovePosition(aPercent_num)
	{
		let lStartX_num = -130;
		let lEndX_num = 180;

		let lDistance_num = Math.abs(lStartX_num - lEndX_num);

		this._fCrownSweep_spr.position.x = lStartX_num + aPercent_num*lDistance_num;
	}

	_getUsernameTextFormat()
	{
		return {
			fontFamily: "fnt_nm_roboto_m",
			fontSize: 21,
			fill: 0xffffff
		};
	}

	_getPrizeTextFormat()
	{
		return {
			fontFamily: "fnt_nm_roboto_b",
			fontSize: 56,
			fill: [0xffff7f, 0xfff251, 0xfff494, 0xfff4aa, 0xffe337, 0xb76f1b, 0x755108, 0xa66b1a, 0xe0ce3e],
			fillGradientStops: [0, 0.05, 0.25, 0.28, 0.44, 0.58, 0.66, 0.81, 1],
		};
	}
}
export default BattlegroundYouWonView;