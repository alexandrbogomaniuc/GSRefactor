import GUSLobbyGameDialogButton from './GUSLobbyGameDialogButton';

class GUSLobbyNonActiveGameDialogButton extends GUSLobbyGameDialogButton
{
	constructor(aBaseAssetActiveName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType)
	{
		super(aBaseAssetActiveName_str, aBaseAssetActiveName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType);
		this.handleUp();
	}

	_getBtnTextActiveStyle()
	{
		return this._getBtnTextDectiveStyle();
	}

}

export default GUSLobbyNonActiveGameDialogButton