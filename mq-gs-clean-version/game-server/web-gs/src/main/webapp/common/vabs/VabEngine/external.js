if (!Array.prototype.filter)
{
    Array.prototype.filter = function(fun/*, thisArg*/)
    {
        'use strict';

        if (this === void 0 || this === null) {
            throw new TypeError();
        }

        var t = Object(this);
        var len = t.length >>> 0;
        if (typeof fun !== 'function')
        {
            throw new TypeError();
        }

        var res = [];
        var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
        for (var i = 0; i < len; i++)
        {
            if (i in t)
            {
                var val = t[i];
                if (fun.call(thisArg, val, i, t))
                {
                    res.push(val);
                }
            }
        }

        return res;
  };
}

String.prototype.replaceFirst = String.prototype.replace;

String.prototype.replace = function( token, newToken, ignoreCase )
{
    var _token;
    var str = this + "";
    var i = -1;

    if ( typeof token === "string" )
    {
        if ( ignoreCase )
        {
            _token = token.toLowerCase();
            while(( i = str.toLowerCase().indexOf(token, i >= 0 ? i + newToken.length : 0) ) !== -1)
            {
                str = str.substring( 0, i ) + newToken + str.substring(i + token.length);
            }
        }
        else
        {
            return this.split(token).join(newToken);
        }
    }
    return str;
};

function isJsonString(str)
{
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
}

function getRandom(min, max)
{
    return Math.round(min + Math.random()*(max-min));
}

function setColor(r, g, b, a)
{
    return 'rgba('+r+', '+g+', '+b+', '+a+')'
}

function tryParseInt(str)
{
    var retValue = null;
    if(str !== null)
    {
        if(str.length > 0)
        {
            if (!isNaN(str))
            {
                retValue = parseInt(str, 10);
            }
        }
    }
    return retValue;
}

if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
}

function getScrollBarWidth ()
{
    var inner = document.createElement('p');
    inner.style.width = "100%";
    inner.style.height = "200px";

    var outer = document.createElement('div');
    outer.style.position = "absolute";
    outer.style.top = "0px";
    outer.style.left = "0px";
    outer.style.visibility = "hidden";
    outer.style.width = "200px";
    outer.style.height = "150px";
    outer.style.overflow = "hidden";
    outer.appendChild (inner);

    document.body.appendChild (outer);
    var w1 = inner.offsetWidth;
    outer.style.overflow = 'scroll';
    var w2 = inner.offsetWidth;
    if (w1 == w2) w2 = outer.clientWidth;
    document.body.removeChild (outer);
    return (w1 - w2);
}