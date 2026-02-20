import Sprite from './Sprite';

/**
 * Text written by bitmap font.
 * @class
 */
class BitmapText extends Sprite
{
	/**
	 * Text.
	 * @type {string}
	 */
	get text()
	{
		return this._fText_str;
	}

	/**
	 * @constructor
	 * @param {Map} textures - Map of glyphs textures
	 * @param {string} [text=""] - Text value.
	 * @param {number} [letterSpace=0] - Letter space.
	 */
	constructor(textures, text = "", letterSpace = 0)
	{
		super();

		this.lettersTextures = textures;
		this.bitmapText = null;;
		this.textWidth = 0;
		this.letterSpace = letterSpace;

		this._lettersTint = undefined;
		this._lettersBlendMode = undefined;

		this._fText_str = text;

		this.write(text);
	}

	/**
	 * Update text.
	 * @param {string} text 
	 */
	write(text = "")
	{
		this.clear();
		this.bitmapText = this.addChild(new Sprite());

		for(let i = 0; i < text.length; ++i)
		{
			this.addLetter(text[i]);
		}

		this._applyTint();
		this._applyBlendMode();

		this._fText_str = text;
	}

	/**
	 * Apply tint to text.
	 * @param {number} color_num - Tint color.
	 */
	addTint(color_num)
	{
		this._lettersTint = color_num;

		this._applyTint();
		this._applyBlendMode();
	}

	/**
	 * Apply blend mode to text.
	 * @param {string} blendMode_str - Blend mode.
	 */
	addBlendMode(blendMode_str)
	{
		this._lettersBlendMode = blendMode_str;

		this._applyBlendMode();
	}

	/**
	 * Draw glyph.
	 * @param {string} letter 
	 */
	addLetter(letter)
	{
		let lTexture_t = this.lettersTextures[letter];

		if (lTexture_t)
		{
			let renderedLettersAmount = this.bitmapText.children && this.bitmapText.children.length || 0;
			let prevLetter = renderedLettersAmount ? this.bitmapText.getChildAt(renderedLettersAmount-1) : null;
			var bitmapLetter = this.bitmapText.addChild(new Sprite());

			bitmapLetter.textures = [lTexture_t];
			bitmapLetter.gotoAndStop(0);

			if (lTexture_t._pivot)
			{
				bitmapLetter.pivot.set(lTexture_t._pivot.x, lTexture_t._pivot.y);
			}

			let letterX = lTexture_t.width/2;
			if (prevLetter)
			{
				letterX += prevLetter.position.x + prevLetter.width/2 + this.letterSpace;
				this.textWidth += this.letterSpace;
			}
			bitmapLetter.position.x = letterX;
			this.textWidth += lTexture_t.width;
		}
	}

	/** Destroy bitmap text instance. */
	destroy()
	{
		super.destroy();

		this.lettersTextures = null;
		this.textWidth = null;
		this.letterSpace = null;
		this.bitmapText = null;

		this._lettersTint = undefined;
		this._lettersBlendMode = undefined;
	}

	/** Clear text view. */
	clear()
	{
		if (this.bitmapText)
		{
			this.bitmapText.destroy();
			this.bitmapText = null;
		}
		
		this.textWidth = 0;
	}

	_applyTint()
	{
		if (this._lettersTint === undefined || !this.bitmapText)
		{
			return;
		}

		let renderedLettersAmount = this.bitmapText.children && this.bitmapText.children.length || 0;
		for (let i=0; i<renderedLettersAmount; i++)
		{
			let letter = this.bitmapText.getChildAt(i);
			letter.tint = this._lettersTint;
		}
	}

	_applyBlendMode()
	{
		if (this._lettersBlendMode === undefined || !this.bitmapText)
		{
			return;
		}

		let renderedLettersAmount = this.bitmapText.children && this.bitmapText.children.length || 0;
		for (let i=0; i<renderedLettersAmount; i++)
		{
			let letter = this.bitmapText.getChildAt(i);
			letter.blendMode = this._lettersBlendMode;
		}
	}
}

export default BitmapText;