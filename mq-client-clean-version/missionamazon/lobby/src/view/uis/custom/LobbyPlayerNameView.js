import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class LobbyPlayerNameView extends Sprite
{
	init(aName_str, aAnchorPoint_p, aFontSize_num = 13, aMaxWidth_num = 135)
	{
		this._init(aName_str, aAnchorPoint_p, aFontSize_num, aMaxWidth_num);
	}

	updateName(aName_str)
	{
		this._updateName(aName_str);
	}

	constructor()
	{
		super();

		this._fPlayerName_tf = null;
		this._fFontSize_num = null;
		this._fAlign_str = null;
	}

	_init(aName_str, aAnchorPoint_p, aFontSize_num, aMaxWidth_num)
	{
		this._fFontSize_num = aFontSize_num;
		this._fAlign_str = this._specifyAlign(aAnchorPoint_p);

		this._fPlayerName_tf = this.addChild(new TextField(this._specifyTextFormat(aName_str)));
		this._fPlayerName_tf.text = aName_str;
		this._fPlayerName_tf.maxWidth = aMaxWidth_num;
		this._fPlayerName_tf.anchor.set(aAnchorPoint_p.x, aAnchorPoint_p.y);
	}

	_specifyAlign(aAnchorPoint_p)
	{
		if (aAnchorPoint_p.x == 0)
		{
			this._fAlign_str = "left";
		}
		else if (aAnchorPoint_p.x == 1)
		{
			this._fAlign_str = "right";
		}
		else
		{
			this._fAlign_str = "center";
		}
	}

	_updateName(aName_str)
	{
		this._fPlayerName_tf.text = aName_str;
		this._fPlayerName_tf.textFormat = this._specifyTextFormat(aName_str);
	}

	_specifyTextFormat(aName_str)
	{
		let lFontFamily_str = "fnt_nm_barlow";
		let lIsGlyphsSupported_bln = APP.fonts.isGlyphsSupported(lFontFamily_str, aName_str);
		if (!lIsGlyphsSupported_bln)
		{
			lFontFamily_str = "sans-serif";
		}

		let lStyle_obj = {
			align: this._fAlign_str,
			fontFamily: lFontFamily_str,
			fontSize: this._fFontSize_num,
			fill: 0xffffff,
			shortLength: 110 // to fit player panel
		};

		return lStyle_obj;
	}

	destroy()
	{
		super.destroy();

		this._fFontSize_num = null;
		this._fPlayerName_tf = null;
		this._fAlign_str = null;
	}
}

export default LobbyPlayerNameView