import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField, { DEFAULT_SYSTEM_FONT } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { DropShadowFilter } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';
import Button from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { PLAYER_PARAMS, PLAYER_CAF_STATUS } from '../../../../../model/uis/custom/dialogs/custom/BattlegroundCafRoomManagerDialogInfo';

class BattlegroundPrivatePlayersListItem extends Sprite
{
	static get EVENT_ON_KICK_CLICKED () { return "EVENT_ON_KICK_CLICKED" };
	static get EVENT_ON_CANCEL_KICK_CLICKED () { return "EVENT_ON_CANCEL_KICK_CLICKED" };

	update(aData_obj, reinviteCountData)
	{
		this._fItemData_obj = this._validateItemData(aData_obj);
		this._fReinvite_countData = reinviteCountData;

		this._updateView();
	}

	get nickName()
	{
		return this._fItemData_obj[PLAYER_PARAMS.NICK] || undefined;
	}

	constructor(aData_obj)
	{
		super();

		this._fBaseContainer_sprt = null;
		this._fManagerContainer_sprt = null;
		this._fContentContainer_sprt = null;
		this._fManagerBaseStroke_sprt = null;
		this._fManagerBase_sprt = null;
		this._fFriendBase_sprt = null;
		this._fPositionText_tf = null;
		this._fNicknameText_tf = null;
		this._fCrown_sprt = null;
		this._fKick_btn = null;
		this._fPlayerNotReady_sprt = null;
		this._fPlayerRedIcon_sprt = null;
		this._fPlayerZZZSprite = null;

		this._fItemData_obj = this._validateItemData(aData_obj);

		this._init();

		this._updateView();
	}

	_validateItemData(aData_obj)
	{
		return aData_obj || { [PLAYER_PARAMS.IS_KICKED]: true };
	}

	_init()
	{
		this._fBaseContainer_sprt = this.addChild(new Sprite());
		this._fContentContainer_sprt = this.addChild(new Sprite());

		this._addBack();
		this._addHrLinePosition();
		this._addHrLineFriend();
		this._addPosition();
		this._addNickName();
		this._addCrown();
		this._addClock();
		this._addPlayerReady();
		this._addPlayerNotReadyIcon();
		this._addRedXIcon();
		this._addZZZIcon();
		this._addKickButton();
		this._addInviteButton();
		
	}

	_addBack()
	{
		this._fManagerContainer_sprt = this._fBaseContainer_sprt.addChild(new Sprite());

		this._fManagerBaseStroke_sprt = this._fManagerContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/row_manager"));
		this._fManagerBaseStroke_sprt.position.set(0, 2);

		this._fFriendBase_sprt = this._fBaseContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/row_not_manager"));
		this._fFriendBase_sprt.position.set(0, 2);
	}
	
	_addHrLinePosition()
	{
		let lHr_spr = this._fBaseContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/line_hr_white"));
		lHr_spr.position.set(-88, -1);
	}

	_addHrLineFriend()
	{
		let lHr_spr = this._fFriendBase_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/line_hr_white"));
		lHr_spr.position.set(39, -1);

		lHr_spr = this._fFriendBase_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/line_hr_white"));
		lHr_spr.position.set(69, -1);
	}

	_addPosition()
	{
		let lPositionText_tf = this._fPositionText_tf = this._fBaseContainer_sprt.addChild(new TextField(this._positionFriendStyle));
		lPositionText_tf.anchor.set(0.5, 0.5);
		lPositionText_tf.position.set(-98, 0.5);
		lPositionText_tf.text = "";
	}

	_addNickName()
	{
		let lNicknameText_tf = this._fNicknameText_tf = this._fContentContainer_sprt.addChild(new TextField(this._nicknameFriendStyle));
		lNicknameText_tf.maxWidth = 160;
		lNicknameText_tf.anchor.set(0, 0.5);
		lNicknameText_tf.position.set(-81, 1);
		lNicknameText_tf.text = "";
	}

	_addClock()
	{
		let lClock_spr = this._fClock_sprt = this._fContentContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/player_clock"));
		lClock_spr.position.set(54, 0);
	}

	_addPlayerReady()
	{
		let lPlayerReady_spr = this._fPlayerReady_sprt = this._fContentContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/player_ready"));
		lPlayerReady_spr.position.set(54, 0);
	}

	_addZZZIcon()
	{
		let lPlayerReady_spr = this._fPlayerZZZSprite = this._fContentContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/player_zzz"));
		lPlayerReady_spr.position.set(54, 0);
	}

	_addRedXIcon()
	{
		let lPlayerReady_spr = this._fPlayerRedIcon_sprt = this._fContentContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/player_red_x"));
		lPlayerReady_spr.position.set(54, 0);
	}

	_addPlayerNotReadyIcon()
	{
		let lPlayerReady_spr = this._fPlayerNotReady_sprt = this._fContentContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/player_not_ready"));
		lPlayerReady_spr.scale.set(1.6, 1.6);
		lPlayerReady_spr.position.set(54,5);
	}

	_addCrown()
	{
		let l_s = this._fCrown_sprt = this._fContentContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/crown"));
		l_s.position.set(90, 0);
	}

	_addKickButton()
	{
		let lKick_btn = this._fKick_btn = this._fContentContainer_sprt.addChild(new Button("dialogs/battleground/caf_room_manager/kick_button", "TACafPrivateBattlegroundPlayerKickButton", true));
		lKick_btn.position.set(90, 4);
		lKick_btn.on("pointerclick", this._onKickBtnClicked, this);
	}
	_addInviteButton()
	{
		let lInvite_btn = this._fInvite_btn = this._fContentContainer_sprt.addChild(new Button("dialogs/battleground/caf_room_manager/invite_button", "TACafPrivateBattlegroundPlayerInviteButton", true));
		lInvite_btn.position.set(85, 4);
		lInvite_btn.on("pointerclick", this._onInviteBtnClicked, this);
	}

	_updateView()
	{
		
		let lItemData_obj = this._fItemData_obj;
		let l_pi = APP.playerController.info;
		let lIsRoomManager_bl = (lItemData_obj[PLAYER_PARAMS.NICK] == l_pi.nickname) && l_pi.isCAFRoomManager;
		let lIsKicked_bl = lItemData_obj[PLAYER_PARAMS.IS_KICKED];

		this._fManagerContainer_sprt.visible = lIsRoomManager_bl;
		this._fFriendBase_sprt.visible = !lIsRoomManager_bl;


		if(lItemData_obj.pendingInvite || this.reinviteCount>=3)
		{
			this._fInvite_btn.enabled = false;
		}else{
			this._fInvite_btn.enabled = true;
		}

		if (lIsKicked_bl)
		{
			let lNickName_str = lItemData_obj[PLAYER_PARAMS.NICK] || "";
			if(lNickName_str == ""){
				this._fContentContainer_sprt.visible = false;
				return;
			}

			this._fContentContainer_sprt.visible = true;
			this._fPlayerNotReady_sprt.visible = false; 
			this._fPlayerRedIcon_sprt.visible = false; 
			this._fPlayerZZZSprite.visible = false;
			this._fNicknameText_tf.alpha = 0.5;
			this._fClock_sprt.visible = false; 
			this._fCrown_sprt.visible = false;
			this._fPlayerReady_sprt.visible = false; 
			this._fInvite_btn.visible = true;
			this._fKick_btn.visible = false;

			let lPositionFontStyle_obj = lIsRoomManager_bl ? this._positionManagerStyle : this._positionFriendStyle;
			let lPosition_str = "" + ((lItemData_obj.index || 0) + 1);
			this._fPositionText_tf.text = lPosition_str;
			this._fPositionText_tf.textFormat = lPositionFontStyle_obj;

			let lNickNameFontStyle_obj = lIsRoomManager_bl ? this._nicknameManagerStyle : this._nicknameFriendStyle;

			if (!APP.fonts.isGlyphsSupported(lNickNameFontStyle_obj.fontFamily, lNickName_str))
			{
				lNickNameFontStyle_obj.fontFamily = DEFAULT_SYSTEM_FONT;
			}

			this._fNicknameText_tf.text = lNickName_str;
			this._fNicknameText_tf.maxWidth = lIsRoomManager_bl ? 160 : 113;
			this._fNicknameText_tf.textFormat = lNickNameFontStyle_obj;
		}
		else
		{
			this._fContentContainer_sprt.visible = true;

			let lPositionFontStyle_obj = lIsRoomManager_bl ? this._positionManagerStyle : this._positionFriendStyle;
			let lPosition_str = "" + ((lItemData_obj.index || 0) + 1);
			
			this._fPositionText_tf.text = lPosition_str;
			this._fPositionText_tf.textFormat = lPositionFontStyle_obj;

			let lNickNameFontStyle_obj = lIsRoomManager_bl ? this._nicknameManagerStyle : this._nicknameFriendStyle;
			let lNickName_str = lItemData_obj[PLAYER_PARAMS.NICK] || "";

			if (!APP.fonts.isGlyphsSupported(lNickNameFontStyle_obj.fontFamily, lNickName_str))
			{
				lNickNameFontStyle_obj.fontFamily = DEFAULT_SYSTEM_FONT;
			}

			this._fNicknameText_tf.text = lNickName_str;
			this._fNicknameText_tf.maxWidth = lIsRoomManager_bl ? 160 : 113;
			this._fNicknameText_tf.textFormat = lNickNameFontStyle_obj;

			//this._fCrown_sprt.visible = lIsRoomManager_bl;
			this._fKick_btn.visible = !lIsRoomManager_bl;

			
			let lIsReady_bl = lItemData_obj[PLAYER_PARAMS.STATUS]  == PLAYER_CAF_STATUS.READY;
			//this._fClock_sprt.visible = !lIsRoomManager_bl && !lIsReady_bl;
			//this._fPlayerReady_sprt.visible = !lIsRoomManager_bl && lIsReady_bl;

			this._fPlayerNotReady_sprt.visible = false; 
			this._fPlayerRedIcon_sprt.visible = false; 
			this._fPlayerZZZSprite.visible = false;
			this._fNicknameText_tf.alpha = 1;
			this._fClock_sprt.visible = false; 
			this._fCrown_sprt.visible = false;
			this._fPlayerReady_sprt.visible = false; 
			this._fInvite_btn.visible = false;

			let status = lItemData_obj[PLAYER_PARAMS.STATUS];

			if(!lIsReady_bl && lIsRoomManager_bl == false)
			{
				
				switch(status){
					case PLAYER_CAF_STATUS.ACCEPTED:
						this._fPlayerZZZSprite.visible = true;
						this._fNicknameText_tf.alpha = 0.5;
						break;
					case PLAYER_CAF_STATUS.INVITED:
						this._fPlayerZZZSprite.visible = true;
						this._fNicknameText_tf.alpha = 0.5;
						break;
					case PLAYER_CAF_STATUS.LOADING:
						this._fClock_sprt.visible = true;
						break;
					case PLAYER_CAF_STATUS.REJECTED:
						this._fPlayerRedIcon_sprt.visible = true; 
						break;
					case PLAYER_CAF_STATUS.WAITING:
						this._fPlayerNotReady_sprt.visible = true; 
						break;
				}
			}else if (lIsReady_bl && lIsRoomManager_bl == false){
				this._fPlayerReady_sprt.visible = true; 
				
			}else{
				if(lIsRoomManager_bl){
					this._fCrown_sprt.visible = true;
				}
			}

			if (lItemData_obj[PLAYER_PARAMS.IS_KICK_IN_PROGRESS] || !lItemData_obj[PLAYER_PARAMS.IS_KICK_ALLOWED] || status == PLAYER_CAF_STATUS.INVITED || status == PLAYER_CAF_STATUS.LOADING || status == PLAYER_CAF_STATUS.ACCEPTED)
			{
				this._fKick_btn.enabled = false;
				this._fKick_btn.alpha = 0;
			}
			else
			{
				this._fKick_btn.enabled = true;
				this._fKick_btn.alpha = 1;
			}
		}
	}

	get _positionFriendStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 12,
			align: "center",
			fill: 0xffffff,
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: true
		};

		return lStyle_obj;
	}

	get _positionManagerStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 12,
			align: "center",
			fill: [0xf8e169, 0xf8e169, 0xd49205, 0xf8e169, 0xf8e169, 0xffeacf, 0xd49705, 0xffc000, 0xf8e169, 0xf8e169, 0xffffff, 0xd49705, 0xffffff, 0xffffff],
			fillGradientType: PIXI.TEXT_GRADIENT.LINEAR_VERTICAL,
			fillGradientStops: [0, 0.15, 0.21, 0.29, 0.36, 0.45, 0.48, 0.56, 0.59, 0.65, 0.68, 0.83, 0.88, 1],
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: true
		};

		return lStyle_obj;
	}

	get _nicknameFriendStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 13,
			align: "left",
			fill: 0xffffff,
			dropShadow: true,
			shortLength: 160,
			dropShadowColor: 0x000000,
			dropShadowAngle: 131,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: false,
			letterSpacing: 1
		};

		return lStyle_obj;
	}

	get _nicknameManagerStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 13,
			align: "left",
			fill: [0xf8e169, 0xf8e169, 0xd49205, 0xf8e169, 0xf8e169, 0xffeacf, 0xd49705, 0xffc000, 0xf8e169, 0xf8e169, 0xffffff, 0xd49705, 0xffffff, 0xffffff],
			fillGradientType: PIXI.TEXT_GRADIENT.LINEAR_VERTICAL,
			fillGradientStops: [0, 0.15, 0.21, 0.29, 0.36, 0.45, 0.48, 0.56, 0.59, 0.65, 0.68, 0.83, 0.88, 1],
			dropShadow: true,
			shortLength: 160,
			dropShadowColor: 0x000000,
			dropShadowAngle: 131,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: false,
			letterSpacing: 1
		};

		return lStyle_obj;
	}

	_onKickBtnClicked()
	{
		this._fItemData_obj.pendingInvite = null;
		this.emit(BattlegroundPrivatePlayersListItem.EVENT_ON_KICK_CLICKED);
	}

	_onInviteBtnClicked()
	{
		if(this._fItemData_obj.pendingInvite)
		{
			return;
		}
		let lItemData_obj = this._fItemData_obj;
		let lNickName_str = lItemData_obj[PLAYER_PARAMS.NICK]
		this._fReinvite_countData[lNickName_str]++;
		this._fItemData_obj.pendingInvite = true;
		this.emit(BattlegroundPrivatePlayersListItem.EVENT_ON_CANCEL_KICK_CLICKED);
	}

	get reinviteCount(){
		let lItemData_obj = this._fItemData_obj;
		let lNickName_str = lItemData_obj[PLAYER_PARAMS.NICK];
		if(!lNickName_str || !this._fReinvite_countData) return 0;
		return this._fReinvite_countData[lNickName_str];
	}


	destroy()
	{
		super.destroy();
	}
}

export default BattlegroundPrivatePlayersListItem;