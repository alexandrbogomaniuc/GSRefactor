import BackgroundTilesetBaseClassView from './BackgroundTilesetBaseClassView';
import BackgroundTileKaktuses0View from './tiles/BackgroundTileKaktuses0View';
import BackgroundTileKaktuses1View from './tiles/BackgroundTileKaktuses1View';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../config/AtlasConfig';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class BackgroundTilesetKaktusesView extends BackgroundTilesetBaseClassView
{
	constructor()
	{
		super(2);
	}

	//override
	getTilesetHeight()
	{
		return 115;
	}

	//override
	generateTileView(aTemplateTileIndex_int)
	{
		return new BackgroundTileKaktuses0View();
		/*
		switch(aTemplateTileIndex_int)
		{
			case 0: return new BackgroundTileKaktuses0View();
			case 1: return new BackgroundTileKaktuses1View();
		}
		*/
	}

	//override
	getInitialPositionY()
	{
		return GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;
	}

	//override
	getOffsetPerMultiplierY()
	{
		return 125;
	}

	//OVERRIDE...
	getOffsetPerPreLaunchY()
	{
		return 220;
	}
	//...OVERRIDE

}
export default BackgroundTilesetKaktusesView;

BackgroundTilesetKaktusesView.getKaktusTextures = function()
{
	if (!BackgroundTilesetKaktusesView.kaktus_textures)
	{
		BackgroundTilesetKaktusesView.kaktus_textures = [];

		BackgroundTilesetKaktusesView.kaktus_textures = AtlasSprite.getFrames([APP.library.getAsset('game/bg_assets')], [AtlasConfig.BackgroundAssets], 'kaktus');
		BackgroundTilesetKaktusesView.kaktus_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return BackgroundTilesetKaktusesView.kaktus_textures;
}