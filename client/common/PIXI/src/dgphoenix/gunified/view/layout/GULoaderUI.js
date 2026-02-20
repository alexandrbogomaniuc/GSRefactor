import LoaderUI from '../../../unified/view/layout/LoaderUI';

class GULoaderUI extends LoaderUI
{
	get preloaderSoundButtonController()
	{
		return this._preloaderSoundButtonController;
	}

	constructor(layout, loader)
	{
		super(layout, loader);

		this._fPreloaderSoundButtonController_psbc = null;
		this._fPreloaderSoundButtonView_psbv = null;
	}

	createLayout()
	{
		this._addSoundButton();
	}

	//PRELOADER SOUND BUTTON...
	_addSoundButton()
	{
		this._preloaderSoundButtonController.init();
		this._preloaderSoundButtonController.initView(this._preloaderSoundButtonView);
	}

	get _preloaderSoundButtonController()
	{
		return this._fPreloaderSoundButtonController_psbc || (this._fPreloaderSoundButtonController_psbc = this.__providePreloaderSoundButtonControllerInstance());
	}

	__providePreloaderSoundButtonControllerInstance()
	{
		return new GUPreloaderSoundButtonController();
	}

	get _preloaderSoundButtonView()
	{
		return this._fPreloaderSoundButtonView_psbv || (this._fPreloaderSoundButtonView_psbv = this._initSoundButtonView());
	}

	_initSoundButtonView()
	{
		// must be overridden
		return new GUPreloaderSoundButtonView();
	}
	//...PRELOADER SOUND BUTTON

	destructor()
	{
		super.destructor();

		this._fPreloaderSoundButtonController_psbc && this._fPreloaderSoundButtonController_psbc.destroy();
		this._fPreloaderSoundButtonController_psbc = null;
		this._fPreloaderSoundButtonView_psbv = null;
	}
}

export default GULoaderUI;