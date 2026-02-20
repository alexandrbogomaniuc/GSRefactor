import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { StringUtils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const MAP_SCALE = 1.026;
const MAPS_PATH_BACK = 'maps/#level#/back_#level#';
const MAPS_PATH_DECORATION = 'maps/#level#/decoration_#level#_#index#';
const MAPS_DESCRIPTION = {
	"1":
	{
		"decoration": 
		{
			"0":
			{
				"x": 395 / 2,
				"y": 371 / 2,
				"zIndex": 190
			},
			"1":
			{
				"x": 256 / 2 + 1408 / 2,
				"y": 193.5 / 2,
				"zIndex": 57
			},
			"2":
			{
				"x": 73.5 / 2,
				"y": 206 / 2 + 317 / 2/*229 / 2 + 272 / 2*/,
				"zIndex": 263
			}
		}
	},
	"2":
	{
		"decoration":
		{
			"0":
			{
				"x": 960 / 2,
				"y": 540 / 2,
				"zIndex": 540
			},
			"1":
			{
				"x": 960 - 181 / 2,
				"y": 281 / 2,
				"zIndex": 180
			}
		}
	},
}

class MapsView extends SimpleUIView 
{
	constructor()
	{
		super();

		this._fMapId_int = undefined;

		this._fBack_sprt = undefined;
		this._fDecorations_sprt_arr = [];
		this._fTintedObjects_sprt_arr = [];
	}

	//PUBLIC...
	/**
	 * draw or redraw map
	 * @param aMapId_int map id
	 */
	i_drawMap(aMapId_int)
	{
		this.i_destroyMap();
		
		this._fMapId_int = aMapId_int;

		let lMap_obj = MAPS_DESCRIPTION[this._fMapId_int];
		let lMapScale_num = MAP_SCALE;

		let lBackPath_str = StringUtils.replaceAll(MAPS_PATH_BACK, '#level#', this._fMapId_int);
		this._fBack_sprt = this._backContainer.addChild(APP.library.getSprite(lBackPath_str));
		let dx = this._fBack_sprt.width * (lMapScale_num - 1);
		let dy = this._fBack_sprt.height * (lMapScale_num - 1);
		this._fBack_sprt.scale.set(lMapScale_num);

		this._fTintedObjects_sprt_arr = this._fTintedObjects_sprt_arr || [];

		this._fDecorations_sprt_arr = [];
		let lDecorationsNumber_int = Object.keys(lMap_obj.decoration).length;

		for (let i=0; i<lDecorationsNumber_int; i++)
		{
			let lDecoration_obj = lMap_obj.decoration[i];
			let lDecorationPath_str = StringUtils.replaceAll(MAPS_PATH_DECORATION, '#level#', this._fMapId_int);
			lDecorationPath_str = StringUtils.replaceAll(lDecorationPath_str, '#index#', i);
			
			let lDecoration_sprt = this._decorationsContainer.addChild(APP.library.getSprite(lDecorationPath_str));
			lDecoration_sprt.position.set(lDecoration_obj.x * lMapScale_num - dx/2, lDecoration_obj.y * lMapScale_num - dy/2);
			lDecoration_sprt.zIndex = lDecoration_obj.zIndex * lMapScale_num;
			lDecoration_sprt.scale.set(lMapScale_num);
			this._fDecorations_sprt_arr.push(lDecoration_sprt);

			let lTintedDecoration_sprt = lDecoration_sprt.clone();
			lTintedDecoration_sprt.tint = 0x000000;
			lTintedDecoration_sprt.alpha = 0;
			lTintedDecoration_sprt.visible = false;
			this._fTintedObjects_sprt_arr.push(this._decorationsContainer.addChild(lTintedDecoration_sprt));
		}

		//DEBUG...
		// let polygon = this.uiInfo.currentMapWalkingZone;
		// let g = new PIXI.Graphics();
		// g.beginFill(0xff0000, 0.6);
		// console.log("[Y] DrawPolygon >> ", polygon);
		// g.drawPolygon(polygon);
		// // g.drawRect(0, 0, 400, 500);
		// // g.drawPolygon(0, 259, 286, 139, 593, 153);
		// g.endFill();
		// g.zIndex = 1000000;
		// g.position.set(-960/2, -540/2);
		// this._backContainer.addChild(g);
		//...DEBUG
	}

	i_destroyMap()
	{
		this._fBack_sprt && this._fBack_sprt.destroy();
		this._fBack_sprt = null;

		while (this._fDecorations_sprt_arr.length > 0)
		{
			this._fDecorations_sprt_arr.pop().destroy();
		}
		this._fDecorations_sprt_arr = null;

		while (this._fTintedObjects_sprt_arr.length > 0)
		{
			this._fTintedObjects_sprt_arr.pop().destroy();
		}
		this._fTintedObjects_sprt_arr = null;

		this._fMapFxAnimation_obj && this._fMapFxAnimation_obj.destroy();
		this._fMapFxAnimation_obj = null;
	}

	setTintedView(aVisible_bl, aDuration_int = 0)
	{
		let lTintedObjectsAmount_int = this._fTintedObjects_sprt_arr.length;
		for (var i = 0; i < lTintedObjectsAmount_int; i++)
		{
			let lAlpha_num = aVisible_bl ? (i > 0 ? 0.4 : 1) : 0;
			let lObject_sprt = this._fTintedObjects_sprt_arr[i];

			if (aDuration_int > 0)
			{
				if (aVisible_bl)
				{
					lObject_sprt.visible = true;
					lObject_sprt.fadeTo(lAlpha_num, aDuration_int);
				}
				else
				{
					lObject_sprt.fadeTo(lAlpha_num, aDuration_int, null, this.hideTintedObject.bind(this, lObject_sprt, i));
				}
			}
			else
			{
				lObject_sprt.visible = aVisible_bl;
				lObject_sprt.alpha = lAlpha_num;
			}
		}
	}

	hideTintedObject(aObject_sprt)
	{
		aObject_sprt.visible = false;
	}

	destroy()
	{
		this.i_destroyMap();

		this._fMapId_int = undefined;

		this._fBack_sprt = undefined;
		this._fDecorations_sprt_arr = null;
		this._fTintedObjects_sprt_arr = null;

		super.destroy();
	}
	//...PUBLIC

	//PRIVATE...
	get _backContainer ()
	{
		return APP.currentWindow.gameField.mapContainers.backContainer;
	}

	get _decorationsContainer ()
	{
		return APP.currentWindow.gameField.mapContainers.decorationsContainer;
	}
	//...PRIVATE
}

export default MapsView;