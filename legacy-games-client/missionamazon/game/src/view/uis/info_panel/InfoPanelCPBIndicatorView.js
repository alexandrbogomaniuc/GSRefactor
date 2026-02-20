import GUIndicatorView from '../../../../../../common/PIXI/src/dgphoenix/gunified/view/ui/GUIndicatorView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class InfoPanelCPBIndicatorView extends GUIndicatorView
{

	updateBonusMode(aFrbMode_bln)
	{
		this._fFrbMode_bln = aFrbMode_bln;
	}

	//INIT...
	constructor(aOptTranslatableCaption_str, aFrbMode_bln)
	{
		super(aOptTranslatableCaption_str);

		this._fFrbMode_bln = aFrbMode_bln;
		this._fLastSetValue_num = 0;
	}

	_initIndicatorView()
	{
		this._fValueContainer_sprt.position.set(6, 0);

		super._initIndicatorView();
	}
	//...INIT

	//CAPTION...
	_initCaptionView(aOptTranslatableCaption_str = undefined)
	{
		if (APP.isBattlegroundGame)
		{
			return;
		}

		let lTranslatableAssetId_str = aOptTranslatableCaption_str === undefined ? this._getDefaultCaptionAssetId() : aOptTranslatableCaption_str;
		if (lTranslatableAssetId_str != undefined)
		{
			this._fCaptionView_ta = this._fCaptionContainer_sprt.addChild((I18.generateNewCTranslatableAsset(lTranslatableAssetId_str)));
		}
	}

	_getDefaultCaptionAssetId()
	{
		return "TAInfoPanelCPBLabel";
	}
	//...CAPTION

	//VALUE...
	set _indicatorValue(aValue_num)
	{
		if (!Utils.isNumber(aValue_num) || aValue_num === undefined)
		{
			throw new Error("Invalid indicator value: " + aValue_num);
			return;
		}

		this._fIndicatorValue_num = aValue_num;

		this._forceIndicatorUpdate(aValue_num);
	}

	_initValueView()
	{
		super._initValueView();
		this._fValueView_tf.anchor.set(0, 0.5);

		let lAssetDescriptor_ad =  I18.getTranslatableAssetDescriptor(this._getDefaultCaptionAssetId());
		let lAreaDescriptor_obj = lAssetDescriptor_ad.areaInnerContentDescriptor.areaDescriptor;

		this._fValueContainer_sprt.position.x += lAreaDescriptor_obj.width + lAreaDescriptor_obj.x;
		this._fValueContainer_sprt.position.y += lAreaDescriptor_obj.height/2 + lAreaDescriptor_obj.y;
	}
	
	_getValueTextFormat()
	{
		let format = {
			align: "left",
			fill: 0x838383,
			fontFamily: "fnt_nm_barlow",
			fontSize: 10
		};
		return format;
	}

	_getValueMaxWidth()
	{
		return 0;//96;
	}
	
	_formatValue(aValue_num)
	{
		this._fLastSetValue_num = aValue_num;

		let lPossibleBets_arr = APP.playerController.info.possibleBetLevels;
		let lVal_str = APP.currencyInfo.i_formatInterval(aValue_num, aValue_num * lPossibleBets_arr[lPossibleBets_arr.length-1], true);

		return lVal_str;
	}

	get indicatorMaxWidth()
	{
		return 120;
	}

	_onValueChanged()
	{
		this._fValueContainer_sprt.scale.x = 1;
		let bounds = this._fValueContainer_sprt.getBounds();
		if (bounds && bounds.width > this.indicatorMaxWidth)
		{
			this._fValueContainer_sprt.scale.x = this.indicatorMaxWidth/bounds.width;
		}

		super._onValueChanged();
	}
	//...VALUE

	destory()
	{
		this._fCaption_ta = null;
		this._fCaptionView_ta = null;
		
		super.destroy();
	}
}

export default InfoPanelCPBIndicatorView;