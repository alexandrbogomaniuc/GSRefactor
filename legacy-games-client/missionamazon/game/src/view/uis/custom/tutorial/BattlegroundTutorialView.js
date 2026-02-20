import { APP } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import SimpleUIView from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView";
import { DropShadowFilter } from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters";
import I18 from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18";
import { Sprite } from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import { FRAME_RATE } from "../../../../../../shared/src/CommonConstants";
import { Sequence } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation";
import { Utils } from "../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils";
import Timer from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer";


class BattlegroundTutorialView extends SimpleUIView
{
	static get DO_NOT_SHOW_AGAIN_BUTTON_CLICKED()	{ return 'DO_NOT_SHOW_AGAIN_BUTTON_CLICKED' }
	static get VIEW_HIDDEN()						{ return 'VIEW_HIDDEN' }

	static get AUTOHIDE_TIME()						{ return 10000 }

	constructor()
	{
		super();
		this._fBaseContainer_spr = null;
		this._fBackground_g = null;
		this._fHints_g = null;
		this._fShowAgainContainer_spr = null;
		this._fShowAgain_g = null;
		this._fTickSign_spr = null;

		this._fBGWidth_num = null;
		this._fBGHeight_num = null;

		this._fSpotPosition_num = null;
		
		this._fTimerToHide_t = null;

		this._fAppearAnimation_s = null;
		this._fDisappearAnimation_s = null;

		this._init();
	}

	i_startAppearingAnimation(aData_obj, aOptAutoHide_bl=false)
	{
		this._resetAnimations();

		let lLines_obj_arr = null;
		lLines_obj_arr = this.__prepareBatllegroundLines(aData_obj);
		lLines_obj_arr && this.__drawHints(lLines_obj_arr);

		let l_seq = [
			{
				tweens: [{prop: 'alpha', to: 1}],
				duration: 30 * FRAME_RATE,
				onfinish: ()=>
				{
					if (aOptAutoHide_bl)
					{
						this._fTimerToHide_t = new Timer(this._hideTutorial.bind(this), BattlegroundTutorialView.AUTOHIDE_TIME);
					}

					this._fBaseContainer_spr.on('pointerclick', this._tryToHideByClick, this);
					
					this._resetAnimations();
				}
			}
		];

		this._fAppearAnimation_s = Sequence.start(this._fBackground_g, l_seq);
	}

	i_hideTutorial()
	{
		this._hideTutorial();
	}

	get isDisappearingInProgress()
	{
		return !!this._fDisappearAnimation_s;
	}

	_init()
	{
		this._fBGWidth_num = APP.config.size.width;
		this._fBGHeight_num = APP.config.size.height;
		
		this._fBaseContainer_spr = this.addChild(new Sprite());
		this._fBaseContainer_spr.position.set(-this._fBGWidth_num/2, -this._fBGHeight_num/2);
		this._fBaseContainer_spr.hitArea = new PIXI.Rectangle(0, 0, this._fBGWidth_num, this._fBGHeight_num);

		this._fBackground_g = this._fBaseContainer_spr.addChild(new PIXI.Graphics());
		this._fBackground_g.beginFill(0x000000, 0.5).drawRect(0, 0, this._fBGWidth_num, this._fBGHeight_num).endFill();
		this._fBackground_g.alpha = 0;

		this._clearElements();
		this._clearHints();
	}

	_resetAnimations()
	{
		Sequence.destroy(Sequence.findByTarget(this._fBackground_g));

		this._fAppearAnimation_s = null;
		this._fDisappearAnimation_s = null;
	}

	_getElements(aData_obj)
	{
		this._clearElements();

		let lSWBounds_obj = {
			height: 289,
			width: 140,
			x: -10.5,
			y: 79.5
		};

		this._fScoreboard_spr = this._fBackground_g.addChild(APP.library.getSpriteFromAtlas("tutorial/scoreboard_example"));
		this._fScoreboard_spr.scale.set(1.1);
		this._fScoreboard_spr.position.set(lSWBounds_obj.x + lSWBounds_obj.width/2 + 7, lSWBounds_obj.y + lSWBounds_obj.height/2 + 15);

		let lTSPanelBounds_obj = {
			height: 243,
			width: 38,
			x: 921,
			y: 174.5
		};

		this._fTreasuresPanel_spr = this._fBackground_g.addChild(APP.library.getSpriteFromAtlas("tutorial/treasures_example"));
		this._fTreasuresPanel_spr.scale.set(1.5);
		this._fTreasuresPanel_spr.position.set(lTSPanelBounds_obj.x + lTSPanelBounds_obj.width/2 - 9, lTSPanelBounds_obj.y + lTSPanelBounds_obj.height/2);

		let lRealSpotBounds_obj = aData_obj.mainSpot;
		let lIsBottom_bl = aData_obj.isSpotAtBottom;
		let lInitSpotPositionY_num = lIsBottom_bl ? lRealSpotBounds_obj.y + lRealSpotBounds_obj.height - 65 : lRealSpotBounds_obj.y + 65;
		let lInitSpotPositionX_num = lIsBottom_bl ? lRealSpotBounds_obj.x + lRealSpotBounds_obj.width/2 + 15 : lRealSpotBounds_obj.x + lRealSpotBounds_obj.width/2 + 10;

		let lSpotExample = lIsBottom_bl ? "_bottom" : "";
		this._fSpot_spr = this._fBackground_g.addChild(APP.library.getSpriteFromAtlas("tutorial/spot_example_battleround" + lSpotExample));
		this._fSpot_spr.position.set(lInitSpotPositionX_num, lInitSpotPositionY_num);
	}

	__prepareBatllegroundLines(aData_obj)
	{
		if (!aData_obj)
		{
			return null;
		}

		this._getElements(aData_obj);

		let lPositionId_num = aData_obj.positionId;
		let lRealSpotBounds_obj = aData_obj.mainSpot;
		let lIsBottom_bl = aData_obj.isSpotAtBottom;
		let lInitSpotPositionY_num = lIsBottom_bl ? lRealSpotBounds_obj.y + lRealSpotBounds_obj.height - 25 : lRealSpotBounds_obj.y + lRealSpotBounds_obj.height + 15;

		this._clearHints();

		let lHints_obj_arr = [];

		let lSpotBounds_obj = this._fSpot_spr.getBounds();

		//YOUR POSITION...
		let lPositionOffsets = [
			{ textX: 148, textY: ( APP.isMobile ? -85 : -103 ), x: [5, 40, 175] , y: ( APP.isMobile ? [-50, -84, -84] : [-60, -100, -100] ) },
			{ textX: 35, textY: ( APP.isMobile ? -120 : -163 ), x: [0, 0, lRealSpotBounds_obj.width*1/3, -lRealSpotBounds_obj.width*1/3, 0] , y: ( APP.isMobile ? [-75, -117, -117, -117, -117] : [-75, -160, -160, -160, -160] ) },
			{ textX: -71, textY: -103, x: [-5,-40,-176] , y: [-55, -100, -100] },
			{ textX: 143, textY: 37, x: [-5, 35, 170] , y: [-30, 40, 40] },
			{ textX: -67, textY: 25, x: [-5,-40,-170] , y: [-30, 28, 28] },
			{ textX: -71, textY: 0, x: [-5,-40,-176] , y: [-42, 3, 3] }
		]

		let lSpotOffsets_obj = lPositionOffsets[lPositionId_num];

		let lHint_obj = {
			textAssetName: 'TATutorialYourPosition',
			textPosition:
			{
				x: lRealSpotBounds_obj.x + lSpotOffsets_obj.textX,
				y: lInitSpotPositionY_num + lSpotOffsets_obj.textY
			},
			points: 
			(lPositionId_num == 1) ?
			[
				{ x: lRealSpotBounds_obj.x + lRealSpotBounds_obj.width/2 + lSpotOffsets_obj.x[0], y: lInitSpotPositionY_num + lSpotOffsets_obj.y[0], dot: true},
				{ x: lRealSpotBounds_obj.x + lRealSpotBounds_obj.width/2 + lSpotOffsets_obj.x[1], y: lInitSpotPositionY_num + lSpotOffsets_obj.y[1], dot: false},
				{ x: lRealSpotBounds_obj.x + lRealSpotBounds_obj.width/2 + lSpotOffsets_obj.x[2], y: lInitSpotPositionY_num + lSpotOffsets_obj.y[2], dot: false},
				{ x: lRealSpotBounds_obj.x + lRealSpotBounds_obj.width/2 + lSpotOffsets_obj.x[3], y: lInitSpotPositionY_num + lSpotOffsets_obj.y[3], dot: false},
				{ x: lRealSpotBounds_obj.x + lRealSpotBounds_obj.width/2 + lSpotOffsets_obj.x[4], y: lInitSpotPositionY_num + lSpotOffsets_obj.y[4], dot: false},
			]
			:
			[
				{ x: lRealSpotBounds_obj.x + lRealSpotBounds_obj.width/2 + lSpotOffsets_obj.x[0], y: lInitSpotPositionY_num + lSpotOffsets_obj.y[0], dot: true, diagonal: false},
				{ x: lRealSpotBounds_obj.x + lRealSpotBounds_obj.width/2 + lSpotOffsets_obj.x[1], y: lInitSpotPositionY_num + lSpotOffsets_obj.y[1], dot: false, diagonal: true},
				{ x: lRealSpotBounds_obj.x + lRealSpotBounds_obj.width/2 + lSpotOffsets_obj.x[2], y: lInitSpotPositionY_num + lSpotOffsets_obj.y[2], dot: false, diagonal: false},
			]
		};
		lHints_obj_arr.push(lHint_obj);
		//...YOUR POSITION

		//WEAPON STACK...
		lPositionOffsets = [
			{ textX: -20, textY: 30, x: [-80, 85] , y: [30, 30] }, //top
			{ textX: -20, textY: -22, x: [-80, 85] , y: [-22, -22] }, //bottom
			{ textX: -230, textY: 98, x: [-80, -110, -230] , y: [30, 98, 98] }, //top right
			{ textX: -230, textY: -90, x: [-80, -110, -230] , y: [-22, -90, -90] }, //bottom right
		]

		lSpotOffsets_obj = (!lIsBottom_bl) ? 
			((lPositionId_num != 5) ? lPositionOffsets[0] : lPositionOffsets[2]) 
			: 
			((lPositionId_num != 2) ? lPositionOffsets[1] : lPositionOffsets[3]);

		lHint_obj = {
			textAssetName: 'TATutorialWeaponStack',
			textPosition:
			{
				x: lSpotBounds_obj.x + lSpotBounds_obj.width + lSpotOffsets_obj.textX,
				y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.textY
			},
			points:
			(lPositionId_num != 5 && lPositionId_num != 2) ?
			[
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width + lSpotOffsets_obj.x[0], y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.y[0], dot: true},
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width + lSpotOffsets_obj.x[1], y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.y[1], dot: false},
			]
			:
			[
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width + lSpotOffsets_obj.x[0], y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.y[0], dot: true, diagonal: false},
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width + lSpotOffsets_obj.x[1], y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.y[1], dot: false, diagonal: true},
				{ x: lSpotBounds_obj.x + lSpotBounds_obj.width + lSpotOffsets_obj.x[2], y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.y[2], dot: false, diagonal: false},
			]
		};

		lHints_obj_arr.push(lHint_obj);
		//...WEAPON STACK

		//AMMUNITION...
		lPositionOffsets = [
			{ textX: -88, textY: -32, x: [30, -90] , y: [-33, -33] }, //top
			{ textX: -88, textY: 7, x: [30, -90] , y: [8, 8] }, //bottom
			{ textX: 30, textY: 34, x: [25, 25, 120] , y: [-34, 35, 35] }, //top left
			{ textX: 30, textY: ( APP.isMobile ? -51 : -71 ), x: [25, 25, 120] , y: ( APP.isMobile ? [10, -50, -50] : [0, -70, -70] ) }, //bottom left
		]

		lSpotOffsets_obj = (!lIsBottom_bl) ? 
			((lPositionId_num != 3) ? lPositionOffsets[0] : lPositionOffsets[2]) 
			: 
			((lPositionId_num != 0) ? lPositionOffsets[1] : lPositionOffsets[3]);

		lHint_obj = {
			textAssetName: 'TATutorialAmmunition',
			textPosition:
			{
				x: lSpotBounds_obj.x + lSpotOffsets_obj.textX,
				y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.textY
			},
			points:
			(lPositionId_num != 3 && lPositionId_num != 0) ?
			[
				{ x: lSpotBounds_obj.x + lSpotOffsets_obj.x[0], y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.y[0], dot: true},
				{ x: lSpotBounds_obj.x + lSpotOffsets_obj.x[1], y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.y[1], dot: false},
			]
			:
			[
				{ x: lSpotBounds_obj.x + lSpotOffsets_obj.x[0], y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.y[0], dot: true},
				{ x: lSpotBounds_obj.x + lSpotOffsets_obj.x[1], y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.y[1], dot: false},
				{ x: lSpotBounds_obj.x + lSpotOffsets_obj.x[2], y: lSpotBounds_obj.y + lSpotBounds_obj.height/2 + lSpotOffsets_obj.y[2], dot: false},
			]
		};

		lHints_obj_arr.push(lHint_obj);
		//...AMMUNITION

		let lSWBounds_obj = {
			height: 289,
			width: 140,
			x: -10.5,
			y: 79.5
		};

		//LEADERBOARD...
		lPositionOffsets = [
			{ textX: 115, textY: -25, x: [60, 110, 110, 220] , y: [20, 20, -40, -40] },
			{ textX: 85, textY: 45, x: [55, 190] , y: [30, 30] }, //top left
		]

		lSpotOffsets_obj = (lPositionId_num != 3) ? lPositionOffsets[0] : lPositionOffsets[1];

		lHint_obj = {
			textAssetName: 'TATutorialBattlegroundScoreTab',
			textPosition:
			{
				x: lSWBounds_obj.x + lSWBounds_obj.width/2 + lSpotOffsets_obj.textX,
				y: lSWBounds_obj.y + lSWBounds_obj.height/2 + lSpotOffsets_obj.textY
			},
			points:
			(lPositionId_num != 3) ?
			[
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + lSpotOffsets_obj.x[0], y: lSWBounds_obj.y + lSWBounds_obj.height/2 + lSpotOffsets_obj.y[0], dot: true},
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + lSpotOffsets_obj.x[1], y: lSWBounds_obj.y + lSWBounds_obj.height/2 + lSpotOffsets_obj.y[1], dot: false},
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + lSpotOffsets_obj.x[2], y: lSWBounds_obj.y + lSWBounds_obj.height/2 + lSpotOffsets_obj.y[2], dot: false},
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + lSpotOffsets_obj.x[3], y: lSWBounds_obj.y + lSWBounds_obj.height/2 + lSpotOffsets_obj.y[3], dot: false},
			]
			:
			[
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + lSpotOffsets_obj.x[0], y: lSWBounds_obj.y + lSWBounds_obj.height/2 + lSpotOffsets_obj.y[0], dot: true},
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + lSpotOffsets_obj.x[1], y: lSWBounds_obj.y + lSWBounds_obj.height/2 + lSpotOffsets_obj.y[1], dot: false},
			]
		};

		lHints_obj_arr.push(lHint_obj);
		//...LEADERBOARD

		//PRIZE...
		lHint_obj = {
			textAssetName: 'TATutorialBattlegroundPrize',
			textPosition:
			{
				x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 130,
				y: lSWBounds_obj.y + lSWBounds_obj.height - 35
			},
			points: [
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 55, y: lSWBounds_obj.y + lSWBounds_obj.height - 45, dot: true},
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 200, y: lSWBounds_obj.y + lSWBounds_obj.height - 45, dot: false},
			]
		};

		lHints_obj_arr.push(lHint_obj);

		lHint_obj = {
			textAssetName: 'TATutorialBattlegroundPrizeHint',
			textPosition:
			{
				x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 82,
				y: lSWBounds_obj.y + lSWBounds_obj.height - 15 + (APP.isMobile ? 10 : 0 )
			},
			points: [
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 55, y: lSWBounds_obj.y + lSWBounds_obj.height - 45 , dot: false},
				{ x: lSWBounds_obj.x + lSWBounds_obj.width/2 + 55, y: lSWBounds_obj.y + lSWBounds_obj.height - 45 , dot: false},
			]
		}

		lHints_obj_arr.push(lHint_obj);
		//...PRIZE

		//TREASURES SIDEBAR...
		let lTSPanelBounds_obj = {
			height: 243,
			width: 38,
			x: 911,
			y: 173.5
		};

		let lTSPanelStartPosition_obj = {
			x: lTSPanelBounds_obj.x + lTSPanelBounds_obj.width/2 - 10,
			y: lTSPanelBounds_obj.y + lTSPanelBounds_obj.height/2 + 95 + (APP.isMobile ? -2 : 0 )
		};

		lHint_obj = {
			textAssetName: 'TATutorialCollectTreasures',
			textPosition:
			{
				x: lTSPanelStartPosition_obj.x - 45,
				y: lTSPanelStartPosition_obj.y - 50
			},
			points: [
				{ x: lTSPanelStartPosition_obj.x, y: lTSPanelStartPosition_obj.y - 60, dot: true},
				{ x: lTSPanelStartPosition_obj.x - 200, y: lTSPanelStartPosition_obj.y - 60, dot: false}
			]
		};

		lHints_obj_arr.push(lHint_obj);
		//...TREASURES SIDEBAR

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
					!lPreviousPoint_obj // not the first point
					||
					(
						lPreviousPoint_obj // not linked with previous point
						&& lPoint_obj.x !== lPreviousPoint_obj.x
						&& lPoint_obj.y !== lPreviousPoint_obj.y
						&& !lPoint_obj.diagonal
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

	_clearElements()
	{
		this._fSpot_spr && this._fSpot_spr.destroy();
		this._fScoreboard_spr && this._fScoreboard_spr.destroy();
		this._fTreasuresPanel_spr && this._fTreasuresPanel_spr.destroy();
		this._fProTip_spr && this._fProTip_spr.destroy();
	}

	_getLineStyle()
	{
		return { width: 2, color: 0xffffff };
	}

	_tryToHideByClick(e)
	{
		if (
			!Utils.isPointInsidePolygon(this._fShowAgainContainer_spr.globalToLocal(e.data.local), this._fShowAgainContainer_spr.hitArea) && 
			this._fBackground_g && 
			this._fBackground_g.alpha > 0.5 &&
			this._fTimerToHide_t
		)
		{
			this._hideTutorial();
		}
	}

	_hideTutorial()
	{
		this._clearElements();
		this._clearHints();

		this._fTimerToHide_t && this._fTimerToHide_t.destructor();
		this._fTimerToHide_t = null;

		this._resetAnimations();

		this._fBaseContainer_spr.off('pointerclick', this._tryToHideByClick, this);

		let l_seq = [
			{
				tweens: [{prop: 'alpha', to: 0}],
				duration: 10 * FRAME_RATE,
				onfinish: ()=>
				{
					this._resetAnimations();
					this.visible = false;

					this.emit(BattlegroundTutorialView.VIEW_HIDDEN);
				}
			}
		];

		this._fDisappearAnimation_s = Sequence.start(this._fBackground_g, l_seq);
	}

	_onCheckButtonClicked(e)
	{
		this._fTickSign_spr.visible = !this._fTickSign_spr.visible;
		this.emit(BattlegroundTutorialView.DO_NOT_SHOW_AGAIN_BUTTON_CLICKED, {isDontShowAgainActive: this._fTickSign_spr.visible});
	}

	destroy()
	{
		this._fTimerToHide_t && this._fTimerToHide_t.destructor();
		this._fTimerToHide_t = null;

		this._clearElements();
		this._clearHints();

		this._resetAnimations();

		this._fBaseContainer_spr = null;
		this._fBackground_g = null;
		this._fHints_g = null;
		this._fSpot_spr = null;
		this._fShowAgainContainer_spr = null;
		this._fShowAgain_g = null;
		this._fTickSign_spr = null;

		super.destroy();
	}
}
export default BattlegroundTutorialView;