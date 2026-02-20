import DialogView from '../DialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class BonusDialogView extends DialogView
{
	constructor()
	{
		super();
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
			let lSubStringsCount_num = (msgText.split(aOptSubString_str).length - 1);

			while (lSubStringsCount_num-- > 0)
			{
				msgText = I18.prepareNumberPoweredMessage(msgText, aOptSubString_str, aOptValue_num);
			}
		}

		let lValue_str;
		if (
			messageId === "TADialogFRBLobbyIntro"		//these messages have no money value
			|| messageId === "TADialogFRBLobbyIntro"
			)
		{
			lValue_str = aOptValue_str || aOptValue_num || "";
		}
		else // others have money value and must be formatted
		{
			if (aOptValue_str !== undefined)
			{
				lValue_str = APP.currencyInfo.i_formatString(aOptValue_str);
			}
			else if (aOptValue_num !== undefined)
			{
				lValue_str = APP.currencyInfo.i_formatNumber(aOptValue_num, true);
			}
		}
		msgText = msgText.replace("/VALUE/", lValue_str);

		msg.text = msgText;
		this._messageContainer.addChild(msg);
	}
}

export default BonusDialogView;