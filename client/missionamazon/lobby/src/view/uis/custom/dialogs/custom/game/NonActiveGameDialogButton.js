import GameDialogButton from './GameDialogButton';

class NonActiveGameDialogButton extends GameDialogButton
{
	constructor(aBaseAssetActiveName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType, isNeedButtonStyleUpdateOnHandle)
	{
		super(aBaseAssetActiveName_str, aBaseAssetActiveName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType, isNeedButtonStyleUpdateOnHandle);
	}

	_getBtnTextActiveStyle()
	{
		return this._getBtnTextDectiveStyle();
	}

}

export default NonActiveGameDialogButton