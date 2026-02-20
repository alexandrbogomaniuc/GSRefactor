import Button from '../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class MainPLayerSpotHitZone extends Button {

	static get TYPE_HIT_ZONE_CIRCLE()	{return 1;}
	static get TYPE_HIT_ZONE_RECT()		{return 2;}

	constructor(type_int, param_obj) {
		super();

		this._fTypeArea_int = type_int;
		this._fParam_obj = param_obj;

		this._init()
		this._initButtonBehaviour();

		var cursorController = APP.currentWindow.cursorController;
		this.mouseover = () => cursorController.setOverRestrictedZone(true);
		this.mouseout = () => cursorController.setOverRestrictedZone(false);
	}

	_init()
	{
		//DEBUG... 
		//Needed to highlight buttons
		// this._fButton_b = this.addChild(new PIXI.Graphics());

		// if (this.isAllParamsForAreaExists())
		// {
		// 	switch(this._fTypeArea_int)
		// 	{
		// 		case MainPLayerSpotHitZone.TYPE_HIT_ZONE_CIRCLE:
		// 				this._fButton_b.beginFill(0xFF0000).drawCircle(this._fParam_obj.x, this._fParam_obj.y, this._fParam_obj.width, this._fParam_obj.height).endFill();	
		// 			break;
		// 		case MainPLayerSpotHitZone.TYPE_HIT_ZONE_RECT:
		// 				this._fButton_b.beginFill(0xFF0000).drawRect(this._fParam_obj.x, this._fParam_obj.y, this._fParam_obj.width, this._fParam_obj.height).endFill();	
		// 			break;
		// 		default:
		// 			break;
		// 	}
		// }
		
		// this._fButton_b.alpha = 0.2;
		//...DEBUG
	}

	_initButtonBehaviour()
	{
		if (this.isAllParamsForAreaExists())
		{
			
			switch(this._fTypeArea_int)
			{
				case MainPLayerSpotHitZone.TYPE_HIT_ZONE_CIRCLE:
						this.setHitArea(new PIXI.Circle(this._fParam_obj.x, this._fParam_obj.y, this._fParam_obj.width, this._fParam_obj.height));
						this.setEnabled();
					break;
				case MainPLayerSpotHitZone.TYPE_HIT_ZONE_RECT:
						this.setHitArea(new PIXI.Rectangle(this._fParam_obj.x, this._fParam_obj.y, this._fParam_obj.width, this._fParam_obj.height));
						this.setEnabled();
					break;
				default:
					break;
			}
		}
		else
		{
			console.log("[WARNING] Not all parameters for the zone are available!");
		}

		this.on("pointerdown", (e)=>e.stopPropagation(), this);
		this.on("pointerclick", (e)=>e.stopPropagation(), this);
	}

	isAllParamsForAreaExists()
	{
		return this._fParam_obj.x != undefined && this._fParam_obj.y != undefined && this._fParam_obj.width != undefined && this._fParam_obj.height != undefined;
	}

	destroy()
	{
		this.off("pointerdown", (e)=>e.stopPropagation(), this);
		this.off("pointerclick", (e)=>e.stopPropagation(), this);

		this._fTypeArea_int = null;
		this._fParam_obj = null;

		super.destroy();
	}
}

export default MainPLayerSpotHitZone;