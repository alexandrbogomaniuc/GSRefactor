import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import GameplayStateScreenProgressSinglePayoutView from '../GameplayStateScreenProgressSinglePayoutView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameButton from '../../../../ui/GameButton';
import Button from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';

class BattlegroundGameplayStateScreenProgressSinglePayoutView extends Sprite
{
	static get EVENT_ON_EJECT_INITIATED()		{ return GameplayStateScreenProgressSinglePayoutView.EVENT_ON_EJECT_INITIATED };

	constructor(aBetIndex_int)
	{
		super();
		
		this._fBetIndex_int = aBetIndex_int;
		this._fCurBetInfo_bi = null;
		this._fIsInitialEjectPositionApplied_bl = false;

		//CONTENT...
		this._addContent();
		//...CONTENT
	}

	get betIndex()
	{
		return this._fBetIndex_int;
	}

	get curBetInfo()
	{
		return this._fCurBetInfo_bi;
	}

	get ejectButton()
	{
		return this._fEject_btn;
	}

	adjust(aBetInfo_bi)
	{
		let l_gpc = APP.gameController.gameplayController;
		let l_gpi = l_gpc.info;
		let l_gpv = l_gpc.view;
		let l_bsc = l_gpc.gamePlayersController.betsController;

		let lEject_btn = this._fEject_btn;
		lEject_btn.visible = l_gpi.serverMultiplierTimeDefined;

		let lIsBetEjectInProgress_bl = l_bsc.isBetCancelInProgress(aBetInfo_bi.betId);
		lEject_btn.enabled = !aBetInfo_bi.isEjected && l_gpi.roundInfo.isRoundPlayActive && !lIsBetEjectInProgress_bl && !l_gpi.gamePlayersInfo.isMasterPlayerLeaveRoomTriggered;

		if (!this._fIsInitialEjectPositionApplied_bl)
		{
			this._fIsInitialEjectPositionApplied_bl = true;
			this._adjustEjectButtonPosition();
		}
		
		this._fCurBetInfo_bi = aBetInfo_bi;
	}

	updateArea()
	{
		this._adjustEjectButtonPosition();
	}

	_adjustEjectButtonPosition()
	{
		let l_gpc = APP.gameController.gameplayController;
		let l_gpv = l_gpc.view;

		let lEject_btn = this._fEject_btn;
		let lEjectPos_p = null;
		if(APP.layout.isPortraitOrientation)
		{
			lEjectPos_p = this.localToLocal(30, 170, l_gpv.ejectButtonsContainer);
		}else{
			lEjectPos_p = this.localToLocal(0, 124, l_gpv.ejectButtonsContainer);
		}
		
		lEject_btn.position.set(lEjectPos_p.x, lEjectPos_p.y);
	}

	_addContent()
	{
		let l_gpv = APP.gameController.gameplayController.view;

		let l_gr = new PIXI.Graphics();
		l_gr = new PIXI.Graphics().beginFill(0xefc033).drawRoundedRect(-85, -18, 170, 36, 5).endFill();

		let lEject_btn = this._fEject_btn = l_gpv.ejectButtonsContainer.addChild(new GameButton(l_gr, "TABTGPayoutEjectButtonLabel", true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lEject_btn.visible = false;
		lEject_btn.on("pointerdown", this._onEjectBtnClicked, this);
	}

	_onEjectBtnClicked(event)
	{
		if(!APP.isMobile && event.data.originalEvent.button != 0) return;
		if (!this._fCurBetInfo_bi)
		{
			throw new Error('Attempt to eject for undefined bet');
			return;
		}

		this.emit(BattlegroundGameplayStateScreenProgressSinglePayoutView.EVENT_ON_EJECT_INITIATED, {betInfo: this._fCurBetInfo_bi});
	}
}

export default BattlegroundGameplayStateScreenProgressSinglePayoutView;