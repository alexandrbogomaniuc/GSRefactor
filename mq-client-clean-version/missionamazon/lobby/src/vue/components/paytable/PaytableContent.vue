import VueApplicationController from '../../../../../../../branches/wrathofra_vue_integrated/wrathofra/lobby/src/vue/VueApplicationController';

<template>
	<span v-html='contentHtml' ref='mainContainer' id='mainContainer'>
	</span>
</template>

<script type="text/javascript">

	import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
	import BaseImage from './../base/BaseImage.vue';
	import Vue from 'vue';
	import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
	import VueApplicationController from '../../VueApplicationController';
	import {ENEMY_TYPES, INDICATORS_CONSTANT_VALUES } from '../../../../../shared/src/CommonConstants';
	import WeaponSlider from './WeaponSlider.vue';

	const PAYOUT_MULTIPLAER = 1; //server constant, actual for all rooms, any payout is calculated as: (enemy HP for shot) * PAYOUT_MULTIPLAER

	const BET_LEVEL =
	[
		1,
		2,
		3,
		5,
		10
	]

	const PAGES = {
		1: "how_to_play",
		2: "enemies",
		3: "enemies2",
		4: "legendary_enemies",
		5: "rare_gems",
		6: "weapons",
		7: "special_weapons",
		8: "kills", 
		9: "controls_1",
		10: "controls_2"
	}

	export default {

		name: "paytable-content",

		props: {
			enemyPayouts: {
				type: Array,
				default: function() {
					return null;
				}
			},
			weaponCostMultiplier: {
				type: Array,
				default: function() {
					return ['0', '0', '0', '0', '0'];
				}
			},
			maxShotsOnScreen: {
				type: Number,
				default: 0
			},
			gemPayouts: {
				type: Array,
				default: function() {
					return ['0', '0', '0', '0', '0'];
				}
			},
		},

		data() {
			return {
				_fEnemyWeaponSliderInstance: null
			}
		},

		methods: {
			init() {
				APP.vueApplicationController.once(VueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER, this._onFirstTimeToShowVueLayer, this);
				APP.vueApplicationController.on(VueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER, this._onValidetePayouts, this);
			},

			_onFirstTimeToShowVueLayer(event) 
			{
				this.$nextTick(function() 
				{
					//optimization...
					this.$refs.mainContainer.display = 'none';
					//...optimization
					this.instantiateSlots();
					//optimization...
					this.$refs.mainContainer.display = 'block';
					//...optimization

					this._initEnemyWeaponSlider();

					APP.vueApplicationController.on(VueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER, this._onSubsequentTimeToShowVueLayer, this);
				})
			},

			_onSubsequentTimeToShowVueLayer(event)
			{
				this.$nextTick(function() 
				{
					//optimization...
					this.$refs.mainContainer.display = 'none';
					//...optimization
					this._validateGameModeContent();
					//optimization...
					this.$refs.mainContainer.display = 'block';
					//...optimization
				})
			},

			_onValidetePayouts()
			{
				this._updateStakeEnemyIndicators();
				this._updateWeaponIndicators();
			},

			_initEnemyWeaponSlider()
			{
				this._fEnemyWeaponSliderInstance = new this.weaponSliderClass();
				this._fEnemyWeaponSliderInstance.$mount();
				let lSpan_htmlel = this.$refs.mainContainer.querySelector('div[id=enemy_weapon_slider]');

				this._fEnemyWeaponSliderInstance.$on("weaponIndicatorUpdated", this._weaponEnemyIndicatorUpdatedHandler);
				lSpan_htmlel.appendChild(this._fEnemyWeaponSliderInstance.$el);

				this._updateWeaponIndicators();
			},


			_updateWeaponSliderPositionIfRequired(aPageName_str)
			{
				let lBossPositionDescriptor_html = document.getElementById("enemy_weapon_slider_optional_boss_page_position_descriptor");
				let lRegularPositionDescriptor_html = document.getElementById("enemy_weapon_slider_optional_regular_page_position_descriptor");

				let lSliderElement_html = document.getElementById("enemy_weapon_slider");

				if(
					lSliderElement_html &&
					lBossPositionDescriptor_html &&
					lRegularPositionDescriptor_html
					)
				{

					let lTargetPositionDescriptor_html = lRegularPositionDescriptor_html;

					lSliderElement_html.style["left"] = lTargetPositionDescriptor_html.style["left"];
					lSliderElement_html.style["top"] = lTargetPositionDescriptor_html.style["top"];
				}
			},

			_weaponEnemyIndicatorUpdatedHandler(aConfig_obj)
			{
				this._weaponIndicatorUpdated(aConfig_obj);
			},

			_weaponIndicatorUpdated(aConfig_obj)
			{
				this._updateWeaponIndicators(aConfig_obj.weaponId, aConfig_obj.betLevel);

				let lSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll('span[class=enemy_payout]');
				let lSpanRare_htmlel_arr = this.$refs.mainContainer.querySelectorAll('span[class=enemy_rare_payout]');

				for (let i = 0; i < lSpan_htmlel_arr.length; i++)
				{
					let lEnemyId_int = lSpan_htmlel_arr[i].id;
					this._insertPayout(lSpan_htmlel_arr[i], lEnemyId_int, aConfig_obj.weaponId, this._fCurrentStake_int, aConfig_obj.betLevel);
				}

				for (let i = 0; i < lSpanRare_htmlel_arr.length; i++)
				{
					let lEnemyId_int = lSpanRare_htmlel_arr[i].id;
					this._insertPayout(lSpanRare_htmlel_arr[i], lEnemyId_int, aConfig_obj.weaponId, this._fCurrentStake_int, aConfig_obj.betLevel);
				}
			},

			_updateWeaponIndicators(aCurrentWeaponId_int = this._getCurrentWeaponId(), aCurrentBetLevel_int = this.getCurrentBetLevel())
			{
				let lCurrentStake_int = this._getCurrentStake();

				this._fEnemyWeaponSliderInstance && this._fEnemyWeaponSliderInstance.updateIndicator(aCurrentWeaponId_int, lCurrentStake_int, aCurrentBetLevel_int);	
			},

			_updateStakeEnemyIndicators()
			{
				let lStakeText_str = APP.currencyInfo.i_formatNumber(this._getCurrentStake(), true);

				let lWidth_int = lStakeText_str.length;
				let lMaxWidth_int = 7;
				let htmlElement = this.$refs.mainContainer.querySelector('span[id=stake_indicator_text]');

				let htmlDotElement = this.$refs.mainContainer.querySelector('font[id=dot]');

				if(htmlElement)
				{
					let text = "[" + "<font style='color:#ffc70f'>" + lStakeText_str + "</font>" + "]";

					if(lWidth_int > lMaxWidth_int)
					{
						text = "<br>" + text;

						if(!htmlDotElement)
						{
							text += "<br>"
						}
					}

					htmlElement.innerHTML = text;
					
				}

				if(htmlDotElement)
				{
					htmlDotElement.innerHTML = ".";
				}
			},

			_getCurrentStake()
			{
				let lCurrentStake_int = undefined;

				if (APP.lobbyStateController.info.lobbyScreenVisible)
				{
					lCurrentStake_int = this._getMinStake();
				}
				else
				{
					let lPlayerInfo = APP.playerController.info;
					lCurrentStake_int = lPlayerInfo.currentStake || lPlayerInfo.enterRoomStake;

					//BATTLEGROUND LOADING GAME CASE...
					let lBattlegroundInfo_bi = APP.battlegroundController.info;

					if(
						lCurrentStake_int === undefined &&
						lBattlegroundInfo_bi.isBattlegrGroundGame()
						)
					{
						lCurrentStake_int = lBattlegroundInfo_bi._fConfirmedBuyInCost_num;
					}
					//...BATTLEGROUND LOADING GAME CASE
				}

				this._fCurrentStake_int = lCurrentStake_int;

				return lCurrentStake_int;
			},

			_getMinStake()
			{
				let lCurrentStakes_int_arr = APP.playerController.info.stakes;
				return lCurrentStakes_int_arr && lCurrentStakes_int_arr[0];
			},

			_getCurrentWeaponId()
			{
				let lCurrentWeaponId_int = undefined;

				if (APP.lobbyStateController.info.lobbyScreenVisible)
				{
					lCurrentWeaponId_int = -1;
				}
				else
				{
					lCurrentWeaponId_int = APP.playerController.info.weaponId;
				}

				if (lCurrentWeaponId_int == undefined)
				{
					lCurrentWeaponId_int = -1;
				}

				return lCurrentWeaponId_int;
			},

			getCurrentBetLevel()
			{
				let lCurrentBetLevel_int = undefined;

				if (APP.lobbyStateController.info.lobbyScreenVisible)
				{
					lCurrentBetLevel_int = 1;
				}
				else
				{
					lCurrentBetLevel_int = APP.playerController.info.betLevel;
				}

				if (lCurrentBetLevel_int == undefined)
				{
					lCurrentBetLevel_int = 1;
				}

				return lCurrentBetLevel_int;
			},

			_insertPayout(aTargetSpan_htmlel, aEnemyId_int, aWeaponId_int, aCurrentStake_int, aBetLevel_int)
			{

				let lResultString_str = "";
				let lPayout_obj = null;
				let lEnemyPayouts_arr_obj = this.enemyPayouts;
				
				for(let enemy of lEnemyPayouts_arr_obj)
				{
					if(enemy.idEnemy === parseInt(aEnemyId_int))
					{
						lPayout_obj = enemy.prize;
					}
				}
				
				if(lPayout_obj.minPayout !== lPayout_obj.maxPayout)
				{
					try
					{
						let lMin_num = Math.floor(aCurrentStake_int * lPayout_obj.minPayout * aBetLevel_int * PAYOUT_MULTIPLAER);
						let lMax_num = Math.floor(aCurrentStake_int * lPayout_obj.maxPayout * aBetLevel_int * PAYOUT_MULTIPLAER)
						lResultString_str = APP.currencyInfo.i_formatInterval(lMin_num, lMax_num, true);
					}
					catch(e)
					{
						APP.logger.i_pushWarning(`Paytable Content. Can not find payout for paytable enemyID: ${aEnemyId_int}; WeaponId: ${aWeaponId_int}.`);
						console.log("Can not find payout for paytable enemyID: " + aEnemyId_int + " WeaponId: " + aWeaponId_int);
						console.warn(e);
						lResultString_str = "0";
					}
				}
				else
				{
					let lMin_num = Math.floor(aCurrentStake_int * lPayout_obj.minPayout * aBetLevel_int * PAYOUT_MULTIPLAER);
					lResultString_str = APP.currencyInfo.i_formatNumber(lMin_num, true);
				}


				let lPayoutMaxTextWidth_num = aEnemyId_int == ENEMY_TYPES.BOSS ? 180 : 120;
				Object.assign(aTargetSpan_htmlel.style, {
									"margin-left": "0px",
									"margin-right": "0px"
								});

				let lOriginWidth_num = this.getTextWidth(lResultString_str, "12px fnt_nm_barlow_semibold");

				if(lOriginWidth_num > lPayoutMaxTextWidth_num)
				{
					let lScale_num = (lPayoutMaxTextWidth_num * 100) / lOriginWidth_num / 100;
					let lMargin_num = aEnemyId_int == ENEMY_TYPES.BOSS ? 0 : lOriginWidth_num * lScale_num;

					Object.assign(aTargetSpan_htmlel.style, {
								"transform": "scaleX(" + lScale_num + ")",
								"margin-left": -lMargin_num + "px",
								"margin-right": -lMargin_num + "px"
							});
				}
				else
				{
					Object.assign(aTargetSpan_htmlel.style, {
								"transform": "scaleX(1)",
								"margin-left": "0px",
								"margin-right": "0px"
							});
				}
				
				aTargetSpan_htmlel.innerText = lResultString_str;

				this._updateGemPayoutIndicator();
			},

			getTextWidth(text, font)
			{
				var canvas = this.getTextWidth.canvas || (this.getTextWidth.canvas = document.createElement("canvas"));
				var context = canvas.getContext("2d");
				context.font = font;
				var metrics = context.measureText(text);
				return metrics.width;
			},

			instantiateSlots() 
			{
				//insert independent images...
				let lSpanImagesList_arr = this.$refs.mainContainer.querySelectorAll('span[type=img][src]');
				for (let lSpanImage_htmlel of lSpanImagesList_arr)
				{
					let lImageAssetName_str = lSpanImage_htmlel.getAttribute('src');
					lSpanImage_htmlel.appendChild(this.generateImage(lImageAssetName_str));
				}
				//...insert independent images

				//insert translated images...
				let lSpanTranslatableImagesList_arr = this.$refs.mainContainer.querySelectorAll('span[type=img][translatable=true]');
				for (let lSpanTranslatableImage_htmlel of lSpanTranslatableImagesList_arr)
				{
					let lTranslatableAssetId_str = lSpanTranslatableImage_htmlel.id;
					lSpanTranslatableImage_htmlel.appendChild(this.generateImage(lTranslatableAssetId_str));
				}
				//...insert translated images

				//WEAPON PAID MULTIPLIER...
				if (this.weaponCostMultiplier && this.weaponCostMultiplier.length > 0)
				{
					for (let i=0; i < this.weaponCostMultiplier.length; i++)
					{
						let lSpanId_str = "#WEAPON_PAID_" + i;
						this.insertValue(lSpanId_str, this.weaponCostMultiplier[i]);
					}
				}

				if (this.weaponCostMultiplier && this.weaponCostMultiplier.length > 0)
				{
					for (let i=0; i < this.weaponCostMultiplier.length; i++)
					{
						let lSpanId_str = "#CONTROLS_WEAPON_PAID_" + i;
						this.insertValue(lSpanId_str, this.weaponCostMultiplier[i]);
					}
				}

				if (this.weaponCostMultiplier && this.weaponCostMultiplier.length > 0)
				{
					for (let i=0; i < this.weaponCostMultiplier.length; i++)
					{
						let lSpanId_str = "#CONTROLS_MOBILE_WEAPON_PAID_" + i;
						this.insertValue(lSpanId_str, this.weaponCostMultiplier[i]);
					}
				}
				//...WEAPON PAID MULTIPLIER

				this.insertValue("#MAX_SHOTS_COUNT", this.maxShotsOnScreen);
				
				this._replacingNumerals();

				this._validateContesntAndStyleSupport();
			},

			_replacingNumerals()
			{
				let lSubString_str = '#shots:';
				let lSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll('span [class=shots_text]');
				for (let i = 0; i < lSpan_htmlel_arr.length; i++) {
					let lReplaseTextHelper_str = "#REPLACE_SHOTS_TEXT_" + i;
					let lrezult_str = I18.prepareNumberPoweredMessage(lSpan_htmlel_arr[i].innerText, lSubString_str, this.maxShotsOnScreen);
					this.insertValue(lReplaseTextHelper_str, lrezult_str);
				}
			},

			_validateContesntAndStyleSupport()
			{
				let lSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll('span[id=gradient]');
				for (let i = 0; i < lSpan_htmlel_arr.length; i++)
				{
					this.applyGradientStyle(lSpan_htmlel_arr[i].style);
				}
				
				if(!this.supportsCSS('mix-blend-mode', 'screen'))
				{
					let lSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll('div[id=fx]');

					for (let i = 0; i < lSpan_htmlel_arr.length; i++)
					{
						Object.assign(lSpan_htmlel_arr[i].style, {
							"opacity": "0",
							});
					}
				}

				this._validateGameModeContent();
			},

			_validateGameModeContent()
			{
				let lIsKeepSWModeActive_bl = APP.isKeepSWModeActive;
				let lSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll('span[class=weapon_indicator]');

				for (let i = 0; i < lSpan_htmlel_arr.length; i++)
				{
					Object.assign(lSpan_htmlel_arr[i].style, {
						"opacity": lIsKeepSWModeActive_bl ? "1" : "0",
						});
				}

				let lIsSWCompensationAllowed_bl = !(APP.FRBController.info.isActivated
													|| APP.lobbyBonusController.info.isActivated 
													|| APP.tournamentModeController.info.isTournamentMode);

				let lHideSWInfoSelector_str = lIsSWCompensationAllowed_bl ? 'span[id=sw_info_lose]' : 'span[id=sw_info_cash_prize]';
				let lHideSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll(lHideSWInfoSelector_str);

				for (let i = 0; i < lHideSpan_htmlel_arr.length; i++)
				{
					Object.assign(lHideSpan_htmlel_arr[i].style, {
						"display": "none"
					});
				}

				let lShowSWInfoSelector_str = !lIsSWCompensationAllowed_bl ? 'span[id=sw_info_lose]' : 'span[id=sw_info_cash_prize]';
				let lShowSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll(lShowSWInfoSelector_str);

				for (let i = 0; i < lShowSpan_htmlel_arr.length; i++)
				{
					Object.assign(lShowSpan_htmlel_arr[i].style, {
						"display": "inline"
					});
				}

				lSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll('span[id=sw_info_hide_when_frb]');

				for (let i = 0; i < lSpan_htmlel_arr.length; i++)
				{
					Object.assign(lSpan_htmlel_arr[i].style, {
						"display": APP.FRBController.info.isActivated ? "none" : "block"
						});
				}

				lSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll('span[id=sw_info_show_when_frb]');

				for (let i = 0; i < lSpan_htmlel_arr.length; i++)
				{
					Object.assign(lSpan_htmlel_arr[i].style, {
						"display": lIsSWCompensationAllowed_bl ? "none" : "block"
						});
				}
				
				
				lShowSWInfoSelector_str = !APP.isMobile ? 'span[id=mobile_view]' : 'span[id=desktop_view]';
				lShowSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll(lShowSWInfoSelector_str);

				for (let i = 0; i < lShowSpan_htmlel_arr.length; i++)
				{
					Object.assign(lShowSpan_htmlel_arr[i].style, {
						"display": "inline"
					});
				}

				lHideSWInfoSelector_str = !APP.isMobile ? 'span[id=mobile_view]' : 'span[id=desktop_view]';
				lHideSpan_htmlel_arr = this.$refs.mainContainer.querySelectorAll(lHideSWInfoSelector_str);

				for (let i = 0; i < lHideSpan_htmlel_arr.length; i++)
				{
					Object.assign(lHideSpan_htmlel_arr[i].style, {
						"display": "none"
					});
				}
			},

			applyGradientStyle(targetStyle)
			{
				Object.assign(targetStyle, {
					"background": "transparent",
					"color": "#fccc32",
					"webkitBackgroundClip": "none",
					"backgroundClip": "none",
					"webkitTextFillColor": "#fccc32"
					});
			},

			insertValue(aSpanId_str, aValue_num)
			{
				let lSpan_htmlel = this.$refs.mainContainer.querySelector(aSpanId_str);
				lSpan_htmlel && (lSpan_htmlel.innerText = aValue_num);
				//check for text scaling
			},

			generateImage(assetName)
			{
				let baseImageInstance = new this.baseImageClass({
					propsData: { assetName:  assetName}
				});
				baseImageInstance.$mount();
				return baseImageInstance.$el;
			},

			updateContent(page)
			{
				//VALIDATE WEAPONS SLIDER VISIBILITY...
				var lSliderHtmlElement = document.getElementById("enemy_weapon_slider");

				if(lSliderHtmlElement)
				{
					var lDisplayMode_str = "none";

					switch(this._getPageId(page))
					{
						case "enemies":
						case "enemies2":
						case "legendary_enemies":
							lDisplayMode_str = "block";
							break;
					}

					lSliderHtmlElement.style["display"] = lDisplayMode_str;
					this._updateWeaponSliderPositionIfRequired(page);
				}
				//...VALIDATE WEAPONS SLIDER VISIBILITY

				let lPagesList_arr = this.$refs.mainContainer.querySelectorAll('div[class=page]');
				for (let lPage_htmlel of lPagesList_arr)
				{
					if(lPage_htmlel.getAttribute('id') == this._getPageId(page))
					{
						Object.assign(lPage_htmlel.style, {
							"display": "block"
						});
					}
					else
					{
						Object.assign(lPage_htmlel.style, {
							"display": "none"
						});
					}
				}

				if(
					this._getPageId(page) == "enemies" ||
					this._getPageId(page) == "enemies2" ||
					this._getPageId(page) == "legendary_enemies"
					)
				{
					this._fEnemyWeaponSliderInstance.updateTexts();

					let lConfig_obj = {
						weaponId: this._fEnemyWeaponSliderInstance.currentWeaponID,
						betLevel: this._fEnemyWeaponSliderInstance.currentBetLevel
					}

					this._weaponEnemyIndicatorUpdatedHandler(lConfig_obj);
				}

				if(this._getPageId(page) == "special_weapons")
				{

				}

				//SUBSTITUTE TEXT PLACEHOLDERS WITH VALUES...

				//SW TARGETS...
				let lTargetsCounts_int_arr = INDICATORS_CONSTANT_VALUES.SPECIAL_WEAPONS_MAX_TARGETS_COUNTS;
				for( let i = 0; i < lTargetsCounts_int_arr.length; i++ )
				{
					this._replaceAll(
						"##SPECIAL_WEAPONS_MAX_TARGETS_COUNTS_" + i + "##",
						lTargetsCounts_int_arr[i]
						);
				}
				//...SW TARGETS

				//SLOTS SPINS COUNT...
				this._replaceAll(
					"##SLOTS_SPINS_COUNT##",
					INDICATORS_CONSTANT_VALUES.SLOTS_SPINS_COUNT
					);
				//...SLOTS SPINS COUNT
				//...SUBSTITUTE TEXT PLACEHOLDERS WITH VALUES
				


			},

			_updateGemPayoutIndicator()
			{
 				//GEM PAID...
				if(this.gemPayouts && this.gemPayouts.length > 0)
				{
					for(var i = 0; i < this.gemPayouts.length; i++)
					{
						let lSpanId_str = "#GEM_PAID_" + i;
						let lCurrentStake_int = this._getCurrentStake();
						let lMin_num = Math.floor(lCurrentStake_int * this.gemPayouts[i][0] * BET_LEVEL[i]);
						let lMax_num = Math.floor(lCurrentStake_int * this.gemPayouts[i][1] * BET_LEVEL[i]);
						var lFormatPayouts_str = APP.currencyInfo.i_formatInterval(lMin_num, lMax_num, true);
						this.insertValue(lSpanId_str, lFormatPayouts_str);
						
						let lSpan_htmlel = this.$refs.mainContainer.querySelectorAll('span[class=gem_paid_' + i + ']');
						let lPayoutMaxTextWidth_num = 110;
						let lOriginWidth_num = this.getTextWidth(lFormatPayouts_str, "15px fnt_nm_barlow_bold");

						if(lOriginWidth_num > lPayoutMaxTextWidth_num)
						{
							let lScale_num = (lPayoutMaxTextWidth_num * 100) / lOriginWidth_num / 100;

							Object.assign(lSpan_htmlel[0].style, {
										"transform": "scaleX(" + lScale_num + ")",
									});
						}
					}
				}
				//..GEM PAID
			},

			updateWeaponPaidMultiplier(aWeaponPaidMultiplier_arr)
			{
				this.weaponCostMultiplier = aWeaponPaidMultiplier_arr;
				
				//WEAPON PAID MULTIPLIER...
				if (this.weaponCostMultiplier && this.weaponCostMultiplier.length > 0)
				{
					for (let i=0; i < this.weaponCostMultiplier.length; i++)
					{
						let lSpanId_str = "#WEAPON_PAID_" + i;
						this.insertValue(lSpanId_str, this.weaponCostMultiplier[i]);
					}
				}

				if (this.weaponCostMultiplier && this.weaponCostMultiplier.length > 0)
				{
					for (let i=0; i < this.weaponCostMultiplier.length; i++)
					{
						let lSpanId_str = "#CONTROLS_WEAPON_PAID_" + i;
						this.insertValue(lSpanId_str, this.weaponCostMultiplier[i]);
					}
				}

				if (this.weaponCostMultiplier && this.weaponCostMultiplier.length > 0)
				{
					for (let i=0; i < this.weaponCostMultiplier.length; i++)
					{
						let lSpanId_str = "#CONTROLS_MOBILE_WEAPON_PAID_" + i;
						this.insertValue(lSpanId_str, this.weaponCostMultiplier[i]);
					}
				}
				//...WEAPON PAID MULTIPLIER
			},

			_replaceAll(aPlaceHolder_str, aValue)
			{
				let lPaytable_html = document.getElementById("PAYTABLE");
				
				if(!lPaytable_html)
				{
					return;
				}

				let lElements_html_arr = lPaytable_html.getElementsByTagName("*");

				for(let i = lElements_html_arr.length -1; i >= 0; i--)
				{
					let lElement_html = lElements_html_arr[i];

					if(lElement_html.innerHTML.includes(aPlaceHolder_str))
					{
						lElement_html.innerHTML = lElement_html.innerHTML.replace(new RegExp(aPlaceHolder_str, 'g'), aValue + "");
					}
				}
			},

			_getPageId(aPageNumber)
			{
				if(PAGES[aPageNumber] == "controls_1" && APP.isMobile)
				{
					return "mobile_controls_1";
				}
				return PAGES[aPageNumber];
			},

			supportsCSS(property, value)
			{
				try
				{
					var element = document.createElement('span');
					if (element.style[property] !== undefined)
					{
						element.style[property] = value;
					}
					else
					{
						return false;
					}
					return element.style[property] === value;
				} 
				catch (e)
				{
					return false;
				}
			}
		},

		computed: {
			contentHtml: function () {
				return '<style>' + I18.getMobilePaytableStylesheet(APP) + '</style>' + I18.getMobilePaytableContent(APP);
			},
			baseImageClass: function() {
				return Vue.extend(BaseImage);
			},
			weaponSliderClass: function() {
				return Vue.extend(WeaponSlider);
			},
		},

		mounted() {
			this.init();
		}
	}
</script>

<style scoped >
	#mainContainer {
		width: 100%;
		margin: 0 auto;
		position: absolute;
	}
</style>