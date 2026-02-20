import SimpleXMLParser from '../../xml/SimpleXMLParser';
import RectDescriptorParser from '../geom/RectDescriptorParser';
import AlignDescriptorParser from '../align/AlignDescriptorParser';
import ScaleDescriptorParser from '../scale/ScaleDescriptorParser';
import AreaInnerContentDescriptor from './AreaInnerContentDescriptor';

/**
 * Parser of area content descriptor (XML format).
 * Describes a way of content placement inside area (bounds rectangle, scale, align).
 * @class
 * @example
 * Area element example:
 * <area x="-256" y="-32" w="512" h="64">
 * 	<scale x="auto" y="auto" distortion="false"/>
 * 	<align h="center" v="middle"/>
 * </area>
 */
class AreaInnerContentDescriptorParser
{
	constructor(areaInnerContentDescriptor)
	{
		this._areaInnerContentDescriptor = null;

		this._initAreaInnerContentDescriptorParser(areaInnerContentDescriptor);
	}

	/**
	 * Area content descriptor instance.
	 * @type {AreaInnerContentDescriptor}
	 */
	get areaInnerContentDescriptor()
	{
		return this._areaInnerContentDescriptor;
	}

	_initAreaInnerContentDescriptorParser(areaInnerContentDescriptor)
	{
		if (areaInnerContentDescriptor)
		{
			this._parse(areaInnerContentDescriptor);
		}
	}

	_parse(areaInnerContentDescriptor)
	{
		var lArea_ur = new RectDescriptorParser(areaInnerContentDescriptor).rectDescriptor;
		var lAlignDescriptor_uad = new AlignDescriptorParser(SimpleXMLParser.getTag(areaInnerContentDescriptor.childNodes, "align", 0, true)).alignDescriptor;
		var lScaleDescriptor_usd = new ScaleDescriptorParser(SimpleXMLParser.getTag(areaInnerContentDescriptor.childNodes, "scale", 0, true)).scaleDescriptor;
		
		this._areaInnerContentDescriptor = new AreaInnerContentDescriptor(lArea_ur, lAlignDescriptor_uad, lScaleDescriptor_usd);
	}

}

export default AreaInnerContentDescriptorParser;