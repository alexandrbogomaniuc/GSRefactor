import I18 from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18";
import { APP } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import { BitmapText, Sprite } from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import SimpleUIView from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView";
import AtlasConfig from "../../../../config/AtlasConfig";
import AtlasSprite from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite";

class TopPanelView extends SimpleUIView
{   

	updateRake(aValue_num)
	{
		this._fTotalRakeValue_num = aValue_num;
	}

	updateTotalPot(aValue_num)
	{
		let lTotalPotValue_num = aValue_num*(100-this._fTotalRakeValue_num)/100;
		let lFormattedValue_str;

		// [OWL] TODO: apply changes for alll systems without any conditions
		if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
		{
			lFormattedValue_str = APP.currencyInfo.i_formatNumber(lTotalPotValue_num, false, APP.isBattlegroundGame, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
		}
		else
		{
			lFormattedValue_str = APP.currencyInfo.i_formatNumber(lTotalPotValue_num, false, APP.isBattlegroundGame, 2, undefined, false);
		}
		let lTotalPotValue_str = lFormattedValue_str;
		let lSymbol_str = APP.currencyInfo.i_getCurrencySymbol();
		if(lSymbol_str.toUpperCase() === "QC")
		{
			this._leftAlignedCurrencyMark_bl = false;
			this._fTotalPotCurrencySymbol_ta.write("PM");
		}else{
			this._leftAlignedCurrencyMark_bl = true;
			this._fTotalPotCurrencySymbol_ta.write("$");
		}
		
		this._fTotalPotValue_ta.write(lTotalPotValue_str);
		this.validate();
	}

	updateTotalAstronauts(aValue_num)
	{
		
		this._fTotalPotAStronautsValue_tf.write(aValue_num + "");
	}

	updateLayout(aLayout_rt, aIsPortraitMode_bl)
	{
		this._fContentX_num = aLayout_rt.x;
		this._fContentY_num = aLayout_rt.y;

		this._updateViewPosition(aIsPortraitMode_bl);
		
		if (this._fContentWidth_num !== aLayout_rt.width || this._fContentHeight_num !== aLayout_rt.height || this._fIsPortraitMode_bl !== aIsPortraitMode_bl)
		{
			this._fContentWidth_num = aLayout_rt.width;
			this._fContentHeight_num = aLayout_rt.height;
			this._fIsPortraitMode_bl = aIsPortraitMode_bl;

			if (this.uiInfo) // view is already initialized
			{
				this._updateLayoutSettings();
			}
		}
	}

	adjustLayoutSettings()
	{
		this._updateViewPosition(this._fIsPortraitMode_bl);


		this._updateLayoutSettings();
	}

	_updateViewPosition(aIsPortraitMode_bl)
	{
		let lX_num = this._fContentX_num || 0;
		let lY_num = this._fContentY_num || 0;

		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;

		if (!!aIsPortraitMode_bl && lRoundInfo_ri.isRoundWaitState)
		{
			lY_num += 10;
		}

		this.position.set(lX_num, lY_num);
	}

	validate()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lIsPortraitMode_bi = APP.layout.isPortraitOrientation;
		const layoutXOffset = 10; 
		const totalPotWidth = (this._fTotalPotValue_ta.textWidth * this._fTotalPotValue_ta.scale.x) ;
		const halfWidthTotalPotValue = totalPotWidth / 2;
		const topAnker = -37; 
		this._fTotalPotValue_ta.position.set(( halfWidthTotalPotValue *-1) + layoutXOffset, topAnker);
		if(this._leftAlignedCurrencyMark_bl)
		{
			this._fTotalPotCurrencySymbol_ta.position.set(this._fTotalPotValue_ta.position.x - this._fTotalPotCurrencySymbol_ta.textWidth - layoutXOffset, topAnker);
		}else{
			this._fTotalPotCurrencySymbol_ta.position.set(this._fTotalPotValue_ta.position.x + totalPotWidth + layoutXOffset , topAnker);
		}

		if (lRoundInfo_ri.isRoundQualifyState || lRoundInfo_ri.isRoundPlayState)
		{
			this._fTotalPotWin_sprt.y = lIsPortraitMode_bi ? 45:25;
			this._fTotalPot_ta.visible = true;
			this._fNextRoundPot_ta.visible = false;
			this._fTotalPotAStronautsValue_tf.visible = true;

			this._fCrown_sprt.position.set(0, -59);
			this._fTotalPotFrame_sprt.position.y = -9;

			if (lIsPortraitMode_bi)
			{
				this._fBaseContainer_sprt.scale.set(1.17, 1.1);
				this._fTotalPotAstronautsValue_ta.visible = false;
				if (this._fTotalAstrounautsValuePortrait_ta)
				{
					this._fTotalAstrounautsValuePortrait_ta.visible = true;
				}
				this._fTotalPotFrame_sprt.position.y = 8;
				this._fCrown_sprt.position.y = -44;
			}
			else
			{
				this._fBaseContainer_sprt.scale.set(1.17, 1);
				this._fTotalPotAstronautsValue_ta.visible = true;
				if (this._fTotalAstrounautsValuePortrait_ta)
				{
					this._fTotalAstrounautsValuePortrait_ta.visible = false;
				}
			}
			this._fBaseContainer_sprt.position.set(127, 112);
			this._fPortraitAstronautsRemainingPanelBase_gr.visible = true;
		}
		else
		{
			this._fTotalPotWin_sprt.y = 0;
			this._fTotalPot_ta.visible = false;
			this._fNextRoundPot_ta.visible = true;
			this._fTotalPotAstronautsValue_ta.visible = false;
			this._fTotalPotAStronautsValue_tf.visible = false;

			if (lIsPortraitMode_bi)
			{
				let lPortraitModeYScale = APP.isMobile ? 0.83 : 0.88;
				this._fBaseContainer_sprt.scale.set(1.17, lPortraitModeYScale);
			}
			else
			{
				this._fBaseContainer_sprt.scale.set(1.17, 0.72);
			}
			let lTopPoint_num = APP.isMobile ? lIsPortraitMode_bi ? 74 : 80 : 80;
			this._fBaseContainer_sprt.position.set(127, lTopPoint_num);

			if (APP.isMobile)
			{
				
				this._fTotalPotFrame_sprt.position.y = lIsPortraitMode_bi ? -35 : -30;
				this._fCrown_sprt.position.set(0, lIsPortraitMode_bi ? -85 : -80);
			}
			else
			{
				
				this._fTotalPotFrame_sprt.position.y = lIsPortraitMode_bi ? -30 : -35;
				this._fCrown_sprt.position.set(0, lIsPortraitMode_bi ? -80 : -85);
			}

			if (this._fTotalAstrounautsValuePortrait_ta)
			{
				this._fTotalAstrounautsValuePortrait_ta.visible = false;
			}
			this._fPortraitAstronautsRemainingPanelBase_gr.visible = false;
		}
	}

	//INIT...
	constructor()
	{
		super();

		this._fContentWidth_num = undefined;
		this._fContentHeight_num = undefined;
		this._fContentX_num = undefined;
		this._fContentY_num = undefined;

		this._fIsPortraitMode_bl = false;

		this._fTotalPotValue = null;
		this._fTotalRakeValue_num = null;
		this._fTotalPotValueTemplate_str = null;
		this._fTotalPotAstronautsValue_ta = null;
		this._fTotalPotAstronautsValueTemplate_str = null;

		this._fTotalPotCurrencySymbol_ta = null;

		this._fTotalPot_ta = null;
		this._fNextRoundPot_ta = null;

		this._fTotalPotFrame_sprt = null;
		this._fCrown_sprt = null;

		this._fPortraitAstronautsRemainingPanelBase_gr = null;
		this._fTotalAstrounautsValuePortrait_ta = null;
	}

	__init()
	{
		this._fBaseContainer_sprt = this.addChild(new Sprite());
		this._fTextures_full_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset("scorescoreboard_font/scoreboard_font")], [AtlasConfig.ScoreBoardFont], "");

		this._fBaseContainer_sprt.addChild(APP.library.getSprite("game/total_pot_bg"));
		this._fBaseContainer_sprt.position.set(127, 112);
		this._fBaseContainer_sprt.scale.set(1.17, 1);

		this._fPortraitAstronautsRemainingPanelBase_gr = this.addChild(new PIXI.Graphics);

		let lTotalPot_ta = this._fTotalPot_ta = this.addChild(new BitmapText(this._fTextures_full_tx_map, "", 1));
		lTotalPot_ta.visible = false;
		lTotalPot_ta.write("TOTAL POT");
		lTotalPot_ta.scale.set(0.85,0.85);

		let lNextRoundPot_ta = this._fNextRoundPot_ta = this.addChild(new BitmapText(this._fTextures_full_tx_map, "", 1));
		lNextRoundPot_ta.visible = false;
		lNextRoundPot_ta.write("NEXT ROUND POT");
		lNextRoundPot_ta.scale.set(0.85,0.85);

		let lContentContainer_sprt = this._fContentContainer_sprt = this.addChild(new Sprite());
		this._fTotalPotFrame_sprt = lContentContainer_sprt.addChild(APP.library.getSprite("game/total_pot_frame"));
		lContentContainer_sprt.position.set(127, 120);

		let lCrown_spr = this._fCrown_sprt = lContentContainer_sprt.addChild(APP.library.getSprite("game/crown"));
		lCrown_spr.position.set(0, -50);

		let lTotalPotWin_sprt = this._fTotalPotWin_sprt = lContentContainer_sprt.addChild(new Sprite());
		this._fTotalPotWin_sprt.position.x = -11;

		
		this._fTotalPotValue_ta = lTotalPotWin_sprt.addChild(new BitmapText(this._fTextures_full_tx_map, "", 5));
		this._fTotalPotValue_ta.addTint(0xf4d425);
		this._fTotalPotValueTemplate_str = this._fTotalPotValue_ta.text;
		this._fTotalPotValue_ta.scale.set(1.5,1.5);
		//this._fTotalPotValue_ta.position.set(0,-38);

		this._fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset("roboto/bmp_roboto_bold")], [AtlasConfig.BmpRobotoBold], "");

		this._fTotalPotCurrencySymbol_ta = lTotalPotWin_sprt.addChild(new BitmapText(this._fTextures_full_tx_map, "", 5));
		this._fTotalPotCurrencySymbol_ta.position.set(0,0);
		this._fTotalPotCurrencySymbol_ta.addTint(0xf4d425);

		this._fTotalPotAstronautsValue_ta = this.addChild(APP.library.getSprite("labels/total_astronauts"));
		this._fTotalPotAstronautsValue_ta.scale.set(0.85,0.85);

		this._fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset("roboto/bmp_roboto_bold")], [AtlasConfig.BmpRobotoBold], "");
		this._fTotalPotAStronautsValue_tf = this.addChild(new BitmapText(this._fTextures_tx_map, "", 0));
		this._fTotalPotAStronautsValue_tf.addTint(0xffffff);
		this._updateLayoutSettings();
	}
	
	//...INIT

	_updateLayoutSettings()
	{
		if (this._fContentWidth_num === undefined || this._fContentHeight_num === undefined) return;
		

		if (APP.isMobile)
		{
			this._fTotalPot_ta.position.set(125 - ((this._fTotalPot_ta.textWidth * this._fTotalPot_ta.scale.x )/2), 25);
			this._fTotalPotAstronautsValue_ta.position.set(68, 183);
			this._fNextRoundPot_ta.position.set(125- ((this._fNextRoundPot_ta.textWidth * this._fNextRoundPot_ta.scale.x )/2), this._fIsPortraitMode_bl ? 5 : 14);
		}
		else
		{
			this._fTotalPot_ta.position.set(125 -  ((this._fTotalPot_ta.textWidth * this._fTotalPot_ta.scale.x )/2), 28);
			this._fTotalPotAstronautsValue_ta.position.set(70, 185);
			this._fNextRoundPot_ta.position.set(125- ((this._fNextRoundPot_ta.textWidth * this._fNextRoundPot_ta.scale.x )/2), 14);
		}

		if (this._fIsPortraitMode_bl)
		{
			let lBaseContainerBounds_rect = this._fBaseContainer_sprt.getLocalBounds();
			let lBaseContainerLeftPoint_num = this._fBaseContainer_sprt.position.x;
			this._fPortraitAstronautsRemainingPanelBase_gr.beginFill(0x111421).drawRect(
				lBaseContainerLeftPoint_num + lBaseContainerBounds_rect.width/2 + 29,
				-8,
				lBaseContainerBounds_rect.width + 36,
				APP.isMobile ? 35 : 44
				);

			if (!this._fTotalAstrounautsValuePortrait_ta)
			{
				this._fTotalAstrounautsValuePortrait_ta = this._fPortraitAstronautsRemainingPanelBase_gr.addChild(APP.library.getSprite("labels/total_astronauts_portrait"));
				this._fTotalAstrounautsValuePortrait_ta.x = 370;
				this._fTotalAstrounautsValuePortrait_ta.y = 7;
				this._fTotalAstrounautsValuePortrait_ta.scale.set(0.8,0.8);
			}
			this._fTotalAstrounautsValuePortrait_ta.visible = true;

			this._fTotalPotAStronautsValue_tf.position.set(475, APP.isMobile ? 13 : 15);
			this._fTotalPotAStronautsValue_tf.scale.set(0.2,0.2);
		}
		else
		{
			if (this._fTotalAstrounautsValuePortrait_ta)
			{
				this._fTotalAstrounautsValuePortrait_ta.visible = false;
			}
			this._fPortraitAstronautsRemainingPanelBase_gr.clear();


			this._fTotalPotAStronautsValue_tf.position.set(120, APP.isMobile ? 193:194);
			this._fTotalPotAStronautsValue_tf.scale.set(0.16,0.16);
		}

		this.validate();
	}
}

export default TopPanelView;