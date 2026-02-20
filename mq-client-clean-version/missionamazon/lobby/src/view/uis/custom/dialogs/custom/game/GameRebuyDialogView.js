import DialogView from '../../DialogView';
import DialogRefreshButtonView from './DialogRefreshButtonView';
import I18 from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import NumberValueFormat from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class GameRebuyDialogView extends DialogView
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

	setMessage(messageId, aOptRebuyValue_num = undefined, aOptRechargeValue_str = undefined, aCurrenceSymbol_str = undefined)
	{
		this._setMessage(messageId, aOptRebuyValue_num, aOptRechargeValue_str, aCurrenceSymbol_str);
	}

	_setMessage(messageId, aOptRebuyValue_num = undefined, aOptRechargeValue_str = undefined, aCurrenceSymbol_str = undefined)
	{
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset(messageId);
		let msgText = msg.text;
		
		let lRebuyValue_str	= isNaN(aOptRebuyValue_num) ? "" : APP.currencyInfo.i_formatNumber(aOptRebuyValue_num, true, true);
		msgText = msgText.replace("/REBUY_VALUE/", lRebuyValue_str);

		let lRechargeValue_str	= isNaN(aOptRechargeValue_str) ? "" : NumberValueFormat.formatMoney(aOptRechargeValue_str);
		msgText = msgText.replace("/RECHARGE_VALUE/", lRechargeValue_str);
		
		msg.text = msgText;
		this._messageContainer.addChild(msg);
	}

}

export default GameRebuyDialogView;