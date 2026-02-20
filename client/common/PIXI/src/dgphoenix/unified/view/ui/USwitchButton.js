import { APP } from '../../controller/main/globals';
import I18 from '../../controller/translations/I18';
import Sprite from '../base/display/Sprite';

/**
 * Component that switches between 2 possible states.
 * Only one state can be active at the moment.
 * Both states are always visible, but switch between active/inactive views.
 * @class
 * @extends Sprite
 */
class USwitchButton extends Sprite
{
	static get EVENT_ON_STATE_CHANGED()			{ return "onSwitcherStateChanged"; }

	/**
	 * Switch between states.
	 * @param {boolean} aState_bln - Is ON state active or not.
	 */
	updateToggleState(aState_bln)
	{
		this._updateToggleState(aState_bln);
	}

	constructor(aProps_obj)
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

		this._parseProperties(aProps_obj);

		this._initButtons();
		this._initButtonsCaption();
	}

	_parseProperties(aProps_obj)
	{
		let lProps_obj = aProps_obj || {};

		this._fOnBlackBtnBaseAssetName_str = lProps_obj.onBlackBtnBaseAssetName || undefined;
		this._fOnYellowBtnBaseAssetName_str = lProps_obj.onYellowBtnBaseAssetName || undefined;
		this._fOnCaptionTAssetName_str = lProps_obj.onCaptionTAssetName || undefined;
		this._fOnBtnSoundName_str = lProps_obj.onBtnSoundName || undefined;
		this._fOnBtnPosition_obj = lProps_obj.onBtnPosition || {x: 50, y: 0};
		this._fOnBtnScale_obj = lProps_obj.onBtnScale || {x: 1, y: 1};

		this._fOffBlackBtnBaseAssetName_str = lProps_obj.offBlackBtnBaseAssetName || undefined;
		this._fOffYellowBtnBaseAssetName_str = lProps_obj.offYellowBtnBaseAssetName || undefined;
		this._fOffCaptionTAssetName_str = lProps_obj.offCaptionTAssetName || undefined;
		this._fOffBtnSoundName_str = lProps_obj.offBtnSoundName || undefined;
		this._fOffBtnPosition_obj = lProps_obj.offBtnPosition || {x: -50, y: 0};
		this._fOffBtnScale_obj = lProps_obj.offBtnScale || {x: -1, y: 1};
	}

	_initButtons()
	{
		this._fBtnContainer_spr = this.addChild(new Sprite());

		this._fOffBtn_spr = this._fBtnContainer_spr.addChild(new Sprite());
		this._fOffBtn_spr.position.set(this._fOffBtnPosition_obj.x, this._fOffBtnPosition_obj.y);
		this._fOffBtn_spr.scale.set(this._fOffBtnScale_obj.x, this._fOffBtnScale_obj.y);
		this._fOffBtn_spr.interactive = true;
		this._fOffBtn_spr.buttonMode = true;
		this._fOffBtn_spr.on("pointerclick", this._onOffBtnClick, this);

		this._fOnBtn_spr = this._fBtnContainer_spr.addChild(new Sprite());
		this._fOnBtn_spr.position.set(this._fOnBtnPosition_obj.x, this._fOnBtnPosition_obj.y);
		this._fOnBtn_spr.scale.set(this._fOnBtnScale_obj.x, this._fOnBtnScale_obj.y);
		this._fOnBtn_spr.interactive = true;
		this._fOnBtn_spr.buttonMode = true;
		this._fOnBtn_spr.on("pointerclick", this._onOnBtnClick, this);

		this._fOnBlackBtn_spr = APP.library.getSprite(this._fOnBlackBtnBaseAssetName_str);
		this._fOnYellowBtn_spr = APP.library.getSprite(this._fOnYellowBtnBaseAssetName_str);

		this._fOffBlackBtn_spr = APP.library.getSprite(this._fOffBlackBtnBaseAssetName_str);
		this._fOffYellowBtn_spr = APP.library.getSprite(this._fOffYellowBtnBaseAssetName_str);
	}

	_initButtonsCaption()
	{
		this._fToggleLabelOff_cta = this._fOffBtn_spr.addChild(I18.generateNewCTranslatableAsset(this._fOffCaptionTAssetName_str));
		this._fToggleLabelOn_cta = this._fOnBtn_spr.addChild(I18.generateNewCTranslatableAsset(this._fOnCaptionTAssetName_str));

		this._fToggleLabelOn_cta.scale.x = -1;
	}

	_onOffBtnClick()
	{
		if (this._fCurrentState_bln == false) return;

		if (this._fOffBtnSoundName_str !== undefined && APP.soundsController && APP.soundsController.play) // soundsController should be removed from here
		{
			APP.soundsController.play(this._fOffBtnSoundName_str);
		}

		this._fCurrentState_bln = false;
		this.emit(USwitchButton.EVENT_ON_STATE_CHANGED, {state: this._fCurrentState_bln});
		this._updateToggleState(this._fCurrentState_bln);
	}

	_onOnBtnClick()
	{
		if (this._fCurrentState_bln == true) return;

		if (this._fOnBtnSoundName_str !== undefined && APP.soundsController && APP.soundsController.play) // soundsController should be removed from here
		{
			APP.soundsController.play(this._fOnBtnSoundName_str);
		}

		this._fCurrentState_bln = true;
		this.emit(USwitchButton.EVENT_ON_STATE_CHANGED, {state: this._fCurrentState_bln});
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

	/**
	 * Destroy instance.
	 */
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

export default USwitchButton;