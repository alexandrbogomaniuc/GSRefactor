import Sprite from '../../../../../unified/view/base/display/Sprite';
import { APP } from '../../../../../unified/controller/main/globals';
import TextField from '../../../../../unified/view/base/display/TextField';
import Timer from '../../../../../unified/controller/time/Timer';
import I18 from '../../../../../unified/controller/translations/I18';
import Counter from '../../../../../unified/view/custom/Counter';
import GUSLobbyWebSocketInteractionController from '../../../../controller/interaction/server/GUSLobbyWebSocketInteractionController';

class GUSLobbyCPanelBalanceBlock extends Sprite
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
			APP.webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);
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
		let lBalanceLabelAsset_ta = I18.generateNewCTranslatableAsset(this.__balanceCaptionTAssetName);
		this.addChild(lBalanceLabelAsset_ta);
		let lLabelPos_p = this.__balanceCaptionPosition;
		lBalanceLabelAsset_ta.position.set(lLabelPos_p.x, lLabelPos_p.y);

		let lBalanceBase_sprt = APP.library.getSprite(this.__iconAssetName);
		this.addChild(lBalanceBase_sprt);
		let lIconPos_p = this.__iconPosition;
		let lIconScale_obj = this.__iconScale;
		lBalanceBase_sprt.position.set(lIconPos_p.x, lIconPos_p.y);
		lBalanceBase_sprt.scale.set(lIconScale_obj.x, lIconScale_obj.y);

		let lValuePos_p = this.__valueTextPosition;
		this._fBalanceView_tf = this.addChild(new TextField(this.__balanceTextFormat));
		this._fBalanceView_tf.anchor.set(0, 0.5);
		this._fBalanceView_tf.position.set(lValuePos_p.x, lValuePos_p.y);
		this._fBalanceView_tf.maxWidth = this.__maxTextWidth;
		this._setBalance(APP.playerController.info.balance);

		this._fBalanceCounter_c = new Counter({callback:this._setBalance.bind(this)}, {callback:this._getBalance.bind(this)});

		this._addSeptum();
	}

	get __balanceCaptionTAssetName()
	{
		// must be overridden
		return undefined;
	}

	get __balanceCaptionPosition()
	{
		return new PIXI.Point(20, -17);
	}

	get __iconAssetName()
	{
		// must be overridden
		return undefined;
	}

	get __iconPosition()
	{
		return APP.isMobile ? new PIXI.Point(-6, -7) : new PIXI.Point(-6, 1);
	}

	get __iconScale()
	{
		return APP.isMobile ? {x: 1, y: 0.72} : {x: 1, y: 1};
	}

	get __maxTextWidth()
	{
		return APP.isMobile ? 76 : 62;
	}

	get __valueTextPosition()
	{
		return APP.isMobile ? new PIXI.Point(-13, 7) : new PIXI.Point(11, 4);
	}

	get __septumParams()
	{
		return { x: -26, y: 0, height: (APP.isMobile ? 22 : 18), width: 1, color: 0x4e4e4e }
	}

	_addSeptum()
	{
		let lSeptumParams_obj = this.__septumParams;
		let lHeight_num = lSeptumParams_obj.height;
		let lWidth_num = lSeptumParams_obj.width;

		this._fSeptum_grphc = this.addChild(new PIXI.Graphics());
		this._fSeptum_grphc.beginFill(lSeptumParams_obj.color).drawRect(-lWidth_num, -lHeight_num/2, lWidth_num, lHeight_num).endFill();
		this._fSeptum_grphc.position.set(lSeptumParams_obj.x, lSeptumParams_obj.y);
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
		this.emit(GUSLobbyCPanelBalanceBlock.EVENT_REFRESH_BALANCE_REQUEST);
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

	get __balanceTextFormat()
	{
		return {
			fontFamily: "fnt_nm_cmn_barlow",
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

		APP.webSocketInteractionController.off(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);
	}
}

export default GUSLobbyCPanelBalanceBlock