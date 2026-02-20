import QueueItem from './QueueItem';
import AJAXLoader from './AJAXLoader';
import opentype from '../../../../lib/fonts/opentype';
import { EDGE, IE } from '../../../../view/layout/features';

const MAX_REGISTER_DURATION = 1000;
/**
 * @class
 * @inheritDoc
 * @classdesc Loader for custom fonts
 */
class FontLoader extends QueueItem
{
	prepareData(src)
	{
		this._sources = this.prepareSource(src);
	}

	prepareSource(src)
	{
		if (src instanceof Array)
		{
			return src;
		}
		return src ? [src] : [];
	}

	prepareKey(src)
	{
		return src;
	}

	load(cache = true)
	{
		super.load(cache);
		
		if (!this.complete)
		{
			if (this._sources.length)
			{
				let src = this._sources.shift();
				let self = this;
				let loader = new AJAXLoader(src, "arraybuffer");

				loader.once("success", (e) => {
					if (!e.target.data)
					{
						return self.load(cache);
					}
					self.data = e.target.data;
					self.key = e.target.key;
					self._sources = [e.target.key];
					self._cache = cache;
					self._configureFont();
				});

				loader.once("error", (e) => {
					self._statusMessage = self._statusMessage || "";
					self._statusMessage += `${e.message}\n`;
					self._status = 'error';
					self.completeLoad(cache);
				});

				loader.load(cache);
			}
			else
			{
				this._status = "error";
				this._statusMessage = "All provided sources are invalid!";
				this.completeLoad(cache);
			}
		}
	}

	_configureFont()
	{
		var buffer = this.data;
		this.font = opentype.parse(buffer);
		

		var base64 = this.arrayBufferToBase64(buffer);
		var fileExtension = this.getExtension(this.key).toLowerCase();

		var fontType;
		if (fileExtension === "otf")
		{
			fontType = "opentype";
		}
		else if (fileExtension === "ttf")
		{
			fontType = "truetype";
		}
		else
		{
			throw new Error(`Unsupported font type: "${fileExtension}"`);
		}

		var url = "data:font/" + fontType + ";base64," + base64;
		this.initializeCSSFont(url);
	}

	/**
	 * Register font
	 * @param {String} url 
	 */
	initializeCSSFont(url)
	{
		if (IE || EDGE)
		{
			var htmlElement = document.documentElement;
			var langAttribute = "lang";
			if (!htmlElement.hasAttribute(langAttribute))
			{
				htmlElement.setAttribute(langAttribute, "");
			}
		}

		var span = this._ruleSpan = document.createElement("span");
		var spanStyle = span.style;
		spanStyle.fontFamily = this.name;
		spanStyle.fontSize = "32px";
		spanStyle.visibility = "hidden";
		span.innerHTML = "BESbswy";
		document.body.appendChild(span);
		
		if (!this._startRuleWidth)
		{
			this._startRuleWidth = span.offsetWidth;
		}

		this._fontRegistrationStartTime_int = Date.now();

		this._checkFontRegistration();
		
		this.insertFontIntoStylesheet(url);
	}

	_checkFontRegistration()
	{
		this._clearCheckAttemptTimeout();

		let lCurFontRegistrationDuration_int = Date.now() - this._fontRegistrationStartTime_int;
		if (this._startRuleWidth == this._ruleSpan.offsetWidth && lCurFontRegistrationDuration_int < MAX_REGISTER_DURATION)
		{
			this._checkAttemptTimeout = setTimeout(this._checkFontRegistration.bind(this), 50);
		}
		else
		{
			this._onFontConfigured();
		}
	}

	_clearCheckAttemptTimeout()
	{
		clearTimeout(this._checkAttemptTimeout);
	}

	insertFontIntoStylesheet(url)
	{
		var style = document.createElement("style");
		document.head.appendChild(style);

		var appendedStyle = document.styleSheets[document.styleSheets.length - 1];
		var cssRules = appendedStyle.cssRules;
		var cssRule = "@font-face {font-family: [FONT_NAME_PLACEHOLDER]; src: url([FONT_URL_PLACEHOLDER]); }"
						.replace("[FONT_NAME_PLACEHOLDER]", this.name)
						.replace("[FONT_URL_PLACEHOLDER]", url);

		appendedStyle.insertRule(cssRule, cssRules.length);

		return style;
	}

	arrayBufferToBase64(buffer)
	{
		var str = this.arrayBufferToString(buffer);
		return window.btoa(str);
	}

	arrayBufferToString(buf) 
	{
		var str = "";
		var bytes = new Uint8Array(buf)
		for (var i = 0; i < bytes.byteLength; i++)
		{
			str += String.fromCharCode(bytes[i])
		}
		return str;
	}

	stringToArrayBuffer(str) 
	{
		var buf = new ArrayBuffer(str.length);
		var bufView = new Uint8Array(buf);
		for (var i=0, strLen=str.length; i < strLen; i++) 
		{
			bufView[i] = str.charCodeAt(i);
		}
		return buf;
	}

	getExtension(url)
	{
		var delimiterIndex = url.lastIndexOf(".");
		if (delimiterIndex < 0)
		{
			return null;
		}
		return url.substr(delimiterIndex + 1);
	}

	_onFontConfigured()
	{
		this._ruleSpan.innerHTML = "";

		this._statusMessage = "OK";
		this.completeLoad(this._cache);
	}

	completeLoad(cache = true)
	{
		this._clearCheckAttemptTimeout();

		super.completeLoad(cache);
	}
}

export default FontLoader;