import GameplayInfo from '../../../model/gameplay/GameplayInfo';
import RoundDetailsBaseView from './RoundDetailsBaseView';

class RoundDetailsView extends RoundDetailsBaseView
{
	static get ROUND_DETAILS_WIDTH () 			{ return 485; }
	static get ROUND_DETAILS_HEIGHT () 			{ return 180; }
	static get ROUND_DETAILS_MARGIN () 			{ return 15; }

	// Override
	get detailsWidth()
	{
		return RoundDetailsView.ROUND_DETAILS_WIDTH;
	}

	// Override
	get detailsHeight()
	{
		return RoundDetailsView.ROUND_DETAILS_HEIGHT;
	}

	// Override
	get detailsMargin()
	{
		return RoundDetailsView.ROUND_DETAILS_MARGIN;
	}
	
	constructor()
	{
		super();

		this._fRoundStartTimeIndicator_tf = null;
		this._fBetsCountIndicator_tf = null;
		this._fCrashPointIndicator_tf = null;
	}


	// Override
	__initFields()
	{
		this._fRoundStartTimeIndicator_tf = this.initField("TARoundStartTime", 67);
		this._fBetsCountIndicator_tf = this.initField("TABetsCount", 92);
		this._fCrashPointIndicator_tf = this.initField("TACrashPoint", 117)
		this.initUniqueTokenFields("TAUniqueToken", 142);
	}

	// Override
	__setFields()
	{
		this._fRoundStartTimeIndicator_tf && (this._fRoundStartTimeIndicator_tf.text = this.formatDate(this.uiInfo.roundStartTime));
		this._fBetsCountIndicator_tf && (this._fBetsCountIndicator_tf.text = this.uiInfo.betsCount);
		this._fCrashPointIndicator_tf && (this._fCrashPointIndicator_tf.text = GameplayInfo.formatMultiplier(this.uiInfo.multiplier));
		this.setUniqueToken(this.uiInfo.uniqueToken);
	}
}

export default RoundDetailsView;