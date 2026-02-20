import Sprite from '../../../unified/view/base/display/Sprite';
import GUSLobbyRoomButtons from '../uis/lobby_room_buttons/GUSLobbyRoomButtons';
import { APP } from '../../../unified/controller/main/globals';
import GUSLobbyPlayerController from '../../controller/custom/GUSLobbyPlayerController';
import GUSLobbyTooltipsView from '../uis/tooltips/GUSLobbyTooltipsView';
import Button from '../../../unified/view/ui/Button';
import GUSLobbyPlayerNameView from '../uis/GUSLobbyPlayerNameView';
import PlayerInfo from '../../../unified/model/custom/PlayerInfo';

class GUSLobbyView extends Sprite
{
	static get EVENT_ON_LAUNCH_GAME_CLICKED()						{return "onLaunchGameBtnClicked";}
	static get EVENT_ON_LOBBY_PROFILE_CLICKED()						{return "onLobbyViewProfileButtonClicked";}
	static get EVENT_ON_PLAYER_STATS_UPDATED()						{return "EVENT_ON_PLAYER_STATS_UPDATED";}

	static get EVENT_LOBBY_ROOM_STAKE_SELECTED()					{return GUSLobbyRoomButtons.EVENT_LOBBY_ROOM_STAKE_SELECTED;}
	
	updateLobbyData()
	{
		this._updateLobbyData();
	}

	show()
	{
		super.show()
	}

	hide()
	{
		super.hide()
	}

	refreshIndicatorsView()
	{
	}

	onWeaponsUpdated(aData_arr)
	{
	}

	constructor(aPlayer_obj)
	{
		super();

		this._fPlayer_obj = aPlayer_obj;
		this._fUserDetailsContainer_spr = null;
		
		/**
		 * @type {PlayerInfo}
		 * @private
		 */
		this._fPlayerInfo_pi = APP.playerController.info;
		this._fLobbyRoomButtons_lrb = null;
		this._fRoomButtonsContainer_sprt = null;
		this._fTooltipsContainer_sprt = null;
	}

	init()
	{
		this._addback();
		this._addCaptions();
		this._addUserDetails();
		this._addBrand();

		this._fRoomButtonsContainer_sprt = this.addChild(new Sprite());

		if (!APP.isTutorialSupported)
		{
			this._fTooltipsContainer_sprt = this.addChild(new Sprite());
			APP.tooltipsController.initView(this._tooltipsView);
		}

		APP.playerController.on(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.playerController.on(GUSLobbyPlayerController.EVENT_ON_TOLL_TIP_STATE_CHANGE, this._onPlayerInfoUpdated, this);
	}

	_addback()
	{
	}

	_addCaptions()
	{
	}

	_addBrand()
	{
	}

	_updateLobbyData()
	{
		this._fStakes_num_arr = this._fPlayerInfo_pi.stakes;

		switch (APP.appParamsInfo.roomsOrder)
		{
			case "ASC":
				break;
			case "DESC":
			default:
				this._fStakes_num_arr.reverse();
				break;
		}

		this._updateNickName();
		this._updateLobbyRoomButtons();
	}

	//TOOLTIPS...
	get _tooltipsView()
	{
		return this._fTooltipsView_ltv || (this._fTooltipsView_ltv = this._initTooltipsView());
	}

	_initTooltipsView()
	{
		let l_ltv = this.__provideTooltipsInstance();
		this._fTooltipsContainer_sprt.addChild(l_ltv);
		
		return l_ltv;
	}

	__provideTooltipsInstance()
	{
		return new GUSLobbyTooltipsView;
	}
	//...TOOLTIPS

	//ROOM_BUTTONS...
	get _lobbyRoomButtons()
	{
		return this._fLobbyRoomButtons_lrb || (this._fLobbyRoomButtons_lrb = this._initLobbyRoomButtons());
	}

	_initLobbyRoomButtons()
	{
		let l_lrb = this._fRoomButtonsContainer_sprt.addChild(this.__provideRoomButtonsInstance());
		
		l_lrb.on(GUSLobbyRoomButtons.EVENT_LOBBY_ROOM_STAKE_SELECTED, this._onLobbyRoomSelected, this);
		return l_lrb;
	}

	__provideRoomButtonsInstance()
	{
		let l_lrb = new GUSLobbyRoomButtons();
		return l_lrb;
	}

	_updateLobbyRoomButtons()
	{
		this._fStakes_num_arr && this._lobbyRoomButtons.update(this._fStakes_num_arr);
	}

	_onLobbyRoomSelected(event)
	{
		this.emit(GUSLobbyView.EVENT_ON_LAUNCH_GAME_CLICKED, { stake: event.value });
	}
	//...ROOM_BUTTONS

	//USER_DETAILS...
	_addUserDetails()
	{
		this._addUserDetailsContainer();
		this._addUserDetailsBackground();
		this._addUserDetailsCaptions();
		this._addUserDetailsButtons();
	}

	_addUserDetailsContainer()
	{
		this._fUserDetailsContainer_spr = this.addChild(new Sprite());
	}

	_addUserDetailsBackground()
	{
	}

	_addUserDetailsCaptions()
	{
		this._fUserDetailsCaptionsContainer = this._fUserDetailsContainer_spr.addChild(new Sprite());

		this._addUserName();
	}

	_addUserDetailsButtons()
	{
		this._fEdit_btn = this._fUserDetailsContainer_spr.addChild(this.__provideEditUserDetailsButtonInstance());
		this._fEdit_btn.on("pointerclick", this._onProfileBtnClicked, this);
	}

	__provideEditUserDetailsButtonInstance()
	{
		return new Button();
	}

	_onProfileBtnClicked()
	{
		this.emit(GUSLobbyView.EVENT_ON_LOBBY_PROFILE_CLICKED);
	}

	_addUserName()
	{
		let lPlayerNameView_lpv = this._fPlayerNameView_lpv = this._fUserDetailsCaptionsContainer.addChild(new GUSLobbyPlayerNameView());

		let lProps_obj = this.__nickNameProps;
		lPlayerNameView_lpv.init(this._fPlayer_obj.nickname, lProps_obj.pivot, lProps_obj.fontName, lProps_obj.fontSize, lProps_obj.maxWidth);
		lPlayerNameView_lpv.position.set(lProps_obj.position.x, lProps_obj.position.y);
	}

	get __nickNameProps()
	{
		return {
			pivot: new PIXI.Point(0, 0.5),
			fontName: "sans-serif",
			position: new PIXI.Point(0, 0)
		}
	}

	_updateNickName(aNickname_str)
	{
		if (this._fPlayerNameView_lpv)
		{
			this._fPlayerNameView_lpv.updateName(aNickname_str || APP.lobbyScreen.playerInfo.nickname);
		}
	}

	//...USER_DETAILS

	destroy()
	{
		this._fPlayer_obj = null;
		this._fUserDetailsContainer_spr = null;
		this._fPlayerInfo_pi = null;
		this._fLobbyRoomButtons_lrb = null;
		this._fStakes_num_arr = null;
		this._fEdit_btn = null;
		this._fPlayerNameView_lpv = null;
		this._fRoomButtonsContainer_sprt = null;
		this._fTooltipsContainer_sprt = null;
		this._fUserDetailsCaptionsContainer = null;
		
		super.destroy();
	}

	_onPlayerInfoUpdated(event)
	{
		if (event.data[PlayerInfo.KEY_NICKNAME])
		{
			this._updateNickName(event.data[PlayerInfo.KEY_NICKNAME].value);
		}
	}
}

export default GUSLobbyView