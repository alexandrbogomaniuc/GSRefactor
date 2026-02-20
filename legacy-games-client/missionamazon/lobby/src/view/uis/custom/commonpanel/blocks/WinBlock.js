import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Counter from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/Counter';

class WinBlock extends Sprite
{
	hideSeptum()
	{
		this._fSeptum_grphc.visible = false;
	}

	showSeptum()
	{
		this._fSeptum_grphc.visible = true;
	}

	updateWinIfRequired(aValue_num)
	{
		this._updateWinIfRequired(aValue_num);
	}

	resetCounting()
	{
		this._resetCounting();
	}

	constructor(aIndicatorsUpdateTime_num)
	{
		super();

		this._fIndicatorsUpdateTime_num = aIndicatorsUpdateTime_num;
		this._fWinValue_num = null;

		this._fWinView_tf = null;
		this._fWinCounter_c = null;

		this._fSeptum_grphc = null;

		this._init();
	}

	_init()
	{
		let lWinLabelAsset_ta = I18.generateNewCTranslatableAsset("TACommonPanelWinLabel");
		this.addChild(lWinLabelAsset_ta);
		lWinLabelAsset_ta.position.set(-16, -17);

		let lWinBase_sprt = APP.library.getSprite("common_win_icon");
		this.addChild(lWinBase_sprt);
		lWinBase_sprt.position.set(-42, 0);

		this._fWinView_tf = this.addChild(new TextField(this._winTextFormat));
		this._fWinView_tf.anchor.set(0, 0.5);
		this._fWinView_tf.position.set(-26, 4);
		this._fWinView_tf.maxWidth = 62;

		this._setWin(0);
		this._fWinCounter_c = new Counter({callback: this._setWin.bind(this)}, {callback: this._getWin.bind(this)});

		if (APP.isMobile)
		{
			lWinBase_sprt.position.y = -7;
			lWinBase_sprt.scale.set(0.76);

			this._fWinView_tf.position.set(-48, 7);
			this._fWinView_tf.maxWidth = 76;
		}

		this._addSeptum();
	}

	_addSeptum()
	{
		this._fSeptum_grphc = this.addChild(new PIXI.Graphics());
		let lHeight_num = APP.isMobile ? 22 : 18;
		this._fSeptum_grphc.beginFill(0x4e4e4e).drawRect(-1, -lHeight_num/2, 1, lHeight_num).endFill();
		this._fSeptum_grphc.position.set(-63, 0);
	}

	_setWin(aValue_num)
	{
		this._fWinValue_num = aValue_num;
		this._fWinView_tf.text = this._formatMoneyValue(aValue_num);
	}

	_getWin()
	{
		return this._fWinValue_num;
	}

	_updateWinIfRequired(aValue_num)
	{
		aValue_num = aValue_num || 0;

		let lDuration_num = this._fIndicatorsUpdateTime_num;
		if (this._fWinCounter_c.inProgress)
		{
			if (lDuration_num > 0 || aValue_num == 0)
			{
				this._fWinCounter_c.stopCounting();
			}
			else
			{
				this._fWinCounter_c.updateCounting(aValue_num);
			}
		}

		if (lDuration_num > 0 || aValue_num == 0)
		{
			this._fWinCounter_c.startCounting(aValue_num, lDuration_num);
		}
		else if (this._fWinValue_num !== aValue_num)
		{
			this._setWin(aValue_num);
		}
	}

	_resetCounting()
	{
		this._fWinCounter_c && this._fWinCounter_c.finishCounting();
	}

	_formatMoneyValue(aValue_num)
	{
		if (aValue_num !== undefined)
		{
			return APP.currencyInfo.i_formatNumber(aValue_num, true);
		}

		return "";
	}

	get _winTextFormat()
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
		this._fWinCounter_c && this._fWinCounter_c.destructor();

		super.destroy();

		this._fIndicatorsUpdateTime_num = null;
		this._fWinValue_num = null;

		this._fWinView_tf = null;
		this._fWinCounter_c = null;

		this._fSeptum_grphc = null;
	}
}

export default WinBlock