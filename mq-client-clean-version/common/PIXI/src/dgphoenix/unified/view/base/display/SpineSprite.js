import Ticker from '../../../controller/time/Ticker';
import Sprite from './Sprite';
import { APP } from '../../../controller/main/globals';
import { Utils } from '../../../model/Utils';

var stateEvents = [
	"start",
	"interrupt",
	"end",
	"dispose",
	"complete",
	"event"
];

var SPINE_POOL = {};

/**
 * @description Sprite for Spine models
 * @augments Sprite
 * @example let view = new SpineSprite(spineData, spineName);
 */
class SpineSprite extends Sprite {

	/**
	 * @constructor
	 * @param skeletonData Spine model data.
	 * @param spineName Spine model name.
	 */
	constructor(skeletonData, spineName) {

		super();

		this._lastUpdateDelta = 0;

		if(!skeletonData) throw Error("SpineSprite requires prepared skeleton data");
		this._spineName = spineName;

		let view = this._getAvailableSpineFromPool(spineName);
		if (!view)
		{
			if (!!PIXI.heaven && APP.isHeavenSpineUsageAllowed && APP.isPixiHeavenLibrarySupported)
			{
				view = new PIXI.heaven.Spine(skeletonData);
			}
			else
			{
				view = new PIXI.spine.Spine(skeletonData);
			}
		}

		view.skeleton.setToSetupPose();
		view.autoUpdate = false;

		this.addChild(view);

		//DEBUG...
		this._bounds_gr = this.addChild(new PIXI.Graphics());
		//...DEBUG

		this._fFunctionsToCallAtFrames_obj_arr = [];

		/**
		 * View of Spine animation
		 * @type {PIXI.spine.Spine}
		 */
		this.view = view;

		Ticker.on('tick', this.tick, this);
	}

	_getAvailableSpineFromPool(spineName)
	{
		let lAvailableSpines = SPINE_POOL[spineName];
		if (!!lAvailableSpines && !!lAvailableSpines.length)
		{
			return lAvailableSpines.pop();
		}

		return null;
	}

	/**
	 * Is animation playing in reverse mode or not.
	 */
	get i_isAnimationReverse()
	{
		return this._fIsAnimationReverse_bl || false;
	}

	/**
	 * Destroy instance.
	 */
	destroy() {
		Ticker.off('tick', this.tick, this);

		if (this.view)
		{
			this.removeAllListeners();
			this.clearStateListeners();
			
			this.view.parent && this.view.parent.removeChild(this.view);

			SPINE_POOL[this._spineName] = SPINE_POOL[this._spineName] || [];
			SPINE_POOL[this._spineName].push(this.view);
		}
		this.view = null;

		super.destroy();

		this._fFunctionsToCallAtFrames_obj_arr = null;
		this._spineName = undefined;
	}

	on(event, fn, context) {
		if(stateEvents.indexOf(event) >= 0) {
			var data = {};
			data[event] = fn;
			this.view.state.addListener(data);

			if(context) {
				console.warn("Spine state does not support context for listeners");
			}
			return;
		}

		super.on(event, fn, context);
	}

	once(event, fn, context) {
		if(stateEvents.indexOf(event) >= 0) {
			throw Error("Spine state does not support once listeners");
			return;
		}

		super.once(event, fn, context);
	}

	removeListener(event, fn, context, once) {
		if(stateEvents.indexOf(event) >= 0) {
			if(context) {
				console.warn("Spine state does not support context for listeners");
			}

			if(once) {
				throw Error("Spine state does not support once listeners");
			}

			var data = {};
			data[event] = fn;
			this.view.state.removeListener(data);

			return;
		}

		super.removeListener(event, fn, context, once);
	}

	off(event, fn, context, once) {
		this.removeListener(event, fn, context, once);
	}

	removeAllListeners()
	{
		if (this.view && this.view.state)
		{
			this.view.state.clearListeners();
			this.view.state.onComplete = null;
			if (this.view.state.tracks && this.view.state.tracks[0])
			{
				this.view.state.tracks[0].onComplete = null;
			}
		}

		super.removeAllListeners();
	}

	clearStateListeners()
	{
		if (this.view && this.view.state)
		{
			this.view.state.clearListeners();
			this.view.state.onComplete = null;
		}
	}

	/**
	 * Set animation by name
	 * @param trackIndex
	 * @param animationName
	 * @param loop
	 * @param reverse - to play animation backward
	 */
	setAnimationByName(trackIndex, animationName, loop, reverse=false) {
		this.view.state.setAnimation(trackIndex, animationName, loop);
		this._fIsAnimationReverse_bl = reverse;
		this.view.state.timeScale = Math.abs(this.view.state.timeScale);

		if (reverse)
		{
			let lAnimationDuration_num = this.view.state.tracks[0].animation.duration / this.view.state.timeScale;
			this.view.state.timeScale *= -1;
			this.view.update(-lAnimationDuration_num);
		}
		else
		{
			this.view.update(0);
		}
	}

	/**
	 * Set default duration for animations mix.
	 * @param {number} duration 
	 */
	setAnimationsDefaultMixDuration(duration)
	{
		if (isNaN(duration) || duration <= 0)
		{
			return;
		}

		this.view.stateData.defaultMix = duration;
	}

	/**
	 * Set duration for mix between specific animations.
	 * @param {string} fromName - Name of previous aniamtion.
	 * @param {string} toName - Name of next animation.
	 * @param {number} duration - Duration of mix period.
	 */
	setAnimationMix(fromName, toName, duration)
	{
		this.view.stateData.setMix(fromName, toName, duration);
	}

	/**
	 * Get data of animation.
	 * @param {number} animationIndex 
	 * @returns {Object}
	 */
	getAnimationData(animationIndex)
	{
		let animations = this.view.spineData.animations;
		if (!animations || !animations.length || animations.length <= animationIndex)
		{
			return null;
		}

		return animations[animationIndex];
	}

	/**
	 * Checks whether animation exists or not (by name).
	 * @param {*} animationName 
	 * @returns {boolean}
	 */
	hasAnimation(animationName)
	{
		return this.view.state.hasAnimation(animationName);
	}

	/**
	 * Get array of polygons for specific meshes.
	 * @param {string[]} optMeshNames 
	 * @returns {PIXI.Polygon[]}
	 */
	calcMeshesHull(optMeshNames)
	{
		return this._calcMeshesHull(optMeshNames);
	}

	/**
	 * Add callback for specific animation and position.
	 * @param {*} a_obj 
	 * @param {Function} a_obj.function - Callback.
	 * @param {number|number[]} a_obj.timeStamps - Timestamps for callback.
	 * @param {number} a_obj.percentDelta - allowable time interval.
	 * @param {string} a_obj.animationName - Animation name.
	 */
	addCallFunctionAtStamps(a_obj)
	{
		let lDelta_num = a_obj.percentDelta || 0.017;
		let lTimeStamps_arr = Array.isArray(a_obj.timeStamps) ? a_obj.timeStamps : [a_obj.timeStamps];
		let lAnimationName_str = a_obj.animationName || this.view.state.tracks[0].animationName;

		let l_obj = {function: a_obj.function, timeStamps: lTimeStamps_arr, percentDelta: lDelta_num, animationName: lAnimationName_str};

		this._fFunctionsToCallAtFrames_obj_arr.push(l_obj);
	}

	/**
	 * remove callback callback for specific animation and position.
	 * @param {*} a_obj 
	 * @param {Function} a_obj.function - Callback.
	 * @param {number|number[]} a_obj.timeStamps - Timestamps for callback.
	 * @param {number} a_obj.percentDelta - allowable time interval.
	 * @param {string} a_obj.animationName - Animation name.
	 */
	removeCallsAtStamps(a_obj)
	{
		let l_func = a_obj.function;
		let lTimeStamps_arr = Array.isArray(a_obj.timeStamps) ? a_obj.timeStamps : [a_obj.timeStamps];

		for (let lExisting_obj of this._fFunctionsToCallAtFrames_obj_arr)
		{
			//check if the obj hase appropriate function to call (the one we want to delete)
			if (lExisting_obj.function == l_func)
			{
				
				let lIndex_num = this._fFunctionsToCallAtFrames_obj_arr.indexOf(lExisting_obj);
				//checking if we delete appropriate stamps
				if (lTimeStamps_arr.length == 1 && lTimeStamps_arr[0] == undefined)
				{
					this._fFunctionsToCallAtFrames_obj_arr.splice(lIndex_num, 1);
					return;
				}
				else
				{
					lExisting_obj.timeStamps = lExisting_obj.timeStamps.filter(el => !lTimeStamps_arr.includes(el)); //removing points from a_obj.timeStamps (lTimeStamps_arr)
					
					if (lExisting_obj.timeStamps.length == 0)
					{
						this._fFunctionsToCallAtFrames_obj_arr.splice(lIndex_num, 1);
					}
				}
			}
		}
	}

	/**
	 * Do step of animation.
	 * @param {*} e 
	 */
	tick(e) {
		//[Y] TODO investigate why sometimes exceptions happens
		if(this.playing && this.parent) {
			try
			{
				this._lastUpdateDelta = e.delta / 1000;
				
				this.view.update(this._lastUpdateDelta);

				if (this._fIsAnimationReverse_bl)
				{
					let lCurrentTrack_obj = this.view.state.tracks[0];
					if (lCurrentTrack_obj.nextAnimationLast < 0)
					{
						this.view.update(lCurrentTrack_obj.animation.duration / this.view.state.timeScale);
						this.emit("reverseAnimationCompleted");
					}
					
				}

				if (Array.isArray(this._fFunctionsToCallAtFrames_obj_arr) && this._fFunctionsToCallAtFrames_obj_arr.length)
				{
					for (let l_obj of this._fFunctionsToCallAtFrames_obj_arr)
					{
						let lCurrentTrack_obj = this.view.state.tracks[0];
						
						if (
							l_obj.timeStamps.some((e, i, ar)=>{
								return Math.abs(lCurrentTrack_obj.animationLast - e) <= l_obj.percentDelta
							})
							&& l_obj.animationName == lCurrentTrack_obj.animation.name
						)
						{
							l_obj.function.call();
						}
					}
				}

				this.emit('update');

				//DEBUG...
				// this._drawMeshHull();
				//...DEBUG
			}
			catch (e)
			{
				this.destroy();
			}
		}
	}

	/*
	*@aColor_num - main color of tint
	@aOptTintIntencity_num = [-1, 1] - smth like brightness, 1 means the whole object will be black, 
	0 means that tint color will be applied as is https://github.com/pixijs/pixi-spine/blob/master/examples/change_tint.md. 
	BTW, less than 0 means the brightness/contrast will be increased, but for some spine slots there might be artefacts
	aOptTintIntencity_num canceled, beacause the approach caused some problems with the same spines. For some enemies (who didn't need the tinet) spine was tinted.
	*/

	/**
	 * Tint spine view.
	 * @param {Number} aColor_num
	 */
	 tintIt(aColor_num)
	 {
		 this.view.tint = aColor_num;
		 // this.view.update();
	 }
 
	 /**
	  * Untint spine view.
	  */
	 untint()
	 {
		 this.view.tint = 0xffffff;
		 // this.view.update();
	 }

	 /**
	  * Add blend mode.
	  * @param {string} aBlendMode_int 
	  */
	blendIt(aBlendMode_int)
	{
		var slots = this.view.skeleton.slots;
		for (var i = 0, n = slots.length; i < n; i++)
		{
			var slot = slots[i];
			slot.blendMode = aBlendMode_int;
		}
	}

	_drawMeshHull(optMeshNames)
	{
		let gr = this._bounds_gr;
		gr.clear();

		let meshPolygons = this._calcMeshesHull(optMeshNames);

		for (let i=0; i<meshPolygons.length; i++)
		{
			let hullPolygon = meshPolygons[i];
			gr.lineStyle(2, 0x00ff00, 1);
			gr.beginFill(0x0000ff, 0.5).drawPolygon(hullPolygon).endFill();
		}
	}

	i_calcMeshesHull(optMeshNames)
	{
		return this._calcMeshesHull(optMeshNames);
	}

	_calcMeshesHull(optMeshNames)
	{
		let meshPolygons = [];

		var drawOrder = this.view.skeleton.drawOrder;
		for (var i = 0, n = drawOrder.length; i < n; i++)
		{
			var meshHulls = [];

			var slot = drawOrder[i];
			var vertices = null;
			var attachment = slot.getAttachment();
			let hullLength = 0;
			if (attachment instanceof PIXI.spine.core.MeshAttachment)
			{
				let attname = attachment.name.substr((attachment.name.indexOf("_")+1));
				if (!!optMeshNames && !!optMeshNames.length && optMeshNames.indexOf(attname) < 0)
				{
					continue;
				}
				var mesh = attachment;
				hullLength = mesh.hullLength;
				vertices = PIXI.spine.core.Utils.setArraySize([], hullLength, 0);
				mesh.computeWorldVertices(slot, 0, hullLength, vertices, 0, 2);

				if (vertices != null)
				{
					meshPolygons.push(new PIXI.Polygon(vertices));
				}
			}
		}

		return meshPolygons;
	}

	/**
	 * Update spine animation position.
	 * @param {number} dt Delta time.
	 */
	updatePosition(dt)
	{
		if (dt == null) dt = this._lastUpdateDelta;
		this.view.update(dt);
		this.emit('update');
	}
}

export default SpineSprite;