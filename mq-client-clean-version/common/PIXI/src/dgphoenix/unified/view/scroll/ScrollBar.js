import Sprite from '../base/display/Sprite';
import ScrollableContainer from './ScrollableContainer';
import { APP } from '../../controller/main/globals';
import BaseSlider from '../slider/BaseSlider';

/**
 * Scrollbar 
 * @class
 * @augments Sprite
 */
class ScrollBar extends Sprite
{
	constructor()
	{
		super();
		this._fSlider_s = null;
		this._fScrollableContainer_sc = null;
		this._fScrollableContainerMask_gr = null;

		this._fHitArea_obj = null;
		this._fVisibleArea_obj = null;

		this._fScrollEnabled_bln = false;
		this._fScrollHandler_func = this._mouseWheelHandler.bind(this);	
		
		this._fLastDragPoint_num = null;

		this._fDragabbleDirectionBottom_bl = null;
	}

	get dragabbleDirectionMultiplication()
	{
		return this._fDragabbleDirectionBottom_bl ? -1 : 1;
	}

	_setDragabbleDirectionTop()
	{
		this._fDragabbleDirectionBottom_bl = false;
	}

	_setDragabbleDirectionBottom()
	{
		this._fDragabbleDirectionBottom_bl = true;
	}

	/**
	 * Slider view.
	 * @type {BaseSlider}
	 */
	get slider()
	{
		return this._fSlider_s;
	}

	/**
	 * Set or update slider view.
	 * @param {BaseSlider} aSlider_s
	 */
	set slider(aSlider_s)
	{
		if (this._fSlider_s === aSlider_s)
		{
			return;
		}

		if (!!this._fSlider_s)
		{
			this._fSlider_s.off("trackUpdated", this._updateListPosition, this);
			this._fSlider_s.off(BaseSlider.EVENT_ON_SCROLL_MULTIPLIER_UPDATED, this._onSliderScrollMultiplierUpdated, this);
		}

		this._fSlider_s = aSlider_s;
		this._fSlider_s.on("trackUpdated", this._updateListPosition, this);
		this._fSlider_s.on(BaseSlider.EVENT_ON_SCROLL_MULTIPLIER_UPDATED, this._onSliderScrollMultiplierUpdated, this);
	}

	/**
	 * Container handled by scrollbar.
	 * @type {Sprite}
	 */
	get scrollableContainer()
	{
		return this._fScrollableContainer_sc;
	}

	/**
	 * Set container handled by scrollbar.
	 */
	set scrollableContainer(aScrollableContainer_sprt)
	{
		this.__initScrollableContainer(aScrollableContainer_sprt);
	}

	/**
	 * Hit area for scrolling by mouse.
	 * @type {PIXI.Rectangle}
	 */
	get hitArea()
	{
		return this._fHitArea_obj || super.hitArea;
	}

	set hitArea(aValue_obj)
	{
		this._fHitArea_obj = aValue_obj;
	}

	/**
	 * Visible area for scrollable container.
	 * @type {PIXI.Rectangle}
	 */
	get visibleArea()
	{
		return this._fVisibleArea_obj;
	}

	set visibleArea(aValue_obj)
	{
		this._fVisibleArea_obj = aValue_obj;
		
		if (this._fScrollableContainerMask_gr)
		{
			this._fScrollableContainerMask_gr.clear();
			this._fScrollableContainerMask_gr.beginFill(0xff0000).drawRect(aValue_obj.x, aValue_obj.y, aValue_obj.width, aValue_obj.height).endFill();
		}
	}

	/** Enable scrolling. */
	enableScroll()
	{
		this._subscribeOnWheelHandler();
	}

	/** Disable scrolling. */
	disableScroll()
	{
		this._unsubscribeOnWheelHandler();
	}

	/** Enable scrolling by drag. */
	enableDrag()
	{
		this._subscribeOnDragHandlers();
	}

	/** Disable scrolling by drag. */
	disableDrag()
	{
		this._unsubscribeOnDragHandlers();
	}

	//abstract
	__initSlider(backAsset, trackAsset, btnAsset, stepsCount = 20, buttonIndent=2, holdInterval, aOptDiscreteTrackScale_bl = false)
	{
		throw new Error("ScrollBar :: calling abstract method __initSlider");
	}

	__initScrollableContainer(aContainer_sprt)
	{
		this._fScrollableContainer_sc = aContainer_sprt;

		let lVisibleArea_rect = this.visibleArea;
		if (lVisibleArea_rect)
		{
			this._fScrollableContainerMask_gr = this.addChild(new PIXI.Graphics());
			this._fScrollableContainerMask_gr.beginFill(0xff0000).drawRect(lVisibleArea_rect.x, lVisibleArea_rect.y, lVisibleArea_rect.width, lVisibleArea_rect.height).endFill();
			this._fScrollableContainer_sc.mask = this._fScrollableContainerMask_gr;
		}
		this._fScrollableContainer_sc.on(ScrollableContainer.EVENT_ON_CONTENT_UPDATED, this._onContentUpdated, this);
	}

	_onScroll(aValue_num)
	{		
		if (this.slider)
		{
			if (!this.slider.visible)
				return;
			if (aValue_num > 0)
			{
				this.slider.moveUp(true);
			}
			else
			{
				this.slider.moveDown(true);
			}
		}
		else
		{
			this._scrollContainer(aValue_num);
		}
	}

	_scrollContainer(aValue_num)
	{
		//abstract
		throw new Error('Abstract method call!');
	}

	_updateListPosition(aEvent_obj)
	{
		if (!this.scrollableContainer)
			return;
		let lStep_num = aEvent_obj.value;
		this.scrollableContainer.moveTo(lStep_num);
	}

	_onSliderScrollMultiplierUpdated(aEvent_obj)
	{
		this._updateSlider();
	}

	_subscribeOnWheelHandler()
	{
		if (this._fScrollEnabled_bln) return;

		if ("onwheel" in document)
		{
			document.addEventListener("wheel", this._fScrollHandler_func);
		}
		else if ("onmousewheel" in document)
		{
			document.addEventListener("mousewheel", this._fScrollHandler_func);
		}
		else
		{
			document.addEventListener("MozMousePixelScroll", this._fScrollHandler_func);
		}

		this._fScrollEnabled_bln = true;
	}

	_unsubscribeOnWheelHandler()
	{
		if (!this._fScrollEnabled_bln) return;

		if ("onwheel" in document)
		{
			document.removeEventListener("wheel", this._fScrollHandler_func);
		}
		else if ("onmousewheel" in document)
		{
			document.removeEventListener("mousewheel", this._fScrollHandler_func);
		}
		else
		{
			document.removeEventListener("MozMousePixelScroll", this._fScrollHandler_func);
		}

		this._fScrollEnabled_bln = false;
	}

	_mouseWheelHandler(e)
	{
		if (!this.visible) return;
		if (!this.hitArea) return;
				
		let lVal_num = -(e.deltaY || e.detail);
		if (!lVal_num) lVal_num = e.wheelDelta;

		let lLocalMouse_obj = this.toLocal(APP.stage.renderer.plugins.interaction.mouse.global);
		let lIsHit_bln = this.hitArea.contains(lLocalMouse_obj.x, lLocalMouse_obj.y);

		if (lIsHit_bln)
		{
			this._onScroll(lVal_num);
		}
	}

	_subscribeOnDragHandlers()
	{
		let lScrollableContainer_sc = this.scrollableContainer;
		if (lScrollableContainer_sc)
		{
			lScrollableContainer_sc.on("pointerdown", this._onDraggebleContainerPushed.bind(this));
			lScrollableContainer_sc.on("pointerup", this.__onDraggebleContainerPulled.bind(this));
			lScrollableContainer_sc.on("pointerupoutside", this.__onDraggebleContainerPulled.bind(this));
			lScrollableContainer_sc.on("pointermove", this._onDraggebleContainerDragged.bind(this));
			lScrollableContainer_sc.on("contextmenu", this.__onDraggebleContainerPulled.bind(this));
		}
	}

	_unsubscribeOnDragHandlers()
	{
		let lScrollableContainer_sc = this.scrollableContainer;
		if (lScrollableContainer_sc)
		{
			lScrollableContainer_sc.off("pointerdown", this._onDraggebleContainerPushed.bind(this));
			lScrollableContainer_sc.off("pointerup", this.__onDraggebleContainerPulled.bind(this));
			lScrollableContainer_sc.off("pointerupoutside", this.__onDraggebleContainerPulled.bind(this));
			lScrollableContainer_sc.off("pointermove", this._onDraggebleContainerDragged.bind(this));
			lScrollableContainer_sc.off("contextmenu", this.__onDraggebleContainerPulled.bind(this));
		}
		this._fLastDragPoint_num = null;
	}

	_onDraggebleContainerPushed(e)
	{
		if (this.slider && this.slider.visible)
		{
			this._fLastDragPoint_num = this.parent.toLocal(e.data.global)[this.__getDragParameter()];
		}
		else
		{
			this._fLastDragPoint_num = null;
		}
	}

	__onDraggebleContainerPulled(e)
	{
		this._fLastDragPoint_num = null;
	}

	_onDraggebleContainerDragged(e)
	{
		if (this._fLastDragPoint_num === null) return;
		let lCurrentPoint_num = this.parent.toLocal(e.data.global)[this.__getDragParameter()];
		if (lCurrentPoint_num === this._fLastDragPoint_num)
		{
			return;
		}

		let lOffset_num = lCurrentPoint_num - this._fLastDragPoint_num;

		if (this.slider && this.slider.visible && Math.abs(lOffset_num) < this.slider.scrollMultiplier)
		{
			return;
		}
		let lDelta_num = lOffset_num * this.dragabbleDirectionMultiplication;
		this._fLastDragPoint_num = lCurrentPoint_num;

		if (this.slider && this.slider.visible)
		{
			this.slider.onTrackUpStep(lDelta_num);
		}
		else
		{
			throw new Error('Slider is required for drag scrolling.');
		}
	}

	__getDragParameter()
	{
		throw new Error("Abstract method called!");
	}

	_onContentUpdated(event)
	{
		this._updateSlider();
	}

	_updateSlider()
	{
		throw new Error("Abstract method called!");
	}

	/**
	 * Destroy instance.
	 */
	destroy()
	{
		this._unsubscribeOnWheelHandler();
		this._unsubscribeOnDragHandlers();
		this.scrollableContainer.off(ScrollableContainer.EVENT_ON_CONTENT_UPDATED, this._onContentUpdated, this);
		super.destroy();
	}
}

export default ScrollBar