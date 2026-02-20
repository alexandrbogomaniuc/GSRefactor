import BackgroundTileBaseClassView from '../BackgroundTileBaseClassView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';

const SMOKES_SETTINGS = 
[
	{scale: 1.93, rotation_grad: 70, alpha: 0.2},
	{scale: 2.87, rotation_grad: 162, alpha: 0.35},
	{scale: 1.93, rotation_grad: 200, alpha: 0.17}
]

class BackgroundTilePurpleSmokeView extends BackgroundTileBaseClassView
{
	constructor(aIndex_int)
	{
		super();
		
		let l_rcdo = this._fSmokeView_sprt = APP.library.getSprite("game/bg_smoke_purple");
		l_rcdo.anchor.set(0, 1);
		this.addChild(l_rcdo);

		this._fSmokeIndex_int = aIndex_int;

		this._adjustView();
	}

	get smokeIndex()
	{
		return this._fSmokeIndex_int;
	}

	_adjustView()
	{
		let lScale_num = 1;
		let lRotation_num = 0;
		let lAlpha_num = 1;

		let lSettings_obj = SMOKES_SETTINGS[this._fSmokeIndex_int] || null;

		if (!!lSettings_obj)
		{
			lScale_num = lSettings_obj.scale;
			lRotation_num = lSettings_obj.rotation_grad;
			lAlpha_num = lSettings_obj.alpha;
		}

		this._fSmokeView_sprt.scale.set(lScale_num, lScale_num);
		this._fSmokeView_sprt.rotation = Utils.gradToRad(lRotation_num);
		this._fSmokeView_sprt.alpha = lAlpha_num;
	}
}
export default BackgroundTilePurpleSmokeView;