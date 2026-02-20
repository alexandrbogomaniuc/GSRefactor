import GameplayInfo from '../../../../model/gameplay/GameplayInfo';
import RoundDetailsBaseView from "../RoundDetailsBaseView";
import { APP } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";

class BattlegroundRoundDetailsView extends RoundDetailsBaseView
{
	static get ROUND_DETAILS_WIDTH () 			{ return 485; }
	static get ROUND_DETAILS_HEIGHT () 			{ return 230; }
	static get ROUND_DETAILS_MARGIN () 			{ return 15; }

	// Override
	get detailsWidth()
	{
		return BattlegroundRoundDetailsView.ROUND_DETAILS_WIDTH;
	}

	// Override
	get detailsHeight()
	{
		return BattlegroundRoundDetailsView.ROUND_DETAILS_HEIGHT;
	}

	// Override
	get detailsMargin()
	{
		return BattlegroundRoundDetailsView.ROUND_DETAILS_MARGIN;
	}
	
	constructor()
	{
		super();

		this._fRoundStartTimeIndicator_tf = null;
		this._fCrashPointIndicator_tf = null;
		this._fPotIndicator_tf = null;
		this._fPotWinnerIndicator_tf = null;
		this._fCurrentPlayerPlaceIndicator_tf = null;
	}


	// Override
	__initFields()
	{
		this._fRoundStartTimeIndicator_tf = this.initField("TARoundStartTime", 67);
		this._fCrashPointIndicator_tf = this.initField("TACrashPoint", 92);
		this._fPotIndicator_tf = this.initField("TARoundsHistoryPot", 117);
		this._fPotWinnerIndicator_tf = this.initField("TARoundsHistoryWinner", 142);
		this._fCurrentPlayerPlaceIndicator_tf = this.initField("TARoundsHistoryPlayerPlace", 167);
		this.initUniqueTokenFields("TAUniqueToken", 192);
	}

	// Override
	__setFields()
	{
		this._fRoundStartTimeIndicator_tf && (this._fRoundStartTimeIndicator_tf.text = this.formatDate(this.uiInfo.roundStartTime));
		this._fCrashPointIndicator_tf && (this._fCrashPointIndicator_tf.text = GameplayInfo.formatMultiplier(this.uiInfo.multiplier));
		this._fPotIndicator_tf && (this._fPotIndicator_tf.text = this.uiInfo.totalPot);
		this._fPotWinnerIndicator_tf && (this._fPotWinnerIndicator_tf.text = this.uiInfo.winnerPot);
		this._fCurrentPlayerPlaceIndicator_tf && (this._fCurrentPlayerPlaceIndicator_tf.text = this.uiInfo.currentPlayerPlace);  
		this.setUniqueToken(this.uiInfo.uniqueToken);
	}
}

export default BattlegroundRoundDetailsView;