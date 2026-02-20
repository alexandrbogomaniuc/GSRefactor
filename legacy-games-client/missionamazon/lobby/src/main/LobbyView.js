import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Button from '../ui/LobbyButton';
import TextField from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import * as FEATURES from '../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';

import EditProfileScreenController from '../controller/uis/custom/secondary/edit_profile/EditProfileScreenController';
import LobbyPlayerController from '../controller/custom/LobbyPlayerController';
import PlayerInfo from '../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import LobbyLogoView from '../view/uis/custom/LobbyLogoView';
import LobbyPlayerNameView from '../view/uis/custom/LobbyPlayerNameView';
import LobbyRoomButtons from '../components/lobby_room_buttons/LobbyRoomButtons';
import ProfileAvatar from '../components/profile/ProfileAvatar';

class LobbyView extends Sprite
{
	static get LOBBY_PLAYER_INFO_BLOCK_Y()							{ return 162 }
	static get LOBBY_PLAYER_INFO_BLOCK_HEIGHT()						{ return 80 }

	static get EVENT_ON_LAUNCH_GAME_CLICKED()						{return "onLaunchGameBtnClicked";}
	static get EVENT_ON_LOBBY_PROFILE_CLICKED()						{return "onLobbyViewProfileButtonClicked";}
	static get EVENT_ON_PLAYER_STATS_UPDATED()						{return "EVENT_ON_PLAYER_STATS_UPDATED";}
	
	static get EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK() 			{return LobbyRoomButtons.EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK};
	static get EVENT_LOBBY_ROOM_STAKE_SELECTED()					{return LobbyRoomButtons.EVENT_LOBBY_ROOM_STAKE_SELECTED};
	

	get profileController()
	{
		return APP.secondaryScreenController.editProfileScreenController;
	}

	static get AVATAR_TEXTURES()
	{
		initTexturesIfRequired();
		return avatar_textures;
	}

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
		this._updateStats();
	}

	onWeaponsUpdated(aData_arr)
	{
		this._onWeaponsUpdated(aData_arr);
	}

	constructor (aPlayer_obj)
	{
		super();

		this._fPlayer_obj = aPlayer_obj;
		this._fUserDetailsContainer_spr = null;
		this._fPlayerInfo_pi = APP.playerController.info;
		this._fLobbyRoomButtons_lrb = null;
		this._fRoomButtonsContainer_sprt = null;
		this._fChooseLobbyText_cta = null;
	}

	init()
	{
		this._fRoomButtonsContainer_sprt = this.addChild(new Sprite());
		this._addCaptions();
		this._addUserDetails();
		this._addBrand();

		APP.playerController.on(LobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.playerController.on(LobbyPlayerController.EVENT_ON_TOLL_TIP_STATE_CHANGE, this._onPlayerInfoUpdated, this);
	}

	_addCaptions()
	{
		let logo = this.addChild(new LobbyLogoView());
		logo.position.set(0, -196);

		// let lLobbyRoomsCaption = this.addChild(I18.generateNewCTranslatableAsset('TALobbyRoomsCaption'));
		// lLobbyRoomsCaption.position.set(0, -140);

		if (APP.isMobile)
		{
			logo.y -= 10;
			// lLobbyRoomsCaption.y -= 6;
		}
		
		this._createChooseLobbyText();
	}

	_addBrand()
	{	
		let lBrand = this.addChild(I18.generateNewCTranslatableAsset('TAPreloaderBrand'));
		lBrand.position.set(-364, -202);
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

		this._updateStats();
		this._updateNickName();
		this._updateLobbyRoomButtons();
	}

	//ROOM_BUTTONS...

	_onWeaponsUpdated(aData_arr)
	{
		this._lobbyRoomButtons.updateWeapons(aData_arr);
	}

	get _lobbyRoomButtons()
	{
		return this._fLobbyRoomButtons_lrb || (this._fLobbyRoomButtons_lrb = this._initLobbyRoomButtons());
	}

	_initLobbyRoomButtons()
	{
		let l_lrb = this._fRoomButtonsContainer_sprt.addChild(new LobbyRoomButtons());
		l_lrb.position.set(161, 50);
		l_lrb.on(LobbyRoomButtons.EVENT_LOBBY_ROOM_STAKE_SELECTED, this._onLobbyRoomSelected, this);
		l_lrb.on(LobbyRoomButtons.EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK, this._onLobbyRoomWeaponsIndicatorClick, this);
		l_lrb.on(LobbyRoomButtons.EVENT_LOBBY_ROOM_ENABALED_STATE_CHANGED, this._onLobbyRoomEnabledStateChanged, this);
		l_lrb.visible = false; 
		return l_lrb;
	}

	_updateLobbyRoomButtons()
	{
		this._fStakes_num_arr && this._lobbyRoomButtons.update(this._fStakes_num_arr);
	}

	_onLobbyRoomSelected(event)
	{
		this.emit(LobbyView.EVENT_ON_LAUNCH_GAME_CLICKED, {stake:event.value});
	}

	_onLobbyRoomWeaponsIndicatorClick(event)
	{
		this.emit(LobbyView.EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK, {stake:event.value});
	}
	
	_onLobbyRoomEnabledStateChanged(event)
    {
        this._updateChooseLobbyText(event.isDisabledState);
    }
 
    _updateChooseLobbyText(aIsDisabledState_bln)
    {
        aIsDisabledState_bln
            ? this._fChooseLobbyText_cta.position.set(0, 55 + (APP.isMobile ? -15 : 0))
            : this._fChooseLobbyText_cta.position.set(0, 0 + (APP.isMobile ? -10 : 0))
    }

	_createChooseLobbyText()
	{
		this._fChooseLobbyText_cta = this.addChild(I18.generateNewCTranslatableAsset("TALobbyChooseRoomLabel"));
        this._fChooseLobbyText_cta.position.set(0, 0 + (APP.isMobile ? -10 : 0))
	}
	//...ROOM_BUTTONS

	//USER_DETAILS...

	_addUserDetails()
	{
		this._fUserDetailsContainer_spr = this.addChild(new Sprite());
		this._fUserDetailsContainer_spr.position.set(-480, 171 + (APP.isMobile ? -25 : 0));

		this._addUserDetailsBackground();
		this._addUserDetailsCaptions();
		this._addUserDetailsAvatar();
		this._addUserDetailsStat();
		this._addUserDetailsButtons();
	}

	_addUserDetailsBackground()
	{
		let l_s = this._fUserDetailsContainer_spr.addChild(APP.library.getSprite("lobby/player_info_background"));
		l_s.anchor.set(0, 0);
		l_s.position.x = -288;
	}

	_getPlayerInfosOffsetY()
	{
		if(APP.isMobile)
		{
			return 3;
		}

		return 0;
	}

	_addUserDetailsCaptions()
	{
		this._fUserDetailsCaptionsContainer = this._fUserDetailsContainer_spr.addChild(new Sprite());
		this._fUserDetailsCaptionsContainer.position.set(0, 3);

		let lTotalKillsLabel_cta = this._fUserDetailsCaptionsContainer.addChild(I18.generateNewCTranslatableAsset("TALobbyTotalKillsLabel"));
		lTotalKillsLabel_cta.position.set(241, 25 + this._getPlayerInfosOffsetY());
		lTotalKillsLabel_cta.visible = false;
		this._fTotalKillsLabel_cta = lTotalKillsLabel_cta;

		let lGameRoundsLabel_cta = this._fUserDetailsCaptionsContainer.addChild(I18.generateNewCTranslatableAsset("TALobbyGameRoundsLabel"));
		lGameRoundsLabel_cta.position.set(248, 42 + this._getPlayerInfosOffsetY());
		lGameRoundsLabel_cta.visible = false;
		this._fGameRoundsLabel_cta = lGameRoundsLabel_cta;

		let lPlayerNameView_lpv = this._fPlayerNameView_lpv = this._fUserDetailsCaptionsContainer.addChild(new LobbyPlayerNameView());
		lPlayerNameView_lpv.init(this._fPlayer_obj.nickname, new PIXI.Point(0, 0));
		lPlayerNameView_lpv.position.set(24, 22);
	}

	_addUserDetailsAvatar()
	{
		this._fAvatar_sprt = this._fUserDetailsContainer_spr.addChild(new Sprite());
		this._fAvatar_sprt.anchor.set(0.5);
		this._fAvatar_sprt.position.set(165, 44);

		this._updateAvatarAtFirstTime();

		this.profileController.on(EditProfileScreenController.EVENT_ON_AVATAR_CHANGED, this._updateAvatar, this);
	}

	_updateAvatarAtFirstTime()
	{
		let lAvatarRenderTexture = APP.secondaryScreenController.editProfileScreenController.getAvatarRenderTexture();
		if (lAvatarRenderTexture)
		{
			this._updateAvatar();
			return;
		}

		var avatar = APP.lobbyScreen.playerInfo.avatar;

		let container = new Sprite();
		container.addChild(this._getAvatarSprite("backs_avatar/back_" + avatar.back));
		container.addChild(this._getAvatarSprite("heroes_avatar/hero_" + avatar.hero));
		container.addChild(this._getAvatarSprite("rims_avatar/rim_" + avatar.border));

		let bounds = container.getBounds();
		
		let brt = new PIXI.BaseRenderTexture(bounds.width, bounds.height, PIXI.SCALE_MODES.NEAREST, 2);
		var renderTexture = new PIXI.RenderTexture(brt);
		container.position.set(bounds.width/2, bounds.height/2);
		APP.stage.renderer.render(container, renderTexture);

		container.destroy();

		this._fAvatar_sprt.texture = renderTexture;
	}

	_getAvatarSprite(aSrc_str)
	{
		let lTexture_t = this._getAvatarTexture(aSrc_str);
		if (lTexture_t)
		{
			let l_spr = new Sprite();
			l_spr.texture = lTexture_t;

			return l_spr;
		}
		else
		{
			throw new Error (`Cannot find texture for avatar ${aSrc_str}`);
		}
	}

	_getAvatarTexture(aName_str)
	{
		for (let i = 0; i < ProfileAvatar.AVATAR_TEXTURES.length; i++)
		{
			if(ProfileAvatar.AVATAR_TEXTURES[i]._atlasName == aName_str)
			{
				return ProfileAvatar.AVATAR_TEXTURES[i];
			}
		}

		return null;
	}

	_updateAvatar()
	{
		this._fAvatar_sprt.scale.set(0.3);
		this._fAvatar_sprt.texture = this.profileController.getAvatarRenderTexture();
	}

	_addUserDetailsStat()
	{
		this._fUserDetailsStatContainer_spr = this._fUserDetailsContainer_spr.addChild(new Sprite());
		this._fUserDetailsStatContainer_spr.position.set(0, 0);

		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 12,
			fill: 0xffffff,
			align: "center",
			letterSpacing: 1.3,
			padding: 5
		};

		this._fTotalKill_tf = this._fUserDetailsStatContainer_spr.addChild(new TextField(lStyle_obj));
		this._fTotalKill_tf.maxWidth = 80;
		this._fTotalKill_tf.anchor.set(0, 1);
		this._fTotalKill_tf.visible = false;
		let l_cta = this._fTotalKillsLabel_cta;

		this._fTotalKill_tf.position.set(l_cta.position.x + 27, l_cta.position.y + 11 + (APP.isMobile ? -1 : 0));
		this._fTotalKill_tf.alpha = 0.6;

		this._fRounds_tf = this._fUserDetailsStatContainer_spr.addChild(new TextField(lStyle_obj));
		this._fRounds_tf.maxWidth = 80;
		this._fRounds_tf.anchor.set(0, 1);
		this._fRounds_tf.visible = false;
		l_cta = this._fGameRoundsLabel_cta;
		this._fRounds_tf.position.set(l_cta.position.x + 35, l_cta.position.y + 11 + (APP.isMobile ? -1 : 0));
		this._fRounds_tf.alpha = 0.6;

		this._updateStats();
	}

	_updateStats()
	{
		if(this._fTotalKill_tf)								this._fTotalKill_tf.text = 			new String(this._fPlayer_obj.kills).replace(/(\d)(?=(\d\d\d)+([^\d]|$))/g, '$1,');
		if(this._fRounds_tf) 								this._fRounds_tf.text =				new String(this._fPlayer_obj.rounds).replace(/(\d)(?=(\d\d\d)+([^\d]|$))/g, '$1,');

		this.emit(LobbyView.EVENT_ON_PLAYER_STATS_UPDATED);
	}

	_addUserDetailsButtons()
	{
		this._fEdit_btn = this._fUserDetailsContainer_spr.addChild(new Button("lobby/edit_btn", "TALobbyEditProfileBtnLabel", true));
		this._fEdit_btn.on("pointerclick", this._onProfileBtnClicked, this);
		this._fEdit_btn.position.set(63, 60);
		this._fEdit_btn.caption.position.set(-8, -4);

		if (FEATURES.IE)
		{
			this._fEdit_btn.caption.pivot.set(-1, -1);
		}
	}

	_onProfileBtnClicked()
	{
		this.emit(LobbyView.EVENT_ON_LOBBY_PROFILE_CLICKED);
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
		this._lobbyRoomButtons = null;
		this._fUserDetailsCaptionsContainer = null;
		this._fAvatar_sprt = null;
		this._fUserDetailsStatContainer_spr = null;
		this._fEdit_btn = null;
		this._fPlayerNameView_lpv= null;
		this._fRoomButtonsContainer_sprt = null;
		this._fIntervalTimer_tmr = null;
		this._fTotalKill_tf = null;
		this._fRounds_tf = null;

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

export default LobbyView;