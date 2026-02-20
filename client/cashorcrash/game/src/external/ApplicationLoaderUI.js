import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import {default as BaseUI} from '../../../../common/PIXI/src/dgphoenix/gunified/view/layout/GULoaderUI';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Timer from "../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer";
import GamePreloaderSoundButtonController from '../controller/uis/custom/preloader/GamePreloaderSoundButtonController';
import GamePreloaderSoundButtonView from '../view/uis/custom/preloader/GamePreloaderSoundButtonView';
import SyncQueue from '../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/SyncQueue';
import GamePreloaderLogoView from '../view/uis/custom/preloader/GamePreloaderLogoView';
import LoadingScreenPlayNowButton from './LoadingScreenPlayNowButton';
import PreloaderTextures from './PreloaderTextures';
import { FRAME_RATE } from '../config/Constants';
import Sequence from '../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';

let PROGRESS_SPEED = 100;   // time for 1 percent move
const FULL_BAR_WIDTH = 462;

class LoaderUI extends BaseUI
{
	static get EVENT_ON_CLICK_TO_START_CLICKED() 	{return "onClickToStartClicked";}

	constructor(layout, loader)
	{
		super(layout, loader = new SyncQueue());

		this._fPreloaderView_spr = null;
		this._fLastProgress_num = 0;
		this._fCompleteTimer_t = null;
		this._barMask = null;
		this._fIntervalTimer_t = null;

		this._fPlayNowButtonDisabled_btn = null;
		this._fPlayNowButtonEnabled_btn = null;

		this._fPlayNowButtonDisabledPortrait_btn = null;
		this._fPlayNowButtonEnabledPortrait_btn = null;

		this._fPortraitButtons_spr = null;
		this._fAlbumButtons_spr = null;

		this._fLoadingBarHeader_spr = null;
		this._fLoadingBarContainer_spr = null;
		
		this._fLogo_sprt = null;
		this._fBackPlanet_spr = null;

		this._fBarMaskWidth_num = 0;

		this._fTips_spr_arr = [];
	}

	createLayout()
	{
		PreloaderTextures.initTextures();

		this._addPreloaderContainer();
		this._addBack();
		this._addLogo();
		this._addSoundButton();
		this._addLoadingBar();
		this._addButtons();
		this._addBrand();
		if (APP.isBattlegroundGame)
		{
			this._addTips();
		}

		this.addListeners();
	}

	_addPreloaderContainer()
	{
		let lBackOffsetY_num = ((APP.config.margin.bottom || 0) - (APP.config.margin.top || 0)) / 2;

		this._fPreloaderView_spr = APP.preloaderStage.view.addChild(new Sprite());
		this._fPreloaderView_spr.position.y = -lBackOffsetY_num;
	}

	_addLogo()
	{
		let lLogo_spr = this._fLogo_sprt = this._fPreloaderView_spr.addChild(new GamePreloaderLogoView);
		this._updateLogoPosition();
		lLogo_spr.visible = false;
		
	}

	_updateLogoPosition()
	{
		return;
		let lIsPortraitMode_bi = APP.layout.isPortraitOrientation;
		let lIsBattlegroundGame = APP.isBattlegroundGame;
		let lLogo_spr;

		if (I18.currentLocale === 'zh' || I18.currentLocale === 'zh-cn')
		{
			if (lIsPortraitMode_bi)
			{
				if (this._fLogo_sprt.children.length > 1)
				{
					lLogo_spr = this._fLogo_sprt.getChildAt(1);
					this._fLogo_sprt.getChildAt(0).visible = false;
					lLogo_spr.visible = true;
				}
				else
				{
					lLogo_spr = this._fLogo_sprt.getChildAt(0);
					this._fLogo_sprt.getChildAt(0).visible = true;
				}
			}
			else
			{
				lLogo_spr = this._fLogo_sprt.getChildAt(0);
				if (this._fLogo_sprt.children.length > 1)
				{
					this._fLogo_sprt.getChildAt(1).visible = false;
				}
				lLogo_spr.visible = true;
			}
		}
		else
		{
			lLogo_spr = this._fLogo_sprt;
		}
		
		if (lIsPortraitMode_bi)
		{
			if (lIsBattlegroundGame)
			{
				lLogo_spr.scaleTo(2.22);
				lLogo_spr.position.set(17, -120);
			}
			else
			{
				lLogo_spr.scaleTo(1.9);
				lLogo_spr.position.set(3, 12);
			}
		}
		else
		{
			lLogo_spr.scaleTo(2.22);
			
			lLogo_spr.position.set(27, 31);
			if (lIsBattlegroundGame)
			{
				lLogo_spr.position.set(27, -30);
			}
		}
	}

	_addTips()
	{
		this._fTipsContainer = APP.preloaderStage.view.addChild(new Sprite());
		this._fTipsContainer.visible = false;

		let l_s = this._fTipsContainer;
		l_s.position.set(-103, -30);

		let lTipImagesPositions_obj_arr =
		[
			{x: 5, y: 10},
			{x: 0, y: 0},
			{x: 0, y: 0},
		];
		
		for( let i = 0; i < lTipImagesPositions_obj_arr.length; i++ )
		{
			let lTipId_num = i + 1;
			let lTipContainer_s = l_s.addChild(new Sprite);

			//TIP IMAGE...
			let lImageName_str = "preloader/tips/tips_" + lTipId_num;

			let lTipImage_s = lTipContainer_s.addChild(APP.library.getSprite(lImageName_str));
			lTipImage_s.anchor.set(0.5, 0.5);
			lTipImage_s.scale.set(0.8);
			lTipImage_s.position.set(lTipImagesPositions_obj_arr[i].x, lTipImagesPositions_obj_arr[i].y);
			//...TIP IMAGE

			//TIP TEXT...
			let l_cta = null;
			let lTranslatableAssetDescriptorName_str = 'TAPreloaderTip' + lTipId_num;

			l_cta = lTipContainer_s.addChild(I18.generateNewCTranslatableAsset(lTranslatableAssetDescriptorName_str));
			l_cta.position.set(0, 87);
			//...TIP TEXT

			this._fTips_spr_arr.push(lTipContainer_s);
		}

		this._updateTipsPosition();
	}

	_updateTipsPosition()
	{
		let lIsPortraitMode_bi = APP.layout.isPortraitOrientation;

		let lTipsPositions_obj_arr =
		[
			{x: -130, y: 64},
			{x: 112, y: 64},
			{x: 350, y: 64},
		];

		if (lIsPortraitMode_bi)
		{
			lTipsPositions_obj_arr =
			[
				{x: -20, y: 0},
				{x: 230, y: 0},
				{x: 112, y: 230},
			];
		}

		for( let i = 0; i < this._fTips_spr_arr.length; i++ )
		{
			this._fTips_spr_arr[i].position.set(lTipsPositions_obj_arr[i].x, lTipsPositions_obj_arr[i].y);
		}

		this._fTips_spr_arr[2].scale.set(lIsPortraitMode_bi ? 1.25 : 1);
	}

	_addBack()
	{
		let lBackOffsetY_num = ((APP.config.margin.bottom || 0) - (APP.config.margin.top || 0)) / 2;

		this._fBackSpace_sprt = this._fPreloaderView_spr.addChild(new Sprite);
		this._fBackSunset_sprt = this._fPreloaderView_spr.addChild(new Sprite);
		this._fBackSunset_sprt.texture = PreloaderTextures['sunset_textures'][0];
		this._fBackSunset_sprt.scale.set(2);
		this._fBackSunset_sprt.anchor.set(0.5, 1);
		this._newBack_p_spr = this._fPreloaderView_spr.addChild(APP.library.getSprite("preloader_p_bg"));
		this._newBack_l_spr = this._fPreloaderView_spr.addChild(APP.library.getSprite("preloader_l_bg"));
		this._rebuildBack();

		let lBackPlanet_spr = this._fBackPlanet_spr = this._fPreloaderView_spr.addChild(new Sprite);
		lBackPlanet_spr.texture = PreloaderTextures['planet_textures'][0];
		this._updatePlanetPosition();
		
		lBackPlanet_spr.position.y = ~~lBackOffsetY_num - 175;
		lBackPlanet_spr.position.x = 84;
		lBackPlanet_spr.visible = false;	
	}

	_updatePlanetPosition()
	{
		return;
		let lIsPortraitMode_bi = APP.layout.isPortraitOrientation;
		let lBackPlanet_spr = this._fBackPlanet_spr;
		
		if (lIsPortraitMode_bi)
		{
			lBackPlanet_spr.scale.set(0.9, 0.9);
		}
		else
		{
			lBackPlanet_spr.scale.set(1, 1);
		}
	}

	_rebuildBack()
	{
		let lScreenWidth_num = APP.screenWidth;
		let lScreenHeight_num = APP.screenHeight;

		let lBack_spr = this._fBackSpace_sprt;
		lBack_spr.position.set(-lScreenWidth_num/2, -lScreenHeight_num/2);

		let lSpaceAsset_a = PreloaderTextures['space_textures'][0];
		let lRowsAmnt_num = Math.ceil(lScreenHeight_num/lSpaceAsset_a.height);
		let lColsAmnt_num = Math.ceil(lScreenWidth_num/lSpaceAsset_a.width);
		let lNewSpacePartsAmount_num = lRowsAmnt_num*lColsAmnt_num;
		let lCurSpacePartsAmount_num = lBack_spr.children ? lBack_spr.children.length : 0;
		
		let lSpacePartView_sprt;
		for (let i=0; i<lNewSpacePartsAmount_num; i++)
		{
			if (i < lCurSpacePartsAmount_num)
			{
				lSpacePartView_sprt = lBack_spr.getChildAt(i);
			}
			else
			{
				lSpacePartView_sprt = lBack_spr.addChild(new Sprite);
				lSpacePartView_sprt.texture = lSpaceAsset_a;
				lSpacePartView_sprt.anchor.set(0, 0);
			}

			lSpacePartView_sprt.position.x = (i%lColsAmnt_num) * lSpaceAsset_a.width;
			lSpacePartView_sprt.position.y = (~~(i/lColsAmnt_num)%lRowsAmnt_num) * lSpaceAsset_a.height;
			lSpacePartView_sprt.visible = true;
		}

		if (lNewSpacePartsAmount_num < lCurSpacePartsAmount_num)
		{
			for (let i=lCurSpacePartsAmount_num; i<lNewSpacePartsAmount_num; i++)
			{
				lSpacePartView_sprt.visible = false;
			}
		}

		this._fBackSunset_sprt.position.y = lScreenHeight_num/2+650;

		if(this._newBack_p_spr && this._newBack_p_spr)
		{
			if(lScreenHeight_num>lScreenWidth_num){
				this._newBack_p_spr.visible = true;
				this._newBack_l_spr.visible = false;
			}else{
				this._newBack_l_spr.visible = true;
				this._newBack_p_spr.visible = false;
			}
		}
	}

	_addBrand()
	{
		let lCustomerBrand_obj = APP.customerspecController.info.brand;
		let lSettingsBrand_obj = APP.config.brand;
		let lBrandEnable_bln = lSettingsBrand_obj.enable;

		if (lCustomerBrand_obj && lCustomerBrand_obj.priority >= lSettingsBrand_obj.priority)
		{
			lBrandEnable_bln = lCustomerBrand_obj.enable;
		}

		if (lBrandEnable_bln)
		{
			let lBrand_spr = this._fBrand_sprt = this._fPreloaderView_spr.addChild(I18.generateNewCTranslatableAsset('TAPreloaderBrand'));
			

			this._updateBrandPosition();
			this._fBrand_sprt.visible = false;
		}
	}

	_updateBrandPosition()
	{
		return;
		let lScreenWidth_num = APP.screenWidth;
		let lScreenHeight_num = APP.screenHeight;

		let lBrand_spr = this._fBrand_sprt;

		lBrand_spr.position.set(-lScreenWidth_num/2+116, -lScreenHeight_num/2+79);
	}

	_addSoundButton()
	{
		super._addSoundButton();

		this._updateSoundButtonPosition();
	}

	_addLoadingBar()
	{
		let lIsBattlegroundGame = APP.isBattlegroundGame;
		let lHeaderCTAName_spr = lIsBattlegroundGame ? 'TALoadingHeaderBattleground' : 'TALoadingHeader';
		let lHeader_spr = this._fPreloaderView_spr.addChild(I18.generateNewCTranslatableAsset(lHeaderCTAName_spr));

		let lFillOffset = 2;
		let lLoadingBarContainer_spr = this._fPreloaderView_spr.addChild(new Sprite);

		let lBarContainer_spr = lLoadingBarContainer_spr.addChild(new Sprite);
		
		let lBarBack = lBarContainer_spr.addChild(new Sprite);
		lBarBack.texture = PreloaderTextures['bar_textures'][0];
		lBarBack.position.set(-2, -61);

		let lBarFill = lBarContainer_spr.addChild(new Sprite);
		lBarFill.texture = PreloaderTextures['bar_textures'][1];
		lBarFill.anchor.set(0, 0.5);
		lBarFill.position.x = -lBarFill.width/2 - lFillOffset;
		lBarFill.position.y = - 61;

		var lBarMask = this._barMask = lBarContainer_spr.addChild(new Sprite);
		var lBarMaskGr = this._fBarMaskView_gr = lBarMask.addChild(new PIXI.Graphics());
		lBarMask.position.x = -lBarFill.width/2 - lFillOffset;

		this.barMaskWidth = 0.001*FULL_BAR_WIDTH;
		lBarFill.mask = lBarMaskGr;

		this._fLoadingBarHeader_spr = lHeader_spr;
		this._fLoadingBarContainer_spr = lLoadingBarContainer_spr;

		this._updateLoadingBarPosition();
	}

	_updateLoadingBarPosition()
	{
		let lIsPortraitMode_bi = APP.layout.isPortraitOrientation;
		let lIsBattlegroundGame = APP.isBattlegroundGame;

		if (lIsPortraitMode_bi)
		{
			this._fLoadingBarHeader_spr.position.set(-4, 30);
			this._fLoadingBarContainer_spr.position.set(0, 130);
			if (lIsBattlegroundGame)
			{
				this._fLoadingBarHeader_spr.position.set(65, 440);
				this._fLoadingBarContainer_spr.position.set(0, 460);
				this._fLoadingBarContainer_spr.scale.set(1,1);
			}
		}
		else
		{
			this._fLoadingBarHeader_spr.position.set(-4, 48);
			this._fLoadingBarContainer_spr.position.set(0, 148);
			if (lIsBattlegroundGame)
			{
				this._fLoadingBarHeader_spr.position.set(290, 240);
				this._fLoadingBarContainer_spr.position.set(245, 260);
				this._fLoadingBarContainer_spr.scale.set(0.95,0.95);
			}
		}
	}

	_addButtons()
	{
		let lX_num = 250;
		let lY_num = 220;
		let lx_num_portrait = 5;
		let lYPortrait_num = APP.isBattlegroundGame ? 400 : 220;
		let lIsVisible_bl = false; // sounds are off by default, so Play Now button is not required

		this._fPortraitButtons_spr = this._fPreloaderView_spr.addChild(new Sprite);
		this._fAlbumButtons_spr = this._fPreloaderView_spr.addChild(new Sprite);

		//DISABLED VERSION...
		let lPlayNowButtonDisabled_btn = new LoadingScreenPlayNowButton(false);
		lPlayNowButtonDisabled_btn.position.set(lX_num, lY_num);
		lPlayNowButtonDisabled_btn.visible = lIsVisible_bl;

		this._fAlbumButtons_spr.addChild(lPlayNowButtonDisabled_btn);
		this._fPlayNowButtonDisabled_btn = lPlayNowButtonDisabled_btn;

		let lPlayNowButtonDisabledPortrait_btn = new LoadingScreenPlayNowButton(false, true);
		lPlayNowButtonDisabledPortrait_btn.position.set(lx_num_portrait, lYPortrait_num);
		lPlayNowButtonDisabledPortrait_btn.visible = lIsVisible_bl;

		this._fPortraitButtons_spr.addChild(lPlayNowButtonDisabledPortrait_btn);
		this._fPlayNowButtonDisabledPortrait_btn = lPlayNowButtonDisabledPortrait_btn;
		//...DISABLED VERSION

		//ENABLED VERSION...
		let lPlayNowButtonEnabled_btn = new LoadingScreenPlayNowButton(true);
		lPlayNowButtonEnabled_btn.position.set(lX_num, lY_num);
		lPlayNowButtonEnabled_btn.visible = false;

		this._fAlbumButtons_spr.addChild(lPlayNowButtonEnabled_btn);
		this._fPlayNowButtonEnabled_btn = lPlayNowButtonEnabled_btn;

		let lPlayNowButtonEnabledPortrait_btn = new LoadingScreenPlayNowButton(true, true);
		lPlayNowButtonEnabledPortrait_btn.position.set(lx_num_portrait, lYPortrait_num);
		lPlayNowButtonEnabledPortrait_btn.visible = false;

		this._fPortraitButtons_spr.addChild(lPlayNowButtonEnabledPortrait_btn);
		this._fPlayNowButtonEnabledPortrait_btn = lPlayNowButtonEnabledPortrait_btn;
		//...ENABLED VERSION
	}

	_updateButtonsPosition()
	{
		console.log(" update button positions ");
		if (APP.layout.isPortraitOrientation)
		{
			this._fPortraitButtons_spr.visible = true;
			this._fAlbumButtons_spr.visible = false;
		}
		else
		{
			this._fPortraitButtons_spr.visible = false;
			this._fAlbumButtons_spr.visible = true;
		}
	}

	//PRELOADER SOUND BUTTON...
	__providePreloaderSoundButtonControllerInstance()
	{
		return new GamePreloaderSoundButtonController();
	}

	_initSoundButtonView()
	{
		let l_sbv = this._fPreloaderView_spr.addChild(new GamePreloaderSoundButtonView());
		return l_sbv;
	}

	_updateSoundButtonPosition()
	{
		let lScreenWidth_num = APP.screenWidth;
		let lScreenHeight_num = APP.screenHeight;

		let l_sbv = this._preloaderSoundButtonView;
		// l_sbv.position.set(-448, -207);
		l_sbv.position.set(-lScreenWidth_num/2+33, -lScreenHeight_num/2+66);

		if (APP.isMobile)
		{
			l_sbv.scale.set(1.8);
			l_sbv.position.y += 14;
			l_sbv.position.x += 4;
		}
	}
	//...PRELOADER SOUND BUTTON

	fitLayout()
	{
	}

	onOrientationChanged(e)
	{
		this._rebuildBack();
		this._updateSoundButtonPosition();
		this._updateBrandPosition();
		this._updateLogoPosition();
		this._updatePlanetPosition();
		if (APP.isBattlegroundGame)
		{
			this._updateTipsPosition();
		}
		this._updateButtonsPosition();
		this._updateLoadingBarPosition();
	}

	showProgress(aPercent_num = 0)
	{
		if (this._fLastProgress_num === aPercent_num)
		{
			return;
		}

		this._fLastProgress_num = aPercent_num;
		let lEndScaleCount_num = Math.max(aPercent_num / 100, 0.001);
		let lEndBarWidth_num = lEndScaleCount_num*FULL_BAR_WIDTH;

		this._barMask.removeTweens();
		this._barMask.addTween(
									'barMaskWidth',
									lEndBarWidth_num, PROGRESS_SPEED,
									null, this._onProgressTweenCompleted.bind(this),
									undefined,
									this
								).play();
	}

	set barMaskWidth(value)
	{
		this._fBarMaskWidth_num = value;

		var lBarMaskGr = this._fBarMaskView_gr;
		lBarMaskGr.clear();
		lBarMaskGr.beginFill(0x00ff00).drawRoundedRect(0, -70.5, value, 20, 10).endFill();
	}

	get barMaskWidth()
	{
		return this._fBarMaskWidth_num;
	}

	_onProgressTweenCompleted()
	{
		if (this._fLastProgress_num === 100)
		{
			this._barMask.removeTweens();

			this._fCompleteTimer_t = new Timer(this.onCompleteTimerFinished.bind(this), PROGRESS_SPEED * 3);
		
			//DEBUG...
			// this.showClickToStart();
			//...DEBUG
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

		this._fPlayNowButtonDisabledPortrait_btn.visible = false;
		this._fPlayNowButtonEnabledPortrait_btn.visible = true;

		if (APP.isBattlegroundGame)
		{
			this._fLoadingBarContainer_spr.visible = false;
			this._fLoadingBarHeader_spr.visible = false;
		}

		this._fPlayNowButtonEnabled_btn.once("pointerdown", this._onClickToStart, this);
		this._fPlayNowButtonEnabledPortrait_btn.once("pointerdown", this._onClickToStart, this);
		this._updateButtonsPosition();
		this.startBlink();
		this.startBlinkPortrait();
	}

	startBlink()
	{
		if (this._fBlink_seq)
		{
			return;
		}

		this._fPlayNowButtonEnabled_btn.scale.x = 1.2;
		this._fBlink_seq = Sequence.start(this._fPlayNowButtonEnabled_btn, this._blinkSeq);
	}

	startBlinkPortrait()
	{
		if (this._fBlink_seq_portrait)
		{
			return;
		}

		this._fPlayNowButtonEnabledPortrait_btn.scale.x = 1.2;
		this._fBlink_seq_portrait = Sequence.start(this._fPlayNowButtonEnabledPortrait_btn, this._blinkSeqPortrait);
	}


	get _blinkSeqPortrait()
	{
		let lSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 1},{prop: 'scale.y', to: 1}],	duration: 30*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1.2},{prop: 'scale.y', to: 1.2}],	duration: 30*FRAME_RATE, onfinish:  () => this.onScaleComplatedPortrait()}
		];

		return lSeq_arr
	}

	get _blinkSeq()
	{
		let lSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 1},{prop: 'scale.y', to: 1}],	duration: 30*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1.2},{prop: 'scale.y', to: 1.2}],	duration: 30*FRAME_RATE, onfinish:  () => this.onScaleComplated()}
		];

		return lSeq_arr
	}
	
	onScaleComplated()
	{
		this._fBlink_seq && this._fBlink_seq.destructor();
		this._fBlink_seq = null;

		this.startBlink();
	}

	onScaleComplatedPortrait()
	{
		this._fBlink_seq_portrait && this._fBlink_seq_portrait.destructor();
		this._fBlink_seq_portrait = null;

		this.startBlinkPortrait();
	}
		

	

	_onClickToStart()
	{
		this._fPlayNowButtonDisabled_btn.visible = true;
		this._fPlayNowButtonEnabled_btn.visible = false;

		this._fPlayNowButtonDisabledPortrait_btn.visible = false;
		this._fPlayNowButtonEnabledPortrait_btn.visible = false;

		this._fPlayNowButtonEnabled_btn.off("pointerclick");
		this._fPlayNowButtonEnabledPortrait_btn.off("poointerclick");

		this._fBlink_seq && this._fBlink_seq.destructor();
		this._fBlink_seq_portrait && this._fBlink_seq_portrait.destructor();

		this.emit(LoaderUI.EVENT_ON_CLICK_TO_START_CLICKED);
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

		this._fPreloaderView_spr = null;
		this._barMask = null;
	
		this._fPlayNowButtonDisabled_btn = null;
		this._fPlayNowButtonEnabled_btn = null;

		this._fPlayNowButtonDisabledPortrait_btn = null;
		this._fPlayNowButtonEnabledPortrait_btn = null;

		this._fPortraitButtons_spr = null;
		this._fAlbumButtons_spr = null;

		this._fBackSpace_sprt = null;
		this._fBackSunset_sprt = null;
		this._fBrand_sprt = null;

		if (this._fTips_spr_arr)
		{
			for (let i=0; i<this._fTips_spr_arr.length; i++)
			{
				this._fTips_spr_arr[i].destroy();
			}
		}
		this._fTips_spr_arr = null;
		this._fTipsContainer = null;
	}
}

export default LoaderUI;