import Button from '../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PreloaderTextures from './PreloaderTextures';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class LoadingScreenPlayNowButton extends Button
{
	constructor(aIsEnabled_bl, aOptIsPortrait_bl = false)
	{
		PreloaderTextures.initTextures();

		let lBaseAssetName_str = aIsEnabled_bl ? PreloaderTextures["play_now_btn_textures"][1] : PreloaderTextures["play_now_btn_textures"][0];
		let lCTAName_str = "TAPreloderPlayNow";

		if (APP.isBattlegroundGame)
		{
			if (aOptIsPortrait_bl)
			{
				lBaseAssetName_str = "preloader/click_continue_portrait";
				lCTAName_str = "TAPreloderClickContinuePortrait";
			}
			else
			{
				lBaseAssetName_str = "preloader/click_continue";
				lCTAName_str = "TAPreloderClickContinue";
			}
		}

		super(lBaseAssetName_str, lCTAName_str, true, true, 0, undefined);

		if(aIsEnabled_bl)
		{
			if (aOptIsPortrait_bl)
			{
				this.hitArea = new PIXI.Rectangle(-122, -20, 244, 40);
			}
			else
			{
				this.hitArea = new PIXI.Rectangle(-73, -17, 146, 32);
			}
			if (!APP.isBattlegroundGame)
			{
				this._addBaseAssetActiveBack();
			}
		}
		else
		{
			this.alpha = 0.5;
			this.interactive = false;
		}

	}


	_addBaseAssetActiveBack()
	{
		this._fBaseAssetActiveName_spr = this.holder.addChildAt(new Sprite, 1);
		this._fBaseAssetActiveName_spr.texture = PreloaderTextures["play_now_btn_textures"][2];
		this._fBaseAssetActiveName_spr.visible = false;
	}

	handleOver() 
	{
		if(this._fBaseAssetActiveName_spr)
		{
			this._fBaseAssetActiveName_spr.visible = true;
		}
	}

	handleOut() 
	{
		if(
			this._fBaseAssetActiveName_spr &&
			!this._fHolded_bln
			)
		{
			this._fBaseAssetActiveName_spr.visible = false;
		}
	}

	handleUp(e)
	{
		super.handleUp(e);

		if(this._fBaseAssetActiveName_spr)
		{
			this._fBaseAssetActiveName_spr.visible = false;
		}
	}

	destroy()
	{
		super.destroy();

		this._fBaseAssetActiveName_spr = null;
	}

}

export default LoadingScreenPlayNowButton