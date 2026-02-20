import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import NumberValueFormat from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import { DropShadowFilter } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';

class BattlegroundResultPlayersListView extends Sprite
{
	update(aData_obj)
	{
		//DEBUG...
		/*if(
			!aData_obj ||
			aData_obj.length === 0
			)
		{
			aData_obj =
			[
				{
					avatar: {borderStyle: 1, hero: 0, background: 1},
					betLevel: 1,
					currentScore: 12132,
					enterDate: 1627530911507,
					id: 1,
					level: 1,
					nickname: "Peter",
					roundWin: 1230,
					specialWeaponId: -1,
					totalDamage: 10,
					totalScore: 12132,
					unplayedFreeShots: 0,
				},
				{
					avatar: {borderStyle: 1, hero: 0, background: 1},
					betLevel: 1,
					currentScore: 2150,
					enterDate: 1627530911507,
					id: 0,
					level: 1,
					nickname: "Killer12",
					roundWin: 0,
					specialWeaponId: -1,
					totalDamage: 0,
					totalScore: 2150,
					unplayedFreeShots: 0,
				},
				{
					avatar: {borderStyle: 1, hero: 0, background: 1},
					betLevel: 1,
					currentScore: 1110,
					enterDate: 1627530911507,
					id: 2,
					level: 1,
					nickname: "TESTPLAYER2",
					roundWin: 0,
					specialWeaponId: -1,
					totalDamage: 0,
					totalScore: 1110,
					unplayedFreeShots: 0,
				}

			]
		}*/

		//...DEBUG

		this._update(aData_obj);
	}

	constructor()
	{
		super();

		this._fRows_arr = null;
		this._fPostitions_arr = null;
		this._fNicknames_arr = null;
		this._fDamage_arr = null;
		this._fCrowns_arr = null;
		this._fPlayerRowId_num = null;
		this._init();
	}

	_init()
	{
		this._fRowsBacks_arr = [];
		this._fPostitions_arr = [];
		this._fNicknames_arr = [];
		this._fDamage_arr = [];
		this._fCrowns_arr = [];

		
		let lStroke_spr = this.addChild(APP.library.getSpriteFromAtlas("round_result/btg/score_stroke"));
		lStroke_spr.position.set(0,0);

		let lAlpha_num = 1;
		for (let i = 0; i < 6; ++i)
		{
			this._addRow(-89 + 35 * i, i, lAlpha_num);

			lAlpha_num -= 0.1;
		}
	}

	_addRow(aPositionY_num, aId_num, aAlpha_num)
	{
		let lRow_sprt = this.addChild(new Sprite());
		lRow_sprt.position.set(0, aPositionY_num);

		let lBack_grphc = lRow_sprt.addChild(new PIXI.Graphics());
		lBack_grphc.beginFill(0x292929, aAlpha_num).drawRoundedRect(-136, -14, 272, 28, 3);

		var dropShadowFilter = new DropShadowFilter();
		dropShadowFilter.alpha = 0.6;
		dropShadowFilter.blur = 1;
		dropShadowFilter.distance = 2;
		dropShadowFilter.rotation = 90;

		lBack_grphc.filters = [dropShadowFilter];


		this._fRowsBacks_arr.push(lBack_grphc);

		let l_s = lRow_sprt.addChild(APP.library.getSpriteFromAtlas("battleground/crown"));
		l_s.position.x = 120;
		l_s.visible = (aId_num === 0);
		this._fCrowns_arr.push(l_s);

		let lPositionText_tf = lRow_sprt.addChild(new TextField(this._positionStyle));
		lPositionText_tf.anchor.set(0.5, 0.5);
		lPositionText_tf.position.set(-123, 0.5);
		lPositionText_tf.text = (aId_num + 1);

		this._fPostitions_arr.push(lPositionText_tf);

		let lHr_spr = lRow_sprt.addChild(APP.library.getSpriteFromAtlas("round_result/btg/line_hr_white"));
		lHr_spr.position.set(-113, -1);

		let lNicknameText_tf = lRow_sprt.addChild(new TextField(this._nicknameStyle));
		lNicknameText_tf.maxWidth = 160;
		lNicknameText_tf.anchor.set(0, 0.5);
		lNicknameText_tf.position.set(-106, 1);
		lNicknameText_tf.text = "-";

		this._fNicknames_arr.push(lNicknameText_tf);

		let lDamageText_cta = lRow_sprt.addChild(new TextField(this._damageStyle));
		lDamageText_cta.anchor.set(0, 0.5);
		lDamageText_cta.position.set(100, 1);
		lDamageText_cta.maxWidth = 60;
		lDamageText_cta.text = "-";

		this._fDamage_arr.push(lDamageText_cta);
	}

	_update(aData_obj)
	{
		let lPayerId_num = null;
		let lIsCrownReuired_bl = false;

		for (let i = 0; i < this._fNicknames_arr.length; ++i)
		{
			this._updateNicknameText(this._fNicknames_arr[i], (aData_obj[i] ? aData_obj[i].nickname : "-"));

			let lScore_num = undefined;

			if(
				aData_obj &&
				aData_obj[i] &&
				aData_obj[i].battlegroundInfo
				)
			{
				lScore_num = aData_obj[i].battlegroundInfo.score;

				if(aData_obj[i].battlegroundInfo.rank !== 1)
				{
					lIsCrownReuired_bl = true;
				}
			}

			if(lScore_num === undefined)
			{
				lScore_num = -1;
			}


			this._fDamage_arr[i].text = lScore_num >= 0 ? NumberValueFormat.formatMoney(Math.floor(lScore_num), true, 0) : "-";
			this._fDamage_arr[i].maxWidth = (i === 0) ? 75 : 94;
			this._fDamage_arr[i].position.x = 100 - this._fDamage_arr[i].width; // align: "right" immitation
			if (aData_obj[i] && APP.playerController.info.nickname == aData_obj[i].nickname)
			{
				lPayerId_num = i;
			}
		}

		for (let i = 0; i < this._fNicknames_arr.length; ++i)
		if(
			aData_obj &&
			aData_obj[i] &&
			aData_obj[i].battlegroundInfo &&
			aData_obj[i].battlegroundInfo.pot > 0 &&
			aData_obj[i].battlegroundInfo.score > 0
			)
				{
					this._fCrowns_arr[i].visible = true;
				}
		else
				{
					this._fCrowns_arr[i].visible = false;
				}

		if (this._fPlayerRowId_num !== lPayerId_num)
		{
			if (this._fPlayerRowId_num !== null)
			{
				this._resetRow()
			}

			this._setMainPlayerRow(lPayerId_num);
		}
	}

	_updateNicknameText(nickname_tf, value)
	{
		let fontName = "fnt_nm_barlow_bold";
		if (APP.fonts.isGlyphsSupported(this._nicknameStyle.fontFamily, value))
		{
			fontName = this._nicknameStyle.fontFamily;
		}
		
		let txtStyle = nickname_tf.getStyle() || {};
		txtStyle.fontFamily = fontName;
		nickname_tf.textFormat = txtStyle;
		
		nickname_tf.text = value;
	}

	_resetRow()
	{
		let lOldPlayerRowBack_grphc = this._fRowsBacks_arr[this._fPlayerRowId_num];
		lOldPlayerRowBack_grphc.clear();
		lOldPlayerRowBack_grphc.beginFill(0x292929, 0.8).drawRoundedRect(-136, -14, 272, 28, 3);

		let lOldPlayerRowNickname_tf = this._fNicknames_arr[this._fPlayerRowId_num];
		lOldPlayerRowNickname_tf.textFormat = this._nicknameStyle;

		let lOldPlayerRowDamage_cta = this._fDamage_arr[this._fPlayerRowId_num];
		lOldPlayerRowDamage_cta.textFormat = this._damageStyle;

		let lOldPlayerRowPosition_cta = this._fPostitions_arr[this._fPlayerRowId_num];
		lOldPlayerRowPosition_cta.textFormat = this._positionStyle;
	}

	_setMainPlayerRow(lPayerId_num)
	{ 
		this._fPlayerRowId_num = lPayerId_num;
		let lPlayerRowBack_grphc = this._fRowsBacks_arr[this._fPlayerRowId_num];
		lPlayerRowBack_grphc.clear();
		lPlayerRowBack_grphc.beginFill(0xfccc32, 1).drawRoundedRect(-136, -14, 272, 28, 3);

		let lPlayerPosition_tf = this._fPostitions_arr[this._fPlayerRowId_num];
		lPlayerPosition_tf.textFormat = this._mainPositionStyle;

		let lPlayerRowNickname_tf = this._fNicknames_arr[this._fPlayerRowId_num];
		lPlayerRowNickname_tf.textFormat = this._mainPlayerNicknameStyle;

		let lPlayerRowDamage_cta = this._fDamage_arr[this._fPlayerRowId_num];
		lPlayerRowDamage_cta.textFormat = this._damageMainStyleFill;
	}

	get _damageStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 17,
			align: "right", //don't work for one line
			fill: 0xffffff,
			dropShadow: false,
			bold: true

		};

		return lStyle_obj;
	}

	get _positionStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 12,
			align: "center",
			fill: 0xffffff,
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: true
		};

		return lStyle_obj;
	}

	get _mainPositionStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 13,
			align: "center",
			fill: 0x000000,
			dropShadow: true,
			dropShadowColor: 0xffffff,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: true
		};

		return lStyle_obj;
	}

	get _nicknameStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 15,
			align: "left",
			fill: 0xffffff,
			dropShadow: true,
			shortLength: 160,
			dropShadowColor: 0x000000,
			dropShadowAngle: 131,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: false,
			letterSpacing: 1
		};

		return lStyle_obj;
	}

	get _mainPlayerNicknameStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 15,
			align: "left",
			fill: 0x000000,
			dropShadow: true,
			shortLength: 160,
			dropShadowColor: 0x000000,
			dropShadowAngle: 131,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5,
			bold: false,
			letterSpacing: 1
		};

		return lStyle_obj;
	}

	get _damageMainStyleFill()
	{
		let lStyle_obj = {
			align: "right", //don't work for one line
			fill: 0x000000
		};

		return lStyle_obj;
	}

	destroy()
	{
		super.destroy();

		this._fRows_arr = null;
		this._fPlayerRowId_num = null;
	}
}

export default BattlegroundResultPlayersListView;