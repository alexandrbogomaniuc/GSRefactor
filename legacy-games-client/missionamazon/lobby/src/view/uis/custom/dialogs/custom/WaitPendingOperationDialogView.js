import DialogView from '../DialogView';

class WaitPendingOperationDialogView extends DialogView
{
	constructor()
	{
		super();
	}

	showMessage()
	{
		this._baseContainer.visible = true;
		this._messageContainer.visible = true;
		this._buttonsContainer.visible = true;
		this._overlayContainer.visible = true;
	}
	
	hideMessage()
	{
		this._baseContainer.visible = false;
		this._messageContainer.visible = false;
		this._buttonsContainer.visible = false;
		this._overlayContainer.visible = false;
	}
}

export default WaitPendingOperationDialogView;