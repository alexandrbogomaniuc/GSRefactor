<template>
	<div>
		<div id="weapon_slider">
			<div class='weapon_slider_btn' id='weaponPrewBtnContainer'>
				<BaseButton
										@click='handleWeaponPrewButtonClick'
										id='weaponPrewBtn'
										bgAssetName='paytable/pages/enemies/weapon_slider/arrow_btn'
									/>
			</div>
			<div id="back" ref='back'>
				<div id="weapons" ref='weapons'>
				</div>
			</div>
			<div id="weaponsNames" ref='weaponsNames'>
			</div>
			<div class='weapon_slider_btn' id='weaponNextBtnContainer'>
				<BaseButton
										style="transform: scaleX(-1)"
										@click='handleWeapnNextButtonClick'
										id='weaponNextBtn'
										bgAssetName='paytable/pages/enemies/weapon_slider/arrow_btn'
									/>
			</div>
		</div>
		<div id="bet_slider">
			<div class='bet_slider_btn' id='betPrewBtnContainer'>
				<BaseButton
										@click='handleBetPrewButtonClick'
										id='BetPrewBtn'
										bgAssetName='paytable/pages/controls/minus'
									/>
			</div>
			<div id="currentBet" ref='currentBet' style="white-space: nowrap;">
			</div>
			<div class='bet_slider_btn' id='betNextBtnContainer'>
				<BaseButton
										@click='handleBetNextButtonClick'
										id='BetNextBtn'
										bgAssetName='paytable/pages/controls/plus'
									/>
			</div>

			<BaseTextField 
				id="betLevelCapture"
				translatableAssetId='TAPaytableEnemiesBetLevel'
			/>
			
		</div>
	</div>
</template>

<script type="text/javascript">

	import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
	import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
	import VueApplicationController from '../../VueApplicationController';
	import BaseImage from './../base/BaseImage.vue';
	import BaseTextField from './../base/BaseTextField.vue';
	import Vue from 'vue';
	import { WEAPONS } from '../../../../../shared/src/CommonConstants';

	const WEAPONS_IDS_AS_COINS_ARRAY = [
		WEAPONS.DEFAULT,
		WEAPONS.INSTAKILL,
		WEAPONS.RICOCHET,
		WEAPONS.FLAMETHROWER,
		WEAPONS.CRYOGUN,
		WEAPONS.ARTILLERYSTRIKE
	];	

	const WEAPONS_ARRAY = [
		{betLevel: 1,	 weaponID: WEAPONS.DEFAULT}, //turret 1
		{betLevel: 2,	 weaponID: WEAPONS.DEFAULT}, //turret 2
		{betLevel: 3,	 weaponID: WEAPONS.DEFAULT}, //turret 3
		{betLevel: 5,	 weaponID: WEAPONS.DEFAULT}, //turret 4
		{betLevel: 10,	 weaponID: WEAPONS.DEFAULT}, //turret 5
		{betLevel: null, weaponID: WEAPONS.INSTAKILL},
		{betLevel: null, weaponID: WEAPONS.RICOCHET},
		{betLevel: null, weaponID: WEAPONS.FLAMETHROWER},
		{betLevel: null, weaponID: WEAPONS.CRYOGUN},
		{betLevel: null, weaponID: WEAPONS.ARTILLERYSTRIKE}
	];

	const WEAPONS_QUEUE = [
		{weaponID: WEAPONS.DEFAULT},
		{weaponID: WEAPONS.INSTAKILL},
		{weaponID: WEAPONS.RICOCHET},
		{weaponID: WEAPONS.FLAMETHROWER},
		{weaponID: WEAPONS.CRYOGUN},
		{weaponID: WEAPONS.ARTILLERYSTRIKE}
	];


	export default {
		name: 'weapon_slider',

		data() {
			return {
				currentWeaponID: -1,
				currentBetLevel: 1,
				currentWeaponsQueueIndex: 0,
				currentBetLevelQueueIndex: 0,
				currentStake: undefined,
				weapons_arr: [],
				weaponsNames_arr: [],
				weaponsNames_arr_span: [],
				possibleBetLevels: APP.playerController.info.possibleBetLevels,
			}
		},

		methods: {
			init()
			{
				this._addBack();
				this._addWeapons();
				this._updateIndicator();
			},

			updateIndicator(aCurrentWeaponId_int, aCurrentStake_int, aCurrentBetLevel_int)
			{
				if (	this.currentWeaponID == aCurrentWeaponId_int
					&& 	this.currentStake == aCurrentStake_int
					&&	this.currentBetLevel == aCurrentBetLevel_int
						) return;
						
				this.currentWeaponID = aCurrentWeaponId_int;
				this.currentStake = aCurrentStake_int;
				this.currentBetLevel = aCurrentBetLevel_int;

				this.currentWeaponsQueueIndex = WEAPONS_IDS_AS_COINS_ARRAY.indexOf(aCurrentWeaponId_int);
				this.currentBetLevelQueueIndex = this.possibleBetLevels.indexOf(aCurrentBetLevel_int);

				this._updateIndicator();
			},

			_updateIndicator()
			{
				let lWeaponId_int = this.currentWeaponID;
				let lBetLevel_int = this.currentBetLevel;

				for (let i = 0; i < this.weapons_arr.length; i++)
				{
					if(this.weapons_arr[i].id == lWeaponId_int)
					{
						if (this.weapons_arr[i].id == WEAPONS.DEFAULT && this.weapons_arr[i].betLevel != lBetLevel_int)
						{
							Object.assign(this.weapons_arr[i].style, {
							"opacity": "0",
							});
							continue;
						}
						Object.assign(this.weapons_arr[i].style, {
							"opacity": "1",
							});
					}
					else
					{
						Object.assign(this.weapons_arr[i].style, {
							"opacity": "0",
							});
					}
				}

				for (let i = 0; i < this.weaponsNames_arr.length; i++)
				{
					this.weaponsNames_arr_span[i].updateMessage();

					if(this.weaponsNames_arr[i].id == lWeaponId_int)
					{
						if (this.weaponsNames_arr[i].id == WEAPONS.DEFAULT && this.weaponsNames_arr[i].betLevel != lBetLevel_int)
						{
							Object.assign(this.weaponsNames_arr[i].style, {
							"opacity": "0",
							});
							continue;
						}
						Object.assign(this.weaponsNames_arr[i].style, {
							"opacity": "1",
							});
					}
					else
					{
						Object.assign(this.weaponsNames_arr[i].style, {
							"opacity": "0",
							});
					}
				}

				this.$refs.currentBet.innerText = lBetLevel_int;

				let lConfig_obj = {
					weaponId: this.currentWeaponID,
					betLevel: this.currentBetLevel
				}

				this.$emit('weaponIndicatorUpdated', lConfig_obj);
			},

			_addBack()
			{
				let lBeckAssetSpr = this._generateImage("paytable/pages/enemies/weapon_slider/back");

				this.$refs.back.appendChild(lBeckAssetSpr);
			},

			updateTexts()
			{
				for (let i = 0; i < this.weaponsNames_arr.length; i++)
				{
					this.weaponsNames_arr_span[i].updateMessage();
				}
			},

			_addWeapons()
			{
				for (let i = 0; i < WEAPONS_ARRAY.length; i++)
				{
					let lWeaponId_int = WEAPONS_ARRAY[i].weaponID;
					let lBetLevel_int = WEAPONS_ARRAY[i].betLevel;
					let lWeaponConfig_obj = this._getWeaponConfig(lWeaponId_int, lBetLevel_int);

					let lWeapon = this._generateImage(lWeaponConfig_obj.src);
					lWeapon.id = lWeaponId_int;
					lWeapon.betLevel = lBetLevel_int;
					this.$refs.weapons.appendChild(lWeapon);
					this.weapons_arr.push(lWeapon);

					Object.assign(lWeapon.style, {
							"position": "absolute",
							"margin-left": lWeaponConfig_obj.left + "px",
							"margin-top": lWeaponConfig_obj.top + "px",
							});

					let lWeapon_span = this._generateText(lWeaponConfig_obj.ctaAssetID);
					this.weaponsNames_arr_span.push(lWeapon_span);
					let lWeaponName = lWeapon_span.$el;
					lWeaponName.id = lWeaponId_int;
					lWeaponName.betLevel = lBetLevel_int;
					this.$refs.weaponsNames.appendChild(lWeaponName);
					this.weaponsNames_arr.push(lWeaponName);

					lWeapon_span.updateMessage();

					Object.assign(lWeaponName.style, {
							"position": "absolute",
							});
				}
			},

			_generateText(aTranslatableAssetId_str)
			{
				let baseTextFieldInstance = new this.baseTextFieldClass({
					propsData: { translatableAssetId:  aTranslatableAssetId_str}
				});
				baseTextFieldInstance.$mount();

				return baseTextFieldInstance;
			},

			_generateImage(assetName)
			{
				let baseImageInstance = new this.baseImageClass({
					propsData: { assetName:  assetName}
				});
				baseImageInstance.$mount();
				return baseImageInstance.$el;
			},


			_getWeaponConfig(aWeaponId_num, aBetLevel)
			{
				switch(aWeaponId_num)
				{
					case -1:
						switch(aBetLevel)
						{
							case 1:			return {src: "paytable/pages/enemies/weapon_slider/weapons/turret_1", top: -21, left: 3, ctaAssetID: "TAPaytableEnemiesWeaponsTurret"};
							case 2:			return {src: "paytable/pages/enemies/weapon_slider/weapons/turret_2", top: -34, left: 4, ctaAssetID: "TAPaytableEnemiesWeaponsTurret"};
							case 3:			return {src: "paytable/pages/enemies/weapon_slider/weapons/turret_3", top: -43, left: 5, ctaAssetID: "TAPaytableEnemiesWeaponsTurret"};
							case 5:			return {src: "paytable/pages/enemies/weapon_slider/weapons/turret_4", top: -54, left: 0, ctaAssetID: "TAPaytableEnemiesWeaponsTurret"};
							case 10:		return {src: "paytable/pages/enemies/weapon_slider/weapons/turret_5", top: -58, left: 0, ctaAssetID: "TAPaytableEnemiesWeaponsTurret"};
						}
					case 4:	return {src: "paytable/pages/enemies/weapon_slider/weapons/gun_off", top: -28, left: 12, ctaAssetID: "TAPaytableEnemiesWeaponsPlasmaRifle"};
					case 3:	return {src: "paytable/pages/enemies/weapon_slider/weapons/laser", top: -15, left: -13, ctaAssetID: "TAPaytableEnemiesWeaponsLaser"};
					case 9:	return {src: "paytable/pages/enemies/weapon_slider/weapons/flamethrower", top: -40, left: 8, ctaAssetID: "TAPaytableEnemiesWeaponsFlamethrower"};
					case 10:return {src: "paytable/pages/enemies/weapon_slider/weapons/cryogun", top: -45, left: 6, ctaAssetID: "TAPaytableEnemiesWeaponsCryogun"};
					case 7:	return {src: "paytable/pages/enemies/weapon_slider/weapons/artillery_grenade", top: 1, left: 18, ctaAssetID: "TAPaytableEnemiesWeaponsArtilleryStrike"};

					default: 
						throw new Error (`Wrong weapon id ${aWeaponId_num} or betLevel ${aBetLevel}`);
				}
			},
			handleWeaponPrewButtonClick()
			{
				this.currentWeaponsQueueIndex--;
				if(this.currentWeaponsQueueIndex < 0)
				{
					this.currentWeaponsQueueIndex = WEAPONS_QUEUE.length - 1;
				}

				this.currentWeaponID = WEAPONS_QUEUE[this.currentWeaponsQueueIndex].weaponID;

				this._updateIndicator();
			},

			handleWeapnNextButtonClick()
			{
				this.currentWeaponsQueueIndex++;

				if(this.currentWeaponsQueueIndex > WEAPONS_QUEUE.length - 1)
				{
					this.currentWeaponsQueueIndex = 0;
				}

				this.currentWeaponID = WEAPONS_QUEUE[this.currentWeaponsQueueIndex].weaponID;

				this._updateIndicator();
			},

			handleBetPrewButtonClick()
			{
				this.currentBetLevelQueueIndex--;
				if(this.currentBetLevelQueueIndex < 0)
				{
					this.currentBetLevelQueueIndex = this.possibleBetLevels.length - 1;
				}

				this.currentBetLevel = this.possibleBetLevels[this.currentBetLevelQueueIndex];

				this._updateIndicator();
			},

			handleBetNextButtonClick()
			{
				this.currentBetLevelQueueIndex++;
				if(this.currentBetLevelQueueIndex > this.possibleBetLevels.length - 1)
				{
					this.currentBetLevelQueueIndex = 0;
				}

				this.currentBetLevel = this.possibleBetLevels[this.currentBetLevelQueueIndex];

				this._updateIndicator();
			}
		},

		mounted() {
			this.init();
		},

		computed: {
			baseImageClass: function() {
				return Vue.extend(BaseImage);
			},
			baseTextFieldClass: function() {
				return Vue.extend(BaseTextField);
			},
		},

		created() {
		},
	}
</script>

<style scoped>
	#back {
		position: absolute;
		width: 100%;
		height: 100%;
	}

	#weapons {
		position: absolute;
		width: 100%;
		height: 100%;
	}

	#weaponsNames {
		position: absolute;
		width: 100%;
		height: 100%;
		margin-left: 33px;
		margin-top: 76px;
		text-align: center;
		font-family: "fnt_nm_barlow_bold";
		font-size: 11px;
	}

	#currentBet {
		position: absolute;
		font-size: 20px;
		margin-left: 32px;
		margin-top: 18px;
		transform: translateX(-50%);
		color: #818180;
	}

	.weapon_slider_btn {
		position: absolute;
		margin-top: 20px;
		width: 30px;
		z-index: 1;
	}

	.bet_slider_btn {
		position: absolute;
		margin-top: 20px;
		width: 30px;
		z-index: 1;
	}

	#bet_slider {
		position: absolute;
		margin-top: 85px;
	}

	#betPrewBtnContainer {
		margin-left: -18px;
	}

	#betNextBtnContainer {
		margin-left: 57px;
	}

	#betLevelCapture {
		position: absolute;
		margin-left: 32px;
		top: 51px;
		transform: translateX(-50%);
	}

	#weaponPrewBtnContainer {
		margin-left: -12px;
	}

	#weaponNextBtnContainer {
		margin-left: 66px;
	}
</style>