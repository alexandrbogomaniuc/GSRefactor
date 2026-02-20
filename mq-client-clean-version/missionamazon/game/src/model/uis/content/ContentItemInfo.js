import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
class ContentItemInfo
{
	static get TYPE_TREASURE () { return 'treasure' };
	static get TYPE_WEAPON () { return 'weapon' };

	constructor(assetType=ContentItemInfo.TYPE_TREASURE, playerSeatId, assetName, optAssetAnchor, optAssetTexture, optWeaponId_num, aAwardedWeapon_obj, aTreasureId_num, aOptAssetTextureGlow_txtr, aOptAssetGlowItemDescriptor_obj)
	{
		this._assetType = assetType;
		this._assetName = assetName;
		this._playerSeatId = playerSeatId;
		this._assetAnchor = optAssetAnchor;
		this._assetTexture = optAssetTexture;
		this._weaponId = optWeaponId_num;
		this._awardedWeapon = aAwardedWeapon_obj;// object {id, shots, [reason]}
		this._treasureId = aTreasureId_num;
		this._assetTextureGlow = aOptAssetTextureGlow_txtr;
		this._assetGlowItemDescriptor_obj = aOptAssetGlowItemDescriptor_obj; //{assetName, position, scale}
	}

	get assetTextureGlow()
	{
		return this._assetTextureGlow;
	}

	get assetGlowItemDescriptor()
	{
		return this._assetGlowItemDescriptor_obj;
	}

	get treasureId()
	{
		return this._treasureId;
	}

	get assetType()
	{
		return this._assetType;
	}

	get assetName()
	{
		return this._assetName;
	}

	get assetAnchor()
	{
		return this._assetAnchor;
	}

	get playerSeatId()
	{
		return this._playerSeatId;
	}

	get isMasterSeat()
	{
		return !isNaN(this.playerSeatId) && (this.playerSeatId === APP.playerController.info.seatId);
	}

	get assetTexture()
	{
		return this._assetTexture;
	}

	get weaponId()
	{
		return this._weaponId;
	}

	get awardedWeapon()
	{
		return this._awardedWeapon;
	}

	get isWeaponType()
	{
		return this.assetType === ContentItemInfo.TYPE_WEAPON;
	}

	destroy()
	{
		this._assetType = undefined;
		this._assetName = undefined;
		this._playerSeatId = undefined;
		this._assetAnchor = null;
		this._assetTexture = null;
		this._weaponId = null;
		this._awardedWeapon = null;
	}

}

export default ContentItemInfo