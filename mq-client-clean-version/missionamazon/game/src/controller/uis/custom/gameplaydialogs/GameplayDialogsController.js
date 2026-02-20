import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayDialogController from './GameplayDialogController';
import GameplayDialogsInfo from '../../../../model/uis/custom/gameplaydialogs/GameplayDialogsInfo';
import GameplayDialogsView from '../../../../view/uis/custom/gameplaydialogs/GameplayDialogsView';
import BattlegroundCountDownDialogController from './custom/BattlegroundCountDownDialogController';
import CafPrivateRoundCountDownDialogController from './custom/CafPrivateRoundCountDownDialogController';

class GameplayDialogsController extends SimpleUIController
{
	static get EVENT_DIALOG_ACTIVATED() {return DialogController.EVENT_DIALOG_ACTIVATED};
	static get EVENT_DIALOG_DEACTIVATED() {return DialogController.EVENT_DIALOG_DEACTIVATED};

	static _sortDialogsByPresentationPriority (dialog1, dialog2)
	{
		var firstDialogInfo = dialog1.info;
		var secondDialogInfo = dialog2.info;

		var firstDialogPriority = firstDialogInfo.priority;
		var secondDialogPriority = secondDialogInfo.priority;
		var lRet_num = secondDialogPriority - firstDialogPriority;
		if (!lRet_num)
		{
			var firstDialogActivationTime = firstDialogInfo.activationTime;
			var secondDialogActivationTime = secondDialogInfo.activationTime;

			lRet_num = firstDialogActivationTime - secondDialogActivationTime;
		}
		return lRet_num;
	}

	constructor(optInfo)
	{
		super(new GameplayDialogsInfo());

		this._dialogsControllers = null;
		this._fViewContainer_sprt = null;

		this._initDialogsController();
	}

	initView(viewContainer)
	{
		this._fViewContainer_sprt = viewContainer;

		let view = new GameplayDialogsView();
		this._fViewContainer_sprt.addChild(view);

		super.initView(view);
	}

	get viewContainer()
	{
		return this._fViewContainer_sprt;
	}

	destroy()
	{
		this._dialogsControllers = null;

		super.destroy();
	}

	get gameBattlegroundCountDownDialogController()
	{
		return this._gameBattlegroundCountDownDialogController;
	}

	get gameCafPrivateRoundCountDownDialogController()
	{
		return this._gameCafPrivateRoundCountDownDialogController;
	}

	_initDialogsController()
	{
		this._dialogsControllers = [];
	}

	__init ()
	{
		super.__init();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		var info = this.info;
		var dialogsAmount = info.dialogsCount;
		for (var i = 0; i < dialogsAmount; i++)
		{
			var notificaionController = this.__getDialogController(i);
			notificaionController.init();
		}
	}

	__initViewLevel ()
	{
		super.__initViewLevel();

		var view = this.__fView_uo;
		var dialogsAmount = this.info.dialogsCount;
		for (var i = 0; i < dialogsAmount; i++)
		{
			var dialogController = this.__getDialogController(i);
			if (dialogController.isViewLevelSelfInitializationMode)
			{
				if (!dialogController.hasView)
				{
					dialogController.initViewLevelSelfInitializationViewProvider(view);
				}
			}
			else
			{
				dialogController.initView(view.getDialogView(i));
			}
		}
	}

	__getDialogController (dialogId)
	{
		return this._dialogsControllers[dialogId] || this._initDialogController(dialogId);
	}

	_initDialogController (dialogId)
	{
		var dialogController = this.__generateDialogController(this.info.getDialogInfo(dialogId));
		this._dialogsControllers[dialogId] = dialogController;

		dialogController.on(GameplayDialogController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		dialogController.on(GameplayDialogController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);

		return dialogController;
	}

	__generateDialogController (dialogInfo)
	{
		var dialogController;
		var dialogId = dialogInfo.dialogId;
		var dialogInfo = dialogInfo;

		switch (dialogId)
		{
			case GameplayDialogsInfo.DIALOG_ID_BATTLEGROUND_COUNT_DOWN:
				dialogController = new BattlegroundCountDownDialogController(dialogInfo, this);
				break;
			case GameplayDialogsInfo.DIALOG_ID_CAF_PRIVATE_ROUND_COUNT_DOWN:
				dialogController = new CafPrivateRoundCountDownDialogController(dialogInfo, this);
				break;
			default:
				throw new Error(`Unsupported dialog id: ${dialogId}`);
		}

		return dialogController;
	}

	_onDialogActivated (aEvent_ue)
	{
		this._updateDialogForPresentationSettings();
		this.emit(aEvent_ue);
	}

	_onDialogDeactivated (aEvent_ue)
	{
		this._updateDialogForPresentationSettings();
		this.emit(aEvent_ue);
	}

	_updateDialogForPresentationSettings ()
	{
		var sortedActiveDialogs = this._getActiveDialogsWithPresentationPrioritySorting();
		var info = this.info;

		if (!sortedActiveDialogs)
		{
			info.dialogIdForPresentation = undefined;
		}
		else
		{
			info.dialogIdForPresentation = sortedActiveDialogs[0].info.dialogId;
		}
	}

	_getActiveDialogsWithPresentationPrioritySorting ()
	{
		var activeDialogs = null;
		var dialogsAmount = this.info.dialogsCount;
		for (var i = 0; i < dialogsAmount; i++)
		{
			var dialogController = this.__getDialogController(i);
			if (dialogController.info.isActive)
			{
				activeDialogs = activeDialogs || [];
				activeDialogs.push(dialogController);
			}
		}

		if (activeDialogs)
		{
			activeDialogs.sort(GameplayDialogsController._sortDialogsByPresentationPriority);
		}

		return activeDialogs;
	}

	get _gameBattlegroundCountDownDialogController()
	{
		return this.__getDialogController(GameplayDialogsInfo.DIALOG_ID_BATTLEGROUND_COUNT_DOWN);
	}

	get _gameCafPrivateRoundCountDownDialogController()
	{
		return this.__getDialogController(GameplayDialogsInfo.DIALOG_ID_CAF_PRIVATE_ROUND_COUNT_DOWN);
	}
}

export default GameplayDialogsController