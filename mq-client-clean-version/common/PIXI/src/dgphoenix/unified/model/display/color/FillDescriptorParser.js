import SimpleXMLParser from '../../xml/SimpleXMLParser';
import LGFillDescriptorParser from './gradient/LGFillDescriptorParser';

/**
 * Parser of fill descriptor (XML element).
 * @class
 * @example
 * Fill descriptor XML element:
 * <fill type="linear_gradient" repeat="lines"> ... </fill>
 */
class FillDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} fillDescriptorElement - Fill XML element.
	 */
	constructor(fillDescriptorElement)
	{
		this._fillDescriptor = null;

		this._initFillDescriptorParser(fillDescriptorElement);
	}

	/** Fill descriptor instance. */
	get fillDescriptor()
	{
		return this._fillDescriptor;
	}

	_initFillDescriptorParser(fillDescriptorElement)
	{
		if (fillDescriptorElement)
		{
			this._parse(fillDescriptorElement);
		}
	}

	_parse(fillDescriptorElement)
	{
		let lFillType_str = SimpleXMLParser.getAttributeValue(fillDescriptorElement, "type", true);
		let linearGradientDescriptor = null;

		if (lFillType_str === "linear_gradient")
		{
			linearGradientDescriptor = new LGFillDescriptorParser(fillDescriptorElement).lgFillDescriptor;
		}
		else
		{
			throw new Error(`Fill Type is not supported: '${lFillType_str}'; `);
		}

		this._fillDescriptor = linearGradientDescriptor;
	}
}

export default FillDescriptorParser;