import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import BalanceBlock from './BalanceBlock';
import TimeBlock from './TimeBlock';
import WinsBlock from './WinsBlock';
import SelectableBottomPanelButton from './SelectableBottomPanelButton';
import GameSoundButtonView from '../secondary/GameSoundButtonView';
import SeptumView from './SeptumView';
import { GAME_VIEW_SETTINGS } from '../../../main/GameBaseView';

class BottomPanelView extends SimpleUIView
{
	static get EVENT_ROUND_HISTORY_ITEM_CLICKED()	{ return WinsBlock.EVENT_ROUND_HISTORY_ITEM_CLICKED; }
	static get EVENT_HOME_BUTTON_CLICKED()			{ return "onHomeButtonClicked"; }
	static get EVENT_HISTORY_BUTTON_CLICKED()		{ return "onHistoryButtonClicked"; }
	static get EVENT_INFO_BUTTON_CLICKED()			{ return "onInfoButtonClicked"; }
	static get EVENT_SETTINGS_BUTTON_CLICKED()		{ return "onSettingsButtonClicked"; }
	static get EVENT_BACK_BUTTON_CLICKED()			{ return "onBackButtonClicked"; }

	//INTERFACE...
	updateBalance(aValue_num)
	{
		this._getBalanceIndicator().updateBalance(aValue_num);
	}

	enableWinBlock()
	{
		this._fWinsIndicator_wsb && this._fWinsIndicator_wsb.enableButtons();
	}

	disableWinBlock()
	{
		this._fWinsIndicator_wsb && this._fWinsIndicator_wsb.disableButtons();
	}

	disableInterfaceButtons()
	{
		this._fInfoButton_sb.setDisabled();
		this._fHistoryButton_sb && this._fHistoryButton_sb.setDisabled();
		this._fSettingsButton_sb.setDisabled();
	}

	enableInterfaceButtons()
	{
		this._fInfoButton_sb.setEnabled();
		this._fHistoryButton_sb && this._fHistoryButton_sb.setEnabled();
		this._fSettingsButton_sb.setEnabled();
	}

	updateWinsLog(aValues_num_arr)
	{
		this._fWinsIndicator_wsb && this._fWinsIndicator_wsb.updateWinsLog(aValues_num_arr);
	}

	updateWinsLogAim(aWinItemIndex_int)
	{
		this._fWinsIndicator_wsb.updateCurrentAim(aWinItemIndex_int);
	}

	syncTime(aValue_num)
	{
		if (this._fTimeIndicator_tb)
		{
			this._fTimeIndicator_tb.time = aValue_num;
		}
	}

	get appliedTime()
	{
		if (!this._fTimeIndicator_tb)
		{
			return undefined;
		}

		return this._fTimeIndicator_tb.time;
	}

	get appliedTimeDefined()
	{
		return this.appliedTime !== undefined;
	}

	get soundButtonView()
	{
		return this._getSoundButtonView();
	}

	updateArea()
	{
		this._updateArea();
	}
	//...INTERFACE

	constructor()
	{
		super();

		this._fLeftContainer_sprt = null;
		this._fRightContainer_sprt = null;

		this._fHomeButton_sb = null;
		this._fHistoryButton_sb = null;
		this._fInfoButton_sb = null;
		this._fSettingsButton_sb = null;
		this._fSoundButtonView_gsbv = null;
		this._fBackButton_sb = null;

		this._fTimeIndicator_tb = null;
		this._fBalanceIndicator_bb = null;
		this._fWinsIndicator_wsb = null;
	}

	__init()
	{
		super.__init();

		APP.gameScreenView.bottomPanelContainer.addChild(this);

		//BACKGROUND...
		this._fBackground_gr = this.addChild(new PIXI.Graphics);
		this._updateBackground();
		//...BACKGROUND

		this._fLeftContainer_sprt = this.addChild(new Sprite);
		this._fRightContainer_sprt = this.addChild(new Sprite);
		this._fSeptumsContainer_sprt = this.addChild(new Sprite);

		if (this.uiInfo.isHomeButtonRequired)
		{
			this._initHomeButton();
		}
		if (this.uiInfo.isHistoryButtonRequired)
		{
			this._initHistoryButton();
		}
		if (this.uiInfo.isTimeIndicatorRequired)
		{
			this._initTimeIndicator();
		}
		this._initInfoButton();
		this._initSettingsButton();
		this._initSoundButtonView();
		this._initWinsIndicator();
		this._updateArea();
	}

	_updateArea()
	{
		let lScreenWidth_num = APP.config.size.width;
		let lScreenHeight_num = APP.config.size.height;
		let lCurBapHeight_num = GAME_VIEW_SETTINGS.BAP_HEIGHT[APP.layout.orientation];
		
		this.position.set(-lScreenWidth_num/2, lScreenHeight_num/2-lCurBapHeight_num);

		this._updateBackground();

		this._fLeftContainer_sprt.position.set(0, lCurBapHeight_num - GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT/2);
		this._fRightContainer_sprt.position.set(APP.screenWidth, lCurBapHeight_num - GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT/2);
		this._fSeptumsContainer_sprt.position.set(0, lCurBapHeight_num - GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT/2);

		this._adjustElements();
	}

	_updateBackground()
	{
		let lIsPortraitMode_bi = APP.layout.isPortraitOrientation;

		let lCurBapHeight_num = GAME_VIEW_SETTINGS.BAP_HEIGHT[APP.layout.orientation];
		let lBackY_num = lIsPortraitMode_bi ? GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT : 0;

		this._fBackground_gr.cacheAsBitmap = false;
		this._fBackground_gr.clear();
		this._fBackground_gr.beginFill(0x000000).drawRect(0, lBackY_num, APP.screenWidth, GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT).endFill();
	}

	_adjustElements()
	{
		this._hideAllSeptums();

		let lElementsDistance_num = 2;

		let lLeftFloatOffsetX_num = 0;
		let lCurrentArea_r = null;
		let lSeptum_sv = null;
		let lCurBapHeight_num = GAME_VIEW_SETTINGS.BAP_HEIGHT[APP.layout.orientation];

		if (this._fHomeButton_sb)
		{
			lCurrentArea_r = this._fHomeButton_sb.getHitArea();
			this._fHomeButton_sb.position.x = lLeftFloatOffsetX_num + lCurrentArea_r.width/2;
			lLeftFloatOffsetX_num = this._fHomeButton_sb.position.x + lCurrentArea_r.width/2;
		}

		if (this._fHistoryButton_sb)
		{
			if (lLeftFloatOffsetX_num > 0)
			{
				lSeptum_sv = this._activateNextAvailableSeptum();
				lSeptum_sv.position.x = lLeftFloatOffsetX_num + lElementsDistance_num/2;
			}

			lCurrentArea_r = this._fHistoryButton_sb.getHitArea();
			this._fHistoryButton_sb.position.x = lLeftFloatOffsetX_num + lCurrentArea_r.width/2 + lElementsDistance_num;
			lLeftFloatOffsetX_num = this._fHistoryButton_sb.position.x + lCurrentArea_r.width/2;
		}

		if (this._fTimeIndicator_tb)
		{
			if (lLeftFloatOffsetX_num > 0)
			{
				lSeptum_sv = this._activateNextAvailableSeptum();
				lSeptum_sv.position.x = lLeftFloatOffsetX_num + lElementsDistance_num/2;
			}

			this._fTimeIndicator_tb.position.x = lLeftFloatOffsetX_num + TimeBlock.BLOCK_WIDTH/2 + lElementsDistance_num;
			lLeftFloatOffsetX_num = this._fTimeIndicator_tb.position.x + TimeBlock.BLOCK_WIDTH/2;
		}

		if (lLeftFloatOffsetX_num > 0)
		{
			lSeptum_sv = this._activateNextAvailableSeptum();
			lSeptum_sv.position.x = lLeftFloatOffsetX_num + lElementsDistance_num/2;
		}

		let lBalanceIndicator_bb = this._getBalanceIndicator();
		lBalanceIndicator_bb.position.x = lLeftFloatOffsetX_num + BalanceBlock.BLOCK_WIDTH/2 + lElementsDistance_num;
		lLeftFloatOffsetX_num = lBalanceIndicator_bb.position.x + BalanceBlock.BLOCK_WIDTH/2;

		if (lCurBapHeight_num == GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT)
		{
			lSeptum_sv = this._activateNextAvailableSeptum();
			lSeptum_sv.position.x = lLeftFloatOffsetX_num + lElementsDistance_num/2;
		}

		let lRightFloatOffsetX_num = 0;
		if (this._fBackButton_sb)
		{
			lCurrentArea_r = this._fBackButton_sb.getHitArea();
			this._fBackButton_sb.position.x = lRightFloatOffsetX_num - lCurrentArea_r.width/2;
			lRightFloatOffsetX_num = this._fBackButton_sb.position.x - lCurrentArea_r.width/2;
		}

		if (this._fSoundButtonView_gsbv)
		{
			if (lRightFloatOffsetX_num < 0)
			{
				lSeptum_sv = this._activateNextAvailableSeptum();
				lSeptum_sv.position.x = APP.screenWidth + lRightFloatOffsetX_num - lElementsDistance_num/2;
			}

			lCurrentArea_r = this._fSoundButtonView_gsbv.getButtonStateView(0).getHitArea();
			this._fSoundButtonView_gsbv.position.x = lRightFloatOffsetX_num - lCurrentArea_r.width/2;
			lRightFloatOffsetX_num = this._fSoundButtonView_gsbv.position.x - lCurrentArea_r.width/2;
		}

		if (this._fSettingsButton_sb)
		{
			if (lRightFloatOffsetX_num < 0)
			{
				lSeptum_sv = this._activateNextAvailableSeptum();
				lSeptum_sv.position.x = APP.screenWidth + lRightFloatOffsetX_num - lElementsDistance_num/2;
			}

			lCurrentArea_r = this._fSettingsButton_sb.getHitArea();
			this._fSettingsButton_sb.position.x = lRightFloatOffsetX_num - lCurrentArea_r.width/2 - lElementsDistance_num;
			lRightFloatOffsetX_num = this._fSettingsButton_sb.position.x - lCurrentArea_r.width/2;
		}

		if (this._fInfoButton_sb)
		{
			if (lRightFloatOffsetX_num < 0)
			{
				lSeptum_sv = this._activateNextAvailableSeptum();
				lSeptum_sv.position.x = APP.screenWidth + lRightFloatOffsetX_num - lElementsDistance_num/2;
			}

			lCurrentArea_r = this._fInfoButton_sb.getHitArea();
			this._fInfoButton_sb.position.x = lRightFloatOffsetX_num - lCurrentArea_r.width/2 - lElementsDistance_num;
			lRightFloatOffsetX_num = this._fInfoButton_sb.position.x - lCurrentArea_r.width/2;
		}

		if (this._fWinsIndicator_wsb)
		{
			let lWinsMaxWidth_num = APP.screenWidth - lLeftFloatOffsetX_num + lRightFloatOffsetX_num - lElementsDistance_num*2;
			let lWinsX_num = lLeftFloatOffsetX_num + lElementsDistance_num;
			let lWinsY_num = 0;
			
			if (lCurBapHeight_num > GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT)
			{
				lWinsMaxWidth_num = APP.screenWidth;
				lWinsX_num = 0;
				lWinsY_num = -(APP.screenHeight-GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y - GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height)+GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT;
			}
			else
			{
				lSeptum_sv = this._activateNextAvailableSeptum();
				lSeptum_sv.position.x = APP.screenWidth + lRightFloatOffsetX_num - lElementsDistance_num/2;
			}

			this._fWinsIndicator_wsb.setMaxWidth(lWinsMaxWidth_num);
			this._fWinsIndicator_wsb.position.set(lWinsX_num, lWinsY_num);
		}
	}

	_getBalanceIndicator()
	{
		return this._fBalanceIndicator_bb || this._initBalanceIndicator();
	}

	_initBalanceIndicator()
	{
		let lBalanceIndicator_bb = this._fLeftContainer_sprt.addChild(new BalanceBlock());
		this._fBalanceIndicator_bb = lBalanceIndicator_bb;
		return lBalanceIndicator_bb;
	}

	_initTimeIndicator()
	{
		let lTimeIndicator_tb = this._fLeftContainer_sprt.addChild(new TimeBlock(this.uiInfo.timeFromServer, this.uiInfo.timerFrequency, this.uiInfo.timerOffset));
		this._fTimeIndicator_tb = lTimeIndicator_tb;
	}

	_initWinsIndicator()
	{
		let lWinsIndicator_wsb = this._fLeftContainer_sprt.addChild(new WinsBlock());
		lWinsIndicator_wsb.on(WinsBlock.EVENT_ROUND_HISTORY_ITEM_CLICKED, this.emit, this);
		this._fWinsIndicator_wsb = lWinsIndicator_wsb;
	}

	_initHomeButton()
	{
		let lButton_sb = this._fLeftContainer_sprt.addChild(new SelectableBottomPanelButton("common_btn_home", null, APP.isMobile ? 1.4 : 1));
		lButton_sb.on("pointerclick", this._onHomeButtonClicked, this);
		this._fHomeButton_sb = lButton_sb;
	}
	
	_onHomeButtonClicked(event)
	{
		this.emit(BottomPanelView.EVENT_HOME_BUTTON_CLICKED);
	}

	_initHistoryButton()
	{
		let lButton_sb = this._fLeftContainer_sprt.addChild(new SelectableBottomPanelButton("common_btn_history", null, APP.isMobile ? 1.4 : 1));
		lButton_sb.on("pointerclick", this._onHistoryButtonClicked, this);
		this._fHistoryButton_sb = lButton_sb;
	}
	
	_onHistoryButtonClicked(event)
	{
		this.emit(BottomPanelView.EVENT_HISTORY_BUTTON_CLICKED);
	}

	_initInfoButton()
	{
		let lButton_sb = this._fRightContainer_sprt.addChild(new SelectableBottomPanelButton("common_btn_info", null, APP.isMobile ? 1.6 : 1));
		lButton_sb.on("pointerclick", this._onInfoButtonClicked, this);
		this._fInfoButton_sb = lButton_sb;
	}

	_onInfoButtonClicked(event)
	{
		// this._restartHideGroupTimer();
		this.emit(BottomPanelView.EVENT_INFO_BUTTON_CLICKED);
	}

	_initSettingsButton()
	{
		let lButton_sb = this._fRightContainer_sprt.addChild(new SelectableBottomPanelButton("common_btn_settings", null, APP.isMobile ? 1.6 : 1));
		lButton_sb.on("pointerclick", this._onSettingsButtonClicked, this);
		this._fSettingsButton_sb = lButton_sb;
	}

	_onSettingsButtonClicked(event)
	{
		// this._restartHideGroupTimer();
		this.emit(BottomPanelView.EVENT_SETTINGS_BUTTON_CLICKED);
	}

	_initBackButton()
	{
		let lButton_sb = this._fRightContainer_sprt.addChild(new SelectableBottomPanelButton("common_btn_back", null, APP.isMobile ? 1.6 : 1));
		lButton_sb.on("pointerclick", this._onBackButtonClicked, this);
		this._fBackButton_sb = lButton_sb;
	}

	_onBackButtonClicked(event)
	{
		this.emit(BottomPanelView.EVENT_BACK_BUTTON_CLICKED);
	}

	_getSoundButtonView()
	{
		return this._fSoundButtonView_gsbv || this._initSoundButtonView();
	}

	_initSoundButtonView()
	{
		let lSoundButtonView_gsbv = new GameSoundButtonView();
		this._fRightContainer_sprt.addChild(lSoundButtonView_gsbv);
		this._fSoundButtonView_gsbv = lSoundButtonView_gsbv;
		return lSoundButtonView_gsbv;
	}

	//SEPTUMS...
	_hideAllSeptums()
	{
		if (!this._fSeptumsContainer_sprt.children || !this._fSeptumsContainer_sprt.children.length)
		{
			return;
		}

		for (let i=0; i<this._fSeptumsContainer_sprt.children.length; i++)
		{
			let lSeptum_sv = this._fSeptumsContainer_sprt.getChildAt(i);
			lSeptum_sv.visible = false;
		}
	}

	_activateNextAvailableSeptum()
	{
		if (!this._fSeptumsContainer_sprt.children || !this._fSeptumsContainer_sprt.children.length)
		{
			return this._generateSeptum();
		}

		for (let i=0; i<this._fSeptumsContainer_sprt.children.length; i++)
		{
			let lSeptum_sv = this._fSeptumsContainer_sprt.getChildAt(i);
			if (!lSeptum_sv.visible)
			{
				lSeptum_sv.visible = true;
				return lSeptum_sv;
			}
		}

		return this._generateSeptum();
	}

	_generateSeptum()
	{
		let lSeptumWidth_num = 1;
		let lSeptumHeight_num = ~~(GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT*0.8);

		let lSeptum_sv = this._fSeptumsContainer_sprt.addChild(new SeptumView(lSeptumWidth_num, lSeptumHeight_num));
		lSeptum_sv.visible = true;

		return lSeptum_sv;
	}
	//...SEPTUMS
	
	destroy()
	{
		super.destroy();

		this._fSoundButtonView_gsbv = null;
		this._fSettingsButton_sb = null;
		this._fInfoButton_sb = null;
		this._fBalanceIndicator_bb = null;
		this._fTimeIndicator_tb = null;
		this._fWinsIndicator_wsb = null;
		this._fHistoryButton_sb = null;
		this._fHomeButton_sb = null;
		this._fBackButton_sb = null;
		
		this._fRightContainer_sprt = null;
		this._fLeftContainer_sprt = null;
		this._fSeptumsContainer_sprt = null;
	}
}

export default BottomPanelView;