import GUDialogView from '../../GUDialogView';
import GUDialogRefreshButtonView from './GUDialogRefreshButtonView';
import I18 from '../../../../../../unified/controller/translations/I18';
import { APP } from '../../../../../../unified/controller/main/globals';
import NumberValueFormat from '../../../../../../unified/view/custom/values/NumberValueFormat';

class GUGameRebuyDialogView extends GUDialogView
{
	constructor()
	{
		super();
	}

	__retreiveDialogButtonViewInstance(aIntId_int)
	{
		if (aIntId_int === 1)
		{
			return new GUDialogRefreshButtonView();
		}

		return super.__retreiveDialogButtonViewInstance(aIntId_int);
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

		let lRechargeValue_str = isNaN(aOptRechargeValue_str) ? "" : NumberValueFormat.formatMoney(aOptRechargeValue_str);
		msgText = msgText.replace("/RECHARGE_VALUE/", lRechargeValue_str);

		msg.text = msgText;
		this._messageContainer.addChild(msg);
	}
}

export default GUGameRebuyDialogView;