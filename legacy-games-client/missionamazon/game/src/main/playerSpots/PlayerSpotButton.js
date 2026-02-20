import Button from '../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SimpleUIView from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';

class PlayerSpotButton extends SimpleUIView
{
	static get BET_LEVEL_BUTTON_Z_INDEX() { return 27021; }

	startGlowAnimation()
	{
		this._startGlowAnimation();
	}

	constructor(baseAssetName, disabledAssetName, aIsPulsationAllowed_bl = true)
	{
		super(baseAssetName, undefined, true)

		this._fIsPulsationAllowed_bl = aIsPulsationAllowed_bl;
		this._disabledAssetName = disabledAssetName;

		this._baseAssetName = baseAssetName;
		this._fDisabledView_sprt = null;

		this._baseView =this.addChild(APP.library.getSpriteFromAtlas(baseAssetName));

		this._fCircle_spr_arr = [];

		this._enabled = null;
		this.setDisabled();

		this._tryToStartPulsation();
	}

	get buttonWidth()
	{
		return this.disabledView.width;
	}

	get buttonHeigth()
	{
		return this.disabledView.height;
	}

	get disabledView()
	{
		return this._fDisabledView_sprt || (this._fDisabledView_sprt = this._initDisabledView());
	}	

	get enabled()
	{
		return this._enabled;
	}

	set enabled(value)
	{
		value = !!value;

		if (value === this._enabled)
		{
			return;
		}

		if (value)
		{
			this.setEnabled();
		}
		else
		{
			this.setDisabled();
		}
	}

	_initDisabledView()
	{
		if (!this._disabledAssetName)
		{
			return new Sprite;
		}

		let lSprt = APP.library.getSpriteFromAtlas(this._disabledAssetName);
		lSprt.anchor.set(0.5, 0.5)
		return lSprt;
	}

	setEnabled()
	{
		if (this._baseView)
		{
			this._baseView.visible = true;
		}

		if (this._fDisabledView_sprt)
		{
			this._fDisabledView_sprt.visible = false;
		}

		this._enabled = true;

		this._tryToStartPulsation();
	}

	setDisabled()
	{
		this._enabled = false;
		
		let lDisabledView_sprt = this.disabledView;
		let lFirstDisabledView_bln = !lDisabledView_sprt.parent;
		this.addChild(lDisabledView_sprt);

		if (lFirstDisabledView_bln)
		{
			let lDisabledViewLocBnds_rect = lDisabledView_sprt.getLocalBounds();
			let lDisabledViewLeftBorder_num = lDisabledView_sprt.x + lDisabledViewLocBnds_rect.x * lDisabledView_sprt.scale.x;
			lDisabledView_sprt.x = this._fLeftBorder_num - lDisabledViewLeftBorder_num;

			let lDisabledViewTopBorder_num = lDisabledView_sprt.y + lDisabledViewLocBnds_rect.y * lDisabledView_sprt.scale.y;
			lDisabledView_sprt.y = this._fTopBorder_num - lDisabledViewTopBorder_num;
		}		

		lDisabledView_sprt.visible = true;

		this._resetPulsating()

		if (this._baseView)
		{
			this._baseView.visible = false;
		}

		lDisabledView_sprt.x = 0;
		lDisabledView_sprt.y = 0;
	}

	_tryToStartPulsation()
	{
		if (this._enabled && this._fIsPulsationAllowed_bl && !this._fPulseSequence_s)
		{
			this._pulse();
		}
	}

	_pulse(aDelayTime_num)
	{
		aDelayTime_num = +aDelayTime_num || 0;

		this._fPulseSequence_s = Sequence.start(this._baseView, this._pulseSequence, aDelayTime_num);
	}

	_resetPulsating()
	{
		Sequence.destroy(Sequence.findByTarget(this._baseView));

		this._fPulseSequence_s = null;

		this._baseView.scale.set(1);
	}

	get _pulseSequence()
	{
		let lSequence_arr = [
			{ tweens: [{ prop: "scale.x", to: 1.06 }, { prop: "scale.y", to: 1.06 }], duration: 5 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.03 }, { prop: "scale.y", to: 1.03 }], duration: 4 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.00 }, { prop: "scale.y", to: 1.00 }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.06 }, { prop: "scale.y", to: 1.06 }], duration: 5 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.00 }, { prop: "scale.y", to: 1.00 }], duration: 6 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.06 }, { prop: "scale.y", to: 1.06 }], duration: 5 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.03 }, { prop: "scale.y", to: 1.03 }], duration: 4 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.00 }, { prop: "scale.y", to: 1.00 }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.06 }, { prop: "scale.y", to: 1.06 }], duration: 5 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.03 }, { prop: "scale.y", to: 1.03 }], duration: 4 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.06 }, { prop: "scale.y", to: 1.06 }], duration: 4 * FRAME_RATE },
			{ tweens: [], duration: 6 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.03 }, { prop: "scale.y", to: 1.03 }], duration: 5 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.06 }, { prop: "scale.y", to: 1.06 }], duration: 6 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.00 }, { prop: "scale.y", to: 1.00 }], duration: 6 * FRAME_RATE, onfinish: () => { this._onPulseCycleCompleted() } }
		];

		return lSequence_arr;
	}

	_onPulseCycleCompleted()
	{
		this._resetPulsating();

		this._pulse(Utils.random(5, 10, false) * 60 * FRAME_RATE);
	}

	_startGlowAnimation()
	{
		this._resetPulsating();

		let lGlow_spr = this._getGlowView();
		lGlow_spr.scale.set(0.97, 0.97)
		lGlow_spr.alpha = 1;
		lGlow_spr.visible = true;

		lGlow_spr.scaleTo(1.3, 13 * FRAME_RATE, undefined, () => { this._desroyGlow(lGlow_spr) });

		let lAlphaSeq = [
			{ tweens: [], duration: 5 * FRAME_RATE },
			{ tweens: [{ prop: "alpha", to: 0 }], duration: 8 * FRAME_RATE },
		];
		Sequence.start(lGlow_spr, lAlphaSeq);

		let lBaseScaleSeq_arr = [
			{ tweens: [{ prop: "scale.x", to: 0.9 }, { prop: "scale.y", to: 0.9 }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1.06 }, { prop: "scale.y", to: 1.06 }], duration: 7 * FRAME_RATE },
			{ tweens: [{ prop: "scale.x", to: 1 }, { prop: "scale.y", to: 1 }], duration: 2 * FRAME_RATE }
		];
		Sequence.start(this._baseView, lBaseScaleSeq_arr);
	}

	_desroyGlow(aGlow_spr)
	{
		let lId_num = this._fCircle_spr_arr.indexOf(aGlow_spr);
		if (~lId_num)
		{
			this._fCircle_spr_arr.splice(lId_num, 1);
		}
		Sequence.destroy(Sequence.findByTarget(aGlow_spr));
		aGlow_spr && aGlow_spr.destroy();
		aGlow_spr = null;
	}

	_resetGlowAnimation()
	{
		Sequence.destroy(Sequence.findByTarget(this._baseView));

		gv.scale.set(0.99, 0.99)
		gv.alpha = 1;

		gv.visible = false;
	}

	_getGlowView()
	{
		let l_spr = APP.library.getSpriteFromAtlas(this._baseAssetName);
		l_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this.addChild(l_spr);
		l_spr.visible = false;
		this._fCircle_spr_arr.push(l_spr);
		return l_spr;
	}

	destroy()
	{
		this._resetPulsating();

		if (this._fCircle_spr_arr)
		{
			for (let l_sprt of this._fCircle_spr_arr)
			{
				l_sprt && l_sprt.destroy();
				l_sprt = null;
			}

			this._fCircle_spr_arr = null;
		}

		this._fIsPulsationAllowed_bl = null;
		this._disabledAssetName = null;
		this._baseAssetName = null;
		this._fDisabledView_sprt = null;
		this._enabled = null;

		super.destroy();
	}
}

export default PlayerSpotButton