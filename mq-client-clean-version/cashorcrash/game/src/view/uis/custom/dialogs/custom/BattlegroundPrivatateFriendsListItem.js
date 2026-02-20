import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField, { DEFAULT_SYSTEM_FONT } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Button from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { PLAYER_PARAMS, PLAYER_CAF_STATUS } from '../../../../../model/uis/custom/dialogs/custom/BattlegroundCafRoomManagerDialogInfo';

class BattlegroundPrivatateFriendsListItem extends Sprite {
	static get EVENT_ON_INVITE_CLICKED() { return "EVENT_ON_INVITE_CLICKED" };

	update(aData_obj) {
		this._fItemData_obj = aData_obj;

		this._updateView();
	}

	get nickName() {
		return this._fItemData_obj[PLAYER_PARAMS.NICK] || undefined;
	}

	constructor(aData_obj) {
		super();

		this._fBaseContainer_sprt = null;
		this._fManagerContainer_sprt = null;
		this._fContentContainer_sprt = null;
		//this._fManagerBaseStroke_sprt = null;
		this._fManagerBase_sprt = null;
		this._fFriendBase_sprt = null;
		this._fOnlineMarker_sp = null;
		this._fNicknameText_tf = null;
		this._invite_btn = null;
		this._fItemData_obj = aData_obj;
		this._init();

	}



	_init() {
		this._fBaseContainer_sprt = this.addChild(new Sprite());
		this._fContentContainer_sprt = this.addChild(new Sprite());

		this._addBack();
		this._addOnlineStatusMarkers();
		this._addNickName();
		this._addInviteButton();
	}

	_addBack() {
		this._fManagerContainer_sprt = this._fBaseContainer_sprt.addChild(new Sprite());

		/*this._fManagerBaseStroke_sprt = this._fManagerContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/row_manager_friends"));
		this._fManagerBaseStroke_sprt.position.set(0, 2);*/

		this._fFriendBase_sprt = this._fBaseContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/row_manager_friends"));
		this._fFriendBase_sprt.position.set(0, 2);
	}


	_addOnlineStatusMarkers() {
		let lOnlineMarker_sp = this._fOnlineMarker_sp = this._fBaseContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/green_marker"));
		lOnlineMarker_sp.anchor.set(0.5, 0.5);
		lOnlineMarker_sp.position.set(-90, 0.5);
		lOnlineMarker_sp.alpha = 1;


		let lpOfflineMarker_sp = this._fOfflineMarker_sp = this._fBaseContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/red_marker"));
		lpOfflineMarker_sp.anchor.set(0.5, 0.5);
		lpOfflineMarker_sp.position.set(-90, 0.5);
		lpOfflineMarker_sp.alpha = 0;
	}

	_addNickName() {
		let lNicknameText_tf = this._fNicknameText_tf = this._fContentContainer_sprt.addChild(new TextField(this._nicknameFriendStyle));
		lNicknameText_tf.maxWidth = 160;
		lNicknameText_tf.anchor.set(0, 0.5);
		lNicknameText_tf.position.set(-81, 1);
		lNicknameText_tf.text = "Neki nikic";
	}

	_addInviteButton() {
		let invite_btn = this._invite_btn = this._fContentContainer_sprt.addChild(new Button("dialogs/battleground/caf_room_manager/invite_button", "TACafPrivateBattlegroundPlayerInviteButton", true));
		invite_btn.position.set(85, 4);
		invite_btn.on("pointerclick", this._onInviteButtonClicked, this);
	}

	_updateView() {

		let lItemData_obj = this._fItemData_obj;
		this._fContentContainer_sprt.visible = true;
		let lNickNameFontStyle_obj = this._nicknameFriendStyle;
		let lNickName_str = lItemData_obj[PLAYER_PARAMS.NICK];
		if (!APP.fonts.isGlyphsSupported(lNickNameFontStyle_obj.fontFamily, lNickName_str)) {
			lNickNameFontStyle_obj.fontFamily = DEFAULT_SYSTEM_FONT;
		}

		this._fNicknameText_tf.text = lNickName_str;
		this._fNicknameText_tf.maxWidth = 113;
		this._fNicknameText_tf.textFormat = lNickNameFontStyle_obj;

		this._invite_btn.visible = true;
		if (this._fItemData_obj.online == true) {
			if (this._fOnlineMarker_sp.alpha == 0) {
				this._fOnlineMarker_sp.alpha = 1;
				this._fOfflineMarker_sp.alpha = 0;
			}
		} else {
			if (this._fOfflineMarker_sp.alpha == 0) {
				this._fOfflineMarker_sp.alpha = 1;
				this._fOnlineMarker_sp.alpha = 0;
			}
		}

		if (this._fNicknameText_tf.alpha == 0) {
			this._fNicknameText_tf.alpha = 1;
		}


	}

	get _positionFriendStyle() {
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 12,
			align: "center",
			fill: 0xffffff,
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI / 2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: true
		};

		return lStyle_obj;
	}

	get _positionManagerStyle() {
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 12,
			align: "center",
			fill: [0xf8e169, 0xf8e169, 0xd49205, 0xf8e169, 0xf8e169, 0xffeacf, 0xd49705, 0xffc000, 0xf8e169, 0xf8e169, 0xffffff, 0xd49705, 0xffffff, 0xffffff],
			fillGradientType: PIXI.TEXT_GRADIENT.LINEAR_VERTICAL,
			fillGradientStops: [0, 0.15, 0.21, 0.29, 0.36, 0.45, 0.48, 0.56, 0.59, 0.65, 0.68, 0.83, 0.88, 1],
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI / 2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: true
		};

		return lStyle_obj;
	}

	get _nicknameFriendStyle() {
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

	get _nicknameManagerStyle() {
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

	_onInviteButtonClicked(event) {
		this.emit(BattlegroundPrivatateFriendsListItem.EVENT_ON_INVITE_CLICKED);
	}

	destroy() {
		super.destroy();
	}
}

export default BattlegroundPrivatateFriendsListItem;