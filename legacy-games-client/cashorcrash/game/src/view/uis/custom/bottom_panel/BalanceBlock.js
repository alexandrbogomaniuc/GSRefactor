import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class BalanceBlock extends Sprite
{
	static get BLOCK_WIDTH()
	{
		return 120;
	}

	updateBalance(aVal_num)
	{
		this._setBalance(aVal_num);
	}
	
	constructor()
	{
		super();

		this._fContentContainer_sprt = null;
		this._fBalanceValue_num = null;
		this._fBalanceView_tf = null;

		this._init();
	}

	_init()
	{
		// DEBUG...
		// this.addChild(new PIXI.Graphics).beginFill(0xffff00).drawRect(-BalanceBlock.BLOCK_WIDTH/2, -10, BalanceBlock.BLOCK_WIDTH, 20).endFill();
		// this.addChild(new PIXI.Graphics).beginFill(0xff0000).drawRect(-1, -7, 2, 14).endFill();
		// ...DEBUG
		
		let lContainer_sprt = this._fContentContainer_sprt = this.addChild(new Sprite);

		let lBalanceLabelAsset_ta = APP.library.getSprite("labels/balance");
		lContainer_sprt.addChild(lBalanceLabelAsset_ta);
		lBalanceLabelAsset_ta.position.set(33, -7);
		lBalanceLabelAsset_ta.scale.set(0.35,0.35);

		let lBalanceIcon_sprt = APP.library.getSprite("common_balance_icon");
		lContainer_sprt.addChild(lBalanceIcon_sprt);
		lBalanceIcon_sprt.position.set(-6, 1);

		this._fBalanceView_tf = lContainer_sprt.addChild(new TextField(this._balanceTextFormat));
		this._fBalanceView_tf.anchor.set(0, 0.5);
		this._fBalanceView_tf.position.set(11, 4);
		this._fBalanceView_tf.maxWidth = 78;
		this._setBalance(0);

		if (APP.isMobile)
		{
			lBalanceIcon_sprt.position.y = -7;
			lBalanceIcon_sprt.scale.set(0.72);

			this._fBalanceView_tf.position.set(-13, 7);
			this._fBalanceView_tf.maxWidth = BalanceBlock.BLOCK_WIDTH-18;
		}

		this._alignContent();
	}

	_setBalance(aValue_num)
	{
		this._fBalanceValue_num = aValue_num;

		let lVisibleBalanceValue_num = aValue_num >= 0 ? aValue_num : 0;
		this._fBalanceView_tf.text = this._formatMoneyValue(lVisibleBalanceValue_num);
	}

	_getBalance()
	{
		return this._fBalanceValue_num;
	}

	_formatMoneyValue(aValue_num)
	{
		if (aValue_num !== undefined)
		{
			// [OWL] TODO: apply changes for alll systems without any conditions
			if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
			{
				return APP.currencyInfo.i_formatNumber(aValue_num, true, APP.isBattlegroundGame, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
			}
			else
			{
				return APP.currencyInfo.i_formatNumber(aValue_num, true, APP.isBattlegroundGame, 2);
			}
		}

		return "";
	}

	get _balanceTextFormat()
	{
		return {
			fontFamily: "fnt_nm_barlow",
			fontSize: 13,
			align: "left",
			letterSpacing: 0.5,
			padding: 5,
			fill: 0xffffff
		};
	}

	_alignContent()
	{
		let lPrevText = this._fBalanceView_tf.text;
		this._fBalanceView_tf.text = "12345678912345456789";
		let lContainer_sprt = this._fContentContainer_sprt;
		let lContainerBounds_r = lContainer_sprt.getBounds();
		let lGlobalX_num = lContainerBounds_r.x;
		let lLocalX_num = this.globalToLocal(lGlobalX_num, 0).x;

		lContainer_sprt.x = -lLocalX_num - lContainerBounds_r.width/2;

		this._fBalanceView_tf.text = lPrevText || "";
	}

	destroy()
	{
		super.destroy();

		this._fContentContainer_sprt = null;
		this._fBalanceValue_num = null;
		this._fBalanceView_tf = null;
	}
}

export default BalanceBlock