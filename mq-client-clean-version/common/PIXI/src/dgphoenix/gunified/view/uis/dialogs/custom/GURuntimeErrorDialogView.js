import GUDialogView from '../GUDialogView';
import AlignDescriptor from '../../../../../unified/model/display/align/AlignDescriptor';
import TextField from '../../../../../unified/view/base/display/TextField';
import I18 from '../../../../../unified/controller/translations/I18';

class GURuntimeErrorDialogView extends GUDialogView
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
		var el = document.createElement('textarea');
		el.value = this._fMessage_str;
		document.body.appendChild(el);

		if (navigator.userAgent.match(/ipad|ipod|iphone/i))
		{
			var editable = el.contentEditable;
			var readOnly = el.readOnly;

			// convert to editable with readonly to stop iOS keyboard opening
			el.contentEditable = true;
			el.readOnly = true;

			// create a selectable range
			var range = document.createRange();
			range.selectNodeContents(el);

			// select the range
			var selection = window.getSelection();
			selection.removeAllRanges();
			selection.addRange(range);
			el.setSelectionRange(0, 999999);

			// restore contentEditable/readOnly to original state
			el.contentEditable = editable;
			el.readOnly = readOnly;
		}
		else
		{
			el.select();
		}
		document.execCommand('copy');
		document.body.removeChild(el);
	}

	_getValueTextFormat()
	{
		let format = {
			align: AlignDescriptor.RIGHT,
			fill: 0xffffff,
			fontFamily: "fnt_nm_cmn_calibri",
			fontSize: 10
		};

		return format;
	}
}

export default GURuntimeErrorDialogView;