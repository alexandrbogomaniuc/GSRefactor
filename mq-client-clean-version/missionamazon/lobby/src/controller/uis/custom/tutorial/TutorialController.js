import SimpleUIController from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController";
import { APP } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import { FRAME_RATE } from "../../../../../../shared/src/CommonConstants";
import LobbyExternalCommunicator, { GAME_MESSAGES } from "../../../../external/LobbyExternalCommunicator";
import LobbyApp from "../../../../LobbyAPP";
import TutorialView from "../../../../view/uis/custom/tutorial/TutorialView";
import SecondaryScreenController from "../secondary/SecondaryScreenController";
import DialogController from "../dialogs/DialogController"

class TutorialController extends SimpleUIController
{
	static get EVENT_ON_TIME_TO_SHOW_TUTORIAL_LAYER()	{ return "EVENT_ON_TIME_TO_SHOW_TUTORIAL_LAYER" }
	static get EVENT_ON_TIME_TO_HIDE_TUTORIAL_LAYER()	{ return "EVENT_ON_TIME_TO_HIDE_TUTORIAL_LAYER" }
	static get AUTOHIDE_TIME() 							{ return 10*30*FRAME_RATE }

	static get EVENT_ON_TUTORIAL_NEEDED_STATE_CHANGED()	{ return "EVENT_ON_TUTORIAL_NEEDED_STATE_CHANGED"}

	constructor()
	{
		super(null, new TutorialView());
		this._fIsDoNotShowAgainChecked_bl = false;
		this._fHasTutorialBeenShown_bl = false;
		this._fBindSecScreenClosed_fnc = null;

		APP.externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onMessageReceived, this);
		APP.secondaryScreenController.on(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._checkIfShowTutorialAgainRequired, this);
	}

	get isTutorialDisplayed()
	{
		return this.view && this.view.visible;
	}

	get showAgain()
	{
		return !this._fIsDoNotShowAgainChecked_bl;
	}

	i_initViewOnStage(aStage_s)
	{
		this._fStage_s = aStage_s;
		this.view.i_init(aStage_s);
		this.view.on(TutorialView.DO_NOT_SHOW_AGAIN_BUTTON_CLICKED, this._onShowAgainClicked, this);
		this.view.on(TutorialView.VIEW_HIDDEN, this.i_hideTutorial, this);
	}

	initView(aView)
	{
		super.initView(aView);
		this.view && this.view.hide();
	}

	i_showTutorialIfNeeded(aObjectsData_obj)
	{
		if (APP.lobbyScreen.visible || !APP.layout.isGamesLayoutVisible)
		{
			return;
		}
		
		if (APP.playerController.info.toolTipEnabled && !this._fHasTutorialBeenShown_bl)
		{
			let lDialogActive_bl = APP.isAnyDialogActive;
			if (APP.isSecondaryScreenActive || lDialogActive_bl)
			{
				lDialogActive_bl && APP.dialogsController.on(DialogController.EVENT_DIALOG_DEACTIVATED, this.i_showTutorialIfNeeded.bind(this, aObjectsData_obj))
				return;
			}

			this.emit(TutorialController.EVENT_ON_TIME_TO_SHOW_TUTORIAL_LAYER);
			this.view && this.view.i_startAppearingAnimation(aObjectsData_obj) && this.view.show();
			this._fIsDoNotShowAgainChecked_bl = false;
			this._fHasTutorialBeenShown_bl = true;
		}
	}

	_checkIfShowTutorialAgainRequired(event)
	{
		if(event && event.tutorialShowAgainRequired !== undefined && event.tutorialShowAgainRequired !== null && this._fHasTutorialBeenShown_bl)
		{
			this._fHasTutorialBeenShown_bl = !event.tutorialShowAgainRequired;
		}
	}

	_onSecondaryScreenClosed(aData_obj)
	{
		this.i_showTutorialIfNeeded(aData_obj);
	}

	i_hideTutorial()
	{
		this.view.hide();
		this.emit(TutorialController.EVENT_ON_TIME_TO_HIDE_TUTORIAL_LAYER);
	}
	
	_onShowAgainClicked(e)
	{
		if (this._fIsDoNotShowAgainChecked_bl)
		{
			this._fIsDoNotShowAgainChecked_bl = false;
		}
		else
		{
			this._fIsDoNotShowAgainChecked_bl = true;
		}
	}

	_onMessageReceived(e)
	{
		switch (e.type)
		{
			case GAME_MESSAGES.TIME_TO_SHOW_TUTORIAL:
				if(this._fBindSecScreenClosed_fnc)
				{
					break;
				}
				this._fIsEventListenerIsActive = true;
				if(APP.playerController.info.toolTipEnabled)
				{
					this.i_showTutorialIfNeeded(e.data);
				}
				this._fBindSecScreenClosed_fnc = this._onSecondaryScreenClosed.bind(this, e.data);
				APP.secondaryScreenController.on(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._fBindSecScreenClosed_fnc, this);
				break;
			case GAME_MESSAGES.BACK_TO_LOBBY:
				this._fBindSecScreenClosed_fnc && APP.secondaryScreenController.off(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._fBindSecScreenClosed_fnc);
				this._fBindSecScreenClosed_fnc = null;
				this._fHasTutorialBeenShown_bl = false;
				break;
			case GAME_MESSAGES.ON_SIT_OUT_REQUIRED:
			case GAME_MESSAGES.ROUND_RESULT_ACTIVATED:
				if(!this._fBindSecScreenClosed_fnc)
				{
					break;
				}
				APP.secondaryScreenController.off(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._fBindSecScreenClosed_fnc);
				this._fBindSecScreenClosed_fnc = null;
				this.i_hideTutorial();
				break;
		}
	}

	destroy()
	{
		super.destroy();
		APP.secondaryScreenController.off(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._checkIfShowTutorialAgainRequired)
		APP.exterternalCommunicator.off(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onMessageReceived);
		this._fBindSecScreenClosed_fnc && APP.secondaryScreenController.off(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._fBindSecScreenClosed_fnc);
		this._fBindSecScreenClosed_fnc = null;
	}
}
export default TutorialController;