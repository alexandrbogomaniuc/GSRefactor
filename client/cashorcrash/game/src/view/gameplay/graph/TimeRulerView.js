import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GraphView from './GraphView';
import MAnimation from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MAnimation';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class TimeRulerView extends Sprite
{
	constructor()
	{
		super();

		this._updateDimensions();

		this._fContentContainer_sprt = null;
		this._fTextFields_rctfv_arr = [];
		this._fCurrentTimeValueOnRightMostEndOfRuler_num = this.getInitialTimeValueOnRightMostEndOfRulerAccordingMockap();

		this._fCurAdjustedMultiplierRoundDuration_num = undefined;

		//CONENT CONTAINER...
		this._fContentContainer_sprt = this.addChild(new Sprite());
		this._fLines_gr = this._fContentContainer_sprt.addChild(new PIXI.Graphics());
		//...CONTENT CONTAINER

		this.updateArea();
	}

	_updateDimensions()
	{
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;

		TimeRulerView.RULER_WIDTH = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width;
		TimeRulerView.RULER_HEIGHT = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height - GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y;
		TimeRulerView.RULER_VISUAL_X_OFFSET = 0;
		TimeRulerView.RULER_VISUAL_WIDTH = TimeRulerView.RULER_WIDTH - TimeRulerView.RULER_VISUAL_X_OFFSET;
		TimeRulerView.RULER_VISUAL_Y_OFFSET = TimeRulerView.RULER_HEIGHT - (lIsPortraitMode_bl ? 30 : 35);
		TimeRulerView.RULER_VISUAL_HEIGHT = TimeRulerView.RULER_HEIGHT - TimeRulerView.RULER_VISUAL_Y_OFFSET - (lIsPortraitMode_bl ? 5 : 10);

		//DEBUG...
		// this._debug_gr = this._debug_gr || this.addChild(new PIXI.Graphics);
		// this._debug_gr.clear().beginFill(0x0000ff, 0.8).drawRect(0, 0, TimeRulerView.RULER_WIDTH, TimeRulerView.RULER_HEIGHT).endFill();
		// this._debug_gr.beginFill(0xff0000, 0.5).drawRect(TimeRulerView.RULER_VISUAL_X_OFFSET, TimeRulerView.RULER_VISUAL_Y_OFFSET, TimeRulerView.RULER_VISUAL_WIDTH, TimeRulerView.RULER_VISUAL_HEIGHT).endFill();
		//...DEBUG
	}

	//DEFAULT SETTINGS...
	getInitialTimeValueOnRightMostEndOfRulerAccordingMockap()
	{
		return 1200;
	}
	//...DEFAULT SETTINGS

	getTextFieldView(aIndex_int)
	{
		let l_rctfv_arr = this._fTextFields_rctfv_arr;

		if(!l_rctfv_arr[aIndex_int])
		{
			let lStyle_obj = {
				fontFamily: "fnt_nm_myriad_pro_bold",
				fontSize: 12,
				fill: 0xe4e4e6,
				align: "center"
			};

			let l_rctfv = new TextField(lStyle_obj);
			l_rctfv.anchor.set(0.5, 0.5);
			l_rctfv.position.y = TimeRulerView.RULER_VISUAL_HEIGHT*0.75;
			l_rctfv_arr[aIndex_int] = l_rctfv;

			this._fContentContainer_sprt.addChild(l_rctfv);
		}

		return l_rctfv_arr[aIndex_int];
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lCurGameplayMultiplierRoundDuration_num = l_gpi.multiplierRoundDuration;

		if (this._fCurAdjustedMultiplierRoundDuration_num === lCurGameplayMultiplierRoundDuration_num)
		{
			return;
		}

		let lInitialRulerRightBorderTime_num = this.getInitialTimeValueOnRightMostEndOfRulerAccordingMockap();
		let lCurRulerRightBorderTime_num = lInitialRulerRightBorderTime_num;
		if (lCurGameplayMultiplierRoundDuration_num > lInitialRulerRightBorderTime_num)
		{
			lCurRulerRightBorderTime_num = lCurGameplayMultiplierRoundDuration_num;
		}

		if (APP.isBattlegroundGame){
			lCurRulerRightBorderTime_num*=2;
		}
		
		let lStepTimeSeconds_int = 0.5;
		if (lCurRulerRightBorderTime_num >= 2200)
		{
			let lCurRulerRightBorderTimeSeconds_int = Math.floor(lCurRulerRightBorderTime_num/1000);
			let lDecMultZeroesCount_int = +((""+lCurRulerRightBorderTimeSeconds_int).length-1);
			let lDecMult_str = "1";
			for (let i=0; i<lDecMultZeroesCount_int; i++)
			{
				lDecMult_str += "0";
			}
			let lDecMult_int = +lDecMult_str;
			if (lCurRulerRightBorderTimeSeconds_int < 2*lDecMult_int)
			{
				lDecMult_int/=10;
			}
			let lStepsBorderSeconds_num = 20*lDecMult_int;
			lStepTimeSeconds_int = 2*lDecMult_int;
			if (lCurRulerRightBorderTimeSeconds_int < 4*lDecMult_int)
			{
				lStepsBorderSeconds_num = 4*lDecMult_int;
				lStepTimeSeconds_int = 1*lDecMult_int;
			}
		}

		//REFILL...
		this.refillRuler(
			lStepTimeSeconds_int*1000,
			lCurRulerRightBorderTime_num);
		//...REFILL

		this._fCurrentTimeValueOnRightMostEndOfRuler_num = lCurRulerRightBorderTime_num;
		this._fCurAdjustedMultiplierRoundDuration_num = lCurGameplayMultiplierRoundDuration_num;
	}

	updateArea()
	{
		this._updateDimensions();

		this._fContentContainer_sprt.position.set(TimeRulerView.RULER_VISUAL_X_OFFSET, TimeRulerView.RULER_VISUAL_Y_OFFSET);

		this._fCurAdjustedMultiplierRoundDuration_num = undefined;
	}

	clearRuler()
	{
		for( let i = 0; i < this._fTextFields_rctfv_arr.length; i++ )
		{
			if (this._fTextFields_rctfv_arr[i])
			{
				this._fTextFields_rctfv_arr[i].visible = false;
			}
		}

		this._fLines_gr.clear();
	}

	refillRuler( aStepTime_int, aCurRulerRightBorderTime_num )
	{
		this.clearRuler();

		let lStepSizeInPixels_num = TimeRulerView.RULER_WIDTH*aStepTime_int/aCurRulerRightBorderTime_num;
		let lStepIndex_int = 0;
		let lCurStepTime_int = aStepTime_int;
		this._fLines_gr.beginFill(0xe4e4e6, 1);
		while (lCurStepTime_int <= aCurRulerRightBorderTime_num)
		{
			let lCurStepPosX_num = lStepSizeInPixels_num*(lStepIndex_int+1);
		
			this._fLines_gr.drawRect(
				lCurStepPosX_num - 1,
				0,
				2,
				TimeRulerView.RULER_VISUAL_HEIGHT * 0.5);

			
			let l_rctfv = this.getTextFieldView(lStepIndex_int);

			l_rctfv.maxWidth = Math.trunc(lStepSizeInPixels_num*0.8);
			l_rctfv.text = Math.trunc(((lStepIndex_int+1) * aStepTime_int ) / 1000) + "s";
			l_rctfv.position.x = lCurStepPosX_num;
			l_rctfv.visible = true;
			
			lStepIndex_int++;
			lCurStepTime_int += aStepTime_int;
		}
		this._fLines_gr.endFill();
	}

	getCorrespondentCoordinateX(aMillisecondIndex_int)
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_gpv = APP.gameController.gameplayController.view;
		let lRoundInfo_ri = l_gpi.roundInfo;

		let lScaleX_num = aMillisecondIndex_int / this._dependencyAreaTimeValueOnRightMostEnd;

		if (l_gpi.isPreLaunchFlightRequired && lScaleX_num > 0 && lScaleX_num < 1)
		{
			lScaleX_num = MAnimation.getEasingMultiplier(MAnimation.EASE_OUT, lScaleX_num);
		}
		
		let lX_num = GraphView.DEPENDENCY_AREA_BORDER_LEFT_X + GraphView.DEPENDENCY_AREA_WIDTH * lScaleX_num;

		if (lX_num > GraphView.DEPENDENCY_AREA_BORDER_RIGHT_X)
		{
			lX_num = GraphView.DEPENDENCY_AREA_BORDER_RIGHT_X;
		}
		else if (lX_num < GraphView.DEPENDENCY_AREA_BORDER_LEFT_X)
		{
			lX_num = GraphView.DEPENDENCY_AREA_BORDER_LEFT_X;

			if (l_gpi.isPreLaunchFlightRequired)
			{
				lX_num = l_gpv.getStarshipLaunchX();

				let lFullPreLaunchDistanceX_num = GraphView.TURN_DISTANCE_X;
				let lPreLaunchTurnDistanceX_num = lFullPreLaunchDistanceX_num;
				let lPreLaunchFullTurnDuration_int = l_gpi.preLaunchFlightTurnDuration;
				let lRestPreLaunchDuration_int = Math.abs(aMillisecondIndex_int);

				if (lRestPreLaunchDuration_int < lPreLaunchFullTurnDuration_int)
				{
					let lPassedPreLaunchTurnDuration_num = lPreLaunchFullTurnDuration_int-lRestPreLaunchDuration_int;

					let lPreLaunchTurnDurationProgress_num = lPassedPreLaunchTurnDuration_num / lPreLaunchFullTurnDuration_int;
					lX_num += lPreLaunchTurnDistanceX_num * MAnimation.getEasingMultiplier(MAnimation.EASE_IN, lPreLaunchTurnDurationProgress_num);
				}
				else if (aMillisecondIndex_int >= 0)
				{
					lX_num += lFullPreLaunchDistanceX_num;
				}
			}
		}
		
		return lX_num;
	}

	getVisuallyMatchingMillisecondIndex(aX_num)
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lX_num = aX_num;

		if(lX_num < GraphView.DEPENDENCY_AREA_BORDER_LEFT_X)
		{
			lX_num = GraphView.DEPENDENCY_AREA_BORDER_LEFT_X;
		}

		let lScaleX_num = (lX_num - GraphView.DEPENDENCY_AREA_BORDER_LEFT_X)/ GraphView.DEPENDENCY_AREA_WIDTH;

		if (l_gpi.isPreLaunchFlightRequired && lScaleX_num > 0 && lScaleX_num < 1)
		{
			lScaleX_num = MAnimation.getEasingMultiplier(MAnimation.EASE_IN, lScaleX_num);
		}

		return Math.trunc(lScaleX_num * this._dependencyAreaTimeValueOnRightMostEnd);
	}

	get _dependencyAreaTimeValueOnRightMostEnd()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		
		let lTimeValue_num = this._fCurrentTimeValueOnRightMostEndOfRuler_num;

		if (APP.isBattlegroundGame){
			lTimeValue_num = (lTimeValue_num-7000)/2;
		}

		let lMinTimeValue_num = l_gpi.isPreLaunchFlightRequired ? 4000 : this.getInitialTimeValueOnRightMostEndOfRulerAccordingMockap();
		if (lTimeValue_num < lMinTimeValue_num)
		{
			lTimeValue_num = lMinTimeValue_num;
		}

		return lTimeValue_num;
	}
}

export default TimeRulerView;