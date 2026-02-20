<template>
	<BaseButton
				id='printableRulesButton'
				bgAssetName='paytable/deactive_btn'
				bgActiveAssetName='paytable/active_btn'
				:style="labelStyle"
				@click="$emit('click')"
				ref='baseButton'
			>
			<template v-slot:label>{{label}}</template>
	</BaseButton>
</template>

<script type="text/javascript">

	import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
	import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
	import VueApplicationController from '../../VueApplicationController';

	export default {
		name: 'printable-rules-button',

		data() {
			return {
				label: ""
			}
		},

		methods: {
			updateMessage() {

				let lAsset_tad = I18.getTranslatableAssetDescriptor("TAPaytablePrintableLable");
				if (!lAsset_tad)
				{
					return;
				}
				let textDescriptor = lAsset_tad.textDescriptor;
				this.label = textDescriptor.text;
				
				let areaInnerContentDescriptor = lAsset_tad.areaInnerContentDescriptor;
				let areaDescriptor = areaInnerContentDescriptor.areaDescriptor;

				this.$nextTick(
					function()
					{
						this.writeIntoArea();
					}
				)
			},

			writeIntoArea() {
				let lAsset_tad = I18.getTranslatableAssetDescriptor("TAPaytablePrintableLable");
				let areaInnerContentDescriptor = lAsset_tad.areaInnerContentDescriptor;
				let areaDescriptor = areaInnerContentDescriptor.areaDescriptor;
				let textDescriptor = lAsset_tad.textDescriptor;
				
				let tf = this.$children[0].$refs.lblSpan;
				let curTextWidth = tf.offsetWidth;
				let curTextHeight = tf.offsetHeight;
				
				var lAuxXScale_num = 1;
				var lAuxYScale_num = 1;
				var lAuxXScaleRequired_bl = false;
				var lAuxYScaleRequired_bl = false;

				lAuxXScaleRequired_bl = (areaDescriptor.width < curTextWidth);
				if (lAuxXScaleRequired_bl)
				{
					lAuxXScale_num = areaDescriptor.width / curTextWidth;
				}

				lAuxYScaleRequired_bl = (areaDescriptor.height < curTextHeight);
				if (lAuxYScaleRequired_bl)
				{
					lAuxYScale_num = areaDescriptor.height / curTextHeight;
				}

				var lX_num = Math.floor(areaDescriptor.x + (areaDescriptor.width - curTextWidth) / 2);
				var lY_num = Math.floor(areaDescriptor.y + (areaDescriptor.height - curTextHeight) / 2);

				let lContent_htmlel = this.$children[0].$refs.lblSpan;
				let btnBase = this.$refs.baseButton;
				
				Object.assign(lContent_htmlel.style, {
					"transform": "scale(" + lAuxXScale_num + ", " + lAuxYScale_num + ")",
					"webkitTransform": "scale(" + lAuxXScale_num + ", " + lAuxYScale_num + ")",
					"msTransform": "scale(" + lAuxXScale_num + ", " + lAuxYScale_num + ")",
					"left": lX_num+"px",
					"top": lY_num+"px",
					"marginLeft": Math.floor(btnBase.innerWidth/2)+"px",
					"marginTop": Math.floor(btnBase.innerHeight/2)+"px"
				})
			},

			_addListeners()
			{
				APP.vueApplicationController.once(VueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER, this._onTimeToShowVueLayer, this);
			},

			_onTimeToShowVueLayer()
			{
				this.updateMessage();
			},

			onMouseEnter: function(e) {
				let lContent_htmlel = this.$children[0].$refs.lblSpan;

				Object.assign(lContent_htmlel.style, {
					"color": "#000000",
				})
			},

			onMouseLeave: function(e) {
				let lContent_htmlel = this.$children[0].$refs.lblSpan;

				Object.assign(lContent_htmlel.style, {
					"color": "#ffffff",
				})
			}
		},

		mounted() {
			this._addListeners();
		},

		computed: {
			labelStyle: {
				get() {
					let lAsset_tad = I18.getTranslatableAssetDescriptor("TAPaytablePrintableLable");
					let textDescriptor = lAsset_tad.textDescriptor;
					let fontDescriptor = textDescriptor.fontDescriptor;

					let lblStyle = {
						fontSize: fontDescriptor.fontSize + 'px',
						fontFamily: fontDescriptor.fontName,
						color: fontDescriptor.color.toCSSString(),
						letterSpacing: textDescriptor.letterSpacing + 'px',
					};

					if (!isNaN(textDescriptor.lineSpacing))
					{
						lblStyle.lineHeight = textDescriptor.lineSpacing + "px";
					}
					return lblStyle;
				}
			}
		},

		created() {
			this.updateMessage();
		},
	}
</script>

<style scoped>
</style>