import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GlowingSprite from '../../../../ui/GlowingSprite';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class FreeShotsLabelView extends Sprite {

	i_updateFreeShotsCount(aFreeShots_int, aIsAnimationNeeded_bl = false)
	{
		this._updateFreeShotsCount(aFreeShots_int, aIsAnimationNeeded_bl);
	}

	i_animate()
	{
		this._animate();
	}

	show()
	{
		if (!this.visible)
		{
			this._animate();
		}
		super.show();
	}

	hide()
	{
		if (this.visible)
		{
			this._reset();
		}
		super.hide();
	}

	constructor()
	{
		super();

		this._fContainer_sprt = this.addChild(new Sprite);

		this._fCurrentValue_int = undefined;
		this._fTextField_tf = null;
		this._fBaseFreeShotsText_str = null;

		this._init();
	}

	_init()
	{
		this._initTextField();
	}

	_initTextField()
	{
		let lAssetDescriptor_tad = I18.getTranslatableAssetDescriptor(this._freeShotsLabelTranslatableAssetName);
		this._fTextField_tf = this._fContainer_sprt.addChild(I18.generateNewCTranslatableAsset(this._freeShotsLabelTranslatableAssetName));
		this._fTextField_tf.anchor.set(0.5, 0.5);
		this._fTextField_tf.maxWidth = 70;

		this._fBaseFreeShotsText_str = lAssetDescriptor_tad.textDescriptor.content.text;
		return this._fTextField_tf;
	}

	_updateFreeShotsCount(aFreeShots_int, aIsAnimationNeeded_bl = false)
	{
		this._fTextField_tf.text = this._fBaseFreeShotsText_str.replace("/VALUE/", aFreeShots_int);
		if (this._fCurrentValue_int !== aFreeShots_int)
		{
			this._fCurrentValue_int = aFreeShots_int;
			this._animate();
		}
	}

	get _freeShotsLabelTranslatableAssetName()
	{
		return "TAWeaponsSidebarIconFreeShotsLabel";
	}

	_animate()
	{
		this._resetSequences();

		this._tryToStartGlow();

		let lScaleSequence_arr = [
			{
				tweens: [
					{ prop: "scale.x", to: 1.1 },
					{ prop: "scale.y", to: 1.1 }
				],
				duration: 2 * FRAME_RATE
			},
			{
				tweens: [
					{ prop: "scale.x", to: 1 },
					{ prop: "scale.y", to: 1 }
				],
				duration: 10 * FRAME_RATE
			}
		];
		Sequence.start(this._fContainer_sprt, lScaleSequence_arr);
	}

	_tryToStartGlow()
	{
		// [Y]: prohibit glow for low spec deviceS as it needs screnshot capture for glow rendering
		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater && !APP.isMobile)
		{
			if (this._fContainer_sprt.duplicateSprite != null)
			{
				this._fContainer_sprt.i_invalidateDuplicateSprite();
			}
			else
			{
				let lGlowFilterParams_obj = {
					distance: 1,
					innerStrength: 2,
					outerStrength: 0.1,
					glowColor: 0xfbe9bd,
					quality: 1
				}
				this._fContainer_sprt = GlowingSprite.convertIntoGlowingSprite(this._fContainer_sprt, lGlowFilterParams_obj);
			}

			this._fContainer_sprt.duplicateSprite.alpha = 1;
			let lGlowSequence_arr = [
				{
					tweens: [ { prop: "alpha", to: 0.7 } ],
					duration: 2 * FRAME_RATE
				},
				{
					tweens: [{ prop: "alpha", to: 0 }],
					duration: 6 * FRAME_RATE
				}
			];
			Sequence.start(this._fContainer_sprt.duplicateSprite, lGlowSequence_arr);
		}
	}

	_reset()
	{
		this._resetSequences();

		this._fContainer_sprt.scale.set(1);
		if (this._fContainer_sprt.duplicateSprite)
		{
			this._fContainer_sprt.duplicateSprite.alpha = 0;
		}
	}

	_resetSequences()
	{
		Sequence.destroy(Sequence.findByTarget(this));
	}

	destroy()
	{
		this._resetSequences();
		super.destroy();
	}

}

export default FreeShotsLabelView;