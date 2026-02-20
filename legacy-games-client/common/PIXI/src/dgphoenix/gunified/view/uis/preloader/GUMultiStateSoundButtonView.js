import MultiStateButtonView from '../../../../unified/view/ui/MultiStateButtonView';
import Button from '../../../../unified/view/ui/Button';

class GUMultiStateSoundButtonView extends MultiStateButtonView
{
	static get EVENT_SOUND_ON_BUTTON_CLICKED() 		{return "onSoundOnButtonClicked";}
	static get EVENT_SOUND_OFF_BUTTON_CLICKED()		{return "onSoundOffButtonClicked";}

	static get STATE_SOUND_ON() 	{return 0;}
	static get STATE_SOUND_OFF() 	{return 1;}

	constructor(aOptDrawSeptum_bln = true, aOptBaseScale_obj = undefined) 
	{
		super(aOptDrawSeptum_bln, aOptBaseScale_obj);

		this._initSoundButtonView();
	}

	_initSoundButtonView()
	{
		this._getButtonStateView(GUMultiStateSoundButtonView.STATE_SOUND_ON);
		this._getButtonStateView(GUMultiStateSoundButtonView.STATE_SOUND_OFF);
	}

	_initButtonStateView(aStateId_int)
	{
		let lButtonState_btn;
		switch(aStateId_int)
		{
			case GUMultiStateSoundButtonView.STATE_SOUND_ON:
				lButtonState_btn = this.addChild(this.__provideOnButtonInstance());
				lButtonState_btn.on("pointerclick", this._onSoundOnBtnClicked, this);
				break;
			case GUMultiStateSoundButtonView.STATE_SOUND_OFF:
				lButtonState_btn = this.addChild(this.__provideOffButtonInstance());
				lButtonState_btn.on("pointerclick", this._onSoundOffBtnClicked, this);
				break;
		}
		return lButtonState_btn;
	}

	__provideOnButtonInstance()
	{
		let lButtonState_btn = new Button(this.__onButtonAssetName, undefined, true);

		return lButtonState_btn;
	}

	get __onButtonAssetName()
	{
		// must be overridden
		return undefined;
	}

	__provideOffButtonInstance()
	{
		let lButtonState_btn = this.addChild(new Button(this.__offButtonAssetName, undefined, true));

		return lButtonState_btn;
	}

	get __offButtonAssetName()
	{
		// must be overridden
		return undefined;
	}

	_onSoundOnBtnClicked(event)
	{
		this.emit(GUMultiStateSoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED);
	}

	_onSoundOffBtnClicked(event)
	{
		this.emit(GUMultiStateSoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED);
	}
}

export default GUMultiStateSoundButtonView;