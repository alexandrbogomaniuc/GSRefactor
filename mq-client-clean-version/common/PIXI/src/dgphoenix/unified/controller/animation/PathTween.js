import { Bezier } from '../../model/math/Bezier';
import Tween from './Tween';
import Timer from '../time/Timer';

/**
 * @class
 * @description Path tween
 * @memberOf module:animation
 * @param {Object} obj - target object
 * @param {Array} path - array of path points
 * @param {Boolean} [isBezierCurve=false] - true if points of path are also points of Bezie curve
 * @param {Boolean} [loop=false] - true if animation is looped
 * @example
 * 
 * var t = new PathTween(b, [{x: 50, y: 200}, {x: 80, y: 100}, {x: 250, y: 180}], true);
 * t.start(1000*3, Easing.bounce.easeOut, onFinish, onChange);
 */

/**
 * Instances registry
 * @type {Set}
 * @ignore
 */
let TWEENS = new Set();

/**
 * New created instances. Flushes in TWEENS after the tick.
 * This is to avoid circular ticks when Tween created inside another Tween callback.
 * @type {Set}
 * @ignore
 */
let NEW_TWEENS = new Set();

class PathTween 
{
	constructor(obj, path, isBezierCurve=false, loop=false) 
	{
		this.obj = obj;
		this.path = path;
		this.loop = loop;

		if(isBezierCurve) this.path = Bezier.getCurve(path);

		this.pathLen = this.getPathLen();
		this.position = 0;

		this.tween = null;

		this.startTimer = null;

		this._autoDestroyOnComplete = true;

		this._onCompleteHandler = null;
		this._onChangeHandler = null;

		this._destroyed = false;

		NEW_TWEENS.add(this);
	}

	get autoDestroyOnComplete()
	{
		return this._autoDestroyOnComplete;
	}

	set autoDestroyOnComplete(value)
	{
		this._autoDestroyOnComplete = value;
	}

	getPathLen() {
		var len = 0;
		for (var i = 1, n = this.path.length; i < n; i++) {
			len += Math.sqrt(
				Math.pow(this.path[i].x - this.path[i-1].x, 2)
				+
				Math.pow(this.path[i].y - this.path[i-1].y, 2)
			);
		}
		return len;
	}

	/** Returns an angle of current position */
	getCurrentAngle() {
		return this.getAngle(this.position);
	}

	/** Returns an angle of path's part */
	getAngle(needLen) {
		var path = this.getSegment(needLen);
		if (!path || path.length < 2) return 0;
		return Math.atan2(path[1].y-path[0].y, path[1].x-path[0].x);
	}

	getCurrentSegment() {
		return this.getSegment(this.position);
	}

	/** Returns the segment of the path */
	getSegment(needLen) {
		if (this.path.length <= 2) return this.path; // path has only 1 segment
	
		var w, h, len=0, pathLen=0, ok=true, i=0, cx=this.path[0].x, cy=this.path[0].y;
	
		var res = [];
		while(ok) {
			i++;
	
			if(i >= this.path.length) {
				return [this.path[this.path.length-2], this.path[this.path.length-1]];
			}
			else {
				//add the length of the next segment
				w = cx - this.path[i].x;
				h = cy - this.path[i].y;
				len = Math.sqrt(w*w+h*h);
	
				//out of required length
				if(pathLen + len >= needLen) {
					return [this.path[i-1], this.path[i]];
				}
				//move the point
				else {
					pathLen += len;
					cx = this.path[i].x;
					cy = this.path[i].y;
				}
			}
		}
	}

	getCurrentPoint() {
		return this.getPoint(this.position);
	}

	getPoint(needLen) {
		var w, h, len=0, pathLen=0, ok=true, i=0, cx=this.path[0].x, cy=this.path[0].y;

		while(ok) {
			i++;

			if(i >= this.path.length) {
				return {x: this.path[this.path.length-1].x, y: this.path[this.path.length-1].y};
			}
			else {
				//add the length of the next segment
				w = cx - this.path[i].x;
				h = cy - this.path[i].y;
				len = Math.sqrt(w*w+h*h);

				//out of required length
				if(pathLen + len >= needLen) {
					//do step back
					var angle = Math.atan2(this.path[i].y - cy, this.path[i].x - cx);
					len = needLen - pathLen;
					cx += Math.cos(angle) * len;
					cy += Math.sin(angle) * len;
					return {x: cx, y: cy};
				}
				//move the point
				else {
					pathLen += len;
					cx = this.path[i].x;
					cy = this.path[i].y;
				}
			}
		}
	}

	/**
	 * Animation start
	 * @param duration
	 * @param [ease]
	 * @param [onfinish]
	 * @param [onchange]
	 * @param [delay]
	 */
	start(duration, ease, onfinish, onchange, delay) {
		var self = this;

		this._onCompleteHandler = onfinish;
		this._onChangeHandler = onchange;

		function doStart() {
			self.position = 0;
			if(self.tween) {
				self.tween.stop();
				self.tween.destroy = true;
			}

			self.tween = new Tween(self, "position", 0, self.pathLen, duration, ease);

			self.tween.on("finish", self.updateOnFinish, self);
			self.tween.on("change", self.updateOnTween, self);

			self.tween.play();

			self.startTimer = null;
		}

		if(delay) this.startTimer = new Timer(doStart, delay);
		else doStart();
	}

	/** pause playing */
	pause() {
		if(this.tween) this.tween.pause();
		if(this.startTimer) this.startTimer.pause();
	}

	/** resume playing */
	play() {
		if(this.tween) this.tween.play();
		if(this.startTimer) this.startTimer.pause();
	}

	/** move to the beginning */
	rewind() {
		if(this.tween) this.tween.rewind();
		if(this.startTimer) this.startTimer.rewind();
	}

	/** move to the end */
	forward() {
		if(this.tween) this.tween.forward();
		if(this.startTimer) {
			this.startTimer.pause();
			this.startTimer.destroy = true;
		}
	}

	/** stop playing */
	stop() {
		if(this.tween) this.tween.stop();
		if(this.startTimer) {
			this.startTimer.pause();
			this.startTimer.destroy = true;
		}
	}

	updateOnFinish(e) {
		var self = e.target.obj;

		var point = self.path[self.path.length - 1];
		self.obj.x = point.x;
		self.obj.y = point.y;

		if(self.loop) {
			self.rewind();
			self.play();
			e.cancelDestroy = true;
			return false;
		}
		else
		{
			if (this._onCompleteHandler)
			{
				this._onCompleteHandler(e);
			}

			if (this.autoDestroyOnComplete)
			{
				this.destructor();
			}
		}
	}

	updateOnTween(e) {
		var self = e.target.obj;

		var point = self.getCurrentPoint();
		self.obj.x = point.x;
		self.obj.y = point.y;

		if (this._onChangeHandler)
		{
			this._onChangeHandler(e);
		}
	}

	destructor() 
	{
		if (this._destroyed)
		{
			return;
		}
		
		Tween.destroy(Tween.findByTarget(this));

		if (this.startTimer)
		{
			this.startTimer.destructor();
		}

		TWEENS.delete(this);
		NEW_TWEENS.delete(this);

		this.obj = null;
		this.tween = null;
		this.startTimer = null;

		this._onCompleteHandler = null;
		this._onChangeHandler = null;

		this._destroyed = true;
	}

	/**
	 * Twens destruction
	 * @param {Iterable} [items] All tweens by default
	 * @ignore
	 */
	static destroy(items = [...TWEENS, ...NEW_TWEENS]) {
		for (let t of items) {
			t.destructor();
		}
	}

	/**
	 * Find all tweens by a prop
	 * @param {String} prop - list of objects
	 * @param {...*} values - list of values
	 * @return {Tween[]} - all tweens with the specified property value (strict comparison) 
	 * @ignore
	 */
	static filter(prop, ...values) {
		let result = [...TWEENS, ...NEW_TWEENS];
		if (undefined !== prop && values.length) {
			for (let val of values) {
				result = result.filter(
					tween => tween[prop] === val
				);
			}
		}
		return result;
	}

	/**
	 * Search for all tweens where the object is any of the passed arguments
	 * @method module:animation.Tween.findByTarget
	 * @param {...*} targets - list of objects
	 * @return {Tween[]} all tweens for the passed object
	 */
	static findByTarget(...targets) {
		return this.filter('obj', ...targets);
	}
}

export default PathTween;