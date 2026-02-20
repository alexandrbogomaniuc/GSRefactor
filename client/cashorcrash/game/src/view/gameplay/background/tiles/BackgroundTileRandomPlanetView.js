import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import BackgroundTilesetPlanetsView from '../BackgroundTilesetPlanetsView';

const PLANET_VIEWS_AMOUNT = 5;

class BackgroundTileRandomPlanetView extends Sprite
{
	static get PLANET_VIEWS_AMOUNT()
	{
		return PLANET_VIEWS_AMOUNT;
	}

	constructor(aBaseIndex_int)
	{
		super();

		this._fPlanets_sprt_arr = [];

		for( let i = 0; i < PLANET_VIEWS_AMOUNT; i++ )
		{
			let l_sprt = new Sprite;
			l_sprt.textures = [BackgroundTilesetPlanetsView.getPlanetTextures()[i]];
			l_sprt.anchor.set(0, 1);
			l_sprt.scale.set(0.5);
			this._fPlanets_sprt_arr[i] = this.addChild(l_sprt);
			l_sprt.visible = false;
		}

		this._fBaseIndex_int = aBaseIndex_int;
		this._fCurrentPlanetIndex_int = undefined;
	}

	get currentPlanetIndex()
	{
		return this._fCurrentPlanetIndex_int;
	}

	randomize(aOffset_num=0, aExcludeIndexes_int_arr=[])
	{
		if (!aExcludeIndexes_int_arr) aExcludeIndexes_int_arr = [];
		if(!Array.isArray(aExcludeIndexes_int_arr)) aExcludeIndexes_int_arr = [aExcludeIndexes_int_arr];

		let l_gpi = APP.gameController.gameplayController.info;
		let lPlanetsCount_int = this._fPlanets_sprt_arr.length;

		let lFilteredPlanetIndexes_int_arr = [];
		for( let i = 0; i < lPlanetsCount_int; i++ )
		{
			if (aExcludeIndexes_int_arr.indexOf(i) >= 0)
			{
				continue;
			}
			else
			{
				lFilteredPlanetIndexes_int_arr.push(i);
			}
		}

		let lFilteredPlanetsCount_int = lFilteredPlanetIndexes_int_arr.length;
		let lFilteredPlanetIndex_int = Math.trunc(l_gpi.getPseudoRandomValue(this._fBaseIndex_int+aOffset_num) * lFilteredPlanetsCount_int);
		
		let lPlanetIndex_int = lFilteredPlanetsCount_int > 0 ? lFilteredPlanetIndexes_int_arr[lFilteredPlanetIndex_int] : 0;

		this._fCurrentPlanetIndex_int = lPlanetIndex_int;

		for( let i = 0; i < lPlanetsCount_int; i++ )
		{
			this._fPlanets_sprt_arr[i].visible = (lPlanetIndex_int === i);
		}
	}
}
export default BackgroundTileRandomPlanetView;