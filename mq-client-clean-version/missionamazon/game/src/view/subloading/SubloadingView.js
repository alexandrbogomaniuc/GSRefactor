import SimpleUIView from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PreloadingSpinner from '../../../../../common/PIXI/src/dgphoenix/gunified/view/custom/PreloadingSpinner';
import I18 from '../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class SubloadingView extends SimpleUIView {
	
	constructor() {
		super();

		this.waitScreen = null;
		this._fSubloadingContainer_sprt = null;
		this._fWaitScreen_sprt = null;
		this._fWaitScreenContent_sprt = null;
	}
	
	i_showLoadingScreen(aSubloadingContainerInfo_obj)
	{
		this._fSubloadingContainer_sprt = aSubloadingContainerInfo_obj.container;
		this._fSubloadingContainer_sprt.addChild(this);

		this.zIndex = aSubloadingContainerInfo_obj.zIndex;

		this._fWaitScreenContent_sprt.visible = !APP.tournamentModeController.info.isTournamentOnServerCompletedState;
	}

	i_hideLoadingScreen()
	{
		this.parent && this.parent.removeChild(this);
		this._fWaitScreenContent_sprt.visible = false;
	}

	get isLoadingScreenShown()
	{
		return !!this.parent;
	}

	__init()
	{
		let backOffsetY_num = ((APP.config.margin.bottom || 0) - (APP.config.margin.top || 0)) / 2;
		this._fWaitScreen_sprt = this.addChild(APP.library.getSprite('preloader/back'));
		this._fWaitScreen_sprt.position.y = backOffsetY_num;
		
		this._fWaitScreenContent_sprt = this._fWaitScreen_sprt.addChild(new Sprite());

		let lLoadingBase_sprt = this._fWaitScreenContent_sprt.addChild(APP.library.getSprite('preloader/loading_back'));
		lLoadingBase_sprt.position.set(0, 203);		
		lLoadingBase_sprt.scale.set(2.45, 0.32)

		let lCaption_cta =this._fWaitScreenContent_sprt.addChild(I18.generateNewCTranslatableAsset('TAWaitWhileMapIsLoadingCaption'));
		lCaption_cta.position.set(-180, 200);

		this._fWaitScreenContent_sprt.spinner = this._fWaitScreenContent_sprt.addChild(new PreloadingSpinner(2100, 110));
		this._fWaitScreenContent_sprt.spinner.position.y = 200;
		this._fWaitScreenContent_sprt.spinner.position.x = -400;
		this._fWaitScreenContent_sprt.spinner.startAnimation();

		this._fWaitScreenContent_sprt.visible = false;

	}

	destroy()
	{
		this._fWaitScreen_sprt = null;
		this._fWaitScreenContent_sprt = null;
		this._fSubloadingContainer_sprt = null;
		
		super.destroy();
	}
}

export default SubloadingView;