import AtlasSprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../config/AtlasConfig';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

var PreloaderTextures = 
{
	bar_textures: null,
	planet_textures: null,
	play_now_btn_textures: null,
	space_textures: null,
	sunset_textures: null
}

PreloaderTextures.initTextures = function()
{
	PreloaderTextures.setTexture('bar_textures', ["preloader/preloader_assets"], [AtlasConfig.PreloaderAssets], 'bar');
	PreloaderTextures.setTexture('planet_textures', ["preloader/preloader_assets"], [AtlasConfig.PreloaderAssets], 'planet');
	PreloaderTextures.setTexture('play_now_btn_textures', ["preloader/preloader_assets"], [AtlasConfig.PreloaderAssets], 'play_now');
	PreloaderTextures.setTexture('space_textures', ["preloader/space_assets"], [AtlasConfig.PreloaderSpaceAssets], 'space');
	PreloaderTextures.setTexture('sunset_textures', ["preloader/space_assets"], [AtlasConfig.PreloaderSpaceAssets], 'sunset');
}

PreloaderTextures.setTexture = function (name, imageNames, configs, path)
{
	if (!PreloaderTextures[name])
	{
		PreloaderTextures[name] = [];

		if(!Array.isArray(imageNames)) imageNames = [imageNames];
		if(!Array.isArray(configs)) configs = [configs];

		let assets = [];
		imageNames.forEach(function(item){assets.push(APP.library.getAsset(item))});

		PreloaderTextures[name] = AtlasSprite.getFrames(assets, configs, path);
		PreloaderTextures[name].sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}
};

export default PreloaderTextures;