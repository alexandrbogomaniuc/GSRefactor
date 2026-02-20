<template>	
	<span ref='mainContainer' :style="txtStyle">
		{{text}}
	</span>	
</template>

<script>
	import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
	import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

	export default {
		name: "BaseTextField",

		props: {
			translatableAssetId: String
		},

		created() {

		},

				methods: {
			updateMessage()
			{
				let lHTMLText_str = this.$refs.mainContainer.innerHTML;
				for ( let i = 1; i < lHTMLText_str.length - 1; i++)
				{
					if(lHTMLText_str.charAt(i) == "\n")
					{
						this.$refs.mainContainer.innerHTML = this.$refs.mainContainer.innerHTML.replace("\n", "<br/>");
					}
				}

				let lAsset_tad = I18.getTranslatableAssetDescriptor(this.translatableAssetId);
				let areaInnerContentDescriptor = lAsset_tad.areaInnerContentDescriptor;
				let areaDescriptor = areaInnerContentDescriptor.areaDescriptor;
				
				let tf = this.$refs.mainContainer;
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

				let lContent_htmlel = this.$refs.mainContainer;

				Object.assign(lContent_htmlel.style, {
					"transform": "translate(-50%, -50%) scale(" + lAuxXScale_num + ", " + lAuxYScale_num + ")",
					"webkitTransform": "translate(-50%, -50%) scale(" + lAuxXScale_num + ", " + lAuxYScale_num + ")",
					"msTransform": "translate(-50%, -50%) scale(" + lAuxXScale_num + ", " + lAuxYScale_num + ")",
				})
			}
		},

		computed: {
			txtStyle: {
				get() {
					let lAsset_tad = I18.getTranslatableAssetDescriptor(this.translatableAssetId);
					let textDescriptor = lAsset_tad.textDescriptor;
					let fontDescriptor = textDescriptor.fontDescriptor;

					let lStyle_obj = {
						fontSize: fontDescriptor.fontSize + 'px',
						fontFamily: fontDescriptor.fontName,
						color: fontDescriptor.color.toCSSString(),
						letterSpacing: textDescriptor.letterSpacing + 'px'
					};

					if (!isNaN(textDescriptor.lineSpacing))
					{
						lblStyle.lineHeight = textDescriptor.lineSpacing + "px";
					}
					return lStyle_obj;
				}
			},
			text: {
				get() {
					let lAsset_tad = I18.getTranslatableAssetDescriptor(this.translatableAssetId);
					let textDescriptor = lAsset_tad.textDescriptor;
					return textDescriptor.text;
				}
			}
		}
	}
</script>

<style scoped>
	* {
		position: absolute;
		white-space: nowrap;
	}
</style>