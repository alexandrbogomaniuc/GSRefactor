import StarshipBaseView from './StarshipBaseView';
import StarshipFalloutView from './starshipEffects/StarshipFalloutView';
import StarshipRedTriangleHeatPreFlightSmokeAnimation from './starshipEffects/StarshipRedTriangleHeatPreFlightSmokeAnimation';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import BattlegroundSpeedUpAnimationView from './starshipEffects/battleground/BattlegroundSpeedUpAnimationView';

class StarshipRedTriangleView extends StarshipBaseView
{
	constructor()
	{
		super();
	}

	//OVERRIDE...
	_generateStarshipBody()
	{
		let l_sprt = new Sprite;
		l_sprt.textures = [StarshipBaseView.getShipBodyTextures()[0]];

		return l_sprt;
	}
	//...OVERRIDE

	//OVERRIDE...
	_addFire()
	{
		let lFire_sprt = new Sprite();

		let lFireView_sprt = lFire_sprt.addChild(this._generateFireView());
		lFireView_sprt.position.set(-2, 50);
		lFireView_sprt.scale.set(2*0.8);
		
		lFireView_sprt = lFire_sprt.addChild(this._generateFireView());
		lFireView_sprt.position.set(-36, 35);
		lFireView_sprt.scale.set(2*0.2);

		lFireView_sprt = lFire_sprt.addChild(this._generateFireView());
		lFireView_sprt.position.set(35, 35);
		lFireView_sprt.scale.set(2*0.2);
		
		this._fShakeContainer_sprt.addChild(lFire_sprt);

		return lFire_sprt;
	}

	_generateStarshipHeatPreFlightSmokeAnimation()
	{
		let l_sprt = new StarshipRedTriangleHeatPreFlightSmokeAnimation();
		l_sprt.position.set(-5, 50);

		return l_sprt;
	}
	//...OVERRIDE

	//OVERRIDE...
	_generateHeat()
	{
		let lHeat_rcdo = new Sprite;
		lHeat_rcdo.textures = [StarshipBaseView.getShipHeatTextures()[0]];
		lHeat_rcdo.position.y = 47;
		lHeat_rcdo.blendMode = PIXI.BLEND_MODES.ADD;

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
				[0.85, 10],
				[1, 5],
				[0.95, 10],
				[1, 10],
				[0.75, 5],
				[1, 10],
			]);

		return l_rctl;
	}
	//...OVERRIDE

	//OVERRIDE...
	generateFalloutEffect()
	{
		return new StarshipFalloutView([StarshipBaseView.getShipBodyTextures()[0]]);
	}
	//...OVERRIDE

	//OVERRIDE...
	get _preLaunchOffsetPoint()
	{
		return {x: 75, y: -100};
	}
	//...OVERRIDE

	//OVERRIDE...
	_generateBTGSpeedUpAnimationView()
	{
		let l_suav = new BattlegroundSpeedUpAnimationView(0, this);
		return l_suav;
	}
	//...OVERRIDE
}

export default StarshipRedTriangleView