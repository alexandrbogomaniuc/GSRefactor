import { APP } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import SimpleUIView from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView";
import { DropShadowFilter } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';
import I18 from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18";
import { Sprite } from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import { FRAME_RATE } from "../../../../../../shared/src/CommonConstants";
import { Sequence } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation";
import { Utils } from "../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils";
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

class TutorialView extends SimpleUIView
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

		lLines_obj_arr = this.__prepareLines(aData_obj);

		lLines_obj_arr && this.__drawHints(lLines_obj_arr);

		let l_seq = [
			{
				tweens: [{prop: 'alpha', to: 1}],
				duration: 50 * FRAME_RATE,
				onfinish: ()=>
				{
					this._fTimerToHide_t = new Timer(this._hideTutorial.bind(this), TutorialView.AUTOHIDE_TIME);
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
		aStage_s.view.on('pointerdown', this._onViewClicked.bind(this));

		this._fBackground_g = aStage_s.view.addChild(new PIXI.Graphics());
		this._fBackground_g.beginFill(0x000000, 0.5).drawRect(0, 0, this._fBGWidth_num, this._fBGHeight_num).endFill();
		this._fBackground_g.alpha = 0;

		this._clearHints();
	}

	__prepareLines(aData_obj)
	{
		if (!aData_obj)
		{
			return null;
		}

		this._fSpot_spr = this._fBackground_g.addChild(APP.library.getSprite("tutorial/spot_example"));
		this._fSpot_spr.position.set(this._fBGWidth_num/2, this._fBGHeight_num/2);
		this._fSpot_spr.scale.set(1.3);
		this._clearHints();

		let lHints_obj_arr = [];

		let lSpotBounds_obj = this._fSpot_spr.getBounds();
		//AIMING...
		let lHint_obj = {
			textAssetName: 'TATutorialAiming',
			textPosition:
			{
				x: lSpotBounds_obj.x + lSpotBounds_obj.width/2,
				y: lSpotBounds_obj.y - 45
			},
			points: [
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width/2, y: lSpotBounds_obj.y+30, dot: true, resolution: 4},
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width/2, y: lSpotBounds_obj.y-40, dot: false, resolution: 4}
			]
		};

		lHints_obj_arr.push(lHint_obj);
		//...AIMING

		//INCREASE BET LEVEL...
		lHint_obj = {
			textAssetName: 'TATutorialIncreaseBetLevel',
			textPosition:
			{
				x: lSpotBounds_obj.x + lSpotBounds_obj.width - 100,
				y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 - 50
			},
			points: [
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width - 100, y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + 5, dot: true},
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width - 100, y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 - 45, dot: false},
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width + 75, y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 - 45, dot: false},
			]
		};

		lHints_obj_arr.push(lHint_obj);
		//...INCREASE BET LEVEL

		//LOWER BET LEVEL...
		lHint_obj = {
			textAssetName: 'TATutorialLowerBetLevel',
			textPosition:
			{
				x: lSpotBounds_obj.x + 100 + (APP.isMobile ? -15 : 0 ),
				y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 - 50
			},
			points: [
				{ x: lSpotBounds_obj.x + 100, y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + 5, dot: true},
				{ x: lSpotBounds_obj.x + 100, y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 - 45, dot: false},
				{ x: lSpotBounds_obj.x - 60, y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 - 45, dot: false},
			]
		};

		lHints_obj_arr.push(lHint_obj);
		//...LOWER BET LEVEL

		//RETURN TO TURRET...
		lHint_obj = {
			textAssetName: 'TATutorialReturnToTurret',
			textPosition:
			{
				x: lSpotBounds_obj.x + lSpotBounds_obj.width - 35,
				y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + 65
			},
			points: [
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width - 40, y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + 15, dot: true},
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width - 40, y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + 70, dot: false},
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width + 115 + (APP.isMobile ? 5 : 0 ), y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + 70, dot: false},
			]
		};

		lHints_obj_arr.push(lHint_obj);
		//...RETURN TO TURRET

		let lSWBounds_obj = aData_obj.SWPanel;
		let lSWLocalBounds_obj = aData_obj.SWPanel;
		let lSWLineSpaceX = 38;
		let lSWLineSpaceY = 23;
		if(!APP.isMobile)
		{
			lSWLineSpaceY += 7;
			lSWLineSpaceX += 7;
		}
		//SPECIAL WEAPONS...
		lHint_obj = {
			textAssetName: 'TATutorialSpecialWeapons',
			textPosition:
			{
				x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 15,
				y: lSWBounds_obj.y + lSWBounds_obj.height/2 - 15
			},
			points: [
				{ x: lSWBounds_obj.x + lSWLineSpaceX, y: lSWBounds_obj.y + 5, dot: true},
				{ x: lSWBounds_obj.x + lSWLineSpaceX, y: lSWBounds_obj.y - 10, dot: false},
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 10, y: lSWBounds_obj.y - 10, dot: false},
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 10, y: lSWBounds_obj.y + lSWBounds_obj.height - lSWLineSpaceY, dot: false},
				{ x: lSWBounds_obj.x + lSWLineSpaceX, y: lSWBounds_obj.y + lSWBounds_obj.height - lSWLineSpaceY, dot: false},
				{ x: lSWBounds_obj.x + lSWLineSpaceX, y: lSWBounds_obj.y + lSWBounds_obj.height - lSWLineSpaceY - 15, dot: true},
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 10, y: lSWBounds_obj.y + lSWBounds_obj.height/2 - 10, dot: false},
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 120, y: lSWBounds_obj.y + lSWBounds_obj.height/2 - 10, dot: false},
			]
		};

		lHints_obj_arr.push(lHint_obj);
		//...SPECIAL WEAPONS

		let lRealSpotBounds_obj = aData_obj.mainSpot;
		let lInitSpotPositionY_num = aData_obj.isSpotAtBottom ? lRealSpotBounds_obj.y + lRealSpotBounds_obj.height - 35 : lRealSpotBounds_obj.y + 35;
		//YOUR POSITION...
		lHint_obj = {
			textAssetName: 'TATutorialYourPosition',
			textPosition:
			{
				x: lRealSpotBounds_obj.x + 30,
				y: lInitSpotPositionY_num - 25
			},
			points: [
				{ x: lRealSpotBounds_obj.x + 30, y: lInitSpotPositionY_num, dot: true},
				{ x: lRealSpotBounds_obj.x + 30, y: lInitSpotPositionY_num - 20, dot: false},
				{ x: lRealSpotBounds_obj.x + lRealSpotBounds_obj.width*4/5, y: lInitSpotPositionY_num - 20, dot: false},
			]
		};

		lHints_obj_arr.push(lHint_obj);
		//...YOUR POSITION

		//WEAPON SETTINGS...
		let lSettingsPosition_obj = {
		x: APP.isMobile ?  this._fBGWidth_num - 50 :  this._fBGWidth_num - 165,
		y: this._fBGHeight_num - 15
		};

		lHint_obj = {
			textAssetName: APP.isMobile ? 'TATutorialSettings' : 'TATutorialWeaponSettings',
			textPosition:
			{
				x: lSettingsPosition_obj.x - 180,
				y: lSettingsPosition_obj.y - 5
			},
			points: [
				{ x: lSettingsPosition_obj.x, y: lSettingsPosition_obj.y, dot: true},
				{ x: lSettingsPosition_obj.x - 180, y: lSettingsPosition_obj.y, dot: false}
			]
		};

		if(APP.isAutoFireMode || APP.isMobile)lHints_obj_arr.push(lHint_obj);
		//...WEAPON SETTINGS

		let lAutoTargetBounds_obj = aData_obj.autoTargetSwitcher;
		//AUTOTARGET...
		lHint_obj = {
			textAssetName: 'TATutorialLockOntoEnemy',
			textPosition:
			{
				x: lAutoTargetBounds_obj.x + lAutoTargetBounds_obj.width/2 - 80,
				y: lAutoTargetBounds_obj.y + lAutoTargetBounds_obj.height/2 - 53
			},
			points: [
				{ x: lAutoTargetBounds_obj.x + lAutoTargetBounds_obj.width/2, y: lAutoTargetBounds_obj.y + lAutoTargetBounds_obj.height/2 + 5, dot: true},
				{ x: lAutoTargetBounds_obj.x + lAutoTargetBounds_obj.width/2, y: lAutoTargetBounds_obj.y + lAutoTargetBounds_obj.height/2 - 50, dot: false},
				{ x: lAutoTargetBounds_obj.x + lAutoTargetBounds_obj.width/2 - 85, y: lAutoTargetBounds_obj.y + lAutoTargetBounds_obj.height/2 - 50, dot: false},
				{ x: lAutoTargetBounds_obj.x + lAutoTargetBounds_obj.width/2 + 85, y: lAutoTargetBounds_obj.y + lAutoTargetBounds_obj.height/2 - 50, dot: false},
				{ x: lAutoTargetBounds_obj.x + lAutoTargetBounds_obj.width/2, y: lAutoTargetBounds_obj.y + lAutoTargetBounds_obj.height/2 - 50, dot: false}
			]
		};

		if (APP.isMobile)
		{
			lHints_obj_arr.push(lHint_obj);
		}
		//...AUTOTARGET

		return lHints_obj_arr;
	}

	__drawHints(aHints_obj_arr)
	{
		if (!aHints_obj_arr)
		{
			return
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
			let lShowAgainText_ta = this._fShowAgain_g.addChild(I18.generateNewCTranslatableAsset('TATutorialDoNotShowTutorialAgain'));
			lShowAgainText_ta.position.x = 30;

			let lBounds_obj = this._fShowAgainContainer_spr.getLocalBounds();
			this._fShowAgainContainer_spr.hitArea = new PIXI.Rectangle(lBounds_obj.x, lBounds_obj.y, lBounds_obj.width, lBounds_obj.height);
			this._fShowAgainContainer_spr.on('pointerdown', this._onCheckButtonClicked.bind(this));

			this._fTickSign_spr = this._fShowAgainContainer_spr.addChild(new PIXI.Text('\u2713', {fontSize: 26, fill: 0x704604}));
			this._fTickSign_spr.position.set(0, -10)
			this._fTickSign_spr.visible = false;
		}
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
					this.emit(TutorialView.VIEW_HIDDEN);
				}
			}
		];

		Sequence.destroy(Sequence.findByTarget(this._fBackground_g));
		Sequence.start(this._fBackground_g, l_seq);
	}

	_onCheckButtonClicked(e)
	{
		this._fTickSign_spr.visible = !this._fTickSign_spr.visible;
		this.emit(TutorialView.DO_NOT_SHOW_AGAIN_BUTTON_CLICKED);
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
export default TutorialView;