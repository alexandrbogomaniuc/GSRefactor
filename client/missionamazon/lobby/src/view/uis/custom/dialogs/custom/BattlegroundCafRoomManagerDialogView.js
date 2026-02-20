import DialogView from '../DialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ActiveGameDialogButton from './game/ActiveGameDialogButton';
import DialogButton from '../DialogButton';
import BattlegroundPrivatePlayersListView from './BattlegroundPrivatePlayersListView';
import BattlegroundPrivateFriendsListView from './BattlegroundPrivateFriendsListView';
import { Sprite } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import Button from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import VerticalSlider from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/slider/VerticalSlider';
import VerticalScrollBar from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/VerticalScrollBar';
import InputField from '../../../../../ui/InputField';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';


export const BACK_TYPE =
{
	RETURN_TO_LOBBY: 'RETURN_TO_LOBBY',
	RETURN_TO_SCOREBOARD: 'RETURN_TO_SCOREBOARD'
}


export const MODE_TYPE =
{
	COLAPSED: 'COLAPSED',
	EXPANDED: 'EXPANDED'
}

const USER_NAME_FONT_NAME = "fnt_nm_barlow";
const COLAPSED_POSITIONS = [-122, -57, 160, 275, 110, 145, 113];
const EXPANDED_POSITIONS = [-210, -142, 105, 190, 25, 145, 28];


class BattlegroundCafRoomManagerDialogView extends DialogView {
	static get EVENT_ON_ITEM_KICK_CLICKED() { return BattlegroundPrivatePlayersListView.EVENT_ON_ITEM_KICK_CLICKED };
	static get EVENT_ON_CANCEL_KICK_CLICKED() { return BattlegroundPrivatePlayersListView.EVENT_ON_CANCEL_KICK_CLICKED };
	static get EVENT_ON_INVITE_FRIEND_CLICKED() { return BattlegroundPrivateFriendsListView.EVENT_ON_INVITE_FRIEND_CLICKED };
	static get EVENT_ON_CANCEL_READY_BTN_CLICKED() { return "EVENT_ON_CANCEL_READY_BTN_CLICKED" };
	static get EVENT_ON_ROUND_START_BTN_CLICKED() { return "EVENT_ON_ROUND_START_BTN_CLICKED" };

	constructor() {
		super();
		this._searchValue_str = "";
	}



	_getRestrictedNicknameGlyphs() {
		let restrictedGlyphs = APP.playerController.info.nicknameGlyphs;
		if (!restrictedGlyphs) {
			restrictedGlyphs = PlayerInfo.DEFAULT_NICK_GLYPHS;
		}

		return restrictedGlyphs;
	}

	_generateProperUsernameFontName(restrictedGlyphs) {
		let fontName = "sans-serif";
		if (APP.fonts.isGlyphsSupported(USER_NAME_FONT_NAME, restrictedGlyphs)) {
			fontName = USER_NAME_FONT_NAME;
		}

		return fontName;
	}

	setOkCancelCustomMode() {
		this._setButtonsCount(4);
	}

	get readyButton() {
		return this._getButtonView(0);
	}

	get cancelReadyButton() {
		return this._getButtonView(2);
	}

	get startRoundButton() {
		return this._getButtonView(3);
	}

	get backButton() {
		return this._getButtonView(1);
	}

	validateManagerStateButtons() {
		this._validateManagerStateButtons();
	}

	validatePlayersListView() {
		this._fPlayersListView_bpplv.update(this.uiInfo.playersListData, this.uiInfo.reinviteCountData);
	}

	_filterFriendsList(rawData) {
		let filteredData = [];
		if (this._searchValue_str == "") {
			filteredData = rawData;
		} else {
			const searchValue = this._searchValue_str;
			for (let i = 0; i < rawData.length; i++) {
				const dataSet = rawData[i];
				if (dataSet.nickname.indexOf(searchValue) > -1) {
					filteredData.push(dataSet);
				}
			}
		}


		return filteredData;
	}

	validateFriendsListView() 
	{
		if (!this.uiInfo.friendsListData) 
		{
			if(this._ff_background_colapsed.alpha!=0.5)this._ff_background_colapsed.alpha = 0.5;
			return;
		}else if(this.uiInfo.friendsListData.length ==0)
		{
			if(this._ff_background_colapsed.alpha!=0.5)this._ff_background_colapsed.alpha = 0.5;
		}else{
			if(this._ff_background_colapsed.alpha!=1)this._ff_background_colapsed.alpha = 1;
		}
		
		const rawFrriendsListData = this.uiInfo.friendsListData;

		if (this._onlineCounter_tf.text != rawFrriendsListData.length.toString()) {
			this._onlineCounter_tf.text = rawFrriendsListData.length.toString();
		};

		const filteredList = this._filterFriendsList(rawFrriendsListData);
		const sizeChanged = this._fFriendsView_bpflv.update(filteredList);

		if (filteredList.length < 5) {
			this._minimumSlide = false;
		} else {
			this._minimumSlide = true;
		}

		// slider thumb has a asynchronous bug. If updated while invisible it will become visible, and if updated immediately will show wrong size
		if (sizeChanged) {

			if(this.uiInfo.friendsListData.length!=0){
				setTimeout(() => {
					this.setMode(this._modeType);
				}, 0);
				setTimeout(() => {
					this.setMode(this._modeType);
				}, 1500);
			}else{
				this.setMode(MODE_TYPE.COLAPSED);
			}
			

		}
	}

	get showSearchTip() {
		if (this._searchValue_str.length == 0) return true;
	}

	//INIT VIEW...
	_initDialogView() {
		super._initDialogView();

		this._overlayContainer.visible = false;

		this._baseContainer.position.set(0, 0);
		this._buttonsContainer.position.set(120, 0);
		this._messageContainer.position.set(0, 0);
	}

	updateBackType(btnType) {
		this._fBtnType = btnType;

		this._updateBackButtonCaption();
	}

	_updateBackButtonCaption() {
		this.backButton.captionId = this._fBtnType === BACK_TYPE.RETURN_TO_SCOREBOARD ? "TABattlegroundBackToResults" : "TABattlegroundBackToLobbyButtonCaption";
	}

	_addDialogBase() {
		this._fBtnType = BACK_TYPE.RETURN_TO_LOBBY;

		//OVERLAY...
		let lOverlay_g = new PIXI.Graphics();
		lOverlay_g.beginFill(0x000000).drawRect(0, 0, 960, 540).endFill();
		lOverlay_g.alpha = 0.8;;
		lOverlay_g.position.set(-960 / 2, -540 / 2);
		this._baseContainer.addChild(lOverlay_g);
		//...OVERLAY

		//DIALOG BASE...

		this._lFrame_g = this.addChild(APP.library.getSprite("dialogs/battleground/frame"));
		this._lFrame_g.scale.set(1.159, 1);
		this._baseContainer.addChild(this._lFrame_g);
		this._lFrame_g.position.set(0, 25);

		//...DIALOG BASE

		//LOGO...
		this._logo = I18.generateNewCTranslatableAsset("TABattlegroundDialogLogo");
		this._logo.position.set(3, -122);
		this._baseContainer.addChild(this._logo);
		//...LOGO

		//TEXTS...
		this._messageContainer.destroyChildren();
		//...TEXTS

		//TABLE...
		this._fPlayersListView_bpplv = this.addChild(new BattlegroundPrivatePlayersListView());
		this._fPlayersListView_bpplv.position.set(-99, -57);
		this._fPlayersListView_bpplv.on(BattlegroundPrivatePlayersListView.EVENT_ON_ITEM_KICK_CLICKED, this.emit, this);
		this._fPlayersListView_bpplv.on(BattlegroundPrivatePlayersListView.EVENT_ON_CANCEL_KICK_CLICKED, this.emit, this);

		this._baseContainer.addChild(this._fPlayersListView_bpplv);

		this._ff_seacrh_background = this.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/friends_frame_search"));
		this._ff_seacrh_background.position.set(-78, 200);
		this._ff_seacrh_background.scale.set(1.2, 1.3);

		this._ff_background_expended = this.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/friends_frame_expanded"));
		this._ff_background_expended.position.set(-80, 250);
		this._ff_background_expended.scale.set(1.2, 1.5);

		this._ff_background_colapsed = this.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/friends_frame_colapsed"));
		this._ff_background_colapsed.position.set(-80, 250);
		this._ff_background_colapsed.scale.set(1.2, 1.5);

		let lScrollBack_grphc = new PIXI.Graphics();
		lScrollBack_grphc.beginFill(0x262626).drawRoundedRect(-2.5, -50, 5, 100, 2).endFill();
		let lScrollThumb_grphc = new PIXI.Graphics();
		lScrollThumb_grphc.beginFill(0x5c5c5c).drawRoundedRect(-2.5, -25, 5, 50, 2).endFill();
		this._fMenuSliderThumb = lScrollThumb_grphc;

		this._fMenuSlider_vs = new VerticalSlider(lScrollBack_grphc, lScrollThumb_grphc, undefined, undefined, 0, null, false);
		this._fMenuSlider_vs.scrollMultiplier = 1;
		this._fMenuSlider_vs.position.set(15, 120);
		this.addChild(this._fMenuSlider_vs);

		this._fFriendsView_bpflv = new BattlegroundPrivateFriendsListView();
		this._fFriendsView_bpflv.on(BattlegroundPrivateFriendsListView.EVENT_ON_INVITE_FRIEND_CLICKED, this.emit, this);

		this._fStakesMenuScrollBar_vsb = this.addChild(new VerticalScrollBar());
		this._fStakesMenuScrollBar_vsb.position.set(-99, 160);
		this._fStakesMenuScrollBar_vsb.visibleArea = new PIXI.Rectangle(-174, -10, 348, 103);
		this._fStakesMenuScrollBar_vsb.hitArea = new PIXI.Rectangle(-174, -10, 370, 104);
		this._fStakesMenuScrollBar_vsb.slider = this._fMenuSlider_vs;
		this._fStakesMenuScrollBar_vsb.scrollableContainer = this._fFriendsView_bpflv;

		this._fStakesMenuScrollBar_vsb.addChild(this._fFriendsView_bpflv);

		this._ff_togglerUp_spr = this.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/toggle_up"));
		this._ff_togglerUp_spr.position.set(-5, 10);

		this._ff_togglerDown_spr = this.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/toggle_down"));
		this._ff_togglerDown_spr.position.set(-5, 10);


		this._ffManIcon_spr = this.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/man_icon"));
		this._ffManIcon_spr.position.set(-30, 10);


		this._baseContainer.addChild(this._ff_background_expended);
		this._baseContainer.addChild(this._ff_background_colapsed);

		this._baseContainer.addChild(this._fStakesMenuScrollBar_vsb);
		this._baseContainer.addChild(this._fMenuSlider_vs);

		const onlineFriendsLabel = I18.generateNewCTranslatableAsset("TACafPrivateBattlegroundPlayerOnlineFriends");
		this._onlineFriendsLabel_tf = this._baseContainer.addChild(onlineFriendsLabel);
		if(APP.isMobile)
		{
			this._onlineFriendsLabel_tf.position.set(-180, 10);
		}else{
			this._onlineFriendsLabel_tf.position.set(-240, 10);
		}
		
		this._baseContainer.addChild(this._ff_seacrh_background);

		this._reactebleArea_spr = this._baseContainer.addChild(new Sprite());
		this._reactableArea = new PIXI.Graphics();
		this._reactableArea.beginFill(0x262626).drawRoundedRect(0, 0, 220, 20, 2).endFill();
		this._reactableArea.position.set(-200, 30);
		this._reactableArea.alpha = 0;
		this._reactebleArea_spr.addChild(this._reactableArea);
		this._reactebleArea_spr.on("pointerclick", this.onExpandButtonClicked, this);


		const restrictedGlyphs = this._getRestrictedNicknameGlyphs();

		const fontName = this._generateProperUsernameFontName(restrictedGlyphs);

		const lInputConfig_obj = {
			fontColor: 0xffffff,
			fontFamily: fontName,
			fontSize: 12,
			width: 370,
			height: 39,
			selectionColor: 0x9d5525,
			textAlign: "center",
			offsetY: -4,
			selectYShift: -1,
			maxTextLength: 20,
			acceptableChars: restrictedGlyphs,
			caseSensitive: true
		}

		this._fSearch_tf = this.addChild(new InputField(null, lInputConfig_obj));
		this._fSearch_tf.position.set(-90, 70);

		let onlineCounterStyle_obj = {
			fontFamily: fontName,
			fontSize: 12,
			align: "left",
			fill: 0x000000,
			dropShadow: true,
			shortLength: 5,
			dropShadowColor: 0x000000,
			dropShadowAngle: 131,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: true,
			letterSpacing: 1
		};

		const onlineCount_tf = this._onlineCounter_tf = this._baseContainer.addChild(new TextField(onlineCounterStyle_obj));
		onlineCount_tf.maxWidth = 160;
		onlineCount_tf.anchor.set(0, 0.5);
		onlineCount_tf.position.set(-40, 50);
		onlineCount_tf.text = "";

		this._fSearch_tf.on(InputField.EVENT_ON_BLUR, this._onInputBlur, this);
		this._fSearch_tf.on(InputField.EVENT_ON_FOCUS, this._onInputFocus, this);

		this._baseContainer.addChild(this._fSearch_tf);

		this._fTip_spr = this.addChild(I18.generateNewCTranslatableAsset('TALobbyNewPlayerEnterTip'));
		this._fTip_spr.position.set(-90, 70);
		this._baseContainer.addChild(this._fTip_spr);

		this._fSearch_tf.on(InputField.EVENT_ON_VALUE_CHANGED, () => {

			this._searchValue_str = this._fSearch_tf.getValue();
			this._fTip_spr.alpha = 0;
		}, this);

		this.alpha = 0;


		setTimeout(() => {
			this.setMode(MODE_TYPE.COLAPSED);
			this.alpha = 1;
		}, 0);
	}




	_onInputBlur() {
		let lValue_str = this._fSearch_tf.getValue();
		if (this.showSearchTip && this._modeType == MODE_TYPE.EXPANDED) {
			this._fTip_spr.alpha = 1;
		} else {
			this._fTip_spr.alpha = 0;
		}
	}

	_onInputFocus() {
		this._fTip_spr.alpha = 0;
	}


	onExpandButtonClicked(evemt) {
		if (!this.uiInfo.friendsListData || this.uiInfo.friendsListData.length == 0) return;
		switch (this._modeType) {
			case MODE_TYPE.COLAPSED:
				this.setMode(MODE_TYPE.EXPANDED);
				break;
			case MODE_TYPE.EXPANDED:
				this.setMode(MODE_TYPE.COLAPSED);
				break;
		}
	}


	setMode(modeType) {
		this._modeType = modeType;

		let implementationArray = COLAPSED_POSITIONS;
		let implementationScale = 1;
		this._fStakesMenuScrollBar_vsb._updateSlider();
		switch (modeType) {
			case MODE_TYPE.COLAPSED:
				implementationArray = COLAPSED_POSITIONS;
				implementationScale = 1;
				this._ff_background_expended.visible = false;
				this._fStakesMenuScrollBar_vsb.visible = false;
				this._fMenuSlider_vs.visible = false;
				this._ff_togglerDown_spr.visible = true;
				this._ff_togglerUp_spr.visible = false;
				this._ff_seacrh_background.visible = false;
				this._fTip_spr.visible = false;
				this._fSearch_tf.visible = false;
				this._fTip_spr.alpha = 0;
				break;
			case MODE_TYPE.EXPANDED:
				implementationArray = EXPANDED_POSITIONS;
				implementationScale = 1.6;
				this._ff_background_expended.visible = true;
				this._minimumSlide ? this._fMenuSlider_vs.visible = true : this._fMenuSlider_vs.visible = false;
				this._fStakesMenuScrollBar_vsb.visible = true;
				this._ff_togglerDown_spr.visible = false;
				this._ff_togglerUp_spr.visible = true;
				this._ff_seacrh_background.visible = true;
				this._fTip_spr.visible = true;
				this._fSearch_tf.visible = true;

				if (this.showSearchTip) {
					this._fTip_spr.alpha = 1;
				} else {
					this._fTip_spr.alpha = 0;
				}
				break;
		}


		this._implementLayout(implementationScale, implementationArray);
		this._alignButtonView(1, 1, this.readyButton);
		this._alignButtonView(1, 1, this.cancelReadyButton);
		this._alignButtonView(2, 1, this.startRoundButton);
		this._alignButtonView(3, 1, this.backButton);

	}

	_implementLayout(backScale, implementationArray) {
		this._lFrame_g.scale.y = backScale;
		this._logo.position.y = implementationArray[0];
		this._fPlayersListView_bpplv.position.y = implementationArray[1];
		this._fStakesMenuScrollBar_vsb.position.y = implementationArray[2];
		this._ff_background_expended.y = implementationArray[3];
		this._ff_background_colapsed.y = implementationArray[3] + 3;
		this._onlineFriendsLabel_tf.y = implementationArray[4];
		this._ff_togglerDown_spr.y = implementationArray[4] + 12;
		this._ff_togglerUp_spr.y = implementationArray[4] + 14;
		this._ffManIcon_spr.y = implementationArray[4] + 12;
		this._fMenuSlider_vs.y = implementationArray[5];
		this._reactableArea.y = implementationArray[6];
		this._onlineCounter_tf.y = implementationArray[4] + 13;

	}

	_initButtonView(aIntId_int) {
		let l_domdb = super._initButtonView(aIntId_int)

		if (aIntId_int === 2) // back button
		{
			this._updateBackButtonCaption();
		}

		return l_domdb;
	}

	__retreiveDialogButtonViewInstance(aIntId_int) {
		let lButton_btn;
		switch (aIntId_int) {
			case 0:
				lButton_btn = new ActiveGameDialogButton("lobby/battleground/active_btn", "TABattlegroundCAFReadyButtonCaption", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_ACCEPT, false, true);
				lButton_btn.isOkButton = true;
				break;
			case 1:
				lButton_btn = new ActiveGameDialogButton("dialogs/battleground/confirm_buy_in/return_to_lobby_button", "TABattlegroundBackToLobbyButtonCaption", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_CANCEL, false);
				lButton_btn.isCancelButton = true;
				break;
			case 2:
				lButton_btn = new ActiveGameDialogButton("lobby/battleground/active_btn", "TABattlegroundCAFCancelReadyButtonCaption", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_ACCEPT, false, true);
				lButton_btn.isCancelReadyButton = true;
				break;
			case 3:
				lButton_btn = new ActiveGameDialogButton("lobby/battleground/active_btn", "TABattlegroundCAFStartRoundButtonCaption", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_ACCEPT, false, true);
				lButton_btn.isRoundStartButton = true;
				break;
			default:
				throw new Error(`Unsupported button id: ${aIntId_int}`);
		}
		return lButton_btn;
	}

	_onButtonClicked(event) {
		var button = event.target;

		if (button.isOkButton) {
			this.emit(DialogView.EVENT_ON_OK_BTN_CLICKED);
		}
		else if (button.isCancelButton) {
			this.emit(DialogView.EVENT_ON_CANCEL_BTN_CLICKED);
		}
		else if (button.isCancelReadyButton) {
			this.emit(BattlegroundCafRoomManagerDialogView.EVENT_ON_CANCEL_READY_BTN_CLICKED);
		}
		else if (button.isRoundStartButton) {
			this.emit(BattlegroundCafRoomManagerDialogView.EVENT_ON_ROUND_START_BTN_CLICKED);
		}

		this.emit(DialogView.EVENT_ON_BTN_CLICKED);
	}

	_alignButtonView(aIntButtonId_int, aButtonsCount_int, aButtonView_domdb) {
		var lXOffset_num = 0;
		var lYOffset_num = 0;

		switch (aIntButtonId_int) {
			case 0:
			case 2:

				lYOffset_num = this._modeType == MODE_TYPE.EXPANDED ? - 122 : -42;
				break;
			case 1:
				lYOffset_num = this._modeType == MODE_TYPE.EXPANDED ? - 18 : 68;
				break;
			case 3:
				lYOffset_num = this._modeType == MODE_TYPE.EXPANDED ? -67 : 18;
				break;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	_validateManagerStateButtons() {
		let lUIInfo_bcrmdi = this.uiInfo;

		// READY BUTTON...
		this.readyButton.visible = !lUIInfo_bcrmdi.isReadyConfirmed;
		let lIsReadyButtonEnabled_bl = this.readyButton.visible
			&& !lUIInfo_bcrmdi.isReadyConfirmationInProgress
			&& !lUIInfo_bcrmdi.isPlayerSitOutState
			&& !lUIInfo_bcrmdi.isGameStateQualify;

		if (lIsReadyButtonEnabled_bl) {
			this.okButton.enabled = true;
			this.okButton.alpha = 1;
		}
		else {
			this.readyButton.enabled = false;
			this.readyButton.alpha = 0.3;
		}
		// ...READY BUTTON

		// CANCEL READY BUTTON...
		this.cancelReadyButton.visible = !this.readyButton.visible;
		let lIsCancelReadyButtonEnabled_bl = this.cancelReadyButton.visible
			&& lUIInfo_bcrmdi.isReadyConfirmed
			&& !lUIInfo_bcrmdi.isCancelReadyTriggered
			&& !lUIInfo_bcrmdi.isPlayerSitOutState
			&& !lUIInfo_bcrmdi.isGameStateQualify;

		if (lIsCancelReadyButtonEnabled_bl) {
			this.cancelReadyButton.enabled = true;
			this.cancelReadyButton.alpha = 1;
		}
		else {
			this.cancelReadyButton.enabled = false;
			this.cancelReadyButton.alpha = 0.3;
		}
		// ...CANCEL READY BUTTON

		// START ROUND BUTTON...
		this.startRoundButton.visible = true;
		let lIsStartRoundButtonEnabled_bl = this.startRoundButton.visible
			&& lUIInfo_bcrmdi.isReadyConfirmed
			&& !lUIInfo_bcrmdi.isCancelReadyTriggered
			&& !lUIInfo_bcrmdi.isPlayerSitOutState
			&& !lUIInfo_bcrmdi.isGameStateQualify
			&& lUIInfo_bcrmdi.readyPlayersAmount > 1;


		if (lIsStartRoundButtonEnabled_bl) {
			this.startRoundButton.enabled = true;
			this.startRoundButton.alpha = 1;
		}
		else {
			this.startRoundButton.enabled = false;
			this.startRoundButton.alpha = 0.3;
		}
		// ...START ROUND BUTTON

		// BACK BUTTON...
		this.backButton.visible = true;
		let lIsBackButtonEnabled_bl = this.backButton.visible
			&& (
				(this._fBtnType === BACK_TYPE.RETURN_TO_SCOREBOARD)
				|| lIsReadyButtonEnabled_bl
			);

		if (lIsBackButtonEnabled_bl) {
			this.backButton.enabled = true;
			this.backButton.alpha = 1;
		}
		else {
			this.backButton.enabled = false;
			this.backButton.alpha = 0.3;
		}
		// ...BACK BUTTON
	}


	get _supportedButtonsCount() {
		return 4;
	}
}

export default BattlegroundCafRoomManagerDialogView;