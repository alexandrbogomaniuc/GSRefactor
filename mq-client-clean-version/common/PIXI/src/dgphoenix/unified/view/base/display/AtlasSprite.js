import Sprite from './Sprite';
import { APP, generateAbsoluteURL } from '../../../controller/main/globals';

var TextureCache = {};

/**
 * @description Sprite with frames based on atlas config
 * @augments Sprite
 * @example let view = new AtlasSprite(APP.library.getAsset("items"), itemsAtlas, "weapons/sword/1");
 */
class AtlasSprite extends Sprite {

	/**
	 * @constructor
	 * @param assets asset(s) of image
	 * @param atlases config(s) of atlas
	 * @param path path to frame(s)
	 */
	constructor(assets, atlases, path) {
		super();

		this.textures = AtlasSprite.getFrames(assets, atlases, path);
		this.texture = this.textures[0];

		if(this.totalFrames > 1) this.gotoAndPlay(0);
		else this.gotoAndStop(0);
	}

	/**
	 * Returns frames config list
	 * @param atlas Config(s) of atlas
	 * @param path Path to frame(s)
	 * @returns {Array}
	 * @static
	 */
	static getFramesInfo(atlas, path) {
		let configs = [];

		let scale = atlas.meta.scale ? atlas.meta.scale*1 : 1;

		let frameKeys = Object.keys(atlas.frames);

		for (let name of frameKeys) {
			if (name.substr(0, path.length) == path) {
				configs.push({name: name, frame: atlas.frames[name], scale: scale});
			}
		}

		return configs;
	}

	/**
	 * Generate masked texture(s).
	 * @param {Asset|Asset[]} assets - Source texture asset(s).
	 * @param {Asset|Asset[]} masks - Mask asset(s).
	 * @param {string} newName - New generated texture unique name.
	 * @static 
	 */
	static generateMaskedTextures(assets, masks, newName)
	{
		if (PIXI.utils.BaseTextureCache[generateAbsoluteURL(newName)])
		{
			return;
		}

		if(!Array.isArray(assets)) assets = [assets];
		if(!Array.isArray(masks)) masks = [masks];

		if (assets.length != masks.length)
		{
			throw new Error(`masks number and assets number are different`);
		}

		for(let ix=0; ix<assets.length; ix++) 
		{
			let asset = assets[ix];
			let mask = masks[ix];
			
			let textureUrl = generateAbsoluteURL(asset.src);
			let baseTexture = PIXI.utils.BaseTextureCache[textureUrl];

			if (!baseTexture)
			{
				throw new Error(`Texture ${textureUrl} not found`);
			}

			let maskTextureUrl = generateAbsoluteURL(mask.src);
			let maskTexture = PIXI.utils.BaseTextureCache[maskTextureUrl];

			if (!maskTexture)
			{
				throw new Error(`Texture ${maskTextureUrl} not found`);
			}

			let baseScaleMode = baseTexture.scaleMode;
			let baseResolution = baseTexture.resolution;

			let mergeContainer = new PIXI.Sprite();
			let baseSprite = mergeContainer.addChild(new PIXI.Sprite.from(new PIXI.Texture(baseTexture)));
			let maskSprite = mergeContainer.addChild(new PIXI.Sprite.from(new PIXI.Texture(maskTexture)));
			baseSprite.mask = maskSprite;

			let newTexture = new PIXI.RenderTexture.create({ width: baseTexture.width, height: baseTexture.height, scaleMode: baseScaleMode, resolution: baseResolution });
			
			if (APP.stage.isWebglContextLost)
			{
				return new Sprite();
			}

			APP.stage.renderer.render(mergeContainer, { renderTexture: newTexture});
			
			let lBaseTexName_str = generateAbsoluteURL(newName);
			PIXI.utils.BaseTextureCache[lBaseTexName_str] = newTexture.baseTexture;
			
			newTexture.destroy(false);

			APP.library.removeAsset(asset);
			APP.library.removeAsset(mask);

			baseSprite.destroy({children: true, texture: true, baseTexture: true});
			maskSprite.destroy({children: true, texture: true, baseTexture: true});
			mergeContainer.destroy({children:true});
		}
	}

	/**
	 * Get list of frames textures
	 * @param assets asset(s) of sprite
	 * @param atlases config(s) of atlas
	 * @param path path to frame(s)
	 * @returns {Array}
	 * @static
	 */
	static getFrames(assets, atlases, path) {

		if(!Array.isArray(assets)) assets = [assets];
		if(!Array.isArray(atlases)) atlases = [atlases];

		let frames = [];

		for(let ix=0; ix<assets.length; ix++) {
			let asset = assets[ix];
			let atlas = atlases[ix];

			let textureUrl = generateAbsoluteURL(asset.src);
			let baseTexture = PIXI.utils.BaseTextureCache[textureUrl];

			if (!baseTexture) {
				throw Error(`Texture ${textureUrl} not found`);
			}

			let configs = AtlasSprite.getFramesInfo(atlas, path);
			
			for (let config of configs) {
				
				let name = (baseTexture.resource ? baseTexture.resource.url : "") + "_" + config.name;
				if (TextureCache[name]) {
					frames.push(TextureCache[name]);
				}
				else {
					let resolution = config.scale;
					let rect = config.frame.frame;
					let frame = null;
					let trim = null;
					let pivot = null;
					let orig = new PIXI.Rectangle(0, 0, config.frame.sourceSize.w / resolution, config.frame.sourceSize.h / resolution);

					if (config.frame.rotated) {
						frame = new PIXI.Rectangle(rect.x / resolution, rect.y / resolution, rect.h / resolution, rect.w / resolution);
					}
					else {
						frame = new PIXI.Rectangle(rect.x / resolution, rect.y / resolution, rect.w / resolution, rect.h / resolution);
					}

					if (config.frame.trimmed) {
						trim = new PIXI.Rectangle(
							config.frame.spriteSourceSize.x / resolution,
							config.frame.spriteSourceSize.y / resolution,
							config.frame.spriteSourceSize.w / resolution,
							config.frame.spriteSourceSize.h / resolution
						);
					}

					if (config.frame.pixi_pivot) {
						pivot = new PIXI.Point(config.frame.pixi_pivot.x, config.frame.pixi_pivot.y);
					}

					let tex = new PIXI.Texture(baseTexture, frame, orig, trim, config.frame.rotated ? 2 : 0);
					tex._atlasName = config.name;
					tex._pivot = pivot;
					frames.push(tex);
					TextureCache[name] = tex;

					baseTexture.once('dispose', () => { 
														tex.destroy(); 
														delete TextureCache[name]; 
													});
				}
			}
		}

		return frames.length ? frames : [PIXI.Texture.EMPTY];
	}

	/**
	 * Get map of frames textures
	 * @param assets asset(s) of sprite
	 * @param atlases confis(s) of atlas
	 * @param path path to frame(s)
	 * @returns {Map}
	 * @static
	 */
	static getMapFrames(assets, atlases, path)
	{
		
		if(!Array.isArray(assets)) assets = [assets];
		if(!Array.isArray(atlases)) atlases = [atlases];

		let lResult_m = new Map();

		for(let ix = 0; ix < assets.length; ix++) {
			let lTextureUrl_str = generateAbsoluteURL(assets[ix].src);
			let lBaseTexture_bt = PIXI.utils.BaseTextureCache[lTextureUrl_str];

			if (!lBaseTexture_bt) {
				throw Error(`Texture ${lTextureUrl_str} not found`);
			}

			let lConfigs_arr = AtlasSprite.getFramesInfo(atlases[ix], path);
			
			for (let lConfig_obj of lConfigs_arr) {
				
				let lFullName_str = (lBaseTexture_bt.resource ? lBaseTexture_bt.resource.url : "") + "_" + lConfig_obj.name;
				if (TextureCache[lFullName_str]) {
					lResult_m[lConfig_obj.name] = TextureCache[lFullName_str];
				}
				else {
					let lResolution_num = lConfig_obj.scale;
					let lRect_obj = lConfig_obj.frame.frame;
					let lFrame_obj = null;
					let lTrim_obj = null;
					let lPivot_obj = null;
					let lOrig_obj = new PIXI.Rectangle(0, 0, lConfig_obj.frame.sourceSize.w / lResolution_num, lConfig_obj.frame.sourceSize.h / lResolution_num);

					if (lConfig_obj.frame.rotated) {
						lFrame_obj = new PIXI.Rectangle(lRect_obj.x/lResolution_num, lRect_obj.y/lResolution_num, lRect_obj.h/lResolution_num, lRect_obj.w/lResolution_num);
					}
					else {
						lFrame_obj = new PIXI.Rectangle(lRect_obj.x/lResolution_num, lRect_obj.y/lResolution_num, lRect_obj.w/lResolution_num, lRect_obj.h/lResolution_num);
					}

					if (lConfig_obj.frame.trimmed) {
						lTrim_obj = new PIXI.Rectangle(
							lConfig_obj.frame.spriteSourceSize.x / lResolution_num,
							lConfig_obj.frame.spriteSourceSize.y / lResolution_num,
							lConfig_obj.frame.spriteSourceSize.w / lResolution_num,
							lConfig_obj.frame.spriteSourceSize.h / lResolution_num
						);
					}

					if (lConfig_obj.frame.pixi_pivot) {
						lPivot_obj = new PIXI.Point(lConfig_obj.frame.pixi_pivot.x, lConfig_obj.frame.pixi_pivot.y);
					}

					let l_t = new PIXI.Texture(lBaseTexture_bt, lFrame_obj, lOrig_obj, lTrim_obj, lConfig_obj.frame.rotated ? 2 : 0);
					l_t._atlasName = lConfig_obj.name;
					l_t._pivot = lPivot_obj;
					lResult_m[lConfig_obj.name] = l_t;
					TextureCache[lFullName_str] = l_t;

					lBaseTexture_bt.once('dispose', () => { 
														l_t.destroy(); 
														delete TextureCache[lFullName_str]; 
													});
				}
			}
		}

		return lResult_m;
	}

	/**
	 * Sort textures by number index
	 * @param start start position of number index in frame name
	 * @param len length of number index
	 */
	numericSortTextures(start, len) {
		this.textures.sort((a, b) => {
			let ix1 = a._atlasName.substr(start, len) * 1;
			let ix2 = b._atlasName.substr(start, len) * 1;

			if(ix1 > ix2) return 1;
			if(ix1 < ix2) return -1;
			return 0;
		});
		this.texture = this.textures[this.currentFrame];
	}
}

export default AtlasSprite;