import SimpleInfo from '../../model/base/SimpleInfo';
import EventDispatcher from '../events/EventDispatcher';

/**
 * Base controller
 * @class
 * @extends EventDispatcher
 */

class SimpleController extends EventDispatcher
{
	//IL CONSTRUCTION...
	constructor(aOptInfo_usi, aOptParentController_usc)
	{
		super();

		//IL IMPLEMENTATION...
		/**
		 * Info instance
		 * @type {SimpleInfo}
		 */
		this.__fInfo_usi = null;

		/**
		 * Controller initialization marker.
		 * @type {boolean}
		 */
		this.__fInitialized_bl = false;

		/**
		 * Link to parent controller.
		 * @type {SimpleController}
		 */
		this._fParentController_usc = null;

		this._initUSimpleController(aOptInfo_usi, aOptParentController_usc);
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...
	get info()
	{
		return this.i_getInfo();
	}

	i_getInfo()
	{
		return this.__getInfo();
	}

	get parentController()
	{
		return this.i_getParentController();
	}

	i_getParentController()
	{
		return this.__getParentController();
	}

	init()
	{
		this.__init();
	}

	i_init()
	{
		this.__init();
	}

	destroy()
	{
		this.__fInitialized_bl = false;
		this.__fInfo_usi && this.__fInfo_usi.destroy();
		this.__fInfo_usi = null;
		this._fParentController_usc = null;
	}
	//...IL INTERFACE

	//ILI INIT...
	_initUSimpleController(aOptInfo_usi, aOptParentController_usc)
	{
		this.__fInfo_usi = aOptInfo_usi ? aOptInfo_usi : new SimpleInfo();
		this._fParentController_usc = aOptParentController_usc;
	}
	//...ILI INIT

	__init()
	{
		if (this.__fInitialized_bl)
		{
			throw new Error("Instance is already initialized!");
		}

		this.__fInitialized_bl = true;
		this.__initModelLevel();
		this.__initControlLevel();
	}

	__initModelLevel()
	{

	}

	__initControlLevel()
	{

	}

	__getInfo()
	{
		return this.__fInfo_usi;
	}

	__getParentController()
	{
		return this._fParentController_usc;
	}
}

export default SimpleController;