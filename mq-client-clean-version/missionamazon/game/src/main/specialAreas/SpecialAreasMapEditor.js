import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SpecialAreasMap from './SpecialAreasMap';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

/*
	SpecialAreasMapEditor is to create any zone on the map.

	Just create new SpecialAreasMapEditor.

	Controls:
	LMB - draw zones,
	RMB - clear zones
	key P - log to console
	---
	
	Bitmap from console should be added to SpecialAreasMap class with unique ID
*/

class SpecialAreasMapEditor
{
	constructor()
	{
		this._fGameField_gf = APP.gameScreen.gameField;
		this._fSpecialAreasMap_sam = this._fGameField_gf.areasMap;

		this._fBitMap_int_arr_arr = [];
		this._fIsLMBDown_bl = false;
		this._fIsRMBDown_bl = false;

		for( let y = 0; y < SpecialAreasMap.HEIGHT; y++ )
		{
			this._fBitMap_int_arr_arr[y] = [];

			for( let x = 0; x < SpecialAreasMap.WIDTH; x++ )
			{
				this._fBitMap_int_arr_arr[y][x] = 0;
			}
		}

		// document.addEventListener("keyup", this._print.bind(this));

		//CONTAINER...
		let l_s = new Sprite();
		l_s.zIndex = 10000000;
		l_s.position.set(-960 / 2, -540 / 2);
		l_s.on('mousedown', (e)=> this._onMouseDown(e));
		l_s.on('rightdown', (e)=> this._onRigthMouseDown(e));
		l_s.on('rightup', (e)=> this._onRigthMouseUp(e));
		l_s.on('mouseup', (e)=> this._onMouseUp(e));
		l_s.on('mousemove', (e)=> this._onMouseMove(e));
		l_s.on('rightupoutside', (e)=> this._drop(e));
		l_s.on('mouseupoutside', (e)=> this._drop(e));
		l_s.on('mouseout', (e)=> this._drop(e));
		this._fContainer_s = APP.gameScreen.addChild(l_s);
		//...CONTAINER

		//EDIT EXISTANT BIT MAP IF REQUIRED...
		//this._applyBitMap("000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001111101101111110111111011000000000000000000000001111101101111110111111011000000000000000000000001100000001100110110011011000000000000000000000001100001101100110110011011000000000000000000000001100001101111110110011011000000000000000000000001100001101111110110011000000000000000000000000001111101101100110111111011000000000000000000000001111101101100110111111011000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
		//...EDIT EXISTANT BIT MAP IF REQUIRED

		this._redraw();
	}


	_applyBitMap(aBitMap_str)
	{
		let lX_int = 0;
		let lY_int = 0;

		for( let i = 0; i < aBitMap_str.length; i++ )
		{
			this._fBitMap_int_arr_arr[lY_int][lX_int] = Number.parseInt(aBitMap_str[i]);

			lX_int++;

			if(lX_int === SpecialAreasMap.WIDTH)
			{
				lX_int = 0;
				lY_int++;
			}

		}

		this._redraw();
	}

	_drop()
	{
		this._fIsLMBDown_bl = false;
		this._fIsRMBDown_bl = false;
	}

	_onRigthMouseDown(aEvent_obj)
	{
		this._fIsLMBDown_bl = false;
		this._fIsRMBDown_bl = true;

		let lX_num = aEvent_obj.data.global.x;
		let lY_num = aEvent_obj.data.global.y;

		this._onInteraction(lX_num, lY_num);
	}

	_onMouseDown(aEvent_obj)
	{
		this._fIsLMBDown_bl = true;
		this._fIsRMBDown_bl = false;

		let lX_num = aEvent_obj.data.global.x;
		let lY_num = aEvent_obj.data.global.y;

		this._onInteraction(lX_num, lY_num);
	}

	_onMouseUp(aEvent_obj)
	{
		this._fIsLMBDown_bl = false;
	}

	_onRigthMouseUp()
	{
		this._fIsRMBDown_bl = false;
	}

	_onMouseMove(aEvent_obj)
	{
		let lX_num = aEvent_obj.data.global.x;
		let lY_num = aEvent_obj.data.global.y;

		this._onInteraction(lX_num, lY_num);
	}

	_onInteraction(aX_num, aY_num)
	{
		let lResultValue_int = -1;

		if(this._fIsLMBDown_bl)
		{
			lResultValue_int = 1;
		}
		else if(this._fIsRMBDown_bl)
		{
			lResultValue_int = 0;
		}

		if(lResultValue_int === -1)
		{
			return;
		}

		let lX_int = Math.trunc(aX_num / SpecialAreasMap.CELL_SIZE);
		let lY_int = Math.trunc(aY_num / SpecialAreasMap.CELL_SIZE);

		if(
			lX_int < 0 ||
			lY_int < 0 ||
			lX_int >= SpecialAreasMap.WIDTH ||
			lY_int >= SpecialAreasMap.HEIGHT
			)
		{
			return;
		}

		let lIsRedrawRequired_bl = (this._fBitMap_int_arr_arr[lY_int][lX_int] !== lResultValue_int);

		this._fBitMap_int_arr_arr[lY_int][lX_int] = lResultValue_int;

		if(lIsRedrawRequired_bl)
		{
			this._redraw();	
		}
	}

	_redraw()
	{

		let lCellSize_int = SpecialAreasMap.CELL_SIZE;
		let lWidth_int = SpecialAreasMap.WIDTH;
		let lHeight_int = SpecialAreasMap.HEIGHT;


		let l_s = this._fContainer_s;

		l_s.destroyChildren();
		let l_g = new PIXI.Graphics();

		l_g.beginFill(0X000000, 0.01);
		l_g.drawRect(0, 0, 960, 540);
		l_g.endFill();

		for( let y = 0; y < lHeight_int; y++ )
		{
			for( let x = 0; x < lWidth_int; x++ )
			{
				if(this._fBitMap_int_arr_arr[y][x] === 1)
				{
					l_g.beginFill(0XFF0000, 0.75);
				}
				else
				{
					l_g.beginFill(0X000000, 0.15);
				}

				l_g.drawRect(
					x * lCellSize_int + 1,
					y * lCellSize_int + 1,
					lCellSize_int - 2,
					lCellSize_int - 2);
				l_g.endFill();
			}
		}


		l_s.addChild(l_g);
	
	}

	_print(aEvent_obj)
	{

		if(aEvent_obj.keyCode !== 80) //P(rint)
		{
			return;
		}

		let l_str = "";

		for( let y = 0; y < SpecialAreasMap.HEIGHT; y++ )
		{
			for( let x = 0; x < SpecialAreasMap.WIDTH; x++ )
			{
				l_str += this._fBitMap_int_arr_arr[y][x] + "";
			}
		}

		console.log(l_str);
	}
}


export default SpecialAreasMapEditor;