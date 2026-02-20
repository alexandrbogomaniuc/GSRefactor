import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class SwitchButton extends Sprite
{
	static get EVENT_ON_STATE_CHANGED()					{ return "onSwitcherStateChanged"; }
	static get EVENT_ON_TOGGLE_BTN_CLICK()				{ return "EVENT_ON_TOGGLE_BTN_CLICK"; }

	updateToggleState(aState_bln)
	{
		this._updateToggleState(aState_bln);
	}

	constructor()
	{
		super();

		this._fCurrentState_bln = null;

		this._fToggleLabelOff_cta = null;
		this._fToggleLabelOn_cta = null;

		this._fOffBtn_spr = null;
		this._fOnBtn_spr = null;
		this._fOffBlackBtn_spr = null;
		this._fOffYellowBtn_spr = null;
		this._fOnBlackBtn_spr = null;
		this._fOnYellowBtn_spr = null;

		this._fBtnContainer_spr = null;

		this._initButtons();
		this._initButtonsCaption();
	}

	_initButtons()
	{
		this._fBtnContainer_spr = this.addChild(new Sprite());

		this._fOffBtn_spr = this._fBtnContainer_spr.addChild(new Sprite());
		this._fOffBtn_spr.position.set(51.5, 0);
		this._fOffBtn_spr.interactive = true;
		this._fOffBtn_spr.buttonMode = true;
		this._fOffBtn_spr.on("pointerclick", this._onOffBtnClick, this);

		this._fOnBtn_spr = this._fBtnContainer_spr.addChild(new Sprite());
		this._fOnBtn_spr.position.set(-51.5, 0);
		this._fOnBtn_spr.interactive = true;
		this._fOnBtn_spr.buttonMode = true;
		this._fOnBtn_spr.on("pointerclick", this._onOnBtnClick, this);
		this._fOnBtn_spr.scale.x = -1;

		this._fOnBlackBtn_spr = APP.library.getSprite("settings/toggle_button/off_btn");
		this._fOnYellowBtn_spr = APP.library.getSprite("settings/toggle_button/on_btn");

		this._fOffBlackBtn_spr = APP.library.getSprite("settings/toggle_button/off_btn");
		this._fOffYellowBtn_spr = APP.library.getSprite("settings/toggle_button/on_btn");
	}

	_initButtonsCaption()
	{
		this._fToggleLabelOff_cta = this._fOffBtn_spr.addChild(I18.generateNewCTranslatableAsset("TASettingsTutorialLabelOff"));
		this._fToggleLabelOn_cta = this._fOnBtn_spr.addChild(I18.generateNewCTranslatableAsset("TASettingsTutorialLabelOn"));

		this._fToggleLabelOn_cta.scale.x = -1;
	}

	_onOffBtnClick()
	{
		if (this._fCurrentState_bln == false) return;

		this.emit(SwitchButton.EVENT_ON_TOGGLE_BTN_CLICK);

		this._fCurrentState_bln = false;
		this.emit(SwitchButton.EVENT_ON_STATE_CHANGED, {state: this._fCurrentState_bln});
		this._updateToggleState(this._fCurrentState_bln);
	}

	_onOnBtnClick()
	{
		if (this._fCurrentState_bln == true) return;

		this.emit(SwitchButton.EVENT_ON_TOGGLE_BTN_CLICK);

		this._fCurrentState_bln = true;
		this.emit(SwitchButton.EVENT_ON_STATE_CHANGED, {state: this._fCurrentState_bln});
		this._updateToggleState(this._fCurrentState_bln);
	}

	_updateToggleState(aState_bln)
	{
		this._fCurrentState_bln = aState_bln;

		if (this._fCurrentState_bln)
		{
			this._fOffBtn_spr.texture = this._fOffBlackBtn_spr.texture;
			this._fOnBtn_spr.texture = this._fOnYellowBtn_spr.texture;

			this._fToggleLabelOff_cta.assetContent.textFormat = this._getBtnTextDectiveStyle();
			this._fToggleLabelOn_cta.assetContent.textFormat = this._getBtnTextActiveStyle();

			this._fOnBtn_spr.interactive = false;
			this._fOnBtn_spr.buttonMode = false;

			this._fOffBtn_spr.interactive = true;
			this._fOffBtn_spr.buttonMode = true;
		}
		else
		{
			this._fOffBtn_spr.texture = this._fOffYellowBtn_spr.texture;
			this._fOnBtn_spr.texture = this._fOnBlackBtn_spr.texture;

			this._fToggleLabelOff_cta.assetContent.textFormat = this._getBtnTextActiveStyle();
			this._fToggleLabelOn_cta.assetContent.textFormat = this._getBtnTextDectiveStyle();

			this._fOnBtn_spr.interactive = true;
			this._fOnBtn_spr.buttonMode = true;

			this._fOffBtn_spr.interactive = false;
			this._fOffBtn_spr.buttonMode = false;
		}
	}

	_getBtnTextActiveStyle()
	{
		return {
			fill: 0x000000,
			dropShadow: true,
			dropShadowColor: 0xffffff,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 0.5,
			dropShadowAlpha: 0.5
		};
	}

	_getBtnTextDectiveStyle()
	{
		return {
			fill: 0xffffff,
			dropShadow: false
		};
	}

	destroy()
	{
		super.destroy();

		this._fCurrentState_bln = null;

		this._fOffBtn_spr = null;
		this._fOnBtn_spr = null;
		this._fOffBlackBtn_spr = null;
		this._fOffYellowBtn_spr = null;
		this._fOnBlackBtn_spr = null;
		this._fOnYellowBtn_spr = null;
		this._fBtnContainer_spr = null;

		this._fToggleLabelOff_cta = null;
		this._fToggleLabelOn_cta = null;
	}
} 

export default SwitchButton;