import { TOUCH_SCREEN } from '../../layout/features';
import Tween from '../../../controller/animation/Tween';
import { Rectangle } from '../../../model/math/Rectangle';
import { Vector } from '../../../model/math/Vector';
import { Utils } from '../../../model/Utils';
import CompositeHitArea from './CompositeHitArea';
import { generateAbsoluteURL } from '../../../controller/main/globals';

var pointerEvents = [
	"mousedown",
	"mouseup",
	"mousemove",
	"click",
	"touchstart",
	"touchend",
	"touchmove",
	"tap",
	"mouseover",
	"mouseout",
	"mouseupoutside",
	"rightclick",
	"rightdown",
	"rightup",
	"rightupoutside",
	"touchendoutside",
	"pointerdown",
	"pointerup",
	"pointermove",
	"pointerclick",
	"pointerupoutside"
];

var mouseEvents = [
	"mousedown",
	"mouseup",
	"mousemove",
	"click",
	"mouseover",
	"mouseout",
	"mouseupoutside",
	"rightclick",
	"rightdown",
	"rightup",
	"rightupoutside"
];

var touchEvents = [
	"touchstart",
	"touchend",
	"touchmove",
	"tap",
	"touchendoutside"
];

var TextureCache = {};

/**
 * @class
 * @inheritDoc
 * @classdesc Sprite
 * @augments PIXI.AnimatedSprite
 */
let IDD_COUNTER = 0;
const __BaseClass = PIXI.AnimatedSprite || PIXI.MovieClip;
class Sprite extends __BaseClass
{
	static get EVENT_ON_CHANGE_FRAME() {return "changeframe"};
	static get EVENT_ON_ANIMATION_END() {return "animationend"};
	static get EVENT_ON_DRAGGED() {return "dragged"};
	static get EVENT_ON_DESTROYING() {return "destroying"};

	/**
	 * @constructor
	 * @param {Asset} [asset=null] asset or assets array, or null for empty Sprite
	 */
	constructor(asset=null) {
		let frames = asset ? Sprite.getFrames(asset) : [PIXI.Texture.EMPTY];
		super(frames);

		this.asset = asset;

		this.anchor.set(0.5);

		this.animationSpeed = 24/60;

		this._zIndex = 0;
		this._dragPoint = null;

		this._tweens = [];

		if(this.totalFrames > 1) this.gotoAndPlay(0);
		else this.gotoAndStop(0);

		this._stageRenderer = null;
		this._fBlurFilter_bf = null;

		this.IDD = ++IDD_COUNTER;
	}

	gotoAndStop(...args) {
		var ret = super.gotoAndStop(...args);
		if(this.updateTexture) this.updateTexture();
		return ret;
	}

	gotoAndPlay(...args) {
		var ret = super.gotoAndPlay(...args);
		if(this.updateTexture) this.updateTexture();
		return ret;
	}

	/**
	 * Set stage renderer.
	 * @param {Renderer} value - Stage renderer.
	 */
	set stageRenderer(value)
	{
		this._stageRenderer = value;
	}

	/**
	 * Get stage renderer.
	 * @type {Renderer}
	 */
	get stageRenderer()
	{
		return this._stageRenderer || this._findStageRenderer();
	}

	_findStageRenderer()
	{
		let prnt = this.parent;
		while (!!prnt)
		{
			if (!!prnt.parent)
			{
				prnt = prnt.parent;
			}
			else
			{
				break;
			}		
		}
		if (!!prnt && !!prnt.stageRenderer)
		{
			return prnt.stageRenderer;
		}

		return null;
	}

	/**
	 * Create new Sprite instance by asset.
	 * @param {Asset} asset 
	 * @returns {Sprite}
	 * @static
	 */
	static create(asset) {
		return new Sprite(asset);
	}

	/**
	 * Create new Sprite instance.
	 * @param {PIXI.Texture[]} sourceTextures - Array of textures.
	 * @param {number} startFrame - Start texture frame in textures array.
	 * @param {number} totalFrames - Total frames amount.
	 * @returns {Sprite}
	 */
	static createMultiframesSprite(sourceTextures, startFrame=0, totalFrames=undefined)
	{
		let l_sprt = new Sprite();
		let textures = sourceTextures.slice() || [];

		if (startFrame >= textures.length)
		{
			textures = [];
		}
		else if (startFrame > 0)
		{
			textures.splice(0, (startFrame-1));
		}
		else if (startFrame < 0)
		{
			startFrame = Math.abs(startFrame);
			for (let i=0; i<startFrame; i++)
			{
				textures.unshift(PIXI.Texture.EMPTY);
			}
		}

		if (totalFrames !== undefined && totalFrames > 0)
		{
			if (totalFrames <= textures.length)
			{
				textures.length = totalFrames;
			}
			else
			{
				let addFramesAmount = totalFrames - textures.length;
				for (let i=0; i<startFrame; i++)
				{
					textures.push(PIXI.Texture.EMPTY);
				}
			}
		}

		l_sprt.textures = textures;

		return l_sprt;
	}

	/** Set visible. */
	show()
	{
		this.visible = true;
	}

	/** Set invisible. */
	hide()
	{
		this.visible = false;
	}

	/**
	 * Add blur.
	 * @param {number} aBlur_num 
	 */
	setBlur(aBlur_num)
	{
		let l_bf = this._fBlurFilter_bf;
		if(aBlur_num > 0 && !l_bf)
		{
			l_bf = this._fBlurFilter_bf = new PIXI.filters.BlurFilter();
		}

		if(aBlur_num > 0)
		{
			l_bf.blur = aBlur_num;
			if (!!this.filters)
			{
				if (this.filters.indexOf(l_bf) < 0)
				{
					this.filters = this.filters.concat([l_bf]);
				}
			}
			else
			{
				this.filters = [l_bf];
			}
		}
		else
		{
			if (!!this.filters)
			{
				let lBlurFilterIndex_int = !!l_bf ? this.filters.indexOf(l_bf) : -1;
				if (lBlurFilterIndex_int >= 0)
				{
					this.filters.splice(lBlurFilterIndex_int, 1);
				}
				else
				{
					this.filters = null;
				}
			}
		}
	}

	on(event, fn, context) {

		/**
		 * mousedown and touchstart events wrapper. Use for uniform behaviour for desktop and mobile devices.
		 * @event Sprite#pointerdown
		 */
		if(event == "pointerdown") {
			super.on("mousedown", fn, context);
			super.on("touchstart", fn, context);
		}

		/**
		 * mouseup and touchend events wrapper. Use for uniform behaviour for desktop and mobile devices.
		 * @event Sprite#pointerup
		 */
		if(event == "pointerup") {
			super.on("mouseup", fn, context);
			super.on("touchend", fn, context);
		}

		/**
		 * mousemove and touchmove events wrapper. Use for uniform behaviour for desktop and mobile devices.
		 * @event Sprite#pointermove
		 */
		if(event == "pointermove") {
			super.on("mousemove", fn, context);
			super.on("touchmove", fn, context);
		}

		/**
		 * click and tap events wrapper. Use for uniform behaviour for desktop and mobile devices.
		 * @event Sprite#pointerclick
		 */
		if(event == "pointerclick") {
			super.on("click", fn, context);
			super.on("tap", fn, context);
		}

		/**
		 * mouseupoutside and touchendoutside events wrapper. Use for uniform behaviour for desktop and mobile devices.
		 * @event Sprite#pointerupoutside
		 */
		if(event == "pointerupoutside") {
			super.on("mouseupoutside", fn, context);
			super.on("touchendoutside", fn, context);
		}

		if(pointerEvents.indexOf(event) >= 0) this.interactive = true;

		super.on(event, fn, context);
	}

	once(event, fn, context) {

		if(event == "pointerdown") {
			super.once("mousedown", fn, context);
			super.once("touchstart", fn, context);
		}

		if(event == "pointerup") {
			super.once("mouseup", fn, context);
			super.once("touchend", fn, context);
		}

		if(event == "pointermove") {
			super.once("mousemove", fn, context);
			super.once("touchmove", fn, context);
		}

		if(event == "pointerclick") {
			super.once("click", fn, context);
			super.once("tap", fn, context);
		}

		if(event == "pointerupoutside") {
			super.once("mouseupoutside", fn, context);
			super.once("touchendoutside", fn, context);
		}

		if(pointerEvents.indexOf(event) >= 0) this.interactive = true;

		return super.once(event, fn, context);
	}

	removeListener(event, fn, context, once) {
		if(event == "pointerdown") {
			super.removeListener("mousedown", fn, context, once);
			super.removeListener("touchstart", fn, context, once);
		}

		if(event == "pointerup") {
			super.removeListener("mouseup", fn, context, once);
			super.removeListener("touchend", fn, context, once);
		}

		if(event == "pointermove") {
			super.removeListener("mousemove", fn, context, once);
			super.removeListener("touchmove", fn, context, once);
		}

		if(event == "pointerclick") {
			super.removeListener("click", fn, context, once);
			super.removeListener("tap", fn, context, once);
		}

		if(event == "pointerupoutside") {
			super.removeListener("mouseupoutside", fn, context);
			super.removeListener("touchendoutside", fn, context);
		}

		super.removeListener(event, fn, context, once);

		if(pointerEvents.indexOf(event) >= 0) {
			let ok = false;
			for(let t of pointerEvents) {
				if(this.listeners(t).length > 0) {
					ok = true;
					break;
				}
			}
			this.interactive = ok;
		}
	}

	off(event, fn, context, once) {
		this.removeListener(event, fn, context, once);
	}

	hasEventListener(event)
	{
		return this.listeners(event, true);
	}

	emit(event) {

		var a1 = arguments[1];

		if(!a1) a1 = {};

		if (pointerEvents.indexOf(event) >= 0) {

			if(TOUCH_SCREEN && touchEvents.indexOf(event) < 0) return;
			if(!TOUCH_SCREEN && mouseEvents.indexOf(event) < 0) return;

			if(!a1.data) a1.data = {};
			a1.data.local = a1.data.getLocalPosition(this);
		}
		else
		{
			a1.target = this;
		}
		if(!a1.type) a1.type = event;

		if (typeof event === "string")
		{
			if(arguments.length < 3) super.emit(event, a1);
			if(arguments.length == 3) super.emit(event, a1, arguments[2]);
			if(arguments.length == 4) super.emit(event, a1, arguments[2], arguments[3]);
			if(arguments.length >= 5) super.emit(event, a1, arguments[2], arguments[3], arguments[4]);
		}
		else if (typeof event === "object" && event.target)
		{
			super.emit(event.type, event);
		}
	}

	/**
	 * Remove all Tweens added by addTween
	 */
	removeTweens()
	{
		if (!this._tweens || !this._tweens.length)
		{
			return;
		}

		while (this._tweens.length)
		{
			let tween = this._tweens.splice(0, 1)[0];
			tween.destructor();
		}
		this._tweens = null;
	}

	/**
	 * Add new Tween for Sprite
	 * @return {Tween}
	 */
	addTween(prop, end, duration, ease, onfinish, onchange, obj, autoRewind, delay) {
		if(!obj) obj = this;
		var t = new Tween(obj, prop, obj[prop], end, duration, ease, autoRewind, delay);
		if (onchange) t.on('change', onchange);
		if (onfinish) t.on('finish', onfinish);

		t.on(Tween.EVENT_ON_DESTROYING, this._onTweenDestroying, this);

		this._tweens = this._tweens || [];
		this._tweens.push(t);

		return t;
	}

	_onTweenDestroying(event)
	{
		let tween = event.target;
		this.removeTween(tween);
	}

	/**
	 * Remove tween.
	 * @param {Tween} tween 
	 */
	removeTween(tween)
	{
		if (!this._tweens || !this._tweens.length)
		{
			return null;
		}

		let tweenIndex = this._tweens.indexOf(tween);
		if (tweenIndex >= 0)
		{
			this._tweens.splice(tweenIndex, 1);
		}

		tween.off(Tween.EVENT_ON_DESTROYING, this._onTweenDestroying, this);
		tween.destructor();
	}

	/**
	 * Move sprite to specified position.
	 * @param {Number} x target X
	 * @param {Number} y target Y
	 * @param {Number} duration duratrion of movement in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {DisplayObject}
	 */
	moveTo(x, y, duration, ease, onfinish, onchange, autoRewind, delay) {

		this.addTween('x', x, duration, ease, null, null, this, autoRewind, delay).play();
		this.addTween('y', y, duration, ease, onfinish, onchange, this, autoRewind, delay).play();
		return this;
	}

	moveYTo(y, duration, ease, onfinish, onchange, autoRewind, delay) {
		let t = this.addTween('y', y, duration, ease, onfinish, onchange, this, autoRewind, delay).play();
		return t;
	}

	moveXTo(x, duration, ease, onfinish, onchange, autoRewind, delay) {
		let t = this.addTween('x', x, duration, ease, onfinish, onchange, this, autoRewind, delay).play();
		return t;
	}

	/**
	 * Move sprite to a specified distance.
	 * @param {Number} x - X distance
	 * @param {Number} y - Y distance
	 * @param {Number} duration - duratrion of movement in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end 
	 * @param {number} delay Delay to start playing in ms
	 * @return {Sprite}
	 */
	moveBy(x, y, duration, ease, onfinish, onchange, autoRewind, delay) {
		return this.moveTo(this.x + x, this.y + y, duration, ease, onfinish, onchange, autoRewind, delay);
	}

	/**
	 * Update alpha to specified value
	 * @param {Number} alpha Alpha (0-1)
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {DisplayObject}
	 */
	fadeTo(alpha, duration, ease, onfinish, onchange, autoRewind, delay) {
		this.addTween('alpha', alpha, duration, ease, onfinish, onchange, this, autoRewind, delay).play();
		return this;
	}

	/**
	 * Update alpha by specified delta
	 * @param {Number} alpha Alpha delta (0-1)
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {Sprite}
	 */
	fadeBy(alpha, duration, ease, onfinish, onchange, autoRewind, delay) {
		var val = Math.max(0, Math.min(1, this.alpha + alpha));
		return this.fadeTo(val, duration, ease, onfinish, onchange, autoRewind, delay);
	}

	/**
	 * Rotation to specified value
	 * @param {Number} rotation Angle in radians
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {DisplayObject}
	 */
	rotateTo(rotation, duration, ease, onfinish, onchange, autoRewind, delay) {
		let t = this.addTween('rotation', rotation, duration, ease, onfinish, onchange, this, autoRewind, delay).play();
		return t;
	}

	/**
	 * Rotation by specified delta
	 * @param {Number} rotation Angle delta in radians
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {Sprite}
	 */
	rotateBy(rotation, duration, ease, onfinish, onchange, autoRewind, delay) {
		return this.rotateTo(this.rotation + rotation, duration, ease, onfinish, onchange, autoRewind, delay);
	}

	/**
	 * Skew in X-axis to specified angle
	 * @param {Number} skew Angle in radians
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {DisplayObject}
	 */
	skewXTo(skew, duration, ease, onfinish, onchange, autoRewind, delay) {
		this.addTween('x', skew, duration, ease, onfinish, onchange, this.skew, autoRewind, delay).play();
		return this;
	}

	/**
	 * Skew in X-axis by specified delta
	 * @param {Number} skew Angle delta in radians
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {Sprite}
	 */
	skewXBy(skew, duration, ease, onfinish, onchange, autoRewind, delay) {
		return this.skewXTo(this.skew.x + skew, duration, ease, onfinish, onchange, autoRewind, delay);
	}

	/**
	 * Skew in Y-axis to specified angle
	 * @param {Number} skew Angle in radians
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {DisplayObject}
	 */
	skewYTo(skew, duration, ease, onfinish, onchange, autoRewind, delay) {
		this.addTween('y', skew, duration, ease, onfinish, onchange, this.skew, autoRewind, delay).play();
		return this;
	}

	/**
	 * Skew in Y-axis by specified delta
	 * @param {Number} skew Angle in radians
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {Sprite}
	 */
	skewYBy(skew, duration, ease, onfinish, onchange, autoRewind, delay) {
		return this.skewYTo(this.skew.y + skew, duration, ease, onfinish, onchange, autoRewind, delay);
	}

	/**
	 * Update X scale to specified value
	 * @param {Number} scale Scale
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {Sprite}
	 */
	scaleXTo(scale, duration, ease, onfinish, onchange, autoRewind, delay) {
		this.addTween('x', scale, duration, ease, onfinish, onchange, this.scale, autoRewind, delay).play();
		return this;
	}

	/**
	 * Update X scale by specified delta
	 * @param {Number} scale Scale delta
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {Sprite}
	 */
	scaleXBy(scale, duration, ease, onfinish, onchange, autoRewind, delay) {
		this.addTween('x', this.scale.x + scale, duration, ease, onfinish, onchange, this.scale, autoRewind, delay).play();
		return this;
	}

	/**
	 * Update Y scale to specified value
	 * @param {Number} scale Scale
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {Sprite}
	 */
	scaleYTo(scale, duration, ease, onfinish, onchange, autoRewind, delay) {
		this.addTween('y', scale, duration, ease, onfinish, onchange, this.scale, autoRewind, delay).play();
		return this;
	}

	/**
	 * Update Y scale by specified delta
	 * @param {Number} scale Scale delta
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {Sprite}
	 */
	scaleYBy(scale, duration, ease, onfinish, onchange, autoRewind, delay) {
		this.addTween('y', this.scale.y + scale, duration, ease, onfinish, onchange, this.scale, autoRewind, delay).play();
		return this;
	}

	/**
	 * Update both X and Y scale to specified value 
	 * @param {Number} scale Scale
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {DisplayObject}
	 */
	scaleTo(scale, duration, ease, onfinish, onchange, autoRewind, delay) {
		this.scaleXTo(scale, duration, ease, null, null, autoRewind, delay);
		this.scaleYTo(scale, duration, ease, onfinish, onchange, autoRewind, delay);
		return this;
	}

	/**
	 * Update both X and Y scale by specified delta
	 * @param {Number} scale Scale delta
	 * @param {Number} duration duratrion in ms
	 * @param {Function} [ease] 
	 * @param {Function} [onfinish] callback on finish (ex. <a href="Tween.html#onfinish">Tween</a>)
	 * @param {Function} [onchange] callback of animation step (ex. <a href="Tween.html#onchange">Tween</a>)
	 * @param {boolean} autoRewind Autorepeat after the end
	 * @param {number} delay Delay to start playing in ms
	 * @return {DisplayObject}
	 */
	scaleBy(scale, duration, ease, onfinish, onchange, autoRewind, delay) {
		this.scaleXBy(scale, duration, ease, null, null, autoRewind, delay);
		this.scaleYBy(scale, duration, ease, onfinish, onchange, autoRewind, delay);
		return this;
	}

	/**
	 * Check in sprite contains point
	 * @param x
	 * @param y
	 * @returns {boolean}
	 */
	hitTestPoint(x, y) {
		let point;

		if(y === undefined) point = x;
		else point = new PIXI.Point(x, y);

		return this.containsPoint(point);
	}

	/**
	 * Calculates absolute sprite rotation
	 * @returns {number}
	 */
	getAbsoluteRotation() {
		let r = this.rotation;
		let parent = this.parent;
		while(parent) {
			r += parent.rotation;
			parent = parent.parent;
		}
		return r;
	}

	/**
	 * Calculates absolute X scale
	 * @returns {number}
	 */
	getAbsoluteScaleX() {
		let scale = this.scale.x;
		let parent = this.parent;
		while(parent) {
			scale *= parent.scale.x;
			parent = parent.parent;
		}
		return scale;
	}

	/**
	 * Calculates absolute Y scale
	 * @returns {number}
	 */
	getAbsoluteScaleY() {
		let scale = this.scale.y;
		let parent = this.parent;
		while(parent) {
			scale *= parent.scale.y;
			parent = parent.parent;
		}
		return scale;
	}

	/**
	 * Calculates absolute coordinates of sprite center
	 * @returns {number}
	 */
	getGlobalCenter() {
		if(this.anchor.x == 0.5 && this.anchor.y == 0.5) return this.getGlobalPosition();

		let dx = 0.5 - this.anchor.x;
		let dy = 0.5 - this.anchor.y;
		let angle = Math.atan2(dy, dx) + this.getAbsoluteRotation();
		let w = this.width * Math.abs(this.getAbsoluteScaleX()) * Math.abs(dx);
		let h = this.height * Math.abs(this.getAbsoluteScaleY()) * Math.abs(dy);
		let len = Math.sqrt(w*w + h*h);

		let p = this.getGlobalPosition();

		p.x += Math.cos(angle) * len;
		p.y += Math.sin(angle) * len;

		return p;
	}

	/**
	 * Checks for intersection with another sprite
	 * @param {Sprite} obj
	 * @returns {boolean}
	 */
	hitTest(obj) {
		let r1 = this.getAbsoluteRotation();
		let r2 = obj.getAbsoluteRotation();

		if(r1 == 0 && r2 == 0) {
			let c1 = this.getGlobalCenter();
			let c2 = obj.getGlobalCenter();
			let cW1 = this.width * Math.abs(this.getAbsoluteScaleX());
			let cH1 = this.height * Math.abs(this.getAbsoluteScaleY());
			let cW2 = obj.width * Math.abs(obj.getAbsoluteScaleX());
			let cH2 = obj.height * Math.abs(obj.getAbsoluteScaleY());
			let cX1 = c1.x - cW1 / 2;
			let cY1 = c1.y - cH1 / 2;
			let cX2 = c2.x - cW2 / 2;
			let cY2 = c2.y - cH2 / 2;

			let top = Math.max(cY1, cY2);
			let left = Math.max(cX1, cX2);
			let right = Math.min(cX1 + cW1, cX2 + cW2);
			let bottom = Math.min(cY1 + cH1, cY2 + cH2);
			let width = right - left;
			let height = bottom - top;

			return (width > 0 && height > 0);
		}
		else {
			let r1 = this.getDrawRectangle(), r2 = obj.getDrawRectangle();
			return r1.hitTestRectangle(r2);
		}
	}

	/**
	 * Get AABB of sprite
	 * @returns {Rectangle}
	 */
	getDrawRectangle() {
		let c = this.getGlobalCenter();
		let r = new Rectangle(0, 0, this.width * Math.abs(this.getAbsoluteScaleX()), this.height * Math.abs(this.getAbsoluteScaleY()), this.getAbsoluteRotation());
		r.move(c.x, c.y);
		return r;
	}

	/**
	 * Checks an object is a child of sprite
	 * @param obj
	 * @returns {boolean}
	 */
	contains(obj) {
		return this.children.indexOf(obj) >= 0;
	}

	/**
	 * Clone sprite
	 * @returns {Sprite}
	 */
	clone() {
		var spr = new Sprite(this.asset);
		Object.assign(spr, this);
		spr.IDD = ++IDD_COUNTER;
		spr.parent = null;
		return spr;
	}

	get zIndex() {
		return this._zIndex;
	}
	set zIndex(val) {
		this._zIndex = val;

		if(this.parent) this.applyZIndex();
		else this.once("added", this.applyZIndex, this);
	}

	/** @ignore */
	applyZIndex()
	{
		this.parent.children.sort( (child1, child2) => {
			if(child1.zIndex === undefined || child1.zIndex === undefined)
			{
				return 0;
			}
			if (child1.zIndex > child2.zIndex)
			{
				return 1;
			}
			if (child1.zIndex < child2.zIndex)
			{
				return -1;
			}
			return 0;
		} );
	}

	/** @ignore */
	doDrag(e) {
		if(!this._dragPoint) return;

		if(e.data.originalEvent.touches && e.data.originalEvent.touches.length != 1) return;

		var x = e.data.global.x - this._dragPoint.x * this.scale.x;
		var y = e.data.global.y - this._dragPoint.y * this.scale.y;
		var p = this.parent.globalToLocal(x, y);
		this.position.set(p.x, p.y);

		this.emit(Sprite.EVENT_ON_DRAGGED);
	}

	/**
	 * Start drag`n`drop
	 * @param x
	 * @param y
	 */
	startDrag(x, y) {
		this._dragPoint = new PIXI.Point(x, y);
		this.on("pointermove", this.doDrag);
	}

	/**
	 * Stop drag`n`drop
	 */
	stopDrag() {
		this._dragPoint = null;
		this.off("pointermove", this.doDrag);
	}

	/**
	 * Convert local coordinate to global
	 * @param x
	 * @param y
	 * @returns {Vector}
	 */
	localToGlobal(x, y) {
		var p = (( typeof x == 'object') && ( typeof x['x'] != 'undefined') && ( typeof x['y'] != 'undefined')) ? new Vector(x.x + 0, x.y + 0) : new Vector(x, y);
		//p.rotate(this.getAbsoluteRotation()).add(this.getGlobalPosition());
		p.rotate(-this.getAbsoluteRotation()).add(this.getGlobalPosition());;
		return p;
	}

	/**
	 * Convert global coordinate to local
	 * @param x
	 * @param y
	 * @returns {Vector}
	 */
	globalToLocal(x, y) {
		var p = (( typeof x == 'object') && ( typeof x['x'] != 'undefined') && ( typeof x['y'] != 'undefined')) ? new Vector(x.x + 0, x.y + 0) : new Vector(x, y);
		p.subtract(this.getGlobalPosition()).rotate(this.getAbsoluteRotation());
		return p;
	}

	/**
	 * Convert local coordinate to coordinate system of another sprite
	 * @param x
	 * @param y
	 * @param {Sprite} target
	 * @returns {Vector}
	 */
	localToLocal(x, y, target) {
		return target.globalToLocal(this.localToGlobal(x, y));
	}

	/**
	 * Step to another frame.
	 * @param {number} delta 
	 */
	update(delta)
	{
		if (this._destroyed)
		{
			throw new Error("Attempt to update destroyed Sprite!");
		}

		var currentFrame = this.currentFrame;

		super.update(delta);

		if(this.playing && currentFrame != this.currentFrame) {
			var cnt = 0;
			if(this.currentFrame > currentFrame) cnt = this.currentFrame - currentFrame;
			else cnt = this.currentFrame + this.totalFrames - currentFrame;

			var frame = currentFrame;
			for(var i=0; i<cnt; i++)
			{
				if (this._destroyed)
				{
					break;
				}

				this.emit(Sprite.EVENT_ON_CHANGE_FRAME, {frame: frame});
				frame++;
				if(frame >= this.totalFrames) {
					frame = 0;
					this.emit(Sprite.EVENT_ON_ANIMATION_END);
				}
			}
		}
	}

	/** Safe removing of children by index (from-to) */
	destroyChildren(options, beginIndex, endIndex)
	{
		var removed = super.removeChildren(beginIndex, endIndex);
		for(var child of removed) {
			if(child.destroy) child.destroy(options);
		}
	}

	/** Safe destroy of sprite */
	destroy(options)
	{
		if (this._destroyed)
		{
			return;
		}

		this.emit(Sprite.EVENT_ON_DESTROYING);

		// console.log("[Sprite] destroy", this.IDD);

		if (options === undefined)
		{
			options = {children: true, texture: false, baseTexture: false};
		}

		this.removeTweens();

		this.asset = null;
		this._dragPoint = null;

		this._fBlurFilter_bf = null;

		super.destroy(options);

		this._textures = null;
		this._texture = null;
	}

	/** @ignore */
	_doDrawHitArea(area, view) {
		if(area instanceof PIXI.Rectangle) {
			view.drawRect(area.x, area.y, area.width, area.height);
		}
		else if(area instanceof PIXI.RoundedRectangle) {
			view.drawRoundedRect(area.x, area.y, area.width, area.height, area.radius);
		}
		else if(area instanceof PIXI.Circle) {
			view.drawCircle(area.x, area.y, area.radius);
		}
		else if(area instanceof PIXI.Ellipse) {
			view.drawEllipse(area.x, area.y, area.width, area.height)
		}
		else if(area instanceof PIXI.Polygon) {
			view.moveTo(area.points[0], area.points[1]);
			for(let i=2; i<area.points.length; i+=2) {
				view.lineTo(area.points[i], area.points[i+1]);
			}
		}
		else if(area instanceof CompositeHitArea) {
			for(let cArea of area.areas) this._doDrawHitArea(cArea, view);
		}
	}

	/** Adds view for hitArea. Useful for debug*/
	drawHitArea(aOptAlpha_num=1) {
		if(!this.hitArea) return;
		if(this.hitAreaView) this.removeChild(this.hitAreaView);

		let view = new PIXI.Graphics();
		view.beginFill(0xff0000, aOptAlpha_num);
		view.lineStyle(1, 0x000000);

		this._doDrawHitArea(this.hitArea, view);

		view.endFill();

		this.addChild(view);
		this.hitAreaView = view;
	}

	/** Update size of frames */
	resize(width, height) {
		if(this.width == width && this.height == height) return;

		for(var texture of this.textures) {
			texture.frame.width = width;
			texture.frame.height = height;
			texture._updateUvs();
		}
	}

	/** Clone all textures to unlink from cache */
	cloneTextures() {
		var textures = [];
		for(var tex of this.textures) {
			let frame = new PIXI.Rectangle(tex.frame.x, tex.frame.y, tex.frame.width, tex.frame.height);
			let orig = new PIXI.Rectangle(tex.orig.x, tex.orig.y, tex.orig.width, tex.orig.height);
			let trim = undefined;
			if(tex.trim) trim = new PIXI.Rectangle(tex.trim.x, tex.trim.y, tex.trim.width, tex.trim.height);
			let rotate = tex.rotate;

			textures.push(new PIXI.Texture(tex.baseTexture, frame, orig, trim, rotate));
		}
		this.textures = textures;
		this.texture = this.textures[this.currentFrame];
	}

	/** Prevent all interactive events */
	preventInteractiveEvents() {
		this.interactive = true;
		this.on("pointerdown", Utils.preventInteractiveEvent);
		this.on("pointerup", Utils.preventInteractiveEvent);
		this.on("pointermove", Utils.preventInteractiveEvent);
		this.on("pointerclick", Utils.preventInteractiveEvent);
		this.on("pointerupoutside", Utils.preventInteractiveEvent);
		this.on("mouseover", Utils.preventInteractiveEvent);
		this.on("mouseout", Utils.preventInteractiveEvent);
	}

	/** Gets the list of texture frames */
	static getFrames(assets) {
		if(!Array.isArray(assets)) assets = [assets];

		let frames = [];

		for(let asset of assets) {
			let textureUrl = generateAbsoluteURL(asset.src);
			let baseTexture = PIXI.utils.BaseTextureCache[textureUrl];
			if (!baseTexture) {
				throw Error(`Texture ${textureUrl} not found`);
			}

			let totalFrames = asset.totalFrames || asset.frames * asset.layers;

			let frame = 0, layer = 0;
			for (let i = 0; i < totalFrames; i++) {
				let name = baseTexture.resource.url + "_" + i;
				if (TextureCache[name]) {
					frames.push(TextureCache[name]);
				}
				else {
					let texFrame = new PIXI.Rectangle(layer * asset.width, frame * asset.height, asset.width, asset.height);
					let tex = new PIXI.Texture(baseTexture, texFrame);
					frames.push(tex);
					TextureCache[name] = tex;

					baseTexture.once('dispose', () => {
														tex.destroy(); 
														delete TextureCache[name]; 
													});
				}

				frame++;
				if (frame >= asset.frames) {
					frame = 0;
					layer++;
				}
			}
		}

		return frames;
	}
}

export default Sprite;