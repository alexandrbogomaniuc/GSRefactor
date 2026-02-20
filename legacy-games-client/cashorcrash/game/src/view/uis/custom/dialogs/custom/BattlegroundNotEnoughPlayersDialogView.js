import DialogView from '../DialogView';
import DialogButton from '../DialogButton';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class BattlegroundNotEnoughPlayersDialogView extends DialogView
{

	constructor()
	{
		super();
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
		msgContainer.position.set(-2, -30);

		let msgContainer2 = this._messageContainer2 = this.addChild(new Sprite());
		msgContainer2.zIndex = 3;
		msgContainer2.position.set(-2, -30);
		
		let btnsContainer = this._buttonsContainer = this.addChild(new Sprite());
		btnsContainer.zIndex = 4;
		btnsContainer.position.set(-3, 37);

		let overlayContainer = this._overlayContainer = this.addChild(new Sprite());
		overlayContainer.zIndex = 5;
		overlayContainer.position.set(0, 0);
	}

	_alignButtonView (aIntButtonId_int, aButtonsCount_int = 2, aButtonView_domdb)
	{
		var lXOffset_num = 0;
		var lYOffset_num = 0;
		var lDirection_num = aIntButtonId_int > 0 ? 1 : -1;
		lYOffset_num = 20*lDirection_num;
		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}

	setAdditionalMessage (messageId)
	{
		this._setAdditionalMessage(messageId)
	}

	_setAdditionalMessage (messageId)
	{
		this._messageContainer2.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset(messageId);

		this._messageContainer2.addChild(msg);
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		return new DialogButton("preloader/dialogs/btn_base_long");
	}
}

export default BattlegroundNotEnoughPlayersDialogView;