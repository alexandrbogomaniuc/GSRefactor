import GUDialogView from '../GUDialogView';
import GUSLobbyBattlegroundRulesScrollBarView from './GUSLobbyBattlegroundRulesScrollBarView';
import { APP } from '../../../../../unified/controller/main/globals';
import GUSLobbyActiveGameDialogButton from './game/GUSLobbyActiveGameDialogButton';
import I18 from '../../../../../unified/controller/translations/I18';

const HTML_CONTAINER_HEIGHT = 320;

class GUSBattlegroundRulesDialogView extends GUDialogView
{
	constructor()
	{
		super();

		this._fTextContainer_html = null;
		this._fScrollBarView_sbv = null;
	}

	setRulesHtml(aRulesHTML_str)
	{
		let l_html = this._getTextContainer();

		l_html.innerHTML = aRulesHTML_str;

		let l_sbv = this.getScrollBarView();

		l_sbv.setTotalScrollHeightInPixels(l_html.scrollHeight);
		l_sbv.setScrollProgressInPixels(0);
	}

	//INIT VIEW...
	_initDialogView()
	{
		super._initDialogView();

		this._baseContainer.position.set(0, 0);
		this._buttonsContainer.position.set(0, 0);
		this._messageContainer.position.set(-8, -165);
	}

	_getTextContainer()
	{
		if(!this._fTextContainer_html)
		{
			let l_html = document.createElement("div");
			//l_html.style["background-color"] = "blue";
			l_html.style.position = "relative";
			l_html.style.left ="470px";
			l_html.style.top = "270px";
			l_html.style.width = "450px";
			l_html.style.height = HTML_CONTAINER_HEIGHT + "px";
			l_html.style["z-index"] = "999";
			l_html.style["transform"] = "translate(-50%,-50%)";
			l_html.style["overflow-y"] = "scroll";
			l_html.style["-ms-overflow-style"] = 'none';
			l_html.style["scrollbar-width"] = 'none';
			l_html.style["color"] = 'white';
			l_html.className = 'hiddenScrollbar';
			l_html.addEventListener("scroll", this._onScroll.bind(this));


			var style = document.createElement("style");
			style.innerHTML = `.hiddenScrollbar::-webkit-scrollbar {display: none;}`;
			document.head.appendChild(style);

			this._fTextContainer_html = l_html;
		}

		return this._fTextContainer_html;
	}
	
	_addDialogBase()
	{
		//OVERLAY...
		let lOverlay_g = new PIXI.Graphics();
		lOverlay_g.beginFill(0x000000).drawRect(0, 0, 960, 540).endFill();
		lOverlay_g.alpha = 0.8;;
		lOverlay_g.position.set(-960 / 2, -540 / 2);
		this._baseContainer.addChild(lOverlay_g);
		//...OVERLAY

		//DIALOG BASE...
		let lWidth_num = 520;
		let lHeight_num = 500;

		let lFrame_g = new PIXI.Graphics();
		lFrame_g.beginFill(0xffca13).drawRect(0, 0, lWidth_num, lHeight_num).endFill();
		lFrame_g.position.set(-lWidth_num / 2, -lHeight_num / 2);

		this._baseContainer.addChild(lFrame_g);

		lWidth_num--;
		lHeight_num--;

		let l_g = new PIXI.Graphics();
		l_g.beginFill(0x000000).drawRect(0, 0, lWidth_num, lHeight_num).endFill();
		l_g.position.set(-lWidth_num / 2, -lHeight_num / 2);

		this._baseContainer.addChild(l_g);
		//...DIALOG BASE

		this.on('mousemove', this._onMouseMove.bind(this));
	}

	_onMouseMove(e)
	{
		let l_sbv = this.getScrollBarView();

		l_sbv.onMouseMove(
			e.data.global.x - l_sbv.position.x - 960 / 2,
			e.data.global.y - l_sbv.position.y - 540 / 2
			)

	}

	getScrollBarView()
	{
		if(!this._fScrollBarView_sbv)
		{
			this._fScrollBarView_sbv = this.addChild(new GUSLobbyBattlegroundRulesScrollBarView());
			this._fScrollBarView_sbv.position.set(245, -160);
			this._fScrollBarView_sbv.setVisibleHeightInPixels(HTML_CONTAINER_HEIGHT);
			this._fScrollBarView_sbv.setTotalScrollHeightInPixels(HTML_CONTAINER_HEIGHT);
			this._fScrollBarView_sbv.on(GUSLobbyBattlegroundRulesScrollBarView.EVENT_ON_SCROLL, this._onScrollBarScroll.bind(this));
		}
		return this._fScrollBarView_sbv;
	}

	_onScrollBarScroll(event)
	{
		this._getTextContainer().scrollTop = event.scroll;
	}

	showHTMLContainer()
	{
		APP.layout.showHtmlElement(this._getTextContainer());
	}

	get isHTMLContainerActive()
	{
		return !!this._fTextContainer_html && APP.layout.containsHtmlElement(this._fTextContainer_html);
	}

	_onScroll(event)
	{
		this.getScrollBarView().setScrollProgressInPixels(this._fTextContainer_html.scrollTop);
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				lButton_btn = new GUSLobbyActiveGameDialogButton("dialogs/battleground/back_to_lobby_button_base", "TABattlegroundRulesIUnderstand", undefined, undefined, GUSLobbyActiveGameDialogButton.BUTTON_TYPE_ACCEPT);
				lButton_btn.scale.set(1.25, 1.25);
				lButton_btn.isOkButton = true;
				break;
			default:
				throw new Error (`Unsupported button id: ${aIntId_int}`);
		}
		return lButton_btn;
	}

	_alignButtonView (aIntButtonId_int, aButtonsCount_int, aButtonView_domdb)
	{
		var lXOffset_num = 0;
		var lYOffset_num = 0;

		if (aIntButtonId_int === 0)
		{
			lYOffset_num = 200;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	_setMessage(messageId)
	{
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset(messageId);
		msg.position.set(0, -42);
		this._messageContainer.addChild(msg);
	}

	get _supportedButtonsCount ()
	{
		return 1;
	}
}

export default GUSBattlegroundRulesDialogView;