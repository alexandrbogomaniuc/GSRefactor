import init from './main.js';

import {default as ExternalAPI} from './external/ApplicationAPI';

//----------------------------------------------------------------------------------------------------------------------
function environmentReady() {
	window.removeEventListener('load', environmentReady);
	init(ExternalAPI, {});
}

//----------------------------------------------------------------------------------------------------------------------
if (!!window["gameEnvReady"])
{
	environmentReady();
}
else
{
	window.addEventListener('load', environmentReady);
}