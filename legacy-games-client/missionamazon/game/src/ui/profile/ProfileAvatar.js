import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AtlasConfig from '../../config/AtlasConfig';
import AtlasSprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';

let avatar_textures = null;
function initTexturesIfRequired()
{
	if (!avatar_textures)
	{
		let lAvatarAssetName_str = "avatar/avatar";
		let lAvatarAsset_obj = APP.library.getAsset(lAvatarAssetName_str);

		avatar_textures = AtlasSprite.getFrames(lAvatarAsset_obj, AtlasConfig.Avatar, "");
	}
}

class ProfileAvatar extends Sprite 
{
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

		this._availableStyles = availableStyles || { //All available styles. Confines needed only in lobby ProfileAvatar, here we can use all of them
			borders: [0,1,2,3,4],
			heroes: [0,1,2,3,4],
			backs: [0,1,2,3,4,5]
		};

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

	get _userStyleDefined()
	{
		return Boolean(this._userStyles.border !== null && this._userStyles.hero !== null && this._userStyles.back !== null);
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

	destroy()
	{
		this._availableStyles = null;
		this._userStyles = null;
		this._currentStyles = null;

		this._borderTextures = null;
		this._heroTextures = null;
		this._backTextures = null;

		super.destroy();
	}
}

export default ProfileAvatar;