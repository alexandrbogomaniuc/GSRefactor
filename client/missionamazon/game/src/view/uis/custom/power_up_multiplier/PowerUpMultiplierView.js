import { Sequence } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { AtlasSprite, Sprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import AtlasConfig from '../../../../config/AtlasConfig';
import CriticalHitMultiplierView from '../critical_hit/CriticalHitMultiplierView';

class PowerUpMultiplierView extends CriticalHitMultiplierView
{
	static get EVENT_ON_APPEAR_ANIMATION_COMPLETED()			{ return 'onAppearAnimationCompleted'; }

	i_startWiggleAnimation()
	{
		this._startWiggleIdleAnimation();
	}

	i_startAppearAnimation(aStartPosition_obj, aFinalPosition_obj)
	{
		this._startAppearAnimation(aStartPosition_obj, aFinalPosition_obj);
	}

	get multiplier()
	{
		return this._fValue_int || 0;
	}

	constructor()
	{
		super();

		this._fValue_int = null;
		this._fOrangeFlare_spr = null;
	}

	__init()
	{
		this._fFlare_spr = this.__fContainer_sprt.addChild(APP.library.getSpriteFromAtlas("common/multiplicator_background_flare"));
		this._fFlare_spr.scale.set(2.5);
		this._fFlare_spr.blendMode = PIXI.BLEND_MODES.ADD;

		super.__init();
		this.__fValue_bt.scale.set(0.8);
		
		this._fBonusText_cta = this.__fContainer_sprt.addChild(new I18.generateNewCTranslatableAsset("TABattleGroundPowerUpMultiplicatorBonus"));
		let lBounds_obj = this.__fValue_bt.getBounds();
		this._fBonusText_cta.position.set(0, lBounds_obj.height/2+2);
		this._fBonusText_cta.scale.set(0.5);

		this.i_startWiggleAnimation();
	}

	__getAssetName()
	{
		return "powerup_multiplicator/white_numbers";
	}

	__getAssetAtlas()
	{
		return AtlasConfig.WhiteNumbers;
	}

	__alignValue()
	{
		super.__alignValue();

		if (this.__fValue_bt && this._fBonusText_cta)
		{
			let lBounds_obj = this.__fValue_bt.getBounds();
			lBounds_obj && this._fBonusText_cta.position.set(0, lBounds_obj.height/2+4);
		}
	}

	__getLetterSpacing()
	{
		return -6;
	}

	__setValue(aValue_num)
	{
		this._fValue_int = aValue_num;

		let lText_str = "x" + String(aValue_num);
		super.__setValue(lText_str);
	}

	_startWiggleIdleAnimation()
	{
		if (this.__fContainer_sprt)
		{
			let l_seq = [
				{
					tweens: [
						{ prop: 'position.x', to: Utils.getRandomWiggledValue(-2, 2), ease: Easing.quadratic.easeIn},
						{ prop: 'position.y', to: Utils.getRandomWiggledValue(-2, 2), ease: Easing.quadratic.easeOut }
					],
					duration: 20 * FRAME_RATE,
					onfinish: () =>
					{
						this._startWiggleIdleAnimation();
					}
				}
			];

			Sequence.start(this.__fContainer_sprt, l_seq);
		}
	}

	_startAppearAnimation(aStartPosition_obj, aFinalPosition_obj)
	{
		this.position = aStartPosition_obj;

		const lScaleSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 2.5 }, { prop: 'scale.y', to: 2.5 }], duration: 6 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 2 }, { prop: 'scale.y', to: 2 }], duration: 4 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 2.14 }, { prop: 'scale.y', to: 2.14 }], duration: 8 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 1 }, { prop: 'scale.y', to: 1 }], duration: 8 * FRAME_RATE, ease: Easing.exponential.easeIn, onfinish: () => {
				Sequence.destroy(Sequence.findByTarget(this));
				this.__fContainer_sprt && this.__fContainer_sprt.hide();
				this._startFinalAnimation();
			}},
		];

		const lPositionSeq_arr = [
			{ tweens: [], duration: 18 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: aFinalPosition_obj.x}, { prop: 'position.y', to: aFinalPosition_obj.y}], duration: 8 * FRAME_RATE, ease: Easing.exponential.easeIn},
		];

		Sequence.start(this, lScaleSeq_arr);
		Sequence.start(this, lPositionSeq_arr);
	}

	_startFinalAnimation()
	{
		if (!APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this.destroy();
			return;
		}

		// sparkles ...
		let lSparkles_spr = this.addChild(new Sprite());
		lSparkles_spr.textures = AtlasSprite.getFrames([APP.library.getAsset("treasures/sparkles_0"), APP.library.getAsset("treasures/sparkles_1")], [AtlasConfig.Sparkles0, AtlasConfig.Sparkles1], "");
		lSparkles_spr.blendMode = PIXI.BLEND_MODES.ADD;
		lSparkles_spr.pivot.set(348, 0);
		lSparkles_spr.animationSpeed = 0.6;

		this._fSparkles_spr = lSparkles_spr;

		this._fSparkles_spr.on('animationend', () => {
			this._fSparkles_spr && this._fSparkles_spr.destroy();
			this._tryToCompleteAppearAnimation();
		});

		// ...sparkles

		// orange flare...
		let lTopOrangeFlare_sprt = this.addChild(APP.library.getSpriteFromAtlas("common/orange_flare"));
		lTopOrangeFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lTopOrangeFlare_sprt.zIndex = this.zIndex + 1;
		lTopOrangeFlare_sprt.scale.set(0, 0);

		this._fOrangeFlare_spr = lTopOrangeFlare_sprt;

		let lTopFlareSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 5},	{prop: 'scale.y', to: 2}],	duration: 3*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 2},	{prop: 'scale.y', to: 3}],	duration: 3*FRAME_RATE, onfinish: () => {
				// this._fSparkles_spr.play();
			}},
			{tweens: [{prop: 'scale.x', to: 0},		{prop: 'scale.y', to: 0}], duration: 4*FRAME_RATE, onfinish: () => {
				Sequence.destroy(Sequence.findByTarget(this._fOrangeFlare_spr));
				this._fOrangeFlare_spr && this._fOrangeFlare_spr.destroy();
				this._fOrangeFlare_spr = null;
				this._tryToCompleteAppearAnimation();
			}}
		];
		// ...orange flare

		Sequence.start(lTopOrangeFlare_sprt, lTopFlareSeq_arr);
		this._fSparkles_spr.play();
	}

	_tryToCompleteAppearAnimation()
	{
		let lFlareSequence_arr = Sequence.findByTarget(this._fOrangeFlare_spr);
		if (
			(this._fSparkles_spr && this._fSparkles_spr.playing) ||
			(this._fOrangeFlare_spr && lFlareSequence_arr.length > 0)
		)
		{
			return
		}
		else
		{
			this.emit(PowerUpMultiplierView.EVENT_ON_APPEAR_ANIMATION_COMPLETED)
		}
	}

	destroy()
	{
		this.__fContainer_sprt && Sequence.destroy(Sequence.findByTarget(this.__fContainer_sprt));
		this._fBonusText_cta = null;

		Sequence.destroy(Sequence.findByTarget(this));

		this._fOrangeFlare_spr && Sequence.destroy(Sequence.findByTarget(this._fOrangeFlare_spr));
		this._fOrangeFlare_spr = null;

		this._fSparkles_spr && this._fSparkles_spr.destroy();
		this._fSparkles_spr = null;

		super.destroy();
	}
}

export default PowerUpMultiplierView;