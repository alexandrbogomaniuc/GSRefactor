import VueApplicationController from '../../../../../../../branches/wrathofra_vue_integrated/wrathofra/lobby/src/vue/VueApplicationController';

<template>
	<div class="page_container" id="contentPageContainer" ref="pageContainer">
		<div class='paytable_content_wrapper' id="paytable_content_wrapper">
			<span v-html='contentHtml' ref='mainContainer' id='mainContainer'>
			</span>

			<scroll-bar-component ref='scrollBar'
							:referenceElementId="'PAYTABLE'"
							:referenceHeightCutOffset=30
							:referenceParentElementId="'contentPageContainer'"
							:referenceRightBorderElementId="'paytable_content_wrapper'"
							:marginRight="17">
			</scroll-bar-component>

			
		</div>
	</div>
</template>

<script type="text/javascript">
	import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
	import BaseImage from './../base/BaseImage.vue';
	import Vue from 'vue';
	import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
	import VueApplicationController from '../../VueApplicationController';
	import ScrollBarComponent from "../scroll_bar/ScrollBarComponent.vue";
	import NumberValueFormat from "../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat";

	const TEXT_FONT_SIZE = {
		title: 36,
		description: 24	
	}

	const WIDTH_CHANGE_STATE = {
		INVALID: -1,
		DECREASED: 1,
		NOT_CHANGED: 2,
		INCREASED: 3
	}

	const MAX_ALLOWABLE_FONT_SIZE = 50;

	export default {

		name: "paytable-content",

		props: {
			maxPossibleWin: {
				type: Number,
				default: 0
			},

			maxTotalWin: {
				type: Number,
				default: 0
			},

			maxMultiplier: {
				type: Number,
				default: 0
			},

			rakePercent: {
				type: Number,
				default: 0
			},

			maxPlayers: {
				type: Number,
				default: 100
			}
		},

		data() {
			return {
				clientWidth: 960,
				clientHeight: 540,
				rowTopHeight: 60,
				isPaytableWidthDeterm: null,
				isTouchStart: null,
				isTouchStartY: null,
				parentFrame: null,
				rowTop: null,
				imgsOffset: [],
				watcherElementResize: 0,
				delayResizeTimer: 1,
				translationBasedTextSlots: []
			}
		},

		watch: {
			watcherElementResize: function () {
					this.onResize();
				}
		},

		components: {
			'scroll-bar-component' : ScrollBarComponent
		},

		methods: {
			init() {
				APP.vueApplicationController.once(VueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER, this._onFirstTimeToShowVueLayer, this);
				APP.vueApplicationController.on(VueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER, this._onValidetePaytable, this);
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

					APP.vueApplicationController.on(VueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER, this._onSubsequentTimeToShowVueLayer, this);

					this.$refs.scrollBar.update();

					if (APP.isMobile)
					{
						let cantouch = ("ontouchstart" in document.documentElement)||("ontouchstart" in window);
						let canpointer = ("onpointerdown" in document.documentElement)||("onpointerdown" in window);
						
						let element = document.getElementById('paytable_frame');

						if (cantouch)
						{
							element.addEventListener("touchstart", this.onTouchStart);
  							element.addEventListener("touchend", this.onTouchDown);
  							element.addEventListener("touchcancel", this.onTouchDown);
  							element.addEventListener("touchmove", this.onTouchMove);
						}

						if (canpointer)
						{
							element.addEventListener('pointerup', this.onPonterUp);
							element.addEventListener('pointerdown', this.onPonterDown);
							element.addEventListener('pointermove', this.onPonterMove);
						}

						window.addEventListener("orientationchange", this.onOrientationChange);
						
					}
				})
			},

			onOrientationChange()
			{
				this.delayedResizeCall();
			},

			_onSubsequentTimeToShowVueLayer(event)
			{
				this.$nextTick(function() 
				{
					//optimization...
					this.$refs.mainContainer.display = 'none';
					//...optimization

					//optimization...
					this.$refs.mainContainer.display = 'block';
					//...optimization
				})
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

				//insert translated texts...
				let lSpanTranslatableTextsList_arr = this.$refs.mainContainer.querySelectorAll('span[type=txt_based][translatable=true]');
				for (let lSpanTranslatableText_htmlel of lSpanTranslatableTextsList_arr)
				{
					let lTranslatableAssetId_str = lSpanTranslatableText_htmlel.id;

					let lAsset_tad = I18.getTranslatableAssetDescriptor(lTranslatableAssetId_str);
					let textDescriptor = lAsset_tad.textDescriptor;
					let fontDescriptor = textDescriptor.fontDescriptor;
					let lFontSizeMultiplier_num = +lSpanTranslatableText_htmlel.getAttribute("font-size-multiplier") || 1;
					let lFontSize_num = Math.floor(fontDescriptor.fontSize*lFontSizeMultiplier_num);
					
					let lStyle_obj = {
						fontSize: lFontSize_num + 'px',
						fontFamily: fontDescriptor.fontName,
						color: fontDescriptor.color.toCSSString(),
						letterSpacing: textDescriptor.letterSpacing + 'px',
						whiteSpace: 'nowrap'
					};

					Object.assign(lSpanTranslatableText_htmlel.style, lStyle_obj);

					let lReg = /\n/g;
					let lTxt_str = textDescriptor.text.replace(lReg, "<br/>")
					lSpanTranslatableText_htmlel.innerHTML = lTxt_str;

					this.translationBasedTextSlots.push(lSpanTranslatableText_htmlel);
				}
				//...insert translated texts

				this._onValidetePaytable();
			},

			_onValidetePaytable()
			{
				this._onValidetePossibleWin();
				this._onValideteRakePercent();
				this._onValidateMaxPlayers();
				this.onResize();
			},
			
			_onValidetePossibleWin()
			{
				let lMaxPossibleWin_htmle = this.$refs.mainContainer.querySelectorAll('span [id=max_possible_win]')[0];
				let lWinFormated_str;
				
				// [OWL] TODO: apply changes for alll systems without any conditions
				if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
				{
					lWinFormated_str = APP.currencyInfo.i_formatNumber(this.maxPossibleWin, true, undefined, undefined, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
				}
				else
				{
					lWinFormated_str = APP.currencyInfo.i_formatNumber(this.maxPossibleWin);
				}

				lMaxPossibleWin_htmle && (lMaxPossibleWin_htmle.innerHTML = lWinFormated_str);

				let lMaxTotalWin_htmle = this.$refs.mainContainer.querySelectorAll('span [id=max_total_win]')[0];
				let lMaxWinFormated_str;
				
				// [OWL] TODO: apply changes for alll systems without any conditions
				if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
				{
					lMaxWinFormated_str = APP.currencyInfo.i_formatNumber(this.maxTotalWin, true, undefined, undefined, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
				}
				else
				{
					lMaxWinFormated_str = APP.currencyInfo.i_formatNumber(this.maxTotalWin);
				}
				lMaxTotalWin_htmle && (lMaxTotalWin_htmle.innerHTML = lMaxWinFormated_str);

				//TODO: remove after all localizations will be updated...
				let lMaxTotalWin1_htmle = this.$refs.mainContainer.querySelectorAll('span [id=max_total_win_1]')[0];
				lMaxTotalWin1_htmle && (lMaxTotalWin1_htmle.innerHTML = lMaxWinFormated_str);

				let lMaxTotalWin2_htmle = this.$refs.mainContainer.querySelectorAll('span [id=max_total_win_2]')[0];
				lMaxTotalWin2_htmle && (lMaxTotalWin2_htmle.innerHTML = lMaxWinFormated_str);
				//...remove after all localizations will be updated

				let lMaxMultiplier_htmle = this.$refs.mainContainer.querySelectorAll('span [id=max_multiplier]')[0];
				lMaxMultiplier_htmle && (lMaxMultiplier_htmle.innerHTML = NumberValueFormat.format(this.maxMultiplier, 2));
			},

			_onValideteRakePercent()
			{
				let lRakePercent_htmle = this.$refs.mainContainer.querySelectorAll('span [id=rake_percent]')[0];
				lRakePercent_htmle && (lRakePercent_htmle.innerHTML = this.rakePercent);

				let lPlayerPercent_htmle = this.$refs.mainContainer.querySelectorAll('span [id=player_percent]')[0];
				lPlayerPercent_htmle && (lPlayerPercent_htmle.innerHTML = 100 - this.rakePercent);
			},

			_onValidateMaxPlayers()
			{
				let lMaxPlayers_htmle = this.$refs.mainContainer.querySelectorAll('span [id=max_players]')[0];
				lMaxPlayers_htmle && (lMaxPlayers_htmle.innerHTML = this.maxPlayers);
			},

			generateImage(assetName)
			{
				let baseImageInstance = new this.baseImageClass({
					propsData: { assetName:  assetName}
				});
				baseImageInstance.$mount();
				return baseImageInstance.$el;
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
			},

			onResize()
			{
				this.parentFrame = this.$parent.$refs.paytableDiv;
				this.rowTop = this.$parent.$refs.rowTop;

				if (!this.parentFrame || !this.rowTop)
				{
					this.delayedResizeCall(true);
					return;
				}

				let clientHeight 	= APP.layout.paytableHeadLayer.clientHeight;
				let clientWidth 	= document.documentElement.clientWidth;
				let lRowTop =		 this.rowTop.clientHeight;

				
				let widthChangeState = WIDTH_CHANGE_STATE.INVALID;

				if (!this.isPaytableWidthDeterm)
				{
					widthChangeState = WIDTH_CHANGE_STATE.INVALID;
				} 
				else if (this.clientWidth > clientWidth)
				{
					widthChangeState = WIDTH_CHANGE_STATE.DECREASED;
				} 
				else if (this.clientWidth == clientWidth)
				{
					if (!this.isPaytableWidthDeterm)
					{
						widthChangeState = WIDTH_CHANGE_STATE.INVALID;
					}
					else
					{
						widthChangeState = WIDTH_CHANGE_STATE.NOT_CHANGED;
					}
				}
				else 
				{
					widthChangeState = WIDTH_CHANGE_STATE.INCREASED;
				}

				if (this.parentFrame.clientWidth == 0)
				{
					this.isPaytableWidthDeterm = false;
				}
				else
				{
					this.isPaytableWidthDeterm = true;
				}

				this.clientWidth = clientWidth;
				this.clientHeight = clientHeight;
				this.rowTopHeight = lRowTop;
				
				this.validatePaytableContentHeight(widthChangeState);

				for (let i=0; i<this.translationBasedTextSlots.length; i++)
				{
					let lta = this.translationBasedTextSlots[i];
					let lCurTextContainerWidth_num = lta.offsetWidth;
					let lCurTextMaxWidth_num = lCurTextContainerWidth_num-10;
					let lRememberedPositionType = lta.style.position;

					Object.assign(lta.style, {position: 'absolute'});
					
					let lCurTextWidth_num = lta.offsetWidth;

					let lTxtScale_num = 1;
					if (lCurTextWidth_num > lCurTextMaxWidth_num)
					{
						lTxtScale_num = lCurTextMaxWidth_num/lCurTextWidth_num;
					}

					let lTranslateX_num = -Math.max(0, (1-lCurTextContainerWidth_num/lCurTextWidth_num)) * 50;

					Object.assign(lta.style, {
												position: lRememberedPositionType,
												transform: "translate("+lTranslateX_num+"%, -50%) scale("+lTxtScale_num+", "+lTxtScale_num+")",
												webkitTransform: "translate("+lTranslateX_num+"%, -50%) scale("+lTxtScale_num+", "+lTxtScale_num+")",
												msTransform: "translate("+lTranslateX_num+"%, -50%) scale("+lTxtScale_num+", "+lTxtScale_num+")"
											});
				}
			},

			validatePaytableContentHeight(aWidthChangeState)
			{
				let paytable = document.getElementById("PAYTABLE");
				if (!paytable)
				{
					return;
				}

				setTimeout(()=>{
					let paytable_frame_element = document.getElementById("paytable_frame"); 
					if(paytable_frame_element){
						let paytable_frame_height = paytable_frame_element.clientHeight;
						paytable.style.maxHeight = (paytable_frame_height - this.rowTopHeight - 2)+"px";
					}else{
						paytable.style.maxHeight = (this.clientHeight - this.rowTopHeight - 2)+"px";
					}
					this.validateScrollBarAndText(aWidthChangeState);
				},100);
			},

			validateScrollBarAndText(aWidthChangeState = null)
			{
				let paytable = document.getElementById("PAYTABLE");

				if (!paytable)
				{
					return;
				}

				let styleSheet = I18.getMobilePaytableStylesheet(APP);

				if (styleSheet)
				{
					this.setFontSize(styleSheet, '.description', 0);
					this.setFontSize(styleSheet, '.title', 0);
				}				

				this.validateTextFontSize(paytable, 'text_description', aWidthChangeState, TEXT_FONT_SIZE.description);
				this.validateTextFontSize(paytable, 'text_title', aWidthChangeState, TEXT_FONT_SIZE.title);

				this.$refs.scrollBar.update();
			},

			setFontSize(aStyleSheet_str, aBlock_str, aStartPosition_num)
			{
				let lStyleBlockStartPos_num = aStyleSheet_str.indexOf(aBlock_str, aStartPosition_num);
				if (lStyleBlockStartPos_num === -1)
				{
					return;
				}
				
				let lBlockNameLen_num = aBlock_str.length;

				if (aStyleSheet_str[lStyleBlockStartPos_num + lBlockNameLen_num + 1] === ' '
					|| aStyleSheet_str[lStyleBlockStartPos_num + lBlockNameLen_num + 1] === '{'
					|| aStyleSheet_str[lStyleBlockStartPos_num + lBlockNameLen_num + 1] === '\n'
					|| aStyleSheet_str[lStyleBlockStartPos_num + lBlockNameLen_num + 1] === ''
					|| aStyleSheet_str[lStyleBlockStartPos_num + lBlockNameLen_num] === ' '
					|| aStyleSheet_str[lStyleBlockStartPos_num + lBlockNameLen_num] === '{'
					|| aStyleSheet_str[lStyleBlockStartPos_num + lBlockNameLen_num] === '\n'
					|| aStyleSheet_str[lStyleBlockStartPos_num + lBlockNameLen_num] === '')
				{
					let lStyleBlockEndPos_num = aStyleSheet_str.indexOf('}', lStyleBlockStartPos_num);
					let lStyleBlock_str = aStyleSheet_str.slice(lStyleBlockStartPos_num, lStyleBlockEndPos_num);

					let lStyleBlock_arr = lStyleBlock_str.split('\n');

					for (let i = 0; i < lStyleBlock_arr.length; i++)
					{
						lStyleBlock_arr[i] = lStyleBlock_arr[i].replace('\t', '').replace('\r', '');

						if (lStyleBlock_arr[i].indexOf('font-size:') !== -1)
						{
							let l_str = lStyleBlock_arr[i].slice(lStyleBlock_arr[i].indexOf('font-size:') + 10, lStyleBlock_arr[i].indexOf(';'));
				 			l_str = l_str.trim();
				 			let lFontSize_num = +l_str.replace('px', '');
							
				 			if (TEXT_FONT_SIZE[aBlock_str.slice(1, )] !== lFontSize_num && lFontSize_num <= MAX_ALLOWABLE_FONT_SIZE)
				 			{
				 				TEXT_FONT_SIZE[aBlock_str.slice(1, )] = lFontSize_num;
				 			}
						}
					}
				}
				else
				{
					this.setFontSize(aStyleSheet_str, aBlock_str, lStyleBlockStartPos_num + lBlockNameLen_num);
				}
			},

			validateTextFontSize(aPaytableContainer, aTextDecriptor, aWidthChangeState, aTextFontSize)
			{
				let lScreenModeRatio_num = APP.layout.isPortraitOrientation ? 0.7 : 1;

				let spanDescription = aPaytableContainer.querySelectorAll('span [type='+aTextDecriptor+']');

				for(var i = 0; i < spanDescription.length; i++) 
					{
						let ratio = (spanDescription[i].offsetWidth) / (spanDescription[i].scrollWidth);

						if (
							(
								aWidthChangeState ==  WIDTH_CHANGE_STATE.NOT_CHANGED
								|| aWidthChangeState ==  WIDTH_CHANGE_STATE.DECREASED
								|| aWidthChangeState ==  WIDTH_CHANGE_STATE.INVALID
							)
							&& ratio < 1
						)
						{
							let needSize = parseInt(aTextFontSize * ratio * ratio * lScreenModeRatio_num);
							spanDescription[i].style.fontSize = needSize+"px";
						
							this.delayedResizeCall();
						}
						else if (
									(
										(
											aWidthChangeState ==  WIDTH_CHANGE_STATE.INCREASED
											|| aWidthChangeState ==  WIDTH_CHANGE_STATE.INVALID
										)
										&& ratio >= 1
									)
								)
						{
							spanDescription[i].style.fontSize = Math.floor(aTextFontSize*lScreenModeRatio_num)+"px";
							this.activateWatcher();
						}
						else if (lScreenModeRatio_num !== 1)
						{
							spanDescription[i].style.fontSize = Math.floor(aTextFontSize*lScreenModeRatio_num)+"px";
						}
					}				
			},

			delayedResizeCall(aRepeatResize = false)
			{
				this.delayResizeTimer = aRepeatResize ? this.delayResizeTimer + 1: 1;

				if (this.delayResizeTimer < 100)
				{
					this.timer = setTimeout(this.onResize, this.delayResizeTimer);
				}
				else
				{
					console.error("An image scaling error occurred.");
				}
			},

			activateWatcher(e)
			{
				this.watcherElementResize++;
				this.watcherElementResize = this.watcherElementResize % 100;
			},

			onTouchDown(e)
			{
				this.isTouchStart = false;
			},

			onTouchMove(e)
			{
				if (!this.isTouchStart)
				{
					return;
				}

				this.validatePaytableScrollPosition(e.changedTouches[0].pageY);
			},

			onTouchStart(e)
			{
				this.isTouchStart = true;
				this.isTouchStartY = e.changedTouches[0].pageY;
			},

			onPonterUp(e)
			{
				this.isTouchStart = false;
			},

			onPonterMove(e)
			{
				if (!this.isTouchStart)
				{
					return;
				}

				this.validatePaytableScrollPosition(e.clientY);
			},

			onPonterDown(e)
			{
				this.isTouchStart = true;
				this.isTouchStartY = e.clientY;
			},

			validatePaytableScrollPosition(y)
			{
				if (this.isTouchStartY != y)
				{
					let det = this.isTouchStartY - y;
					this.isTouchStartY = y;

					let element = document.getElementById('PAYTABLE');
					let lScrollTop = element.scrollTop;
					let newY = lScrollTop + det;

					if (newY < 0)
					{
						element.scrollTop = 0;
					}
					else if (newY >= 0 && newY < (element.scrollHeight - element.clientHeight))
					{
						element.scrollTop = newY;
					}
					else
					{
						element.scrollTop = element.scrollHeight - element.clientHeight;
					}
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
		},

		updated: function () {
		},

		mounted() {
			this.init();
		},

		created() {
			window.addEventListener('resize', this.onResize);
			this.onResize();
		},
	}
</script>

<style scoped >
	#mainContainer {
		width: 100%;
		margin: 0 auto;
		height: 100%;
	}

	.paytable_content_wrapper
	{
		width: 100%;
		white-space: nowrap;
	}
</style>