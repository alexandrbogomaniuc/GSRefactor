import DialogView from '../../DialogView';
import DialogRefreshButtonView from './DialogRefreshButtonView';
import I18 from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class NEMDialogView extends DialogView
{
	constructor()
	{
		super();
	}

	startBalanceRefreshingHandling()
	{
		this._lockDialog();
		this.customButton && this.customButton.startCaptionRotation();
	}

	stopBalanceRefreshingHandling()
	{
		this._unlockDialog();
		this.customButton && this.customButton.interruptCaptionRotation();
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		if (aIntId_int === 1)
		{
			return new DialogRefreshButtonView();
		}

		return super.__retreiveDialogButtonViewInstance(aIntId_int);
	}

	setMessage(messageId, aOptValue_num = undefined, aOptValue_str = undefined, aOptSubString_str = undefined)
	{
		this._setMessage(messageId, aOptValue_num, aOptValue_str, aOptSubString_str);
	}

	_setMessage(messageId, aOptValue_num = undefined, aOptValue_str = undefined, aOptSubString_str = undefined)
	{
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset(messageId);
		let msgText = msg.text;
		
		if (aOptValue_num !== undefined && !isNaN(aOptValue_num) && aOptSubString_str !== undefined)
		{			
			msgText = I18.prepareNumberPoweredMessage(msgText, aOptSubString_str, aOptValue_num);			
		}

		if (aOptValue_str !== undefined || aOptValue_num !== undefined)
		{
			let lValue_str = aOptValue_str ? aOptValue_str : aOptValue_num;
			msgText = msgText.replace("/VALUE/", lValue_str);
		}
		
		msg.text = msgText;
		this._messageContainer.addChild(msg);
	}

}

export default NEMDialogView;