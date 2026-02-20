import DialogInfo from '../DialogInfo';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

export const PLAYER_PARAMS = {
	NICK: 					"nickname",
	IS_KICK_IN_PROGRESS: 	"isKickInProgress",
	IS_KICK_ALLOWED: 		"isKickAllowed",
	IS_KICKED: 				"isKicked",
	READY: 					"ready",
	STATUS:					"status",
	IS_OWNER:				"isOwner"
}

export const PLAYER_CAF_STATUS = {
	INVITED:				"INVITED",
    ACCEPTED:				"ACCEPTED",
    REJECTED:				"REJECTED",
    KICKED:					"KICKED",
    LOADING:				"LOADING",
    READY:					"READY",
    WAITING:				"WAITING",
    PLAYING:				"PLAYING"
}

class BattlegroundCafRoomManagerDialogInfo extends DialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fBuyInCost_num = undefined;
		this._fIsPlayerSitOutState_bl = false;
		this._fIsGameStateQualify_bl = false;
		this._fIsReadyConfirmationTriggered_bl = false;
		this._fIsCancelReadyTriggered_bl = false;

		this._fPlayersListData_obj_arr = null;
		this._readyConfirmed = false;

		this._reinviteCount_obj_arr = [];
		this._readyBtnCliked = false; 
		this._cancelButtonClicked = false;
	}


	get cancelButtonClicked()
	{
		return this._cancelButtonClicked;
	}

	
	set cancelButtonClicked(aValue)
	{
		 this._cancelButtonClicked = aValue;
	}


	get readyButtonClicked()
	{
		return this._readyBtnCliked;
	}

	
	set readyButtonClicked(aValue)
	{
		 this._readyBtnCliked = aValue;
	}

	get reinviteDataCount(){
		return this._reinviteCount_obj_arr;
	}

	setBuyInCost(aBuyInCost_num)
	{
		this._fBuyInCost_num = aBuyInCost_num;
	}

	getBuyInCost()
	{
		return this._fBuyInCost_num;
	}

	
	setIsReadyConfirmed(aBValue)
	{
		return this._readyConfirmed = aBValue;
	}

	updateFriendsListData(friends_obj_arr)
	{
		this._friends_obj_arr = friends_obj_arr;
	}

	updatePlayersListData(aObserversList_obj_arr)
	{

		if (!aObserversList_obj_arr || !aObserversList_obj_arr.length)
		{
			this._fPlayersListData_obj_arr = null;
			return;
		}

		/*for(let s=0; s<aObserversList_obj_arr.length; s++){
			aObserversList_obj_arr[s].status = PLAYER_CAF_STATUS.INVITED;
		}

		let statuses = [PLAYER_CAF_STATUS.INVITED, PLAYER_CAF_STATUS.WAITING, PLAYER_CAF_STATUS.REJECTED, PLAYER_CAF_STATUS.LOADING, PLAYER_CAF_STATUS.ACCEPTED ]

		const updateObj = statuses[Utils.random(0,4)];
		const updateObj2 = statuses[Utils.random(0,4)];

		aObserversList_obj_arr.push({nickname:"frontend7",isKicked:false, status:updateObj});
		aObserversList_obj_arr.push({nickname:"frontend8",isKicked:false, status:updateObj2});*/
		
		let l_pi = {};


		let lKickedPlayers_obj_arr = [];
		let lActualNicks_str_arr = [];
		for (let i=0; i<aObserversList_obj_arr.length; i++)
		{
			let lCurObserverData_obj = aObserversList_obj_arr[i];
			let lCurPlayerData_obj = this._getPlayerData(lCurObserverData_obj.nickname);

			lActualNicks_str_arr.push(lCurObserverData_obj.nickname);

		
			
			if (lCurPlayerData_obj)
			{
				lCurPlayerData_obj[PLAYER_PARAMS.STATUS] = lCurObserverData_obj.status;
				lCurPlayerData_obj[PLAYER_PARAMS.IS_OWNER] = lCurObserverData_obj.isOwner;
				if (lCurObserverData_obj.isKicked)
				{
					this._removePLayerData(lCurPlayerData_obj);
					lKickedPlayers_obj_arr.push(lCurPlayerData_obj);
				}
			}
			else
			{
				lCurPlayerData_obj = {};
				lCurPlayerData_obj[PLAYER_PARAMS.NICK] = lCurObserverData_obj.nickname;
				lCurPlayerData_obj[PLAYER_PARAMS.STATUS] = lCurObserverData_obj.status;
				lCurPlayerData_obj[PLAYER_PARAMS.IS_OWNER] = lCurObserverData_obj.isOwner;

				this._fPlayersListData_obj_arr = this._fPlayersListData_obj_arr || [];

				if ((lCurObserverData_obj.nickname == l_pi.nickname) && l_pi.isCAFRoomManager)
				{
					this._fPlayersListData_obj_arr.unshift(lCurPlayerData_obj);
				}
				else if (lCurObserverData_obj.isKicked)
				{
					lKickedPlayers_obj_arr.push(lCurPlayerData_obj);
				}
				else
				{
					this._fPlayersListData_obj_arr.push(lCurPlayerData_obj);
				}
			}
			
			lCurPlayerData_obj[PLAYER_PARAMS.IS_KICKED] = lCurObserverData_obj.isKicked;
			if (lCurPlayerData_obj[PLAYER_PARAMS.IS_KICKED])
			{
				lCurPlayerData_obj[PLAYER_PARAMS.IS_KICK_IN_PROGRESS] = false;
			}

			if(lCurObserverData_obj.status == PLAYER_PARAMS.READY && !lCurPlayerData_obj[PLAYER_PARAMS.IS_KICKED] ){
				lCurPlayerData_obj[PLAYER_PARAMS.READY] = true;

			}else{
				lCurPlayerData_obj[PLAYER_PARAMS.READY] = false;
			}

			/*lCurPlayerData_obj[PLAYER_PARAMS.READY] = !lCurPlayerData_obj[PLAYER_PARAMS.IS_KICKED]
														&& l_pi.isSeaterOnServer(lCurPlayerData_obj[PLAYER_PARAMS.NICK])
														&& l_pi.isSeaterBattlegroundBuyInConfirmed(lCurPlayerData_obj[PLAYER_PARAMS.NICK]);*/

		}

		this._fPlayersListData_obj_arr = this._fPlayersListData_obj_arr.concat(lKickedPlayers_obj_arr);

		for (let i=0; i<this._fPlayersListData_obj_arr.length; i++)
		{
			if (lActualNicks_str_arr.indexOf(this._fPlayersListData_obj_arr[i].nickname) >= 0)
			{
				// player is actual, keep in the list
			}
			else
			{
				this._fPlayersListData_obj_arr.splice(i, 1);
				i--;
			}
		}
		for( let p =0; p<this._fPlayersListData_obj_arr.length; p++)
		{
		 	const nickname = this._fPlayersListData_obj_arr[p].nickname;
			if(!this._reinviteCount_obj_arr[nickname])
			{
					this._reinviteCount_obj_arr[nickname] = 0;
			}
		}
		

		this.updateKickAllowedState();
	}

	updatePlayerKickProgressState(aPlayerNickname_str, aIskickInProgress_bl)
	{
		if (!this._fPlayersListData_obj_arr || !this._fPlayersListData_obj_arr.length)
		{
			throw new Error(`Attempt to kick player that is not in players list, nickname: ${aPlayerNickname_str}`);
		}

		let lCurPlayerData_obj = this._getPlayerData(aPlayerNickname_str);
		lCurPlayerData_obj[PLAYER_PARAMS.IS_KICK_IN_PROGRESS] = !!aIskickInProgress_bl;	
	}

	updateKickAllowedState ()
	{
		if (!this._fPlayersListData_obj_arr || !this._fPlayersListData_obj_arr.length)
		{
			return;
		}

		for (let i=0; i<this._fPlayersListData_obj_arr.length; i++)
		{
			let lCurPlayerData_obj = this._fPlayersListData_obj_arr[i];
			lCurPlayerData_obj[PLAYER_PARAMS.IS_KICK_ALLOWED] = !this.isGameStateQualify;
		}
	}

	resetPlayersKickProgressState()
	{
		if (!this._fPlayersListData_obj_arr || !this._fPlayersListData_obj_arr.length)
		{
			return;
		}

		for (let i=0; i<this._fPlayersListData_obj_arr.length; i++)
		{
			let lCurPlayerData_obj = this._fPlayersListData_obj_arr[i];
			lCurPlayerData_obj[PLAYER_PARAMS.IS_KICK_IN_PROGRESS] = false;
		}
	}

	get playersListData()
	{
		return this._fPlayersListData_obj_arr;
	}

	get friendsListData()
	{
		return this._friends_obj_arr;
	}

	get notKickedPlayersAmount()
	{
		if (!this._fPlayersListData_obj_arr || !this._fPlayersListData_obj_arr.length)
		{
			return 0;
		}

		let l_int = 0;
		for (let i=0; i<this._fPlayersListData_obj_arr.length; i++)
		{
			let lCurPlayerData_obj = this._fPlayersListData_obj_arr[i];
			if (lCurPlayerData_obj[PLAYER_PARAMS.IS_KICKED])
			{
				// skip kicked player
			}
			else
			{
				l_int++;
			}
		}

		return l_int;
	}

	get readyPlayersAmount()
	{
		if (!this._fPlayersListData_obj_arr || !this._fPlayersListData_obj_arr.length)
		{
			return 0;
		}

		let l_int = 0;
		for (let i=0; i<this._fPlayersListData_obj_arr.length; i++)
		{
			let lCurPlayerData_obj = this._fPlayersListData_obj_arr[i];
			if (lCurPlayerData_obj[PLAYER_PARAMS.STATUS] == PLAYER_CAF_STATUS.READY)
			{
				l_int++;
			}
		}

		return l_int;
	}

	_removePLayerData(aPlayerData_obj)
	{
		if (!this._fPlayersListData_obj_arr || !this._fPlayersListData_obj_arr.length)
		{
			return;
		}

		let lIndex_int = this._fPlayersListData_obj_arr.indexOf(aPlayerData_obj);
		if (lIndex_int >= 0)
		{
			this._fPlayersListData_obj_arr.splice(lIndex_int, 1);
		}
	}

	_getPlayerData(aNickname_str)
	{
		if (!this._fPlayersListData_obj_arr || !this._fPlayersListData_obj_arr.length)
		{
			return null;
		}

		for (let i=0; i<this._fPlayersListData_obj_arr.length; i++)
		{
			let lCurPlayerData_obj = this._fPlayersListData_obj_arr[i];
			if (lCurPlayerData_obj[PLAYER_PARAMS.NICK] === aNickname_str)
			{
				return lCurPlayerData_obj;
			}
		}

		return null;
	}

	get isPlayerSitOutState()
	{
		return this._fIsPlayerSitOutState_bl;
	}

	set isPlayerSitOutState(aState_bl)
	{
		this._fIsPlayerSitOutState_bl = aState_bl;
	}

	get isGameStateQualify()
	{
		return this._fIsGameStateQualify_bl;
	}

	set isGameStateQualify(aState_bl)
	{
		this._fIsGameStateQualify_bl = aState_bl;
	}

	get isReadyConfirmationTriggered()
	{
		return this._fIsReadyConfirmationTriggered_bl;
	}

	set isReadyConfirmationTriggered(aState_bl)
	{
		this._fIsReadyConfirmationTriggered_bl = aState_bl;
	}

	get isCancelReadyTriggered()
	{
		return this._fIsCancelReadyTriggered_bl;
	}

	set isCancelReadyTriggered(aState_bl)
	{
		this._fIsCancelReadyTriggered_bl = aState_bl;
	}

	get isReadyConfirmationInProgress()
	{
		return false;
	}

	get isReadyConfirmed()
	{
		return this._readyConfirmed;
	}

	
	get isDisabled(){
		return APP.isCAFMode?false:true;
	}


}

export default BattlegroundCafRoomManagerDialogInfo