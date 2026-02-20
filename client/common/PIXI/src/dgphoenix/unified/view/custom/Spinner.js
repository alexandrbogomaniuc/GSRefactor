import Sprite from '../base/display/Sprite';
import Tween from '../../controller/animation/Tween';

/**
 * Standard spinner view. Animation starts automatically after view created.
 * @class
 * @augments Sprite
 */
class Spinner extends Sprite
{
	/**
	 * @constructor
	 * @param {number} duration - Animation cycle duration in milliseconds.
	 * @param {number} delay - Segments animation delay.
	 * @param {number} scale - View scale.
	 */
	constructor(duration = 3800, delay = 100, scale = 1, easing) {
		super();

		this.container = this.addChild(new Sprite);
		this.container.scale.set(scale);
		this.duration = duration;
		this.delay = delay;
		this.animationInProgress = false;

		this.startedAnimations = 0;

		this.createView();
	}

	onAnimationCicleEnd(){
		if (--this.startedAnimations == 0){
			this.emit("endcicle");
		}
		this.emit("endOneCicle");
	}

	/**
	 * Create spinner view.
	 */
	createView() {
		var positions = [
			{x: 0, y: 0},
			{x: -5, y: -2},
			{x: -5, y: -5},
			{x: -3, y: -7},
			{x: 0, y: -7.6},
			{x: 3, y: -5.6},
			{x: 2.8, y: -2.5},
			{x: 0.8, y: 0.2}
		];

		for (var i = 0; i < 8; ++i){
			this.container.addChild(this.getRect(i, positions[i]));
		}

		this.startAnimation();
	}

	getRect(index, position){
		var rect = null;
		
		if (!this.sourceRect){
			this.sourceRect = new PIXI.Graphics();
			this.sourceRect.beginFill(0xEEDDB9, 1);
			this.sourceRect.drawRoundedRect(0, 0, 3, 10, 2);
			this.sourceRect.endFill();
			this.sourceRect.pivot.set(3, 0);
			rect = this.sourceRect;
		}
		else{
			rect = this.sourceRect.clone();
		}

		rect.rotation = Math.PI * (index/4);
		rect.position = position;

		return rect;
	}

	/**
	 * Start spinner animation.
	 */
	startAnimation(){
		if (this.animationInProgress == true){
			this.stopAnimation();
		}

		this.animationInProgress = true;
		this.spin_tweeners = [];

		for (var i = 0; i < this.container.children.length; ++i){
			this.spin_tweeners[i] = new Tween(this.container.children[i], "alpha", 0, 1, this.duration);
			this.spin_tweeners[i].autoRewind = true;
			this.spin_tweeners[i].play(this.delay*i);
			this.spin_tweeners[i].once("endcicle", this.onAnimationCicleEnd, this);
			++this.startedAnimations;
		}
	}

	/**
	 * Clear spinner view.
	 */
	clear(){
		if (this.spin_tweeners){
			this.stopAnimation();
			this.container.destroy();
			this.container = null;
			this.spin_tweeners = null;
			this.startedAnimations = 0;
		}
	}

	/**
	 * Stop animation.
	 */
	stopAnimation(){
		this.animationInProgress = false;
		this.startedAnimations = 0;
		for (var i = 0; i < this.container.children.length; ++i){
			this.spin_tweeners[i].stop();
			this.spin_tweeners[i].once("endcicle", this.onAnimationCicleEnd, this);
		}
	}

	/**
	 * Destroy instance.
	 */
	destroy()
	{
		this.container = null;
		this.duration = undefined;
		this.delay = undefined;
		this.animationInProgress = undefined;

		this.startedAnimations = undefined;

		super.destroy();
	}

}

export default Spinner;