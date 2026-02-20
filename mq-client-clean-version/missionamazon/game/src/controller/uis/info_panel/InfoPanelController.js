import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import InfoPanelInfo from '../../../model/uis/info_panel/InfoPanelInfo';
import GamePlayerController from '../../custom/GamePlayerController';
import GameScreen from '../../../main/GameScreen';
import GameField from '../../../main/GameField';
import { LOBBY_MESSAGES } from '../../../external/GameExternalCommunicator';
import GameExternalCommunicator from '../../../external/GameExternalCommunicator';
import Game from '../../../Game';
import GameFRBController from '../custom/frb/GameFRBController';
import GameBonusController from '../custom/bonus/GameBonusController';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';


class InfoPanelController extends SimpleUIController
{
	static get LATENCY_REQUEST() { return 'LATENCY_REQUEST'; }

	constructor()
	{
		super (new InfoPanelInfo());
	}

	__init()
	{
		super.__init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let l_rrsv = this.view;

		this._initInfoValues();
		l_rrsv.update();

		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_ID_CHANGED, this._onRoomIdUpdated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROUND_ID_UPDATED, this._onRoundIdUpdated, this);
		this._playerController.on(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		this._gameField.on(GameField.EVENT_ON_ENEMY_KILLED_BY_PLAYER, this._onEnemyKilled, this);
		this._gameField.on(GameField.EVENT_ON_ROOM_FIELD_CLEARED, this._onGameFieldCleared, this);
		this._gameScreen.gameFrbController.on(GameFRBController.EVENT_ON_FRB_MODE_CHANGED, this._onFRBModeChanged, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);

	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.LATENCY:
				this._onServerLatencyRequest(data);
			break;
		}

		//
	}

	_onServerLatencyRequest(aEvent_obj) {
		/*{
			"rid": 1,
			"step": 1, //it could be [1,2,3,4]
			"class": "Latency",
			"serverTs": "1709640957383, //timestamp
			"serverAckTs": 1709640957456,
			"clientTs": 1709640957420,
			"clientAckTs": 1709640957560
			"latencyValue":optional float
		}*/

		const step = aEvent_obj.step;
		const serverTs = aEvent_obj.serverTs;
		const serverAckTs = aEvent_obj.serverAckTs;
		const clientTs = aEvent_obj.clientTs;
		const clientAckTs = aEvent_obj.clientAckTs;
		const serverLatency = aEvent_obj.latencyValue;
		let serverMessage;
		switch (step) 
		{
			case 1:

				serverMessage = {
					serverTs: serverTs,
					serverAckTs: serverAckTs,
					clientTs: Date.now(),
					clientAckTs: clientAckTs,
					step: 2
				}

				break;
			case 3:
				serverMessage = {
					serverTs: serverTs,
					serverAckTs: serverAckTs,
					clientTs: clientTs,
					clientAckTs: Date.now(),
					step: 4
				}
				break;

		}

		if(serverMessage.step == 4){
			const finalLatency = serverLatency || ((serverMessage.serverAckTs - serverMessage.serverTs) + (serverMessage.clientAckTs - serverMessage.clientTs)) * 0.5;
			this.info.latency = finalLatency;
			this.view.update();
		}

		
		this.emit(InfoPanelController.LATENCY_REQUEST, serverMessage);

	}

	get _gameScreen()
	{
		return APP.currentWindow;
	}

	get _gameField()
	{
		return APP.currentWindow.gameField;
	}

	get _playerController()
	{
		return APP.playerController;
	}

	get _playerInfo()
	{
		return APP.playerController.info;
	}

	get _weaponsInfo()
	{
		return this._gameScreen.weaponsController.info;
	}

	get _gameStateController()
	{
		return this._gameScreen.gameStateController;
	}

	_initInfoValues()
	{
		this.info.roomId = "-";
		this.info.roundId = "-";

		if (this._gameScreen.room)
		{
			this.info.roomId = this._gameScreen.room.id;
			this.info.roundId = this._gameScreen.room.roundId;
		}

		this.info.cpb = this._playerInfo.currentStake;

		this.info.messageNickname = "";
		this.info.messageEnemy = "";
	}

	_onFRBModeChanged()
	{
		let l_rrsv = this.view;

		l_rrsv.update();
	}

	_onRoundIdUpdated(aEvent_obj)
	{
		this.info.roundId = aEvent_obj.roundId;
		let l_rrsv = this.view;

		l_rrsv.update();
	}

	_onRoomIdUpdated(aEvent_obj)
	{
		this.info.roomId = aEvent_obj.id;
		let l_rrsv = this.view;

		l_rrsv.update();
	}

	_onPlayerInfoUpdated()
	{
		this.info.cpb = this._playerInfo.currentStake;
		let l_rrsv = this.view;

		l_rrsv.update();
	}

	_onEnemyKilled(aEvent_obj)
	{
		this.info.messageNickname = aEvent_obj.playerName;
		this.info.messageEnemy = aEvent_obj.enemyName;
		let l_rrsv = this.view;

		l_rrsv.updateKills();
	}

	_onGameFieldCleared()
	{
		
	}

	destroy()
	{
		super.destroy();
	}
}

export default InfoPanelController;