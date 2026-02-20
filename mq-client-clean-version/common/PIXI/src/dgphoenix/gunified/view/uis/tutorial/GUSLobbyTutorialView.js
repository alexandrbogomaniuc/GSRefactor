import SimpleUIView from '../../../../unified/view/base/SimpleUIView';
import { APP } from '../../../../unified/controller/main/globals';
import Timer from '../../../../unified/controller/time/Timer';
import Sequence from '../../../../unified/controller/animation/Sequence';
import { DropShadowFilter } from '../../../../unified/view/base/display/Filters';
import { Utils } from '../../../../unified/model/Utils';
import Sprite from '../../../../unified/view/base/display/Sprite';
import I18 from '../../../../unified/controller/translations/I18';
import { FRAME_RATE } from '../../../../unified/controller/time/Ticker';

class GUSLobbyTutorialView extends SimpleUIView
{
	static get DO_NOT_SHOW_AGAIN_BUTTON_CLICKED()	{ return 'DO_NOT_SHOW_AGAIN_BUTTON_CLICKED' }
	static get VIEW_HIDDEN()						{ return 'VIEW_HIDDEN' }

	static get AUTOHIDE_TIME()						{ return 10000 }

	constructor()
	{
		super();

		this._fBackground_g = null;
		this._fHints_g = null;
		this._fShowAgainContainer_spr = null;
		this._fShowAgain_g = null;
		this._fTickSign_spr = null;
		this._fTimerToHide_t = null;
		
		this._fBGWidth_num = null;
		this._fBGHeight_num = null;
	}

	i_init(aStage_s)
	{
		this._init(aStage_s);
	}

	i_startAppearingAnimation(aData_obj)
	{
		let lLines_obj_arr = null;

		if (APP.isBattlegroundRoomMode)
		{
			lLines_obj_arr = this.__prepareBatllegroundLines(aData_obj)
		}
		else
		{
			lLines_obj_arr = this.__prepareLines(aData_obj);
		}

		lLines_obj_arr && this.__drawHints(lLines_obj_arr);

		let l_seq = [
			{
				tweens: [{prop: 'alpha', to: 1}],
				duration: 50 * FRAME_RATE,
				onfinish: ()=>
				{
					this._fTimerToHide_t = new Timer(this._hideTutorial.bind(this), GUSLobbyTutorialView.AUTOHIDE_TIME);
					Sequence.destroy(Sequence.findByTarget(this._fBackground_g));
				}
			}
		];

		Sequence.start(this._fBackground_g, l_seq);
	}

	_init(aStage_s)
	{
		this._fBGWidth_num = aStage_s.config.size.width;
		this._fBGHeight_num = aStage_s.config.size.height;

		aStage_s.view.position.set(0, 0);
		aStage_s.view.hitArea = new PIXI.Rectangle(0, 0, this._fBGWidth_num, this._fBGHeight_num);
		aStage_s.view.on('pointerdown', this._onViewClicked, this);

		this._fBackground_g = aStage_s.view.addChild(this.__prepareBackground());
		this._fBackground_g.alpha = 0;

		this._clearHints();
	}

	__prepareBackground()
	{
		return new PIXI.Graphics().beginFill(0x000000, 0.5).drawRect(0, 0, this._fBGWidth_num, this._fBGHeight_num).endFill();
	}

	__prepareLines(aData_obj)
	{
		return null;
	}

	__prepareBatllegroundLines(aData_obj)
	{
		return null;
	}

	__drawHints(aHints_obj_arr)
	{
		if (!aHints_obj_arr)
		{
			return null
		}

		this._fHints_g.lineStyle(this._getLineStyle());
		
		for (let lHint_obj of aHints_obj_arr)
		{
		
			let lPoints_arr = lHint_obj.points;

			for (let i = 0; i < lPoints_arr.length; i++)
			{
				let lPoint_obj = lPoints_arr[i];
				let lPreviousPoint_obj = lPoints_arr[i-1];

				if (
					!lPreviousPoint_obj
					||
					(
						lPreviousPoint_obj
						&& lPoint_obj.x !== lPreviousPoint_obj.x
						&& lPoint_obj.y !== lPreviousPoint_obj.y
					)
				)
				{
					this._fHints_g.moveTo(lPoint_obj.x, lPoint_obj.y);
				}

				this._fHints_g.lineTo(lPoint_obj.x, lPoint_obj.y);

				if (lPoint_obj && lPoint_obj.dot)
				{
					this._fHints_g.beginFill(0xffffff).drawCircle(lPoint_obj.x, lPoint_obj.y, 3).endFill();
					this._fHints_g.moveTo(lPoint_obj.x, lPoint_obj.y);
				}

			}

			let lHintText_t = this._fHints_g.addChild(I18.generateNewCTranslatableAsset(lHint_obj.textAssetName));
			let lTextPosition_obj = lHint_obj.textPosition;
			lHintText_t.position.set(lTextPosition_obj.x, lTextPosition_obj.y);
			lHintText_t.anchor.set(0.5, 1);

			let lPoxitionY_num = APP.isMobile ? this._fBGHeight_num - 60 : this._fBGHeight_num - 50;
			this._fShowAgainContainer_spr = this._fHints_g.addChild(new Sprite());
			this._fShowAgainContainer_spr.position.set(10, lPoxitionY_num);

			this._fShowAgain_g = this._fShowAgainContainer_spr.addChild(new PIXI.Graphics());
			this._fShowAgain_g.beginFill(0xffffff).drawRoundedRect(0, 3, 15, 15, 2).endFill();
			let lShowAgainText_ta = this._fShowAgain_g.addChild(I18.generateNewCTranslatableAsset(this.__showAgainCaptionAssetId));
			lShowAgainText_ta.position.x = 30;

			let lBounds_obj = this._fShowAgainContainer_spr.getLocalBounds();
			this._fShowAgainContainer_spr.hitArea = new PIXI.Rectangle(lBounds_obj.x, lBounds_obj.y, lBounds_obj.width, lBounds_obj.height);
			this._fShowAgainContainer_spr.on('pointerdown', this._onCheckButtonClicked.bind(this));

			this._fTickSign_spr = this._fShowAgainContainer_spr.addChild(new PIXI.Text('\u2713', {fontSize: 26, fill: 0x704604}));
			this._fTickSign_spr.position.set(0, -10)
			this._fTickSign_spr.visible = false;
		}
	}

	get __showAgainCaptionAssetId()
	{
		// must be overridden
		return undefined;
	}

	_clearHints()
	{
		//somewhy clear method doesn't work
		this._fHints_g && this._fHints_g.destroy();
		this._fHints_g = this._fBackground_g.addChild(new PIXI.Graphics());

		this._fHints_g.filters = [ 
			new DropShadowFilter({distance: 0, color: 0x704604, blur: 1, alpha: 1, resolution: 2}),
			new DropShadowFilter({distance: 1, color: 0xffea00, blur: 1, alpha: 1, quality:5, resolution: 2}),
			new DropShadowFilter({distance: 2, color: 0xffea00, blur: 5, alpha: .7, quality:5, resolution: 2})
		];
	}

	_getLineStyle()
	{
		return { width: 2, color: 0xffffff };
	}

	_onViewClicked(e)
	{
		if (
				!Utils.isPointInsidePolygon(this._fShowAgainContainer_spr.globalToLocal(e.data.local), this._fShowAgainContainer_spr.hitArea)
				&& this._fBackground_g && this._fBackground_g.alpha > 0.5
			)
		{
			this._hideTutorial();
		}
	}

	_hideTutorial()
	{
		this._fTimerToHide_t && this._fTimerToHide_t.destructor();

		this._clearHints();

		let l_seq = [
			{
				tweens: [{prop: 'alpha', to: 0}],
				duration: 5 * FRAME_RATE,
				onfinish: ()=>
				{
					Sequence.destroy(Sequence.findByTarget(this._fBackground_g));
					this.emit(GUSLobbyTutorialView.VIEW_HIDDEN);
				}
			}
		];

		Sequence.destroy(Sequence.findByTarget(this._fBackground_g));
		Sequence.start(this._fBackground_g, l_seq);
	}

	_onCheckButtonClicked(e)
	{
		this._fTickSign_spr.visible = !this._fTickSign_spr.visible;
		this.emit(GUSLobbyTutorialView.DO_NOT_SHOW_AGAIN_BUTTON_CLICKED);
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this._fBackground_g));

		this._fBackground_g = null;
		this._fHints_g = null;
		this._fShowAgainContainer_spr = null;
		this._fShowAgain_g = null;
		this._fTickSign_spr = null;

		this._fTimerToHide_t && this._fTimerToHide_t.destructor();
		this._fTimerToHide_t = null;

		super.destroy();
	}
}
export default GUSLobbyTutorialView;