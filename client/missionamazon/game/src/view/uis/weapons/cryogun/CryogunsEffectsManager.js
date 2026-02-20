import AtlasConfig from '../../../../config/AtlasConfig';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

let CryogunsEffectsManager = {
	textures: {
		iceExplode: null		
	}
};

CryogunsEffectsManager.setTexture = function (name, imageNames, configs, path) {
	if(!CryogunsEffectsManager.textures[name]){
		CryogunsEffectsManager.textures[name] = [];

		if(!Array.isArray(imageNames)) imageNames = [imageNames];
		if(!Array.isArray(configs)) configs = [configs];

		let assets = [];
		imageNames.forEach(function(item){assets.push(APP.library.getAsset(item))});

		CryogunsEffectsManager.textures[name] = AtlasSprite.getFrames(assets, configs, path);
		CryogunsEffectsManager.textures[name].sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}
};

CryogunsEffectsManager.getIceExplodeTextures = function()
{
	CryogunsEffectsManager.setTexture('iceExplode', 'weapons/Cryogun/ice_explode', AtlasConfig.IceExplode, '');
	return this.textures.iceExplode;
}

export default CryogunsEffectsManager;