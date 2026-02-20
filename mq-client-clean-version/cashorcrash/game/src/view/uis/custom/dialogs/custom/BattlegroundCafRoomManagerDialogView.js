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
const COLAPSED_POSITIONS = [-122, -57, 160, 275, 110, 145, 113, 0];
const EXPANDED_POSITIONS = [-210, -142, 105, 190, 25, 145, 28, -85];


class BattlegroundCafRoomManagerDialogView extends DialogView {
	static get EVENT_ON_ITEM_KICK_CLICKED() { return BattlegroundPrivatePlayersListView.EVENT_ON_ITEM_KICK_CLICKED };
	static get EVENT_ON_CANCEL_KICK_CLICKED() { return BattlegroundPrivatePlayersListView.EVENT_ON_CANCEL_KICK_CLICKED };
	static get EVENT_ON_INVITE_FRIEND_CLICKED() { return BattlegroundPrivatePlayersListView.EVENT_ON_INVITE_CLICKED };
	static get EVENT_ON_CANCEL_READY_BTN_CLICKED() { return "EVENT_ON_CANCEL_READY_BTN_CLICKED" };
	static get EVENT_ON_ROUND_START_BTN_CLICKED() { return "EVENT_ON_ROUND_START_BTN_CLICKED" };
	static get EVENT_ON_READY_BTN_CLICKED() { return "EVENT_ON_READY_BTN_CLICKED" };


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
		let filteredFriendsList = this._searchFriendsAndObserversList(this.uiInfo.playersListData);
		this._fPlayersListView_bpplv.update(filteredFriendsList, this.uiInfo.reinviteDataCount);
		if (this.currentListLength != filteredFriendsList.length) {
			this.currentListLength = filteredFriendsList.length;
			this._fObserversScrollBar_vsb._updateSlider();
			if (this.currentListLength <= 5) {
				if (this._fObserversSlider_vs.visible) {
					this._fObserversSlider_vs.visible = false;
				}
			} else {
				if (!this._fObserversSlider_vs.visible) {
					this._fObserversSlider_vs.visible = true;
				}
			}
		}

	}

	_searchFriendsAndObserversList(rawData) {
		if(!rawData){
			rawData = [];
		}
		let filteredData = [];
		if (this._searchValue_str == "" && !!rawData) {
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


		return filteredData || [];
	}

	validateFriendsListView() {
		if (!this.uiInfo.friendsListData) {

			return;
		}
	}

	get showSearchTip() {
		if (this._searchValue_str.length == 0) return true;
	}

	//INIT VIEW...
	_initDialogView() {
		
		super._initDialogView();

		this._overlayContainer.visible = false;

		//this._baseContainer.position.set(0, 0);
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


	_updateArea()
	{
		if(APP.screenWidth >= APP.screenHeight)
		{
			this._baseContainer.position.set(-260, 48);
			this._buttonsContainer.position.set(120, 0);
			this._overlay.scale.set(1,1);
			this._overlay.position.set(-225, -150);
		}else{
			this._baseContainer.position.set(235, 230);
			this._buttonsContainer.position.set(605, 200);
			this._overlay.scale.set(1,1.3);
			this._overlay.position.set(-235, -170);
		}
	}

	_addDialogBase() {
		this._fBtnType = BACK_TYPE.RETURN_TO_LOBBY;

		//OVERLAY...
		let lOverlay_g = new PIXI.Graphics();
		lOverlay_g.beginFill(0x000000).drawRect(0, 0, 260, 360).endFill();
		lOverlay_g.alpha = 1;
		lOverlay_g.position.set(-225, -150);
		this._overlay = this._baseContainer.addChild(lOverlay_g);
		//...OVERLAY

		//DIALOG BASE...

		/*this._lFrame_g = this.addChild(APP.library.getSprite("dialogs/battleground/frame"));
		this._lFrame_g.scale.set(1.159, 1);
		this._baseContainer.addChild(this._lFrame_g);
		this._lFrame_g.position.set(0, 25);*/

		//...DIALOG BASE

		//LOGO...
		this._logo = I18.generateNewCTranslatableAsset("TABattlegroundDialogLogo");
		this._logo.position.set(-90, -120);
		this._logo.scale.set(0.7, 0.7);
		this._baseContainer.addChild(this._logo);
		this._baseContainer.position.set(-260, 48);

		//...LOGO

		//TEXTS...
		this._messageContainer.destroyChildren();
		//...TEXTS

		//TABLE...
		this._fPlayersListView_bpplv = new BattlegroundPrivatePlayersListView();
		//this._fPlayersListView_bpplv.position.set(-99, -57);
		this._fPlayersListView_bpplv.on(BattlegroundPrivatePlayersListView.EVENT_ON_ITEM_KICK_CLICKED, this.emit, this);
		this._fPlayersListView_bpplv.on(BattlegroundPrivatePlayersListView.EVENT_ON_CANCEL_KICK_CLICKED, this.emit, this);
		this._fPlayersListView_bpplv.on(BattlegroundPrivatePlayersListView.EVENT_ON_INVITE_CLICKED, this.emit, this);

		let lScrollBack_grphc_observers = new PIXI.Graphics();
		lScrollBack_grphc_observers.beginFill(0x262626).drawRoundedRect(-2.5, -50, 5, 100, 2).endFill();
		let lScrollThumb_grphc_observers = new PIXI.Graphics();
		lScrollThumb_grphc_observers.beginFill(0x5c5c5c).drawRoundedRect(-2.5, -25, 5, 50, 2).endFill();
		this._fMenuSliderObserversThumb = lScrollThumb_grphc_observers;

		this._fObserversSlider_vs = new VerticalSlider(lScrollBack_grphc_observers, lScrollThumb_grphc_observers, undefined, undefined, 0, null, false);
		this._fObserversSlider_vs.scrollMultiplier = 1;
		this._fObserversSlider_vs.position.set(20, 5);
		this.addChild(this._fObserversSlider_vs);

		this._fObserversScrollBar_vsb = this.addChild(new VerticalScrollBar());
		this._fObserversScrollBar_vsb.position.set(-99, -35);
		this._fObserversScrollBar_vsb.visibleArea = new PIXI.Rectangle(-174, -10, 348, 133);
		this._fObserversScrollBar_vsb.hitArea = new PIXI.Rectangle(-174, -10, 370, 134);
		this._fObserversScrollBar_vsb.slider = this._fObserversSlider_vs;
		this._fObserversScrollBar_vsb.scrollableContainer = this._fPlayersListView_bpplv;
		this._fObserversScrollBar_vsb.addChild(this._fPlayersListView_bpplv);
		this._fObserversScrollBar_vsb.enableScroll();
        this._fObserversScrollBar_vsb.enableDrag();

		

		this._baseContainer.addChild(this._fObserversScrollBar_vsb);
		this._baseContainer.addChild(this._fObserversSlider_vs);

		this._ff_seacrh_background = this.addChild(APP.library.getSprite("dialogs/battleground/caf_room_manager/friends_frame_search"));
		this._ff_seacrh_background.position.set(-78, 60);
		this._ff_seacrh_background.scale.set(1.2, 1.3);

		const fontName = "sans-serif";

		const ISearchInputConfig_obj = {
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
			caseSensitive: true
		}


		this._fSearch_tf = this.addChild(new InputField(null, ISearchInputConfig_obj));
		this._fSearch_tf.position.set(-90, -70);



		this._fSearch_tf.on(InputField.EVENT_ON_BLUR, this._onInputBlur, this);
		this._fSearch_tf.on(InputField.EVENT_ON_FOCUS, this._onInputFocus, this);

		this._baseContainer.addChild(this._ff_seacrh_background);
		this._baseContainer.addChild(this._fSearch_tf);

		this._fTip_spr = this.addChild(I18.generateNewCTranslatableAsset('TALobbyNewPlayerEnterTip'));
		this._fTip_spr.position.set(-90, -70);
		this._baseContainer.addChild(this._fTip_spr);

		this._fSearch_tf.on(InputField.EVENT_ON_VALUE_CHANGED, () => {

			this._searchValue_str = this._fSearch_tf.getValue();
			this._fTip_spr.alpha = 0;
		}, this);

		this.alpha = 0;


		setTimeout(() => {
			this.alpha = 1;
			this._updateArea();
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
				lButton_btn = new ActiveGameDialogButton("lobby/battleground/start_round_btn", "TABattlegroundCAFStartRoundButtonCaption", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_ACCEPT, false, true);
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
			this.emit(BattlegroundCafRoomManagerDialogView.EVENT_ON_READY_BTN_CLICKED);
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

	_addGameLockView()
	{
	}

	_alignButtonView(aIntButtonId_int, aButtonsCount_int, aButtonView_domdb) {
		var lXOffset_num = -475;
		var lYOffset_num = 0;

		switch (aIntButtonId_int) {
			case 0:
			case 2:

				lYOffset_num = -45;
				break;
			case 1:
				lYOffset_num = 68;
				break;
			case 3:
				lYOffset_num = 10;
				break;
			case 4:
				lYOffset_num = 30;
				break;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num + 220);
	}
	//...INIT VIEW

	_validateManagerStateButtons() {

		this._fPlaceBetsController_pbsc = APP.gameController.placeBetsController;
		if(!this._gamePlayersControllerInfo){
			this._gamePlayersControllerInfo = APP.gameController.gameplayController.gamePlayersController.info;
		}

		const masterSeatDefined = this._gamePlayersControllerInfo.isMasterSeatDefined;

		let lUIInfo_bcrmdi = this.uiInfo;

		// READY BUTTON...
		this.readyButton.visible = !lUIInfo_bcrmdi.isReadyConfirmed;
		let lIsReadyButtonEnabled_bl = this.readyButton.visible && !lUIInfo_bcrmdi.readyButtonClicked && !lUIInfo_bcrmdi.isGameStateQualify && masterSeatDefined;

		if (lIsReadyButtonEnabled_bl) {
			this.okButton.enabled = true;
			this.okButton.alpha = 1;
		}
		else {
			this.readyButton.enabled = false;
			this.readyButton.alpha = 0.5;
		}
		// ...READY BUTTON

		// CANCEL READY BUTTON...
		this.cancelReadyButton.visible = !this.readyButton.visible;
		let lIsCancelReadyButtonEnabled_bl = this.cancelReadyButton.visible && !lUIInfo_bcrmdi.cancelButtonClicked && lUIInfo_bcrmdi.isReadyConfirmed && !lUIInfo_bcrmdi.isGameStateQualify;
		

		if (lIsCancelReadyButtonEnabled_bl) {
			this.cancelReadyButton.enabled = true;
			this.cancelReadyButton.alpha = 1;
		}
		else {
			this.cancelReadyButton.enabled = false;
			this.cancelReadyButton.alpha = 0.5;
		}
		// ...CANCEL READY BUTTON

		// START ROUND BUTTON...
		this.startRoundButton.visible = true;
		let lIsStartRoundButtonEnabled_bl = this.startRoundButton.visible
			&& lUIInfo_bcrmdi.isReadyConfirmed
			&& !lUIInfo_bcrmdi.isCancelReadyTriggered
			&& !lUIInfo_bcrmdi.isPlayerSitOutState
			&& !lUIInfo_bcrmdi.isGameStateQualify
			&& lUIInfo_bcrmdi.readyPlayersAmount >= APP.minimalCafPlayersReady;


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
		this.backButton.visible = false;
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