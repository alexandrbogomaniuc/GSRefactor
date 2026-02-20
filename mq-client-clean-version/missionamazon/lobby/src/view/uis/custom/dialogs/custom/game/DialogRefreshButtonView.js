import DialogButton from '../../DialogButton';

class DialogRefreshButtonView extends DialogButton
{
	constructor()
	{
		super();
	}

	startCaptionRotation()
	{
		this.caption.removeTweens();
		this.caption.rotation = 0;
		this.caption.rotateTo(Math.PI*2, 540, undefined, () => {this.startCaptionRotation()});
	}

	interruptCaptionRotation()
	{
		this.caption.removeTweens();
		this.caption.rotation = 0;
	}
}

export default DialogRefreshButtonView