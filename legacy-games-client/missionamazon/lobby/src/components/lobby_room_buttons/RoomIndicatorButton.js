import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Button from '../../ui/LobbyButton';

class RoomIndicatorButton extends Button 
{
	update(aValue_num)
	{
		this._update(aValue_num);
	}

	setSelectedView(aIsMouseOverIndicator)
	{
		this._setSelectedView(aIsMouseOverIndicator);
	}

	setBaseView(aIsMouseOutIndicator)
	{
		this._setBaseView(aIsMouseOutIndicator);
	}

	constructor(aIconAssetName_str) 
	{
		super(undefined, undefined, true);

		this._fIconAssetName_str = aIconAssetName_str;
		this._fStroke_g = null;
		this._fIsSelected_bl = null;
		this._fIsMouseOverIndicator_bl = null;

		this._init();
	}

	_init()
	{
		this._addBack();
		this._addIcon()
		this._addTextField();
	}

	_addBack()
	{
		this._fStroke_g = new PIXI.Graphics();
		this._fStroke_g.beginFill(0xf6921e);
		this._fStroke_g.drawRoundedRect(-23, -10, 46, 20, 5);
		this._fStroke_g.endFill();
		this.holder.addChild(this._fStroke_g);

		let lBack_g = new PIXI.Graphics();
		lBack_g.beginFill(0x000000);
		lBack_g.drawRoundedRect(-22, -9, 44, 18, 4);
		lBack_g.endFill();
		this.holder.addChild(lBack_g);

		this._fIsSelected_bl = false;
	}

	_addIcon()
	{
		let lIcon_spr = this.holder.addChild(APP.library.getSprite(this._fIconAssetName_str));
		lIcon_spr.position.set(-8, 0);
		lIcon_spr.scale.set(0.8, 0.8);
	}

	_addTextField()
	{
		this._fValue_tf = this.holder.addChild(new TextField(this._getStyle));
		this._fValue_tf.maxWidth = 20;
		this._fValue_tf.anchor.set(0.5, 0.5);
		this._fValue_tf.position.set(10, 0);

		this._fValue_tf.text = 0;
	}

	get _getStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 11,
			fill: 0xffffff,
			align: "center"
		};

		return lStyle_obj;
	}

	_update(aValue_num)
	{
		if(this._fValue_tf)
		{
			this._fValue_tf.text = aValue_num;

			if(aValue_num === 0)
			{
				this._fValue_tf.alpha = 0.5; 
			}
			else
			{
				this._fValue_tf.alpha = 1; 
			}
		}
	}

	_setSelectedView(aIsMouseOverIndicator)
	{
		aIsMouseOverIndicator && (this._fIsMouseOverIndicator_bl = true);

		if(this._fStroke_g && !this._fIsSelected_bl)
		{
			this.holder.removeChild(this._fStroke_g);
			this._fStroke_g.destroy();
			this._fStroke_g = null;

			this._fStroke_g = new PIXI.Graphics();
			this._fStroke_g.beginFill(0xf6921e);
			this._fStroke_g.drawRoundedRect(-23, -10, 46, 20, 5);
			this._fStroke_g.endFill();
			this.holder.addChildAt(this._fStroke_g, 0);

			this._fIsSelected_bl = true;
		}
	}

	_setBaseView(aIsMouseOutIndicator)
	{
		aIsMouseOutIndicator && (this._fIsMouseOverIndicator_bl = false);

		if(this._fStroke_g && this._fIsSelected_bl && !this._fIsMouseOverIndicator_bl)
		{
			this.holder.removeChild(this._fStroke_g);
			this._fStroke_g.destroy();
			this._fStroke_g = null;

			this._fStroke_g = new PIXI.Graphics();
			this._fStroke_g.beginFill(0xf6921e);
			this._fStroke_g.drawRoundedRect(-23, -10, 46, 20, 5);
			this._fStroke_g.endFill();
			this.holder.addChildAt(this._fStroke_g, 0);

			this._fIsSelected_bl = false;
		}
	}

	destroy()
	{
		this._fIconAssetName_str = null;
		this._fValue_tf && this._fValue_tf.destroy()
		this._fValue_tf = null;
		this._fStroke_g = null;
		this._fIsSelected_bl = null;
		this._fIsMouseOverIndicator_bl = null;
		super.destroy();
	}
}

export default RoomIndicatorButton;