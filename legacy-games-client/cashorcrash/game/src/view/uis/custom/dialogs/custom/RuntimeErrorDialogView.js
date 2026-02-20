import DialogView from '../DialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import AlignDescriptor from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';
import { StringUtils } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class RuntimeErrorDialogView extends DialogView
{
	copyToClibboard()
	{
		this._copyToClipBoard();
	}

	constructor()
	{
		super();

		this._fMessage_str = "";
	}

	setMessage(messageId, errorMessage)
	{
		this._setMessage(messageId, errorMessage);
	}

	_setMessage(messageId, errorMessage)
	{
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset(messageId);
		msg.position.set(0, -30);


		let errorMessageText = new TextField(this._getValueTextFormat());
		errorMessageText.maxWidth = 240;
		errorMessageText.position.set(-105, 43);
		errorMessageText.text = errorMessage;

		this._messageContainer.addChild(msg);
		//this._messageContainer.addChild(errorMessageText);

		this._fMessage_str = errorMessage; //[Y]TODO add formatting?
	}

	_copyToClipBoard()
	{
		StringUtils.copyToClipBoard(this._fMessage_str);
	}

	_getValueTextFormat()
	{
		let format = {
			align: AlignDescriptor.RIGHT,
			fill: 0xffffff,
			fontFamily: "fnt_nm_calibri",
			fontSize: 10
		};

		return format;
	}
}

export default RuntimeErrorDialogView;