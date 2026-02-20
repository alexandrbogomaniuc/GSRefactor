import StarshipBaseView from './StarshipBaseView';
import StarshipFalloutView from './starshipEffects/StarshipFalloutView';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import BattlegroundSpeedUpAnimationView from './starshipEffects/battleground/BattlegroundSpeedUpAnimationView';

class StarshipBlueRocketView extends StarshipBaseView
{
	constructor()
	{
		super();
	}

	//OVERRIDE...
	_generateStarshipBody()
	{
		let l_sprt = new Sprite;
		l_sprt.textures = [StarshipBaseView.getShipBodyTextures()[1]];

		return l_sprt;
	}
	//...OVERRIDE

	//OVERRIDE...
	_addFire()
	{
		let lFire_sprt = super._addFire();
		lFire_sprt.position.set(-4, 65);

		return lFire_sprt;
	}

	_generateStarshipHeatPreFlightSmokeAnimation()
	{
		let l_sprt = super._generateStarshipHeatPreFlightSmokeAnimation();
		l_sprt.position.set(-5, 60);

		return l_sprt;
	}
	//...OVERRIDE

	//OVERRIDE...
	_generateHeat()
	{
		let lHeat_rcdo = new Sprite;
		lHeat_rcdo.textures = [StarshipBaseView.getShipHeatTextures()[1]];
		lHeat_rcdo.position.y = 58;

		return lHeat_rcdo;
	}

	_generateHeatIdleAnimation()
	{
		let l_rctl = new MTimeLine();

		l_rctl.addAnimation(
			this._fHeat_sprt,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0.75, 10],
				[1, 5],
				[0.85, 10],
				[1, 10],
				[0.65, 5],
				[1, 10],
			]);

		return l_rctl;
	}
	//...OVERRIDE

	//OVERRIDE...
	generateFalloutEffect()
	{
		return new StarshipFalloutView([StarshipBaseView.getShipBodyTextures()[1]]);
	}
	//...OVERRIDE

	//OVERRIDE...
	get _preLaunchOffsetPoint()
	{
		return {x: 70, y: -120};
	}
	//...OVERRIDE

	//OVERRIDE...
	_generateBTGSpeedUpAnimationView()
	{
		let l_suav = new BattlegroundSpeedUpAnimationView(1, this);
		return l_suav;
	}
	//...OVERRIDE
}

export default StarshipBlueRocketView