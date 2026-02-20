import EventDispatcher from '../../../controller/events/EventDispatcher';
import { ALIGN, VALIGN } from '../../../controller/main/globals';
import { Utils } from '../../../model/Utils';
import Sprite from './Sprite';
import { IPAD } from '../../layout/features';
/**
 * Stage.
 * @class
 */
class Stage extends EventDispatcher {

	static get EVENT_ON_WEBGL_CONTEXT_LOST()		{return "EVENT_ON_WEBGL_CONTEXT_LOST";}
	static get EVENT_ON_WEBGL_CONTEXT_RESTORED()	{return "EVENT_ON_WEBGL_CONTEXT_RESTORED";}

	constructor(layout, config, scale=1, type=0, wrapperName) {
		super();

		this.wrapperName = wrapperName;
		
		this.config = Utils.clone(config);
		this.layout = layout;

		this.screen = layout.addScreen(wrapperName);
		
		var options = {
			view: this.screen.canvas,
			resolution: scale,
			backgroundAlpha: config.transparentStage == undefined ? 0 : Number(!config.transparentStage),
			forceCanvas: (type == PIXI.RENDERER_TYPE.CANVAS),
			width: config.size.width,
			height: config.size.height
		};

		this.renderer = PIXI.autoDetectRenderer(options);
		// console.log("** new Stage renderer type:", this.renderer.type, "; name:", wrapperName);
		
		if (this.renderer.type == PIXI.RENDERER_TYPE.WEBGL)
		{
			let cvs = this.renderer.gl.canvas;
			cvs.addEventListener("webglcontextlost", this._handleContextLost.bind(this), false);
			cvs.addEventListener("webglcontextrestored", this._handleContextRestored.bind(this), false);

			//debug...
			// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
			//...debug
		}

		this.layout.on("fitlayout", this._onFitLayout, this);

		this.view = new Sprite();
		this.view.stageRenderer = this.renderer;
		
		this.viewAlign = {
			x: ALIGN.CENTER,
			y: VALIGN.MIDDLE
		};

		this.autoRender = true;

		this.alignView();

		this._fCurrentScale_num = 1;
	}

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 109) //-
	// 	{
	// 		this.renderer.gl.getExtension('WEBGL_lose_context').loseContext();
	// 	}
	// }
	//...DEBUG

	_handleContextLost(event)
	{
		console.log("[Stage] _handleContextLost");
		this.emit(Stage.EVENT_ON_WEBGL_CONTEXT_LOST);
	}

	_handleContextRestored(event)
	{
		console.log("[Stage] _handleContextRestored");
		this.emit(Stage.EVENT_ON_WEBGL_CONTEXT_RESTORED);
	}

	get isWebglContextLost()
	{
		return this.renderer 
				&& this.renderer.type == PIXI.RENDERER_TYPE.WEBGL
				&& this.renderer.gl 
				&& this.renderer.gl.isContextLost()
	}

	get currentScale()
	{
		return this._fCurrentScale_num;
	}

	_onFitLayout(e)
	{
		let newWidht = e.screenWidth;
		let newHeight = e.screenHeight;
		if(IPAD){
			newWidht = newWidht *3;
			newHeight = newHeight *3;
		}
		e.screenHeight = newHeight;
		e.screenWidth = newWidht;
		this.emit('resize', e);
		this._fCurrentScale_num = e.scale;
		if(this.layout.autoFill) {
			this.resize(newWidht, newHeight);
		}
	}

	/**
	 * Align view on stage.
	 * @param {Number} align 
	 * @param {Number} valign 
	 */
	alignView(align=this.viewAlign.x, valign=this.viewAlign.y) {

		this.viewAlign.x = align;
		this.viewAlign.y = valign;

		let x = 0, y = 0;
		let sw = this.screen.canvas.width / this.layout.bitmapScale;
		let sh = this.screen.canvas.height / this.layout.bitmapScale;

		if(align == ALIGN.CENTER) x = sw/2;
		if(align == ALIGN.RIGHT) x = sw;

		if(valign == VALIGN.MIDDLE) y = sh/2;
		if(valign == VALIGN.BOTTOM) y = sh;

		this.view.position.set(x, y);
	}

	/**
	 * Resize stage.
	 * @param {number} width 
	 * @param {number} height 
	 */
	resize(width, height) {
		this.renderer.resize(width, height);
		this.alignView();
	}

	/**
	 * Render view on stage.
	 */
	render() {
		this.renderer.render(this.view);
	}

	/**
	 * Auto render view on ticker tick, if autoRender is on.
	 * @param {number} delta 
	 */
	tick(delta) {
		if(!this.autoRender) return;

		this.render();
	}

	/**
	 * Destroy stage instance.
	 */
	destroy()
	{
		this.wrapperName = undefined;

		this.config = null;
		
		this.layout.removeScreen(this.screen);
		this.screen = null;

		this.view.destroy();
		this.view = null;

		this.viewAlign = null;

		this.autoRender = undefined;

		this.renderer.destroy();
		this.renderer = null;

		this.layout.off("fitlayout", this._onFitLayout, this);
		this.layout = null;

		super.destructor();
	}
}

export default Stage;