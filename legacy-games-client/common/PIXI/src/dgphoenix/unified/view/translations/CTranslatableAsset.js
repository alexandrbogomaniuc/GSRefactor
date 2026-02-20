import Sprite from '../base/display/Sprite';
import AtlasSprite from '../base/display/AtlasSprite';

import TranslatableAssetDescriptor from '../../model/translations/data/TranslatableAssetDescriptor';
import TAImageDescriptor from '../../model/translations/data/TAImageDescriptor';
import TASpriteDescriptor from '../../model/translations/data/TASpriteDescriptor';
import { APP } from '../../controller/main/globals';
import TextField from '../base/display/TextField';
import AlignDescriptor from '../../model/display/align/AlignDescriptor';
import LGFillDescriptor from '../../model/display/color/gradient/LGFillDescriptor';

/**
 * Translatable asset view.
 * @class
 * @extends Sprite
 */
class CTranslatableAsset extends Sprite
{
	/**
	 * Asset text for text-based assets.
	 * @type {string}
	 */
	get text()
	{
		if (!this._fAssetDescriptor_utad.isTextBasedAsset)
		{
			throw new Error ("Attempt to get text from non text based asset");
		}

		return this._fAssetDescriptor_utad.textDescriptor.text;
	}

	/**
	 * Update displayed text.
	 * @param {string} value
	 */
	set text(value)
	{
		this._setText(value);
	}

	/**
	 * Asset view.
	 * @type {TextField|Sprite|AtlasSprite}
	 */
	get assetContent()
	{
		return this._fAssetContent_udo;
	}

	/**
	 * Asset descriptor.
	 * @type {TranslatableAssetDescriptor}
	 */
	get descriptor()
	{
		return this._fAssetDescriptor_utad;
	}

	/**
	 * Update text-based asset.
	 */
	update()
	{
		this._update();
	}

	/**
	 * Set or replace asset descriptor.
	 * @param {TranslatableAssetDescriptor} aAssetDescriptor_utad 
	 */
	setAssetDescriptor(aAssetDescriptor_utad)
	{
		this._setAssetDescriptor(aAssetDescriptor_utad);
	}

	/**
	 * @constructor
	 * @param {TranslatableAssetDescriptor} [aOptAssetDescriptor_utad] 
	 * @param {TextField|Sprite|AtlasSprite} [aAssetContent_udo] 
	 */
	constructor(aOptAssetDescriptor_utad, aAssetContent_udo)
	{
		super();

		/**
		 * Asset view.
		 * @type {TextField|Sprite|AtlasSprite}
		 * @private
		 */
		this._fAssetContent_udo = null;

		/**
		 * Asset descriptor.
		 * @type {TranslatableAssetDescriptor}
		 * @private
		 */
		this._fAssetDescriptor_utad = null;

		/**
		 * Asset area view for debug.
		 * @type {PIXI.Graphics}
		 */
		this._fDebuggingAreaRectangle_ucr = null;

		if (aAssetContent_udo)
		{
			this._fAssetContent_udo = aAssetContent_udo;
		}

		this._initCTranslatableAsset(aOptAssetDescriptor_utad);
	}

	/**
	 * Destroy instance.
	 */
	destroy()
	{
		this._fAssetContent_udo && this._fAssetContent_udo.destroy();
		this._fAssetContent_udo = null;

		this._fAssetDescriptor_utad = null;

		if (this._fDebuggingAreaRectangle_ucr) 
		{
			this._fDebuggingAreaRectangle_ucr.clear();
			this._fDebuggingAreaRectangle_ucr.destroy();
			this._fDebuggingAreaRectangle_ucr = null;
		}

		super.destroy();
	}

	_initCTranslatableAsset(aOptAssetDescriptor_utad)
	{
		this._initDebuggingAreaViewIfRequired();
		if (aOptAssetDescriptor_utad)
		{
			this._setAssetDescriptor(aOptAssetDescriptor_utad);
		}
	}

	_initDebuggingAreaViewIfRequired ()
	{
		if (this._fDebuggingAreaRectangle_ucr)
		{
			return;
		}

		this._fDebuggingAreaRectangle_ucr = new PIXI.Graphics();
		this._fDebuggingAreaRectangle_ucr.x = 0;
		this._fDebuggingAreaRectangle_ucr.y = 0;
		this._fDebuggingAreaRectangle_ucr.height = 1;
		this._fDebuggingAreaRectangle_ucr.width = 1;

		this.addChild(this._fDebuggingAreaRectangle_ucr);
	}

	_setAssetDescriptor(aAssetDescriptor_utad)
	{
		if (!aAssetDescriptor_utad
			|| !(aAssetDescriptor_utad instanceof TranslatableAssetDescriptor))
		{
			throw new Error("Invalid asset descriptor argument value: " + aAssetDescriptor_utad);
		}
		if (aAssetDescriptor_utad.isHiddenAsset)
		{
			throw new Error("Hidden asset is not supported yet: " + aAssetDescriptor_utad.assetId);
		}

		var lOldAssetDescriptor_utad = this._fAssetDescriptor_utad;
		if (!lOldAssetDescriptor_utad)
		{
			this._fAssetDescriptor_utad = aAssetDescriptor_utad;
			this._initAssetContent();
		}
		else
		{
			this._reinitAssetContent(aAssetDescriptor_utad);
		}
	}

	_reinitAssetContent (aNewAssetDescriptor_utad)
	{
		var lOldAssetDescriptor_utad = this._fAssetDescriptor_utad;
		this._fAssetDescriptor_utad = aNewAssetDescriptor_utad;
		this._releaseAssetContentIfRequired(aNewAssetDescriptor_utad, lOldAssetDescriptor_utad);
		this._initAssetContent();
	}

	_releaseAssetContentIfRequired (aNewAssetDescriptor_utad, aOldAssetDescriptor_utad)
	{
		if (
				aNewAssetDescriptor_utad.isTextBasedAsset
				&& aOldAssetDescriptor_utad.isTextBasedAsset
			)
		{
			//for text based asset content regeneration is not required, the same content will be used
			return;
		}
		this._releaseAssetContent();
	}

	_releaseAssetContent ()
	{
		if (!this._fAssetContent_udo)
		{
			return;
		}
		this._fAssetContent_udo.destroy();
		this._fAssetContent_udo = null;
	}

	_initAssetContent ()
	{
		var l_utad = this._fAssetDescriptor_utad;
		if (l_utad.isTextBasedAsset)
		{
			this._initTextBasedAsset(l_utad.textDescriptor);
		}
		else if (l_utad.isImageBasedAsset)
		{
			this._initImageBasedAsset(l_utad.imageDescriptor, l_utad);
		}
		else if (l_utad.isVoidBasedAsset)
		{
			if(!this._fAssetContent_udo)
			{
				throw new Error("Content is undefined");
			}
			else
			{
				this._alignAssetContent(l_utad.areaInnerContentDescriptor);
			}
		}
		else
		{
			throw new Error("Unsupported asset type: " + l_utad.assetType);
		}

	}

	//TEXT BASED ASSET...
	_initTextBasedAsset(aTextDescriptor_utatd)
	{
		let textField = this._fAssetContent_udo;
		if (!textField)
		{
			textField = new TextField(this.textFormat);
			this._fAssetContent_udo = textField;
			this.addChild(textField);						
		}
		else if (!(textField instanceof TextField))
		{
			throw new Error("Trying to init text based asset on non TextField object!");
		}
		else
		{
			textField.textFormat = this.textFormat;
		}
		
		this._setText(aTextDescriptor_utatd.text);
	}

	/**
	 * Text format based on asset descriptor.
	 * @type {Object}
	 */
	get textFormat()
	{
		let format = {};

		format.trim = false; //If set to true, cuts off some text

		var lAlignDescriptor_uad = this._fAssetDescriptor_utad.areaInnerContentDescriptor.contentAlignDescriptor;
		if (lAlignDescriptor_uad)
		{
			format.textBaseline = AlignDescriptor.ALPHABETIC;
			format.align = lAlignDescriptor_uad.hAlign;
		}
		
		var lLineWidth_num = 0;
		let aTextDescriptor_utatd = this._fAssetDescriptor_utad.textDescriptor;
		if (aTextDescriptor_utatd.isAutoWrapMode)
		{
			format.wordWrap = true;

			if (aTextDescriptor_utatd.isAutoWidthMode)
			{
				lLineWidth_num = this._fAssetDescriptor_utad.areaInnerContentDescriptor.areaDescriptor.width;
			}
			else
			{
				lLineWidth_num = +aTextDescriptor_utatd.width;
			}

			format.wordWrapWidth = lLineWidth_num;
		}
		
		let fontDescriptor = aTextDescriptor_utatd.fontDescriptor;
		format.fontFamily = fontDescriptor.fontName;
		format.fontSize = +fontDescriptor.fontSize;
		format.fontWeight = fontDescriptor.isBold ? 'bold' : 'normal';

		format.lineHeight = aTextDescriptor_utatd.isAutoLineSpacing ? 0 : +aTextDescriptor_utatd.lineSpacing;
		format.letterSpacing = aTextDescriptor_utatd.letterSpacing || 0;

		this.alpha = aTextDescriptor_utatd.alpha || 1;

		let colorDescriptor = this._fAssetDescriptor_utad.textDescriptor.fontDescriptor.color;
		if (colorDescriptor instanceof LGFillDescriptor)
		{
			let colors = [];
			let positions = [];

			var lLinearGradientFillDescriptor_ulgfd = colorDescriptor;
			var lX0_num = lLinearGradientFillDescriptor_ulgfd.pointFrom.x;
			var lY0_num = lLinearGradientFillDescriptor_ulgfd.pointFrom.y;
			var lX1_num = lLinearGradientFillDescriptor_ulgfd.pointTo.x;
			var lY1_num = lLinearGradientFillDescriptor_ulgfd.pointTo.y;

			var lKeysCount_int = lLinearGradientFillDescriptor_ulgfd.keysCount;
			for (var i = 0; i < lKeysCount_int; i++)
			{
				var lKeyColor_ukc = lLinearGradientFillDescriptor_ulgfd.getKeyColor(i);
				colors.push(lKeyColor_ukc.color.toCSSString());
				positions.push(lKeyColor_ukc.position);
			}

			format.fill = colors;
			format.fillGradientStops = positions;
			format.linearGradientPoints = [lX0_num, lY0_num, lX1_num, lY1_num];
		}
		else
		{
			format.fill = colorDescriptor.toCSSString();
		}

		let strokeDescriptor = this._fAssetDescriptor_utad.textDescriptor.fontDescriptor.stroke;
		if (strokeDescriptor)
		{
			format.stroke = strokeDescriptor.color.toCSSString();
			format.strokeThickness = strokeDescriptor.tickness;
		}

		let shadowsDescriptor = this._fAssetDescriptor_utad.textDescriptor.shadowsDescriptor;
		let shadowPadding = 0;
		let textPadding = aTextDescriptor_utatd.padding || 0;
		if (shadowsDescriptor)
		{
			let shadowDescriptor = shadowsDescriptor.getTextShadowDescriptor(0);

			format.dropShadow = true;
			format.dropShadowColor = shadowDescriptor.color.toCSSString(true);
			format.dropShadowAlpha = shadowDescriptor.color.normalizedAlpha;
			format.dropShadowBlur = shadowDescriptor.blurRadius;
			format.dropShadowAngle = shadowDescriptor.shadowAngle*Math.PI/180;
			format.dropShadowDistance = shadowDescriptor.shadowDistance;
			if (shadowDescriptor.isShadowAlphaDefined)
			{
				format.dropShadowAlpha = shadowDescriptor.shadowAlpha;
			}
			
			if ((shadowDescriptor.shadowAngle%360) > 90)
			{
				shadowPadding = shadowDescriptor.shadowDistance;
			}
		}
		format.padding = Math.max(shadowPadding, textPadding);
		
		return format;
	}

	_setText (aText_str)
	{
		this._throwIfNonTextBasedAsset();

		this._fAssetContent_udo.uncache();
		this._fAssetContent_udo.text = aText_str;

		this._alignAssetContent(this._fAssetDescriptor_utad.areaInnerContentDescriptor);

		if (this._fAssetContent_udo.isMobile)
		{
			let lAreaInnerContentDescriptor_uaicd = this._fAssetDescriptor_utad.areaInnerContentDescriptor;
			let lMobileScale_num = this._fAssetContent_udo.mobileScale;
			this._formatForMobile(lAreaInnerContentDescriptor_uaicd.areaDescriptor, lAreaInnerContentDescriptor_uaicd.contentAlignDescriptor, lAreaInnerContentDescriptor_uaicd.contentScaleDescriptor, lMobileScale_num);
		}

		this._fAssetContent_udo.cache();
	}

	_update()
	{
		this._throwIfNonTextBasedAsset();

		this._fAssetContent_udo.uncache();

		this._alignAssetContent(this._fAssetDescriptor_utad.areaInnerContentDescriptor);

		if (this._fAssetContent_udo.isMobile)
		{
			let lAreaInnerContentDescriptor_uaicd = this._fAssetDescriptor_utad.areaInnerContentDescriptor;
			let lMobileScale_num = this._fAssetContent_udo.mobileScale;
			this._formatForMobile(lAreaInnerContentDescriptor_uaicd.areaDescriptor, lAreaInnerContentDescriptor_uaicd.contentAlignDescriptor, lAreaInnerContentDescriptor_uaicd.contentScaleDescriptor, lMobileScale_num);
		}

		this._fAssetContent_udo.cache();
	}

	_throwIfNonTextBasedAsset ()
	{
		if (!this._fAssetDescriptor_utad)
		{
		}
		if (!this._fAssetDescriptor_utad.isTextBasedAsset)
		{
			throw new Error("Trying to execute text-based operation on non-text based asset.");
		}
	}
	//...TEXT BASED ASSET

	//IMAGE BASED ASSET...
	_initImageBasedAsset(aImgDescriptor_utaid, aTADescriptor_utad)
	{
		var lAssetContent_udo;
		if (aImgDescriptor_utaid instanceof TAImageDescriptor)
		{
			lAssetContent_udo = APP.library.getSprite(aTADescriptor_utad.assetId);
			lAssetContent_udo.anchor.set(0);
		}
		else if (aImgDescriptor_utaid instanceof TASpriteDescriptor)
		{
			let imageAssetName;
			let assets = [];
			for (let i=0; i<aImgDescriptor_utaid.imagesUrls.length; i++)
			{
				imageAssetName = aTADescriptor_utad.assetId+`_${i}`;
				assets.push(APP.library.getAsset(imageAssetName));
			}
			let configs = aImgDescriptor_utaid.atlasConfigs;
			
			lAssetContent_udo = new AtlasSprite(assets, configs, aImgDescriptor_utaid.framesPath);
			lAssetContent_udo.textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
			lAssetContent_udo.animationSpeed = aImgDescriptor_utaid.framerate/60;
		}
		
		this._fAssetContent_udo = lAssetContent_udo;
		
		this.addChild(lAssetContent_udo);
		
		this._alignAssetContent(this._fAssetDescriptor_utad.areaInnerContentDescriptor);
	}
	//...IMAGE BASED ASSET


	_alignAssetContent(aAreaInnerContentDescriptor_uaicd)
	{
		if (!aAreaInnerContentDescriptor_uaicd)
		{
			return;
		}

		var lAssetContent_udo = this._fAssetContent_udo;
		var lContentScaleDescriptor_usd = aAreaInnerContentDescriptor_uaicd.contentScaleDescriptor;

		if (lContentScaleDescriptor_usd)
		{
			lAssetContent_udo.scale.set(1);
		}

		var lAreaDescriptor_ur = aAreaInnerContentDescriptor_uaicd.areaDescriptor;
		this._writeIntoArea(lAreaDescriptor_ur, aAreaInnerContentDescriptor_uaicd.contentAlignDescriptor, lContentScaleDescriptor_usd);

		if (lAreaDescriptor_ur)
		{
			this._fDebuggingAreaRectangle_ucr.x = lAreaDescriptor_ur.x;
			this._fDebuggingAreaRectangle_ucr.y = lAreaDescriptor_ur.y;
			this._fDebuggingAreaRectangle_ucr.clear();
			this._fDebuggingAreaRectangle_ucr.beginFill(0xffa000, 0.6);
			this._fDebuggingAreaRectangle_ucr.drawRect(0, 0, lAreaDescriptor_ur.width, lAreaDescriptor_ur.height);
			this._fDebuggingAreaRectangle_ucr.endFill();

			this._fDebuggingAreaRectangle_ucr.visible = false;//APP.isDebugMode;
		}
	}

	_writeIntoArea (aArea_ur, aAlignDescriptor_uad, aOptScaleDescriptor_usd)
	{
		var lAssetContentBounds_rt;
		if (this._fAssetContent_udo instanceof AtlasSprite)
		{
			lAssetContentBounds_rt = this._fAssetContent_udo.getLocalBounds();
		}
		else if (this._fAssetContent_udo instanceof TextField)
		{
			lAssetContentBounds_rt = this._fAssetContent_udo.textBounds;
		}
		else
		{
			lAssetContentBounds_rt = this._fAssetContent_udo.getLocalBounds();
		}

		if (!lAssetContentBounds_rt)
		{
			if (this._fAssetDescriptor_utad.assetType == TranslatableAssetDescriptor.ASSET_TYPE_VOID_BASED)
			{
				lAssetContentBounds_rt = new PIXI.Rectangle(this._fAssetContent_udo.x, this._fAssetContent_udo.y, this._fAssetContent_udo.width, this._fAssetContent_udo.height);
			}
			else
			{
				return;
			}
		}

		var lX_num;
		var lY_num;
		
		var lAreaX_num = aArea_ur.x;
		var lAreaWidth_num = aArea_ur.width;
		var lInitialScaleX_num = this._fAssetContent_udo.scale.x;
		var lScaledObjectWidth_num = lAssetContentBounds_rt.width;
		
		this._fAssetContent_udo.pivot.set(lAssetContentBounds_rt.x, lAssetContentBounds_rt.y);
		
		var lAreaY_num = aArea_ur.y;
		var lAreaHeight_num = aArea_ur.height;
		var lInitialScaleY_num = this._fAssetContent_udo.scale.y;
		var lScaledObjectHeight_num = lAssetContentBounds_rt.height;

		if (aOptScaleDescriptor_usd)
		{
			var lAuxXScale_num = 1;
			var lAuxYScale_num = 1;
			var lAuxXScaleRequired_bl = false;
			var lAuxYScaleRequired_bl = false;
			if (aOptScaleDescriptor_usd.isXAutoScaleMode())
			{
				lAuxXScaleRequired_bl = (lAreaWidth_num < lScaledObjectWidth_num);
				if (lAuxXScaleRequired_bl)
				{
					lAuxXScale_num = lAreaWidth_num / lScaledObjectWidth_num;
				}
			}
			else if (aOptScaleDescriptor_usd.isXExactFitScaleMode())
			{
				lAuxXScaleRequired_bl = (lAreaWidth_num !== lScaledObjectWidth_num);
				if (lAuxXScaleRequired_bl)
				{
					lAuxXScale_num = lAreaWidth_num / lScaledObjectWidth_num;
				}
			}
			

			if (aOptScaleDescriptor_usd.isYAutoScaleMode())
			{
				lAuxYScaleRequired_bl = (lAreaHeight_num < lScaledObjectHeight_num);
				if (lAuxYScaleRequired_bl)
				{
					lAuxYScale_num = lAreaHeight_num / lScaledObjectHeight_num;
				}
			}
			else if (aOptScaleDescriptor_usd.isYExactFitScaleMode())
			{
				lAuxYScaleRequired_bl = (lAreaHeight_num !== lScaledObjectHeight_num);
				if (lAuxYScaleRequired_bl)
				{
					lAuxYScale_num = lAreaHeight_num / lScaledObjectHeight_num;
				}
			}

			if (lAuxXScaleRequired_bl || lAuxYScaleRequired_bl)
			{
				if (!aOptScaleDescriptor_usd.isDistortionAllowed())
				{
					var lAuxScale_num = lAuxXScale_num < lAuxYScale_num ? lAuxXScale_num : lAuxYScale_num;
					lAuxXScale_num = lAuxScale_num;
					lAuxYScale_num = lAuxScale_num;
				}
				var lTotalXScale_num = lAuxXScale_num * lInitialScaleX_num;
				var lTotalYScale_num = lAuxYScale_num * lInitialScaleY_num;
				lScaledObjectWidth_num *= lAuxXScale_num;
				lScaledObjectHeight_num *= lAuxYScale_num;

				this._fAssetContent_udo.scale.set(lTotalXScale_num, lTotalYScale_num);
			}
		}

		if (aAlignDescriptor_uad.isLeftAlign())
		{
			lX_num = lAreaX_num;
		}
		else if (aAlignDescriptor_uad.isCenterAlign())
		{
			lX_num = lAreaX_num + (lAreaWidth_num - lScaledObjectWidth_num) / 2;
		}
		else if (aAlignDescriptor_uad.isRightAlign())
		{
			lX_num = lAreaX_num + (lAreaWidth_num - lScaledObjectWidth_num);
		}

		if (aAlignDescriptor_uad.isTopAlign())
		{
			lY_num = lAreaY_num;
		}
		else if (aAlignDescriptor_uad.isMiddleAlign())
		{
			lY_num = lAreaY_num + (lAreaHeight_num - lScaledObjectHeight_num) / 2;
		}
		else if (aAlignDescriptor_uad.isBottomAlign())
		{
			lY_num = lAreaY_num + (lAreaHeight_num - lScaledObjectHeight_num);
		}
		
		this._fAssetContent_udo.x = lX_num;
		this._fAssetContent_udo.y = lY_num;
	}

	_formatForMobile(aArea_ur, aAlignDescriptor_uad, aScaleDescriptor_sd, aMobileScale_num)
	{
		let lAssetContent_udo = this._fAssetContent_udo;

		lAssetContent_udo.uncache();

		lAssetContent_udo.scale.set(aMobileScale_num);
		if (!aScaleDescriptor_sd || !aScaleDescriptor_sd.isXNoScaleMode())
		{
			lAssetContent_udo.maxWidth = aArea_ur.width;
		}
		
		if (!aScaleDescriptor_sd || !aScaleDescriptor_sd.isYNoScaleMode())
		{
			lAssetContent_udo.maxHeight = aArea_ur.height;
		}
		
		if (aAlignDescriptor_uad.isCenterAlign())
		{
			this._fAssetContent_udo.x = 0;
			this._fAssetContent_udo.anchor.x = 0.5;
		}

		if (aAlignDescriptor_uad.isMiddleAlign())
		{
			this._fAssetContent_udo.y = aArea_ur.y + (aArea_ur.height - this._fAssetContent_udo.textBounds.height) / 2;
		}

		this._fAssetContent_udo.pivot.set(0, 0);
		lAssetContent_udo.cache();
	}

}

export default CTranslatableAsset