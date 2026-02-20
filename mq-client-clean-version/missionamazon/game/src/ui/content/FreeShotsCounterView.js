import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import PathTween from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/PathTween';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GlowingSprite from './../GlowingSprite';

let TEMPLATE_VALUE = "/VALUE/"

class FreeShotsCounterView extends Sprite {

	static i_getWidth()
	{
		let lContainer_sprt = new Sprite();
		let caption = I18.generateNewCTranslatableAsset("TAAwardedFreeShotsCounterLabel");
		let value = I18.generateNewCTranslatableAsset("TAAwardedFreeShotsCounterValue");
		let ending = I18.generateNewCTranslatableAsset("TAAwardedFreeShotsCounterLabelEndingOpt");
		lContainer_sprt.addChild(caption);
		lContainer_sprt.addChild(value);
		lContainer_sprt.addChild(ending);
		return lContainer_sprt.getBounds().width;
	}

	i_startGlowSequence(aGlowSeq_arr)
	{
		this._fContainer_sprt = GlowingSprite.convertIntoGlowingSprite(this._fContainer_sprt);
		Sequence.start(this._fContainer_sprt.duplicateSprite, aGlowSeq_arr);
	}

	i_startFlying(aStartPos_pt)
	{
		APP.soundsController.play('free_shot');
		this.position.set(aStartPos_pt.x, aStartPos_pt.y);
		this.rotation = Utils.gradToRad(-8.4 - 25);

		const BASE_SCALE = 2;

		this.scale.set(BASE_SCALE * 0.557);

		let rotationSequence = [
			{
				tweens: [],
				duration: FRAME_RATE*11
			},
			{
				tweens: [{ prop: "rotation", to: Utils.gradToRad(21.6 - 25)}],
				duration: FRAME_RATE*14
			},
			{
				tweens: [{ prop: "rotation", to: Utils.gradToRad(39.8 - 25)}],
				duration: FRAME_RATE * 6
			}
		];

		let scaleSequence = [
			{
				tweens: [],
				duration: FRAME_RATE * 7
			},
			{
				tweens: [
					{ prop: "scale.x", to: BASE_SCALE * 0.634, ease: Easing.sine.easeInOut },
					{ prop: "scale.y", to: BASE_SCALE * 0.634, ease: Easing.sine.easeInOut }
				],
				duration: FRAME_RATE * 4
			},
			{
				tweens: [],
				duration: FRAME_RATE * 3
			},
			{
				tweens: [
					{ prop: "scale.x", to: BASE_SCALE * 0.528, ease: Easing.sine.easeInOut },
					{ prop: "scale.y", to: BASE_SCALE * 0.528, ease: Easing.sine.easeInOut }
				],
				duration: FRAME_RATE * 5
			},
			{
				tweens: [],
				duration: 8 * FRAME_RATE
			},
			{
				tweens: [
					{ prop: "scale.x", to: BASE_SCALE * 0, ease: Easing.sine.easeInOut },
					{ prop: "scale.y", to: BASE_SCALE * 0, ease: Easing.sine.easeInOut }
				],
				duration: FRAME_RATE * 2
			}
		];

		Sequence.start(this, rotationSequence);
		Sequence.start(this, scaleSequence);

	}

	i_startFinalMove(aDuration_num, aFinalPos_pt, aEasing_obj)
	{

	}

	constructor(aShots_int)
	{
		super();

		this._fShots_int = aShots_int;
		this._fContainer_sprt = null;
		this._initView();
	}

	_initView()
	{
		this._fContainer_sprt = this.addChild(new Sprite);

		let caption = this._fContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAAwardedFreeShotsCounterLabel"));
		let captionTxt = caption.text;
		captionTxt = this._pasteValue(captionTxt, this._fShots_int);
		caption.text = captionTxt;
		
		let value = this._fContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAAwardedFreeShotsCounterValue"));
		value.text = this._fShots_int;
		
		if(I18.getTranslatableAssetDescriptor("TAAwardedFreeShotsCounterLabelEndingOpt"))
		{
			let ending = this._fContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TAAwardedFreeShotsCounterLabelEndingOpt"));
			let endingTxt = ending.text;
			endingTxt = this._pasteValue(endingTxt, this._fShots_int);
			ending.text = endingTxt;
		}

		let lBounds_obj = this._fContainer_sprt.getBounds();
		this._fContainer_sprt.position.x = -lBounds_obj.x;
		this._fContainer_sprt.position.x -= lBounds_obj.width/2;
	}

	_pasteValue(aMessage_str, aValue_int)
	{
		if (aMessage_str.indexOf("#freeshot:") >= 0)
		{
			aMessage_str = I18.prepareNumberPoweredMessage(aMessage_str, "#freeshot:", aValue_int);
		}
		return aMessage_str;
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));
		PathTween.destroy(PathTween.findByTarget(this));

		if (this._fContainer_sprt && this._fContainer_sprt.duplicateSprite)
		{
			Sequence.destroy(Sequence.findByTarget(this._fContainer_sprt.duplicateSprite));
		}

		super.destroy();
	}
}

export default FreeShotsCounterView;