import PlayerController from '../../../unified/controller/custom/PlayerController';
import { APP } from '../../../unified/controller/main/globals';
import PlayerInfo from '../../../unified/model/custom/PlayerInfo';
import { GAME_MESSAGES } from '../external/GUSExternalCommunicator';
import { Utils } from '../../../unified/model/Utils';
import GUSLobbyApplication from '../main/GUSLobbyApplication';
import GUSLobbyWebSocketInteractionController from '../interaction/server/GUSLobbyWebSocketInteractionController';
import { CLIENT_MESSAGES, SERVER_MESSAGES } from '../../model/interaction/server/GUSLobbyWebSocketInteractionInfo';
import GUSLobbyExternalCommunicator from '../external/GUSLobbyExternalCommunicator';
import GUSLobbyScreen from '../../view/main/GUSLobbyScreen';
import GUSLobbyTooltipsController from '../uis/custom/tooltips/GUSLobbyTooltipsController';
import GUSLobbyTutorialController from '../uis/custom/tutorial/GUSLobbyTutorialController';
import GUGamePicksUpSpecialWeaponsFirstTimeDialogController from '../uis/custom/dialogs/custom/game/GUGamePicksUpSpecialWeaponsFirstTimeDialogController';

class GUSLobbyPlayerController extends PlayerController
{
	constructor(aOptInfo)
	{
		super(aOptInfo);
	}

	//INIT...
	__initControlLevel()
	{
		super.__initControlLevel();

		this._fRoundInProgress_bln = false;

		APP.once(GUSLobbyApplication.EVENT_ON_UPDATE_BATTLEGROUND_MODE, this._updateDisableAutofirengIfBattleground, this);

		APP.on(GUSLobbyApplication.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyAppStarted, this);
		APP.webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		APP.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		let lPicksUpSpecialWeaponsFirstTimeDialogController_puswftdc = APP.dialogsController.picksUpSpecialWeaponsFirstTimeDialogController;
		lPicksUpSpecialWeaponsFirstTimeDialogController_puswftdc.once(GUGamePicksUpSpecialWeaponsFirstTimeDialogController.ON_CHANGE_STATE, this._onPicksUpSpecialWeaponsChangeStateRequestConfirmed, this);

		this._initDisableAutofireng();
	}

	__initModelLevel()
	{
		super.__initModelLevel();
	}
	//...INIT

	_updateDisableAutofirengIfBattleground()
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_DISABLE_MQ_AUTOFIRING] = {value: false};
		this.info.setPlayerInfo(PlayerInfo.KEY_DISABLE_MQ_AUTOFIRING, lUpdatedData_obj[PlayerInfo.KEY_DISABLE_MQ_AUTOFIRING]);
		this.emit(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
	}

	_initDisableAutofireng()
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_DISABLE_MQ_AUTOFIRING] = { value: APP.appParamsInfo.disableAutofireng };

		this.info.setPlayerInfo(PlayerInfo.KEY_DISABLE_MQ_AUTOFIRING, lUpdatedData_obj[PlayerInfo.KEY_DISABLE_MQ_AUTOFIRING]);
		this.emit(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, { data: lUpdatedData_obj });
	}

	_onGameMessageReceived(aEvent_obj)
	{
		if (aEvent_obj.type == GAME_MESSAGES.GAME_ROUND_STATE_CHANGED)
		{
			this._fRoundInProgress_bln = aEvent_obj.data.state;
		}
	}

	_onPlayerInfoUpdated(event)
	{
		let data = event.data;

		if (Object.keys(data).length)
		{
			for (let key in data)
			{
				this.info.setPlayerInfo(key, data[key]);
			}
		}
	}

	_onPicksUpSpecialWeaponsChangeStateRequestConfirmed()
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_DID_THE_PLAYER_WIN_SW_ALREADY] = { value: true };

		this.info.setPlayerInfo(PlayerInfo.KEY_DID_THE_PLAYER_WIN_SW_ALREADY, lUpdatedData_obj[PlayerInfo.KEY_DID_THE_PLAYER_WIN_SW_ALREADY]);

		this.emit(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, { data: lUpdatedData_obj });
	}

	_onLobbyAppStarted()
	{
		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_START_GAME_URL_REQUIRED, this._onStartGameUrlRequired, this);
		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_ALREADY_SW_WIN_INFO_UPDATED, this._onAlreadySWWinsInfoUpdated, this);
		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_FIRE_SETTINGS_UPDATED, this._onFireSettingsUpdated, this);
		
		let lCustomerBrand_obj = APP.customerspecController.info.brand;
		let lSettingsBrand_obj = APP.config.brand;

		let lBrandEnable_bln = lSettingsBrand_obj.enable;

		if (lCustomerBrand_obj && lCustomerBrand_obj.priority >= lSettingsBrand_obj.priority)
		{
			lBrandEnable_bln = lCustomerBrand_obj.enable;
		}

		this.info.setPlayerInfo(PlayerInfo.KEY_BRAND_ENABLE, { value: lBrandEnable_bln });

		if (APP.isTutorialSupported)
		{
			APP.tutorialController.on(GUSLobbyTutorialController.EVENT_ON_TUTORIAL_NEEDED_STATE_CHANGED, this._onTooltipsStateChanged, this);
		}
		else
		{
			APP.tooltipsController.on(GUSLobbyTooltipsController.EVENT_ON_TIPS_STATE_CHANGED, this._onTooltipsStateChanged, this);
		}
		
		this.info.setPlayerInfo(PlayerInfo.KEY_TOOL_TIP_ENABLED, { value: APP.tooltipsController.info.tooltipsEnabled });
	}

	_onAlreadySWWinsInfoUpdated()
	{
		let lobbyScreenPlayerInfo = APP.lobbyScreen.playerInfo;

		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_DID_THE_PLAYER_WIN_SW_ALREADY] = { value: lobbyScreenPlayerInfo.didThePlayerWinSWAlready };

		this.info.setPlayerInfo(PlayerInfo.KEY_DID_THE_PLAYER_WIN_SW_ALREADY, lUpdatedData_obj[PlayerInfo.KEY_DID_THE_PLAYER_WIN_SW_ALREADY]);
		this.emit(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, { data: lUpdatedData_obj });
	}

	_onFireSettingsUpdated()
	{
		let lobbyScreenPlayerInfo = APP.lobbyScreen.playerInfo;

		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_LOCK_ON_TARGET] = { value: lobbyScreenPlayerInfo.fireSettings.lockOnTarget };
		lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_TARGET_PRIORITY] = { value: lobbyScreenPlayerInfo.fireSettings.targetPriority };
		lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_AUTO_FIRE] = { value: lobbyScreenPlayerInfo.fireSettings.autoFire };
		lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_FIRE_SPEED] = { value: lobbyScreenPlayerInfo.fireSettings.fireSpeed };

		this.info.setPlayerInfo(PlayerInfo.KEY_FIRE_SETTINGS_LOCK_ON_TARGET, lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_LOCK_ON_TARGET]);
		this.info.setPlayerInfo(PlayerInfo.KEY_FIRE_SETTINGS_TARGET_PRIORITY, lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_TARGET_PRIORITY]);
		this.info.setPlayerInfo(PlayerInfo.KEY_FIRE_SETTINGS_AUTO_FIRE, lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_AUTO_FIRE]);
		this.info.setPlayerInfo(PlayerInfo.KEY_FIRE_SETTINGS_FIRE_SPEED, lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_FIRE_SPEED]);

		this.emit(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, { data: lUpdatedData_obj });
	}

	_onStartGameUrlRequired(event)
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_ENTERING_ROOM_STAKE] = { value: event.stake };

		this.info.setPlayerInfo(PlayerInfo.KEY_ENTERING_ROOM_STAKE, lUpdatedData_obj[PlayerInfo.KEY_ENTERING_ROOM_STAKE]);
		this.emit(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, { data: lUpdatedData_obj });
	}

	_onTooltipsStateChanged(aEvent_obj)
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_TOOL_TIP_ENABLED] = { value: aEvent_obj.state };

		this.info.setPlayerInfo(PlayerInfo.KEY_TOOL_TIP_ENABLED, lUpdatedData_obj[PlayerInfo.KEY_TOOL_TIP_ENABLED]);
		this.emit(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, { data: lUpdatedData_obj });
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		let lUpdatedData_obj = {};

		switch (data.class)
		{
			case SERVER_MESSAGES.ENTER_LOBBY_RESPONSE:
				lUpdatedData_obj[PlayerInfo.KEY_STAKES] = { value: data.stakes, time: data.date };
				lUpdatedData_obj[PlayerInfo.KEY_STAKES_LIMIT] = { value: data.stakesLimit || 0, time: data.date };
				lUpdatedData_obj[PlayerInfo.KEY_BALANCE] = { value: data.balance, time: data.date };
				lUpdatedData_obj[PlayerInfo.KEY_REFRESH_BALANCE] = { value: data.showRefreshBalanceButton || false, time: data.date };
				lUpdatedData_obj[PlayerInfo.KEY_TEST_SYSTEM] = { value: APP.appParamsInfo.testSystem || false };
				lUpdatedData_obj[PlayerInfo.KEY_NICKNAME_GLYPHS] = { value: (data.nicknameGlyphs || PlayerInfo.DEFAULT_NICK_GLYPHS) };
				lUpdatedData_obj[PlayerInfo.KEY_NICKNAME_EDIT_ENABLED] = {value: data.nicknameEditable === undefined ? true : data.nicknameEditable};
				lUpdatedData_obj[PlayerInfo.KEY_BULLETS_LIMIT] = {value: data.maxBulletsLimitOnMap};
				lUpdatedData_obj[PlayerInfo.KEY_PAYTABLE] = { value: data.paytable, time: data.date };

				if (data.currency && data.currency.code)
				{
					lUpdatedData_obj[PlayerInfo.KEY_CURRENCY_CODE] = { value: data.currency.code, time: data.date};
					APP.currencyInfo.i_setCurrencyId(data.currency.code);
				}

				if (data.disableTooltips !== undefined)
				{
					lUpdatedData_obj[PlayerInfo.KEY_TOOL_TIP_ENABLED] = {value: !data.disableTooltips};
				}

				this.__parseCustomEnterLobbyResponseParams(data, lUpdatedData_obj);
				break;

			case SERVER_MESSAGES.BALANCE_UPDATED:
				let commonPanelInfo = APP.commonPanelController.info;
				if (commonPanelInfo.gameUIVisible && commonPanelInfo.playerSatIn)
				{
					// no actions required, balance will be updated via game socket
				}
				else
				{
					lUpdatedData_obj[PlayerInfo.KEY_BALANCE] = { value: data.balance, time: data.date };
				}
				break;

			case SERVER_MESSAGES.OK:
				let lRequestData_obj = event.requestData;

				switch (lRequestData_obj.class)
				{
					case CLIENT_MESSAGES.CHANGE_NICKNAME:
						let nickName = lRequestData_obj.nickname || "";
						if (nickName.length > 0)
						{
							nickName = Utils.filterGlyphs(nickName, this.info.nicknameGlyphs, true);
						}
						lUpdatedData_obj[PlayerInfo.KEY_NICKNAME] = { value: nickName };
						break;
				}
				break;

			case SERVER_MESSAGES.RE_BUY_RESPONSE:
				lUpdatedData_obj[PlayerInfo.KEY_BALANCE] = { value: data.balance, time: data.date };
				break;
		}

		if (Object.keys(lUpdatedData_obj).length)
		{
			for (let lKey_str in lUpdatedData_obj)
			{
				this.info.setPlayerInfo(lKey_str, lUpdatedData_obj[lKey_str]);
			}

			this.emit(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, { data: lUpdatedData_obj });
		}
	}

	__parseCustomEnterLobbyResponseParams(data, lUpdatedData_obj)
	{
		// to be overridden
	}

	destroy()
	{
		APP.off(GUSLobbyApplication.EVENT_ON_UPDATE_BATTLEGROUND_MODE, this._updateDisableAutofirengIfBattleground, this);

		APP.off(GUSLobbyApplication.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.off(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyAppStarted, this);

		APP.webSocketInteractionController.off(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		APP.externalCommunicator.off(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);		

		let lPicksUpSpecialWeaponsFirstTimeDialogController_puswftdc = APP.dialogsController.picksUpSpecialWeaponsFirstTimeDialogController;
		lPicksUpSpecialWeaponsFirstTimeDialogController_puswftdc.off(GamePicksUpSpecialWeaponsFirstTimeDialogController.ON_CHANGE_STATE, this._onPicksUpSpecialWeaponsChangeStateRequestConfirmed, this, true);

		super.destroy();

		this._fRoundInProgress_bln = undefined;
	}
}

export default GUSLobbyPlayerController;