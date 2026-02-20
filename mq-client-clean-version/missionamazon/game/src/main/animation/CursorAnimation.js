import { Sequence } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';

const BASE_SCALE = 1.32;

class CursorAnimation extends Sprite
{
	startRender()
	{
		this._startRender();
	}

	stopRender()
	{
		this._stopRender();
	}

	stopUpdatePosition()
	{
		APP.ticker.off("tick", this._tick, this);
		this._fTickListening_bln = false;
	}

	startUpdatePosition()
	{
		if (this._container_sprt && this._container_sprt.visible && !this._fTickListening_bln)
		{
			APP.ticker.on("tick", this._tick, this);
			this._fTickListening_bln = true;
		}
	}

	setSpecificPosition(aPos_obj)
	{
		this._updatePosition(aPos_obj);
	}

	setScale(aScale_num)
	{
		this._setScale(aScale_num);
	}

	get isCursorRendering()
	{
		return this._container_sprt && this._container_sprt.visible;
	}

	constructor()
	{
		super();

		this._container_sprt = null;
		this._rotationContainer_sprt = null;
		this._cursorOuterView_sprt = null;
		this._cursorView_sprt = null;
		this._fTickListening_bln = false;

		if (!APP.isMobile)
		{
			this._init();
		}
	}

	_init()
	{
		this._container_sprt = this.addChild(new Sprite());
		this._container_sprt.scale.set(BASE_SCALE);
		this._container_sprt.visible = false;

		this._rotationContainer_sprt = this._container_sprt.addChild(new Sprite());

		if (!this._fTickListening_bln)
		{
			APP.ticker.on("tick", this._tick, this);
			this._fTickListening_bln = true;
		}

		this._rotationLoop();
	}

	_rotationLoop()
	{
		let l_seq = [{tweens: [{prop: "rotation", to: Math.PI*2}], duration: 60*FRAME_RATE, onfinish: ()=>this._rotationLoop()}];

		if (this._rotationContainer_sprt)
		{
			this._rotationContainer_sprt.rotation = 0;
			Sequence.start(this._rotationContainer_sprt, l_seq);
		}
	}

	_tick()
	{
		let pos = APP.stage.renderer.plugins.interaction.mouse.global;
		this._updatePosition(pos);
	}

	_updatePosition(mousePos)
	{
		if (this._container_sprt)
		{
			this._container_sprt.position.set(mousePos.x-960/2, mousePos.y-540/2);
		}
	}

	_setScale(aScale_num)
	{
		if (this._container_sprt)
		{
			this._container_sprt.scale.set(aScale_num*BASE_SCALE);
		}
	}

	_startRender()
	{
		if (!this._cursorOuterView_sprt)
		{
			this._cursorOuterView_sprt = this._rotationContainer_sprt.addChild(APP.library.getSprite("cursor/CursorCrosshair_outer"));
		}

		if (!this._cursorView_sprt)
		{
			this._cursorView_sprt = this._container_sprt.addChild(APP.library.getSprite("cursor/CursorCrosshair"));
		}

		if (this._container_sprt)
		{
			this._container_sprt.visible = true;

			if (!this._fTickListening_bln)
			{
				APP.ticker.on("tick", this._tick, this);
				this._fTickListening_bln = true;
			}

			this._tick();
		}
	}

	_stopRender()
	{
		if (this._container_sprt)
		{
			this._container_sprt.visible = false;
			APP.ticker.off("tick", this._tick, this);
			this._fTickListening_bln = false;
		}
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this._container_sprt));

		APP.ticker.off("tick", this._tick, this);

		super.destroy();

		this._container_sprt = null;
		this._rotationContainer_sprt = null;
		this._cursorOuterView_sprt = null;
		this._cursorView_sprt = null;
		this._fTickListening_bln = null;
	}
}

export default CursorAnimation;