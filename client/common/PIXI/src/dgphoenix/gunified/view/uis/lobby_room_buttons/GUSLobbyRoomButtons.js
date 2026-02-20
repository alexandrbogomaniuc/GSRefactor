import Sprite from '../../../../unified/view/base/display/Sprite';
import GUSLobbyArrowButton from './GUSLobbyArrowButton';
import GUSLobbyPlayerController from '../../../controller/custom/GUSLobbyPlayerController';
import PlayerInfo from '../../../../unified/model/custom/PlayerInfo';
import GUSLobbyStateController from '../../../controller/state/GUSLobbyStateController';
import { APP } from '../../../../unified/controller/main/globals';
import GUSLobbyRoomButton from './GUSLobbyRoomButton';

class GUSLobbyRoomButtons extends Sprite
{
	static get EVENT_LOBBY_ROOM_STAKE_SELECTED()			{return GUSLobbyRoomButton.EVENT_LOBBY_ROOM_STAKE_SELECTED;}

	static get ROOMS_SKIPPED_PER_CLICK_COUNT() {return 1};

	update(aStakes_num_arr)
	{
		this._update(aStakes_num_arr);
		this._updateRoomEnable(APP.playerController.info.balance);
	}

	constructor()
	{
		super();

		this._fRoomButtons = [];
		this._fRoomButtonsContainer = null;

		this._fCurrentPosition_int = undefined;
		this._fStakesAmount_int = undefined;
		this._fUpdateSubscribed_bln = false;
		this._fAreaMask_g = null;

		this._init();
	}

	get __isArrowButtonsSupported()
	{
		return true;
	}

	_init()
	{
		this._fRoomButtonsContainer = this.addChild(new Sprite());

		if (this.__isArrowButtonsSupported)
		{
			this.__initLeftArrowBtn();
			this.__initRightArrowBtn();
		}

		APP.playerController.on(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		this._fUpdateSubscribed_bln = true;

		APP.lobbyStateController.on(GUSLobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyScreenVisibilityChanged, this);
	}

	__initLeftArrowBtn()
	{
		this._fArrowLeft_btn = this.addChild(this.__provideLeftArrowBtnInstance());
		this._fArrowLeft_btn.hideSeptum();
		this._fArrowLeft_btn.on("pointerclick", this._moveLeft, this);
	}

	__provideLeftArrowBtnInstance()
	{
		let lArrow_ab = new GUSLobbyArrowButton();

		return lArrow_ab;
	}

	__initRightArrowBtn()
	{
		this._fArrowRight_btn = this.addChild(this.__provideRightArrowBtnInstance());
		this._fArrowRight_btn.scale.x = -1;
		this._fArrowRight_btn.hideSeptum();
		this._fArrowRight_btn.on("pointerclick", this._moveRight, this);
	}

	__provideRightArrowBtnInstance()
	{
		let lArrow_ab = new GUSLobbyArrowButton();

		return lArrow_ab;
	}

	_onLobbyScreenVisibilityChanged(data)
	{
		if (data.visible)
		{
			if (!this._fUpdateSubscribed_bln)
			{
				APP.playerController.on(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
				this._fUpdateSubscribed_bln = true;
			}
		}
		else
		{
			if (this._fUpdateSubscribed_bln)
			{
				APP.playerController.off(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
				this._fUpdateSubscribed_bln = false;
			}
		}
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
		if (this._fRoomButtons)
		{
			for (let i = 0; i < this._fRoomButtons.length; i++)
			{
				const lRoomButton_b = this._fRoomButtons[i];

				if (lRoomButton_b.stake > balance)
				{
					lRoomButton_b.disableRoomBtn();
				}
				else
				{
					lRoomButton_b.enableRoomBtn();
				}
			}
		}
		this._validateArrowButtonsGlow();
	}

	get __maxAvailableRoomsAmount()
	{
		return 5;
	}

	get _isMaxAvailableRoomsAmountDefined()
	{
		let lMaxAmount_int = this.__maxAvailableRoomsAmount;
		return !isNaN(lMaxAmount_int) && lMaxAmount_int > 0;
	}

	_update(aStakes_num_arr)
	{
		this._fRoomButtonsContainer && this._buttonsContainerClear();

		//there was a request to limit room icons amount: https://jira.dgphoenix.com/browse/DRAG-691
		this._fStakesAmount_int = aStakes_num_arr.length;
		if (this._isMaxAvailableRoomsAmountDefined)
		{
			let lMaxAmount_int = this.__maxAvailableRoomsAmount;
			if (this._fStakesAmount_int > lMaxAmount_int)
			{
				this._fStakesAmount_int = lMaxAmount_int;
			}
		}

		var lValueGrow_bl = true;
		if (this._fStakesAmount_int > 1 && aStakes_num_arr[0] > aStakes_num_arr[1])
		{
			lValueGrow_bl = false;
		}

		for (let i = 0; i < this._fStakesAmount_int; i++)
		{
			let lButton_rb = this._fRoomButtonsContainer.addChild(this._getRoomButton(i));
			lButton_rb.updateStake(aStakes_num_arr[i], (lValueGrow_bl ? i : this._fStakesAmount_int - 1 - i));
		}

		let lRoomButtonsContainerPosition_p = this.__calcRoomButtonsContainerPosition();
		this._fRoomButtonsContainer.position.x = lRoomButtonsContainerPosition_p.x;
		this._fRoomButtonsContainer.position.y = lRoomButtonsContainerPosition_p.y;

		if (this.__isArrowButtonsSupported)
		{
			this._fArrowLeft_btn.visible = (this._fStakesAmount_int > GUSLobbyRoomButton.VISIBLE_AMOUNT);
			this._fArrowRight_btn.visible = (this._fStakesAmount_int > GUSLobbyRoomButton.VISIBLE_AMOUNT);
		}

		window.LA = this._fArrowLeft_btn;

		this._updateRoomsPosition(0, true, lValueGrow_bl);

		this._redrowRoomBrowsingProgress();
	}

	_redrowRoomBrowsingProgress()
	{
		// must be overridden
	}

	__calcRoomButtonsContainerPosition()
	{
		let lX_num = -GUSLobbyRoomButton.INIT_X - GUSLobbyRoomButton.VISIBLE_OFFSET_X * (Math.min(this._fStakesAmount_int, GUSLobbyRoomButton.VISIBLE_AMOUNT) - 1) / 2;
		let lY_num = 0;

		return new PIXI.Point(lX_num, lY_num);
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
		let lButton_rb = this.__provideRoomBtninstance(aIndex_int);
		lButton_rb.on(GUSLobbyRoomButtons.EVENT_LOBBY_ROOM_STAKE_SELECTED, this.emit, this);

		this._fRoomButtons[aIndex_int] = lButton_rb;
		return lButton_rb;
	}

	__provideRoomBtninstance(aIndex_int)
	{
		return new GUSLobbyRoomButton(aIndex_int);
	}

	_buttonsContainerClear()
	{
		while (this._fRoomButtonsContainer.children.length != 0)
		{
			this._fRoomButtonsContainer.removeChild(this._fRoomButtonsContainer.children[0]);
		}

		this._fRoomButtonsContainer.mask = this.__areaMask;
	}

	get __areaMask()
	{
		let lMask_g = this._fAreaMask_g;
		if (lMask_g)
		{
			this._fRoomButtonsContainer.addChild(lMask_g);
			return lMask_g;
		}

		lMask_g = this._fAreaMask_g = this._fRoomButtonsContainer.addChild(this.__provideAreaMaskInstance());
		return lMask_g;
	}

	__provideAreaMaskInstance()
	{
		let lMask_g = new PIXI.Graphics();
		lMask_g.beginFill(0x000000).drawRect(-440, -150, 880, 300).endFill();
		lMask_g.position.set(436, 0);

		return lMask_g;
	}

	_moveLeft()
	{
		this._fCurrentPosition_int -= GUSLobbyRoomButtons.ROOMS_SKIPPED_PER_CLICK_COUNT;

		if(this._fCurrentPosition_int < 0)
		{
			this._fCurrentPosition_int = 0;
		}

		this._updateRoomsPosition(this._fCurrentPosition_int);
		this._redrowRoomBrowsingProgress();
	}

	_moveRight()
	{
		this._fCurrentPosition_int += GUSLobbyRoomButtons.ROOMS_SKIPPED_PER_CLICK_COUNT;
		
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
		this.__updateRoomsCoordinates(aIndex_int, aSkipAnimation_bl, aIsReverseGrow_bl);
		
		if (this.__isArrowButtonsSupported)
		{
			this._fArrowLeft_btn.enabled = this._fCurrentPosition_int > 0;
			this._fArrowRight_btn.enabled = this._fCurrentPosition_int < this._fStakesAmount_int - GUSLobbyRoomButton.VISIBLE_AMOUNT;
		}

		this._validateArrowButtonsGlow();
	}

	__updateRoomsCoordinates(aIndex_int, aSkipAnimation_bl, aIsReverseGrow_bl)
	{
		for (let i = 0; i < this._fRoomButtons.length; i++)
		{
			this._fRoomButtons[i].updatePosition(aIndex_int, aSkipAnimation_bl, aIsReverseGrow_bl);
		}
	}

	_areAllVisibleRoomsDisabled()
	{
		let n = Math.min(this._fRoomButtons.length, this._fCurrentPosition_int + GUSLobbyRoomButton.VISIBLE_AMOUNT);
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
		let start = this._fCurrentPosition_int + GUSLobbyRoomButton.VISIBLE_AMOUNT;
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
		if (!this.__isArrowButtonsSupported)
		{
			return;
		}

		if (this._areAllVisibleRoomsDisabled())
		{
			this._fArrowLeft_btn.setGlowable(this._isAnyRoomEnabledToTheLeft());
			this._fArrowRight_btn.setGlowable(this._isAnyRoomEnabledToTheRight());
		}
		else
		{
			this._fArrowLeft_btn.setGlowable(false);
			this._fArrowRight_btn.setGlowable(false);
		}
	}

	destroy()
	{
		APP.playerController.off(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.lobbyStateController.off(GUSLobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyScreenVisibilityChanged, this);

		this._fRoomButtons = null;

		this._fRoomButtonsContainer && this._buttonsContainerClear();

		this._fRoomButtonsContainer && this._fRoomButtonsContainer.destroy();
		this._fRoomButtonsContainer = null;

		this._fUpdateSubscribed_bln = null;

		this._fAreaMask_g = null;

		super.destroy();
	}
}

export default GUSLobbyRoomButtons;