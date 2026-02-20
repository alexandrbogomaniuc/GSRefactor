import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import {ROUND_STATE} from '../../../model/state/GameStateInfo';
import BattlegroundSpotWeaponIconView from './BattlegroundSpotWeaponIconView';
import { WEAPONS } from '../../../../../shared/src/CommonConstants';

const ICON_WIDTH = 21;

class BattlegroundSpotWeaponQueueView extends Sprite
{
	constructor(aWeaponId_int)
	{
		super();

		this._fIcons_bswiv_arr = [];
		this._fIsJustCreated_bl = true;
	}

	addWeapon(aWeaponId_int, aShotsCount_int)
	{
		let l_bswiv = this._getNextAvailableWeaponIcon();
		l_bswiv.position.x = this._getNextPositionX();
		l_bswiv.startIntroAnimation(aWeaponId_int, aShotsCount_int);
	}

	removeWeapon(aWeaponId_int)
	{
		for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
		{
			let lIcon_bswiv = this._fIcons_bswiv_arr[i];

			if(
				lIcon_bswiv.isRequired() &&
				lIcon_bswiv.getWeaponId() === aWeaponId_int
				)
			{
				lIcon_bswiv.setIsRequired(false);
			}
		}
	}

	getNextSuggestedBattlegroundFreeWeaponId()
	{
		let lX_num = 10000;
		let lWeaponId_int = undefined;

		for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
		{

			let lIcon_bswiv = this._fIcons_bswiv_arr[i];

			if(
				lIcon_bswiv.isRequired() &&
				lIcon_bswiv.position.x < lX_num
				)
			{
				lX_num = lIcon_bswiv.position.x;
				lWeaponId_int = lIcon_bswiv.getWeaponId();
			}
		}

		return lWeaponId_int;
	}

	_getNextAvailableWeaponIcon()
	{
		for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
		{
			if(this._fIcons_bswiv_arr[i].canBeReused())
			{
				return this._fIcons_bswiv_arr[i];
			}
		}

		let l_bswiv = new BattlegroundSpotWeaponIconView();
		this.addChild(l_bswiv)
		this._fIcons_bswiv_arr.push(l_bswiv);

		return l_bswiv;
	}

	getAwardedWeaponLandingX(aWeaponId_int)
	{
		let lAllreadyExistantIcon_bswiv = this._getPresentedIconByWeaponId(aWeaponId_int);

		if(lAllreadyExistantIcon_bswiv)
		{
			return lAllreadyExistantIcon_bswiv.position.x;
		}

		return this._getNextPositionX();
	}

	_getNextPositionX(aOptRelativeX_num = 10000)
	{
		let lX_num = 0;

		for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
		{
			let l_bswiv = this._fIcons_bswiv_arr[i];

			if(
				!l_bswiv.canBeReused() &&
				l_bswiv.position.x > lX_num &&
				l_bswiv.position.x < aOptRelativeX_num
				)
			{
				lX_num = l_bswiv.position.x;
			}
		}

		return lX_num + ICON_WIDTH;
	}

	_getLeftMostBorderX(aOptRelativeX_num = 10000)
	{
		let lX_num = ICON_WIDTH;

		for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
		{
			let l_bswiv = this._fIcons_bswiv_arr[i];

			if(
				!l_bswiv.canBeReused() &&
				l_bswiv.position.x > lX_num &&
				l_bswiv.position.x < aOptRelativeX_num
				)
			{
				lX_num = l_bswiv.position.x;
			}
		}

		return lX_num;
	}

	_getPresentedIconByWeaponId(aWeaponId_int)
	{
		let lCount_int = 0;

		for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
		{
			if(
				this._fIcons_bswiv_arr[i].isRequired() &&
				this._fIcons_bswiv_arr[i].getWeaponId() === aWeaponId_int
				)
			{
				return this._fIcons_bswiv_arr[i];
			}
		}

		return null;
	}

	_isLeftMostIconOnBorder(aIcon_bswiv)
	{
		for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
		{
			let l_bswiv = this._fIcons_bswiv_arr[i];

			if(
					(
						!l_bswiv.canBeReused() &&
						l_bswiv !== aIcon_bswiv
					)
					&&
					(
						l_bswiv.position.x < aIcon_bswiv.position.x ||
						l_bswiv.position.x === ICON_WIDTH
					)
				)
			{
				return false;
			}
		}

		return aIcon_bswiv.position.x === ICON_WIDTH;
	}

	_isAnyIconDisappearing()
	{
		for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
		{

			let l_bswiv = this._fIcons_bswiv_arr[i];

			if(
				!l_bswiv.canBeReused() &&
				l_bswiv.isOutroAnimationInProcess()
				)
			{
				return true;
			}
		}

		return false;
	}

	tick(aDelta_int)
	{
		//VALIDATION...
		if (APP.gameScreen.gameStateController.info.gameState === ROUND_STATE.QUALIFY)
		{
			//DO NOT DISPLAY ANY WEAPONS ON QUIALIFY STATE...
			for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
			{
				if(this._fIcons_bswiv_arr[i].isRequired())
				{
					return this._fIcons_bswiv_arr[i].setIsRequired(false);;
				}
			}
			//...DO NOT DISPLAY ANY WEAPONS ON QUIALIFY STATE
		}
		else
		{
	
			let lWeapons_obj_arr = APP.currentWindow.weaponsController.info.weapons;

			if(lWeapons_obj_arr)
			{
				for ( let i = lWeapons_obj_arr.length - 1; i >= 0; i-- )
				{
					let l_obj = lWeapons_obj_arr[i];
					let lWeaponId_int = l_obj.id;
					let lShotsCount_int = l_obj.shots;
					let lAllreadyExistantIcon_bswiv = this._getPresentedIconByWeaponId(lWeaponId_int);

					if(
						lShotsCount_int > 0 &&
						lWeaponId_int !== WEAPONS.HIGH_LEVEL
						)
					{
						if(!lAllreadyExistantIcon_bswiv)
						{
							this.addWeapon(lWeaponId_int, lShotsCount_int);
						}
						else if(lAllreadyExistantIcon_bswiv.getRemainingShotsCount() < lShotsCount_int)
						{
							lAllreadyExistantIcon_bswiv.setRemainingShotsCount(lShotsCount_int);
							lAllreadyExistantIcon_bswiv.tryToPlayRechargeAnimation();
						}
					}
					else if(lAllreadyExistantIcon_bswiv)
					{
						this.removeWeapon(lWeaponId_int);
					}
				}
			}
		}
		//...VALIDATION

		//LASTHAND...
		if(this._fIsJustCreated_bl)
		{
			for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
			{
				let l_bswiv = this._fIcons_bswiv_arr[i];

				if(!l_bswiv.isRequired)
				{
					l_bswiv.drop();
				}
				else
				{
					l_bswiv.skipIntroIfPlaying();
				}
			}

			this._fIsJustCreated_bl = false;

			return;
		}
		//...LASTHAND



		for( let i = 0; i < this._fIcons_bswiv_arr.length; i++ )
		{
			let l_bswiv = this._fIcons_bswiv_arr[i];

			if(!l_bswiv.canBeReused())
			{
				let lLeftBorderX_num = 0;


				if(l_bswiv.isRequired())
				{
					l_bswiv.zIndex = l_bswiv.position.x;
					lLeftBorderX_num = this._getNextPositionX(l_bswiv.position.x);
				}
				else
				{
					l_bswiv.zIndex = -l_bswiv.position.x;
					lLeftBorderX_num = this._getLeftMostBorderX(l_bswiv.position.x);
				}

				if(l_bswiv.position.x > lLeftBorderX_num)
				{
					l_bswiv.position.x -= 0.15 * aDelta_int;

					if(l_bswiv.position.x < lLeftBorderX_num)
					{
						l_bswiv.position.x = lLeftBorderX_num;
					}
				}

				if(
					!l_bswiv.isRequired() &&
					l_bswiv.position.x === lLeftBorderX_num
					)
				{

					if(
						this._isLeftMostIconOnBorder(l_bswiv) &&
						!this._isAnyIconDisappearing()
						)
					{
						l_bswiv.startOutroAnimation();
					}
					else if(!l_bswiv.isOutroAnimationInProcess())
					{
						l_bswiv.drop();
					}
				}
			}
		}
	}
}

export default BattlegroundSpotWeaponQueueView;