import { Sequence } from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation";
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { Sprite } from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import TextField from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField";
import SimpleUIView from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView";
import { FRAME_RATE } from "../../../../../shared/src/CommonConstants";
import ScoreboardItem from './ScoreboardItem';
import { APP } from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import MTimeLine from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine";
import I18 from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18";
import GameScreen from '../../../main/GameScreen';

const MAX_PLAYERS = 6;

const TIME_DEFAULT_COLOR = 0xFFFFFF;
const TIME_OVER_COLOR = 0xFFFFFF;

const TIME_FORMAT = {
	fontFamily: "fnt_nm_barlow_bold_digits",
	fontSize: 17,
	align: "left",
	fill: TIME_DEFAULT_COLOR
};

const PRIZE_FORMAT = {
	fontFamily: "fnt_nm_barlow_bold",
	fontSize: 17,
	align: "left",
	fill: 0xFFFFFF
};

class ScoreboardView extends SimpleUIView
{

	static get EVENT_ON_BOSS_ROUND_MODE_HIDDEN() 	{ return 'onBossRoundModeHidden';}
	static get EVENT_ON_BOSS_TITLE_HIDDEN()			{ return 'EVENT_ON_BOSS_TITLE_HIDDEN';}
	static get EVENT_ON_SCORE_BOARD_SCORES_UPDATED() { return 'EVENT_ON_SCORE_BOARD_SCORES_UPDATED';}
	static get EVENT_ON_SCORE_BOARD_LAST_SECONDS()	{ return 'EVENT_ON_SCORE_BOARD_LAST_SECONDS';}

	constructor()
	{
		super();

		this._fContainer_spr = null;
		this._fRemainingTimeContainer_spr = null;
		this._fTimerBackground_spr = null;
		this._fTimerTitle_tf = null;
		this._fTime_tf = null;
		this._fScoreboardItems_si_arr = [];
		this._fCurrentAmountOfPlayers_num = 0;
		this._fIsBossRoundMode_bl = false;
		
		this._fBossRoundBackground_g = null;
		this._fBossScoreTitle_tf = null;

		this._fSortTimer_t = null;

		this._fAppliedTimeColor_int = undefined;
		this._fPrevLastSecond_int = undefined;
		
		this._init();
	}

	get isAnimationsPlaying()
	{
		for (let lItem_si of this._fScoreboardItems_si_arr)
		{
			if (lItem_si.isAnimationsPlaying)
			{
				return true;
			}
		}

		if (
			Sequence.findByTarget(this._fBossRoundBackground_g).length > 0
			|| this._fIsBossRoundMode_bl
		)
		{
			return true;
		}
		return false;
	}

	i_interruptAnimations()
	{
		this._sortItems(true);

		if (!this._fIsBossRoundMode_bl)
		{
			this.i_hideBossRoundPanel();
		}
	}

	i_showBossRoundPanel(aOptForce_bl=false)
	{
		if (this._fIsBossRoundMode_bl)
		{
			this._onTimeToShowBossScoreTitle();
			return;
		}

		this._fIsBossRoundMode_bl = true;
		let lArrLength_num = this._fScoreboardItems_si_arr && this._fScoreboardItems_si_arr.length;

		for (let i = 0; i < lArrLength_num; i++)
		{
			let lItem_si = this._fScoreboardItems_si_arr[i];
			lItem_si.i_showAdditionalCell(i*2*FRAME_RATE, aOptForce_bl);
		}

		if (aOptForce_bl)
		{
			this._onTimeToShowBossScoreTitle();
		}
		else
		{
			this._fScoreboardItems_si_arr[0].on(ScoreboardItem.EVENT_ON_ADDITIONAL_CELL_APPEARED, this._onTimeToShowBossScoreTitle.bind(this), this);
		}

		this._fSortTimer_t = new Timer(this._sortItems.bind(this), ScoreboardItem.i_getWinCountingDuration()/2, true);
	}

	i_hideBossRoundPanel()
	{
		this._onTimeToHideBossScoreTitle(0, true);
	}

	i_calculateBossRoundWins()
	{
		this._fSortTimer_t && this._fSortTimer_t.destructor();
		this._fSortTimer_t = null;

		let lArrLength_num = this._fScoreboardItems_si_arr && this._fScoreboardItems_si_arr.length;
		for (let i = 0; i < lArrLength_num; i++)
		{
			let lItem_si = this._fScoreboardItems_si_arr[i];
			lItem_si.i_showAdditionalCellWinCounting();
			this.emit(ScoreboardView.EVENT_ON_SCORE_BOARD_SCORES_UPDATED, {seatId: lItem_si.seatId, win: lItem_si.totalScore});
		}

		let lTitleHideDelay_num = ScoreboardItem.i_getWinCountingDuration() - ScoreboardItem.i_getAdditionalCellAnimationDuration()/2;
		this._onTimeToHideBossScoreTitle(lTitleHideDelay_num, false);
	}

	i_updatePlayersBetsInfo(aSeats_obj_arr)
	{
		let lIsSortNeeded_bl = true; // because this method is called when new dara is recieved. in the new data there are
		for (let lSeat_obj of aSeats_obj_arr)
		{
			let lItemIndex_num = this._getItemIndexInScoreboardBySeatId(lSeat_obj.seatId);

			if (lItemIndex_num === -1)
			{
				continue;
			}
			else
			{
				let lPlayerItem_si = this._fScoreboardItems_si_arr[lItemIndex_num];
				lPlayerItem_si.betAmount = lSeat_obj.betAmount

				if (lSeat_obj.winAmount !== lPlayerItem_si.totalScore) // probably some award animations is playing or being awaited, and there is no needs to sort before awards is shown
				{
					lIsSortNeeded_bl = false;
				}
			}
		}

		if (lIsSortNeeded_bl)
		{
			this._sortItems();
		}
	}

	i_resetScoreboard()
	{
		for (let lItem_si of this._fScoreboardItems_si_arr)
		{
			lItem_si.i_resetItem();
		}

		this._fCurrentAmountOfPlayers_num = 0;
		
		this._resetTimeBlinking();

		this._fPrize_tf.text = '';

		this._fPrevLastSecond_int = undefined;
	}

	i_updatePrizePoolValue(aValuePerPerson_num)
	{
		this._formatPrize(aValuePerPerson_num);
	}

	i_updatePlayersInfo(aSeats_obj_arr)
	{
		this.i_resetScoreboard();

		for (let lSeat_obj of aSeats_obj_arr)
		{
			let lSeatId_num =  lSeat_obj.id;
			let lName_str = lSeat_obj.nickname;

			let lItemIndex_num = this._getItemIndexInScoreboardBySeatId(lSeatId_num);

			if (lItemIndex_num === -1)
			{
				lItemIndex_num = this._fCurrentAmountOfPlayers_num;
				this._fCurrentAmountOfPlayers_num += 1;
			}
			let lPlayerItem_si = this._fScoreboardItems_si_arr[lItemIndex_num];
			
			if (lPlayerItem_si !== undefined)
			{
				if (lPlayerItem_si.seatId === null)
				{
					lPlayerItem_si.seatId = lSeatId_num;
				}

				lPlayerItem_si.name = lName_str;
			}
	
			this._sortItems(true);
		}
	}

	i_updateAllScores(aScoresBySeatId_obj_arr, aBossScoresBySeatId_obj)
	{
		this._aScoresBySeatId_obj_arr = aScoresBySeatId_obj_arr;

		for (let lSeat_obj of aScoresBySeatId_obj_arr)
		{
			let lSeatId_num =  lSeat_obj.seatId;

			let lItemIndex_num = this._getItemIndexInScoreboardBySeatId(lSeatId_num);
			let lPlayerItem_si = this._fScoreboardItems_si_arr[lItemIndex_num];
			
			if (lPlayerItem_si !== undefined)
			{
				let lBossRoundScore_num = aBossScoresBySeatId_obj[lSeatId_num];
				let lUsualScore_num = lSeat_obj.winAmount - lBossRoundScore_num; //because winAmount already contains BossRound win
				lPlayerItem_si.i_setScore(lUsualScore_num, true);
				lPlayerItem_si.i_setBossRoundScore(lBossRoundScore_num, true);
			}
	
			this._sortItems(true);
		}
	}

	i_addScore(aData_obj)
	{
		let lSeatId_num = aData_obj.seatId;
		let lScore_num = aData_obj.win;
		
		let lItemIndex_num = this._getItemIndexInScoreboardBySeatId(lSeatId_num);
		let lItem_si = null;

		if (lItemIndex_num !== -1) // if player is already playing
		{
			lItem_si = this._fScoreboardItems_si_arr[lItemIndex_num]
			lItem_si.i_incomeScore(lScore_num);
			let lWin_num = lItem_si._fIsCounting_bl ? lItem_si.totalScore : lItem_si.staticTotalScore;
			this.emit(ScoreboardView.EVENT_ON_SCORE_BOARD_SCORES_UPDATED, {seatId: lSeatId_num, win: lWin_num});
		}

		this._sortItems();
	}

	i_addBossRoundScore(aData_obj)
	{
		let lSeatId_num = aData_obj.seatId;
		let lScore_num = aData_obj.win;
		
		let lItemIndex_num = this._getItemIndexInScoreboardBySeatId(lSeatId_num)

		if (lItemIndex_num !== -1) // if player is already playing
		{
			this._fScoreboardItems_si_arr[lItemIndex_num].i_incomeBossRoundScore(lScore_num);
			this.emit(ScoreboardView.EVENT_ON_SCORE_BOARD_SCORES_UPDATED, {seatId: lSeatId_num, win: this._fScoreboardItems_si_arr[lItemIndex_num].staticTotalScore});
		}

		this._sortItems();
	}

	i_hideAdditionalCells(aForce_bl)
	{
		let lArrLength_num = Array.isArray(this._fScoreboardItems_si_arr) && this._fScoreboardItems_si_arr.length;
		lArrLength_num && this._fScoreboardItems_si_arr[lArrLength_num-1].on(ScoreboardItem.EVENT_ON_ADDITIONAL_CELL_DISAPPEARED, this._onBossRoundModeHidden.bind(this), this);
		for (let i = 0; i < lArrLength_num; i++)
		{
			let lItem_si = this._fScoreboardItems_si_arr[i];
			lItem_si.i_hideAdditionalCell(i*2*FRAME_RATE, aForce_bl);
		}
	}

	getItemView(aSeatId_num)
	{
		return this._fScoreboardItems_si_arr[this._getItemIndexInScoreboardBySeatId(aSeatId_num)];
	}

	_getItemIndexInScoreboardBySeatId(aSeatId_num)
	{
		if (typeof aSeatId_num !== 'number')
		{
			aSeatId_num = Number(aSeatId_num);
			if (isNaN(aSeatId_num))
			{
				return -1;
			}
		}

		for (let lItem_si of this._fScoreboardItems_si_arr)
		{
			if (lItem_si.seatId === aSeatId_num)
			{
				return lItem_si.currentPlace;
			}
		}
		return -1;
	}

	_init()
	{
		this._fContainer_spr = this.addChild(new Sprite());
		
		for (let i = 0; i < MAX_PLAYERS; i++)
		{
			let l_si = new ScoreboardItem();
			l_si.position.set(0, 29*i);
			l_si.i_setPosition(i);
			//l_si.filters = [new PIXI.filters.DropShadowFilter(90, 2, 0x000000, 0.5)];
			this._fContainer_spr.addChild(l_si);
			this._fScoreboardItems_si_arr.push(l_si);
		}

		this._fContainer_spr.position.set(-10, 80);

		// BOSS SCORE TITLE FOR BOSS ROUND ...
		let lItemMainBackgroundWidth_num  = ScoreboardItem.i_getItemBackgroundWidth();
		let lAdditionalCellWidth_num = ScoreboardItem.i_getAdditionalCellBackgroundWidth();
		let lMask_g = this._fContainer_spr.addChild(new PIXI.Graphics());
		lMask_g.beginFill(0x00ff00).drawRect(lItemMainBackgroundWidth_num, -25, lAdditionalCellWidth_num+2, 25);

		this._fBossRoundBackground_g = this._fContainer_spr.addChild(new PIXI.Graphics());
        this._fBossRoundBackground_g.beginFill(0x55a25a).drawRoundedRect(0, 0, lAdditionalCellWidth_num, 25, 4);
		this._fBossRoundBackground_g.position.set(lItemMainBackgroundWidth_num, 10); //below the mask
		this._fBossRoundBackground_g.mask = lMask_g;

		let lBGBounds_obj = this._fBossRoundBackground_g.getBounds();
		this._fBossScoreTitle_tf = this._fBossRoundBackground_g.addChild(I18.generateNewCTranslatableAsset('TABattlegroundScoreboardBossScore'));
		this._fBossScoreTitle_tf.position.set(lBGBounds_obj.width/2, lBGBounds_obj.height/2);
		// ... BOSS SCORE TITLE FOR BOSS ROUND

		this._initPrizePoolPanel();
		this._initTimerPanel()
	}

	_initPrizePoolPanel()
	{
		let lAlpha_num = ScoreboardItem.i_getItemAlphaByPosition(MAX_PLAYERS-1);
		let lWidth_num = ScoreboardItem.i_getItemBackgroundWidth();
		let lHeigth_num = ScoreboardItem.i_getItemBackgroundHeight();
        let lPositionY_num =  (MAX_PLAYERS + 1) * lHeigth_num + 1; //
		this._fPrizePoolContainer_spr = this._fContainer_spr.addChild(new Sprite());
		this._fPrizePoolContainer_spr.position.set(0, lPositionY_num); 

		let lPrizeBackground_g = APP.library.getSprite('battleground/scoreboard/prize_container_background');
        lPrizeBackground_g.anchor.set(0,0);
        lPrizeBackground_g.scale.set(1.125,1.056);

        this._fPrizePoolContainer_spr.addChild(lPrizeBackground_g);

		this._fPrizeTitle_tf = this._fPrizePoolContainer_spr.addChild(I18.generateNewCTranslatableAsset('TABattlegroundScoreboardPrize'));


		let lTimeBGBounds_int = 69;
        let lPositionX_num = lWidth_num - lTimeBGBounds_int/2;

		this._fPrize_tf = this._fPrizePoolContainer_spr.addChild(new TextField(PRIZE_FORMAT));
		this._fPrize_tf.anchor.set(0.5, 0.5);
        this._fPrize_tf.maxWidth = lTimeBGBounds_int - 18;		
		this._fPrize_tf.position.set(lPositionX_num, ScoreboardItem.i_getItemBackgroundHeight()/2);
	}

	_initTimerPanel()
	{		
		let lAlpha_num = ScoreboardItem.i_getItemAlphaByPosition(MAX_PLAYERS-1);
		let lWidth_num = ScoreboardItem.i_getItemBackgroundWidth();
		let lHeigth_num = ScoreboardItem.i_getItemBackgroundHeight();
        let lPositionY_num = MAX_PLAYERS * lHeigth_num - 2; // approximate eposition of th 8th item. The gap will appear because each item has position = height - 1
		
		this._fRemainingTimeContainer_spr = this._fContainer_spr.addChild(new Sprite());
		this._fRemainingTimeContainer_spr.position.set(0, lPositionY_num);

		let lRemainingTimeContainerBackground_g = APP.library.getSprite('battleground/scoreboard/timer_container_background');
        lRemainingTimeContainerBackground_g.anchor.set(0,0);
        lRemainingTimeContainerBackground_g.scale.set(1.057,1.057);
        this._fRemainingTimeContainer_spr.addChild(lRemainingTimeContainerBackground_g);

		//TEXT ROUND ENDS IN...
		this._fTimerTitle_tf = this._fRemainingTimeContainer_spr.addChild(I18.generateNewCTranslatableAsset('TABattlegroundScoreboardRoundEndsIn'));
		//...TEXT ROUND ENDS IN

		//TIMER...
        this._fTimerBackground_spr = this._fRemainingTimeContainer_spr.addChild(new Sprite());		
		this._fTimerBackground_spr.scale.set(1.1, 1.1);
		this._fTimerBackground_spr.anchor.set(1, 0);
		this._fTimerBackground_spr.position.set(lWidth_num, 1);

		let lTimeBGBounds_obj = this._fTimerBackground_spr.getBounds();
		let lPositionX_num = lTimeBGBounds_obj.x + lTimeBGBounds_obj.width;
		this._fTime_tf = this._fRemainingTimeContainer_spr.addChild(new TextField(TIME_FORMAT));
		this._fTime_tf.anchor.set(1, 0.5);
		this._fTime_tf.maxWidth = lTimeBGBounds_obj.width - 18;
		this._fTime_tf.position.set(lPositionX_num, ScoreboardItem.i_getItemBackgroundHeight()/2);
		//...TIMER

		//BLINKING TIMELINE...
		let l_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fTime_tf,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0, 3],
				5,
				[1, 3],
				5,
			]);

		this._fBlinkingTimerTimeline_mtl = l_mtl;
		//...BLINKING TIMELINE

		APP.gameScreen.on(GameScreen.EVENT_ON_TICK_OCCURRED, this._onGSTickOccurred, this);
	}

	_sortItems(aOptSkipAnimation_bl=false)
	{
		if(!this._aScoresBySeatId_obj_arr) return;
		let lSortedByScores_arr = this._fScoreboardItems_si_arr.sort((a, b)=>{
			return b.totalScore - a.totalScore || b.betAmount - a.betAmount; //in case if players have the same scores the result will be sorted by bets
		});

		for (let i = 0; i < 6; i++)
		{
			let lNewIndex_num = lSortedByScores_arr.indexOf(this._fScoreboardItems_si_arr[i]);

			this._fScoreboardItems_si_arr[i].i_setPosition(lNewIndex_num, aOptSkipAnimation_bl);
		}
	}

	_onTimeToShowBossScoreTitle()
	{
		Sequence.destroy(Sequence.findByTarget(this._fBossRoundBackground_g));
		let l_seq = [
			{
				tweens: [
					{prop: 'position.y', to: -25}
				],
				duration: ScoreboardItem.i_getAdditionalCellAnimationDuration()/2,
				onfinish: ()=>{
					Sequence.destroy(Sequence.findByTarget(this._fBossRoundBackground_g));
				}
			}
		];

		Sequence.start(this._fBossRoundBackground_g, l_seq);
	}

	_onTimeToHideBossScoreTitle(aOptDelay_num, aForce_bl)
	{
		let lDuration_num;
		if (aForce_bl)
		{
			lDuration_num = 0;
			Sequence.destroy(Sequence.findByTarget(this._fBossRoundBackground_g));
		}
		else
		{
			lDuration_num = ScoreboardItem.i_getAdditionalCellAnimationDuration()/2;
		}

		let l_seq = [
			{
				tweens: [
					{prop: 'position.y', to: 10}
				],
				duration: lDuration_num,
				onfinish: ()=>{
					Sequence.destroy(Sequence.findByTarget(this._fBossRoundBackground_g));
					this.emit(ScoreboardView.EVENT_ON_BOSS_TITLE_HIDDEN, {force: aForce_bl});
				}
			}
		];

		Sequence.start(this._fBossRoundBackground_g, l_seq, aOptDelay_num);
	}

	_onBossRoundModeHidden()
	{
		this._fIsBossRoundMode_bl = false;
		this.emit(ScoreboardView.EVENT_ON_BOSS_ROUND_MODE_HIDDEN);

		for (let lItem_si of this._fScoreboardItems_si_arr)
		{
			this.emit(ScoreboardView.EVENT_ON_SCORE_BOARD_SCORES_UPDATED, {seatId: lItem_si.seatId, win: lItem_si.totalScore});
		}
	}

	_formatPrize(aValuePerPerson_num)
	{
		let lPrize_num = aValuePerPerson_num * this._fCurrentAmountOfPlayers_num;
		let lStartPrizeToConvert_num = 100000; //the prize will be converted only if it is more that 100K

		this._fPrize_tf.text = APP.currencyInfo.i_formatNumber(lPrize_num, true, true, 2, lStartPrizeToConvert_num)
	}

	_formatTime()
	{
		let l_si = this.uiInfo;

		let lText_str = "";
		let lTextColor_int = TIME_DEFAULT_COLOR;
		
		if (l_si.isRoundStartTimeDefined && l_si.isRoundStartTimeDefined)
		{
			if (l_si.isRoundTimeIsOver)
			{
				this._fPrevLastSecond_int = undefined;

				this._playTimeBlinking();
				lTextColor_int = TIME_OVER_COLOR;
			}
			else
			{
				this._resetTimeBlinking();
			}

			let lRestDuration_num = l_si.restRoundDuration;
			if (lRestDuration_num < 0)
			{
				lRestDuration_num = 0;
			}

			if (lRestDuration_num > l_si.roundPlayableDuration)
			{
				lRestDuration_num = l_si.roundPlayableDuration;
			}

			let lSecondsCount_int = Math.floor((lRestDuration_num / 1000) % 60);
			let lMinutesCount_int = Math.floor((lRestDuration_num / (1000 * 60)) % 60);

			let lSecondsCount_str = (lSecondsCount_int < 10) ? "0" + lSecondsCount_int : "" + lSecondsCount_int;
			let lMinutesCount_str = (lMinutesCount_int == 0) ? "" : "" + lMinutesCount_int;

			lText_str = lMinutesCount_str + ':' + lSecondsCount_str;

			if (!l_si.isRoundTimeIsOver && lMinutesCount_int == 0 && lSecondsCount_int <= 3)
			{
				if (this._fPrevLastSecond_int != lSecondsCount_int)
				{
					let lMillisecondsCount_int = Math.floor(lRestDuration_num % 1000);

					if (lMillisecondsCount_int > 500)
					{
						//console.log("[DEBUG] Received last-seconds event:", aEvent.seconds);
						
						this._fPrevLastSecond_int = lSecondsCount_int;
						this.emit(ScoreboardView.EVENT_ON_SCORE_BOARD_LAST_SECONDS, {seconds: lSecondsCount_int});
					}
				}
			}
		}
		else
		{
			this._resetTimeBlinking();
		}

		this._fTime_tf.text = lText_str;

		if (this._fAppliedTimeColor_int !== lTextColor_int)
		{
			this._fTime_tf.updateFontColor(lTextColor_int);

			this._fAppliedTimeColor_int = lTextColor_int;
		}
		
	}

	_playTimeBlinking()
	{
		if (!this._fBlinkingTimerTimeline_mtl)
		{
			return;
		}

		if (!this._fBlinkingTimerTimeline_mtl.isPlaying())
		{
			this._fBlinkingTimerTimeline_mtl.playLoop();
		}
	}

	_resetTimeBlinking()
	{
		if (!this._fBlinkingTimerTimeline_mtl)
		{
			return;
		}

		if (this._fBlinkingTimerTimeline_mtl.isPlaying())
		{
			this._fBlinkingTimerTimeline_mtl.stop();
		}

		this._fTime_tf.alpha = 1;
	}

	_onGSTickOccurred(event)
	{
		this._formatTime();
	}

	destroy()
	{
		super.destroy();

		this._fContainer_spr && this._fContainer_spr.destroy();
		this._fContainer_spr = null;
		this._fRemainingTimeContainer_spr = null;
		this._fTimerBackground_spr = null;
		this._fTimerTitle_tf = null;
		this._fTime_tf = null;
		this._fCurrentAmountOfPlayers_num = null;
		this._fIsBossRoundMode_bl = null;

		this._fBossRoundBackground_g = null;
		this._fBossScoreTitle_tf = null;

		for (let lItem_si of this._fScoreboardItems_si_arr)
		{
			lItem_si.destroy();
			lItem_si = null;
		}
		this._fScoreboardItems_si_arr = null;

		this._fSortTimer_t && this._fSortTimer_t.destructor();
		this._fSortTimer_t = null;

		this._fBlinkingTimerTimeline_mtl && this._fBlinkingTimerTimeline_mtl.destroy();
		this._fBlinkingTimerTimeline_mtl = null;

		this._fAppliedTimeColor_int = undefined;
		this._fPrevLastSecond_int = undefined;
		this._aScoresBySeatId_obj_arr = null;
	}
}

export default ScoreboardView;