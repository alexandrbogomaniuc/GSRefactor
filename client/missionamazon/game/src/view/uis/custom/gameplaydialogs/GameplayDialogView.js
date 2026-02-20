import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayDialogButton from './GameplayDialogButton';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class GameplayDialogView extends SimpleUIView
{
	static get EVENT_ON_OK_BTN_CLICKED () { return "onOkButtonClicked" };
	static get EVENT_ON_CANCEL_BTN_CLICKED () { return "onCancelButtonClicked" };
	static get EVENT_ON_CUSTOM_BTN_CLICKED () { return "onCustomButtonClicked" };
	static get EVENT_ON_BTN_CLICKED () { return "onButtonClicked" };

	constructor()
	{
		super();

		this._bgContainer = null;
		this._baseContainer = null;
		this._messageContainer = null;
		this._buttonsContainer = null;
		this._buttons = [];
		this._buttonsCount = undefined;

		this._initDialogView();
	}

	setMessage(messageId)
	{
		this._setMessage(messageId);
	}

	setEmptyMode ()
	{
		this._setButtonsCount(0);
	}

	setOkMode ()
	{
		this._setButtonsCount(1);
		this._getButtonView(0).buttonId = GameplayDialogButton.BUTTON_ID_OK;
	}

	setCancelMode ()
	{
		this._setButtonsCount(1);
		this._getButtonView(0).buttonId = GameplayDialogButton.BUTTON_ID_CANCEL;
	}

	setOkCancelMode ()
	{
		this._setButtonsCount(2);
		this._getButtonView(0).buttonId = GameplayDialogButton.BUTTON_ID_OK;
		this._getButtonView(1).buttonId = GameplayDialogButton.BUTTON_ID_CANCEL;
	}

	setOkCustomMode ()
	{
		this._setButtonsCount(2);
		this._getButtonView(0).buttonId = GameplayDialogButton.BUTTON_ID_OK;
		this._getButtonView(1).buttonId = GameplayDialogButton.BUTTON_ID_CUSTOM;
	}

	setOkCancelCustomMode ()
	{
		this._setButtonsCount(3);
		this._getButtonView(0).buttonId = GameplayDialogButton.BUTTON_ID_OK;
		this._getButtonView(1).buttonId = GameplayDialogButton.BUTTON_ID_CUSTOM;
		this._getButtonView(2).buttonId = GameplayDialogButton.BUTTON_ID_CANCEL;
	}

	setCustomMode()
	{
		this._setButtonsCount(1);
		this._getButtonView(0).buttonId = GameplayDialogButton.BUTTON_ID_CUSTOM;
	}

	get okButton()
	{
		for (let i=0; i<this._buttonsCount; i++)	
		{
			let button = this._getButtonView(i);
			if (button.isOkButton)
			{
				return button;
			}
		}
		return null;
	}

	get cancelButton()
	{
		for (let i=0; i<this._buttonsCount; i++)	
		{
			let button = this._getButtonView(i);
			if (button.isCancelButton)
			{
				return button;
			}
		}
		return null;
	}

	get customButton()
	{
		for (let i=0; i<this._buttonsCount; i++)	
		{
			let button = this._getButtonView(i);
			if (button.isCustomButton)
			{
				return button;
			}
		}
		return null;
	}

	_initDialogView()
	{
		this._addContainers();

		this._addGameLockView();
		this._addDialogBase();

		this._addButtons();		
	}

	_addContainers()
	{
		let bgContainer = this._bgContainer = this.addChild(new Sprite());
		bgContainer.zIndex = 0;
		bgContainer.interactive = true;
		bgContainer.buttonMode = false;

		let baseContainer = this._baseContainer = this.addChild(new Sprite());
		baseContainer.zIndex = 1;

		let msgContainer = this._messageContainer = this.addChild(new Sprite());
		msgContainer.zIndex = 2;
		msgContainer.position.set(-2, -3);
		
		let btnsContainer = this._buttonsContainer = this.addChild(new Sprite());
		btnsContainer.zIndex = 3;
		btnsContainer.position.set(0, 67);

		let overlayContainer = this._overlayContainer = this.addChild(new Sprite());
		overlayContainer.zIndex = 4;
		overlayContainer.position.set(0, 0);
	}

	_addGameLockView()
	{
		let bgContainer = this._bgContainer;

		let transparentBack = new PIXI.Graphics();
		transparentBack.beginFill(0x000000, 0.01);
		transparentBack.drawRect(-480, -270, 960, 540);
		transparentBack.endFill();

		if (APP.isMobile)
		{
			let lBlackBack_gr = new PIXI.Graphics();
			lBlackBack_gr.beginFill(0x000000, 0.5);
			lBlackBack_gr.drawRect(-480, -270, 960, 540);
			lBlackBack_gr.endFill();
			bgContainer.addChildAt(lBlackBack_gr, 0);
		}

		bgContainer.addChild(transparentBack);
	}

	_addDialogBase()
	{
		let baseContainer = this._baseContainer;
		let lBase_spr = APP.library.getSprite("preloader/dialogs/base");
		lBase_spr.scale.set(1.1);
		lBase_spr.position.set(-3, 0);
		baseContainer.addChild(lBase_spr);
	}

	//BUTTONS...
	_addButtons()
	{
		this._buttons = [];

		var buttonsCount = this._supportedButtonsCount;
		for (var i = 0; i < buttonsCount; i++)
		{
			var buttonView = this._getButtonView(i);
			buttonView.on("pointerclick", this._onButtonClicked, this);
		}
	}

	_onButtonClicked (event)
	{
		var button = event.target;
		
		if (button.isOkButton)
		{
			this.emit(GameplayDialogView.EVENT_ON_OK_BTN_CLICKED);
		}
		else if (button.isCancelButton)
		{
			this.emit(GameplayDialogView.EVENT_ON_CANCEL_BTN_CLICKED);
		}
		else if (button.isCustomButton)
		{
			this.emit(GameplayDialogView.EVENT_ON_CUSTOM_BTN_CLICKED);
		}

		this.emit(GameplayDialogView.EVENT_ON_BTN_CLICKED);
	}

	_getButtonView (aIntId_int)
	{
		return this._buttons[aIntId_int] || this._initButtonView(aIntId_int);
	}

	_initButtonView (aIntId_int)
	{
		var l_domdb = this.__retreiveDialogButtonViewInstance(aIntId_int);

		this._buttons[aIntId_int] = l_domdb;
		
		this._alignButtonView(aIntId_int, this._supportedButtonsCount, l_domdb);

		this._buttonsContainer.addChild(l_domdb);

		return l_domdb;
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		return new GameplayDialogButton();
	}

	_alignButtonView (aIntButtonId_int, aButtonsCount_int, aButtonView_domdb)
	{
		var lXOffset_num = 0;
		var lYOffset_num = 0;
		
		if (aButtonsCount_int === 1)
		{
			lXOffset_num = 0;
		}
		else if (aButtonsCount_int === 2)
		{
			var lDirection_num = aIntButtonId_int > 0 ? 1 : -1;
			lXOffset_num = 70*lDirection_num;
		}
		else if (aButtonsCount_int === 3)
		{
			var lDirection_num = aIntButtonId_int == 0 ? -1 : (aIntButtonId_int == 1 ? 0 : 1);
			lXOffset_num = 95*lDirection_num;
			lXOffset_num -= 2;
		}
		
		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}

	get _supportedButtonsCount ()
	{
		return 3;
	}

	_setButtonsCount (aCount_int)
	{
		if (aCount_int > this._supportedButtonsCount)
		{
			throw new Error(`Buttons count argument is greater than supported buttons count: ${aCount_int}`);
		}
		var lSupportedButtonsCount_int = this._supportedButtonsCount;
		for (var i = 0; i < lSupportedButtonsCount_int; i++)
		{
			var lButtonView_domdb = this._getButtonView(i);
			if (i < aCount_int)
			{
				lButtonView_domdb.visible = true;
				this._alignButtonView(i, aCount_int, lButtonView_domdb);
			}
			else
			{
				lButtonView_domdb.visible = false;
			}
		}
		this._buttonsCount = aCount_int;
		
	}
	
	_getButtonsCount ()
	{
		return this._buttonsCount;
	}
	//...BUTTONS

	//MESSAGE...
	_setMessage(messageId)
	{
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset(messageId);

		this._messageContainer.addChild(msg);
	}
	//...MESSAGE

	_lockDialog()
	{
		this._bgContainer.zIndex = 100;
	}

	_unlockDialog()
	{
		this._bgContainer.zIndex = 0;
	}

}

export default GameplayDialogView;