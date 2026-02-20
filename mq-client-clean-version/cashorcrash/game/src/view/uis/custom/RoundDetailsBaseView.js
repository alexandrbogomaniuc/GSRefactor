import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Button from '../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { StringUtils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import PointerSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/PointerSprite';

class RoundDetailsBaseView extends SimpleUIView
{
	static get EVENT_ON_CLOSE_BTN_CLICKED()		{ return "onCloseBtnClicked"; }
	static get EVENT_ON_ROUND_DETAILD_OPENED()	{ return "onRoundDetailsOpened"; }
	static get EVENT_ON_ROUND_DETAILD_CLOSED()	{ return "onRoundDetailsClosed"; }

	showScreen()
	{
		this.__setFields();
		
		if (!this.visible)
		{
			this._fIntroTransitionAnimation_mtl.play();
		}

		this.emit(RoundDetailsBaseView.EVENT_ON_ROUND_DETAILD_OPENED);
	}

	hideScreen(aOptIsSkipAnimation_bl)
	{
		if (aOptIsSkipAnimation_bl)
		{
			this.hide();
		}
		else
		{
			this._fOutroTransitionAnimation_mtl.play();
		}

		this.emit(RoundDetailsBaseView.EVENT_ON_ROUND_DETAILD_CLOSED);
	}

	// Override
	get detailsWidth()
	{
		return null;
	}

	// Override
	get detailsHeight()
	{
		return null;
	}

	// Override
	get detailsMargin()
	{
		return null;
	}
	
	//INIT...
	constructor()
	{
		super();

		this._fUniqueTokenIndicator_tf = null;
		this._fUniqueTokenIndicatorAdd_tf = null;
		this._fIntroTransitionAnimation_mtl = null;
		this._fOutroTransitionAnimation_mtl = null;
		this._fUniqueTokenClickableArea_ps = null;
	}
	
	__init()
	{
		super.__init();

		let lBackground_gr = this.addChild(new PIXI.Graphics);
		lBackground_gr.beginFill(0x000000).drawRoundedRect(0, 0, this.detailsWidth, this.detailsHeight, 5).endFill();
		lBackground_gr.alpha=0.8;

		let lCloseButton_btn = this.addChild(new Button("dialogs/exit_btn", null, true, undefined, undefined, Button.BUTTON_TYPE_CANCEL));
		lCloseButton_btn.position.set(this.detailsWidth - 20, 20);
		lCloseButton_btn.on("pointerclick", this._onCloseBtnClicked, this);

		let lTitleCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TARoundHistory"));
		lTitleCaption_ta.position.set(this.detailsMargin, 28);

		this.__initFields();

		this._fIntroTransitionAnimation_mtl = new MTimeLine();
		this._fIntroTransitionAnimation_mtl.addAnimation(
			this,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 5, MTimeLine.EASE_IN_OUT]
			]);
		this._fIntroTransitionAnimation_mtl.callFunctionOnStart(this.show, this);

		this._fOutroTransitionAnimation_mtl = new MTimeLine();
		this._fOutroTransitionAnimation_mtl.addAnimation(
			this,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0, 5, MTimeLine.EASE_IN_OUT]
			]);
		this._fOutroTransitionAnimation_mtl.callFunctionOnFinish(this.hide, this);

		this._fUniqueTokenClickableArea_ps = this.addChild(new PointerSprite());
		this._fUniqueTokenClickableArea_ps.on("pointerclick", this._onUniqueTokenClickableAreaClick, this);

		this.hide();
	}
	//...INIT

	__initFields()
	{
		// Override
	}

	__setFields()
	{
		// Override
	}

	initField(aCaptionTag_txt, aPositionY)
	{
		let lFieldCaption_ta = this.addChild(I18.generateNewCTranslatableAsset(aCaptionTag_txt));
		lFieldCaption_ta.position.set(this.detailsMargin, aPositionY);

		let lFieldIndicator_tf = this._generateIndicatorTextField();
		lFieldIndicator_tf.position.set(this.detailsWidth - this.detailsMargin, aPositionY);

		return lFieldIndicator_tf;
	}

	initUniqueTokenFields(aCaptionTag_txt, aPositionY)
	{
		let lUniqueTokenCaption_ta = this.addChild(I18.generateNewCTranslatableAsset(aCaptionTag_txt));
		lUniqueTokenCaption_ta.position.set(this.detailsMargin, aPositionY);
		let lUniqueTokenIndicatorMaxWidth_num = this.detailsWidth - this.detailsMargin * 3 - lUniqueTokenCaption_ta.getLocalBounds().width;

		this._fUniqueTokenIndicator_tf = this.addChild(this._generateIndicatorTextField(true));
		this._fUniqueTokenIndicator_tf.position.set(this.detailsWidth - this.detailsMargin, aPositionY + 3);
		this._fUniqueTokenIndicator_tf.maxWidth = lUniqueTokenIndicatorMaxWidth_num;

		this._fUniqueTokenIndicatorAdd_tf = this.addChild(this._generateIndicatorTextField(true));
		this._fUniqueTokenIndicatorAdd_tf.position.set(this.detailsWidth - this.detailsMargin, aPositionY + 18);
		this._fUniqueTokenIndicatorAdd_tf.maxWidth = lUniqueTokenIndicatorMaxWidth_num;
	}

	_generateIndicatorTextField(aOptIsTokenIndicator_bl)
	{
		let lIndicator_tf = this.addChild(new TextField({fontFamily: "fnt_nm_barlow_semibold", fontSize: aOptIsTokenIndicator_bl ? 12 : 17, letterSpacing: 0.5, padding: 5, fill: 0xFFFFFF}));
		lIndicator_tf.anchor.set(1, 0);
		lIndicator_tf.maxWidth = 300;
		return lIndicator_tf;
	}

	formatDate(aTime_num)
	{
		let lDate_d = new Date(aTime_num);
		let lYear_str = lDate_d.getFullYear();
		let lMonth_str = ('0' + (lDate_d.getMonth() + 1)).slice(-2);
		let lDate_str = ('0' + lDate_d.getDate()).slice(-2);
		let lHours_str = ('0' + lDate_d.getHours()).slice(-2);
		let lMinutes_str = ('0' + lDate_d.getMinutes()).slice(-2);
		let lSeconds_str = ('0' + lDate_d.getSeconds()).slice(-2);
		return lYear_str + '-' + lMonth_str + '-' + lDate_str + '   ' + lHours_str + ':' + lMinutes_str + ':' + lSeconds_str;
	}

	setUniqueToken(aToken_str)
	{
		if (!this._fUniqueTokenIndicator_tf || !this._fUniqueTokenIndicatorAdd_tf) return;

		if (aToken_str.length > 64)
		{
			let lMiddleIndex_int = Math.ceil(aToken_str.length/2);
			this._fUniqueTokenIndicator_tf.text = aToken_str.slice(0, lMiddleIndex_int);
			this._fUniqueTokenIndicatorAdd_tf.text = aToken_str.slice(lMiddleIndex_int);
		}
		else
		{
			this._fUniqueTokenIndicator_tf.text = aToken_str;
			this._fUniqueTokenIndicatorAdd_tf.text = '';
		}

		let lMainIndBounds_r = this._fUniqueTokenIndicator_tf.getBounds();
		
		let lHitPos_p = this.globalToLocal(lMainIndBounds_r.x, lMainIndBounds_r.y);
		let lHitWidth_num = lMainIndBounds_r.width;
		let lHitHeight_num = lMainIndBounds_r.height;

		let lIsAddTextUsed_bl = !!this._fUniqueTokenIndicatorAdd_tf.text && !!this._fUniqueTokenIndicatorAdd_tf.text.length;
		if (lIsAddTextUsed_bl)
		{
			let lAddIndBounds_r = this._fUniqueTokenIndicatorAdd_tf.getBounds();

			lHitWidth_num = Math.max(lHitWidth_num, lAddIndBounds_r.width);
			lHitHeight_num = Math.max(lHitPos_p.y+lHitHeight_num, this.globalToLocal(lAddIndBounds_r.x, lAddIndBounds_r.y).y+lAddIndBounds_r.height);
		}

		this._fUniqueTokenClickableArea_ps.setHitArea(new PIXI.Rectangle(lHitPos_p.x, lHitPos_p.y, lHitWidth_num, lHitHeight_num));
	}

	_onUniqueTokenClickableAreaClick(event)
	{
		StringUtils.copyToClipBoard(this._fUniqueTokenIndicator_tf.text+this._fUniqueTokenIndicatorAdd_tf.text);
	}

	_onCloseBtnClicked()
	{
		this.emit(RoundDetailsBaseView.EVENT_ON_CLOSE_BTN_CLICKED);
	}
}

export default RoundDetailsBaseView;