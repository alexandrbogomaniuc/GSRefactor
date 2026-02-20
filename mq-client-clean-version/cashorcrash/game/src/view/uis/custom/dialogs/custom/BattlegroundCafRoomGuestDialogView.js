import DialogView from '../DialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ActiveGameDialogButton from './game/ActiveGameDialogButton';
import DialogButton from '../DialogButton';
import BattlegroundPrivatePlayersListView from './BattlegroundPrivatePlayersListView';
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


class BattlegroundCafRoomGuestDialogView extends DialogView {
	static get EVENT_ON_ITEM_KICK_CLICKED() { return BattlegroundPrivatePlayersListView.EVENT_ON_ITEM_KICK_CLICKED };
	static get EVENT_ON_CANCEL_KICK_CLICKED() { return BattlegroundPrivatePlayersListView.EVENT_ON_CANCEL_KICK_CLICKED };
	static get EVENT_ON_CANCEL_READY_BTN_CLICKED() { return "EVENT_ON_CANCEL_READY_BTN_CLICKED" };
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
		this._setButtonsCount(3);
	}

	get readyButton() {
		return this._getButtonView(0);
	}

	get cancelReadyButton() {
		return this._getButtonView(2);
	}


	get backButton() {
		return this._getButtonView(1);
	}

	validateManagerStateButtons() {
		this._validateManagerStateButtons();
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


	_addGameLockView()
	{
	}

	_updateBackButtonCaption() {
		this.backButton.captionId = this._fBtnType === BACK_TYPE.RETURN_TO_SCOREBOARD ? "TABattlegroundBackToResults" : "TABattlegroundBackToLobbyButtonCaption";
	}

	_addDialogBase() {
		this._fBtnType = BACK_TYPE.RETURN_TO_LOBBY;
		this._messageContainer.destroyChildren();
		this.alpha = 0;
		//...TEXTS

		//TABLE...
		this._baseContainer.visible = false;

		setTimeout(() => {
			this.alpha = 1;
			this._updateArea();
		}, 0);
	}

	
	_updateArea()
	{
		if(APP.screenWidth >= APP.screenHeight)
		{
			this._baseContainer.position.set(-260, 48);
			this._buttonsContainer.position.set(120, 0);
		}else{
			this._baseContainer.position.set(220, 230);
			this._buttonsContainer.position.set(600, 195);
		}
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
		
			default:
				throw new Error(`Unsupported button id: ${aIntId_int}`);
		}
		return lButton_btn;
	}

	_onButtonClicked(event) {
		var button = event.target;

		if (button.isOkButton) {
			this.emit(BattlegroundCafRoomGuestDialogView.EVENT_ON_READY_BTN_CLICKED);
	
		// READY BUTTON...
		}
		else if (button.isCancelButton) {
			//this.uiInfo.seti = false;
			this.emit(DialogView.EVENT_ON_CANCEL_BTN_CLICKED);
		}
		else if (button.isCancelReadyButton) {
			this.emit(BattlegroundCafRoomGuestDialogView.EVENT_ON_CANCEL_READY_BTN_CLICKED);
		}
		

		this.emit(DialogView.EVENT_ON_BTN_CLICKED);
	}

	_alignButtonView(aIntButtonId_int, aButtonsCount_int, aButtonView_domdb) {
		var lXOffset_num = -475;
		var lYOffset_num = 0;

		switch (aIntButtonId_int) {
			case 0:
			case 2:

				lYOffset_num =   -45;
				break;
			case 1:
				lYOffset_num =  68;
				break;
			case 3:
				lYOffset_num =  10;
				break;
			case 4:
				lYOffset_num = 30;
				break;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num+220);
	}
	//...INIT VIEW

	_validateManagerStateButtons() {
		let lUIInfo_bcrmdi = this.uiInfo;

		if(!this._gamePlayersControllerInfo){
			this._gamePlayersControllerInfo = APP.gameController.gameplayController.gamePlayersController.info;
		}

		const masterSeatDefined = this._gamePlayersControllerInfo.isMasterSeatDefined;

		// READY BUTTON...
		this.readyButton.visible = !lUIInfo_bcrmdi.isReadyConfirmed;
		let lIsReadyButtonEnabled_bl = this.readyButton.visible
			&& !lUIInfo_bcrmdi.readyButtonClicked
			&& !lUIInfo_bcrmdi.isGameStateQualify
			&& masterSeatDefined;

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
		let lIsCancelReadyButtonEnabled_bl = this.cancelReadyButton.visible
			&& lUIInfo_bcrmdi.isReadyConfirmed
			&& !lUIInfo_bcrmdi.cancelButtonClicked
			&& !lUIInfo_bcrmdi.isGameStateQualify;

		if (lIsCancelReadyButtonEnabled_bl) {
			this.cancelReadyButton.enabled = true;
			this.cancelReadyButton.alpha = 1;
		}
		else {
			this.cancelReadyButton.enabled = false;
			this.cancelReadyButton.alpha = 0.5;
		}
		// ...CANCEL READY BUTTON


		

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
			this.backButton.alpha = 0.5;
		}
		// ...BACK BUTTON
	}


	get _supportedButtonsCount() {
		return 3;
	}
}

export default BattlegroundCafRoomGuestDialogView;