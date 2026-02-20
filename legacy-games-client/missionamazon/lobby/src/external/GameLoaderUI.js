import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { default as BaseUI } from '../../../../common/PIXI/src/dgphoenix/gunified/view/layout/GULoaderUI';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Timer from "../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer";
import I18 from '../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import SyncQueue from '../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/SyncQueue';
import LobbyPreloaderSoundButtonController from '../controller/uis/custom/preloader/LobbyPreloaderSoundButtonController';
import LobbyPreloaderSoundButtonView from '../view/uis/custom/preloader/LobbyPreloaderSoundButtonView';
import LobbyPreloaderLogoView from '../view/uis/custom/preloader/LobbyPreloaderLogoView';
import LoadingScreenPlayNowButton from '../../../game/src/ui/LoadingScreenPlayNowButton';

let PROGRESS_SPEED = 100;   // time for 1 percent move

class LoaderUI extends BaseUI
{
	static get EVENT_ON_CLICK_TO_START_CLICKED() 	{return "onClickToStartClicked";}

	get __backgroundName()
	{
		return 'preloader/back';
	}

	get __logoPosition()
	{
		return {x: 7, y: -184};
	}

	get __logoScale()
	{
		return 1.6
	}

	get __brandPosition()
	{
		return {x: -344, y: -191};
	}

	get __soundButtonPosition()
	{
		return APP.isMobile ? {x: -438, y: -216} : {x: -452, y: -220};
	}

	get __teaserImageNamePrefix()
	{
		return "preloader/teasers/teaser_"
	}

	get __teaserTANamePrefix()
	{
		return APP.isBattlegroundGame ? "TAPreloderTeaserBattleground" :  "TAPreloderTeaser" ;
	}

	get __teasersContainerPosition()
	{
		return {x: 7, y: 70}
	}

	get __loadingBarPosition()
	{
		return {x: 9, y: 185};
	}

	get __playNowButtonPosition()
	{
		return {x: 8, y: 233}
	}
	get __teaserIntervalAdd()
	{
		return -4;
	}

	get __teaserTextPosition()
	{
		return {x: 0, y: 24}
	}

	constructor(layout)
	{
		super(layout, new SyncQueue());

		this.__fPreloaderView_spr = null;
		this._fLastProgress_num = 0;
		this._fCompleteTimer_t = null;
		this._barMask = null;
		this._fIntervalTimer_t = null;

		this._fPlayNowButtonDisabled_btn = null;
		this._fPlayNowButtonEnabled_btn = null;
	}

	createLayout()
	{
		this.__addPreloaderContainer();
		this.__addBack();
		this.__addTeasers();
		this.__addLogo();
		this.__addSoundButton();
		this.__addLoadingBar();
		this.__addButtons();
		this.__addBrand();

		/*if(APP.isBattlegroundGame)
		{
			this._addBattlegroundLogo();
		}*/

		this.addListeners();
	}

	/*_addBattlegroundLogo()
	{
		let lLogo_spr = this.__fPreloaderView_spr.addChild(I18.generateNewCTranslatableAsset('TALobbyPreloaderLogoBattleground'));
		lLogo_spr.position.set(50, 300 + (APP.isMobile ? -10 : 0));
	}*/


	__addPreloaderContainer()
	{
		let lBackOffsetY_num = ((APP.config.margin.bottom || 0) - (APP.config.margin.top || 0)) / 2;

		this.__fPreloaderView_spr = APP.preloaderStage.view.addChild(new Sprite());
		this.__fPreloaderView_spr.position.y = -lBackOffsetY_num;
	}


	__addLogo()
	{
		let lLogo_spr = this.__fPreloaderView_spr.addChild(new LobbyPreloaderLogoView());
		lLogo_spr.position.set(this.__logoPosition.x, this.__logoPosition.y);

		lLogo_spr.scale.set(this.__logoScale);
	}

	__addBack()
	{
		let lBackOffsetY_num = ((APP.config.margin.bottom || 0) - (APP.config.margin.top || 0)) / 2;

		let lBack_spr = this.__fPreloaderView_spr.addChild(APP.library.getSprite(this.__backgroundName));
		lBack_spr.position.y = ~~lBackOffsetY_num;
	}

	__addBrand()
	{
		let lBrand_spr = this.__fPreloaderView_spr.addChild(I18.generateNewCTranslatableAsset('TAPreloaderBrand'));
		lBrand_spr.position.set(this.__brandPosition.x, this.__brandPosition.y);
		
	}

	__addSoundButton()
	{
		super._addSoundButton();
	}

	__addLoadingBar()
	{
		let lFillOffset = 2;
		let lLoadingBarContainer_spr = this.__fPreloaderView_spr.addChild(new Sprite());
		lLoadingBarContainer_spr.position.set(this.__loadingBarPosition.x, this.__loadingBarPosition.y);

		let lBarContainer_spr = lLoadingBarContainer_spr.addChild(new Sprite());
		
		let lBarBack = lBarContainer_spr.addChild(APP.library.getSprite("preloader/loading_bar/back"));
		lBarBack.position.set(-2, 1.5);

		let lBarFill = lBarContainer_spr.addChild(APP.library.getSprite("preloader/loading_bar/fill"));
		lBarFill.anchor.set(0, 0.5);
		lBarFill.position.x = -lBarFill.width/2 - lFillOffset;

		var lBarMask = this._barMask = lBarContainer_spr.addChild(new Sprite);
		var lBarMaskGr = lBarMask.addChild(new PIXI.Graphics());
		lBarMaskGr.beginFill(0x00ff00).drawRect(0, 0, 620, 12);

		lBarMask.anchor.set(0, 0.5);
		lBarMask.scale.x = 0.001;
		lBarMask.position.x = - lBarFill.width/2 - lFillOffset;
		lBarFill.mask = lBarMaskGr;

		lBarContainer_spr.scale.set(1.5, 0.5);
	}

	__addButtons()
	{
		let lIsVisible_bl = (	!APP.isBattlegroundGamePlayMode
								&& APP.browserSupportController.info.isAudioContextSuspended
								&& !APP.isMobile // no need to use "tap to start" for mobile devices, because currently we turn sounds on on;y by user interaction for mobile devices
								//(NOTE: we still need "tap to start" inside the game, because sound button won't unlock audion context in the game if game and lobby are in different domains)
							);


		//DISABLED VERSION...
		let lPlayNowButtonDisabled_btn = new LoadingScreenPlayNowButton(false);
		lPlayNowButtonDisabled_btn.position.set(this.__playNowButtonPosition.x, this.__playNowButtonPosition.y);
		lPlayNowButtonDisabled_btn.visible = lIsVisible_bl;

		this.__fPreloaderView_spr.addChild(lPlayNowButtonDisabled_btn);
		this._fPlayNowButtonDisabled_btn = lPlayNowButtonDisabled_btn;
		//...DISABLED VERSION

		//ENABLED VERSION...
		let lPlayNowButtonEnabled_btn = new LoadingScreenPlayNowButton(true);
		lPlayNowButtonEnabled_btn.position.set(this.__playNowButtonPosition.x, this.__playNowButtonPosition.y);
		lPlayNowButtonEnabled_btn.visible = false;

		this.__fPreloaderView_spr.addChild(lPlayNowButtonEnabled_btn);
		this._fPlayNowButtonEnabled_btn = lPlayNowButtonEnabled_btn;
		//...ENABLED VERSION
	}

	__addTeasers()
	{
		this._fTeasersContainer = this.__fPreloaderView_spr.addChild(new Sprite());
			
		const TEASERS_COUNT_NUM = 3;
		let lImageAnchorX_num = 0.5;
		let lCoefficientForPositionByIndex_num = TEASERS_COUNT_NUM / 2 - lImageAnchorX_num; //all teasers will always be at their points relatively the middle

		for( let i = 0; i < TEASERS_COUNT_NUM; i++ )
		{
			let lTeaser_spr = this._fTeasersContainer.addChild(new Sprite());
	
			//TEASER IMAGE...
			let lImage_spr = lTeaser_spr.addChild(this.__generateTeaserImage(i));
			lImage_spr.anchor.set(lImageAnchorX_num, 1);
			lImage_spr.scale.set(1.2, 1.2);

			//...TEASER IMAGE
	
			//TEASER TEXT...
			let lTranslatableAssetDescriptorName_str = this.__teaserTANamePrefix + i;
	
			let l_cta = lTeaser_spr.addChild(I18.generateNewCTranslatableAsset(lTranslatableAssetDescriptorName_str));
			l_cta.position.set(this.__teaserTextPosition.x, this.__teaserTextPosition.y);
			l_cta.scale.set(1.35,1.35);

			//...TEASER TEXT

			let lPositionX_num = 243 * (i - lCoefficientForPositionByIndex_num)
				+ this.__teaserIntervalAdd * (i - lCoefficientForPositionByIndex_num);
			lTeaser_spr.position.set(lPositionX_num, 0);
		}

		this._fTeasersContainer.position.set(this.__teasersContainerPosition.x, this.__teasersContainerPosition.y);
	}

	__generateTeaserImage(aIndex_num)
	{
		let lImageName_str = this.__teaserImageNamePrefix + aIndex_num;

		if(APP.isBattlegroundGame) lImageName_str +="_btg";
		
		let lImage_spr = APP.library.getSprite(lImageName_str);
		let lImageBounds_obj = lImage_spr.getBounds();

		let lFrame_spr = lImage_spr.addChild(APP.library.getSprite("preloader/tips_base_btg"));
		lFrame_spr.position.set(0, -lImageBounds_obj.height/2);

		return lImage_spr;
	}

	//PRELOADER SOUND BUTTON...
	__providePreloaderSoundButtonControllerInstance()
	{
		return new LobbyPreloaderSoundButtonController();
	}

	_initSoundButtonView()
	{
		let l_sbv = this.__fPreloaderView_spr.addChild(new LobbyPreloaderSoundButtonView());
		l_sbv.position.set(this.__soundButtonPosition.x, this.__soundButtonPosition.y);

		let platformInf = window.getPlatformInfo ? window.getPlatformInfo() : {};
		this._isMobile = platformInf.mobile;

		if (this._isMobile)
		{
			l_sbv.scale.set(1.8);
		}

		return l_sbv;
	}
	//...PRELOADER SOUND BUTTON

	fitLayout()
	{
		// nothing to do
	}

	showProgress(aPercent_num = 0)
	{
		if (this._fLastProgress_num === aPercent_num)
		{
			return;
		}

		this._fLastProgress_num = aPercent_num;
		let lEndScaleCount_num = Math.max(aPercent_num / 150, 0.001);

		this._barMask.removeTweens();
		this._barMask.addTween(
									'x',
									lEndScaleCount_num, PROGRESS_SPEED,
									null, this._onProgressTweenCompleted.bind(this),
									() => {},
									this._barMask.scale
								).play();
	}

	_onProgressTweenCompleted()
	{
		if (this._fLastProgress_num === 100)
		{
			this._barMask.removeTweens();

			this._fCompleteTimer_t = new Timer(this.onCompleteTimerFinished.bind(this), PROGRESS_SPEED * 3);
		}
	}

	showComplete()
	{
		this.showProgress(100);
	}

	onCompleteTimerFinished()
	{
		this._fCompleteTimer_t && this._fCompleteTimer_t.destructor();
		this._fCompleteTimer_t = null;

		this.dispatchComplete();
	}

	//CLICK TO START...
	showClickToStart()
	{
		this._fPlayNowButtonDisabled_btn.visible = false;
		this._fPlayNowButtonEnabled_btn.visible = true;

		this._fPlayNowButtonEnabled_btn.once("pointerclick", () => {
			this._fPlayNowButtonDisabled_btn.visible = true;
			this._fPlayNowButtonEnabled_btn.visible = false;
			this.emit(LoaderUI.EVENT_ON_CLICK_TO_START_CLICKED);
		});

	}
	//...CLICK TO START

	destructor()
	{
		super.destructor();

		this._fLastProgress_num = undefined;

		this._fCompleteTimer_t && this._fCompleteTimer_t.destructor();
		this._fCompleteTimer_t = null;

		this._fIntervalTimer_t && this._fIntervalTimer_t.destructor();
		this._fIntervalTimer_t = null;

		this.__fPreloaderView_spr = null;
		this._barMask = null;

		this._fPlayNowButtonDisabled_btn = null;
		this._fPlayNowButtonEnabled_btn = null;
	}
}

export default LoaderUI;