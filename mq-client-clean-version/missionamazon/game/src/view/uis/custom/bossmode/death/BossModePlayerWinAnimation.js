import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18'
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Sequence from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../../../../shared/src/CommonConstants';
import { DropShadowFilter } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';

class BossModePlayerWinAnimation extends Sprite
{
	static get ON_END_CUPTION_ANIMATION()					{return "ON_END_CUPTION_ANIMATION";}
	constructor()
	{
		super();

		this._fCaption_sprt = null;
		this._fCaptionName_tf = null;
		this._fLightSweep_sprt = null;
		this._fMask_sprt = null;
		this._fPlayerName_str = null;

		this._initCaption();

		this.visible = false;
	}

	set playerName(aPlayerName_str)
	{
		this._fPlayerName_str = aPlayerName_str;
		if (this._fCaptionName_tf)
		{
			this._fCaptionName_tf.text = aPlayerName_str;
			this._fCaptionName_tf.position.x = -this._fCaptionName_tf.textBounds.width/2;
		}
	}

	get playerName()
	{
		return this._fPlayerName_str;
	}

	startAnimation()
	{
		this._startAnimation();
	}
	
	_initCaption()
	{
		this._fCaption_sprt = this.addChild(this._generateCaption());
		let lDropShadowFilter = new DropShadowFilter();
		lDropShadowFilter.alpha = 0.7;
		lDropShadowFilter.angle = 90;
		lDropShadowFilter.distance = 5;
		this._fCaption_sprt.filters = [ lDropShadowFilter ];
	}

	_generateCaption(aOptMainCaption_bl = true)
	{
		let lCaption_sprt = new Sprite();
		let lCaptionTop_cta = lCaption_sprt.addChild(I18.generateNewCTranslatableAsset("TABossModePlayerWin"));
		let lStyle_obj = lCaptionTop_cta.assetContent.style;
		let lName_tf = lCaption_sprt.addChild(new TextField(lStyle_obj));
		let lNameWidth = APP.config.size.width * 0.75;
		lName_tf.textFormat = { fontSize: 80 };
		lName_tf.textFormat = { shortLength: lNameWidth };
		if(this._fPlayerName_str)
		{
			lName_tf.text = this._fPlayerName_str;
		}
		lName_tf.position.x = -lName_tf.textBounds.width/2;

		if (aOptMainCaption_bl)
		{
			this._fCaptionName_tf = lName_tf;
		}

		return lCaption_sprt;
	}

	_startAnimation()
	{
		this.visible = true;
		this._fCaption_sprt.scale.set(0);

		let lScaleSeq_obj = [
			{ tweens: [ {prop: 'scale.x', to: 1.15}, {prop: 'scale.y', to: 1.15} ], duration: 15 * FRAME_RATE },
			{ tweens: [ {prop: 'scale.x', to: 0.9}, {prop: 'scale.y', to: 0.9} ], duration: 10 * FRAME_RATE },
			{ tweens: [], duration: 25 * FRAME_RATE },
			{ tweens: [ {prop: 'scale.x', to: 0.3}, {prop: 'scale.y', to: 0.3} ], duration: 2 * FRAME_RATE },
			{ tweens: [ {prop: 'scale.x', to: 0}, {prop: 'scale.y', to: 0} ], duration: 4 * FRAME_RATE, onfinish: () => { this.visible = false; } }
		];

		this._fLightSweep_sprt = this.addChild(APP.library.getSprite('light_sweep'));
		let lCaption = this._generateCaption(false);
		let lBounds_obj = lCaption.getBounds();
		let l_txtr = PIXI.RenderTexture.create({ width: lBounds_obj.width, height:  lBounds_obj.height});
		APP.stage.renderer.render(lCaption, { renderTexture: l_txtr });
		this._fMask_sprt = new PIXI.Sprite(l_txtr);
		this._fLightSweep_sprt.mask = this._fMask_sprt;
		
		let lWidth_num = this._fCaption_sprt.getLocalBounds().width;
		this._fLightSweep_sprt.position.x = -lWidth_num/2;

		let lSweepSeq_obj = [
			{ tweens: [ {prop: 'position.x', to: lWidth_num/2} ], duration: 28 * FRAME_RATE, onfinish: () => {
				this.emit(BossModePlayerWinAnimation.ON_END_CUPTION_ANIMATION)} }
		];

		Sequence.start(this._fCaption_sprt, lScaleSeq_obj);
		Sequence.start(this._fMask_sprt, lScaleSeq_obj);
		Sequence.start(this._fLightSweep_sprt, lSweepSeq_obj);
	}

	destroy()
	{		
		Sequence.destroy(Sequence.findByTarget(this._fCaption_sprt));
		this._fCaption_sprt && this._fCaption_sprt.destroy();
		this._fCaption_sprt = null;

		Sequence.destroy(Sequence.findByTarget(this._fLightSweep_sprt));
		this._fLightSweep_sprt && this._fLightSweep_sprt.destroy();
		this._fLightSweep_sprt = null;

		Sequence.destroy(Sequence.findByTarget(this._fMask_sprt));
		this._fMask_sprt && this._fMask_sprt.destroy();
		this._fMask_sprt = null;

		this._fPlayerName_str = null;
		
		super.destroy();
	}
}

export default BossModePlayerWinAnimation;