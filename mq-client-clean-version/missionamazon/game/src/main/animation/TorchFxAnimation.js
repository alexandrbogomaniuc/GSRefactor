import AtlasConfig from '../../config/AtlasConfig';
import AtlasSprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class TorchFxAnimation
{
}

TorchFxAnimation.textures = {
	torch: 			null,
	torch_blue: 	null,
	torch_red:		null
}

TorchFxAnimation.setTexture = function (name, imageNames, configs, path) 
{
	if(!TorchFxAnimation.textures[name])
	{
		TorchFxAnimation.textures[name] = [];

		if(!Array.isArray(imageNames)) imageNames = [imageNames];
		if(!Array.isArray(configs)) configs = [configs];

		let assets = [];
		imageNames.forEach(function(item){assets.push(APP.library.getAsset(item))});

		TorchFxAnimation.textures[name] = AtlasSprite.getFrames(assets, configs, path);
		TorchFxAnimation.textures[name].sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}
};

TorchFxAnimation.initTextures = function()
{
	TorchFxAnimation.setTexture('torch', 'common/torch_atlas', AtlasConfig.Torch, '');
	TorchFxAnimation.setTexture('torch_blue', 'common/torch_atlas_blue', AtlasConfig.Torch, '');
	TorchFxAnimation.setTexture('torch_red', 'common/torch_atlas_red', AtlasConfig.Torch, '');
}

export default TorchFxAnimation;