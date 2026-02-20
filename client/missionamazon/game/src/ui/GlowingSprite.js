import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { GlowFilter, OutlineFilter } from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class GlowingSprite extends Sprite {

	static convertIntoGlowingSprite(aSprite_sprt, aOptGlowFilterParams_obj = null)
	{
		return new GlowingSprite(aSprite_sprt, aOptGlowFilterParams_obj);
	}

	get duplicateSprite()
	{
		this._fDuplicate_sprt || (this._fDuplicate_sprt = this._createDuplicateSprite());
		this._fDuplicate_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		return this._fDuplicate_sprt;
	}

	get baseSprite()
	{
		return this._fBaseSprite_sprt;
	}

	get _overlayColor()
	{
		return this._glowFilterParams.overlayColor;
	}

	get _glowColor()
	{
		return this._glowFilterParams.glowColor;
	}

	get _distance()
	{
		return this._glowFilterParams.distance;
	}

	get _outerStrength()
	{
		return this._glowFilterParams.outerStrength;
	}

	get _innerStrength()
	{
		return this._glowFilterParams.innerStrength;
	}

	get _quality()
	{
		return this._glowFilterParams.quality;
	}

	get _glowFilterParams()
	{
		return this._fGlowFilterParams_obj;
	}

	i_invalidateDuplicateSprite()
	{
		this._invalidateDuplicateSprite();
	}

	constructor(aBaseSprite_sprt, aOptGlowFilterParams_obj = null)
	{
		super();
		this._fBaseSprite_sprt = aBaseSprite_sprt;

		this._fGlowFilterParams_obj = aOptGlowFilterParams_obj || {};

		if (this._fGlowFilterParams_obj.distance == null) this._fGlowFilterParams_obj.distance = 6;
		if (this._fGlowFilterParams_obj.outerStrength == null) this._fGlowFilterParams_obj.outerStrength = 4;
		if (this._fGlowFilterParams_obj.innerStrength == null) this._fGlowFilterParams_obj.innerStrength = 4;
		if (this._fGlowFilterParams_obj.glowColor == null) this._fGlowFilterParams_obj.glowColor = 0xFDE9BD;
		if (this._fGlowFilterParams_obj.overlayColor == null) this._fGlowFilterParams_obj.overlayColor = 0xFFEFC1;
		if (this._fGlowFilterParams_obj.quality == null) this._fGlowFilterParams_obj.quality = 1;

		this._fDuplicate_sprt = null;

		let lParent_sprt = aBaseSprite_sprt.parent;
		let lPosition_obj = {x: aBaseSprite_sprt.position.x, y: aBaseSprite_sprt.position.y};
		let lZIndex_int = aBaseSprite_sprt.zIndex;

		this.addChild(this._fBaseSprite_sprt)
		this._fBaseSprite_sprt.position.set(0, 0);

		lParent_sprt.addChild(this);
		this.position.set(lPosition_obj.x, lPosition_obj.y);
		this.zIndex = lZIndex_int;

		this._fDuplicate_sprt = this._createDuplicateSprite();
	}

	_createDuplicateSprite()
	{
		let lRememberStats_obj = {
			basePosX: this.baseSprite.position.x,
			basePosY: this.baseSprite.position.y
		};

		this.baseSprite.position.x = 0;
		this.baseSprite.position.y = 0;

		let lBaseLocBounds_obj = this.baseSprite.getLocalBounds();

		this.baseSprite.x = -lBaseLocBounds_obj.x*this.baseSprite.scale.x;
		this.baseSprite.y = -lBaseLocBounds_obj.y*this.baseSprite.scale.y;

		APP.isDeprecetedInternetExplorer 
			? this.baseSprite.filters = [new OutlineFilter(this._outerStrength, this._glowColor, this._quality)]
			: this.baseSprite.filters = [new GlowFilter({distance: this._distance, outerStrength: this._outerStrength, innerStrength: this._innerStrength, color: this._glowColor, quality: this._quality})];

		let l_txtr = PIXI.RenderTexture.create({width: lBaseLocBounds_obj.width*this.baseSprite.scale.x, height: lBaseLocBounds_obj.height*this.baseSprite.scale.y, scaleMode: PIXI.SCALE_MODES.LINEAR, resolution: 2});
		APP.stage.renderer.render(this.baseSprite, { renderTexture: l_txtr });

		let l_sprt = new PIXI.Sprite(l_txtr);
		l_sprt.tint = this._overlayColor;
		this.baseSprite.filters = [];

		this.baseSprite.x = 0;
		this.baseSprite.y = 0;

		let lLocalBounds_obj = this.baseSprite.getLocalBounds();
		lLocalBounds_obj.x *= this.baseSprite.scale.x;
		lLocalBounds_obj.y *= this.baseSprite.scale.y;
		lLocalBounds_obj.width *= this.baseSprite.scale.x;
		lLocalBounds_obj.height *= this.baseSprite.scale.y;

		l_sprt.position.set(lLocalBounds_obj.x, lLocalBounds_obj.y);
		l_sprt.zIndex = this.baseSprite.zIndex + 1;

		this.baseSprite.position.x = lRememberStats_obj.basePosX;
		this.baseSprite.position.y = lRememberStats_obj.basePosY;
		lRememberStats_obj = null;

		this.addChild(l_sprt);
		return l_sprt;
	}

	_invalidateDuplicateSprite()
	{
		this._destroyDuplicateSprite();

		// create new
		this._fDuplicate_sprt = this._createDuplicateSprite();
	}

	_destroyDuplicateSprite()
	{
		if (this._fDuplicate_sprt)
		{
			this._fDuplicate_sprt.destroy({children: true, texture: true, baseTexture: true});
			this._fDuplicate_sprt = null;
		}
	}

	destroy()
	{
		this._destroyDuplicateSprite();
		this._fBaseSprite_sprt = null;

		super.destroy();
	}
}

export default GlowingSprite;