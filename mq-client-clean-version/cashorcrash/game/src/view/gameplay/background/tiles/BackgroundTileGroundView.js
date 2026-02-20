import BackgroundTileBaseClassView from '../BackgroundTileBaseClassView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../config/AtlasConfig';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class BackgroundTileGroundView extends BackgroundTileBaseClassView
{
	static get GROUND_SKIN_ID_ANTENNAS() { return 0 }
	static get GROUND_SKIN_ID_DESERT() 	{ return 1 }

	constructor(aSkinId_int)
	{
		super();

		this._fGround_rcdo = null;


		//GROUND IMAGE...
		this._fGround_rcdo = new Sprite;
		this._fGround_rcdo.textures = [BackgroundTileGroundView.getGroundTextures()[aSkinId_int]];
		this._fGround_rcdo.anchor.set(0, 1);
		this.addChild(this._fGround_rcdo);
		//...GROUND IMAGE

		//BUSH...
		let l_rcdo = new Sprite;
		l_rcdo.textures = [BackgroundTileGroundView.getBushTextures()[0]];
		l_rcdo.anchor.set(0, 1);
		l_rcdo.position.set(-12, 0);
		this.addChild(l_rcdo);
		//...BUSH
	}

	//override
	getTileWidth()
	{
		return this._fGround_rcdo.getLocalBounds().width;
	}
}
export default BackgroundTileGroundView;

BackgroundTileGroundView.getGroundTextures = function()
{
	if (!BackgroundTileGroundView.ground_textures)
	{
		BackgroundTileGroundView.ground_textures = [];

		BackgroundTileGroundView.ground_textures = AtlasSprite.getFrames([APP.library.getAsset('game/bg_assets')], [AtlasConfig.BackgroundAssets], 'ground');
		BackgroundTileGroundView.ground_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return BackgroundTileGroundView.ground_textures;
}

BackgroundTileGroundView.getBushTextures = function()
{
	if (!BackgroundTileGroundView.bush_textures)
	{
		BackgroundTileGroundView.bush_textures = [];

		BackgroundTileGroundView.bush_textures = AtlasSprite.getFrames([APP.library.getAsset('game/bg_assets')], [AtlasConfig.BackgroundAssets], 'bush');
		BackgroundTileGroundView.bush_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return BackgroundTileGroundView.bush_textures;
}