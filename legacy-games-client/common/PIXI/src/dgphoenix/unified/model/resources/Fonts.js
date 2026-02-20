import Queue from '../../controller/interaction/resources/loaders/Queue';
import { createLoader } from '../../controller/interaction/resources/loaders';
import { APP } from '../../controller/main/globals';

/**
 * @class
 * @classdesc Custom fonts class
 */

class Fonts
{
	constructor()
	{
		this.items = new Map();
	}

	destructor()
	{
		this.items = null;
	}

	/**
	 *
	 * @param assets
	 * @returns {Queue}
	 */
	createLoaderQueue(assets)
	{
		let path = APP.contentPathURLsProvider.notLocalizedFontsPath;

		let queue = new Queue();
		let self = this;

		for (let asset of assets)
		{

			let src = asset.src;

			if (Array.isArray(src))
			{
				for (let i = 0; i < src.length; i++)
				{
					src[i] = `${path}/${src[i]}`;
				}
			}
			else
			{
				src = `${path}/${src}`;
			}

			let loader = createLoader(src);
			loader.name = asset.name || null;
			queue.add(loader);
		}

		queue.on("fileload", (e)=> {
			self.onFontLoaded(e.item);
		});

		return queue;
	}

	onFontLoaded(item)
	{
		this.addFont(item);
	}
	
	/** Adds new font if it does not exist. */
	addFont(item)
	{
		if (!!this.getFont(item.name))
		{
			return;
		}

		this.items.set(item.name, item.font);
	}

	/**
	 * Checks if font has required glyphs.
	 * @param {string} fontFamily - Registered font name.
	 * @param {string} str - Glyphs to check.
	 * @returns {boolean}
	 */
	isGlyphsSupported(fontFamily, str)
	{
		var font = this.getFont(fontFamily);
		return Boolean((str && font.hasGlyphs(str)));
	}

	/**
	 * Gets font by registerd name.
	 * @param {string} fontFamily 
	 * @returns {Font}
	 */
	getFont(fontFamily)
	{
		return this.items.get(fontFamily);
	}
}

export default Fonts;
