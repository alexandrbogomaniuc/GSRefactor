import Button from '../../../unified/view/ui/Button';
import { APP } from '../../../unified/controller/main/globals';

class GUSLobbyButton extends Button
{
	static get EVENT_ON_LOBBY_BTN_PLAY_SOUND()						{ return "EVENT_ON_LOBBY_BTN_PLAY_SOUND"; }

	constructor(baseAssetName, captionId, cursorPointer, noDefaultSound, anchorPoint, buttonType)
	{
		super(baseAssetName, captionId, cursorPointer, noDefaultSound, anchorPoint, buttonType);

		APP.lobbyButtonsConstroller.registerButton(this);
	}

	_tryPlaySound()
	{
		if (!this.noDefaultSound)
		{
			this.emit(GUSLobbyButton.EVENT_ON_LOBBY_BTN_PLAY_SOUND, { buttonType: this._buttonType });
		}
	}

	destroy()
	{
		APP.lobbyButtonsConstroller.unregisterButton(this);

		super.destroy();
	}
}

export default GUSLobbyButton