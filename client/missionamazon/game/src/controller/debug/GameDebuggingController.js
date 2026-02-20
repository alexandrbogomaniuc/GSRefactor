import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Game from '../../Game';

class GameDebuggingController extends SimpleController {

	static get i_EVENT_TINT_UPDATE() {return 'i_EVENT_TINT_UPDATE';}
	static get i_EVENT_SHADOW_TINT_UPDATE() { return 'i_EVENT_SHADOW_TINT_UPDATE';}

	constructor()
	{		
		super();

		this._fDebugTintColor_hex = undefined;
		this._fDebugTintIntencity_num = undefined;
		this._fDebugShadowTintColor_hex = undefined;
	}

	get debugTintColor()
	{
		return this._fDebugTintColor_hex;
	}

	get debugTintIntencity()
	{
		return this._fDebugTintIntencity_num;
	}

	get debugShadowTintColor()
	{
		return this._fDebugShadowTintColor_hex;
	}
	
	//override
	__init()
	{
		super.__init();

		APP.on(Game.EVENT_DEBUG_MESSAGE, this._onDebugMessage, this);
		//0xe3c288
	}

	_onDebugMessage(e)
	{
		switch (e.message)
		{
			case 'tint':
				this._fDebugTintColor_hex = e.data.tintColor;
				this._fDebugTintIntencity_num = e.data.tintIntensity;
				this.emit(GameDebuggingController.i_EVENT_TINT_UPDATE);
				APP.handleHideWindow();
				APP.handleShowWindow();
				break;
			case 'shadowTint':
				this._fDebugShadowTintColor_hex = e.data.tintColor;
				this.emit(GameDebuggingController.i_EVENT_SHADOW_TINT_UPDATE);
				break;
		}
	}

}

export default GameDebuggingController;