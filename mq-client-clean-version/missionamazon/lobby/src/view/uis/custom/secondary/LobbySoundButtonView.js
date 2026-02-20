import MultiStateButtonView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/MultiStateButtonView';
import SelectableButton from '../commonpanel/buttons/SelectableButton';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class LobbySoundButtonView extends MultiStateButtonView
{
	static get EVENT_SOUND_ON_BUTTON_CLICKED() 		{return "onSoundOnButtonClicked";}
	static get EVENT_SOUND_OFF_BUTTON_CLICKED()		{return "onSoundOffButtonClicked";}

	static get STATE_SOUND_ON() 	{return 0;}
	static get STATE_SOUND_OFF() 	{return 1;}

	constructor(aOptDrawSeptum_bln = true, aOptBaseScale_obj=undefined, aOptCaptionAsset_str)
	{
		super();
		this._fDrawSeptum_bln = aOptDrawSeptum_bln;

		this._fBaseScale_obj = aOptBaseScale_obj || (APP.isMobile ? 1.4 : 1);

		this._initSoundButtonView();
		this._initCaption(aOptCaptionAsset_str);
	}

	_initCaption(aOptCaptionAsset_str)
	{
		if (!aOptCaptionAsset_str) return;

		let lCaption_ta = I18.generateNewCTranslatableAsset(aOptCaptionAsset_str);
		this.addChild(lCaption_ta);
		lCaption_ta.position.set(0, 22);
	}

	_initSoundButtonView()
	{
		this._getButtonStateView(LobbySoundButtonView.STATE_SOUND_ON);
		this._getButtonStateView(LobbySoundButtonView.STATE_SOUND_OFF);
	}

	_initButtonStateView(aStateId_int)
	{
		let lButtonState_btn = null;
		let lScale_num = this._fBaseScale_obj;
		switch(aStateId_int)
		{
			case LobbySoundButtonView.STATE_SOUND_ON:
				lButtonState_btn = this.addChild(new SelectableButton("common_btn_sound_on", undefined, this._fDrawSeptum_bln, lScale_num));
				lButtonState_btn.on("pointerclick", this._onSoundOnBtnClicked, this);
				break;
			case LobbySoundButtonView.STATE_SOUND_OFF:
				lButtonState_btn = this.addChild(new SelectableButton("common_btn_sound_off", undefined, this._fDrawSeptum_bln, lScale_num));
				lButtonState_btn.on("pointerclick", this._onSoundOffBtnClicked, this);
				break;
		}
		return lButtonState_btn;
	}

	_onSoundOnBtnClicked(event)
	{
		this.emit(LobbySoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED);
	}

	_onSoundOffBtnClicked(event)
	{
		this.emit(LobbySoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED);
	}
}

export default LobbySoundButtonView;