import XMLLoader from './XMLLoader';
import { GET } from '../../server/ajax';

/**
 * @class
 * @inheritDoc
 * @classdesc customerspec_descriptor xml loader
 */
class CustomerspecLoader extends XMLLoader
{
	constructor(src)
	{
		super(src);
	}

	load(cache = true)
	{
		if (cache) {
			let data = this.cached();
			if (data) {
				this.data = data;
				this.completeLoad(cache);
			}
		}

		this.inProgress = true;

		if (!this.complete)
		{
			let self = this;
			GET(
					this.key,
					null,
					function (data, status, xhr, errorMessage)
					{
						self._status = status;
						if (status == 'error')
						{
							self._statusMessage = errorMessage || (xhr && xhr.statusText) || 'Uknown AJAX error';
						}
						self.data = data;
						self.completeLoad(cache);
					}, 
					this.dataType
				);
		}
	}
}

export default CustomerspecLoader;