import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Button from '../ui/LobbyButton';
import LobbyExternalCommunicator from '../external/LobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';

class CommonButton extends Button
{
	hideSeptum()
	{
		if (!this._fSeptum_grphc) return;
		this._fSeptum_grphc.visible = false;
	}

	showSeptum()
	{
		if (!this._fSeptum_grphc) return;
		this._fSeptum_grphc.visible = true;
	}

	constructor(baseAssetName, captionId = undefined, aOptDrawSeptum_bln = true, aOptScale, aIsBlackAndWhiteButton_bl = false)
	{
		super(baseAssetName, captionId, true);

		this._addUpHandler();

		if (aOptScale)
		{
			if (aOptScale.x && aOptScale.y)
			{
				this._baseView.scale.x = aOptScale.x;
				this._baseView.scale.y = aOptScale.y;
			}
			else
			{
				this._baseView.scale.set(aOptScale);
			}
		}

		this._fSeptum_grphc = null;
		this._fIsBlackAndWhiteButton_bl = aIsBlackAndWhiteButton_bl;

		if (aOptDrawSeptum_bln)
		{
			this._addSeptum();
		}
	}

	_addSeptum()
	{
		this._fSeptum_grphc = this.addChild(new PIXI.Graphics());

		let lHeight_num = APP.isMobile ? 22 : 18;

		this._fSeptum_grphc.beginFill(0x4e4e4e).drawRect(-1, -lHeight_num/2, 1, lHeight_num).endFill();
		this._fSeptum_grphc.position.set(-18, 0);
	}

	_addUpHandler()
	{
		document.body.addEventListener("mouseup", this._onDocumentMouseUp.bind(this));
		document.body.addEventListener("pointerup", this._onDocumentMouseUp.bind(this));
		document.body.addEventListener("touchend", this._onDocumentMouseUp.bind(this));
	}

	_onDocumentMouseUp()
	{
		if (this._fHolded_bln)
		{
			this.handleUp();
		}
	}

	setEnabled()
	{
		this._fIsBlackAndWhiteButton_bl && (this._baseView.tint = 0xffffff);

		super.setEnabled();
	}

	setDisabled()
	{
		this._fIsBlackAndWhiteButton_bl && (this._baseView.tint = 0x969696);

		super.setDisabled();
	}

	destroy()
	{
		super.destroy();

		this._fSeptum_grphc = null;
		this._fIsBlackAndWhiteButton_bl = null;
	}
}

export default CommonButton;
