/**
 * Set of easing functions
 * @module easing
 */

/**
 * @callback easingCallback
 * @param {Number} t - the current time, between 0 and duration inclusive.
 * @param {Number} b - the initial value of the animation property.
 * @param {Number} c - the total change in the animation property.
 * @param {Number} d - the duration of the motion.
 */

import * as back from './back';
import * as bounce from './bounce';
import * as circular from './circular';
import * as cubic from './cubic';
import * as elastic from './elastic';
import * as exponential from './exponential';
import * as linear from './linear';
import * as quadratic from './quadratic';
import * as quartic from './quartic';
import * as quintic from './quintic';
import * as sine from './sine';
import * as smoothstep from './smoothstep';

export {
	back,
	bounce,
	circular,
	cubic,
	elastic,
	exponential,
	linear,
	quadratic,
	quartic,
	quintic,
	sine,
	smoothstep
};
