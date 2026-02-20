import BackgroundFulfillTilesetBaseClassView from './BackgroundFulfillTilesetBaseClassView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PreloaderTextures from '../../../external/PreloaderTextures';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class BackgroundTilesetSpaceView extends BackgroundFulfillTilesetBaseClassView
{
	constructor()
	{
		super();

		this._fOffsetX_num = 0;
		this._fOffsetY_num = 0;
	}

	//override
	getInitialPositionY()
	{
		return GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;
	}

	//override
	generateTileView()
	{
		let l_sprt = new Sprite;
		l_sprt.texture = PreloaderTextures['space_textures'][0];
		return l_sprt;
	}

	//override
	getTileWidth()
	{
		return 352;
	}

	//override
	getTileHeight()
	{
		return 352;
	}

	//override
	getOffsetPerMultiplierX()
	{
		return -50;
	}

	//override
	getOffsetPerMultiplierY()
	{
		return 75;
	}

	//override
	getOffsetPerPreLaunchX()
	{
		return -123;
	}

	//override
	getOffsetPerPreLaunchY()
	{
		return 247;
	}
}
export default BackgroundTilesetSpaceView;