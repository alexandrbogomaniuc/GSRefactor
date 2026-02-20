import NonActiveGameDialogButton from './NonActiveGameDialogButton';

class NonActiveBattlegroundDialogButton extends NonActiveGameDialogButton
{
	constructor(aBaseAssetActiveName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType, isNeedButtonStyleUpdateOnHandle)
	{
		super(aBaseAssetActiveName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType, isNeedButtonStyleUpdateOnHandle);
	}

	_getBtnTextDectiveStyle()
	{
		let lStyle = 
		{
			fill: 0xffffff,
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 0.5,
			dropShadowAlpha: 0.5
		}

		return lStyle;
	}
}

export default NonActiveBattlegroundDialogButton