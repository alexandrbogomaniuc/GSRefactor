<template>
	<div 	v-on:click='callback($event)'
			v-on:mouseenter="onMouseEnter($event)"
			v-on:mouseleave="onMouseLeave($event)"
			:style="{display: 'inline-block', width: innerWidth+ 'px', height: innerHeight + 'px'}"
			@touchstart="down=true"
			@touchend="down=false"
			@mousedown="handleDown"
			@mouseup="handleUp"
			>
		<BaseImage :class='btnImageClass' id='btnBaseImage' ref='btnBaseImage'
			:assetName="bgAssetName"
			:scale="bgScale"
			style='position:absolute;'
		></BaseImage>

		<span class='lbl' ref='lblSpan'><slot name='label'></slot></span>
	</div>
</template>

<script>
	export default {
		name: 'base-button',

		data() {
			return {
				down: false,
				innerWidth: 0,
				innerHeight: 0
			}
		},

		props: {
			id: String,
			bgAssetName: String,
			bgScale: Number,
			leftPadding: {
				type: Number,
				default: 0
			},
			topPadding: {
				type: Number,
				default: 0
			},
			scaleRatio: {
				type: Number,
				default: 1
			}
		},

		methods: {
			callback: function(e) {
				this.$emit('click', this.id);
			},

			onMouseEnter: function(e) {
				this.$emit('mouseenter', this.id);
			},

			onMouseLeave: function(e) {
				this.$emit('mouseleave', this.id);
			},

			handleDown() {
				this.down = true;

				this.$el.addEventListener("dragend", this.handleUpCallback);
				document.addEventListener("mouseup", this.handleUpCallback);
			},

			handleUp() {
				this.down = false;

				this.$el.removeEventListener("dragend", this.handleUpCallback);
				document.removeEventListener("mouseup", this.handleUpCallback);
			}
		},

		computed: {
			btnImageClass: function() {
				return this.down ? "base_button_img_down" : "base_button_img";
			}
		},

		mounted() {
			this.innerWidth = this.$refs.btnBaseImage.croppedWidth;
			this.innerHeight = this.$refs.btnBaseImage.croppedHeight;

			this.handleUpCallback = this.handleUp.bind(this);
		}
	}
</script>

<style scoped>
	* {
		text-align: left;
		white-space: nowrap;
		cursor: pointer;
	}

	.base_button_img[disabled] {
		-webkit-filter: grayscale(1);
		filter: grayscale(1);
		pointer-events: none;
	}

	.base_button_img_down {
		-webkit-transform-origin: center center;
		-ms-transform-origin: center center;
		transform-origin: center center;

		transform: scale(0.9, 0.9);
	}

	.lbl {
		position: absolute;
		left: 50%;
		top: 51%;
		transform: translate(-50%, -51%);
	}

</style>