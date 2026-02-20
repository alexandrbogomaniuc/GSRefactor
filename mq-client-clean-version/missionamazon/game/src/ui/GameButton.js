import Button from '../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class GameButton extends Button
{
	constructor(baseAssetName, captionId, cursorPointer, noDefaultSound, anchorPoint, buttonType)
	{
		super(baseAssetName, captionId, cursorPointer, noDefaultSound, anchorPoint, buttonType);
	}

	_tryPlaySound()
	{
		if (!this.noDefaultSound && APP.soundsController)
		{
			let soundName = "";

			switch (this._buttonType)
			{
				case Button.BUTTON_TYPE_ACCEPT:
					soundName = "mq_gui_button_generic_ui";
				break;
				case Button.BUTTON_TYPE_CANCEL:
					soundName = "mq_gui_button_generic_ui";
				break;
				case Button.BUTTON_TYPE_COMMON:
				default:
					soundName = "mq_gui_button_generic_ui";
				break;
			}

			APP.soundsController.play && APP.soundsController.play(soundName);
		}
	}
}

export default GameButton