import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import StarshipRedTriangleView from './StarshipRedTriangleView';
import StarshipBlueRocketView from './StarshipBlueRocketView';
import StarshipPeanutView from './StarshipPeanutView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import StarshipBaseView from './StarshipBaseView';

class StarshipsPoolView extends Sprite
{
	static get EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED ()			{ return StarshipBaseView.EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED; }
	static get EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED ()				{ return StarshipBaseView.EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED; }

	static registerStarship( aGameplayOverlayView_rgov )
	{
		StarshipsPoolView.starships_rcsv_arr.push(aGameplayOverlayView_rgov);
	}

	static getStarships()
	{
		if(!StarshipsPoolView.starships_rcsv_arr)
		{
			StarshipsPoolView.starships_rcsv_arr = [];
			StarshipsPoolView.STARSHIP_RED_TRIANGLE = new StarshipRedTriangleView();
			StarshipsPoolView.STARSHIP_BLUE_ROCKET = new StarshipBlueRocketView();
			StarshipsPoolView.STARSHIP_PEANUT = new StarshipPeanutView();
			
			StarshipsPoolView.STARSHIP = StarshipsPoolView.STARSHIP_RED_TRIANGLE;
		}

		return StarshipsPoolView.starships_rcsv_arr;
	}

	constructor()
	{
		super();

		this._fBackgroundContainer_rcdc = this.addChild(new Sprite());
		this._fForegroundContainer_rcdc = this.addChild(new Sprite());
		this._fOverlayContainer_rcdc = this.addChild(new Sprite());
	}

	init()
	{
		let l_rcsv_arr = StarshipsPoolView.getStarships();

		for( let i = 0; i < l_rcsv_arr.length; i++ )
		{
			let l_sv = l_rcsv_arr[i];
			l_sv.on(StarshipBaseView.EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED, this.emit, this);
			l_sv.on(StarshipBaseView.EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED, this.emit, this);
			l_sv.deactivateShip();
			this._fForegroundContainer_rcdc.addChild(l_sv);
		}
	}

	setRandomStarship()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_rcsv_arr = StarshipsPoolView.getStarships();
		let lIndex_int = Math.floor(l_gpi.getPseudoRandomValue(0) * l_rcsv_arr.length);
		
		StarshipsPoolView.STARSHIP = l_rcsv_arr[lIndex_int];
		StarshipsPoolView.STARSHIP.adjust();

		for( let i = 0; i < l_rcsv_arr.length; i++ )
		{
			l_rcsv_arr[i].deactivateShip();
		}
	}

	startWiggleStarship()
	{
		StarshipsPoolView.STARSHIP.startWiggleStarship();
	}

	getBackgroundContainer()
	{
		return this._fBackgroundContainer_rcdc;
	}

	getForegroundContainer()
	{
		return this._fForegroundContainer_rcdc;
	}

	getOverlayContainer()
	{
		return this._fOverlayContainer_rcdc;
	}
}
export default StarshipsPoolView;