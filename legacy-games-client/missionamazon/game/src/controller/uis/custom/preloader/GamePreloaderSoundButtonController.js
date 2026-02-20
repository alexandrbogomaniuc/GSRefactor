import MultiStateButtonController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/MultiStateButtonController';
import GamePreloaderSoundButtonView from '../../../../view/uis/custom/preloader/GamePreloaderSoundButtonView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Game from '../../../../Game';
import GameExternalCommunicator from '../../../../external/GameExternalCommunicator';
import { LOBBY_MESSAGES } from '../../../../external/GameExternalCommunicator';

class GamePreloaderSoundButtonController extends MultiStateButtonController
{
	static get EVENT_SOUND_ON_BUTTON_CLICKED() 		{return GamePreloaderSoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED;}
	static get EVENT_SOUND_OFF_BUTTON_CLICKED() 	{return GamePreloaderSoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED;}

	//INIT...
	__init()
	{
		super.__init();

		this._fGameSoundsInfo_gsi = APP.soundsController.info;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(Game.EVENT_ON_SOUND_SETTINGS_CHANGED, this._updateSoundButtonState, this);

		APP.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_sbv = this.view;
		lView_sbv.on(GamePreloaderSoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED, this._onSoundOnButtonClicked, this);
		lView_sbv.on(GamePreloaderSoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED, this._onSoundOffButtonClicked, this);

		this._updateSoundButtonState();
	}
	//...INIT

	_onLobbyExternalMessageReceived(event)
	{
		let msgType = event.type;
		switch (msgType)
		{
			case LOBBY_MESSAGES.DIALOG_ACTIVATED:
				this.view && this.view.hide();
				break;
			case LOBBY_MESSAGES.DIALOG_DEACTIVATED:
				this.view && this.view.show();
				break;
		}
	}

	_updateSoundButtonState()
	{
		let lView_sbv = this.view;
		if (lView_sbv)
		{
			lView_sbv.buttonState = this._fGameSoundsInfo_gsi.isSoundsVolumeOn 
									? GamePreloaderSoundButtonView.STATE_SOUND_ON 
									: GamePreloaderSoundButtonView.STATE_SOUND_OFF;
		}
	}

	_onSoundOnButtonClicked(event)
	{
		let lView_sbv = this.view;
		lView_sbv.buttonState = GamePreloaderSoundButtonView.STATE_SOUND_OFF;

		this.emit(event);
	}

	_onSoundOffButtonClicked(event)
	{
		let lView_sbv = this.view;
		lView_sbv.buttonState = GamePreloaderSoundButtonView.STATE_SOUND_ON;

		this.emit(event);
	}

	destroy()
	{
		this._fGameSoundsInfo_gsi = null;

		APP.off(Game.EVENT_ON_SOUND_SETTINGS_CHANGED, this._updateSoundButtonState, this);

		APP.externalCommunicator.off(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
		
		super.destroy();
	}
}
export default GamePreloaderSoundButtonController