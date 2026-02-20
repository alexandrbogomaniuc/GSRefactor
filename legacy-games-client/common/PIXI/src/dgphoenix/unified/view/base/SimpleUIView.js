import Sprite from './display/Sprite';

/**
 * View class managed by SimpleUIController.
 * @class
 */
class SimpleUIView extends Sprite
{
	constructor()
	{
		super();

		this._uiInfo = null;
	}

	/**
	 * Model corresponding to view.
	 * @type {SimpleUIInfo}
	 */
	get uiInfo()
	{
		return this._uiInfo;
	}

	initViewFromControlLevel(aInfo)
	{
		this._uiInfo = aInfo;
		this.__init();
	}

	__init()
	{
	}

	/**
	 * Destroy view instance.
	 */
	destroy()
	{
		this._uiInfo = null;
		
		super.destroy();
	}
}

export default SimpleUIView