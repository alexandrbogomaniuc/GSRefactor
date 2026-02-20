import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import NonWobblingTextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/NonWobblingTextField';
import GameplayInfo from '../../../model/gameplay/GameplayInfo';
import AlignDescriptor from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';

class InteractiveMultiplierIndicatorView extends Sprite
{
	constructor()
	{
		super();

		this._fLastMultiplier_num = undefined;

		let lContainer_sprt = this._fContainer_sprt = this.addChild(new Sprite);

		this._fBase_gr = lContainer_sprt.addChild(new PIXI.Graphics);

		let l_tf = this._tf = lContainer_sprt.addChild(new NonWobblingTextField());
		l_tf.fontName = "fnt_nm_barlow_semibold";
		l_tf.fontSize = 18;
		l_tf.fontColor = 0xffffff;
		l_tf.setAlign(AlignDescriptor.CENTER, AlignDescriptor.MIDDLE);
	}

	setValue(aMultiplier_num)
	{
		if (this._fLastMultiplier_num == aMultiplier_num)
		{
			return;
		}

		this._fLastMultiplier_num = aMultiplier_num;

		let l_tf = this._tf;
		l_tf.text = GameplayInfo.formatMultiplier(aMultiplier_num);

		this._updateBase();
		this._alignContent();
	}

	_updateBase()
	{
		let lMultiplier_num = this._fLastMultiplier_num;
		let lContainer_sprt = this._fContainer_sprt;
		let lBase_gr = this._fBase_gr;
		let lTextBounds_r = this._tf.getBounds();
		let lTextLocalLetfBorder_p = lContainer_sprt.globalToLocal(lTextBounds_r.x, lTextBounds_r.y);
		let lTextOffset_obj = { x: 4, y: 2};
		let lBorderTickness_num = 2;

		lBase_gr.clear();

		lBase_gr.beginFill(0x474747).drawRoundedRect(
											lTextLocalLetfBorder_p.x-lTextOffset_obj.x-lBorderTickness_num, 
											lTextLocalLetfBorder_p.y-lTextOffset_obj.y-lBorderTickness_num, 
											lTextBounds_r.width+(lTextOffset_obj.x+lBorderTickness_num)*2, 
											lTextBounds_r.height+(lTextOffset_obj.y+lBorderTickness_num)*2, 
											5)
				.endFill();

		lBase_gr.beginFill(0x000000).drawRoundedRect(
											lTextLocalLetfBorder_p.x-lTextOffset_obj.x, 
											lTextLocalLetfBorder_p.y-lTextOffset_obj.y, 
											lTextBounds_r.width+(lTextOffset_obj.x)*2, 
											lTextBounds_r.height+(lTextOffset_obj.y)*2, 
											5)
				.endFill();
	}

	_alignContent()
	{
		let lContainer_sprt = this._fContainer_sprt;
		let lBase_gr = this._fBase_gr;
		let lBaseBounds_r = lBase_gr.getBounds();
		
		lContainer_sprt.position.set(lBaseBounds_r.width/2, lBaseBounds_r.height/2);
	}
}

export default InteractiveMultiplierIndicatorView