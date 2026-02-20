import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import SecondaryScreenView from '../../../../view/uis/custom/secondary/SecondaryScreenView';
import SettingsScreenController from './settings/SettingsScreenController';
import PaytableScreenController from './paytable/PaytableScreenController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import CrashAPP from '../../../../CrashAPP';
import GameWebSocketInteractionController from '../../../interaction/server/GameWebSocketInteractionController';
import BottomPanelController from '../bottom_panel/BottomPanelController';
import DOMLayout from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/DOMLayout';

class SecondaryScreenController extends SimpleUIController
{
	static get EVENT_SCREEN_ACTIVATED()
	{
		return "onSecondaryScreenActivated";
	}

	static get EVENT_SCREEN_SHOWED()
	{
		return "onSecondaryScreenShowed";
	}

	static get EVENT_SCREEN_DEACTIVATED()
	{
		return "onSecondaryScreenDeactivated";
	}

	init()
	{
		super.init();
	}

	initView()
	{
		super._initView(new SecondaryScreenView());

		APP.gameScreenView.secondaryScreenContainer.addChild(this.view)
	}

	get settingsScreenController()
	{
		return this._settingsScreenController;
	}

	get paytableScreenController()
	{
		return this._paytableScreenController;
	}


	//INIT...
	constructor(...args)
	{
		super(...args);

		this._fSettingsScreenController_ssc = null;
		this._fPaytableScreenController_psc = null;
		this._fCurrentScreenController_ssc = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._settingsScreenController.init();
		this._paytableScreenController.init();

		this._addEventListeners();
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_ssv = this.view;

		this._settingsScreenController.initView(lView_ssv.settingsScreenView);
		this._settingsScreenController.hideScreen();

		this._paytableScreenController.hideScreen();
	}
	//...INIT

	_addEventListeners()
	{
		this._settingsScreenController.on(SettingsScreenController.EVENT_ON_OK_BUTTON_CLICKED, this._onSettingsScreenOkClicked, this);

		this._settingsScreenController.on(SettingsScreenController.EVENT_ON_CLOSE_BTN_CLICKED, this._onCloseButtonClicked, this);
		this._paytableScreenController.on(SettingsScreenController.EVENT_ON_CLOSE_BTN_CLICKED, this._onCloseButtonClicked, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		let bottomPanelController = APP.gameController.bottomPanelController;
		bottomPanelController.on(BottomPanelController.EVENT_SETTINGS_BUTTON_CLICKED, this._onSettingsButtonClicked, this);
		bottomPanelController.on(BottomPanelController.EVENT_INFO_BUTTON_CLICKED, this._onInfoButtonClicked, this);

		APP.layout.on(DOMLayout.EVENT_ON_ORIENTATION_CHANGED, this._onAppOrientationChange, this);

		// DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
		
	}

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 81) //q
	//  	{
	//  		this._showScreen(this._settingsScreenController);
	// 	}
	// }
	// ...DEBUG

	_onSettingsScreenOkClicked(event)
	{
		this._hideSceeen();
	}

	_onSettingsButtonClicked(event)
	{
		this._showScreen(this._settingsScreenController);
	}

	_onInfoButtonClicked(event)
	{
		this._showScreen(this._paytableScreenController);
	}

	_onCloseButtonClicked(event)
	{
		this._hideSceeen(true);
	}

	_onGameServerConnectionClosed(event)
	{
		if (event.wasClean)
		{
			return;
		}

		this._hideSceeen(true);
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN:
				this._hideSceeen(true);
				break;
		}
	}

	_showScreen(aScreenController_ssc)
	{
		if (!aScreenController_ssc || this._fCurrentScreenController_ssc === aScreenController_ssc)
		{
			return;
		}

		this._hideCurrentScreen();
		aScreenController_ssc.showScreen();
		this._fCurrentScreenController_ssc = aScreenController_ssc;

		let lView_ssv = this.view;
		if (lView_ssv)
		{
			if (!lView_ssv.visible)
			{
				lView_ssv.visible = true;
				this.emit(SecondaryScreenController.EVENT_SCREEN_ACTIVATED);
			}
		}
	}

	_hideSceeen(aOptCancelChangesOnScreen_bl)
	{
		this._hideCurrentScreen(aOptCancelChangesOnScreen_bl);

		let lView_ssv = this.view;
		if (lView_ssv && lView_ssv.visible)
		{
			lView_ssv.visible = false;
			this.emit(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED);
		}
	}

	_hideCurrentScreen(aOptCancelChangesOnScreen_bl = false)
	{
		if (this._fCurrentScreenController_ssc)
		{
			if (aOptCancelChangesOnScreen_bl && this._fCurrentScreenController_ssc.cancelChanges)
			{
				this._fCurrentScreenController_ssc.cancelChanges();
			}
			this._fCurrentScreenController_ssc.hideScreen();
			this._fCurrentScreenController_ssc = null;
		}
	}

	hideAllScreenButtons()
	{
		this.view.hideAllScreenButtons();
	}

	//SCREENS...
	get _settingsScreenController()
	{
		return this._fSettingsScreenController_ssc || (this._fSettingsScreenController_ssc = new SettingsScreenController());
	}

	get _paytableScreenController()
	{
		return this._fPaytableScreenController_psc || (this._fPaytableScreenController_psc = new PaytableScreenController());
	}
	//...SCREENS

	//ORIENTATION...
	_onAppOrientationChange(event)
	{
		this.view.updateArea();
	}
	//...ORIENTATION
}

export default SecondaryScreenController