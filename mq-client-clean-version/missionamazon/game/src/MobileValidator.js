class MobileValidator
{
	constructor()
	{
		this.isMobile = false;
		this.userAgent = window.navigator.userAgent.toLowerCase();
	}

	ios()
	{
		return this.iphone() || this.ipod() || this.ipad();
	}

	iphone()
	{
		return !this.windows() && this._find('iphone');
	}

	ipad()
	{
		return this._find('ipad');
	}

	ipod()
	{
		return this._find('ipod');
	}

	android()
	{
		return !this.windows() && this._find('android');
	};

	androidPhone()
	{
		return this.android() && this._find('mobile');
	};

	androidTablet()
	{
		return this.android() && !this._find('mobile');
	};

	blackberry()
	{
		return this._find('blackberry') || this._find('bb10') || this._find('rim');
	};

	blackberryPhone()
	{
		return this.blackberry() && !this._find('tablet');
	};

	blackberryTablet()
	{
		return this.blackberry() && this._find('tablet');
	};

	windows()
	{
		return this._find('windows');
	};

	windowsPhone()
	{
		return this.windows() && this._find('phone');
	};

	windowsTablet()
	{
		return this.windows() && (this._find('touch') && !this.windowsPhone());
	};

	fxos()
	{
		return (this._find('(mobile;') || this._find('(tablet;')) && this._find('; rv:');
	};

	fxosPhone()
	{
		return this.fxos() && this._find('mobile');
	};

	fxosTablet()
	{
		return this.fxos() && this._find('tablet');
	};

	meego()
	{
		return this._find('meego');
	};

	mobile()
	{
		return this.androidPhone() || this.iphone() || this.windowsPhone() || this.blackberryPhone() || this.fxosPhone() || this.meego();
	};

	iPad13()
	{
		return navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 0;
	};

	tablet()
	{
		return this.ipad() || this.androidTablet() || this.blackberryTablet() || this.windowsTablet() || this.fxosTablet() || this.iPad13();
	};

	desktop()
	{
		return !this.tablet() && !this.mobile();
	};

	_find(needle)
	{
		return this.userAgent.indexOf(needle) !== -1;
	};
}

export default MobileValidator;
