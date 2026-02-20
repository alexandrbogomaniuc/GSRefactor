import GUSettingsScreenView from './GUSettingsScreenView';
import USwitchButton from '../../../../../unified/view/ui/USwitchButton';
import { APP } from '../../../../../unified/controller/main/globals';
import Sprite from '../../../../../unified/view/base/display/Sprite';
import TextField, { DEFAULT_SYSTEM_FONT } from '../../../../../unified/view/base/display/TextField';

class GUSLobbySettingsScreenView extends GUSettingsScreenView
{
	static get EVENT_ON_SETTINGS_TOOLTIPS_STATE_CHANGED()		{ return "onTooltipsStateChanged"; }
	static get EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED()		{ return "onTutorialStateChanged"; }
	
	updateTooltipsToggleState(aState_bln)
	{
		this._updateTooltipsToggleState(aState_bln);
	}

	updateTutorialToggleState(aState_bln)
	{
		this._updateTutorialToggleState(aState_bln);
	}

	constructor()
	{
		super();

		this._fTutorialCaption_ta = null;
		this._fShowTutorialToggle_sb = null;

		this._fTooltipsCaption_ta = null;
		this._fTooltipsToggle_tg = null;

		this._fTimerAsset_ta = null;
		this._fTimer_tf = null;
	}

	_init()
	{
		super._init();

		if (!!APP.isBattlegroundGame)
		{
			this._addTimerIfRequired();
		}

		if (APP.isTutorialSupported)
		{
			this._initTutorialToggle();
		}
		else
		{
			this._initTooltipToggle();
		}
	}

	_initTooltipToggle()
	{
		this._fTooltipsToggle_tg = this.addChild(this.__provideTooltipsToggleInstance());
		this._fTooltipsToggle_tg.on(USwitchButton.EVENT_ON_STATE_CHANGED, this._onTooltipsStateChanged, this);
	}

	__provideTooltipsToggleInstance()
	{
		let lProps_obj = {};

		let l_sb = new USwitchButton(lProps_obj);
		return l_sb;
	}

	_updateTooltipsToggleState(aState_bln)
	{
		this._fTooltipsToggle_tg.updateToggleState(aState_bln);

		if (this._fTooltipsCaption_ta) this._fTooltipsCaption_ta.visible = true;
		if (this._fTooltipsToggle_tg) this._fTooltipsToggle_tg.visible = true;
	}

	_initTutorialToggle()
	{
		this._fShowTutorialToggle_sb = this.addChild(this.__provideTutorialToggleInstance());
		this._fShowTutorialToggle_sb.on(USwitchButton.EVENT_ON_STATE_CHANGED, this._onTutorialStateChanged, this);
	}

	__provideTutorialToggleInstance()
	{
		let lProps_obj = {};

		let l_sb = new USwitchButton(lProps_obj);
		return l_sb;
	}

	_updateTutorialToggleState(aState_bln)
	{
		this._fShowTutorialToggle_sb.updateToggleState(aState_bln);

		if (this._fTutorialCaption_ta) this._fTutorialCaption_ta.visible = true;
		if (this._fShowTutorialToggle_sb) this._fShowTutorialToggle_sb.visible = true;
	}

	_addTimerIfRequired()
	{
		this._fTimerAsset_ta = this.addChild(this.__provideTimerCaptionInstance());
		
		this._fTimer_tf = this.addChild(this.__provideTimerValueInstance());
		

		this.updateTimeIndicator();
	}

	__provideTimerCaptionInstance()
	{
		return new Sprite;
	}

	__provideTimerValueInstance()
	{
		return new TextField(this._timerStyle);
	}

	updateTimeIndicator(aFormatedValue_str = "00:00")
	{
		if(this._fTimerAsset_ta && this._fTimer_tf)
		{
			if (APP.battlegroundController.isSecondaryScreenTimerRequired)
			{
				this._fTimerAsset_ta.visible = true;
				this._fTimer_tf.visible = true;
				this._fTimer_tf.text = aFormatedValue_str;
			}
			else
			{
				this.hideTimeIndicator();
			}
		}
	}

	hideTimeIndicator()
	{
		if(this._fTimerAsset_ta && this._fTimer_tf)
		{
			this._fTimerAsset_ta.visible = false;
			this._fTimer_tf.visible = false;
		}
	}

	get _timerStyle()
	{
		let lStyle_obj = {
			fontFamily: DEFAULT_SYSTEM_FONT,
			fontSize: 16,
			align: "left",
			fill: 0xffffff
		};

		return lStyle_obj;
	}

	_onTooltipsStateChanged(aEvent_obj)
	{
		this.emit(GUSLobbySettingsScreenView.EVENT_ON_SETTINGS_TOOLTIPS_STATE_CHANGED, { value: aEvent_obj.state });
	}

	_onTutorialStateChanged(aEvent_obj)
	{
		this.emit(GUSLobbySettingsScreenView.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, {value: aEvent_obj.state});
	}

	destroy()
	{
		super.destroy();

		this._fTooltipsCaption_ta = null;
		this._fTooltipsToggle_tg = null;
	}

}

export default GUSLobbySettingsScreenView;