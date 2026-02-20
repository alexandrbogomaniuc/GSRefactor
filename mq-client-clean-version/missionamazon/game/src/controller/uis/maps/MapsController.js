import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import MapsInfo from '../../../model/uis/maps/MapsInfo';
import MapsView from '../../../view/uis/maps/MapsView';
import GameScreen from '../../../main/GameScreen';
import SubloadingController from '../../subloading/SubloadingController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class MapsController extends SimpleUIController {

	static get EVENT_LOAD_MAP() 					{return "eventLoadMap";}
	static get EVENT_LOAD_MAP_IN_BACKGROUND() 		{return "eventLoadMapInBackground";}
	static get EVENT_CURRENT_MAP_ID_UPDATED()		{return "eventCurrentMapIdUpdated";}

	constructor()
	{
		super(new MapsInfo(), new MapsView());

		this._gameScreen = null;
	}

	__init()
	{
		super.__init();

		this._gameScreen = APP.currentWindow;

		this._fSubloadingController_sc = this._gameScreen.subloadingController;
		this._fSubloadingInfo_si = this._fSubloadingController_sc.i_getInfo();

		this._gameScreen.on(GameScreen.EVENT_ON_GAME_FIELD_CLEARED, this._onGameFieldCleared, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_INFO_UPDATED, this._onRoomInfoUpdated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_NEXT_MAP_UPDATED, this._onNextMapUpdated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_DRAW_MAP, this._onTimeToDrawMap, this);
	}

	_onRoomInfoUpdated(aRoom_obj)
	{
		let lInfo_msi = this.i_getInfo();

		if (lInfo_msi.mapId !== aRoom_obj.mapId)
		{
			lInfo_msi.mapId = aRoom_obj.mapId;
			this.emit(MapsController.EVENT_CURRENT_MAP_ID_UPDATED);
		}
	}

	_onNextMapUpdated(aValue_obj)
	{
		let lInfo_msi = this.i_getInfo();

		lInfo_msi.nextMapId = aValue_obj.nextMapId || lInfo_msi.mapId;

		if (!this._fSubloadingInfo_si.i_isMapLoaded(lInfo_msi.nextMapId)
			&& !this._fSubloadingInfo_si.i_isMapLoading(lInfo_msi.nextMapId))
		{
			this.emit(MapsController.EVENT_LOAD_MAP_IN_BACKGROUND, {mapId: lInfo_msi.nextMapId});
		}	
	}

	_onTimeToDrawMap()
	{
		this._fSubloadingController_sc.off(SubloadingController.EVENT_ON_MAP_LOADING_COMPLETED, this._onMapSubloadingCompleted, this);

		let lInfo_msi = this.i_getInfo();
		if (this._fSubloadingInfo_si.i_isMapLoaded(lInfo_msi.mapId))
		{
			this._drawMap();
		}
		else
		{
			this.emit(MapsController.EVENT_LOAD_MAP, {mapId: lInfo_msi.mapId});
			this._fSubloadingController_sc.on(SubloadingController.EVENT_ON_MAP_LOADING_COMPLETED, this._onMapSubloadingCompleted, this);
		}
	}

	_onMapSubloadingCompleted(aEvent_obj)
	{
		let lInfo_msi = this.i_getInfo();
		if (aEvent_obj.mapId === lInfo_msi.mapId)
		{
			this._fSubloadingController_sc.off(SubloadingController.EVENT_ON_MAP_LOADING_COMPLETED, this._onMapSubloadingCompleted, this);
			this._drawMap();
		}
	}

	_drawMap()
	{
		let lInfo_msi = this.i_getInfo();
		let currentMapId = lInfo_msi.mapId;

		this.view.i_drawMap(currentMapId);
	}

	_onGameFieldCleared()
	{
		this.view.setTintedView(false, 0);
	}

	destroy()
	{
		if (this._gameScreen)
		{
			this._gameScreen.off(GameScreen.EVENT_ON_ROOM_INFO_UPDATED, this._onRoomInfoUpdated, this);
			this._gameScreen.off(GameScreen.EVENT_ON_NEXT_MAP_UPDATED, this._onNextMapUpdated, this);
			this._gameScreen.off(GameScreen.EVENT_ON_DRAW_MAP, this._onTimeToDrawMap, this);
			this._gameScreen = null;
		}

		this._fSubloadingController_sc = null;
		this._fSubloadingInfo_si = null;

		super.destroy();
	}
}

export default MapsController;