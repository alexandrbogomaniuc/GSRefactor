import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameField from '../../../main/GameField';
import { ROUND_STATE } from '../../../model/state/GameStateInfo';
import { MIN_SHOT_Y, MAX_SHOT_Y } from '../../../config/Constants';


class CursorController extends SimpleController
{
	get isCursorRendering()
	{
		return this._cursorAnimation && this._cursorAnimation.isCursorRendering;
	}

	constructor(cursorAnimation)
	{
		super();

		this._gameScreen = null;
		this._gameField = null;
		this._fGameStateController_gsc = null;
		this._fGameStateInfo_gsi = null;
		this._cursorAnimation = cursorAnimation;
		
		this._fStageScale_num = 1;
		this._fCurrentCursorScale_num = 1;

		this._fIsOverRestrictedZone_bl = false;

		this._cursorId = 1;
		this._lastDate = 0;
	}

	setOverRestrictedZone(isOver)
	{
		this._fIsOverRestrictedZone_bl = isOver;
		this._validateCursor();
	}

	__init()
	{
		super.__init();

		this._gameScreen = APP.currentWindow;
		this._gameField = this._gameScreen.gameField;

		this._fGameStateController_gsc = this._gameScreen.gameStateController;
		this._fGameStateInfo_gsi = this._fGameStateController_gsc.info;

		this._gameField.on(GameField.EVENT_TIME_TO_VALIDATE_CURSOR, this._onTimeToValidateCursor, this);
		this._gameField.on(GameField.EVENT_ON_STOP_UPDATE_CURSOR_POSITION, this._onStopUpdatePosition, this);
		this._gameField.on(GameField.EVENT_ON_START_UPDATE_CURSOR_POSITION, this._onStartUpdatePosition, this);
		this._gameField.on(GameField.EVENT_ON_SET_SPECIFIC_CURSOR_POSITION, this._onSetSpecificPosition, this);

		APP.stage.on('resize', this._onStageResize, this);
		this._fStageScale_num = APP.stage.currentScale;
	}

	_onStopUpdatePosition()
	{
		this._cursorAnimation && this._cursorAnimation.stopUpdatePosition();
	}

	_onStartUpdatePosition()
	{
		this._cursorAnimation && this._cursorAnimation.startUpdatePosition();
	}

	_onSetSpecificPosition(e)
	{
		this._cursorAnimation && this._cursorAnimation.setSpecificPosition(e.pos);
	}

	_onStageResize(e)
	{
		this._fStageScale_num = e.scale;
		this._validateCursor();
	}

	_onTimeToValidateCursor()
	{
		this._validateCursor();
	}

	_validateCursor()
	{
		if (APP.isMobile) return;

		var renderer = APP.stage.renderer;

		if (	this._fGameStateInfo_gsi.gameState == ROUND_STATE.PLAY
				&& !this._gameField.lobbySecondaryScreenActive
				&& !this._gameField.roundResultActive
				&& !this._gameField.fireSettingsScreenActive
				&& this._gameField.interactive
				&& this._gameField.seatId > -1
				&& !this._fIsOverRestrictedZone_bl
				&& renderer.plugins.interaction.mouse.global.y > MIN_SHOT_Y
				&& renderer.plugins.interaction.mouse.global.y < MAX_SHOT_Y
			)
		{
			let lCursorScale_num = this._fStageScale_num;
			if (lCursorScale_num > 1) lCursorScale_num = 1;
			if (lCursorScale_num < 0.2) lCursorScale_num = 0.2;

			if (this._fCurrentCursorScale_num !== lCursorScale_num)
			{
				this._fCurrentCursorScale_num = lCursorScale_num;
				renderer.view.style.cursor = `url('assets/images/2/cursor/empty.png') 2 2, auto`;

				this._cursorAnimation.setScale(this._fCurrentCursorScale_num);
				this._cursorAnimation.startRender();
			}
		}
		else
		{
			this._fCurrentCursorScale_num = null;
			if	(this.isCursorRendering)
			{
				APP.stage.renderer.view.style.cursor = "inherit";
			}
			this._cursorAnimation.stopRender();
		}
	}
}

export default CursorController;