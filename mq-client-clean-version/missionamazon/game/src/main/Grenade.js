import AtlasSprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../config/AtlasConfig';
import CommonEffectsManager from './CommonEffectsManager';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MissEffect from './missEffects/MissEffect';

let Grenade = {
	textures: {
		grenadeBlast: null,
		groundSmoke: null,
		burstSmoke: null
	}
};

let imageNames = [],
	configs = [];

imageNames.push('weapons/GrenadeGun/Grenade_Blast_ADD_atlas');
configs.push(AtlasConfig.GrenadeNew[0]);

Grenade.setTexture = function (name, imageNames, configs, path) {
	if(!Grenade.textures[name]){
		Grenade.textures[name] = [];

		if(!Array.isArray(imageNames)) imageNames = [imageNames];
		if(!Array.isArray(configs)) configs = [configs];

		let assets = [];
		imageNames.forEach(function(item){assets.push(APP.library.getAsset(item))});

		Grenade.textures[name] = AtlasSprite.getFrames(assets, configs, path);
		Grenade.textures[name].sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}
};

Grenade.getTextures = function(){    
	CommonEffectsManager.getGroundSmokeTextures();
	CommonEffectsManager.getDieSmokeUnmultTextures();
	Grenade.setTexture('grenadeBlast', imageNames, configs, 'Grenade_Blast_ADD');
	if (!Grenade.textures['groundSmoke'])
	{
		Grenade.textures['groundSmoke'] = CommonEffectsManager.textures['groundSmoke'];
	}
	if (!Grenade.textures['burstSmoke'])
	{
		Grenade.textures['burstSmoke'] = MissEffect.getSmokeTextures();
	}
};

export default Grenade;