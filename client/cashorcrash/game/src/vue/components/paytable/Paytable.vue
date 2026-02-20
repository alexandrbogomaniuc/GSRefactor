<template>
	<div ref='paytableDiv' id="paytable_frame" class='paytable_frame' :style='{width: PAYTABLE_WIDTH + "%", height: PAYTABLE_HEIGHT + "%"}'>
		
		<div class='row top_row' ref='rowTop' id="rowTop">

			<span class='top_row_content' ref='topRowContent'>
				<div style="text-align: right; padding-right: 50px" ref='rowTopBtn'>
					<PrintableRulesButton 	id='printableRulesButton'
											@click='handlePrintableRulesButtonClick'
					/>
				</div>
				<div style="text-align: right;">
					<BaseButton
									@click='handleCloseButtonClick'
									id='closeButton'
									bgAssetName='paytable/exit_btn'
								/>
				</div>
			</span>

		</div>

		<div class='paytable_content_container'>
			<PaytableContent
				ref='paytableContent'
				:maxPossibleWin='maxPossibleWin'
				:maxTotalWin='maxTotalWin'
				:maxMultiplier='maxMultiplier'
				:rakePercent='rakePercent'
				:maxPlayers='maxPlayers'
			/>
		</div>
	</div>
</template>

<script type="text/javascript">

	import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
	import PaytableScreenController from '../../../controller/uis/custom/secondary/paytable/PaytableScreenController';
	import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
	import VueApplicationController from '../../VueApplicationController';

	const PAYTABLE_WIDTH = 90;
	const PAYTABLE_HEIGHT = 100;

	export default {
		name: 'paytable',

		data() {
			return {
				paytableData: null,
				currentPage: 1
			}
		},

		methods: {
			init() {
				if (APP.secondaryScreenController.paytableScreenController.paytableData)
				{
					this._onPaytableDataReady();
				}
				APP.secondaryScreenController.paytableScreenController.on(PaytableScreenController.EVENT_ON_PAYTABLE_DATA_READY, this._onPaytableDataReady, this);

				window.addEventListener('resize', this.onResize);

				APP.vueApplicationController.on(VueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER, this._onPaytableLayerShow, this);
			},

			_onPaytableDataReady(event)
			{
				this.initData();
			},

			initData() {
				this.paytableData = APP.secondaryScreenController.paytableScreenController.paytableData;
			},

			handleCloseButtonClick() {
				this.$emit('close');
			},

			handlePrintableRulesButtonClick() {
				this.$emit('printable-rules-click');
			},

			onResize()
			{
				let lTopRowEl = this.$refs.rowTop;
				let lTopRowContentEl = this.$refs.topRowContent;

				let lTopRowWidth_num = lTopRowEl.offsetWidth;
				let lTopRowMaxContentWidth_num = lTopRowWidth_num-50;
				let lTopRowCurContentWidth_num = lTopRowContentEl.offsetWidth;

				let lAuxXScale_num = 1;
				let lTranslateX_num = 0;
				if (lTopRowCurContentWidth_num > lTopRowMaxContentWidth_num)
				{
					lAuxXScale_num = lTopRowMaxContentWidth_num/lTopRowCurContentWidth_num;
					lTranslateX_num = (1-lAuxXScale_num)*100;
				}

				Object.assign(lTopRowContentEl.style, {
												transform: "translate("+lTranslateX_num+"%, 0%) scale("+lAuxXScale_num+", "+lAuxXScale_num+")",
												webkitTransform: "translate("+lTranslateX_num+"%, 0%) scale("+lAuxXScale_num+", "+lAuxXScale_num+")",
												msTransform: "translate("+lTranslateX_num+"%, 0%) scale("+lAuxXScale_num+", "+lAuxXScale_num+")"
											});
			},

			_onPaytableLayerShow()
			{
				this.onResize();
			}
		},

		mounted() {
			this.init();
		},

		computed: {
			maxPossibleWin: {
				get() {
					if (!this.paytableData) return 0;

					return this.paytableData.maxWin;
				}
			},

			maxTotalWin: {
				get() {
					if (!this.paytableData) return 0;

					return this.paytableData.maxTotalWin;
				}
			},

			maxMultiplier: {
				get() {
					if (!this.paytableData) return 0;

					return this.paytableData.maxMultiplier;
				}
			},

			rakePercent: {
				get() {
					if (!this.paytableData) return 0;

					return this.paytableData.rakePercent;
				}
			},

			maxPlayers: {
				get() {
					if (!this.paytableData) return 0;

					return this.paytableData.maxPlayers;
				}
			},

			PAYTABLE_WIDTH: function() {
				return PAYTABLE_WIDTH;
			},
			PAYTABLE_HEIGHT: function() {
				return PAYTABLE_HEIGHT;
			}
		},

		created: function() {
		},
	}
</script>

<style>
</style>

<style scoped>
	* {
		-webkit-transform-origin: top left;
		-ms-transform-origin: top left;
		transform-origin: top left;
		position: relative;
		font-family: "fnt_nm_barlow";
	}

	.paytable_frame {
		color: 				#ffffff;
		background-color: 	rgba(0, 0, 0, 0.75);
		border-radius: 		5px;
		margin-top:			auto;
		margin-left: 		auto;
		margin-right: 		auto;
		border: 			1px solid #ffffff;
	}

	.top_row {
		height: 61px;
		padding-right: 30px;
		padding-left: 30px;
		display: flex;
		justify-content: flex-end;
		align-items: center;
	}

	.top_row_content {
		display: flex;
	}

	.row {
		width: 100%;
		display: flex;
		flex-wrap: nowrap;
		position: relative;
	}

	.vertical_centered {
		top: 50%;
		transform: translate(0, -50%);
		position: absolute;
	}

	.paytable_content_container {
		position: relative;
	}

	#icon {
		font-size: 26px;
		font-family: "fnt_nm_barlow";
	}

</style>