import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';

const INDENT_X = 150;

class CalloutView extends SimpleUIView
{
	static get EVENT_ON_PRESENTATION_ENDED () { return "onPresentationEnded" };

	startCallout()
	{
		this._showCallout();
	}

	stopCallout()
	{
		this._hideCallout();
	}

	constructor()
	{
		super();

		this._baseContainer = null;
		this._panelContainer = null;
		this._messageContainer = null;
		this._FXContainer = null;
		this._additionalContainer = null;

		this.fStartSweepX_num = -75;
		this.fEndSweepX_num = 120;
		this.fCaptionScale = 0.8;
		this.fMaskX = 150;
		this.fMaskY = -10;

		this._initCalloutView();
	}

	setCaption(firstCaptionId, secondCaptionId)
	{
		this._setCaption(firstCaptionId, secondCaptionId);
	}

	setCaptionScale(captionScale = 0.8)
	{
		this._setCaptionScale(captionScale);
	}

	setMaskPosition(maskX = 50, maskY = -10)
	{
		this._setMaskPosition(maskX, maskY);
	}

	setSweepBounds(leftBound = -75, rightBound = 120)
	{
		this._setSweepBounds(leftBound, rightBound);
	}

	_initCalloutView()
	{
		this._addContainers();

		this._addCalloutBase();
	}

	_addContainers()
	{
		let baseContainer = this._baseContainer = this.addChild(new Sprite());
		baseContainer.position.set(APP.config.size.width + INDENT_X, 100);

		let panelContainer = this._panelContainer = this.addChild(new Sprite());
		panelContainer.zIndex = 1;
		this._baseContainer.addChild(panelContainer);

		let additionalContainer = this._additionalContainer = this.addChild(new Sprite());
		this._baseContainer.addChild(additionalContainer);
		additionalContainer.x = -100;

		let messageContainer = this._messageContainer = this.addChild(new Sprite());
		messageContainer.zIndex = 2;
		this._additionalContainer.addChild(messageContainer);

		let fXContainer = this._FXContainer = this.addChild(new Sprite());
		fXContainer.zIndex = 3;
		this._baseContainer.addChild(fXContainer);
	}

	_addCalloutBase()
	{
		let panelContainer = this._panelContainer;
		let lPanel_spr = APP.library.getSprite("callouts/callout_panel");
		lPanel_spr.position.set(23, 0);
		panelContainer.addChild(lPanel_spr);
	}

	//MESSAGE...
	_setCaption(firstCaptionId, secondCaptionId)
	{
		this._messageContainer.destroyChildren();

		this._addFirstLineCaption(firstCaptionId);
		this._addSecondLineCaption(secondCaptionId);
	}

	_addFirstLineCaption(firstCaptionId)
	{
		let messageContainer = this._messageContainer;
		let lCaption_spr = I18.generateNewCTranslatableAsset(firstCaptionId);
		lCaption_spr.position.set(this.fMaskX, -60);
		messageContainer.addChild(lCaption_spr);
	}

	_addSecondLineCaption(secondCaptionId)
	{
		let messageContainer = this._messageContainer;
		let lCaption_spr = I18.generateNewCTranslatableAsset(secondCaptionId);
		lCaption_spr.position.set(this.fMaskX, -60);
		messageContainer.addChild(lCaption_spr);
	}

	_setCaptionScale(captionScale)
	{
		this.fCaptionScale = captionScale;
	}
	//...MESSAGE

	//ANIMATION...
	get _profilingInfo()
	{
		return APP.profilingController.info;
	}

	_showCallout()
	{
		let l_seq = Sequence.findByTarget(this._baseContainer);
		if(l_seq)
		{
			Sequence.destroy(l_seq);
		}
		this._baseContainer.visible = true;
		this._baseContainer.position.x = APP.config.size.width + INDENT_X;
		l_seq = [{tweens: [{ prop: "position.x", to: APP.config.size.width - INDENT_X }],
			duration: 300,
			ease: Easing.back.easeOut,
			onfinish: () =>
			{
				this._initLightSweep();
			}
		}];

		Sequence.start(this._baseContainer, l_seq);

	}

	_onShowTimerCompleted()
	{
		this._hideCallout();
	}

	_hideCallout()
	{
		let l_seq = Sequence.findByTarget(this._baseContainer);
		if(l_seq)
		{
			Sequence.destroy(l_seq);
		}
		let hide = [{
			tweens: [{ prop: "position.x", to: APP.config.size.width + INDENT_X}],
			duration: 300,
			ease: Easing.linear.easeIn,
			onfinish: () =>
			{
				this._baseContainer.visible = false;
				this._onCalloutAnimationCompleted();
			}
		}];

		Sequence.start(this._baseContainer, hide);
	}
	_onCalloutAnimationCompleted()
	{
		this.emit(CalloutView.EVENT_ON_PRESENTATION_ENDED);
	}

	_initLightSweep()
	{
		if (this._fSweep_sprt)
		{
			this._destroyAnimation(this._fSweep_sprt);
		}

		if (this._profilingInfo.isVfxProfileValueMediumOrGreater)
		{
			this._fSweep_sprt = this._FXContainer.addChild(APP.library.getSprite("light_sweep"));
			this._fSweep_sprt.scale.set(0.5);
			this._fSweep_sprt.alpha = 0.8;
			this._fSweep_sprt.blendMode = PIXI.BLEND_MODES.ADD;

			var lBounds_obj = this._messageContainer.getBounds();
			var l_txtr = PIXI.RenderTexture.create({ width: lBounds_obj.width, height:  lBounds_obj.height});
			APP.stage.renderer.render(this._messageContainer, { renderTexture: l_txtr });

			let lMask_sprt = new PIXI.Sprite(l_txtr);
			this._fSweep_sprt.mask = lMask_sprt;
			this._additionalContainer.addChild(lMask_sprt);
		}
		else
		{
			this._fSweep_sprt = this._FXContainer.addChild(new Sprite);
		}

		this._fSweep_sprt.position.set(this.fStartSweepX_num, -10);

		let l_seq = [{ tweens: [{
			prop: 'position.x', to: this.fEndSweepX_num },],
			duration: 90 * FRAME_RATE,
			onfinish: () =>
			{
				this._destroyAnimation(this._fSweep_sprt);
				this._onShowTimerCompleted();
			}
		}];
		Sequence.start(this._fSweep_sprt, l_seq);
	}

	_destroyAnimation(aAnimation_spr)
	{
		Sequence.destroy(Sequence.findByTarget(aAnimation_spr));
		aAnimation_spr && aAnimation_spr.destroy();
		aAnimation_spr = null;

		this._FXContainer.destroyChildren();
	}

	_setSweepBounds(firstCaptionId, secondCaptionId)
	{
		this.fStartSweepX_num = firstCaptionId;
		this.fEndSweepX_num = secondCaptionId;
	}

	_setMaskPosition(maskX, maskY)
	{
		this.fMaskX = maskX;
		this.fMaskY = maskY;
	}
	//...ANIMATION

	destroy()
	{
		this._destroyAnimation(this._baseContainer);
		this._destroyAnimation(this._fSweep_sprt);

		this._baseContainer = null;
		this._panelContainer = null;
		this._messageContainer = null;
		this._FXContainer = null;
		this._additionalContainer = null;

		super.destroy();
	}
}

export default CalloutView;