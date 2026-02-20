import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Counter from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/Counter';
import LobbyWebSocketInteractionController from '../../../../../controller/interaction/server/LobbyWebSocketInteractionController';

class BalanceBlock extends Sprite
{
	static get EVENT_REFRESH_BALANCE_REQUEST()			{ return "onRefreshBalanceRequest"; }

	hideSeptum()
	{
		this._fSeptum_grphc.visible = false;
	}

	showSeptum()
	{
		this._fSeptum_grphc.visible = true;
	}

	initBalanceRefreshTimer()
	{
		this._initBalanceRefreshTimer();
	}

	updateBalanceIfRequired(aVal_num)
	{
		this._updateBalanceIfRequired(aVal_num);
	}

	resetCounting()
	{
		this._resetCounting();
	}

	constructor(aIndicatorsUpdateTime_num)
	{
		super();

		this._fIndicatorsUpdateTime_num = aIndicatorsUpdateTime_num;
		this._fBalanceValue_num = null;

		this._fRefreshTimer_tmr = null;
		this._fBalanceView_tf = null;
		this._fBalanceCounter_c = null;

		this._fSeptum_grphc = null;


		if(APP.isBattlegroundGame)
		{
			APP.webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);
		}

		this._init();
	}

	_onLobbyServerBalanceUpdated(aEvent_obj)
	{
		let lNewBalance_int = aEvent_obj.messageData.balance;
		this._setBalance(lNewBalance_int);
	}

	_init()
	{
		let lBalanceLabelAsset_ta = I18.generateNewCTranslatableAsset("TACommonPanelBalanceLabel");
		this.addChild(lBalanceLabelAsset_ta);
		lBalanceLabelAsset_ta.position.set(20, -17);

		let lBalanceBase_sprt = APP.library.getSprite("common_balance_icon");
		this.addChild(lBalanceBase_sprt);
		lBalanceBase_sprt.position.set(-6, 1);

		this._fBalanceView_tf = this.addChild(new TextField(this._balanceTextFormat));
		this._fBalanceView_tf.anchor.set(0, 0.5);
		this._fBalanceView_tf.position.set(11, 4);
		this._fBalanceView_tf.maxWidth = 62;
		this._setBalance(APP.playerController.info.balance);

		this._fBalanceCounter_c = new Counter({callback:this._setBalance.bind(this)}, {callback:this._getBalance.bind(this)});

		if (APP.isMobile)
		{
			lBalanceBase_sprt.position.y = -7;
			lBalanceBase_sprt.scale.set(0.72);

			this._fBalanceView_tf.position.set(-13, 7);
			this._fBalanceView_tf.maxWidth = 76;
		}

		this._addSeptum();
	}

	_addSeptum()
	{
		this._fSeptum_grphc = this.addChild(new PIXI.Graphics());
		let lHeight_num = APP.isMobile ? 22 : 18;
		this._fSeptum_grphc.beginFill(0x4e4e4e).drawRect(-1, -lHeight_num/2, 1, lHeight_num).endFill();
		this._fSeptum_grphc.position.set(-26, 0);
	}

	_initBalanceRefreshTimer()
	{
		this._sendRefresh();
		if (!this._fRefreshTimer_tmr)
		{
			let lFreq_num = APP.appParamsInfo.updateBalanceFrequency || 8000;
			this._fRefreshTimer_tmr = new Timer(this._sendRefresh.bind(this), lFreq_num, true);
		}
	}

	_sendRefresh()
	{
		this.emit(BalanceBlock.EVENT_REFRESH_BALANCE_REQUEST);
	}

	_setBalance(aValue_num)
	{
		this._fBalanceValue_num = aValue_num;
		this._fBalanceView_tf.text = this._formatMoneyValue(aValue_num);
	}

	_getBalance()
	{
		return this._fBalanceValue_num;
	}

	_updateBalanceIfRequired(aValue_num)
	{
		let lDuration_num = this._fIndicatorsUpdateTime_num;
		if (this._fBalanceCounter_c.inProgress)
		{
			if (lDuration_num > 0 || aValue_num == 0)
			{
				this._fBalanceCounter_c.stopCounting();
			}
			else
			{
				this._fBalanceCounter_c.updateCounting(aValue_num);
			}
		}

		if (lDuration_num > 0 || aValue_num == 0)
		{
			this._fBalanceCounter_c.startCounting(aValue_num, lDuration_num);
		}
		else if (this._fBalanceValue_num !== aValue_num)
		{
			this._setBalance(aValue_num);
		}
	}

	_resetCounting()
	{
		this._fBalanceCounter_c && this._fBalanceCounter_c.finishCounting();
	}

	_formatMoneyValue(aValue_num)
	{
		if (aValue_num !== undefined)
		{
			return APP.currencyInfo.i_formatNumber(aValue_num, true, APP.isBattlegroundGame, 2);
		}

		return "";
	}

	get _balanceTextFormat()
	{
		return {
			fontFamily: "fnt_nm_barlow",
			fontSize: APP.isMobile ? 13 : 11,
			align: "left",
			letterSpacing: 0.5,
			padding: 5,
			fill: 0xffffff
		};
	}

	destroy()
	{
		this._fBalanceCounter_c && this._fBalanceCounter_c.destructor();

		super.destroy();

		this._fIndicatorsUpdateTime_num = null;
		this._fBalanceValue_num = null;
		this._fRefreshTimer_tmr = null;

		this._fBalanceView_tf = null;
		this._fBalanceCounter_c = null;

		this._fSeptum_grphc = null;

		APP.webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);
	}
}

export default BalanceBlock