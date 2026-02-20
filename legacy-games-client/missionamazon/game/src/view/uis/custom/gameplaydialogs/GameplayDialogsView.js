import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import GameplayDialogsInfo from '../../../../model/uis/custom/gameplaydialogs/GameplayDialogsInfo';
import BattlegroundCountDownDialogView from './custom/BattlegroundCountDownDialogView';
import CafPrivateRoundCountDownDialogView from './custom/CafPrivateRoundCountDownDialogView';

class GameplayDialogsView extends SimpleUIView
{
	static isDialogViewBlurForbidden(aDialogId_int)
	{
		switch(aDialogId_int)
		{
			case GameplayDialogsInfo.DIALOG_ID_BATTLEGROUND_COUNT_DOWN:
				return true;
		}

		return false;
	}

	constructor()
	{
		super();

		this.dialogsViews = null;

		this._initDialogsView();
	}

	destroy()
	{
		this.dialogsViews = null;

		super.destroy();
	}

	get battlegroundCountDownDialogView()
	{
		return this._battlegroundCountDownDialogView;
	}

	get cafPrivateRoomCountDownDialogView()
	{
		return this._cafPrivateRoomCountDownDialogView;
	}

	getDialogView (dialogId)
	{
		return this.__getDialogView(dialogId);
	}

	_initDialogsView()
	{
		this.dialogsViews = [];
	}

	__getDialogView (dialogId)
	{
		return this.dialogsViews[dialogId] || this._initDialogView(dialogId);
	}

	_initDialogView (dialogId)
	{
		var dialogView = this.__generateDialogView(dialogId);

		this.dialogsViews[dialogId] = dialogView;
		this.addChild(dialogView);

		return dialogView;
	}

	__generateDialogView (dialogId)
	{
		var dialogView;
		switch (dialogId)
		{
			case GameplayDialogsInfo.DIALOG_ID_BATTLEGROUND_COUNT_DOWN:
				dialogView = new BattlegroundCountDownDialogView();
				break;
			case GameplayDialogsInfo.DIALOG_ID_CAF_PRIVATE_ROUND_COUNT_DOWN:
				dialogView = new CafPrivateRoundCountDownDialogView();
				break;
			default:
				throw new Error (`Unsupported dialog id: ${dialogId}`);
		}
		return dialogView;
	}

	get _battlegroundCountDownDialogView()
	{
		return this.__getDialogView(GameplayDialogsInfo.DIALOG_ID_BATTLEGROUND_COUNT_DOWN);
	}

	get _cafPrivateRoomCountDownDialogView()
	{
		return this.__getDialogView(GameplayDialogsInfo.DIALOG_ID_CAF_PRIVATE_ROUND_COUNT_DOWN);
	}
}

export default GameplayDialogsView;