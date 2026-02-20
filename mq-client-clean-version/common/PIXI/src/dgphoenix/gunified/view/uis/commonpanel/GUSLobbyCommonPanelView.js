import SimpleUIView from '../../../../unified/view/base/SimpleUIView';
import GUSLobbyCPanelBalanceBlock from './blocks/GUSLobbyCPanelBalanceBlock';
import GUSLobbyCPanelTimeBlock from './blocks/GUSLobbyCPanelTimeBlock';
import GUSLobbyCPanelWinBlock from './blocks/GUSLobbyCPanelWinBlock';
import GUSLobbyCPanelInfoBlock from './blocks/GUSLobbyCPanelInfoBlock';
import { APP } from '../../../../unified/controller/main/globals';
import GUDialogsController from '../../../controller/uis/custom/dialogs/GUDialogsController';
import GUSLobbyStateController from '../../../controller/state/GUSLobbyStateController';
import GUSLobbyScreen from '../../main/GUSLobbyScreen';
import GUSLobbySelectableButton from './buttons/GUSLobbySelectableButton';
import GUSLobbyCommonPanelGroupMenuButton from './buttons/GUSLobbyCommonPanelGroupMenuButton';
import Timer from '../../../../unified/controller/time/Timer';
import GUSLobbySoundButtonView from '../GUSLobbySoundButtonView';
import GUSLobbyStateInfo from '../../../model/state/GUSLobbyStateInfo';
import Sprite from '../../../../unified/view/base/display/Sprite';
import I18 from '../../../../unified/controller/translations/I18';
import GUSDialogsInfo from '../../../model/uis/custom/dialogs/GUSDialogsInfo';

const BUTTONS_POSITIONS_DESKTOP = [
	{ x: -460, y: 0 },
	{ x: -426, y: 0 },
	{ x: 175, y: 0 },
	{ x: 251, y: 0 },
	{ x: 279, y: 0 },
	{ x: 316, y: 0 },
	{ x: 353, y: 0 },
	{ x: 390, y: 0 },
	{ x: 427, y: 0 },
	{ x: 464, y: 0 }
];

const BUTTONS_POSITIONS_MOBILE = [
	{ x: -458, y: 0 },
	{ x: 0, y: 206 },
	{ x: 296, y: 0 },
	{ x: 0, y: -94 - 60 },
	{ x: 0, y: -34 },
	{ x: 0, y: -94 - 60 },
	{ x: 0, y: 26 },
	{ x: 0, y: 146 },
	{ x: 0, y: 86 },
	{ x: 0, y: -153 - 60 },
	{ x: 414, y: 0 },
	{ x: 0, y: -94 }
];

class GUSLobbyCommonPanelView extends SimpleUIView
{
	static get EVENT_FIRE_SETTINGS_BUTTON_CLICKED() { return "onFireSettingsButtonClicked"; }
	static get EVENT_HISTORY_BUTTON_CLICKED() 		{ return "onHistoryButtonClicked"; }
	static get EVENT_INFO_BUTTON_CLICKED() 			{ return "onInfoButtonClicked"; }
	static get EVENT_HOME_BUTTON_CLICKED() 			{ return "onHomeButtonClicked"; }
	static get EVENT_EDIT_PROFILE_BUTTON_CLICKED() 	{ return "onEditProfileButtonClicked"; }
	static get EVENT_SETTINGS_BUTTON_CLICKED() 		{ return "onSettingsButtonClicked"; }
	static get EVENT_BACK_TO_LOBBY_BUTTON_CLICKED() { return "onBackToLobbyButtonClicked"; }
	static get EVENT_GROUP_BUTTONS_CHANGED() 		{ return "onGroupButtonsChanged"; }
	static get EVENT_COUNT_GROUP_BUTTONS_CHANGED() 	{ return "onCountGroupButtonsChanged"; }
	static get EVENT_REFRESH_BALANCE_REQUEST() 		{ return GUSLobbyCPanelBalanceBlock.EVENT_REFRESH_BALANCE_REQUEST; }
	static get EVENT_TIME_SYNC_REQUEST() 			{ return GUSLobbyCPanelTimeBlock.EVENT_TIME_SYNC_REQUEST; }
	static get EVENT_SOUND_ON_BUTTON_CLICKED() 		{ return GUSLobbySoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED; }
	static get EVENT_SOUND_OFF_BUTTON_CLICKED() 	{ return GUSLobbySoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED; }

	static get LAYER_ID_BASE() { return 0; }
	static get LAYER_ID_MOBILE_BUTTONS() { return 1; }
	static get LAYER_ID_FIRE_SETTINGS() { return 2; }

	static get COMMON_PANEL_SIZE_DESKTOP() { return { width: 960, height: 30 }; }
	static get COMMON_PANEL_SIZE_MOBILE() { return { width: 960, height: 36 }; }

	static get COMMON_PANEL_LAYERS()
	{
		return [
			{ id: GUSLobbyCommonPanelView.LAYER_ID_BASE, pointerEvents: 'auto', width: 960, height: 30, left: 0, top: 20 }
		];
	}

	static get MOBILE_COMMON_PANEL_LAYERS()
	{
		return [
			{ id: GUSLobbyCommonPanelView.LAYER_ID_BASE, pointerEvents: 'auto', width: 960, height: 36, left: 0, top: 1 },
			{ id: GUSLobbyCommonPanelView.LAYER_ID_MOBILE_BUTTONS, pointerEvents: 'auto', width: 86, height: 240, left: 874, top: -230 }
		];
	}

	init(aPanelStages_st_arr)
	{
		this._initCommonPanelView(aPanelStages_st_arr);
	}

	updateUI()
	{
		this._updateUI();
	}

	applyFrbGameLoadingView()
	{
		this._fGroupButton_btn && this._fGroupButton_btn.hide();
	}

	cancelFrbGameLoadingView()
	{
		this._fGroupButton_btn && this._fGroupButton_btn.show();
	}

	onTimeSyncResponseReceived()
	{
		if (this._fTimeBlock_tb)
		{
			this._fTimeBlock_tb.time = this.uiInfo.timeFromServer;
			this._fTimeBlock_tb.sync();
		}
	}

	initBalanceRefreshTimer()
	{
		if (this._fBalanceBlock_bb)
		{
			this._fBalanceBlock_bb.initBalanceRefreshTimer();
		}
	}

	onFireSettingsStateChanged(aIsActive_bln)
	{
		this._onFireSettingsStateChanged(aIsActive_bln);
	}

	get soundButtonView()
	{
		return this._soundButtonView;
	}

	get infoButtonContainer()
	{
		return this._fInfoButton_btn;
	}

	get isFireSettingsButtonActivated()
	{
		return this._fFireSettingsButtonActivated_bln;
	}

	constructor()
	{
		super();

		this._fStageBase_stg = null;
		this._fStageButtons_stg = null;

		this._fBalanceBlock_bb = null;
		this._fWinBlock_wb = null;
		this._fTimeBlock_tb = null;
		this._fInfoBlock_ib = null;

		this._fMainButtons_arr = null;

		this._fButtonsGroupContainer_sprt = null;
		this._fSoundButtonContainer_lsbv = null;
		this._fHistoryButton_btn = null;
		
		this._fSoundButtonView_sbv = null;

		this._fGroupButton_btn = null;
		this._fBackToLobbyButton_btn = null;
		this._fInfoButton_btn = null;
		this._fSettingsButton_btn = null;
		this._fButtonsGroupBack_grphc = null;
		this._fButtonsGroupContainerShift_num = null;

		this._fFireSettingsButtonActivated_bln = null;

		this._fGroupHidden_bln = null;
		this._fGroupAnimationInProgress_bln = null;

		this._fHideGroupTimer_tmr = null;

		this._fSeptumsContainer_sprt = null;
		this._fSeptums_arr = null;
		this._fOtherBtnClick_bln = null;
		this._fLobbyCaption_cta = null;

		this._fTournamentModeInfo_tmi = null;
		this._fPlayerInfo_lpi = null;
	}

	__provideTimeBlockInstance()
	{
		return new GUSLobbyCPanelTimeBlock(this.uiInfo.timeFromServer, this.uiInfo.timerFrequency);
	}

	__provideBalanceBlockInstance()
	{
		return new GUSLobbyCPanelBalanceBlock(this.uiInfo.gameIndicatorsUpdateTime);
	}

	__provideWinBlockInstance()
	{
		return new GUSLobbyCPanelWinBlock(this.uiInfo.gameIndicatorsUpdateTime);
	}

	__provideInfoBlockInstance()
	{
		return new GUSLobbyCPanelInfoBlock();
	}

	get __homeBtnAssetName()
	{
		//must be overridden
		return undefined;
	}

	get __historyBtnAssetName()
	{
		//must be overridden
		return undefined;
	}

	get __fireSettingsBtnAssetName()
	{
		//must be overridden
		return undefined;
	}

	get __infoBtnAssetName()
	{
		//must be overridden
		return undefined;
	}

	get __settingsBtnAssetName()
	{
		//must be overridden
		return undefined;
	}

	get __backToLobbyBtnAssetName()
	{
		//must be overridden
		return undefined;
	}

	get __groupBtnAssetName()
	{
		//must be overridden
		return undefined;
	}

	get __groupMenuCaptionTAssetName()
	{
		//must be overridden
		return undefined;
	}

	__provideGroupMenuBtnInstance()
	{
		return new GUSLobbyCommonPanelGroupMenuButton(this.__groupBtnAssetName, this.__groupMenuCaptionTAssetName, true);
	}

	_initCommonPanelView(aPanelStages_st_arr)
	{
		this._fTournamentModeInfo_tmi = APP.tournamentModeController.info;

		this._fOtherBtnClick_bln = true;
		this._fMainButtons_arr = [];

		// PANEL_DEBUG switch off elements
		// this.uiInfo.historyCallback = undefined;
		// this.uiInfo.homeCallback = undefined;
		// this.uiInfo.timerFrequency = undefined;

		this._fStageBase_stg = aPanelStages_st_arr[GUSLobbyCommonPanelView.LAYER_ID_BASE];
		if (!this._fStageBase_stg) return;

		let lStageView_sprt = this._fStageBase_stg.view;

		let lPanelBG_grphc = lStageView_sprt.addChild(new PIXI.Graphics());

		let lPanelHeight_num = APP.isMobile ? 36 : 30;

		lPanelBG_grphc.beginFill(0x000000).drawRect(-480, -lPanelHeight_num / 2, 960, lPanelHeight_num).endFill();

		//TIME...
		if (this.uiInfo.timeIndicatorEnabled)
		{
			this._fTimeBlock_tb = lStageView_sprt.addChild(this.__provideTimeBlockInstance());
			this._fTimeBlock_tb.on(GUSLobbyCPanelTimeBlock.EVENT_TIME_SYNC_REQUEST, this.emit, this);
		}
		//...TIME

		//BALANCE...
		this._fBalanceBlock_bb = lStageView_sprt.addChild(this.__provideBalanceBlockInstance());
		this._fBalanceBlock_bb.on(GUSLobbyCPanelBalanceBlock.EVENT_REFRESH_BALANCE_REQUEST, this.emit, this);
		//...BALANCE

		//WIN...
		this._fWinBlock_wb = lStageView_sprt.addChild(this.__provideWinBlockInstance());
		this._fWinBlock_wb.visible = false;
		//...WIN

		//INFO...
		this._fInfoBlock_ib = lStageView_sprt.addChild(this.__provideInfoBlockInstance());
		this._fInfoBlock_ib.visible = false;
		//...INFO

		//BUTTONS...
		if (APP.isMobile)
		{
			this._fStageButtons_stg = aPanelStages_st_arr[GUSLobbyCommonPanelView.LAYER_ID_MOBILE_BUTTONS];
			this._fStageButtons_stg.view.position.y = 0;

			this._fButtonsGroupContainer_sprt = this._fStageButtons_stg.view.addChild(new Sprite());
			this._fButtonsGroupContainer_sprt.visible = false;

			this._initButtonsGroupBack();
		}
		else
		{
			this._fButtonsGroupContainer_sprt = lStageView_sprt.addChild(new Sprite());
		}

		if (this.uiInfo.homeButtonEnabled)
		{
			this._initHomeButton(lStageView_sprt);
		}
		if (this.uiInfo.historyButtonEnabled)
		{
			this._initHistoryButton(APP.isMobile ? this._fButtonsGroupContainer_sprt : lStageView_sprt);
		}
		this._initFireSettingsButton(this._fButtonsGroupContainer_sprt);
		this._initInfoButton(this._fButtonsGroupContainer_sprt);
		this._initSettingsButton(this._fButtonsGroupContainer_sprt);
		this._initSoundButton(this._fButtonsGroupContainer_sprt);
		this._initBackToLobbyButton(this._fButtonsGroupContainer_sprt);

		if (!APP.isMobile)
		{
			this._fMainButtons_arr.push(this._fFireSettingsButton_btn);
			this._fMainButtons_arr.push(this._fInfoButton_btn);
			this._fMainButtons_arr.push(this._fSettingsButton_btn);
			this._fMainButtons_arr.push(this._fSoundButtonContainer_lsbv);
		}
		else
		{
			this._initGroupButton(lStageView_sprt);
		}
		//...BUTTONS

		//CAPTIONS...
		this._fLobbyCaption_cta = lStageView_sprt.addChild(I18.generateNewCTranslatableAsset(this.__commonPanelLobbyCaption));
		this._fLobbyCaption_cta.visible = false;
		//...CAPTIONS

		this._formatBlocks();
		this._subscribeHandlers();

		this._updateUI();
	}

	get __commonPanelLobbyCaption()
	{
		return "TACommonPanelLobbyCaption";
	}

	_formatBlocks()
	{
		if (APP.isMobile)
		{
			if (this._fTimeBlock_tb) this._fTimeBlock_tb.position.set(-420, 0);
			this._fBalanceBlock_bb.position.set(-364, 0);
			this._fWinBlock_wb.position.set(-224, 0);
			this._fInfoBlock_ib.position.set(20, 0);

			if (!this._fHomeButton_btn && !this._fTimeBlock_tb)
			{
				this._fBalanceBlock_bb.position.set(-450, 0);
				this._fBalanceBlock_bb.hideSeptum();
				this._fWinBlock_wb.position.set(-306, 0);
			}
			else if (!this._fTimeBlock_tb)
			{
				this._fBalanceBlock_bb.position.set(-402, 0);
				this._fWinBlock_wb.position.set(-260, 0);
			}
			else if (!this._fHomeButton_btn)
			{
				this._fTimeBlock_tb.position.set(-451, 0);
				this._fBalanceBlock_bb.position.set(-394, 0);
				this._fWinBlock_wb.position.set(-253, 0);
			}
		}
		else
		{
			if (this._fTimeBlock_tb) this._fTimeBlock_tb.position.set(-366, 0);
			this._fBalanceBlock_bb.position.set(-315, 0);
			this._fWinBlock_wb.position.set(-166, 0);
			this._fInfoBlock_ib.position.set(30, 0);

			if (!this._fHomeButton_btn && !this._fHistoryButton_btn)
			{
				if (this._fTimeBlock_tb)
				{
					this._fTimeBlock_tb.hideSeptum();

					this._fTimeBlock_tb.position.set(-432, 0);
					this._fBalanceBlock_bb.position.set(-381, 0);
					this._fWinBlock_wb.position.set(-232, 0);
				}
				else
				{
					this._fBalanceBlock_bb.hideSeptum();

					this._fBalanceBlock_bb.position.set(-450, 0);
					this._fWinBlock_wb.position.set(-301, 0);
				}
			}
			else if (!this._fHomeButton_btn)
			{
				this._fHistoryButton_btn.hideSeptum();

				if (this._fTimeBlock_tb)
				{
					this._fTimeBlock_tb.position.set(-399, 0);
					this._fBalanceBlock_bb.position.set(-348, 0);
					this._fWinBlock_wb.position.set(-199, 0);
					this._fHistoryButton_btn.position.set(this._btnsPos[0].x, this._btnsPos[0].y);
				}
				else
				{
					this._fBalanceBlock_bb.position.set(-420, 0);
					this._fWinBlock_wb.position.set(-272, 0);
					this._fHistoryButton_btn.position.set(this._btnsPos[0].x, this._btnsPos[0].y);
				}
			}
			else if (!this._fHistoryButton_btn)
			{
				this._fHomeButton_btn.hideSeptum();

				if (this._fTimeBlock_tb)
				{
					this._fTimeBlock_tb.position.set(-399, 0);
					this._fBalanceBlock_bb.position.set(-348, 0);
					this._fWinBlock_wb.position.set(-199, 0);
					this._fHomeButton_btn.position.set(this._btnsPos[0].x, this._btnsPos[0].y);
				}
				else
				{
					this._fBalanceBlock_bb.position.set(-420, 0);
					this._fWinBlock_wb.position.set(-272, 0);
					this._fHomeButton_btn.position.set(this._btnsPos[0].x, this._btnsPos[0].y);
				}
			}
			else if (!this._fTimeBlock_tb)
			{
				this._fHomeButton_btn.hideSeptum();

				this._fBalanceBlock_bb.position.set(-384, 0);
				this._fWinBlock_wb.position.set(-244, 0);
			}
			else
			{
				this._fHomeButton_btn.hideSeptum();
			}
		}
	}

	get _lobbyVisible()
	{
		return APP.lobbyStateController.info.lobbyScreenVisible;
	}

	get _secondaryScreenState()
	{
		return APP.lobbyStateController.info.secondaryScreenState;
	}

	_subscribeHandlers()
	{
		APP.dialogsController.on(GUDialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		APP.dialogsController.on(GUDialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);

		APP.lobbyStateController.on(GUSLobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyScreenVisibilityChanged, this);
		APP.lobbyStateController.on(GUSLobbyStateController.EVENT_ON_SECONDARY_SCREEN_STATE_CHANGE, this._onSecondaryScreenStateChanged, this);

		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_LAST_ROOM_RESITOUT_COMPLETED, this._onLastRoomResitoutCompleted, this);

		if (!APP.isMobile)
		{
			window.addEventListener(this._mouseClickEventName, this._onLobbyClicked.bind(this));
		}
	}

	get _mouseClickEventName()
	{
		if (document.PointerEvent)
		{
			return "pointerup";
		}
		else
		{
			if (APP.isMobile)
			{
				return "touchend";
			}
			else
			{
				return "mouseup";
			}
		}
	}

	_unsubscribeHandlers()
	{
		APP.dialogsController.off(GUDialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		APP.dialogsController.off(GUDialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);

		APP.lobbyStateController.off(GUSLobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyScreenVisibilityChanged, this);
		APP.lobbyStateController.off(GUSLobbyStateController.EVENT_ON_SECONDARY_SCREEN_STATE_CHANGE, this._onSecondaryScreenStateChanged, this);

		APP.lobbyScreen.off(GUSLobbyScreen.EVENT_ON_LAST_ROOM_RESITOUT_COMPLETED, this._onLastRoomResitoutCompleted, this);
	}

	get _btnsPos()
	{
		return APP.isMobile ? BUTTONS_POSITIONS_MOBILE : BUTTONS_POSITIONS_DESKTOP;
	}

	_initButtonsGroupBack()
	{
		this._fButtonsGroupContainerShift_num = this.uiInfo.historyButtonEnabled ? 0 : 60;
		this._fButtonsGroupContainer_sprt.position.y = this._fButtonsGroupContainerShift_num;

		let lMaxButtons_num = 6;
		let lBlockHeight_num = 60;
		let lHeight_num = lBlockHeight_num * lMaxButtons_num;
		let lY_num = -lHeight_num / 2;

		this._fButtonsGroupBack_grphc = this._fButtonsGroupContainer_sprt.addChild(new PIXI.Graphics());
		this._fButtonsGroupBack_grphc.beginFill(0x000000, 0.88).drawRect(-43, lY_num, 86, lHeight_num).endFill();

		let lStep_num = lY_num;
		for (let i = 0; i < lMaxButtons_num; ++i)
		{
			let lLine_grphc = this._fButtonsGroupBack_grphc.addChild(new PIXI.Graphics());
			lLine_grphc.beginFill(0x4e4e4e).drawRect(-43, -1, 86, 1).endFill();
			lLine_grphc.position.set(0, lStep_num);

			lStep_num += lBlockHeight_num;
		}

		this._fGroupHidden_bln = true;
	}

	//BUTTONS_INIT...
	_initHomeButton(aContainer_sprt)
	{
		let lSeptum_bln = APP.isMobile ? false : true;
		let lScale_num = APP.isMobile ? 1.4 : 1;

		let lButton_btn = aContainer_sprt.addChild(new GUSLobbySelectableButton(this.__homeBtnAssetName, null, lSeptum_bln, lScale_num));
		lButton_btn.position.set(this._btnsPos[0].x, this._btnsPos[0].y);
		lButton_btn.on("pointerclick", this._onHomeButtonClicked, this);

		this._fHomeButton_btn = lButton_btn;
	}

	_initHistoryButton(aContainer_sprt)
	{
		let lSeptum_bln = APP.isMobile ? false : true;
		let lScale_num = APP.isMobile ? 1.6 : 1;

		let lButton_btn = aContainer_sprt.addChild(new GUSLobbySelectableButton(this.__historyBtnAssetName, null, lSeptum_bln, lScale_num));
		lButton_btn.position.set(this._btnsPos[1].x, this._btnsPos[1].y);
		lButton_btn.on("pointerclick", this._onHistoryButtonClicked, this);

		this._fHistoryButton_btn = lButton_btn;
	}

	_initFireSettingsButton(aContainer_sprt)
	{
		let lSeptum_bln = APP.isMobile ? false : true;
		let lScale_num = APP.isMobile ? 1.6 : 1;

		let lButton_btn = aContainer_sprt.addChild(new GUSLobbySelectableButton(this.__fireSettingsBtnAssetName, null, lSeptum_bln, lScale_num));
		lButton_btn.position.set(this._btnsPos[3].x, this._btnsPos[3].y);
		lButton_btn.on("pointerclick", this._onFireSettingsBtnClicked, this);

		this._fFireSettingsButtonActivated_bln = false;
		this._fFireSettingsButton_btn = lButton_btn;
	}

	_initInfoButton(aContainer_sprt)
	{
		let lSeptum_bln = APP.isMobile ? false : true;
		let lScale_num = APP.isMobile ? 1.6 : 1;

		let lButton_btn = aContainer_sprt.addChild(new GUSLobbySelectableButton(this.__infoBtnAssetName, null, lSeptum_bln, lScale_num));
		lButton_btn.position.set(this._btnsPos[6].x, this._btnsPos[6].y);
		lButton_btn.on("pointerclick", this._onInfoButtonClicked, this);

		this._fInfoButton_btn = lButton_btn;
	}

	_initSettingsButton(aContainer_sprt)
	{
		let lSeptum_bln = APP.isMobile ? false : true;
		let lScale_num = APP.isMobile ? 1.6 : 1;

		let lButton_btn = aContainer_sprt.addChild(new GUSLobbySelectableButton(this.__settingsBtnAssetName, null, lSeptum_bln, lScale_num));
		lButton_btn.position.set(this._btnsPos[7].x, this._btnsPos[7].y);
		lButton_btn.on("pointerclick", this._onSettingsButtonClicked, this);

		this._fSettingsButton_btn = lButton_btn;
	}

	_initSoundButton(aContainer_sprt)
	{
		let lButtonContainer_btn = aContainer_sprt.addChild(this._soundButtonView);
		lButtonContainer_btn.position.set(this._btnsPos[8].x, this._btnsPos[8].y);
		lButtonContainer_btn.on(GUSLobbySoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED, this._hideGroupAfterButtonClick, this);
		lButtonContainer_btn.on(GUSLobbySoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED, this._hideGroupAfterButtonClick, this);
		this._fSoundButtonContainer_lsbv = lButtonContainer_btn;
	}

	_initBackToLobbyButton(aContainer_sprt)
	{
		let lSeptum_bln = APP.isMobile ? false : true;
		let lScale_num = APP.isMobile ? 1.6 : 1;

		let lButton_btn = aContainer_sprt.addChild(new GUSLobbySelectableButton(this.__backToLobbyBtnAssetName, null, lSeptum_bln, lScale_num, true));
		lButton_btn.position.set(this._btnsPos[9].x, this._btnsPos[9].y);
		lButton_btn.on("pointerclick", this._onBackToLobbyButtonClicked, this);

		this._fBackToLobbyButton_btn = lButton_btn;
	}

	_initGroupButton(aContainer_sprt)
	{
		let lButton_btn = aContainer_sprt.addChild(this.__provideGroupMenuBtnInstance());
		lButton_btn.position.set(this._btnsPos[10].x, this._btnsPos[10].y);
		lButton_btn.on("pointerclick", this._onGroupButtonClicked, this);

		APP.layout.hideMobileCommonGroupButtonsLayer();

		this._fGroupButton_btn = lButton_btn;
	}
	//...BUTTONS_INIT

	//SOUND_BUTTON...
	get _soundButtonView()
	{
		let lDrawSeptum_bln = APP.isMobile ? false : true;

		return this._fSoundButtonView_sbv || (this._fSoundButtonView_sbv = this.__provideLobbySoundButtonViewInstance(lDrawSeptum_bln));
	}

	__provideLobbySoundButtonViewInstance(aDrawSeptum_bln)
	{
		return new GUSLobbySoundButtonView(aDrawSeptum_bln);
	}
	//...SOUND_BUTTON
	//...INIT

	//MOBILE_BUTTONS_GROUP...
	_restartHideGroupTimer()
	{
		if (!APP.isMobile) return;

		this._resetHideGroupTimer();

		if (this._fButtonsGroupContainer_sprt.visible)
		{
			this._fHideGroupTimer_tmr = new Timer(this._onHideTimerCompleted.bind(this), 5000, false);
			this._fHideGroupTimer_tmr.start();
		}
	}

	_onHideTimerCompleted()
	{
		this._resetHideGroupTimer();

		if (this._fButtonsGroupContainer_sprt.visible)
		{
			this._changeGroupState();
		}
	}

	_resetHideGroupTimer()
	{
		this._fHideGroupTimer_tmr && this._fHideGroupTimer_tmr.destructor();
		this._fHideGroupTimer_tmr = null;
	}

	_changeGroupState()
	{
		if (this._fGroupAnimationInProgress_bln) return;

		this._fButtonsGroupContainer_sprt.removeTweens();

		let lHiddenPos_num = 20 + this._visibleButtons * 60;

		if (this._fGroupHidden_bln)
		{
			this._fButtonsGroupContainer_sprt.visible = true;
			this._fButtonsGroupContainer_sprt.position.y = lHiddenPos_num;

			this.emit(GUSLobbyCommonPanelView.EVENT_GROUP_BUTTONS_CHANGED, { visible: true });

			this._fGroupAnimationInProgress_bln = true;
			this._fButtonsGroupContainer_sprt.addTween('y', this._fButtonsGroupContainerShift_num, 550, null, this._onGroupShowed.bind(this), null).play();

			this._fGroupButton_btn.onGroupOpening();
		}
		else
		{
			this._fButtonsGroupContainer_sprt.position.y = this._fButtonsGroupContainerShift_num;

			this._fGroupAnimationInProgress_bln = true;
			this._fButtonsGroupContainer_sprt.addTween('y', lHiddenPos_num, 550, null, this._onGroupHided.bind(this), null).play();

			this._fGroupButton_btn.onGroupHiding();
		}
	}

	get _visibleButtons()
	{
		let lLen_num = 6;
		if (this._lobbyVisible)
		{
			--lLen_num;
			--lLen_num;
		}

		if (!this._fHistoryButton_btn)
		{
			--lLen_num;
		}

		if (APP.isBattlegroundGame) --lLen_num;

		return lLen_num;
	}

	_onGroupShowed()
	{
		this._fGroupHidden_bln = false;
		this._fGroupAnimationInProgress_bln = false;

		this._restartHideGroupTimer();
	}

	_onGroupHided()
	{
		this._resetHideGroupTimer();

		this._fButtonsGroupContainer_sprt.visible = false;
		this._fGroupHidden_bln = true;
		this._fGroupAnimationInProgress_bln = false;

		this.emit(GUSLobbyCommonPanelView.EVENT_GROUP_BUTTONS_CHANGED, { visible: false });
	}
	//...MOBILE_BUTTONS_GROUP

	//UI_UPDATES...
	_updateUI()
	{
		this._fStageBase_stg.view.visible = true;

		if (
				!APP.isBattlegroundRoomMode
				&& APP.battlegroundController.info.isBattlegroundGameStarted
			)
		{
			this._fStageBase_stg.view.visible = false;
		}

		if (!APP.isBattlegroundRoomMode)
		{
			this._fBalanceBlock_bb.updateBalanceIfRequired(this.uiInfo.gameBalance);
		}
		
		if (this.uiInfo.gameUIVisible && !this.uiInfo.isWebGlContextLost && !this._fTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			this._fInfoBlock_ib.visible = this.uiInfo.playerSatIn;
			this._fWinBlock_wb.visible = this.uiInfo.playerSatIn;
			this._fBalanceBlock_bb.visible = this.uiInfo.playerSatIn && !this.uiInfo.isDialogActive;
			this._fLobbyCaption_cta.visible = false;

			if (this.uiInfo.playerSatIn)
			{
				this._fWinBlock_wb.updateWinIfRequired(this.uiInfo.gameWin);
			}
		}
		else
		{
			this._fWinBlock_wb.resetCounting();
			this._fBalanceBlock_bb.resetCounting();

			this._fInfoBlock_ib.visible = false;
			this._fInfoBlock_ib.hideFreeLabel();
			this._fWinBlock_wb.visible = false;
			this._fBalanceBlock_bb.visible = this._lobbyVisible && !this.uiInfo.isDialogActive && !APP.lobbyScreen.isRoomLasthand && !this.uiInfo.isWebGlContextLost && !APP.FRBController.info.isActivated;
			this._fLobbyCaption_cta.visible = !this.uiInfo.isWebGlContextLost && !this._fTournamentModeInfo_tmi.isTournamentCompletedOrFailedState;
		}

		//BATTLEGROUND...
		if (APP.isBattlegroundGame)
		{
			this._fWinBlock_wb.visible = false; //no win block in battleground mode

			//show balance only on re-buy dialog
			this._fBalanceBlock_bb.visible = !!APP.dialogsController.battlegroundBuyInConfirmationDialogController.info.isActive;
		}
		//...BATTLEGROUND

		this._validateButtons();
		this._updateButtonsPositions();
	}

	_validateButtons()
	{
		if (!this.uiInfo.gameUIVisible)
		{
			this._fFireSettingsButton_btn.visible = false;
			this._fBackToLobbyButton_btn.visible = false;
		}
		else
		{
			this._fFireSettingsButton_btn.visible = !APP.isBattlegroundGame;
			this._fBackToLobbyButton_btn.visible = true;
		}

		this._fBackToLobbyButton_btn.enabled = !APP.FRBController.info.isActivated
												&& !(APP.lobbyBonusController.info.isActivated && APP.lobbyBonusController.info.isCompletionInProgress)
												&& !APP.isBattlegroundGame;

		this._fSettingsButton_btn.enabled = true;
		this._fInfoButton_btn.enabled = true;
		this._fFireSettingsButton_btn.enabled = !!this._fFireSettingsButton_btn.visible;

		this._fFireSettingsButton_btn.activated = this._fFireSettingsButtonActivated_bln;
		this._fSettingsButton_btn.activated = false;
		this._fInfoButton_btn.activated = false;

		switch (this._secondaryScreenState)
		{
			case GUSLobbyStateInfo.SCREEN_SETTINGS: this._fSettingsButton_btn.activated = true; break;
			case GUSLobbyStateInfo.SCREEN_PAYTABLE: this._fInfoButton_btn.activated = true; break;
		}
	}

	get _playerInfo()
	{
		return this._fPlayerInfo_lpi || (this._fPlayerInfo_lpi = APP.playerController.info);
	}

	_updateButtonsPositions()
	{
		if (!APP.isMobile)
		{
			let lShift_int = !this._fBackToLobbyButton_btn.visible ? 2 : 1;

			for (let i = 0; i < this._fMainButtons_arr.length; ++i)
			{
				this._fMainButtons_arr[i].position.set(this._btnsPos[i + 4 + lShift_int].x, this._btnsPos[i + 4 + lShift_int].y);
			}

			this._fFireSettingsButton_btn.visible && this._fFireSettingsButton_btn.position.set(this._btnsPos[5].x, this._btnsPos[5].y);
		}
		else
		{
			this.emit(GUSLobbyCommonPanelView.EVENT_COUNT_GROUP_BUTTONS_CHANGED, { buttonsAmount: this._visibleButtons });
			this._fButtonsGroupBack_grphc.position.y = 60;
			this._fStageButtons_stg.view.position.y = 0;

			if (APP.isBattlegroundGame)
			{
				this._fButtonsGroupBack_grphc.position.y += 60; // because Fire Settings Button is not visible
			}

			if (this._fFireSettingsButton_btn && this._fFireSettingsButton_btn.visible)
			{
				this._fFireSettingsButton_btn.position.set(this._btnsPos[3].x, this._btnsPos[3].y);
				this._fFireSettingsButton_btn.y += 60;
				this._fFireSettingsButton_btn.y += 60;
				this._fStageButtons_stg.view.position.y += 60;
			}

			if (this._fBackToLobbyButton_btn && this._fBackToLobbyButton_btn.visible)
			{
				this._fBackToLobbyButton_btn.position.set(this._btnsPos[9].x, this._btnsPos[9].y);
				this._fBackToLobbyButton_btn.y += 60;
				this._fBackToLobbyButton_btn.y += 60;
				this._fStageButtons_stg.view.position.y += 60;

				if (APP.isBattlegroundGame)
				{
					this._fBackToLobbyButton_btn.y += 60;
				}
			}

			if (this._lobbyVisible)
			{
				this._fButtonsGroupBack_grphc.position.y = 180;
			}
		}
	}
	//...UI_UPDATES

	//HANDLERS...
	_onSecondaryScreenStateChanged()
	{
		this._updateUI();
	}

	_onDialogActivated()
	{
		if (this.uiInfo.gameUIVisible)
		{
			this._updateUI();
		}

		let lIsGroupMenuVisible_bl = APP.isBattlegroundGame
										? (
											APP.dialogsController.battlegroundBuyInConfirmationDialogController.info.isActive
											|| APP.dialogsController.lobbyBattlegroundNotEnoughPlayersDialogController.info.isActive
											|| APP.dialogsController.gameGameBattlegroundNoWeaponsFiredDialogController.info.isActive
											)
										: false;

		this._fButtonsGroupContainer_sprt.visible = lIsGroupMenuVisible_bl;
		if (APP.isMobile)
		{
			this._fGroupButton_btn.visible = lIsGroupMenuVisible_bl;
		}
	}

	_onDialogDeactivated()
	{
		if (this.uiInfo.gameUIVisible)
		{
			this._updateUI();
		}

		if (!APP.dialogsController.info.hasActiveDialog)
		{
			this._fButtonsGroupContainer_sprt.visible = true;
			if (APP.isMobile)
			{
				this._fGroupButton_btn.visible = true;
			}
		}
	}

	_onLobbyScreenVisibilityChanged(event)
	{
		this._updateUI();
	}

	_onLastRoomResitoutCompleted(event)
	{
		this.initBalanceRefreshTimer();
		this._updateUI();
	}

	_hideGroupAfterButtonClick()
	{
		if (APP.isMobile && this._fButtonsGroupContainer_sprt.visible)
		{
			this._changeGroupState();
		}
	}

	//BUTTONS_HANDLERS...
	_onGroupButtonClicked(event)
	{
		this._changeGroupState();
	}

	_onSettingsButtonClicked(event)
	{
		this._hideGroupAfterButtonClick();
		this._fOtherBtnClick_bln = true;
		this._restartHideGroupTimer();
		this.emit(GUSLobbyCommonPanelView.EVENT_SETTINGS_BUTTON_CLICKED);
	}

	_onBackToLobbyButtonClicked(event)
	{
		this._hideGroupAfterButtonClick();
		this._restartHideGroupTimer();
		this.emit(GUSLobbyCommonPanelView.EVENT_BACK_TO_LOBBY_BUTTON_CLICKED);

		this._updateUI();

		APP.isMobile && this._fFireSettingsButtonActivated_bln && this.emit(GUSLobbyCommonPanelView.EVENT_FIRE_SETTINGS_BUTTON_CLICKED); //https://jira.dgphoenix.com/projects/MQ/issues/MQ-1154
	}

	_onMobileSoundButtonClicked(event)
	{
		this._restartHideGroupTimer();
	}

	_onInfoButtonClicked(event)
	{
		this._hideGroupAfterButtonClick();
		this._fOtherBtnClick_bln = true;
		this._restartHideGroupTimer();
		this.emit(GUSLobbyCommonPanelView.EVENT_INFO_BUTTON_CLICKED);
	}

	_onHistoryButtonClicked(event)
	{
		this._hideGroupAfterButtonClick();
		this._restartHideGroupTimer();
		this.emit(GUSLobbyCommonPanelView.EVENT_HISTORY_BUTTON_CLICKED);
	}

	_onHomeButtonClicked(event)
	{
		this.emit(GUSLobbyCommonPanelView.EVENT_HOME_BUTTON_CLICKED);
	}

	_onFireSettingsBtnClicked()
	{
		this._hideGroupAfterButtonClick();
		this.emit(GUSLobbyCommonPanelView.EVENT_FIRE_SETTINGS_BUTTON_CLICKED);
	}

	_onFireSettingsStateChanged(aIsActive_bln)
	{
		this._fFireSettingsButtonActivated_bln = aIsActive_bln;
		this._updateUI();
	}

	_onLobbyClicked()
	{
		if (this._fFireSettingsButtonActivated_bln && !this._fOtherBtnClick_bln)
		{
			this.emit(GUSLobbyCommonPanelView.EVENT_FIRE_SETTINGS_BUTTON_CLICKED);
		}

		if (this._fOtherBtnClick_bln)
		{
			this._fOtherBtnClick_bln = false;

			if (this._fFireSettingsButtonActivated_bln)
			{
				this._fFireSettingsButtonActivated_bln = false;
				this._updateUI();
			}
		}
	}
	//...BUTTONS_HANDLERS
	//...HANDLERS

	destroy()
	{
		this._unsubscribeHandlers();

		this._fStageBase_stg.destroy();
		this._fStageButtons_stg && this._fStageButtons_stg.destroy();

		this._resetHideGroupTimer();

		super.destroy();

		this._fStageBase_stg = null;
		this._fStageButtons_stg = null;

		this._fMainButtons_arr = null;

		this._fSoundButtonContainer_lsbv = null;
		this._fHistoryButton_btn = null;		
		this._fButtonsGroupContainer_sprt = null;
		this._fSoundButtonView_sbv = null;

		this._fGroupButton_btn = null;
		this._fBackToLobbyButton_btn = null;
		this._fInfoButton_btn = null;
		this._fSettingsButton_btn = null;

		this._fBalanceBlock_bb = null;
		this._fWinBlock_wb = null;
		this._fInfoBlock_ib = null;
		this._fTimeBlock_tb = null;
		this._fButtonsGroupBack_grphc = null;
		this._fButtonsGroupContainerShift_num = null;

		this._fFireSettingsButtonActivated_bln = null;
		this._fGroupHidden_bln = null;
		this._fGroupAnimationInProgress_bln = null;

		this._fSeptumsContainer_sprt = null;
		this._fSeptums_arr = null;
		this._fOtherBtnClick_bln = null;
		this._fLobbyCaption_cta = null;
	}
}

export default GUSLobbyCommonPanelView;