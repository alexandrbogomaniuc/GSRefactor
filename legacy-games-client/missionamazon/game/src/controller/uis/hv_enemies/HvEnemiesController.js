import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import GameScreen from '../../../main/GameScreen';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import HvEnemyController from './HvEnemyController';

class HvEnemiesController extends SimpleController {

	static get EVENT_TIME_TO_CREATE_HV_ENEMY() 	{return HvEnemyController.EVENT_TIME_TO_CREATE_HV_ENEMY;}
	static get EVENT_SET_HV_ENEMY_TIME_OFFSET()	{return HvEnemyController.EVENT_SET_HV_ENEMY_TIME_OFFSET;}

	constructor() {
		super();

		this._fEnemyControllers_hec_obj = {};
	}

	__init()
	{
		super.__init();

		this._gameScreen = APP.currentWindow;
		this._gameScreen.on(GameScreen.EVENT_ON_NEW_HV_ENEMY, this._onNewHvEnemy, this);
		this._gameScreen.on(GameScreen.EVENT_DESTROY_ENEMY, this._onDestroyEnemy, this);
	}

	//PUBLIC...
	i_clearAll()
	{
		for (let lEnemyId_num in this._fEnemyControllers_hec_obj)
		{
			let lHvEnemyController_hec = this._getEnemyControllerById(lEnemyId_num);
			lHvEnemyController_hec && lHvEnemyController_hec.destroy();
		}

		this._fEnemyControllers_hec_obj = {};
	}
	//...PUBLIC

	_onNewHvEnemy(aEvent_obj)
	{
		let data = aEvent_obj.data;
		this._addEnemyController(data);
	}

	_addEnemyController(aEnemyData_obj)
	{
		let l_hec = new HvEnemyController(aEnemyData_obj);
		l_hec.i_init();
		l_hec.once(HvEnemyController.EVENT_TIME_TO_CREATE_HV_ENEMY, this.emit, this);
		l_hec.once(HvEnemyController.EVENT_SET_HV_ENEMY_TIME_OFFSET, this.emit, this);
		let lEnemyId_num = aEnemyData_obj.id;
		this._fEnemyControllers_hec_obj[lEnemyId_num] = l_hec;
	}

	_onDestroyEnemy(aEvent_obj)
	{
		let lEnemyId_num = aEvent_obj.enemyId;
		let lHvEnemyController_hec = this._getEnemyControllerById(lEnemyId_num);
		if (lHvEnemyController_hec)
		{
			lHvEnemyController_hec.destroy();
			this._fEnemyControllers_hec_obj[lEnemyId_num] = null;
		}		
	}

	_getEnemyControllerById(aEnemyId_num)
	{
		return this._fEnemyControllers_hec_obj[aEnemyId_num];
	}

	_removeEnemyController()
	{
		//TODO
	}

	destroy()
	{
		if (this._gameScreen)
		{
			this._gameScreen.off(GameScreen.EVENT_ON_NEW_HV_ENEMY, this._onNewHvEnemy, this);
			this._gameScreen.off(GameScreen.EVENT_DESTROY_ENEMY, this._onDestroyEnemy, this);
			this._gameScreen = null;
		}

		this.i_clearAll();
		this._fEnemyControllers_hec_obj = null;

		super.destroy();
	}

	
}

export default HvEnemiesController;