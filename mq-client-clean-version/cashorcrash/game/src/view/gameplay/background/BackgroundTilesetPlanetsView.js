import BackgroundTilesetBaseClassView from './BackgroundTilesetBaseClassView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../config/AtlasConfig';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class BackgroundTilesetPlanetsView extends BackgroundTilesetBaseClassView
{
	constructor()
	{
		super(0);

		this._fPlanetMarse_rcdo = null;
		this._fPlanetMoon_rcdo = null;
		this._fPlanetGreen_rcdo = null;
		this._fPlanetOrange_rcdo = null;
		this._fPlanetBlue_rcdo = null;
		this._fSmallPlanets_rcdo_arr = [];
		this._fBigPlanets_rcdo_arr = [];

		this._fSmallPlanetIndex_int = undefined;
		this._fBigPlanetIndex_int = undefined;


		//PLANETS...
		//MARSE...
		let l_rcdo = new Sprite;
		l_rcdo.textures = [BackgroundTilesetPlanetsView.getPlanetTextures()[1]];
		l_rcdo.anchor.set(0.5, 0.5);
		l_rcdo.position.set(10, -100);
		this._fBigPlanets_rcdo_arr.push(l_rcdo);
		this._fPlanetMarse_rcdo = this.addChild(l_rcdo);
		//...MARSE

		//ORANGE...
		l_rcdo = new Sprite;
		l_rcdo.textures = [BackgroundTilesetPlanetsView.getPlanetTextures()[3]];
		l_rcdo.anchor.set(0.5, 0.5);
		l_rcdo.position.set(10, -100);
		this._fBigPlanets_rcdo_arr.push(l_rcdo);
		this._fPlanetGreen_rcdo = this.addChild(l_rcdo);
		//...ORANGE

		//BLUE...
		l_rcdo = new Sprite;
		l_rcdo.textures = [BackgroundTilesetPlanetsView.getPlanetTextures()[4]];
		l_rcdo.anchor.set(0.5, 0.5);
		l_rcdo.position.set(100, -250);
		this._fBigPlanets_rcdo_arr.push(l_rcdo);
		this._fPlanetBlue_rcdo = this.addChild(l_rcdo);
		//...BLUE

		//MOON...
		l_rcdo = new Sprite;
		l_rcdo.textures = [BackgroundTilesetPlanetsView.getPlanetTextures()[0]];
		l_rcdo.anchor.set(0.5, 0.5);
		l_rcdo.position.set(-170, 0);
		this._fSmallPlanets_rcdo_arr.push(l_rcdo);
		this._fPlanetMoon_rcdo = this.addChild(l_rcdo);
		//...MOON

		//GREEN...
		l_rcdo = new Sprite;
		l_rcdo.textures = [BackgroundTilesetPlanetsView.getPlanetTextures()[2]];
		l_rcdo.anchor.set(0.5, 0.5);
		l_rcdo.position.set(-170, 0);
		this._fSmallPlanets_rcdo_arr.push(l_rcdo);
		this._fPlanetGreen_rcdo = this.addChild(l_rcdo);
		//...GREEN
		//...PLANETS

		this.setRandomPlanets();
	}

	setRandomPlanets()
	{
		let lSmallPlanets_rcdo_arr = this._fSmallPlanets_rcdo_arr;
		let lBigPlanets_rcdo_arr = this._fBigPlanets_rcdo_arr;
		let lPlanetIndex_int = 0;

		let lGameplayInfo_pgi = APP.gameController.gameplayController.info;

		lPlanetIndex_int = Math.trunc
		(
			lGameplayInfo_pgi.getPseudoRandomValue(1) * 
			lSmallPlanets_rcdo_arr.length + 1
		) - 1;

		for( let i = 0; i < lSmallPlanets_rcdo_arr.length; i++ )
		{
			lSmallPlanets_rcdo_arr[i].visible = (i === lPlanetIndex_int);
		}
		this._fSmallPlanetIndex_int = lPlanetIndex_int;

		lPlanetIndex_int = Math.trunc
		(
			lGameplayInfo_pgi.getPseudoRandomValue(2) * 
			lBigPlanets_rcdo_arr.length + 1
		) - 1;

		for( let i = 0; i < lBigPlanets_rcdo_arr.length; i++ )
		{
			lBigPlanets_rcdo_arr[i].visible = (i === lPlanetIndex_int);
		}
		this._fBigPlanetIndex_int = lPlanetIndex_int;
	}

	get smallPlanetIndex()
	{
		return this._fSmallPlanetIndex_int;
	}

	get bigPlanetIndex()
	{
		return this._fBigPlanetIndex_int;
	}

	//OVERRIDE...
	getInitialPositionX()
	{
		return GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width;
	}
	//...OVERRIDE

	//OVERRIDE...
	getInitialPositionY()
	{
		return 0;
	}
	//...OVERRIDE

	//OVERRIDE...
	getOffsetPerMultiplierX()
	{
		return -75;
	}
	//...OVERRIDE

	//OVERRIDE...
	getOffsetPerMultiplierY()
	{
		return 115;
	}
	//...OVERRIDE

	//OVERRIDE...
	getOffsetPerPreLaunchX()
	{
		return -172;
	}

	getOffsetPerPreLaunchY()
	{
		return 271;
	}
	//...OVERRIDE
}
export default BackgroundTilesetPlanetsView;

BackgroundTilesetPlanetsView.getPlanetTextures = function()
{
	if (!BackgroundTilesetPlanetsView.planet_textures)
	{
		BackgroundTilesetPlanetsView.planet_textures = [];

		BackgroundTilesetPlanetsView.planet_textures = AtlasSprite.getFrames([APP.library.getAsset('game/bg_assets')], [AtlasConfig.BackgroundAssets], 'planet');
		BackgroundTilesetPlanetsView.planet_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return BackgroundTilesetPlanetsView.planet_textures;
}