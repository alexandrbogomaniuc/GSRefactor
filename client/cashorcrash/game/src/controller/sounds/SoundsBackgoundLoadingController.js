import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';

import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameApp from '../../CrashAPP';
import SyncQueue from '../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/resources/loaders/SyncQueue';

import ASSETS from '../../config/assets.json';
import PRELOADER_ASSETS from '../../config/preloader_assets.json';
import SoundSettingsController from './SoundSettingsController';

class SoundsBackgoundLoadingController extends SimpleController
{
	//IL CONSTRUCTION...
	constructor()
	{
		super();
		
		this._soundsQueue = null;
		
		this._initUSoundsBackgoundLoadingController();
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...
	//...IL INTERFACE

	//ILI INIT...
	_initUSoundsBackgoundLoadingController()
	{
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.soundSettingsController.on(SoundSettingsController.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);
	}
	//...ILI INIT

	_onSoundSettingsChanged(event)
	{
		if (event.muted !== undefined && !event.muted)
		{
			APP.soundSettingsController.off(SoundSettingsController.EVENT_ON_SOUND_SETTINGS_CHANGED, this._onSoundSettingsChanged, this);

			this._startSoundsBackgroundLoading();
		}
	}

	_startSoundsBackgroundLoading()
	{
		APP.soundsLoadingInitiated = true;
		
		let queue = this._soundsQueue = new SyncQueue();
		queue.concurrency = 1;

		let preloaderSoundsQueue = APP.soundsController.createLoaderQueue(PRELOADER_ASSETS.sounds);
		preloaderSoundsQueue.concurrency = 1;
		preloaderSoundsQueue.once('error', this._onPreloaderSoundsAssetsLoadingError, this);
		preloaderSoundsQueue.once('complete', this._onPreloaderSoundsAssetsLoaded, this);

		let gameSoundsQueue = APP.soundsController.createLoaderQueue(ASSETS.sounds);
		gameSoundsQueue.concurrency = 1;

		queue.add (
					preloaderSoundsQueue,
					gameSoundsQueue
				);

		queue.once('error', this._onSoundsBgLoadingError, this);
		queue.load();
	}

	_onPreloaderSoundsAssetsLoadingError(e)
	{
		APP.handleAssetsLoadingError(e.key, e.message);
	}

	_onPreloaderSoundsAssetsLoaded(e)
	{
		APP.playPreloaderSounds();
	}

	_onSoundsBgLoadingError(e)
	{
		this._soundsQueue.stopLoading();
		APP.handleAssetsLoadingError(e.key, e.message);
	}
}

export default SoundsBackgoundLoadingController;