import MultiStateButtonView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/MultiStateButtonView';
import Button from '../../../../ui/GameButton';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class GamePreloaderSoundButtonView extends MultiStateButtonView
{
	static get EVENT_SOUND_ON_BUTTON_CLICKED() 		{return "onSoundOnButtonClicked";}
	static get EVENT_SOUND_OFF_BUTTON_CLICKED()		{return "onSoundOffButtonClicked";}

	static get STATE_SOUND_ON() 	{return 0;}
	static get STATE_SOUND_OFF() 	{return 1;}

	constructor() 
	{
		super();

		this._initSoundButtonView();
	}

	_initSoundButtonView()
	{
		this._getButtonStateView(GamePreloaderSoundButtonView.STATE_SOUND_ON);
		this._getButtonStateView(GamePreloaderSoundButtonView.STATE_SOUND_OFF);
	}

	_initButtonStateView(aStateId_int)
	{
		let lButtonState_btn
		switch(aStateId_int)
		{
			case GamePreloaderSoundButtonView.STATE_SOUND_ON:
				lButtonState_btn = this.addChild(new Button("common_btn_sound_on", undefined, true));
				lButtonState_btn.position.set(0.5, -0.5);
				lButtonState_btn.on("pointerclick", this._onSoundOnBtnClicked, this);
				break;
			case GamePreloaderSoundButtonView.STATE_SOUND_OFF:
				lButtonState_btn = this.addChild(new Button("common_btn_sound_off", undefined, true));
				lButtonState_btn.position.set(0.5, -0.5);
				lButtonState_btn.on("pointerclick", this._onSoundOffBtnClicked, this);
				break;
		}
		return lButtonState_btn;
	}

	_onSoundOnBtnClicked(event)
	{
		this.emit(GamePreloaderSoundButtonView.EVENT_SOUND_ON_BUTTON_CLICKED);
	}

	_onSoundOffBtnClicked(event)
	{
		this.emit(GamePreloaderSoundButtonView.EVENT_SOUND_OFF_BUTTON_CLICKED);
	}
}

export default GamePreloaderSoundButtonView;