import SimpleUIController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import GameScreen from '../../main/GameScreen';
import SubloadingInfo from '../../model/subloading/SubloadingInfo';
import SubloadingView from '../../view/subloading/SubloadingView';
import MapsController from '../uis/maps/MapsController';
import {createLoader} from '../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders';
import Queue from '../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/Queue';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MapsInfo from '../../model/uis/maps/MapsInfo';
import GameBonusController from './../uis/custom/bonus/GameBonusController';
import TournamentModeController from '../custom/tournament/TournamentModeController';

class SubloadingController extends SimpleUIController {

	static get EVENT_ON_MAP_LOADING_COMPLETED() {return "eventOnMapLoadingCompleted"};
	static get EVENT_ON_MAP_LOADING_ERROR() 	{return "eventOnMapLoadingError"};

	get isLoadingScreenShown()
	{
		return this.view && this.view.isLoadingScreenShown;
	}

	constructor() {
		super(new SubloadingInfo(), new SubloadingView());

		this._gameScreen = null;
		this._fTournamentModeInfo_tmi = null;
	}

	i_showLoadingScreen()
	{
		this.view.i_showLoadingScreen(this._gameScreen.gameField.subloadingContainerInfo);
	}

	i_hideLoadingScreen()
	{
		this.view.i_hideLoadingScreen();
	}

	i_bonusLoadingCancelRequired()
	{
		this._onBonusLoadinCancelRequired();
	}

	__init()
	{
		super.__init();
		this._gameScreen = APP.currentWindow;
		this._loaders = [];

		this._fMapsController_mc = this._gameScreen.mapsController;
		this._fMapsController_mc.on(MapsController.EVENT_LOAD_MAP, this._loadMap, this);
		this._fMapsController_mc.on(MapsController.EVENT_LOAD_MAP_IN_BACKGROUND, this._loadMapInBackground, this);

		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_SERVER_STATE_CHANGED, this._onTournamentModeServerStateChanged, this);
		tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_CLIENT_STATE_CHANGED, this._onTournamentModeClientStateChanged, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}

	_loadMap(aEvent_obj, aInBackground_bl = false)
	{
		if (this._fTournamentModeInfo_tmi.isTournamentOnServerCompletedState)
		{
			return;
		}

		let lMapId_int = aEvent_obj.mapId;

		let lInfo_si = this.i_getInfo();

		if (lInfo_si.i_isMapLoaded(lMapId_int))
		{
			throw new Error(`The map ${lMapId_int} is already loaded!`);
		}

		if (!aInBackground_bl && !this.view.isLoadingScreenShown)
		{
			this.view.i_showLoadingScreen(this._gameScreen.gameField.subloadingContainerInfo);
		}

		if (lInfo_si.i_isMapLoading(lMapId_int))
		{
			//map is loading
			return;
		}

		let lMapAssets_obj_arr = MapsInfo.getMapAssets(lMapId_int);

		lInfo_si.i_setMapLoading(lMapId_int);
		lInfo_si.loadingInProgress = true;

		let loader = new Queue();

		loader.add
		(
			APP.library.createLoaderQueue(lMapAssets_obj_arr),
		);

		loader.once('error', this._onMapAssetsLoadingError, this);
		loader.once('complete', this._onMapAssetsLoadingComplete.bind(this, lMapId_int));
		loader.load();

		this._loaders.push(loader);
	}

	_loadMapInBackground(aEvent_obj)
	{
		this._loadMap(aEvent_obj, true);
	}

	_onMapAssetsLoadingError(e)
	{
		this.emit(SubloadingController.EVENT_ON_MAP_LOADING_ERROR);
		APP.handleAssetsLoadingError(e.key, e.message);
	}

	_onMapAssetsLoadingComplete(aMapId_int)
	{
		let lInfo_si = this.i_getInfo();
		lInfo_si.i_setMapLoaded(aMapId_int);
		lInfo_si.loadingInProgress = false;
		this.view.i_hideLoadingScreen();

		this.emit(SubloadingController.EVENT_ON_MAP_LOADING_COMPLETED, {mapId: aMapId_int});
	}

	_onBonusLoadinCancelRequired()
	{
		while (this._loaders && this._loaders.length)
		{
			let loader = this._loaders.pop();
			loader.removeAllListeners();
			loader.stopLoading();
		}

		this.i_hideLoadingScreen();
	}

	//TOURNAMENT...
	_onTournamentModeServerStateChanged(event)
	{
		let l_cpv = this.view;
		let lTournamentModeInfo_tmi = this._fTournamentModeInfo_tmi;
		
		if (lTournamentModeInfo_tmi.isTournamentOnServerCompletedState)
		{
			while (this._loaders && this._loaders.length)
			{
				let loader = this._loaders.pop();
				loader.removeAllListeners();
				loader.stopLoading();
			}
		}		
	}

	_onTournamentModeClientStateChanged(event)
	{
		if (this._fTournamentModeInfo_tmi.isTournamentOnClientCompletedState)
		{
			this.i_hideLoadingScreen();
		}
	}
	//...TOURNAMENT

	destroy()
	{
		this._gameScreen = null;
		
		if (this._fMapsController_mc)
		{
			this._fMapsController_mc.off(MapsController.EVENT_LOAD_MAP, this._loadMap, this);
			this._fMapsController_mc.off(MapsController.EVENT_LOAD_MAP_IN_BACKGROUND, this._loadMapInBackground, this);
			this._fMapsController_mc = null;
		}
		
		while (this._loaders.length)
		{
			let loader = this._loaders.pop();
			loader.removeAllListeners();
		}
		this._loaders = null;

		super.destroy();
	}
}

export default SubloadingController;