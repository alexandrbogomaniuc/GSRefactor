import GUSLobbyGameDialogButton from './GUSLobbyGameDialogButton';

class GUSLobbyActiveGameDialogButton extends GUSLobbyGameDialogButton
{
	constructor(aBaseAssetActiveName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType, isNeedButtonStyleUpdateOnHandle, isDeactiveOnlyCaptionStyle)
	{
		super(aBaseAssetActiveName_str, aBaseAssetActiveName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType, isNeedButtonStyleUpdateOnHandle, isDeactiveOnlyCaptionStyle);

		this.handleOver();
	}

	_getBtnTextDectiveStyle()
	{
		return this._getBtnTextActiveStyle();
	}
}

export default GUSLobbyActiveGameDialogButton