import AtlasSprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../config/AtlasConfig';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

let DefaultWeaponsShotEffects = {
	turret_1: null,
	turret_2: null,
	turret_3: null,
	turret_4: null,
	turret_5: null
};

let imageNames = [],
	configs = []	


imageNames.push('weapons/DefaultGun/shot_effects_atlas_0');
imageNames.push('weapons/DefaultGun/shot_effects_atlas_1');

configs.push(AtlasConfig.DefaultWeaponsShotEffects0);
configs.push(AtlasConfig.DefaultWeaponsShotEffects1);

DefaultWeaponsShotEffects.setTexture = function (name, imageNames, configs, path) {
	if(!DefaultWeaponsShotEffects[name]){
		DefaultWeaponsShotEffects[name] = [];

		if(!Array.isArray(imageNames)) imageNames = [imageNames];
		if(!Array.isArray(configs)) configs = [configs];

		let assets = [];
		imageNames.forEach(function(item){assets.push(APP.library.getAsset(item))});

		DefaultWeaponsShotEffects[name] = AtlasSprite.getFrames(assets, configs, path);
		DefaultWeaponsShotEffects[name].sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}
};

DefaultWeaponsShotEffects.getTextures = function(){
	DefaultWeaponsShotEffects.setTexture('turret_1', imageNames, configs, 'turret_1');
	DefaultWeaponsShotEffects.setTexture('turret_2', imageNames, configs, 'turret_2');
	DefaultWeaponsShotEffects.setTexture('turret_3', imageNames, configs, 'turret_3');
	DefaultWeaponsShotEffects.setTexture('turret_4', imageNames, configs, 'turret_4');
	DefaultWeaponsShotEffects.setTexture('turret_5', imageNames, configs, 'turret_5');
};

export default DefaultWeaponsShotEffects;