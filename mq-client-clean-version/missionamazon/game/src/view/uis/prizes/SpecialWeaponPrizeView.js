import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ContentItemInfo from '../../../model/uis/content/ContentItemInfo';
import ContentItem from '../../../ui/content/ContentItem';

class SpecialWeaponPrizeView extends SimpleUIView
{
	static get i_EVENT_ON_ANIMATION_COMPLETED() { return 'i_EVENT_ON_ANIMATION_COMPLETED'; }

	i_init(aContainerInfo_obj)
	{
		aContainerInfo_obj.container.addChild(this);
		this.zIndex = aContainerInfo_obj.zIndex;
	}

	i_startAnimation()
	{
		this._startAnimation();
	}

	constructor()
	{
		super();

		this._fContentItem_ci = null;
	}

	_startAnimation()
	{
		let lWeaponId_int = this.uiInfo.specialWeaponId;
		let lSeatId_int = this.uiInfo.seatId;
		let lStartPosition_pt = this.uiInfo.startPosition;
		let weaponAssetName = APP.currentWindow.gameField.getWeaponEmblemAssetName(lWeaponId_int);
		let weaponAssetAnchor = APP.currentWindow.gameField.getWeaponEmblemAnchor(lWeaponId_int);

		let weaponAssetGlowDescriptor = null;
		if (lSeatId_int === APP.playerController.info.seatId) // master seat
		{
			let weaponAssetGlowName = APP.currentWindow.gameField.getWeaponEmblemGlowAssetName(lWeaponId_int);
			let weaponAssetGlowAnchor = APP.currentWindow.gameField.getWeaponEmblemGlowAnchor(lWeaponId_int);
			let weaponAssetGlowScale = 2;
			let weaponAssetGlowDescriptor = {
				assetName: weaponAssetGlowName,
				anchor: weaponAssetGlowAnchor,
				scale: weaponAssetGlowScale
			};
		}

		let contentItemInfo = new ContentItemInfo(ContentItemInfo.TYPE_WEAPON, lSeatId_int, weaponAssetName, weaponAssetAnchor, null, lWeaponId_int,
			null, null, null, weaponAssetGlowDescriptor);
		let contentItem = this.addChild(new ContentItem(contentItemInfo));

		let lWeaponItemWidth_num = APP.library.getSpriteFromAtlas(weaponAssetName).width;
		let lWeaponItemHeight_num = APP.library.getSpriteFromAtlas(weaponAssetName).height;
		let lWeaponBaseScale_num = contentItem.getBaseScale();
		let lWeaponMaxScale_num = contentItem.getMaxScale();
		let lPosX_num = lStartPosition_pt.x;
		let lPosY_num = lStartPosition_pt.y;
		let lYOffset_num = -100;

		let lMaxWeaponWidth_num = lWeaponItemWidth_num * lWeaponBaseScale_num * lWeaponMaxScale_num;
		let lMaxWeaponHeight_num = lWeaponItemHeight_num * lWeaponBaseScale_num * lWeaponMaxScale_num;

		if (lPosX_num < lMaxWeaponWidth_num)
		{
			lPosX_num = lMaxWeaponWidth_num;
		} 
		else if (lPosX_num > (APP.config.size.width - lMaxWeaponWidth_num))
		{
			lPosX_num = APP.config.size.width - lMaxWeaponWidth_num;
		}

		if (lPosY_num < (lMaxWeaponHeight_num - lYOffset_num))
		{
			lPosY_num = lMaxWeaponHeight_num - lYOffset_num;
		} 
		else if (lPosY_num > (920 - lMaxWeaponHeight_num - lYOffset_num))
		{
			lPosY_num = 920 - lMaxWeaponHeight_num - lYOffset_num;
		}

		contentItem.position.set(lPosX_num, lPosY_num);

		contentItem.once(ContentItem.ON_CONTENT_LANDED, this._onWeaponContentLanded, this);
		contentItem.startAnimation(new PIXI.Point(0, lYOffset_num), false, 2);

		this._fContentItem_ci = contentItem;
	}

	_onWeaponContentLanded()
	{
		this._fContentItem_ci && this._fContentItem_ci.destroy();
		this._fContentItem_ci = null;

		this.emit(SpecialWeaponPrizeView.i_EVENT_ON_ANIMATION_COMPLETED);
	}

	destroy()
	{
		this.removeAllListeners();

		this._fContentItem_ci && this._fContentItem_ci.destroy();
		this._fContentItem_ci = null;

		super.destroy();
	}
}

export default SpecialWeaponPrizeView;