<template>
	<span class="bar" ref="barElement">
		<div class="bar_button" ref="barButtonElement"
			@mousedown="onInteractionStart">
		</div>
	</span>
</template>

<script type="text/javascript">

	export default {
		name: 'scroll-bar-component',

		props: {
			referencePageElementId: String,
			referenceElementId: String,
			referenceHeightCutOffset: [String, Number],
			referenceParentElementId: String,
			referenceRightBorderElementId: String,
			marginRight: [String, Number]
		},

		data() {
			return {
				isTragged: false,
				tragOffsetY: undefined
			}
		},

		computed: {
			testComputed: function() {

			},
		},

		methods: {
			onInteractionStart(event) {
				let refElement = document.getElementById(this.referenceElementId);
				let refPageElement = document.getElementById(this.referencePageElementId);
				let buttonElement = this.$refs.barButtonElement;

				this.$data.tragOffsetY = event.clientY -  buttonElement.offsetTop;
				this.$data.isTragged = true;
				event.stopPropagation();
    			event.preventDefault();
			},

			onInteractionEnd() {
				this.$data.isTragged = false;
			},

			onMouseMove(event) {
				if(this.$data.isTragged)
				{
					let refElement = document.getElementById(this.referenceElementId);
					let refScrollHeight = refElement.scrollHeight;
					let visibleHeight = refElement.clientHeight;

					if(this.referenceHeightCutOffset)
					{
						visibleHeight-= this.referenceHeightCutOffset;
						refScrollHeight-= this.referenceHeightCutOffset;
					}

					let scaleFactor = visibleHeight / refScrollHeight;
					let mouseY = event.clientY;

					refElement.scrollTop = (mouseY - this.$data.tragOffsetY) / scaleFactor;

					this.update();
				}
			},

			update() {
				let barElement = this.$refs.barElement;
				if(!barElement)
				{
					return;
				}

				let buttonElement = this.$refs.barButtonElement;

				let barHeigth = barElement.clientHeight;
				let buttonHeigth = buttonElement.clientHeight;
				let refElement = document.getElementById(this.referenceElementId);

				if (!refElement)
				{
					return;
				}

				let refPageElement = document.getElementById(this.referencePageElementId);

				let visibleHeight = refElement.clientHeight;
				let refScrollHeight = refElement.scrollHeight;

				let scaleFactor = visibleHeight / refScrollHeight;
				let buttonHeight = scaleFactor * visibleHeight;

				if(buttonHeight >= visibleHeight)
				{
					barElement.style.display = "inline-block";
				}
				else
				{
					let offsetX = -barElement.clientWidth;
					let offsetY = refPageElement ? -refPageElement.scrollTop : 0;
					let scrollY = refElement.scrollTop * scaleFactor;
					let lX_num = 0;

					if(this.referenceRightBorderElementId)
					{
						let lRightBorderElement = document.getElementById(this.referenceRightBorderElementId);
						

						let lRightBorderX_num = lRightBorderElement.offsetLeft + lRightBorderElement.clientWidth;
						

						lX_num = lRightBorderX_num + offsetX;

					}
					else
					{
						lX_num = refElement.offsetLeft + refElement.clientWidth + offsetX;

					}

					if(this.marginRight)
					{
						lX_num -= this.marginRight;
					}

					barElement.style.left = lX_num + "px";

					let parentScrollY = 0;

					if(this.referenceParentElementId)
					{
						let refParentElement = document.getElementById(this.referenceParentElementId);
						parentScrollY = refParentElement.scrollTop
					}

					barElement.style.top = (refElement.offsetTop + offsetY + parentScrollY) + "px";

					let height = refElement.clientHeight;

					barElement.style.height = (height)+ "px";
					barElement.style.display = "inline-block";

					buttonElement.style.top = (scrollY) + "px";
					buttonElement.style.height = (buttonHeight) + "px";

					
				}
			},

			scroll() {
				this.update();
			}
		},

		

		created: function() {
			document.body.addEventListener("resize", this.update.bind(this));
			document.body.addEventListener("mouseup", this.onInteractionEnd.bind(this));
			document.body.addEventListener("mousemove", this.onMouseMove.bind(this));

			document.addEventListener('scroll', this.scroll, true);
		},

		mounted: function () {
			this.update();
		},

		beforeDestroy() {
			document.removeEventListener("scroll", this.update, true);
		}
	}
</script>

<style scoped>

	.bar {
		width: 3px;
		background-color: #484848;
		top:0;
		z-index: 1000000;
		overflow-y: hidden;
		display: inline-block;
		height: 500px;
	}

	.bar_button {
		width: 100%;
		height: 100px;
		position: relative;
		background-color: #ffca13;
		top:0;
		overflow-y: hidden;
	}

	.hidden {
		opacity: 0;
	}
</style>