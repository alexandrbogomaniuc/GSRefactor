import AtlasConfig from '../../../config/AtlasConfig';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

let PreloaderSparks = {
	textures: {
		sparks: null		
	}
};

PreloaderSparks.setTexture = function (name, imageNames, configs, path) {
	if(!PreloaderSparks.textures[name]){
		PreloaderSparks.textures[name] = [];

		if(!Array.isArray(imageNames)) imageNames = [imageNames];
		if(!Array.isArray(configs)) configs = [configs];

		let assets = [];
		imageNames.forEach(function(item){assets.push(APP.library.getAsset(item))});

		PreloaderSparks.textures[name] = AtlasSprite.getFrames(assets, configs, path);
		PreloaderSparks.textures[name].sort(function(a, b){if(a._atlasName < b._atlasName) return 1; else return -1});
	}
};

PreloaderSparks.getSparksTextures = function()
{
 	PreloaderSparks.setTexture('sparks', 'preloader/sparks', AtlasConfig.Sparks, '');
	return this.textures.sparks;
}

PreloaderSparks.removeTextures = function () 
{
	PreloaderSparks.textures['sparks'] = null;
}

export default PreloaderSparks;