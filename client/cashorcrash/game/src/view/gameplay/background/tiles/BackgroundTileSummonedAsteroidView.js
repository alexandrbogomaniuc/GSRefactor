import BackgroundTileBaseClassView from '../BackgroundTileBaseClassView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../config/AtlasConfig';

const MIN_SCALE = 0.3;
const MISS_TRAJECTORIES_Y = {
	1:-1.2, 
	2:2,
	3:1.5,
	4:-2,
	10:0.5
}

const MISS_TRAJECTORIES_X = {
	1:-1, 
	2:1,
	3:1,
	4:-1,
	10:0
}

const ASSET_SCALE = {
	0:Math.random() * 2,
	1:15 + Math.random() * 15
}
let _explosion_textures = null; 

function _generateExplosionTextures()
{
	if (_explosion_textures) return;

	_explosion_textures = AtlasSprite.getFrames([APP.library.getAsset("game/bg_objects/bg_object_2")], [AtlasConfig.ExploadingAsteroid], "");
}



class BackgroundTileSummonedAsteroidView extends BackgroundTileBaseClassView
{
	constructor(aIndex_int, type, asset)
	{
		super();
		
		console.log(aIndex_int + ", " + type + "," + asset);
		
		
		if(APP.isBattlegroundGame)
		{
			let l_rcdo  = null; 
			if(type==10 || type==11 || type==12){
				let mainAsteroidAssetName = "0";
				if(type == 11)
				{
					mainAsteroidAssetName = "1";
				}
				if(type == 12)
				{
					mainAsteroidAssetName = "2";
				}
				l_rcdo = this._fAsteroidView_sprt = APP.library.getSprite("game/asteroid_" + mainAsteroidAssetName);
				this.paralaxMultiplier = 0.95;
				//l_rcdo.anchor.set(MISS_TRAJECTORIES_X[type], MISS_TRAJECTORIES_Y[type]);	
				l_rcdo.anchor.set(0.5, 0.5);	
			}else{
				if(asset == 2){
					_generateExplosionTextures();
					l_rcdo = new Sprite()
					l_rcdo.textures = _explosion_textures;
					l_rcdo.animationSpeed = 18/60;
					l_rcdo.blendMode = PIXI.BLEND_MODES.ADD;
					l_rcdo.stop();
					this.paralaxMultiplier = 2.3;	
					l_rcdo.scale.set(0.8 + Math.random() * 1);	
					l_rcdo.anchor.set(0.5,  0.5);
					l_rcdo.rotation = Math.round(Math.random()*360);
					
					setTimeout(()=>{
						l_rcdo.play();
					}, Math.round(400 + Math.random() * 200));
				}else{
					console.log("generating asset " + asset);
					l_rcdo = this._fAsteroidView_sprt = APP.library.getSprite("game/bg_objects/bg_object_" + asset);	
					
					if(asset == "1")
					{
						l_rcdo.scale.set(2);	
						l_rcdo.anchor.set(0.5,0.5);	
						this.paralaxMultiplier = 2.9;
					}else{
						l_rcdo.scale.set(1);	
						l_rcdo.anchor.set(0,0);	
						this.paralaxMultiplier = 2.9;	
					}
				}
			}
			
			this.addChild(l_rcdo);
		
		}else
		{
			l_rcdo.anchor.set(0, 1);
			this.addChild(l_rcdo);	
		}
		
		this._fRandomIndex_int = aIndex_int;
		
	}

	  

	get randomIndex()
	{
		return this._fRandomIndex_int;
	}

	randomize(aOffset_num=0)
	{
		return;
	}
}
export default BackgroundTileSummonedAsteroidView;