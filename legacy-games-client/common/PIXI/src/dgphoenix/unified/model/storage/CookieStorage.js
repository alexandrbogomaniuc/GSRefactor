/**
 * Cookie data storage.
 * @class
 */
class CookieStorage
{
	constructor(key)
	{
		this._key = key;
	}

	/** Get data from cookie. */
	load(callback = null)
	{
		let cookie = this._getCookie(this._key);

		if (callback instanceof Function)
		{
			callback(cookie);
		}

		return cookie;
	}

	/** Save data to cookie. */
	save(value, callback = null, expiresDate = null, path = "/", domainName = document.domain, secure = false)
	{
		this._setCookie(this._key, value, expiresDate, path, domainName, secure);

		if (callback instanceof Function)
		{
			callback();
		}
	}

	_getCookie(key)
	{
		return decodeURIComponent(document.cookie.replace(new RegExp("(?:(?:^|.*;)\\s*" + encodeURIComponent(key).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=\\s*([^;]*).*$)|^.*$"), "$1")) || null;
	}

	_setCookie(key, value, expiresDate, path, domainName, secure)
	{
		if (!key || /^(?:expires|max\-age|path|domain|secure)$/i.test(key)) { return false; }
		var expires = "";

		if (expiresDate)
		{
			switch (expiresDate.constructor)
			{
				case Number:
					expires = expiresDate === Infinity ? "; expires=Fri, 31 Dec 9999 23:59:59 GMT" : "; max-age=" + expiresDate;
				break;
				case String:
					expires = "; expires=" + expiresDate;
				break;
				case Date:
					expires = "; expires=" + expiresDate.toUTCString();
				break;
			}
		}

		var cookie = encodeURIComponent(key) + "=" + encodeURIComponent(value) + expires + (domainName ? "; domain=" + domainName : "") + (path ? "; path=" + path : "") + (secure ? "; secure" : "");
		document.cookie = cookie;

		return true;
	}
}

export default CookieStorage;