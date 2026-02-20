import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import NonWobblingTextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/NonWobblingTextField';
import AlignDescriptor from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';
import { FRAME_RATE } from '../../../config/Constants';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';
import GameplayStateScreenBaseStartView from './GameplayStateScreenBaseStartView';

const PRE_LAUNCH_TARGET_X = 0;
const PRE_LAUNCH_TARGET_Y = 55;

class GameplayStateScreenStartView extends GameplayStateScreenBaseStartView
{
	static get EVENT_ON_TIMER_VALUE_UPDATED ()					{ return GameplayStateScreenBaseStartView.EVENT_ON_TIMER_VALUE_UPDATED; }

	constructor()
	{
		super();

		this._fBlackoutContainer_sprt = null;
		this._fTimeToStart_tf = null;
		this._fBlackout_gr = null;
		this._fContentContainer_sprt = null;
		this._fTimerContainer_sprt = null;
		this._fBaseContentScale_num = 1;

		let l_gpi = APP.gameController.gameplayController.info;

		//BLACKOUT...
		this._addBlackoutView();
		this._updateBlackoutView();
		//...BLACKOUT

		//CONTENT...
		this._addContent();
		//...CONTENT

		this._fBlackoutOutroAnimation_mtl = null;
		this._fOutroAnimation_mtl = null;

		this._tryToAddOutroAnimation();

		if (l_gpi.isPreLaunchFlightRequired)
		{
			this._addZoomOutAnimation();
		}
	}

	_tryToAddOutroAnimation()
	{
		if (this._fOutroAnimation_mtl)
		{
			return;
		}

		let l_gpi = APP.gameController.gameplayController.info;
		let lBetsInfo_bsi = l_gpi.gamePlayersInfo.betsInfo;
		if (lBetsInfo_bsi.isNoMoreBetsPeriodModeDefined)
		{
			if (lBetsInfo_bsi.isNoMoreBetsPeriodMode)
			{
				this._addTimerTransitionAnimations();
			}
			this._addOutroAnimation();
		}
	}

	_addOutroAnimation()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lBetsInfo_bsi = l_gpi.gamePlayersInfo.betsInfo;

		let lOutroFramesAmount_num = lBetsInfo_bsi.isNoMoreBetsPeriodMode ? 20 : 50;
		let lOutroOpacityFramesAmount_num = lBetsInfo_bsi.isNoMoreBetsPeriodMode ? lOutroFramesAmount_num*0.5 : lOutroFramesAmount_num;

		//BLACKOUT OUTRO ANIMATION...
		let l_mt = new MTimeLine();
		//BLACKOUT...
		l_mt.addAnimation(
			this._fBlackoutContainer_sprt,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0, lOutroFramesAmount_num]
			]);
		//...BLACKOUT
		this._fBlackoutOutroAnimation_mtl = l_mt;
		//...BLACKOUT OUTRO ANIMATION

		//OUTRO ANIMATION...
		l_mt = new MTimeLine();
		//CONTENT...
		l_mt.addAnimation(
			this._fContentContainer_sprt,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0, lOutroOpacityFramesAmount_num]
			]);
		//...CONTENT

		if (!lBetsInfo_bsi.isNoMoreBetsPeriodMode)
		{
			let lBaseScale_num = this._fBaseContentScale_num = l_gpi.isPreLaunchFlightRequired ? 0.75 : 1;
			l_mt.addAnimation(
				this._fContentContainer_sprt,
				MTimeLine.SET_SCALE,
				lBaseScale_num,
				[
					lOutroFramesAmount_num*0.5,
					[lBaseScale_num*1.25, lOutroFramesAmount_num*0.5]
				]);
		}

		this._fOutroAnimation_mtl = l_mt;
		//...OUTRO ANIMATION
	}

	_addTimerTransitionAnimations()
	{
		//NO MORE BETS TIMER OUT ANIMATION...
		let l_mt = new MTimeLine();
		l_mt.addAnimation(
			this._fTimerContainer_sprt,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0, 4]
			]);
		this._fNoMoreBetsTimerOutAnimation_mtl = l_mt;
		//...NO MORE BETS TIMER OUT ANIMATION

		//PREPARE FOR LIFTOFF IN ANIMATION...
		l_mt = new MTimeLine();
		l_mt.addAnimation(
			this._fPreparingForLiftoffLabel_ta,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 4]
			]);
		this._fPrepToLiftoffAppearAnimation_mtl = l_mt;
		//...PREPARE FOR LIFTOFF IN ANIMATION

		//PREPARE FOR LIFTOFF OUT ANIMATION...
		l_mt = new MTimeLine();
		l_mt.addAnimation(
			this._fPreparingForLiftoffLabel_ta,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0, 4]
			]);
		this._fPrepToLiftoffDisappearAnimation_mtl = l_mt;
		//...PREPARE FOR LIFTOFF OUT ANIMATION
	}

	_addZoomOutAnimation()
	{
		let l_gpi = APP.gameController.gameplayController.info;

		//ZOOM OUT ANIMATION...
		let l_mt = new MTimeLine();
		let lMoveDuration_num = Math.trunc(l_gpi.preLaunchZoomOutDuration/FRAME_RATE);
		
		l_mt.addAnimation(
			this._fContentContainer_sprt,
			MTimeLine.SET_X,
			PRE_LAUNCH_TARGET_X + 90,
			[
				[PRE_LAUNCH_TARGET_X, lMoveDuration_num, MTimeLine.EASE_IN_OUT]
			]);

		let lPreLaunchTargetY_num = PRE_LAUNCH_TARGET_Y;
		l_mt.addAnimation(
			this._fContentContainer_sprt,
			MTimeLine.SET_Y,
			lPreLaunchTargetY_num-30,
			[
				[lPreLaunchTargetY_num, lMoveDuration_num, MTimeLine.EASE_IN_OUT]
			]);

		this._fZoomOutAnimation_mtl = l_mt;
		//...ZOOM OUT ANIMATION
	}

	updateArea()
	{
		this._updateBlackoutView();

		this.shipContainer.position.set(-GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width/2, -GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height/2);
	}

	get startScreenViewEndTime()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lRoundStartTime_num = lRoundInfo_ri.roundStartTime;

		return lRoundStartTime_num + this._screenVisibilityExtraTime;
	}

	get sitInOutUnitsContainer()
	{
		return this._fSitInOutUnitsContainer_sprt;
	}

	get _screenVisibilityExtraTime()
	{
		let l_gpi = APP.gameController.gameplayController.info;

		return l_gpi.isPreLaunchFlightRequired ? 0 : 1000;
	}

	adjust()
	{
		this._tryToAddOutroAnimation();

		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lCurGameplayTime_num = l_gpi.gameplayTime;
		let lBetsInfo_bsi = l_gpi.gamePlayersInfo.betsInfo;

		let lEndTime_num = this.startScreenViewEndTime;
		let lBlackoutEndTime_num = lEndTime_num;
		let lBlackoutOutro_mtl = this._fBlackoutOutroAnimation_mtl;
		if (lBetsInfo_bsi.isNoMoreBetsPeriod)
		{
			lBlackoutOutro_mtl.windToMillisecond(0);
			this._fBlackoutContainer_sprt.alpha = 1;
		}
		else if (lBetsInfo_bsi.isNoMoreBetsPeriodMode && lRoundInfo_ri.isRoundPlayState)
		{
			lBlackoutOutro_mtl.windToMillisecond(lCurGameplayTime_num-lRoundInfo_ri.roundStartTime);
		}
		else
		{
			lBlackoutOutro_mtl.windToMillisecond(lCurGameplayTime_num, lBlackoutEndTime_num-lBlackoutOutro_mtl.getTotalDurationInMilliseconds());
		}

		let lOutro_mtl = this._fOutroAnimation_mtl;
		if (lBetsInfo_bsi.isNoMoreBetsPeriod)
		{
			lOutro_mtl.windToMillisecond(0);
			this.alpha = 1;
			this._fContentContainer_sprt.scale.set(this._fBaseContentScale_num);
		}
		else if (lBetsInfo_bsi.isNoMoreBetsPeriodMode && lRoundInfo_ri.isRoundPlayState)
		{
			lOutro_mtl.windToMillisecond(lCurGameplayTime_num-lRoundInfo_ri.roundStartTime);
		}
		else
		{
			lOutro_mtl.windToMillisecond(lCurGameplayTime_num, lEndTime_num-lOutro_mtl.getTotalDurationInMilliseconds());
		}

		let lZoomOutAnimation_mtl = this._fZoomOutAnimation_mtl;
		lZoomOutAnimation_mtl && lZoomOutAnimation_mtl.windToMillisecond(lCurGameplayTime_num, l_gpi.multiplierChangeFlightStartTime-l_gpi.preLaunchFlightDuration);

		let l_tf = this._fTimeToStart_tf;

		if (lBetsInfo_bsi.isNoMoreBetsPeriodMode)
		{
			let lIsTimerVisible_bl = lRoundInfo_ri.isRoundWaitState
									|| (
											lRoundInfo_ri.isRoundBuyInState
											&& lRoundInfo_ri.isBuyInStateStartTimeDefined
											&& (lCurGameplayTime_num - lRoundInfo_ri.buyInStateStartTime) <= this._fNoMoreBetsTimerOutAnimation_mtl.getTotalDurationInMilliseconds()
										);

			l_tf.visible = this._fNextLaunchInLabel_ta.visible = lIsTimerVisible_bl;
			if (lIsTimerVisible_bl)
			{
				if (lRoundInfo_ri.isRoundBuyInState)
				{
					this._fNoMoreBetsTimerOutAnimation_mtl.windToMillisecond(lCurGameplayTime_num-lRoundInfo_ri.buyInStateStartTime);
				}
				else
				{
					this._fNoMoreBetsTimerOutAnimation_mtl.windToMillisecond(0);
				}
			}

			this._fNoMoreBetsLabel_ta.visible = this._fPreparingForLiftoffLabel_ta.visible = lBetsInfo_bsi.isNoMoreBetsPeriod || lRoundInfo_ri.isRoundPlayState;

			if (this._fPreparingForLiftoffLabel_ta.visible)
			{
				if (lRoundInfo_ri.isRoundBuyInState)
				{
					if (lRoundInfo_ri.isBuyInStateStartTimeDefined)
					{
						this._fPrepToLiftoffAppearAnimation_mtl.windToMillisecond(lCurGameplayTime_num-lRoundInfo_ri.buyInStateStartTime);
					}
					else
					{
						this._fPrepToLiftoffAppearAnimation_mtl.windToMillisecond(this._fPrepToLiftoffAppearAnimation_mtl.getTotalDurationInMilliseconds());
					}
				}
				else if (lRoundInfo_ri.isRoundPauseState && lRoundInfo_ri.isPauseStateStartTimeDefined)
				{
					let lAnimMS_num = Math.max(0, lCurGameplayTime_num - lRoundInfo_ri.pauseStateStartTime);
					this._fPrepToLiftoffDisappearAnimation_mtl.windToMillisecond(lAnimMS_num);
				}
				else
				{
					this._fPreparingForLiftoffLabel_ta.visible = false;
				}
			}
		}

		this._fTimerContainer_sprt.position.x = 0;

		let lPrevTimerValue_str = l_tf.text;
		let lRestTimeToRound_num = l_gpi.roundStartRestTime;
		l_tf.text = this._formatTimeValue(lRestTimeToRound_num);
		
		if (l_tf.text !== lPrevTimerValue_str)
		{
			this.emit(GameplayStateScreenStartView.EVENT_ON_TIMER_VALUE_UPDATED, {restTime: lRestTimeToRound_num});
		}
	}

	_addBlackoutView()
	{
		let l_sprt = new Sprite();
		let l_gr = this._fBlackout_gr = l_sprt.addChild(new PIXI.Graphics());

		l_sprt.alpha = 0;
		this.addChild(l_sprt);
		
		this._fBlackoutContainer_sprt = l_sprt;
	}

	_clearBlackoutView()
	{
		let l_gr = this._fBlackout_gr;

		l_gr.cacheAsBitmap = false;
		l_gr.clear();
	}

	_updateBlackoutView()
	{
		let l_gr = this._fBlackout_gr;

		l_gr.cacheAsBitmap = false;
		l_gr.clear();

		l_gr.beginFill(0x000000, 0.72).drawRect(
												-GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width/2-1,
												-GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height/2-1,
												GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width + 2,
												GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height + 2)
			.endFill();
	}

	_addContent()
	{
		let l_gpi = APP.gameController.gameplayController.info;

		this._fShipContainer_sprt = this.addChild(new Sprite);

		let lContainer_sprt = this._fContentContainer_sprt = this.addChild(new Sprite);
		this._fTimerContainer_sprt = this._fContentContainer_sprt.addChild(new Sprite);

		this._fSitInOutUnitsContainer_sprt = this.addChild(new Sprite);
		
		let lLabelAsset_ta = I18.generateNewCTranslatableAsset("TANextLaunchInLabel");
		lLabelAsset_ta.position.set(0, -40);
		this._fNextLaunchInLabel_ta = lLabelAsset_ta;
		this._fTimerContainer_sprt.addChild(lLabelAsset_ta);

		let lBaseScale_num = l_gpi.isPreLaunchFlightRequired ? 0.75 : 1;
		lContainer_sprt.scale.set(lBaseScale_num);

		let lPreparingForLiftoffLabel_ta = this._fPreparingForLiftoffLabel_ta = I18.generateNewCTranslatableAsset("TAPreparingForLiftoffLabel");
		lContainer_sprt.addChild(lPreparingForLiftoffLabel_ta);
		lPreparingForLiftoffLabel_ta.position.set(0, 0);
		lPreparingForLiftoffLabel_ta.visible = false;

		let lNoMoreBetsLabelAsset_ta = this._fNoMoreBetsLabel_ta = I18.generateNewCTranslatableAsset("TANoMoreBetsLabel");
		lContainer_sprt.addChild(lNoMoreBetsLabelAsset_ta);
		lNoMoreBetsLabelAsset_ta.position.set(0, 185);
		lNoMoreBetsLabelAsset_ta.visible = false;

		let l_tf = this._fTimeToStart_tf = new NonWobblingTextField();
		this._fTimerContainer_sprt.addChild(l_tf);
		l_tf.fontName = "fnt_nm_barlow_bold";
		l_tf.fontSize = 100;
		l_tf.fontColor = 0xffffff;
		l_tf.setAlign(AlignDescriptor.CENTER, AlignDescriptor.MIDDLE);
		l_tf.letterSpace = -5;
		l_tf.maxWidth = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width;

		if (l_gpi.isPreLaunchFlightRequired)
		{
			lContainer_sprt.position.x = PRE_LAUNCH_TARGET_X;
			lContainer_sprt.position.y = PRE_LAUNCH_TARGET_Y;
		}
	}

	_formatTimeValue(aTime_num)
	{
		aTime_num = +aTime_num;
		if (isNaN(aTime_num) || aTime_num < 0)
		{
			aTime_num = 0;
		}

		let lMillisecondsCount_int = Math.floor((aTime_num % 1000) / 100);
		let lSecondsCount_int = Math.floor((aTime_num / 1000) % 60);
		let lMinutesCount_int = Math.floor((aTime_num / (1000 * 60)) % 60);

		lMinutesCount_int = (lMinutesCount_int < 10) ? "0" + lMinutesCount_int : lMinutesCount_int;
		lSecondsCount_int = (lSecondsCount_int < 10) ? "0" + lSecondsCount_int : lSecondsCount_int;
		lMillisecondsCount_int = (lMillisecondsCount_int < 10) ? lMillisecondsCount_int + "0" : lMillisecondsCount_int;

		let lFormattedTime_str = `${lMinutesCount_int}:${lSecondsCount_int}:${lMillisecondsCount_int}`;
		return lFormattedTime_str;
	}
}

export default GameplayStateScreenStartView;