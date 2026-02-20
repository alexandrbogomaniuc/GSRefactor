import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';

class PreloaderTooltip extends Sprite
{	
	static get END_OF_HIDE_ANIMATION() 	{return "endHideAnimation";}

	get TOOLTIP_SCREEN()
	{
		return {
			0:	"preloader/tips/tip_screen_0",
			1:  "preloader/tips/tip_screen_1",
			2:	"preloader/tips/tip_screen_2",
			3:	"preloader/tips/tip_screen_3",
			4:	"preloader/tips/tip_screen_4",};
	}

	get DESCTOP_TOOLTIP_TEXT()
	{
		return {
			0:	"TAPreloaderTip0",
			1:	"TAPreloaderTip1",
			2:	"TAPreloaderTip2",
			3:	this.DESCTOP_TOOLTIP_TEXT_3,
			4:	"TAPreloaderTip4",
		};
	}

	get DESCTOP_TOOLTIP_TEXT_3()
	{
		return [
		{src: "TAPreloaderTip3_1", x: -90, y: 60},
		{src: "TAPreloaderTip3_2", x: -120, y: 149},
		{src: "TAPreloaderTip3_3", x: 80, y: 60},
		{src: "TAPreloaderTip3_4", x: 115, y: 149},
		];
	}
	
	get MOBILE_TOOLTIP_TEXT()
	{
		return {
			0:	"TAPreloaderMobileTip0",
			1:  "TAPreloaderTip1",
			2:	"TAPreloaderTip2",
			3:	this.DESCTOP_TOOLTIP_TEXT_3,
			4:	"TAPreloaderMobileTip4",
		};
	}

	get ELLIPSE_CONFIGS()
	{
		return {
			0: {src: "preloader/tips/ellipse_tip_0", x: -8, y: 40},
			1: {src: "preloader/tips/ellipse_tip_1", x: 10, y: 90},
			2: {src: "preloader/tips/ellipse_tip_2", x: -83, y: 25},
			3: null,
			4: {src: "preloader/tips/ellipse_tip_0", x: -14, y: 30},
		}
	}


	show()
	{
		this._showTip();
	}

	get id()
	{
		return this._fTooltipID;
	}

	constructor(aTooltipID)
	{
		super();

		this._fTooltipID = aTooltipID;

		if (APP.isMobile)
		{
			this.TOOLTIP_TEXT = this.MOBILE_TOOLTIP_TEXT;
			this.ARROW_POSITION = this.MOBILE_ARROW_POSITION;
		}
		else
		{
			this.TOOLTIP_TEXT = this.DESCTOP_TOOLTIP_TEXT;
			this.ARROW_POSITION = this.DESCTOP_ARROW_POSITION;
		}

		this.visible = false;
		this.alpha = 0;

		this._addContent();
	}

	_addContent()
	{
		let tooltipScreen = this.addChild(APP.library.getSprite(this.TOOLTIP_SCREEN[this._fTooltipID]));

		let ellipseConf = this.ELLIPSE_CONFIGS[this._fTooltipID];
		if (ellipseConf)
		{
			let ellipse = this.addChild(APP.library.getSprite(ellipseConf.src));
			ellipse.position.set(ellipseConf.x, ellipseConf.y);
		}

		if(Array.isArray(this.TOOLTIP_TEXT[this._fTooltipID]))
		{
			let larray_str = this.TOOLTIP_TEXT[this._fTooltipID];
			for(let i = 0; i < larray_str.length; i ++){
				let tooltipText = this.addChild(I18.generateNewCTranslatableAsset(larray_str[i].src));
				tooltipText.position.set(larray_str[i].x, larray_str[i].y);
			}
		}
		else
		{
			let tooltipText = this.addChild(I18.generateNewCTranslatableAsset(this.TOOLTIP_TEXT[this._fTooltipID]));
			tooltipText.position.set(0, 149);
		}
	}

	_showTip()
	{
		this.visible = true;

		let show = [{
			tweens: [{ prop: "alpha", to: 1 }],
			duration: 800,
			ease: Easing.linear.easeOut,
			onfinish: () =>
			{
				this._fIntervalTimer_tmr = new Timer(this._hideTip.bind(this), 2000, true);
				this._fIntervalTimer_tmr.start();
			}
		}];

		Sequence.start(this, show);
	}

	_hideTip()
	{
		if (this._fIntervalTimer_tmr) this._fIntervalTimer_tmr.destructor();

		let hide = [{
			tweens: [{ prop: "alpha", to: 0 }],
			duration: 800,
			ease: Easing.linear.easeOut,
			onfinish: () =>
			{
				this.visible = false;
				this.emit(PreloaderTooltip.END_OF_HIDE_ANIMATION);
			}
		}];

		Sequence.start(this, hide);
	}

	destroy() 
	{
		Sequence.destroy(Sequence.findByTarget(this));
		
		super.destroy();

		if (this._fIntervalTimer_tmr) this._fIntervalTimer_tmr.destructor();
		this._fTooltipID = null;
	}
}

export default PreloaderTooltip;