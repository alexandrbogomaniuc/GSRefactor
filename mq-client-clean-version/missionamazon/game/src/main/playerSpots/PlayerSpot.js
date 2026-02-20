import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { WEAPONS } from '../../../../shared/src/CommonConstants';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import ProfileAvatar from '../../ui/profile/ProfileAvatar';
import TextField from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import WeaponSpotController from '../../controller/uis/weapons/WeaponSpotController';
import PlayerSpotWeaponsBack from './PlayerSpotWeaponsBack';
import { Z_INDEXES } from '../GameField';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

const WEAPON_SPOT_VIEW_SCALE = 1;




class PlayerSpot extends Sprite
{
	static get EVENT_CHANGE_WEAPON() 			{return 'EVENT_CHANGE_WEAPON'};
	static get WEAPON_SCALE() 					{return WEAPON_SPOT_VIEW_SCALE};

	get currentWeaponId()
	{
		return this._fWeaponSpotController_wsc ? this._fWeaponSpotController_wsc.info.currentWeaponId : WEAPONS.DEFAULT;
	}

	get player()
	{
		return this._fPlayer_obj;
	}

	set currentScore(aVal_num)
	{
		if (this._fPlayer_obj)
		{
			this._fPlayer_obj.currentScore = aVal_num;
		}
	}

	get currentScore()
	{
		return this._fPlayer_obj ? this._fPlayer_obj.currentScore : null;
	}

	get gunCenter()
	{
		return this._gunCenter;
	}

	get globalGunCenter()
	{
		return this.localToGlobal(this._gunCenter.x, this._gunCenter.y);
	}

	get weaponSpotView()
	{
		return this._weaponSpotView;
	}

	get muzzleTipGlobalPoint()
	{
		let lGunCenter = this.gunCenter;

		if (!this.weaponSpotView || !this.weaponSpotView.gun)
		{
			return this.localToGlobal(lGunCenter.x, lGunCenter.y);
		}

		let weaponView = this.weaponSpotView;
		let weaponGun = weaponView.gun;

		let muzzleTipOffset = weaponGun.muzzleTipOffset;
		if (!muzzleTipOffset)
		{
			return this.localToGlobal(lGunCenter.x, lGunCenter.y);
		}

		let startPos = new PIXI.Point();
		let startAngle = weaponView.rotation;
		let sign = this.isBottom ? 1 : -1;
		startPos.x = this.position.x + weaponView.x * sign;
		startPos.y = this.position.y + weaponView.y * sign;
		let gunPos = lGunCenter;
		startPos.x += gunPos.x * this.scale.x;
		startPos.y += gunPos.y * this.scale.y;

		let lGunPushEffectCurrentDistance_num = weaponGun.y; /*gun push effect might be in progress*/
		let dist = ( (muzzleTipOffset * weaponGun.i_getWeaponScale()) - lGunPushEffectCurrentDistance_num) * this.scale.x * sign * PlayerSpot.WEAPON_SCALE;

		startPos.x -= Math.cos(startAngle + Math.PI/2)*dist;
		startPos.y -= Math.sin(startAngle + Math.PI/2)*dist;

		return startPos;
	}

	get isWeaponChangeEffectsInProgress()
	{
		return false;
	}

	get spotVisualCenterPoint()
	{
		return new PIXI.Point(this._avatarCenterPoint.x + this._gunCenter.x, this._avatarCenterPoint.y);
	}

	get avatarCenterPoint()
	{
		return this._avatarCenterPoint;
	}

	get isBottom()
	{
		return this._isBottom;
	}

	get isMaster()
	{
		return this._fPlayer_obj.master;
	}

	get id()
	{
		return this._fPlayer_obj.seatId;
	}

	get initialPosition()
	{
		return this._fDefaultPosition_obj;
	}

	updateAvatar(data)
	{
		this._updateAvatar(data);
	}

	changeWeapon(aWeaponId_int, aCurrentDefaultWeaponId_int, aIsSkipAnimation_bl = false, aIsNewAwardedLevelUp_bl = false)
	{
		this._changeWeapon(aWeaponId_int, aCurrentDefaultWeaponId_int, aIsSkipAnimation_bl, aIsNewAwardedLevelUp_bl);
	}

	_getSpotTextureByName(aName_str)
	{
		_initSpotTextures();

		return Utils.getTexture(player_spot_textures, aName_str);
	}


	bounceEffect()
	{
		this._showBounceEffect();
	}

	showFlashInstantKill()
	{
		this._showFlashInstantKill();
	}

	showInstantKillRecoilEffect()
	{
		this._showInstantKillRecoilEffect();
	}

	get nickname()
	{
		if (this._fPlayer_obj)
		{
			return this._fPlayer_obj.nickname;
		}

		return null;
	}

	blurSpot()
	{
		let blurFilter = new PIXI.filters.BlurFilter();
		blurFilter.blur = 2;

		if (this.weaponSpotView)
		{
			if (this.isMaster || this._fPlayer_obj.specialWeaponId == WEAPONS.INSTAKILL || this._fPlayer_obj.specialWeaponId == WEAPONS.INSTAKILL_POWER_UP)
			{
				this.weaponSpotView.gun.hideAlternate();
				this._fWeaponsBack_pswb.hideGlow();
			}
			this.weaponSpotView.filters = [blurFilter];
		}
		this.filters = [blurFilter];
	}

	focusSpot()
	{
		this.filters = null;

		if (this.weaponSpotView)
		{
			this.weaponSpotView.filters = null;
		}

		this._fWeaponsBack_pswb.showGlow();
	}

	get currentDefaultWeaponId()
	{
		return this._fWeaponSpotController_wsc.info.currentDefaultWeaponId || 1;
	}
	
	set betLevelEmptyButtonRestrictedZone(aRestrictedZone_bl)
	{
		this._fBetLevelEmptyButtonRestrictedZone_bl = aRestrictedZone_bl;
	}

	constructor(aPlayer_obj, aPosition_obj)
	{
		super();

		this._fBgContainer_s = this.addChild(new Sprite);

		this._fPlayer_obj = aPlayer_obj;
		this._fDefaultPosition_obj = aPosition_obj;

		this._fUserStyles_obj = {
			back: this._fPlayer_obj.avatar.background,
			hero: this._fPlayer_obj.avatar.hero,
			border: this._fPlayer_obj.avatar.borderStyle
		};

		this._fAvatar_pa = null;

		this._fInstantKillFlashSprite_sprt = null;

		this._fWeaponSpotController_wsc = null;
		this._fWeaponsBack_pswb = null;

		this._fBetLevelEmptyButtonRestrictedZone_bl = null;

		this._init();

		this._addWeapon();

		this.interactive = true;

		this._fBattlegroundCrown_s = null;

		this.__setRestrictedZone();
	}

	__setRestrictedZone()
	{
		this.mouseover = () => this.setOverRestrictedZone(true);
		this.mouseout = () => this.setOverRestrictedZone(false);
	}

	getBackgroundSprite()
	{
		return this._fBgContainer_s;
	}

	setOverRestrictedZone(aRestricted_bl)
	{
		if (this._fBetLevelEmptyButtonRestrictedZone_bl) return;

		var cursorController = APP.currentWindow.cursorController;
		cursorController.setOverRestrictedZone(aRestricted_bl);
	}

	get _isBottom()
	{
		return Boolean(this._fDefaultPosition_obj.direct == 0);
	}

	get _gunCenter()
	{
		return {x: 36, y: -6};
	}

	get _avatarCenterPoint()
	{
		let lAvatarPosition_obj = this._fAvatar_pa.position;
		lAvatarPosition_obj = this.localToGlobal(lAvatarPosition_obj.x, lAvatarPosition_obj.y, this);

		return new PIXI.Point(lAvatarPosition_obj.x, lAvatarPosition_obj.y);
	}

	get _nicknameStyle()
	{
		let lFontFamily_str = "fnt_nm_barlow_semibold";
		if (!APP.fonts.isGlyphsSupported(lFontFamily_str, this._fPlayer_obj.nickname))
		{
			lFontFamily_str = "sans-serif";
		}

		let lStyle_obj = {
			fontFamily: lFontFamily_str,
			fontSize: 7.5,
			align: "left",
			fill: 0xffffff,
			shortLength: this._maxNicknameWidth // to fit player spot
		}

		return lStyle_obj;
	}

	_init()
	{
		this._addBack();
		this._addNickname();
		this._addAvatar();
		this._addWeaponsBack();
	}

	_getBattlegroundCrown()
	{
		if(!this._fBattlegroundCrown_s)
		{
			let l_s = this.addChild(APP.library.getSpriteFromAtlas("battleground/crown"));

			l_s.visible = true;

			if(this.isMaster)
			{
				l_s.position.set(-107, 0);
			}
			else
			{
				l_s.position.set(-70, 0);
			}

			this._fBattlegroundCrown_s = l_s;
		}

		return this._fBattlegroundCrown_s;
	}

	setBattlegroundCrownVisible(aIsVisible_bl)
	{
		if(
			!aIsVisible_bl &&
			!this._fBattlegroundCrown_s
			)
		{
			return;
		}

		this._getBattlegroundCrown().visible = aIsVisible_bl;
	}
/*
	_onServerMessage(event)
	{
		let messageData = event.messageData;
		let messageClass = messageData.class;

		if(messageClass == SERVER_MESSAGES.KING_OF_HILL_CHANGED)
		{

			let lIsVisible_bl = false;

			if(
				this._fPlayer_obj &&
				this._fPlayer_obj.seatId === messageData.newKing
				)
			{
				lIsVisible_bl = true;
			}

			this._getBattlegroundCrown().visible = lIsVisible_bl;
		}
	}*/

	_addBack()
	{
		let lBack_sprt = this.addChild(APP.library.getSpriteFromAtlas("player_spot/back_small"));
		lBack_sprt.on('pointerdown', (e) => { e.stopPropagation() });
	}

	_addNickname()
	{
		let lNickname_tf = this.addChild(new TextField(this._nicknameStyle));
		lNickname_tf.text = this._fPlayer_obj.nickname;
		lNickname_tf.anchor.set(0, 0.5);
		lNickname_tf.position.set(-18, 0);
		lNickname_tf._fShortLength_num = APP.isMobile ? this._maxNicknameWidth * 2 : this._maxNicknameWidth;
		lNickname_tf.text = lNickname_tf._createShortenedForm(lNickname_tf.text); // this._nicknameStyle not apply _createShortenedForm
		return lNickname_tf;
	}

	get _maxNicknameWidth()
	{
		return 32;
	}

	_addAvatar()
	{
		this._fAvatar_pa = this.addChild(new ProfileAvatar(null, this._fUserStyles_obj));
		this._fAvatar_pa.scale.set(0.314);
		this._fAvatar_pa.position.set(-37, 1);
		this._refreshPlayer();

		return this._fAvatar_pa;
	}

	_addWeaponsBack()
	{
		this._fWeaponsBack_pswb = this.addChild(new PlayerSpotWeaponsBack(this._fPlayer_obj));
		this._fWeaponsBack_pswb.position = this._getWeaponsBackPosition();
		this._fWeaponsBack_pswb.scale = this._getWeaponsBackScale();
	}

	_getWeaponsBackPosition()
	{
		return {x: 35.5, y: -3.5};
	}

	_getWeaponsBackScale()
	{
		return {x: 0.75, y: 0.75};
	}

	_updateAvatar(data)
	{
		this._fUserStyles_obj = {
			back: data.background,
			hero: data.hero,
			border: data.borderStyle
		};

		this._fAvatar_pa.update(this._fUserStyles_obj);
		this._refreshPlayer();
	}

	_refreshPlayer()
	{
		this._fPlayer_obj.avatar = {
			background: this._fUserStyles_obj.back,
			hero: this._fUserStyles_obj.hero,
			borderStyle: this._fUserStyles_obj.border
		};
	}

	_addWeapon()
	{
		let lWeaponSpotSprite_sprt = this.addChild(new Sprite);
		lWeaponSpotSprite_sprt.position.set(this._gunCenter.x, this._gunCenter.y);
		lWeaponSpotSprite_sprt.zIndex = 27022;

		if(!this._isBottom)
		{
			lWeaponSpotSprite_sprt.rotation = Math.PI;
		}

		this._fWeaponSpotController_wsc = new WeaponSpotController(this, lWeaponSpotSprite_sprt, this._fPlayer_obj);
		this._fWeaponSpotController_wsc.i_init();

		this._weaponSpotView = this._fPlayer_obj.weaponSpotView = this._fWeaponSpotController_wsc.view; // [Y]TODO to remove
		this._weaponSpotView.scale.set(WEAPON_SPOT_VIEW_SCALE);

		let lCurrentPlayerWeaponId = this._fPlayer_obj.specialWeaponId;
		if (isNaN(lCurrentPlayerWeaponId))
		{
			lCurrentPlayerWeaponId = this._fPlayer_obj.currentWeaponId;
		}

		if (lCurrentPlayerWeaponId === undefined)
		{
			lCurrentPlayerWeaponId = WEAPONS.DEFAULT;
		}

		let lPlayerInfo = APP.playerController.info;
		let lSpotDefaultWeaponId_int = lPlayerInfo.getTurretSkinId(this._fPlayer_obj.betLevel || lPlayerInfo.roomDefaultBetLevel);

		if (this.isMaster)
		{
			if (lCurrentPlayerWeaponId !== undefined)
			{
				this._changeWeapon(lCurrentPlayerWeaponId, lSpotDefaultWeaponId_int);
			}
		}
		else
		{
			this._changeWeapon(lCurrentPlayerWeaponId, lSpotDefaultWeaponId_int);
		}
	}

	_changeWeapon(aWeaponId_int, aCurrentDefaultWeaponId_int, aIsSkipAnimation_bl = false, aIsNewAwardedLevelUp_bl = false)
	{
		if (this._fWeaponsBack_pswb)
		{
			this._fWeaponsBack_pswb.update(aWeaponId_int, aCurrentDefaultWeaponId_int);
			this.emit(PlayerSpot.EVENT_CHANGE_WEAPON, {weaponId: aWeaponId_int, defaultWeaponId: aCurrentDefaultWeaponId_int, isSkipAnimation_bl: aIsSkipAnimation_bl});
		}
	}

	_showFlashInstantKill()
	{
		let lSequence_arr = [
			{tweens: [{ prop: "alpha", to: 1 }],	duration: 166}, //5 * 2 * 16.6
			{tweens: [],							duration: 332}, //10 * 2 * 16.6
			{tweens: [{ prop: "alpha", to:0 }],		duration: 166}
		];

		Sequence.start(this._fInstantKillFlashSprite_sprt, lSequence_arr);
	}

	_showInstantKillRecoilEffect()
	{
		let lInitialPosition_obj = this._fDefaultPosition_obj;

		let lSequence_arr = [
			{tweens: [{ prop: "x", to: lInitialPosition_obj.x },		{ prop: "y", to: lInitialPosition_obj.y + 10 }],	duration: 66.4},//2 * 2 * 16.6
			{tweens: [{ prop: "x", to: lInitialPosition_obj.x + 14 },	{ prop: "y", to: lInitialPosition_obj.y - 6 }],		duration: 66.4},
			{tweens: [{ prop: "x", to: lInitialPosition_obj.x + 1 },	{ prop: "y", to: lInitialPosition_obj.y - 6 }],		duration: 66.4},
			{tweens: [{ prop: "x", to: lInitialPosition_obj.x + 15 },	{ prop: "y", to: lInitialPosition_obj.y - 1 }],		duration: 99.6},//3 * 2 * 16.6
			{tweens: [{ prop: "x", to: lInitialPosition_obj.x },		{ prop: "y", to: lInitialPosition_obj.y}],			duration: 66.4}
		]

		Sequence.start(this, lSequence_arr);
	}

	_showBounceEffect()
	{
		this.removeTweens();

		let lSequence_arr = [
			{tweens: [{prop:'scale.x', to:0.86}, {prop:'scale.y', to:0.86}], duration:55, ease:Easing.sine.easeOut},
			{tweens: [{prop:'scale.x', to:0.92}, {prop:'scale.y', to:0.92}], duration:55, ease:Easing.sine.easeIn},
			{tweens: [{prop:'scale.x', to:1  }, {prop:'scale.y', to:1  }], duration:55, ease:Easing.sine.easeOut}
		];

		Sequence.start(this, lSequence_arr);
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));

		if (this._fInstantKillFlashSprite_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fInstantKillFlashSprite_sprt));
		}

		this._fWeaponSpotController_wsc && this._fWeaponSpotController_wsc.destroy();

		super.destroy();

		this._fWeaponsBack_pswb && this._fWeaponsBack_pswb.destroy();
		this._fWeaponsBack_pswb = null;

		this._fBetLevelEmptyButtonRestrictedZone_bl = null;

		this._fDefaultPosition_obj = null;
		this._fPlayer_obj = null;
		this._fUserStyles_obj = null;
		this._fAvatar_pa = null;

		this._fInstantKillFlashSprite_sprt = null;

		this._fWeaponSpotController_wsc = null;
	}
}

export default PlayerSpot;
