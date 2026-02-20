import SimpleController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import LobbyButton from '../../../../ui/LobbyButton';

class LobbyButtonsController extends SimpleController
{
	static get EVENT_ON_SOME_LOBBY_BTN_PLAY_SOUND()						{ return "EVENT_ON_SOME_LOBBY_BTN_PLAY_SOUND"; }

	registerButton(aButton_b)
	{
		this._registerButton(aButton_b)
	}

	unregisterButton(aButton_b)
	{
		this._unregisterButton(aButton_b)
	}

	constructor(aLobbyButtonsInfo_lbi)
	{
		super(aLobbyButtonsInfo_lbi);
	}

	__initControlLevel()
	{
		super.__initControlLevel();
	}

	_registerButton(aButton_b)
	{
		this.info.registerButton(aButton_b);
		aButton_b.on(LobbyButton.EVENT_ON_LOBBY_BTN_PLAY_SOUND, this._onLobbyBtnPlaySound, this);
	}

	_unregisterButton(aButton_b)
	{
		this.info.unregisterButton(aButton_b);
		aButton_b.off(LobbyButton.EVENT_ON_LOBBY_BTN_PLAY_SOUND, this._onLobbyBtnPlaySound, this);
	}

	_onLobbyBtnPlaySound(aEvent_obj)
	{
		this.emit(LobbyButtonsController.EVENT_ON_SOME_LOBBY_BTN_PLAY_SOUND, { buttonType: aEvent_obj.buttonType });
	}

	destroy()
	{
		super.destroy();
	}
}

export default LobbyButtonsController