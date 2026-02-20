import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AtlasConfig from '../../config/AtlasConfig';
import AtlasSprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';

let avatar_textures = null;
function initTexturesIfRequired()
{
	if (!avatar_textures)
	{
		let lAvatarAssetName_str = "profile/avatar";
		let lAvatarAsset_obj = APP.library.getAsset(lAvatarAssetName_str);

		avatar_textures = AtlasSprite.getFrames(lAvatarAsset_obj, AtlasConfig.Avatar, "");
	}
}

class ProfileAvatar extends Sprite 
{
	static get EVENT_ON_RENDER_TEXTURE_INVALIDATED()		{return "renderTextureInvalidated";}

	static get TOTAL_BORDERS_COUNT()
	{
		return 5;
	}

	static get TOTAL_HEROES_COUNT()
	{
		return 5;
	}

	static get TOTAL_BACKS_COUNT()
	{
		return 6;
	}

	static get AVATAR_TEXTURES()
	{
		initTexturesIfRequired();
		return avatar_textures;
	}

	constructor(availableStyles, userStyles)
	{
		super();

		this._availableStyles = availableStyles;

		let lBorder_num = userStyles.border;
		let lHero_num = userStyles.hero;
		let lBack_num = userStyles.back;

		//TODO: Remove when server be ready
		if (lBorder_num >= ProfileAvatar.TOTAL_BORDERS_COUNT) lBorder_num = 0;
		if (lHero_num >= ProfileAvatar.TOTAL_HEROES_COUNT) lHero_num = 0;
		if (lBack_num >= ProfileAvatar.TOTAL_BACKS_COUNT) lBack_num = 0;

		this._userStyles = {
			border: lBorder_num,
			hero: lHero_num,
			back: lBack_num
		};
		this._currentStyles = {
			border: lBorder_num,
			hero: lHero_num,
			back: lBack_num
		};

		this._borderTextures = [];
		this._heroTextures = [];
		this._backTextures = [];

		this._generateTextures();
		this._initAvatarView();
	}

	_generateTextures()
	{
		let bordersLen = ProfileAvatar.TOTAL_BORDERS_COUNT;
		let heroesLen = ProfileAvatar.TOTAL_HEROES_COUNT;
		let backsLen = ProfileAvatar.TOTAL_BACKS_COUNT;
		let max = Math.max(bordersLen, heroesLen, backsLen);

		let i = 0;

		while (i < max)
		{
			if (i < bordersLen)
			{
				let texture = this._getAvatarTexture("rims/rim_"+i);
				this._borderTextures.push(texture);
			}
			if (i < heroesLen)
			{
				let texture = this._getAvatarTexture("heroes/hero_"+i);
				this._heroTextures.push(texture);
			}
			if (i < backsLen)
			{
				let texture = this._getAvatarTexture("backs/back_"+i);
				this._backTextures.push(texture);
			}

			++i;
		}
	}

	_getAvatarTexture(aName_str)
	{
		for (let i = 0; i < ProfileAvatar.AVATAR_TEXTURES.length; i++)
		{
			if(ProfileAvatar.AVATAR_TEXTURES[i]._atlasName == aName_str)
			{
				return ProfileAvatar.AVATAR_TEXTURES[i];
			}
		}

		return null;
	}

	_initAvatarView()
	{
		this.avatarBackground = this.addChild(new Sprite);
		this.avatarBackground.texture = this._backTextures[this._userStyles.back];
		this.avatarBackground.position.set(0, -5);

		this.avatarHero = this.addChild(new Sprite);
		this.avatarHero.texture = this._heroTextures[this._userStyles.hero];

		this.avatarBorder = this.addChild(new Sprite);
		this.avatarBorder.texture = this._borderTextures[this._userStyles.border];
	}

	update(userStyles)
	{
		this.setBorder(userStyles.border);
		this.setHero(userStyles.hero);
		this.setBack(userStyles.back);

	}

	setBorder(id)
	{
		let textureId = this._availableStyles.borders[id];
		if (textureId != null)
		{
			if (this._currentStyles.border == textureId) return;

			this._currentStyles.border = textureId;
			this.avatarBorder.texture = this._borderTextures[textureId];
		}
	}

	setHero(id)
	{
		let textureId = this._availableStyles.heroes[id];
		if (textureId != null)
		{
			if (this._currentStyles.hero == textureId)
			{
				return;
			}

			this._currentStyles.hero = textureId;
			this.avatarHero.texture = this._heroTextures[textureId];
		}
	}

	setBack(id)
	{
		let textureId = this._availableStyles.backs[id];
		if (textureId != null)
		{
			if (this._currentStyles.back == textureId) return;

			this._currentStyles.back = textureId;
			this.avatarBackground.texture = this._backTextures[textureId];
		}
	}

	get userStyles()
	{
		return this._userStyles;
	}

	get currentStyles()
	{
		return this._currentStyles;
	}

	save()
	{
		this._userStyles = {
			border: this._currentStyles.border,
			hero: this._currentStyles.hero,
			back: this._currentStyles.back
		};

		this._invalidateRenderTexture();
	}

	cancel()
	{
		this.setBorder(this._userStyles.border);
		this.setHero(this._userStyles.hero);
		this.setBack(this._userStyles.back);
	}

	_invalidateRenderTexture()
	{
		if (this._renderTexture)
		{
			this._renderTexture.destroy();
			this._renderTexture = null;
		}
		this._generateAvatarRenderTexture();
		this.emit(ProfileAvatar.EVENT_ON_RENDER_TEXTURE_INVALIDATED);
	}

	getAvatarRenderTexture()
	{
		if (this._renderTexture)
		{
			return this._renderTexture;
		}

		return this._generateAvatarRenderTexture();
	}

	_generateAvatarRenderTexture()
	{
		let container = new Sprite();
		container.name = "AvatarRenderTextureContainer"; // for debug
		container.addChild(this.avatarBackground);
		container.addChild(this.avatarHero);
		container.addChild(this.avatarBorder);

		let bounds = container.getBounds();
		
		let brt = new PIXI.BaseRenderTexture(bounds.width, bounds.height, PIXI.SCALE_MODES.LINEAR);
		this._renderTexture = new PIXI.RenderTexture(brt);

		container.position.set(bounds.width/2, bounds.height/2);

		APP.stage.renderer.render(container, this._renderTexture);

		this.addChild(this.avatarBackground);
		this.addChild(this.avatarHero);
		this.addChild(this.avatarBorder);

		container.destroy();

		return this._renderTexture;
	}

	destroy()
	{
		this._renderTexture && this._renderTexture.destroy();
		super.destroy();
	}
}

export default ProfileAvatar;