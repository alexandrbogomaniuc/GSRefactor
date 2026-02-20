import SimpleController from '../../../../../unified/controller/base/SimpleController';
import GUSLobbyButton from '../../../../view/uis/GUSLobbyButton';
import GUSLobbyButtonsInfo from '../../../../model/uis/custom/lobby_buttons/GUSLobbyButtonsInfo';

class GUSLobbyButtonsController extends SimpleController
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

	constructor(aOptLobbyButtonsInfo_lbi)
	{
		super(aOptLobbyButtonsInfo_lbi || new GUSLobbyButtonsInfo());
	}

	__initControlLevel()
	{
		super.__initControlLevel();
	}

	_registerButton(aButton_b)
	{
		this.info.registerButton(aButton_b);
		aButton_b.on(GUSLobbyButton.EVENT_ON_LOBBY_BTN_PLAY_SOUND, this._onLobbyBtnPlaySound, this);
	}

	_unregisterButton(aButton_b)
	{
		this.info.unregisterButton(aButton_b);
		aButton_b.off(GUSLobbyButton.EVENT_ON_LOBBY_BTN_PLAY_SOUND, this._onLobbyBtnPlaySound, this);
	}

	_onLobbyBtnPlaySound(aEvent_obj)
	{
		this.emit(GUSLobbyButtonsController.EVENT_ON_SOME_LOBBY_BTN_PLAY_SOUND, { buttonType: aEvent_obj.buttonType });
	}

	destroy()
	{
		super.destroy();
	}
}

export default GUSLobbyButtonsController