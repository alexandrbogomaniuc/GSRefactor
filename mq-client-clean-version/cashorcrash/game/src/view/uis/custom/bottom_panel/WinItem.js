import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PointerSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/PointerSprite';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import GameplayInfo from '../../../../model/gameplay/GameplayInfo';

class WinItem extends PointerSprite
{
	static get EVENT_ITEM_CLICKED() 		{ return "onWinItemClicked"; }

	constructor(aId_int)
	{
		super();

		this._fEnabled_bl = false;
		this.enabled = true;

        this._fId_int = aId_int;
        this._fTextView_tf = this.addChild(new TextField(this._getWinTextFormat(0)));
        this._fTextView_tf.anchor.set(0, 0.5);
        this._fTextView_tf.maxWidth = 120;
		
		this.cursorPointer = true;
		this.buttonMode = true;
	}

	set enabled(value)
	{
		value = !!value;

		if (value === this._fEnabled_bl)
		{
			return;
		}

		if (value)
		{
			this.setEnabled();
		}
		else
		{
			this.setDisabled();
		}
	}

	get enabled()
	{
		return this._fEnabled_bl;
	}

    get itemId()
    {
        return this._fId_int;
    }

	getMiddlePoint()
	{
		return new PIXI.Point(this._fTextView_tf.getLocalBounds().width / 2, 0);
	}

    setValue(aValue_num)
    {
        this._fTextView_tf.textFormat = this._getWinTextFormat(aValue_num);
        this._fTextView_tf.text = GameplayInfo.formatMultiplier(aValue_num);
    }

	setDisabled()
	{
		this._fEnabled_bl = false;
		
		this._onUpHandler();

		this.off("pointerclick", this._onClickHandler, this);
		this.off("pointerdown", this._onDownHandler, this);
		this.off("pointerup", this._onUpHandler, this);
		this.off("pointerupoutside", this._onUpOutsideHandler, this);

		APP.off("contextmenu", this._onUpOutsideHandler, this);
		APP.off("lobbycontextmenu", this._onUpOutsideHandler, this);
		APP.off("gamecontextmenu", this._onUpOutsideHandler, this);

		super.setDisabled();
	}

	setEnabled()
	{
		this._fEnabled_bl = true;
		
		this.on("pointerclick", this._onClickHandler, this);
		this.on("pointerdown", this._onDownHandler, this);
		this.on("pointerup", this._onUpHandler, this);
		this.on("pointerupoutside", this._onUpOutsideHandler, this);
		this.on("mouseupoutside", this._onUpOutsideHandler, this);
		APP.on("contextmenu", this._onUpOutsideHandler, this);
		APP.on("lobbycontextmenu", this._onUpOutsideHandler, this);
		APP.on("gamecontextmenu", this._onUpOutsideHandler, this);
		
		super.setEnabled();
	}

	_onClickHandler(e)
	{
		this.emit(WinItem.EVENT_ITEM_CLICKED, {id:this._fId_int});
	}

	_onDownHandler(e) 
	{
		this.holder.scale.set(0.95);
	}

	_onUpOutsideHandler(e) 
	{
		this._onUpHandler(e);
	}

	_onUpHandler(e) 
	{
		if (this.parent)
		{
			this.holder.scale.set(1);
		}
	}

	_getWinTextFormat(aValue_num)
	{
		let lColor_obj = aValue_num < 2 ? 0xff0000 : 0x00ff00;
		return {
			fontFamily: "fnt_nm_roboto_medium",
			fontSize: 16,
			align: "left",
			letterSpacing: 0.5,
			fill: lColor_obj
		};
	}

	destroy()
	{
		APP.off("contextmenu", this._onUpOutsideHandler, this);

        this._fTextView_tf && this.removeChild(this._fTextView_tf);
        this._fTextView_tf = null;

        this._fId_int = null;

		super.destroy();
	}
}

export default WinItem;