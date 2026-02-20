import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import CheckBox from '../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/CheckBox';
import Button from '../../../ui/GameButton';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MultiCheckBox from '../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/MultiCheckBox';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { AtlasSprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasConfig from './../../../config/AtlasConfig';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

var fire_settings_textures = null;
function _initFireSettingsTextures(isMobile = false)
{
	if (!fire_settings_textures)
	{
		if (isMobile)
		{
			fire_settings_textures = AtlasSprite.getFrames(APP.library.getAsset("fire_settings/fire_settings_mobile"), AtlasConfig.FireSettingsMobile, "");
		}
		else
		{
			fire_settings_textures = AtlasSprite.getFrames(APP.library.getAsset("fire_settings/fire_settings"), AtlasConfig.FireSettings, "");
		}
	}
}

class FireSettingsView extends SimpleUIView
{
	static get EVENT_ON_DEACTIVATE()			{ return "onDeactivate"; }
	static get EVENT_ON_FIRE_SETTINGS_CHANGED()	{ return "onFireSettingsChanged"; }

	activate()
	{
		this._activate();
	}

	deactivate()
	{
		this._deactivate();
	}

	update()
	{
		this._update();
	}

	constructor()
	{
		super();

		this._fBindedDocClick_fn = null;
		this._fClickFlag_bln = null;

		this._fLockOnTargetCheckBox_cb = null;
		this._fTargetPriorityCheckBox_mcb = null;
		this._fAutoFireCheckBox_cb = null;
		this._fFireSpeedCheckBox_mcb = null;

		this._fBackContainer_sprt = null;

		this._targetPriorityCaptions_arr = null;
	}

	_getFireSettingsTexture(name)
	{
		_initFireSettingsTextures(APP.isMobile);

		return Utils.getTexture(fire_settings_textures, name);
	}

	__init()
	{
		this._fBindedDocClick_fn = this._onDocumentClicked.bind(this);
		this._fClickFlag_bln = false;

		this._targetPriorityCaptions_arr = [];

		this._initBaseView();

		if (!APP.isMobile)
		{
			if(APP.playerController.info.isDisableAutofiring)
			{
				this.position.set(315, 201);
				this._initDisableAutofiringDesktopView();
			}
			else
			{
				this.position.set(315, 185);
				this._initDesktopView();
			}
		}
		else
		{
			if(APP.playerController.info.isDisableAutofiring)
			{
				this._initDisableAutofiringMobileView();
			}
			else
			{
				this._initMobileView();
			}
		}

		this._updateTargetPriorityCaption();
	}

	_initDisableAutofiringDisableTargetPriorityDesktopView()
	{
		let lBack_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lBack_grphc.beginFill(0x000000, 0.9).drawRoundedRect(-57, -58, 114, 92, 4).endFill();

		let lTriangle_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lTriangle_grphc.beginFill(0x000000, 0.9).lineStyle(0).moveTo(0, 7).lineTo(-6, 0).lineTo(6, 0).lineTo(0, 7).endFill();
		lTriangle_grphc.position.set(0, 34);

		let lLeftArrow_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lLeftArrow_sprt.texture = this._getFireSettingsTexture("arrow_down");
		lLeftArrow_sprt.position.set(-22, -50);

		let lRightArrow_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lRightArrow_sprt.texture = this._getFireSettingsTexture("arrow_down");
		lRightArrow_sprt.position.set(22, -50);

		this._fLockOnTargetCheckBox_cb.position.set(-40, 3-13);
		this._fLockOnTargetCheckBox_cb.scaleStateViews(0.8);

		this._fAutoFireCheckBox_cb.visible = false;
		this._fTargetPriorityCheckBox_mcb.visible = false;

		this._fFireSpeedCheckBox_mcb.position.set(-40, 27-13);
		this._fFireSpeedCheckBox_mcb.scaleStateViews(0.8);

		let lOptionsCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsOptions"));
		lOptionsCaption_ta.position.set(0, -39);

		let lLockOnTargetAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingslockOnTarget"));
		lLockOnTargetAsset_ta.position.set(-26, 3-13);

		let lFireSpeedAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsFireSpeed"));
		lFireSpeedAsset_ta.position.set(-26, 27-13);
	}

	_initDisableAutofiringDesktopView()
	{
		let lBack_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lBack_grphc.beginFill(0x000000, 0.9).drawRoundedRect(-57, -58, 114, 92, 4).endFill();

		let lTriangle_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lTriangle_grphc.beginFill(0x000000, 0.9).lineStyle(0).moveTo(0, 7).lineTo(-6, 0).lineTo(6, 0).lineTo(0, 7).endFill();
		lTriangle_grphc.position.set(0, 34);

		let lLeftArrow_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lLeftArrow_sprt.texture = this._getFireSettingsTexture("arrow_down");
		lLeftArrow_sprt.position.set(-22, -50);

		let lRightArrow_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lRightArrow_sprt.texture = this._getFireSettingsTexture("arrow_down");
		lRightArrow_sprt.position.set(22, -50);

		this._fTargetPriorityCheckBox_mcb.position.set(-40, -19);
		this._fTargetPriorityCheckBox_mcb.scaleStateViews(1);

		this._fLockOnTargetCheckBox_cb.position.set(-40, 3);
		this._fLockOnTargetCheckBox_cb.scaleStateViews(0.8);

		this._fAutoFireCheckBox_cb.visible = false;

		this._fFireSpeedCheckBox_mcb.position.set(-40, 27);
		this._fFireSpeedCheckBox_mcb.scaleStateViews(0.8);

		let lOptionsCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsOptions"));
		lOptionsCaption_ta.position.set(0, -39);

		let lLockOnTargetAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingslockOnTarget"));
		lLockOnTargetAsset_ta.position.set(-26, 3);

		this._addDesktopTargetTypeCaption("TAFireSettingsTargetHigh");
		this._addDesktopTargetTypeCaption("TAFireSettingsTargetLow");
		this._addDesktopTargetTypeCaption("TAFireSettingsTargetSame");

		let lFireSpeedAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsFireSpeed"));
		lFireSpeedAsset_ta.position.set(-26, 27);
	}

	_initDisableAutofiringDisableTargetPriorityMobileView()
	{
		let lBack_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lBack_grphc.beginFill(0x000000, 0.9).drawRoundedRect(-163, -194+18, 325, 375-36, 4).endFill();

		let lBlackBack_gr = new PIXI.Graphics();
		lBlackBack_gr.beginFill(0x000000, 0.45);
		lBlackBack_gr.drawRect(-480, -270, 960, 540);
		lBlackBack_gr.endFill();
		this._fBackContainer_sprt.addChildAt(lBlackBack_gr, 0);

		let lBackTopMap_sprt = this._fBackContainer_sprt.addChild(APP.library.getSprite("fire_settings/header"));
		lBackTopMap_sprt.position.set(0, -163);

		let lPistol_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lPistol_sprt.texture = this._getFireSettingsTexture("pistol_icon");
		lPistol_sprt.position.set(-110, -163);

		let lLine_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lLine_grphc.beginFill(0xffffff, 0.4).drawRect(-127, -1+15, 254, 1).endFill();
		lLine_grphc.position.set(0, 82-18);

		let lOkButton_btn = this._fBackContainer_sprt.addChild(new Button(this._getFireSettingsTexture("btn_ok"), "TAFireSettingsOkMobile", true, undefined, undefined, Button.BUTTON_TYPE_ACCEPT));
		lOkButton_btn.position.set(0, 138-18);
		lOkButton_btn.on("pointerclick", this._onOkButtonClicked, this);

		this._fTargetPriorityCheckBox_mcb.visible = false;

		this._fLockOnTargetCheckBox_cb.position.set(-110, -45+18);
		this._fLockOnTargetCheckBox_cb.scale.set(1.84);
		this._fLockOnTargetCheckBox_cb.scaleStateViews(1.2);

		this._fAutoFireCheckBox_cb.visible = false;

		this._fFireSpeedCheckBox_mcb.position.set(-110, 7+18);
		this._fFireSpeedCheckBox_mcb.scale.set(1.84);
		this._fFireSpeedCheckBox_mcb.scaleStateViews(1.2);

		let lOptionsCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsOptionsMobile"));
		lOptionsCaption_ta.position.set(-64, -163+18);

		let lLockOnTargetAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingslockOnTargetMobile"));
		lLockOnTargetAsset_ta.position.set(-64, -45+18);

		let lFireSpeedAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsFireSpeedMobile"));
		lFireSpeedAsset_ta.position.set(-64, 7+18);
	}

	_initDisableAutofiringMobileView()
	{
		let lBack_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lBack_grphc.beginFill(0x000000, 0.9).drawRoundedRect(-163, -194+18, 325, 375-36, 4).endFill();

		let lBlackBack_gr = new PIXI.Graphics();
		lBlackBack_gr.beginFill(0x000000, 0.45);
		lBlackBack_gr.drawRect(-480, -270, 960, 540);
		lBlackBack_gr.endFill();
		this._fBackContainer_sprt.addChildAt(lBlackBack_gr, 0);

		let lBackTopMap_sprt = this._fBackContainer_sprt.addChild(APP.library.getSprite("fire_settings/header"));
		lBackTopMap_sprt.position.set(0, -163);

		let lPistol_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lPistol_sprt.texture = this._getFireSettingsTexture("pistol_icon");
		lPistol_sprt.position.set(-110, -163);

		let lLine_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lLine_grphc.beginFill(0xffffff, 0.4).drawRect(-127, -1+15, 254, 1).endFill();
		lLine_grphc.position.set(0, 82-18);

		let lOkButton_btn = this._fBackContainer_sprt.addChild(new Button(this._getFireSettingsTexture("btn_ok"), "TAFireSettingsOkMobile", true, undefined, undefined, Button.BUTTON_TYPE_ACCEPT));
		lOkButton_btn.position.set(0, 138-18);
		lOkButton_btn.on("pointerclick", this._onOkButtonClicked, this);

		this._fTargetPriorityCheckBox_mcb.position.set(-110, -96+15);
		this._fTargetPriorityCheckBox_mcb.scale.set(1.84);
		this._fTargetPriorityCheckBox_mcb.scaleStateViews(1.2);

		this._fLockOnTargetCheckBox_cb.position.set(-110, -45+15);
		this._fLockOnTargetCheckBox_cb.scale.set(1.84);
		this._fLockOnTargetCheckBox_cb.scaleStateViews(1.2);

		this._fAutoFireCheckBox_cb.visible = false;

		this._fFireSpeedCheckBox_mcb.position.set(-110, 7+15);
		this._fFireSpeedCheckBox_mcb.scale.set(1.84);
		this._fFireSpeedCheckBox_mcb.scaleStateViews(1.2);

		let lOptionsCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsOptionsMobile"));
		lOptionsCaption_ta.position.set(-64, -163+18);

		let lLockOnTargetAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingslockOnTargetMobile"));
		lLockOnTargetAsset_ta.position.set(-64, -45+18);

		this._addMobileTargetTypeCaption("TAFireSettingsTargetHighMobile");
		this._addMobileTargetTypeCaption("TAFireSettingsTargetLowMobile");
		this._addMobileTargetTypeCaption("TAFireSettingsTargetSameMobile");

		let lFireSpeedAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsFireSpeedMobile"));
		lFireSpeedAsset_ta.position.set(-64, 7+18);
	}

	_initBaseView()
	{
		this._fBackContainer_sprt = this.addChild(new Sprite());
		this._fBackContainer_sprt.position.set(0, 11);

		this._fLockOnTargetCheckBox_cb = this.addChild(new CheckBox(this._getFireSettingsTexture("checkbox_back"), this._getFireSettingsTexture("checkmark")));
		this._fLockOnTargetCheckBox_cb.on(CheckBox.EVENT_ON_CHECKBOX_STATE_CHANGED, this._onLockOnTargetStateChanged, this);
		this._fLockOnTargetCheckBox_cb.checkState = this.uiInfo.lockOnTarget;

		this._fAutoFireCheckBox_cb = this.addChild(new CheckBox(this._getFireSettingsTexture("checkbox_back"), this._getFireSettingsTexture("checkmark")));
		this._fAutoFireCheckBox_cb.on(CheckBox.EVENT_ON_CHECKBOX_STATE_CHANGED, this._onAutoFireStateChanged, this);
		this._fAutoFireCheckBox_cb.checkState = this.uiInfo.autoFire;

		this._fTargetPriorityCheckBox_mcb = this.addChild(new MultiCheckBox(this._getFireSettingsTexture("checkbox_back"), 
			[this._getFireSettingsTexture("target_high"), this._getFireSettingsTexture("target_low"), this._getFireSettingsTexture("target_same")])
		);
		this._fTargetPriorityCheckBox_mcb.on(CheckBox.EVENT_ON_CHECKBOX_STATE_CHANGED, this._onTargetPriorityStateChanged, this);
		this._fTargetPriorityCheckBox_mcb.checkState = this.uiInfo.targetPriority;

		this._fFireSpeedCheckBox_mcb = this.addChild(new MultiCheckBox((this._getFireSettingsTexture("checkbox_back")), 
			[this._getFireSettingsTexture("speed_1"), this._getFireSettingsTexture("speed_2"), this._getFireSettingsTexture("speed_3")])
		);
		this._fFireSpeedCheckBox_mcb.on(CheckBox.EVENT_ON_CHECKBOX_STATE_CHANGED, this._onFireSpeedStateChanged, this);
		this._fFireSpeedCheckBox_mcb.checkState = this.uiInfo.fireSpeed;
	}

	_initDisableTargetPriorityDesktopView()
	{
		let lBack_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lBack_grphc.beginFill(0x000000, 0.9).drawRoundedRect(-57, -68, 114, 114, 4).endFill();

		let lTriangle_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lTriangle_grphc.beginFill(0x000000, 0.9).lineStyle(0).moveTo(0, 7).lineTo(-6, 0).lineTo(6, 0).lineTo(0, 7).endFill();
		lTriangle_grphc.position.set(0, 57-11);

		let lLeftArrow_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lLeftArrow_sprt.texture = this._getFireSettingsTexture("arrow_down");
		lLeftArrow_sprt.position.set(-22, -72+11);

		let lRightArrow_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lRightArrow_sprt.texture = this._getFireSettingsTexture("arrow_down");
		lRightArrow_sprt.position.set(22, -72+11);

		this._fLockOnTargetCheckBox_cb.position.set(-40, -30+22-14);
		this._fLockOnTargetCheckBox_cb.scaleStateViews(0.8);

		this._fTargetPriorityCheckBox_mcb.visible = false;

		this._fAutoFireCheckBox_cb.position.set(-40, -6+22-14);
		this._fAutoFireCheckBox_cb.scaleStateViews(0.8);

		this._fFireSpeedCheckBox_mcb.position.set(-40, 16+22-14);
		this._fFireSpeedCheckBox_mcb.scaleStateViews(0.8);

		let lOptionsCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsOptions"));
		lOptionsCaption_ta.position.set(0, -72+22);

		let lLockOnTargetAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingslockOnTarget"));
		lLockOnTargetAsset_ta.position.set(-26, -30+22-14);

		let lAutoFireAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsAutoFire"));
		lAutoFireAsset_ta.position.set(-26, -6+22-14);

		let lFireSpeedAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsFireSpeed"));
		lFireSpeedAsset_ta.position.set(-26, 16+22-14);
	}

	_initDesktopView()
	{
		let lBack_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lBack_grphc.beginFill(0x000000, 0.9).drawRoundedRect(-57, -68, 114, 114, 4).endFill();

		let lTriangle_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lTriangle_grphc.beginFill(0x000000, 0.9).lineStyle(0).moveTo(0, 7).lineTo(-6, 0).lineTo(6, 0).lineTo(0, 7).endFill();
		lTriangle_grphc.position.set(0, 57-11);

		let lLeftArrow_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lLeftArrow_sprt.texture = this._getFireSettingsTexture("arrow_down");
		lLeftArrow_sprt.position.set(-22, -72+11);

		let lRightArrow_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lRightArrow_sprt.texture = this._getFireSettingsTexture("arrow_down");
		lRightArrow_sprt.position.set(22, -72+11);

		this._fLockOnTargetCheckBox_cb.position.set(-40, -30+22);
		this._fLockOnTargetCheckBox_cb.scaleStateViews(0.8);

		this._fAutoFireCheckBox_cb.position.set(-40, -6+22);
		this._fAutoFireCheckBox_cb.scaleStateViews(0.8);
		
		this._fTargetPriorityCheckBox_mcb.position.set(-40, -52+22);
		this._fTargetPriorityCheckBox_mcb.scaleStateViews(1);

		this._fFireSpeedCheckBox_mcb.position.set(-40, 16+22);
		this._fFireSpeedCheckBox_mcb.scaleStateViews(0.8);

		let lOptionsCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsOptions"));
		lOptionsCaption_ta.position.set(0, -72+22);

		let lLockOnTargetAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingslockOnTarget"));
		lLockOnTargetAsset_ta.position.set(-26, -30+22);

		this._addDesktopTargetTypeCaption("TAFireSettingsTargetHigh");
		this._addDesktopTargetTypeCaption("TAFireSettingsTargetLow");
		this._addDesktopTargetTypeCaption("TAFireSettingsTargetSame");
		
		let lAutoFireAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsAutoFire"));
		lAutoFireAsset_ta.position.set(-26, -6+22);

		let lFireSpeedAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsFireSpeed"));
		lFireSpeedAsset_ta.position.set(-26, 16+22);
	}

	_addDesktopTargetTypeCaption(captionAsset)
	{
		let lTargetPriorityAsset_ta = this.addChild(I18.generateNewCTranslatableAsset(captionAsset));
		lTargetPriorityAsset_ta.position.set(-26, APP.playerController.info.isDisableAutofiring ? -19 : -52 + 22);

		lTargetPriorityAsset_ta.visible = false;

		this._targetPriorityCaptions_arr.push(lTargetPriorityAsset_ta);
	}

	_initDisableTargetPriorityMobileView()
	{
		let lBack_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lBack_grphc.beginFill(0x000000, 0.9).drawRoundedRect(-163, -219+18, 325, 425-36, 4).endFill();

		let lBlackBack_gr = new PIXI.Graphics();
		lBlackBack_gr.beginFill(0x000000, 0.45);
		lBlackBack_gr.drawRect(-480, -270, 960, 540);
		lBlackBack_gr.endFill();
		this._fBackContainer_sprt.addChildAt(lBlackBack_gr, 0);

		let lBackTopMap_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lBackTopMap_sprt.texture = this._getFireSettingsTexture("header");
		lBackTopMap_sprt.position.set(0, -188+18);

		let lPistol_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lPistol_sprt.texture = this._getFireSettingsTexture("pistol_icon");
		lPistol_sprt.position.set(-110, -188+18);

		let lLine_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lLine_grphc.beginFill(0xffffff, 0.4).drawRect(-127, -1+15-18, 218, 1).endFill();
		lLine_grphc.position.set(0, 107);

		let lOkButton_btn = this._fBackContainer_sprt.addChild(new Button(this._getFireSettingsTexture("btn_ok"), "TAFireSettingsOkMobile", true, undefined, undefined, Button.BUTTON_TYPE_ACCEPT));
		lOkButton_btn.position.set(0, 148+15-18);
		lOkButton_btn.on("pointerclick", this._onOkButtonClicked, this);

		this._fLockOnTargetCheckBox_cb.position.set(-110, -85+15+36-40);
		this._fLockOnTargetCheckBox_cb.scale.set(1.84);
		this._fLockOnTargetCheckBox_cb.scaleStateViews(1.2);

		this._fTargetPriorityCheckBox_mcb.visible = false;

		this._fAutoFireCheckBox_cb.position.set(-110, -34+15+36-40);
		this._fAutoFireCheckBox_cb.scale.set(1.84);
		this._fAutoFireCheckBox_cb.scaleStateViews(1.2);

		this._fFireSpeedCheckBox_mcb.position.set(-110, 17+15+36-40);
		this._fFireSpeedCheckBox_mcb.scale.set(1.84);
		this._fFireSpeedCheckBox_mcb.scaleStateViews(1.2);

		let lOptionsCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsOptionsMobile"));
		lOptionsCaption_ta.position.set(-64, -188+36);

		let lLockOnTargetAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingslockOnTargetMobile"));
		lLockOnTargetAsset_ta.position.set(-64, -85+15+36-40);

		let lAutoFireAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsAutoFireMobile"));
		lAutoFireAsset_ta.position.set(-64, -34+15+36-40);

		let lFireSpeedAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsFireSpeedMobile"));
		lFireSpeedAsset_ta.position.set(-64, 17+15+36-40);
	}

	_initMobileView()
	{
		let lBack_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lBack_grphc.beginFill(0x000000, 0.9).drawRoundedRect(-163, -219-10, 325, 425, 4).endFill();

		let lBlackBack_gr = new PIXI.Graphics();
		lBlackBack_gr.beginFill(0x000000, 0.45);
		lBlackBack_gr.drawRect(-480, -270, 960, 540);
		lBlackBack_gr.endFill();
		this._fBackContainer_sprt.addChildAt(lBlackBack_gr, 0);

		let lBackTopMap_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lBackTopMap_sprt.texture = this._getFireSettingsTexture("header");
		lBackTopMap_sprt.position.set(0, -188-10);

		let lPistol_sprt = this._fBackContainer_sprt.addChild(new Sprite());
		lPistol_sprt.texture = this._getFireSettingsTexture("pistol_icon");
		lPistol_sprt.position.set(-110, -188);

		let lLine_grphc = this._fBackContainer_sprt.addChild(new PIXI.Graphics());
		lLine_grphc.beginFill(0xffffff, 0.4).drawRect(-127, -1+15-18, 218, 1).endFill();
		lLine_grphc.position.set(15, 107);

		let lOkButton_btn = this._fBackContainer_sprt.addChild(new Button(this._getFireSettingsTexture("btn_ok"), "TAFireSettingsOkMobile", true, undefined, undefined, Button.BUTTON_TYPE_ACCEPT));
		lOkButton_btn.position.set(0, 148+15-18);
		lOkButton_btn.on("pointerclick", this._onOkButtonClicked, this);

		this._fLockOnTargetCheckBox_cb.position.set(-110, -85+15+8);
		this._fLockOnTargetCheckBox_cb.scale.set(1.84);
		this._fLockOnTargetCheckBox_cb.scaleStateViews(1.2);

		this._fTargetPriorityCheckBox_mcb.position.set(-110, -121+8);
		this._fTargetPriorityCheckBox_mcb.scale.set(1.84);
		this._fTargetPriorityCheckBox_mcb.scaleStateViews(1.2);

		this._fAutoFireCheckBox_cb.position.set(-110, -34+15+8);
		this._fAutoFireCheckBox_cb.scale.set(1.84);
		this._fAutoFireCheckBox_cb.scaleStateViews(1.2);

		this._fFireSpeedCheckBox_mcb.position.set(-110, 17+15+8);
		this._fFireSpeedCheckBox_mcb.scale.set(1.84);
		this._fFireSpeedCheckBox_mcb.scaleStateViews(1.2);

		let lOptionsCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsOptionsMobile"));
		lOptionsCaption_ta.position.set(-64, -188+8);

		let lLockOnTargetAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingslockOnTargetMobile"));
		lLockOnTargetAsset_ta.position.set(-64, -85+15+8);

		this._addMobileTargetTypeCaption("TAFireSettingsTargetHighMobile");
		this._addMobileTargetTypeCaption("TAFireSettingsTargetLowMobile");
		this._addMobileTargetTypeCaption("TAFireSettingsTargetSameMobile");

		let lAutoFireAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsAutoFireMobile"));
		lAutoFireAsset_ta.position.set(-64, -34+15+8);

		let lFireSpeedAsset_ta = this.addChild(I18.generateNewCTranslatableAsset("TAFireSettingsFireSpeedMobile"));
		lFireSpeedAsset_ta.position.set(-64, 17+15+8);
	}

	_addMobileTargetTypeCaption(captionAsset)
	{
		let lTargetPriorityAsset_ta = this.addChild(I18.generateNewCTranslatableAsset(captionAsset));
		lTargetPriorityAsset_ta.position.set(-64, APP.playerController.info.isDisableAutofiring ? -96+15 : -121+8);
		lTargetPriorityAsset_ta.visible = false;

		this._targetPriorityCaptions_arr.push(lTargetPriorityAsset_ta);
	}

	_update()
	{
		let lLockOnTarget_bl = APP.playerController.info.lockOnTarget;
		let lTargetPriority_int = APP.playerController.info.targetPriority;
		let lAutoFire_bl = APP.playerController.info.autoFire;
		let lFireSpeed_int = APP.playerController.info.fireSpeed;

		if(APP.playerController.info.isDisableAutofiring)
		{
			lAutoFire_bl = false;
		}

		this._fLockOnTargetCheckBox_cb.checkState = lLockOnTarget_bl ? 1 : 0;
		this._fAutoFireCheckBox_cb.checkState = lAutoFire_bl ? 1 : 0;
		this._fTargetPriorityCheckBox_mcb.checkState = lTargetPriority_int + 1;
		this._fFireSpeedCheckBox_mcb.checkState = lFireSpeed_int + 1;

		this._updateTargetPriorityCaption();
	}

	_updateTargetPriorityCaption()
	{
		for (let i=0; i<this._targetPriorityCaptions_arr.length; i++)
		{
			this._targetPriorityCaptions_arr[i].visible = (i == (this.uiInfo.targetPriority-1));
		}
	}

	_onLockOnTargetStateChanged(aEvent_obj)
	{
		this.uiInfo.lockOnTarget = aEvent_obj.checked;
		this.emit(FireSettingsView.EVENT_ON_FIRE_SETTINGS_CHANGED);
	}

	_onAutoFireStateChanged(aEvent_obj)
	{
		this.uiInfo.autoFire = aEvent_obj.checked;
		this.emit(FireSettingsView.EVENT_ON_FIRE_SETTINGS_CHANGED);
		
	}

	_onTargetPriorityStateChanged(aEvent_obj)
	{
		this.uiInfo.targetPriority = aEvent_obj.state;

		this._updateTargetPriorityCaption();

		this.emit(FireSettingsView.EVENT_ON_FIRE_SETTINGS_CHANGED);
	}

	_onFireSpeedStateChanged(aEvent_obj)
	{
		this.uiInfo.fireSpeed = aEvent_obj.state;
		this.emit(FireSettingsView.EVENT_ON_FIRE_SETTINGS_CHANGED);
	}

	_activate()
	{
		if (APP.isMobile) return;

		this.on("pointerclick", this._onInsideClicked, this);
		window.addEventListener(this._eventName, this._fBindedDocClick_fn);
	}

	_deactivate()
	{
		if (APP.isMobile) return;

		this.off("pointerclick", this._onInsideClicked, this);
		window.removeEventListener(this._eventName, this._fBindedDocClick_fn);
	}

	_onInsideClicked()
	{
		this._fClickFlag_bln = true;
	}

	_onDocumentClicked(e)
	{
		if (this._fClickFlag_bln)
		{
			this._fClickFlag_bln = false;
			return;
		}

		this.emit(FireSettingsView.EVENT_ON_DEACTIVATE);
	}

	_onOkButtonClicked()
	{
		this.emit(FireSettingsView.EVENT_ON_DEACTIVATE);
	}

	get _eventName()
	{
		if (!!document.PointerEvent)
		{
			return "pointerup";
		}
		else
		{
			if (APP.isMobile)
			{
				return "touchend";
			}
			else
			{
				return "mouseup";
			}
		}
	}

	destroy()
	{
		this._deactivate();

		super.destroy();

		this._fBindedDocClick_fn = null;
		this._fClickFlag_bln = null;

		this._fLockOnTargetCheckBox_cb = null;
		this._fAutoFireCheckBox_cb = null;
		this._fTargetPriorityCheckBox_mcb = null;
		this._fFireSpeedCheckBox_mcb = null;

		this._targetPriorityCaptions_arr = null;
	}
}

export default FireSettingsView