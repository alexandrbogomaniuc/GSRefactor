import BackgroundTileBaseClassView from '../BackgroundTileBaseClassView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PreloaderTextures from '../../../../external/PreloaderTextures';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class BackgroundTileSunsetView extends BackgroundTileBaseClassView
{
	constructor()
	{
		super();

		//SUNSET IMAGE...
		this._fSunset_rcdo = APP.library.getSprite("game/back_sunset");
		this._fSunset_rcdo.anchor.set(0, 1);
		this._fSunset_rcdo.scale.set(1.5, 1.1);
		this.addChild(this._fSunset_rcdo);
		//...SUNSET IMAGE
	}

	//override
	getTileWidth()
	{
		return super.getTileWidth() - 1;
	}
}
export default BackgroundTileSunsetView;