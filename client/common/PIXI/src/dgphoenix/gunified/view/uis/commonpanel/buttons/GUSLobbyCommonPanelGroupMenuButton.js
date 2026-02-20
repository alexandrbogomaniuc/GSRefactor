import GUSLobbyCommonPanelButton from './GUSLobbyCommonPanelButton';

class GUSLobbyCommonPanelGroupMenuButton extends GUSLobbyCommonPanelButton
{
	onGroupOpening()
	{
		this._onGroupOpening();
	}

	onGroupHiding()
	{
		this._onGroupHiding();
	}

	constructor(baseAssetName, captionId, aOptDrawSeptum_bln)
	{
		super(baseAssetName, captionId, aOptDrawSeptum_bln);

		if (this.caption)
		{
			this.caption.position.set(20, 1);
		}
	}

	_onGroupOpening()
	{
		this._fSelectedBack_grphc.visible = true;
	}

	_onGroupHiding()
	{
		this._fSelectedBack_grphc.visible = false;
	}

	_updateBaseView(baseAssetName)
	{
		this._fSelectedBack_grphc = this.holder.addChild(new PIXI.Graphics());
		this._fSelectedBack_grphc.beginFill(0xfccc32).drawRect(-20, -17, 87, 34).endFill();
		this._fSelectedBack_grphc.visible = false;

		this.setHitArea(new PIXI.Rectangle(-20, -17, 87, 34));

		super._updateBaseView(baseAssetName);
	}

	_addSeptum()
	{
		super._addSeptum();

		this._fSeptum_grphc.position.x = -20;
	}

	handleDown()
	{
		this._tryPlaySound();
	}

	destroy()
	{
		super.destroy();

		this._fSelectedBack_grphc = null;
	}
}

export default GUSLobbyCommonPanelGroupMenuButton