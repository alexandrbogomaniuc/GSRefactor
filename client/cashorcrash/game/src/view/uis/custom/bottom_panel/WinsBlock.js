import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import WinItem from './WinItem';
import SelectableBottomPanelButton from './SelectableBottomPanelButton';
import { GAME_VIEW_SETTINGS } from '../../../main/GameBaseView';

class WinsBlock extends Sprite
{
	static get EVENT_ROUND_HISTORY_ITEM_CLICKED() 		{ return WinItem.EVENT_ITEM_CLICKED; }
	static get ITEMS_INTERVAL()							{ return 5; }

	updateWinsLog(aValues_num_arr)
	{
		this._updateWinsLog(aValues_num_arr);
	}

	updateCurrentAim(aIndex_int)
	{
		this._updateCurrentAim(aIndex_int);
	}

	setMaxWidth(aValue_num)
	{
		let lOffsetRight_num = 0;
		if (this._fNextButton_sb)
		{
			lOffsetRight_num = this._fNextButton_sb.getHitArea().width;
			this._fNextButton_sb.position.x = aValue_num - lOffsetRight_num / 2;
		}
		this._fWinsMask_gr.scale.x = (aValue_num - lOffsetRight_num - this._fWinsMask_gr.position.x) / 100;
	}

	enableButtons()
	{
		for (let i = 0; i < this._fWinViews_wi_arr.length; i++)
		{
			this._fWinViews_wi_arr[i].setEnabled();
		}
	}

	disableButtons()
	{
		this._fPrevButton_sb.setDisabled();
		this._fNextButton_sb.setDisabled();

		for (let i = 0; i < this._fWinViews_wi_arr.length; i++)
		{
			this._fWinViews_wi_arr[i].setDisabled();
		}
	}

	constructor()
	{
		super();

		this._fWinValues_num_arr = [];
		this._fWinViews_wi_arr = [];
		this._fCurrentWinIndex_int = undefined;
		
		let lBarRowHeight_num = GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT;
		let lElementsOffsetX_num = (lBarRowHeight_num-2)/2;

		let lWinIcon_sprt = this._fIcon_sprt = this.addChild(APP.library.getSprite("common_win_icon"));
		lWinIcon_sprt.scale.set(APP.isMobile ? 1.4 : 1);
		lWinIcon_sprt.position.set(lElementsOffsetX_num, 0);
		lElementsOffsetX_num += lWinIcon_sprt.getLocalBounds().width;

		let lPrevButton_sb = this._fPrevButton_sb = this.addChild(new SelectableBottomPanelButton("common_arrow_icon", null, APP.isMobile ? 1.6 : 1.1, true));
		lPrevButton_sb.on("pointerclick", this._onPrevButtonClicked, this);
		lPrevButton_sb.rotation = Math.PI;
		lPrevButton_sb.position.set(lElementsOffsetX_num, 0);
		lElementsOffsetX_num += lPrevButton_sb.getHitArea().width;

		let lNextButton_sb = this._fNextButton_sb = this.addChild(new SelectableBottomPanelButton("common_arrow_icon", null, APP.isMobile ? 1.6 : 1.1, true));
		lNextButton_sb.on("pointerclick", this._onNextButtonClicked, this);
		
		let lWinsContainer_sprt = this._fWinsContainer_sprt = this.addChild(new Sprite);
		let lWinsMask_gr = this._fWinsMask_gr = this.addChild(new PIXI.Graphics());
		lWinsMask_gr.beginFill(0x000000).drawRect(0, -lBarRowHeight_num / 2, 100, lBarRowHeight_num).endFill();
		lWinsMask_gr.position.x = lWinsContainer_sprt.position.x = lPrevButton_sb.position.x + lPrevButton_sb.getHitArea().width / 2;
		lWinsContainer_sprt.mask = lWinsMask_gr;

		let lArrowIcon_sprt = this._fArrowIcon_sprt = this.addChild(APP.library.getSprite("common_arrow_icon"));
		lArrowIcon_sprt.scale.set(0.8);
		lArrowIcon_sprt.rotation = Math.PI / 2;
		lArrowIcon_sprt.position.y = -12;
		lArrowIcon_sprt.visible = false;

		this._validateNavigationButtons();
	}

	_updateWinsLog(aValues_num_arr)
	{
		let lWinItemOffsetX_num = 0;
		let lValuesCount_int = aValues_num_arr.length;
		for (let i = 0; i < lValuesCount_int; i++)
		{
			let lWinView_wi = this._getWinView(i);
			lWinView_wi.setValue(aValues_num_arr[i]);
			let lWinBounds_rt = lWinView_wi.getLocalBounds();
			lWinView_wi.position.x = lWinItemOffsetX_num - lWinBounds_rt.x;
			
			lWinItemOffsetX_num += lWinBounds_rt.width + WinsBlock.ITEMS_INTERVAL;
			this._fWinValues_num_arr[i] = aValues_num_arr[i];
		}

		for (let i = this._fWinViews_wi_arr.length - 1; i >= lValuesCount_int; i--)
		{
			this._fWinsContainer_sprt.removeChild(this._fWinViews_wi_arr.pop()).destroy();
		}

		if (this._fCurrentWinIndex_int === undefined)
		{
			this._fWinsContainer_sprt.position.x = this._fWinsMask_gr.position.x; 
		}

		this._validateNavigationButtons();
	}

	_updateCurrentAim(aIndex_int)
	{
		this._fCurrentWinIndex_int = aIndex_int;
		
		for (let i = 0; i < this._fWinViews_wi_arr.length; i++)
		{
			let lWinItem_wi = this._fWinViews_wi_arr[i];
			if (lWinItem_wi.itemId === aIndex_int)
			{
				let lMaskBounds_rt = this._fWinsMask_gr.getBounds();
				let lCurrentWinBounds_rt = lWinItem_wi.getBounds();
				let lOutOfLeftBoundary_num = this.toLocal(lCurrentWinBounds_rt).x - this.toLocal(lMaskBounds_rt).x;
				let lOutOfRightBoundary_num = lOutOfLeftBoundary_num + (lCurrentWinBounds_rt.width - lMaskBounds_rt.width);
				if (lOutOfLeftBoundary_num < 0)
				{
					this._fWinsContainer_sprt.position.x -= lOutOfLeftBoundary_num;
				}
				else if (lOutOfRightBoundary_num > 0)
				{
					this._fWinsContainer_sprt.position.x -= lOutOfRightBoundary_num;
				}
				
				let lWinItemMiddlePointX_int = APP.isMobile ? lWinItem_wi.getMiddlePoint().x / 2 : lWinItem_wi.getMiddlePoint().x;
				this._fArrowIcon_sprt.position.x = lWinItem_wi.localToLocal(lWinItemMiddlePointX_int, 0, this).x;
			}
		}
		this._fArrowIcon_sprt.visible = (aIndex_int !== undefined);
		this._validateNavigationButtons();
	}

	_onPrevButtonClicked(e)
	{
		if (this._fCurrentWinIndex_int > 0)
		{
			this.emit(WinItem.EVENT_ITEM_CLICKED, {id:--this._fCurrentWinIndex_int});
			this._validateNavigationButtons();
		}
	}

	_onNextButtonClicked(e)
	{
		if (this._fCurrentWinIndex_int < this._fWinViews_wi_arr.length - 1)
		{
			this.emit(WinItem.EVENT_ITEM_CLICKED, {id:++this._fCurrentWinIndex_int});
			this._validateNavigationButtons();
		}
	}

	_validateNavigationButtons()
	{
		if (this._fPrevButton_sb)
		{
			if (this._fCurrentWinIndex_int > 0)
			{
				this._fPrevButton_sb.enabled = true;
			}
			else
			{
				this._fPrevButton_sb.enabled = false;
			}
		}
		if (this._fNextButton_sb)
		{
			if (this._fCurrentWinIndex_int < this._fWinViews_wi_arr.length - 1)
			{
				this._fNextButton_sb.enabled = true;
			}
			else
			{
				this._fNextButton_sb.enabled = false;
			}
		}
	}

	_getWinView(aIndex_int)
	{
		if (!this._fWinViews_wi_arr[aIndex_int])
		{
			let lWinItem_wi = this._fWinsContainer_sprt.addChild(new WinItem(aIndex_int));
			lWinItem_wi.on(WinItem.EVENT_ITEM_CLICKED, this.emit, this);
			this._fWinViews_wi_arr[aIndex_int] = lWinItem_wi;
		}
		return this._fWinViews_wi_arr[aIndex_int];
	}

	destroy()
	{
		this._fWinValues_num_arr = null;
		if (this._fWinViews_wi_arr)
		{
			for (let i = 0; i < this._fWinViews_wi_arr.length; i++)
			{
				this._fWinsContainer_sprt.removeChild(this._fWinViews_wi_arr.pop()).destroy();
			}
			this._fWinViews_wi_arr = null;
		}

		this._fWinsMask_gr && this.removeChild(this._fWinsMask_gr);
		this._fWinsMask_gr = null;

		this._fWinsContainer_sprt && this.removeChild(this._fWinsContainer_sprt);
		this._fWinsContainer_sprt = null;
		
		this._fPrevButton_sb && this.removeChild(this._fPrevButton_sb);
		this._fPrevButton_sb = null;

		this._fNextButton_sb && this.removeChild(this._fNextButton_sb);
		this._fNextButton_sb = null;

		this._fArrowIcon_sprt && this.removeChild(this._fArrowIcon_sprt);
		this._fArrowIcon_sprt = null;

		this._fCurrentWinIndex_int = undefined;
		
		super.destroy();
	}
}

export default WinsBlock