<template>
	<div ref='mainDiv' class='vue_screen_div' @touchmove.prevent>
		<iframe
			ref='helperFrame'
			class='helper_frame'
		/> <!-- hack for listening resize event: https://habr.com/post/166321/  -->
		<paytable
			:style='{transform: "scale(" + scaleRatio + ", " + scaleRatio + ")"}'
			@close='onPaytableCloseButtonClicked'
			@printable-rules-click='onPaytablePrintableRulesButtonClicked'/>
	</div>
</template>

<script>

export default {

	name: "screen",

	data: function() {
		return {
			clientWidth: undefined,
			clientHeight: undefined,
			scaleRatio: 1
		}
	},

	methods: {

		handleResize() {
			this.clientWidth 	= this.$refs.mainDiv.clientWidth;
			this.clientHeight 	= this.$refs.mainDiv.clientHeight;
			this.scaleRatio 	= this.clientWidth/960;

			if (!this.clientWdith)
			{
				setTimeout(() => this.handleResize(), 200);
			}
		},

		onPaytableCloseButtonClicked() {
			this.$root.$emit('paytable-close-button-click');
		},

		onPaytablePrintableRulesButtonClicked() {
			this.$root.$emit("paytable-printable-rules-click");
		}
	},

	mounted() {
		this.$nextTick(function() {
			let callback = this.handleResize.bind(this);
			this.$refs.helperFrame.onresize = function ()
			{
				callback();
			}

			//Init
			this.handleResize();
		})
	}
}
</script>

<style>
* {
		box-sizing: border-box;
		-moz-box-sizing:border-box;
		-webkit-box-sizing:border-box;
		-ms-box-sizing:border-box;
}
</style>

<style scoped>	

	.vue_screen_div {
		position: absolute;
		background-color: 	rgba(0, 0, 0, 0);
		width: 100%;
		height: 100%;
	}

	.helper_frame {
		visibility: hidden;
		touch-action: manipulation;
		pointer-events: none;
		position:absolute;
		z-index:-1;
		width: 100%;
		height: 100%;
	}
</style>