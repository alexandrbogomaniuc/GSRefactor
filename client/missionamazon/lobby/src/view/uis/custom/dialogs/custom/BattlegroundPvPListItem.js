import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField, { DEFAULT_SYSTEM_FONT } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { DropShadowFilter } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';
import Button from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { PLAYER_PARAMS, PLAYER_CAF_STATUS } from '../../../../../model/uis/custom/dialogs/custom/BattlegroundCafRoomManagerDialogInfo';

class BattlegroundPvPListItem extends Sprite {

	update(aData_obj, index) 
	{
		this._fPositionText_tf.visible = true;
		this._fNicknameText_tf.visible = true;
		this._fPositionText_tf.text = index;
		this._fNicknameText_tf.text = this._fNicknameText_tf._createShortenedForm(aData_obj.nickname);
		switch(aData_obj.status)
		{
			case "WAITING":
					this._fPlayerReady_tf.visible = false;
					this._fPlayerNotReady_tf.visible = true;
				break;
			default:
					this._fPlayerReady_tf.visible = true;
					this._fPlayerNotReady_tf.visible = false;
				break;
		}
	}

	empty()
	{
		this._fPositionText_tf.visible = false;
		this._fNicknameText_tf.visible = false;
		this._fPlayerReady_tf.visible = false;
		this._fPlayerNotReady_tf.visible = false;
	}

	get nickName() {
		return this._fItemData_obj[PLAYER_PARAMS.NICK] || undefined;
	}

	constructor(aData_obj) {
		super();

		this._fBaseContainer_sprt = null;
		this._fManagerContainer_sprt = null;
		this._fContentContainer_sprt = null;
		this._fManagerBaseStroke_sprt = null;
		this._fManagerBase_sprt = null;
		this._fFriendBase_sprt = null;
		this._fPositionText_tf = null;
		this._fNicknameText_tf = null;
		this._fPlayerNotReady_tf = null;


		this._fItemData_obj = this._validateItemData(aData_obj);

		this._init();
		
	}

	_validateItemData(aData_obj) {
		return aData_obj || { [PLAYER_PARAMS.IS_KICKED]: true };
	}

	_init() {
		this._fBaseContainer_sprt = this.addChild(new Sprite());
		this._fContentContainer_sprt = this.addChild(new Sprite());

		this._addBack();
		this._addHrLinePosition();
		this._addHrLineStatus();
		this._addPosition();
		this._addNickName();
		this._addPlayerReadyText();
		this._addPlayerNotReadyText();

	}

	_addBack() {
		this._fManagerContainer_sprt = this._fBaseContainer_sprt.addChild(new Sprite());

		this._fManagerBaseStroke_sprt = this._fManagerContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/row_manager"));
		this._fManagerBaseStroke_sprt.position.set(0, 2);

		this._fFriendBase_sprt = this._fBaseContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/row_not_manager"));
		this._fFriendBase_sprt.position.set(0, 2);
	}

	_addHrLinePosition() {
		let lHr_spr = this._fBaseContainer_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/line_hr_white"));
		lHr_spr.position.set(-88, -1);
	}

	_addHrLineStatus() {
		let lHr_spr = this._fFriendBase_sprt.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/line_hr_white"));
		lHr_spr.position.set(39, -1);
	}

	_addPosition() {
		let lPositionText_tf = this._fPositionText_tf = this._fBaseContainer_sprt.addChild(new TextField(this._positionFriendStyle));
		lPositionText_tf.anchor.set(0.5, 0.5);
		lPositionText_tf.position.set(-98, 0.5);
		lPositionText_tf.text = "";
	}

	_addNickName() {
		let lNicknameText_tf = this._fNicknameText_tf = this._fContentContainer_sprt.addChild(new TextField(this._nicknameFriendStyle));
		lNicknameText_tf.maxWidth = 115;
		lNicknameText_tf.anchor.set(0, 0.5);
		lNicknameText_tf.position.set(-81, 1);
		lNicknameText_tf.text = "";
	}


	_addPlayerReadyText() {
		let lPlayerReady_tf = this._fPlayerReady_tf = this._fContentContainer_sprt.addChild(new TextField(this._userStatusStyleReady));
		lPlayerReady_tf.maxWidth = 160;
		lPlayerReady_tf.anchor.set(0.5, 0.5);
		lPlayerReady_tf.position.set(75, 0);
		lPlayerReady_tf.text = "joined";
	}


	_addPlayerNotReadyText() {
		let lPlayerReady_tf = this._fPlayerNotReady_tf = this._fContentContainer_sprt.addChild(new TextField(this._userStatusStyleNotReady));
		lPlayerReady_tf.maxWidth = 160;
		lPlayerReady_tf.anchor.set(0.5, 0.5);
		lPlayerReady_tf.position.set(75, 0);
		lPlayerReady_tf.text = "waiting";
	}

	get _userStatusStyleReady(){
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 12,
			align: "center",
			fill: 0x7DDA58,
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI / 2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: true
		};

		return lStyle_obj;
	}

	get _userStatusStyleNotReady(){
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 12,
			align: "center",
			fill: 0xD20103,
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI / 2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: true
		};

		return lStyle_obj;
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
			shortLength: 115,
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

	destroy() {
		super.destroy();
	}
}

export default BattlegroundPvPListItem;