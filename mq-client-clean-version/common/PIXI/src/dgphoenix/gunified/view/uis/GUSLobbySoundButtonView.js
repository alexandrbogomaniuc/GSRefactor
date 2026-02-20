import GUMultiStateSoundButtonView from '../uis/preloader/GUMultiStateSoundButtonView';
import I18 from '../../../unified/controller/translations/I18';
import GUSLobbySelectableButton from './commonpanel/buttons/GUSLobbySelectableButton';

class GUSLobbySoundButtonView extends GUMultiStateSoundButtonView
{
	constructor(aOptDrawSeptum_bln = true, aOptBaseScale_obj = undefined, aOptCaptionAsset_str)
	{
		super(aOptDrawSeptum_bln, aOptBaseScale_obj);
		
		this._initCaption(aOptCaptionAsset_str);
	}

	_initCaption(aOptCaptionAsset_str)
	{
		if (!aOptCaptionAsset_str) return;

		let lCaption_ta = I18.generateNewCTranslatableAsset(aOptCaptionAsset_str);
		this.addChild(lCaption_ta);

		let lCaptionPos_p = this.__captionPosition;
		lCaption_ta.position.set(lCaptionPos_p.x, lCaptionPos_p.y);
	}

	get __captionPosition()
	{
		return new PIXI.Point(0, 22);
	}

	__provideOnButtonInstance()
	{
		let lButtonState_btn = new GUSLobbySelectableButton(this.__onButtonAssetName, undefined, this._fDrawSeptum_bln, this._fBaseScale_obj);

		return lButtonState_btn;
	}

	__provideOffButtonInstance()
	{
		let lButtonState_btn = new GUSLobbySelectableButton(this.__offButtonAssetName, undefined, this._fDrawSeptum_bln, this._fBaseScale_obj);

		return lButtonState_btn;
	}

	get __onButtonAssetName()
	{
		return "common_btn_sound_on";
	}

	get __offButtonAssetName()
	{
		return "common_btn_sound_off";
	}
}

export default GUSLobbySoundButtonView;