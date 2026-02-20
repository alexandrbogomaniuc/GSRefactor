import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';

const ANIMATION_DURATION = 10 * FRAME_RATE;

class DesaturatingSprite extends Sprite {

	i_saturate(aImmediately_bl = false)
	{
		this._saturate(aImmediately_bl);
	}

	i_desaturate(aImmediately_bl = false)
	{
		this._desaturate(aImmediately_bl);
	}

	constructor(aNormalAssetName_str, aDesaturatedAssetName_str, aOptAnchor_obj = {x: 0, y: 0})
	{
		super();

		this._fNormalSprite_sprt = APP.library.getSpriteFromAtlas(aNormalAssetName_str);
		this._fNormalSprite_sprt.anchor.set(aOptAnchor_obj.x, aOptAnchor_obj.y);

		this._fDesaturatedSprite_sprt = APP.library.getSpriteFromAtlas(aDesaturatedAssetName_str);
		this._fDesaturatedSprite_sprt.anchor.set(aOptAnchor_obj.x, aOptAnchor_obj.y);

		this._init();
	}

	get normalSprite()
	{
		return this._fNormalSprite_sprt;
	}

	get desaturatedSprite()
	{
		return this._fDesaturatedSprite_sprt;
	}

	_init()
	{
		this.addChild(this.normalSprite);
		this.addChild(this.desaturatedSprite);

		this._desaturate();
	}

	_saturate(aImmediately_bl = false)
	{
		this._resetSequences();

		if (aImmediately_bl)
		{
			this.desaturatedSprite.alpha = 0;
		}
		else
		{
			let desaturatedSeq = [
				{
					tweens: [ { prop: "alpha", to: 0}],
					duration: ANIMATION_DURATION
				}
			];
			Sequence.start(this.desaturatedSprite, desaturatedSeq);
		}
	}

	_desaturate(aImmediately_bl = false)
	{
		this._resetSequences();

		if (aImmediately_bl)
		{
			this.desaturatedSprite.alpha = 1;
		}
		else
		{
			let desaturatedSeq = [
				{
					tweens: [ { prop: "alpha", to: 1}],
					duration: ANIMATION_DURATION
				}
			];
			Sequence.start(this.desaturatedSprite, desaturatedSeq);
		}
	}

	_resetSequences()
	{
		Sequence.destroy(Sequence.findByTarget(this.normalSprite));
		Sequence.destroy(Sequence.findByTarget(this.desaturatedSprite));

	}
}

export default DesaturatingSprite;