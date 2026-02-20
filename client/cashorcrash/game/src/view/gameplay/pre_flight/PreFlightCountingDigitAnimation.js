import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class PreFlightCountingDigitAnimation extends Sprite
{
	static get SUPPORTED_VALUES()
	{
		return {
					THREE: "3",
					TWO: "2",
					ONE: "1",
					GO: "GO",
				}
	}

	constructor(aDigitType_str)
	{
		super();

		this._fContentContainer_sprt = this.addChild(new Sprite);

		let lView_obj = this._generateView(aDigitType_str);

		this._fBaseView_sprt = this._fContentContainer_sprt.addChild(lView_obj.base);
		this._fGlowView_sprt = this._fContentContainer_sprt.addChild(lView_obj.glow);

		//ANIMATION...
		this._fAnimation_mtl = this._generateAnimation(aDigitType_str);

		this._fAnimationTotalDuration_num = this._fAnimation_mtl.getTotalDurationInMilliseconds();
		//...ANIMATION
	}

	_generateView(aDigitType_str)
	{
		let lBaseView_sprt = new Sprite();
		let lGlowView_sprt = new Sprite();

		switch (aDigitType_str)
		{
			case PreFlightCountingDigitAnimation.SUPPORTED_VALUES.THREE:
				lBaseView_sprt = APP.library.getSprite("game/battleground/final_count/3");
				lGlowView_sprt = APP.library.getSprite("game/battleground/final_count/3_glow");
				break;
			case PreFlightCountingDigitAnimation.SUPPORTED_VALUES.TWO:
				lBaseView_sprt = APP.library.getSprite("game/battleground/final_count/2");
				lGlowView_sprt = APP.library.getSprite("game/battleground/final_count/2_glow");
				break;
			case PreFlightCountingDigitAnimation.SUPPORTED_VALUES.ONE:
				lBaseView_sprt = APP.library.getSprite("game/battleground/final_count/1");
				lGlowView_sprt = APP.library.getSprite("game/battleground/final_count/1_glow");
				break;
			case PreFlightCountingDigitAnimation.SUPPORTED_VALUES.GO:
				lBaseView_sprt = I18.generateNewCTranslatableAsset("TABattlegroundFinalCountingGo");
				lBaseView_sprt.position.x = 120;
				lGlowView_sprt = I18.generateNewCTranslatableAsset("TABattlegroundFinalCountingGoGlow");
				lGlowView_sprt.position.x = 120
				break;
		}

		return { base: lBaseView_sprt, glow: lGlowView_sprt };
	}

	_generateAnimation(aDigitType_str)
	{
		let l_mtl = new MTimeLine();

		if (aDigitType_str === PreFlightCountingDigitAnimation.SUPPORTED_VALUES.GO)
		{
			l_mtl.addAnimation(
				this._fContentContainer_sprt,
				MTimeLine.SET_SCALE,
				0,
				[
					[1.29, 17, MTimeLine.EASE_IN_OUT]
				]
			);

			l_mtl.addAnimation(
				this._fContentContainer_sprt,
				MTimeLine.SET_ALPHA,
				1,
				[
					9,
					[0, 8]
				]
			);

			l_mtl.addAnimation(
				this._fGlowView_sprt,
				MTimeLine.SET_ALPHA,
				1,
				[
					4,
					[0, 3, MTimeLine.EASE_OUT]
				]
			);

			return l_mtl;
		}

		l_mtl.addAnimation(
			this._fContentContainer_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				[1, 7, MTimeLine.EASE_IN],
				[1.211, 2],
				[1, 2],
				18,
				[1.211, 2],
				[1, 2],
				[0, 7, MTimeLine.EASE_OUT]
			]
		);

		l_mtl.addAnimation(
			this._fGlowView_sprt,
			MTimeLine.SET_ALPHA,
			1,
			[
				4,
				[0, 3, MTimeLine.EASE_OUT],
				23,
				[1, 4, MTimeLine.EASE_IN]
			]
		);

		return l_mtl;
	}

	adjust(aAnimMS_num)
	{
		let lMS_int = Math.max(aAnimMS_num, 0);
		this._fAnimation_mtl.windToMillisecond(lMS_int);
	}
}
export default PreFlightCountingDigitAnimation;