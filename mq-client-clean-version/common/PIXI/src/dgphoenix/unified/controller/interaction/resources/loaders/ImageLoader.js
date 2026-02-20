import QueueItem from './QueueItem';
import { CACHE } from '../../../main/globals';

/**
 * @class
 * @inheritDoc
 * @classdesc Image loader
 */
class ImageLoader extends QueueItem {

	prepareData(src){
		return null;
	}

	prepareKey(src){
		return src; 
	}

	load(cache = true)
	{
		let src = this.key;
		let img = CACHE.get(src);
		let baseTexture;

		if (!img || !img.complete)
		{
			this.inProgress = true;
			baseTexture = PIXI.BaseTexture.from(this.generateAbsoluteURL(src));
			img = baseTexture.resource.source;
			CACHE.put(this.key, img);
		}
		this.data = img;

		let completeAttemptTimeout = null;
		let self = this;
		let completeLoadingIfPossible = function() 
		{
			if (self.data.complete && (!baseTexture || baseTexture.valid)) 
			{
				clearTimeout(completeAttemptTimeout);
				self.data.onerror = null;
				self._status = 'success';
				baseTexture = null;
				
				self.completeLoad(cache);
			}
			else
			{
				checkCompleteAfterTimeout();
			}
		};

		let checkCompleteAfterTimeout = function()
		{
			completeAttemptTimeout = setTimeout(completeLoadingIfPossible, 100); // XXX: onload, onerror not fired on all browsers :(
		}

		if (!this.data.complete)
		{
			// try completeAttemptTimeout handle errors
			this.data.onerror = function()
			{
				clearTimeout(completeAttemptTimeout);
				self.data.onerror = null;
				self._status = 'error';
				baseTexture = null;

				self.completeLoad(cache);
			};
		}

		checkCompleteAfterTimeout();
	}

	completeLoad(cache = true)
	{
		super.completeLoad(cache);
	}
}

export default ImageLoader;