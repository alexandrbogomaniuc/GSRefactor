import GUPreloaderSoundButtonController from './GUPreloaderSoundButtonController';
import { APP } from '../../../../../unified/controller/main/globals';
import GUSLobbyDialogsController from '../dialogs/GUSLobbyDialogsController';
import GUPreloaderSoundButtonView from '../../../../view/uis/preloader/GUPreloaderSoundButtonView';
import GUSGameExternalCommunicator, { LOBBY_MESSAGES } from '../../../external/GUSGameExternalCommunicator';

class GUSGamePreloaderSoundButtonController extends GUPreloaderSoundButtonController
{
	__init()
	{
		super.__init();

		this._fGameSoundsInfo_gsi = APP.soundsController.info;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.externalCommunicator.on(GUSGameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}
	
	get __isSoundOn()
	{
		return this._fGameSoundsInfo_gsi.isSoundsVolumeOn;
	}

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

	destroy()
	{
		this._fGameSoundsInfo_gsi = null;
		
		super.destroy();
	}
}
export default GUSGamePreloaderSoundButtonController