import Button from '../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class LobbyButton extends Button
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
			this.emit(LobbyButton.EVENT_ON_LOBBY_BTN_PLAY_SOUND, { buttonType: this._buttonType });
		}
	}

	_initGrayFilter()
	{
		let colorMatrix = [
			//R  G  B  A
			0, 0, 0, 0,
			0, 0, 0, 0,
			0, 0, 0, 0,
			0, 0, 0, 0
		];
		let filter = new PIXI.filters.ColorMatrixFilter();
		filter.matrix = colorMatrix;
		filter.resolution = 2;
		filter.alpha = 0.67;
		filter.blur = 10;
		filter.brightness(0.25);
		return filter;
	}

	destroy()
	{
		APP.lobbyButtonsConstroller.unregisterButton(this);

		super.destroy();
	}
}

export default LobbyButton