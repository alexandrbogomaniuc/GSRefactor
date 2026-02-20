import SimpleXMLParser from '../../xml/SimpleXMLParser';
import TASpriteDescriptor from '../data/TASpriteDescriptor';

/**
 * Translatable asset sprite descriptor parser.
 * @class
 */
class TASpriteDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} spriteDescriptorElement - XML element
	 */
	constructor(spriteDescriptorElement)
	{
		this._spriteDescriptor = null;

		this._initTASpriteDescriptorParser(spriteDescriptorElement);
	}

	/**
	 * @type {TASpriteDescriptor}
	 */
	get spriteDescriptor()
	{
		return this._spriteDescriptor;
	}

	_initTASpriteDescriptorParser(spriteDescriptorElement)
	{
		if (spriteDescriptorElement)
		{
			this._parse(spriteDescriptorElement);
		}
	}

	_parse(spriteDescriptorElement)
	{
		var framesPath = SimpleXMLParser.getAttributeValue(spriteDescriptorElement, "framesPath", true);
		var framerate = SimpleXMLParser.getAttributeValue(spriteDescriptorElement, "framerate", true);

		var atlasConfigDescriptorElement = SimpleXMLParser.getTag(spriteDescriptorElement, "atlas_descriptor", 0, true);
		var atlasConfigUrl = SimpleXMLParser.getAttributeValue(atlasConfigDescriptorElement, "url", true);
		
		var spriteImagesUrls = [];
		var lSpriteSheetDescriptors_e_arr = SimpleXMLParser.getTags(spriteDescriptorElement, "spritesheet");
		for (var i = 0; i < lSpriteSheetDescriptors_e_arr.length; i++)
		{
			var lSpriteSheetDescriptor_e = lSpriteSheetDescriptors_e_arr[i];			
			var lImageUrl_str = SimpleXMLParser.getAttributeValue(lSpriteSheetDescriptor_e, "url", true);

			spriteImagesUrls.push(lImageUrl_str);
		}

		this._spriteDescriptor = new TASpriteDescriptor(atlasConfigUrl, spriteImagesUrls, framesPath, framerate);
	}
}

export default TASpriteDescriptorParser