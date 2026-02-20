import DialogView from '../DialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class WebSocketReconnectionAttemptDialogView extends DialogView
{
	constructor()
	{
		super();
	}

	setMessage(messageId, attemptMessageId, attemptNumber)
	{
		this._setMessage(messageId, attemptMessageId, attemptNumber);
	}

	_setMessage(messageId, attemptMessageId, attemptNumber)
	{
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset(messageId);
		msg.position.set(0, 10);

		let msgAttempt = I18.generateNewCTranslatableAsset(attemptMessageId);
		msgAttempt.position.set(0, 40);

		msgAttempt.text = msgAttempt.text.replace("/VALUE/", attemptNumber)

		this._messageContainer.addChild(msg);
		this._messageContainer.addChild(msgAttempt);
	}
}

export default WebSocketReconnectionAttemptDialogView;