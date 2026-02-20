import GUDialogController from '../../GUDialogController';
import GUGameBaseDialogInfo from '../../../../../../model/uis/custom/dialogs/custom/game/GUGameBaseDialogInfo';
import { APP } from '../../../../../../../unified/controller/main/globals';
import GUSLobbyStateController from '../../../../../state/GUSLobbyStateController';

class GUGameBaseDialogController extends GUDialogController
{
	static get EVENT_DIALOG_ACTIVATED()				{ return GUDialogController.EVENT_DIALOG_ACTIVATED }
	static get EVENT_DIALOG_DEACTIVATED()			{ return GUDialogController.EVENT_DIALOG_DEACTIVATED }
	static get EVENT_DIALOG_PRESENTED()				{ return GUDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()		{ return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii || new GUGameBaseDialogInfo(), undefined, parentController);

		this._initGameBaseDialogController();
	}

	_initGameBaseDialogController()
	{
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._lobbyStateController.on(GUSLobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyScreenVisibleChange, this);
	}

	get _lobbyStateController()
	{
		return APP.lobbyStateController;
	}

	__activateDialog()
	{
		if (
			!this.info.isActivationOverHiddenGameAvailable
			&& this._lobbyStateController.info.lobbyScreenVisible
		)
		{
			this.info.isSwitchToGameAwaiting = true;

			return;
		}

		this.info.isSwitchToGameAwaiting = false;
		super.__activateDialog();
	}

	_onLobbyScreenVisibleChange()
	{
		if (this._lobbyStateController.info.lobbyScreenVisible)
		{
			if (this.info.isActive)
			{
				this.__deactivateDialog();
				this.__activateDialog();
			}
		}
		else
		{
			if (this.info.isSwitchToGameAwaiting)
			{
				this.__activateDialog();
			}
		}
	}

	__deactivateDialog()
	{
		this.info.isSwitchToGameAwaiting = false;

		super.__deactivateDialog();
	}

}

export default GUGameBaseDialogController