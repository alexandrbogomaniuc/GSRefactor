import SimpleController from '../../base/SimpleController';
import SimpleUIInfo from '../../../model/uis/SimpleUIInfo';
import SimpleUIView from '../../../view/base/SimpleUIView';

/**
 * @class
 * @classdesc Base UI controller.
 */
class SimpleUIController extends SimpleController
{
	constructor(aOptInfo_usuii, aOptView_uo, aOptParentController_usc)
	{
		super (aOptInfo_usuii || new SimpleUIInfo, aOptParentController_usc);

		/**
		 * View instance.
		 * @type {SimpleUIView}
		 */
		this.__fView_uo = null;

		/**
		 * Creates __fView_uo instance.
		 */
		this._fViewLevelSelfInitializationViewProvider_uo = null;

		this._initSimpleUIController(aOptView_uo);
	}

	/**
	 * View initialization.
	 * @param {SimpleUIView} aView_uo 
	 */
	initView(aView_uo)
	{
		this._initView(aView_uo);
	}

	/** Destroy controller instance. */
	destroy()
	{
		this.__fView_uo && this.__fView_uo.destroy();
		this.__fView_uo = null;

		this._fViewLevelSelfInitializationViewProvider_uo = null;
		
		super.destroy();
	}

	/** Returns link to a view instance. Creates view instance if it's not still created and view self initialization is allowed. */
	get view ()
	{
		if (
				!this.__fView_uo
				&& this._fViewLevelSelfInitializationViewProvider_uo
			)
		{
			this.__forceViewLevelSelfInitialization();
		}

		return this.__fView_uo;
	}

	/** Indicates whether view is already created or not. */
	get hasView()
	{
		return this.__fView_uo !== undefined
				&& this.__fView_uo !== null;
	}

	/** Registers object that creates view if view self initialization is allowed. */
	initViewLevelSelfInitializationViewProvider (aViewLevelSelfInitializationViewProvider_uo)
	{
		this._initViewLevelSelfInitializationViewProvider(aViewLevelSelfInitializationViewProvider_uo);
	}

	/** Indicates whether view self initialization mode is on or not. */
	get isViewLevelSelfInitializationMode ()
	{
		return this.__isViewLevelSelfInitializationAllowed();
	}

	_initSimpleUIController(aOptView_uo)
	{
		if (
				aOptView_uo !== undefined
				&& aOptView_uo !== null
			)
		{
			this.__fView_uo = aOptView_uo;
		}
	}

	__init ()
	{
		super.__init();

		if (this.__fView_uo)
		{
			this.__initViewLevel();
		}
		else if (this.__isViewLevelSelfInitializationAllowed())
		{
			this.__initViewLevelSelfInitialization();
		}
	}

	_initView(aView_uo)
	{
		if (this.__fView_uo)
		{
			throw new Error("View is already initialized!");
		}

		if (!this.__fInitialized_bl)
		{
			throw new Error("View cannot be initialized before i_init() call!");
		}
		
		this.__fView_uo = aView_uo;
		
		this.__initViewLevel();
	}

	__initViewLevel()
	{
		this.__fView_uo.initViewFromControlLevel(this.info);
	}

	__validate()
	{
		this.__validateModelLevel();
		this.__validateControlLevel();
		if (this.__fView_uo)
		{
			this.__validateViewLevel();
		}
	}

	__validateModelLevel()
	{

	}

	__validateControlLevel()
	{

	}

	__validateViewLevel()
	{
		var info = this.info;
		var view = this.view;
		if (
				info instanceof SimpleUIInfo
				&& view instanceof SimpleUIView
			)
		{
			if (info.visible)
			{
				view.show();
			}
			else
			{
				view.hide();
			}
		}
	}

	__initViewLevelSelfInitialization()
	{
		throw new Error("'__initViewLevelSelfInitialization' must be overrided!");
	}

	__forceViewLevelSelfInitialization ()
	{
		this._initView(this.__getExternalViewForSelfInitialization());
	}

	__getExternalViewForSelfInitialization ()
	{
		throw new Error("'__getExternalViewForSelfInitialization' must be overrided!");
	}

	__getViewLevelSelfInitializationViewProvider ()
	{
		return this._fViewLevelSelfInitializationViewProvider_uo;
	}

	__isViewLevelSelfInitializationViewProviderAccessible ()
	{
		return this._fViewLevelSelfInitializationViewProvider_uo !== null;
	}

	/**
	 * Indicates whether view self initialization is allowed or not. If allowed - view will be created by _fViewLevelSelfInitializationViewProvider_uo.
	 * @returns {Boolean}
	 * @protected
	 */
	__isViewLevelSelfInitializationAllowed ()
	{
		return false;
	}

	_initViewLevelSelfInitializationViewProvider (aViewLevelSelfInitializationViewProvider_uo)
	{
		if (!this.__isViewLevelSelfInitializationAllowed())
		{
			throw new Error("Available for self view init mode only!");
		}
		if (!this.__fInitialized_bl)
		{
			throw new Error("View provider cannot be initialized before i_init() call!");
		}
		if (!(aViewLevelSelfInitializationViewProvider_uo instanceof SimpleUIView))
		{
			throw new Error("Invalid view provider: " + aViewLevelSelfInitializationViewProvider_uo);
		}
		if (this.__fView_uo)
		{
			throw new Error("View is already initialized!");
		}
		if (this._fViewLevelSelfInitializationViewProvider_uo)
		{
			throw new Error("View provider is already initialized!");
		}
		this._fViewLevelSelfInitializationViewProvider_uo = aViewLevelSelfInitializationViewProvider_uo;
		this.__onViewLevelSelfInitializationViewProviderAccessible();
	}

	__onViewLevelSelfInitializationViewProviderAccessible()
	{
	}
}

export default SimpleUIController