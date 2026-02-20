import DialogView from '../DialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import AlignDescriptor from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';

class CriticalErrorDialogView extends DialogView
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

	setMessage(messageId, sessionIdValue, date, errorDate, errorCode, rid)
	{
		this._setMessage(messageId, sessionIdValue, date, errorDate, errorCode, rid);
	}

	_setMessage(messageId, sessionIdValue, date, errorDate, errorCode, rid)
	{
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset(messageId);
		msg.position.set(0, -30);

		let sessionIdAsset = I18.generateNewCTranslatableAsset("TADialogMessageCriticalErrorSessionValueAsset");
		sessionIdAsset.position.set(-110, 30);

		let sessionIdText = new TextField(this._getValueTextFormat());
		sessionIdText.maxWidth = 240;
		sessionIdText.position.set(-105, 23);
		sessionIdText.text = sessionIdValue;
		if (errorCode !== undefined) {
			sessionIdText.text += "/" + errorCode;
		};
		if (rid !== undefined)
		{
			sessionIdText.text += "-" + rid;
		} 
				
		let errorTimeAsset = I18.generateNewCTranslatableAsset("TADialogMessageCriticalErrorTimeValueAsset");
		errorTimeAsset.position.set(-110, 50);

		let errorTimeText = new TextField(this._getValueTextFormat());
		errorTimeText.maxWidth = 240;
		errorTimeText.position.set(-105, 43);
		errorTimeText.text = `${this._pad(date.getUTCMonth()+1)}/${this._pad(date.getUTCDate())}/${date.getUTCFullYear()} ${this._pad(date.getUTCHours())}:${this._pad(date.getUTCMinutes())}:${this._pad(date.getUTCSeconds())} - ${errorDate}`;
		
		this._messageContainer.addChild(msg);
		this._messageContainer.addChild(sessionIdAsset);
		this._messageContainer.addChild(sessionIdText);
		this._messageContainer.addChild(errorTimeAsset);
		this._messageContainer.addChild(errorTimeText);

		this._fMessage_str = sessionIdAsset.text + " " + sessionIdText.text + "\r\n"
							+ errorTimeAsset.text + " " + errorTimeText.text;
	}

	_pad(aDate_num) 
	{
		return aDate_num < 10 
				? '0' + aDate_num 
				: aDate_num;
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
			fontFamily: "fnt_nm_calibri",
			fontSize: 10
		};
		
		return format;
	}

	_alignButtonView (aIntButtonId_int, aButtonsCount_int, aButtonView_domdb)
	{
		var lXOffset_num = 0;
		var lYOffset_num = 0;
		
		if (aButtonsCount_int === 1)
		{
			lXOffset_num = 95;
		}
		else if (aButtonsCount_int === 2)
		{
			var lDirection_num = aIntButtonId_int > 0 ? 1 : -1;
			lXOffset_num = 70*lDirection_num;
		}
		else if (aButtonsCount_int === 3)
		{
			var lDirection_num = aIntButtonId_int == 0 ? -1 : (aIntButtonId_int == 1 ? 0 : 1);
			lXOffset_num = 95*lDirection_num;
			lXOffset_num -= 2;
		}
		
		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
}

export default CriticalErrorDialogView;