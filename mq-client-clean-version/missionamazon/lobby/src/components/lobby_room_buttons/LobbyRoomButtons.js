import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import ArrowButton from './ArrowButton';
import Button from '../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import RoomButton from './RoomButton';
import LobbyStateController from '../../controller/state/LobbyStateController';
import LobbyPlayerController from '../../controller/custom/LobbyPlayerController';
import PlayerInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';

const ROOMS_SKIPPED_PER_CLICK_COUNT = 6;
const CURRENT_COUNT_ROOM = 6;

class LobbyRoomButtons extends Sprite
{
	static get EVENT_LOBBY_ROOM_STAKE_SELECTED()			{return RoomButton.EVENT_LOBBY_ROOM_STAKE_SELECTED};
	static get EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK()	{return RoomButton.EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK};
	static get EVENT_LOBBY_ROOM_ENABALED_STATE_CHANGED()	{return "EVENT_LOBBY_ROOM_ENABALED_STATE_CHANGED"};

	static get ROOM_IMAGES_AMOUNT() 				{return 6;}

	update(aStakes_num_arr)
	{
		this._update(aStakes_num_arr);
		this._updateRoomEnable(APP.playerController.info.balance);
	}

	updateWeapons(aData_arr)
	{
		this._updateWeapons(aData_arr);
	}

	constructor()
	{
		super();

		this._fRoomButtons = [];
		this._fRoomButtonsContainer = null;
		
		this._fCurrentPosition_int = undefined;
		this._fStakesAmount_int = undefined;
		this._fUpdateSubscribed_bln = false;
		this._fDisabledRoomsExist_bln = false;

		this._fAllWeapons_obj_arr = null;

		this._init();
	}

	_init()
	{
		this._fRoomButtonsContainer = this.addChild(new Sprite());
		//TODO: Back if need arrow
		// this._fArrowLeft_btn = this.addChild(new ArrowButton());
		// this._fArrowLeft_btn.position.set(-290, 4);
		// this._fArrowLeft_btn.hideSeptum();
		// this._fArrowLeft_btn.on("pointerclick", this._moveLeft, this);

		// this._fArrowRight_btn = this.addChild(new ArrowButton());
		// this._fArrowRight_btn.position.set(290, 4);
		// this._fArrowRight_btn.scale.x = -1;
		// this._fArrowRight_btn.hideSeptum();
		// this._fArrowRight_btn.on("pointerclick", this._moveRight, this);

		APP.playerController.on(LobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		this._fUpdateSubscribed_bln = true;

		APP.lobbyStateController.on(LobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyScreenVisibilityChanged, this);
	}

	_onLobbyScreenVisibilityChanged(data)
	{
		if (data.visible)
		{
			if (!this._fUpdateSubscribed_bln)
			{
				APP.playerController.on(LobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
				this._fUpdateSubscribed_bln = true;
			}
		}
		else
		{
			if (this._fUpdateSubscribed_bln)
			{
				APP.playerController.off(LobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
				this._fUpdateSubscribed_bln = false;
			}
		}

		this._updateRoomsOverState(data.visible);
	}

	_onPlayerInfoUpdated(event)
	{
		if (event.data[PlayerInfo.KEY_BALANCE])
		{
			this._updateRoomEnable(event.data[PlayerInfo.KEY_BALANCE].value);
		}
	}

	_updateRoomEnable(balance)
	{
		let lPrevDisabledRoomsExist_bln = this._fDisabledRoomsExist_bln;

		if (this._fRoomButtons)
		{
			let lCountOfEnabledRooms_int = 0;
			for (let i = 0; i < this._fRoomButtons.length; i++)
			{
				const lRoomButton_b = this._fRoomButtons[i];
				let lSpecialWeaponsExist_bl = APP.isKeepSWModeActive ? this._isAnySpecialWeaponExistForStake(lRoomButton_b.stake) : false;

				if ((lRoomButton_b.stake > balance) && !lSpecialWeaponsExist_bl)
				{
					lRoomButton_b.disableRoomBtn();
					this._fDisabledRoomsExist_bln = true;
				}
				else
				{
					lRoomButton_b.enableRoomBtn();
					lCountOfEnabledRooms_int++;
				}
			}
			if (lCountOfEnabledRooms_int === this._fRoomButtons.length) this._fDisabledRoomsExist_bln = false;
		}
		this._validateArrowButtonsGlow();
		
		if (lPrevDisabledRoomsExist_bln !== this._fDisabledRoomsExist_bln)
		{
			this.emit(LobbyRoomButtons.EVENT_LOBBY_ROOM_ENABALED_STATE_CHANGED, {isDisabledState: this._fDisabledRoomsExist_bln})
		}
	}

	_isAnySpecialWeaponExistForStake(aStake_num)
	{
		for (var stake in this._fAllWeapons_obj_arr)
		{
			if (stake == aStake_num)
			{
				return this._getWeaponsCount(this._fAllWeapons_obj_arr[stake]) > 0;
			}
		}
		return false;
	}


	_redrowRoomBrowsingProgress()
	{
		if(this._fProgressBarContainer_c)
		{
			if(this._fProgressBarButtons_btn_arr)
			{
				for( let i = 0; i < this._fProgressBarButtons_btn_arr.length; i++ )
				{
					this._fProgressBarButtons_btn_arr[i].removeAllListeners();
					this._fProgressBarButtons_btn_arr[i].destroy();
					this._fProgressBarContainer_c.removeChild(this._fProgressBarButtons_btn_arr[i]);
				}
			}

			if(this._fProgressBar_g)
			{
				this._fProgressBarContainer_c.removeChild(this._fProgressBar_g);
			}

			this.removeChild(this._fProgressBarContainer_c);
			this._fProgressBarButtons_btn_arr = null;
		}


		this._fProgressBarContainer_c = this.addChild(new Sprite());
		this._fProgressBarButtons_btn_arr = [];

		let lTotalSectionsCount_int = Math.ceil(this._fStakesAmount_int / ROOMS_SKIPPED_PER_CLICK_COUNT);
		
		if(lTotalSectionsCount_int === 0)
		{
			return;
		}
		
		let l_g = this._fProgressBarContainer_c.addChild(new PIXI.Graphics());

		this._fProgressBarContainer_c.position.y = APP.isMobile ? 165 : 175;

		this._fProgressBar_g = l_g;
	}

	_onProgressBarButtonClicked(aIndex_int)
	{
		let lNewPosition_int = aIndex_int * ROOMS_SKIPPED_PER_CLICK_COUNT;

		this._fCurrentPosition_int = lNewPosition_int;

		this._updateRoomsPosition(this._fCurrentPosition_int);
		this._redrowRoomBrowsingProgress();
	}

	_updateRoomsOverState(lobbyScreenVisible_bl)
	{
		if(this._fRoomButtons)
		{
			for(let i = 0; i < this._fRoomButtons.length; i++)
			{
				this._fRoomButtons[i].updateOverState(lobbyScreenVisible_bl);
			}
		}
	}

	_update(aStakes_num_arr)
	{
		this._fRoomButtonsContainer && this._buttonsContainerClear();

		//there was a request to limit room icons amount: https://jira.dgphoenix.com/browse/DRAG-691
		this._fStakesAmount_int = aStakes_num_arr.length > CURRENT_COUNT_ROOM ? CURRENT_COUNT_ROOM : aStakes_num_arr.length;

		var lValueGrow_bl = true;
		if(this._fStakesAmount_int > 1 && aStakes_num_arr[0] > aStakes_num_arr[1])
		{
			lValueGrow_bl = false;
		}

		for (let i = 0; i < this._fStakesAmount_int; i++)
		{
			let lButton_rb = this._fRoomButtonsContainer.addChild(this._getRoomButton(i));
			lButton_rb.updateStake(aStakes_num_arr[i], (lValueGrow_bl ? i : this._fStakesAmount_int - 1 - i));
			lButton_rb.updateIndicators(0);

			if(i == 0){
				lButton_rb.scale.set(0.9,0.9);
			}
			if(i == this._fStakesAmount_int-1){
				lButton_rb.scale.set(1.1,1.1);
			}


		}
		this._fRoomButtonsContainer.position.x = -RoomButton.INIT_X - RoomButton.VISIBLE_OFFSET_X * (Math.min(this._fStakesAmount_int, RoomButton.VISIBLE_AMOUNT) - 1) / 2 -189; // -169 === Center correction for new lobby
		this._fRoomButtonsContainer.position.y = -50 + (APP.isMobile ? -10 : 0);

		//TODO: Back if need arrow
		// this._fArrowLeft_btn.visible = (this._fStakesAmount_int > RoomButton.VISIBLE_AMOUNT);
		// this._fArrowRight_btn.visible = (this._fStakesAmount_int > RoomButton.VISIBLE_AMOUNT);
		this._updateRoomsPosition(0, true, lValueGrow_bl);

		this._redrowRoomBrowsingProgress();
	}

	_updateWeapons(aData_arr)
	{
		this._fAllWeapons_obj_arr = aData_arr;

		for (let stake in aData_arr)
		{
			let lButton_btn = this._getRoomButtonByStake(stake);
			let lWeaponsCount_num = this._getWeaponsCount(aData_arr[stake]);

			if (lButton_btn)
			{
				lButton_btn.updateWeaponsIndicator(lWeaponsCount_num);
			}
		}
		this._updateRoomEnable(APP.playerController.info.balance);
	}

	_getRoomButtonByStake(aStake_num)
	{
		return this._fRoomButtons.filter((val)=>{return val.stake == aStake_num})[0];
	}

	_getWeaponsCount(aWeapons_arr)
	{
		return aWeapons_arr.reduce((acc, cur)=>{return cur.shots ? acc+1 : acc}, 0);
	}

	_getRoomButton(aIndex_int)
	{
		return this._fRoomButtons[aIndex_int] || this._initRoomButton(aIndex_int);
	}

	_initRoomButton(aIndex_int)
	{
		let lButton_rb = new RoomButton(aIndex_int);
		lButton_rb.on(LobbyRoomButtons.EVENT_LOBBY_ROOM_STAKE_SELECTED, this.emit, this);

		lButton_rb.on(LobbyRoomButtons.EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK, this.emit, this);

		this._fRoomButtons[aIndex_int] = lButton_rb;
		return lButton_rb;
	}

	_buttonsContainerClear()
	{
		while (this._fRoomButtonsContainer.children.length != 0)
		{
			this._fRoomButtonsContainer.removeChild(this._fRoomButtonsContainer.children[0]);
		}

		let lMask_g = this._fRoomButtonsContainer.addChild(new PIXI.Graphics());
		lMask_g.beginFill(0x000000).drawRect(-470, -300, 930, 600).endFill();
		lMask_g.position.set(436, 0);
		this._fRoomButtonsContainer.mask = lMask_g;
	}

	_moveLeft()
	{
		this._fCurrentPosition_int -= ROOMS_SKIPPED_PER_CLICK_COUNT;

		if(this._fCurrentPosition_int < 0)
		{
			this._fCurrentPosition_int = 0;
		}

		this._updateRoomsPosition(this._fCurrentPosition_int);
		this._redrowRoomBrowsingProgress();
	}

	_moveRight()
	{
		this._fCurrentPosition_int += ROOMS_SKIPPED_PER_CLICK_COUNT;
		
		if(this._fCurrentPosition_int > this._fStakesAmount_int - 1)
		{
			this._fCurrentPosition_int = this._fStakesAmount_int - 1;
		}

		this._updateRoomsPosition(this._fCurrentPosition_int);
		this._redrowRoomBrowsingProgress();
	}

	_updateRoomsPosition(aIndex_int, aSkipAnimation_bl, aIsReverseGrow_bl)
	{
		this._fCurrentPosition_int = aIndex_int;
		for (let i = 0; i < this._fRoomButtons.length; i++)
		{
			this._fRoomButtons[i].updatePosition(aIndex_int, aSkipAnimation_bl, aIsReverseGrow_bl);
		}
		
		//TODO: Back if need arrow
		// this._fArrowLeft_btn.enabled = this._fCurrentPosition_int > 0;
		// this._fArrowRight_btn.enabled = this._fCurrentPosition_int < this._fStakesAmount_int - RoomButton.VISIBLE_AMOUNT;

		this._validateArrowButtonsGlow();

	}

	_areAllVisibleRoomsDisabled()
	{
		let n = Math.min(this._fRoomButtons.length, this._fCurrentPosition_int + RoomButton.VISIBLE_AMOUNT);
		for (let i=this._fCurrentPosition_int; i<n; i++)
		{
			let lRoomButton_b = this._fRoomButtons[i];
			if (lRoomButton_b.enabled)
			{
				return false;
			}
		}
		return true;
	}

	_isAnyRoomEnabledToTheRight()
	{
		let start = this._fCurrentPosition_int + RoomButton.VISIBLE_AMOUNT;
		for (let i=start; i<this._fRoomButtons.length; i++)
		{
			let lRoomButton_b = this._fRoomButtons[i];
			if (lRoomButton_b.enabled)
			{
				return true;
			}
		}
		return false;
	}

	_isAnyRoomEnabledToTheLeft()
	{
		let start = this._fCurrentPosition_int - 1;
		for (let i=start; i>= 0; i--)
		{
			let lRoomButton_b = this._fRoomButtons[i];
			if (lRoomButton_b.enabled)
			{
				return true;
			}
		}
		return false;
	}

	_validateArrowButtonsGlow()
	{
		//TODO: Back if need arrow
		// if (this._areAllVisibleRoomsDisabled())
		// {
		// 	this._fArrowLeft_btn.setGlowable(this._isAnyRoomEnabledToTheLeft());
		// 	this._fArrowRight_btn.setGlowable(this._isAnyRoomEnabledToTheRight());
		// }
		// else
		// {
		// 	this._fArrowLeft_btn.setGlowable(false);
		// 	this._fArrowRight_btn.setGlowable(false);
		// }
	}

	_startArrowsAnimation()
	{
		for (let i = 0; i < this._fRoomButtons.length; i++)
		{
			this._fRoomButtons[i].startArrowAnimation();
		}
	}

	_stopArrowsAnimation()
	{
		for (let i = 0; i < this._fRoomButtons.length; i++)
		{
			this._fRoomButtons[i].stopArrowAnimation();
		}
	}

	destroy()
	{
		APP.playerController.off(LobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.lobbyStateController.off(LobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyScreenVisibilityChanged, this);

		this._fRoomButtons = null;

		this._fRoomButtonsContainer && this._buttonsContainerClear();

		this._fRoomButtonsContainer && this._fRoomButtonsContainer.destroy();
		this._fRoomButtonsContainer = null;

		this._fUpdateSubscribed_bln = null;

		super.destroy();
	}
}

export default LobbyRoomButtons;