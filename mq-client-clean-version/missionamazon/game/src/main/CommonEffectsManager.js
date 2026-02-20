import AtlasSprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../config/AtlasConfig';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

let CommonEffectsManager = {
	textures: {
		groundSmoke: null,
		dieSmokeUnmult: null,
		streak: null
	}
};

CommonEffectsManager.setTexture = function (name, imageNames, configs, path) {
	if(!CommonEffectsManager.textures[name]){
		CommonEffectsManager.textures[name] = [];

		if(!Array.isArray(imageNames)) imageNames = [imageNames];
		if(!Array.isArray(configs)) configs = [configs];

		let assets = [];
		imageNames.forEach(function(item){assets.push(APP.library.getAsset(item))});

		CommonEffectsManager.textures[name] = AtlasSprite.getFrames(assets, configs, path);
		CommonEffectsManager.textures[name].sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}
};

CommonEffectsManager.getGroundSmokeTextures = function() {
	CommonEffectsManager.setTexture('groundSmoke', 'common/u_groundsmoke_unmult', AtlasConfig.CommonEffects[0], 'groundsmoke');
	return this.textures.groundSmoke;
}

CommonEffectsManager.getDieSmokeUnmultTextures = function()
{
	CommonEffectsManager.setTexture('dieSmokeUnmult', 'common/die_smoke_unmult', AtlasConfig.DieSmokeUnmult, '');
	return this.textures.dieSmokeUnmult;
}

CommonEffectsManager.getStreakTextures = function ()
{
	CommonEffectsManager.setTexture('streak', 'common/streak', AtlasConfig.Streak, '');
	return this.textures.streak;
}

export default CommonEffectsManager;