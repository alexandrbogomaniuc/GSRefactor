<template>
	<div
		v-bind:style="{overflow:'hidden', width:croppedWidth + 'px', height: croppedHeight + 'px' }"
		ref='divElement'
	>
		<img
			crossorigin
			:src="imgSrc"
			ref='imgElement'
			:style="{width: fullWidth + 'px', height: fullHeight + 'px'}"
		/>
	</div>
</template>

<script>
	import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

	export default {
		name: "base-image",
		data: function() {
			return {
				croppedWidth: undefined,
				croppedHeight: undefined,
				fullWidth: undefined,
				fullHeight: undefined
			}
		},
		props: {
			assetName: {
				type: String,
				required: true
			}
		},
		computed: {
			imgSrc : function () {
				let lAsset_obj = APP.library.getAsset(this.assetName);
				this.croppedWidth = lAsset_obj.width;
				this.croppedHeight = lAsset_obj.height;

				let lBaseTexture_obj = lAsset_obj.baseTexture;
				if (lBaseTexture_obj)
				{
					this.fullWidth = lBaseTexture_obj.width;
					this.fullHeight = lBaseTexture_obj.height;
				}
				else
				{
					this.fullWidth = this.croppedWidth;
					this.fullHeight = this.croppedHeight;
				}

				return lAsset_obj.bitmap.src;
			}
		},
		mounted() {

		},
		methods: {

		},
		beforeDestroy() {
		}
	}
</script>

<style scoped>
	* {
		display: inline-block;
	}
</style>