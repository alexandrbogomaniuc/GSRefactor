import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import AtlasConfig from './../../../../config/AtlasConfig';
import { AtlasSprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';

const COINS_CONFIG = [
	{
		posStart: {x: (1547 - 1547) / 2, y: (842 - 842) / 2}, posEnd: {x: (2719 - 1547) / 2, y: (438 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.73,
		rotationStart: Utils.gradToRad(29), rotationEnd: Utils.gradToRad(53),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 0*FRAME_RATE
	},
	{
		posStart: {x: (1551 - 1547) / 2, y: (856 - 842) / 2}, posEnd: {x: (365 - 1547) / 2, y: (1068 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.6,
		rotationStart: Utils.gradToRad(22), rotationEnd: Utils.gradToRad(77),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 2*FRAME_RATE
	},
	{
		posStart: {x: (1551 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (2727 - 1547) / 2, y: (1348 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.74,
		rotationStart: Utils.gradToRad(23), rotationEnd: Utils.gradToRad(95),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 1*FRAME_RATE
	},
	{
		posStart: {x: (1451 - 1547) / 2, y: (858 - 842) / 2}, posEnd: {x: (547 - 1547) / 2, y: (514 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.58,
		rotationStart: Utils.gradToRad(32), rotationEnd: Utils.gradToRad(1),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 4*FRAME_RATE
	},
	{
		posStart: {x: (1451 - 1547) / 2, y: (832 - 842) / 2}, posEnd: {x: (2669 - 1547) / 2, y: (568 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.76,
		rotationStart: Utils.gradToRad(20), rotationEnd: Utils.gradToRad(-1),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 3*FRAME_RATE
	},
	{
		posStart: {x: (1451 - 1547) / 2, y: (866 - 842) / 2}, posEnd: {x: (513 - 1547) / 2, y: (818 - 842) / 2},
		scaleStart: 0.62, scaleEnd: 1.55,
		rotationStart: Utils.gradToRad(-21), rotationEnd: Utils.gradToRad(-38),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 1*FRAME_RATE
	},
	{
		posStart: {x: (1731 - 1547) / 2, y: (821 - 842) / 2}, posEnd: {x: (951 - 1547) / 2, y: (1562 - 842) / 2},
		scaleStart: 0.78, scaleEnd: 1.63,
		rotationStart: Utils.gradToRad(12), rotationEnd: Utils.gradToRad(20),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 3*FRAME_RATE
	},
	{
		posStart: {x: (1716 - 1547) / 2, y: (820 - 842) / 2}, posEnd: {x: (2137 - 1547) / 2, y: (1556 - 842) / 2},
		scaleStart: 0.82, scaleEnd: 1.43,
		rotationStart: Utils.gradToRad(13), rotationEnd: Utils.gradToRad(-36),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 4*FRAME_RATE
	},
	{
		posStart: {x: (1731 - 1547) / 2, y: (831 - 842) / 2}, posEnd: {x: (613 - 1547) / 2, y: (1542 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 2,
		rotationStart: Utils.gradToRad(-16), rotationEnd: Utils.gradToRad(147),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 2*FRAME_RATE
	},
	{
		posStart: {x: (1744 - 1547) / 2, y: (856 - 842) / 2}, posEnd: {x: (869 - 1547) / 2, y: (236 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.56,
		rotationStart: Utils.gradToRad(16), rotationEnd: Utils.gradToRad(-53),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 0*FRAME_RATE
	},
	{
		posStart: {x: (1517 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (2413 - 1547) / 2, y: (260 - 842) / 2},
		scaleStart: 0.76, scaleEnd: 1.71,
		rotationStart: Utils.gradToRad(34), rotationEnd: Utils.gradToRad(-29),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 3*FRAME_RATE
	},
	{
		posStart: {x: (1513 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (517 - 1547) / 2, y: (1178 - 842) / 2},
		scaleStart: 0.59, scaleEnd: 1.13,
		rotationStart: Utils.gradToRad(-19), rotationEnd: Utils.gradToRad(27),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 1*FRAME_RATE
	},
	{
		posStart: {x: (1531 - 1547) / 2, y: (871 - 842) / 2}, posEnd: {x: (2475 - 1547) / 2, y: (1634 - 842) / 2},
		scaleStart: 0.8, scaleEnd: 1.71,
		rotationStart: Utils.gradToRad(-23), rotationEnd: Utils.gradToRad(20),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 2*FRAME_RATE
	},
	{
		posStart: {x: (1502 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (2673 - 1547) / 2, y: (1506 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.29,
		rotationStart: Utils.gradToRad(-18), rotationEnd: Utils.gradToRad(147),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 3*FRAME_RATE
	},
	{
		posStart: {x: (1542 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (2273 - 1547) / 2, y: (144 - 842) / 2},
		scaleStart: 0.79, scaleEnd: 1.5,
		rotationStart: Utils.gradToRad(19), rotationEnd: Utils.gradToRad(-53),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 5*FRAME_RATE
	},
	{
		posStart: {x: (1483 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (2717 - 1547) / 2, y: (838 - 842) / 2},
		scaleStart: 0.62, scaleEnd: 2,
		rotationStart: Utils.gradToRad(-16), rotationEnd: Utils.gradToRad(27),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 0*FRAME_RATE
	},
	{
		posStart: {x: (1485 - 1547) / 2, y: (856 - 842) / 2}, posEnd: {x: (2687 - 1547) / 2, y: (1118 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.49,
		rotationStart: Utils.gradToRad(-21), rotationEnd: Utils.gradToRad(53),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 4*FRAME_RATE
	},
	{
		posStart: {x: (1530 - 1547) / 2, y: (864 - 842) / 2}, posEnd: {x: (361 - 1547) / 2, y: (1408 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.27,
		rotationStart: Utils.gradToRad(48), rotationEnd: Utils.gradToRad(-77),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 2*FRAME_RATE
	},
	{
		posStart: {x: (1493 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (1911 - 1547) / 2, y: (1652 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.37,
		rotationStart: Utils.gradToRad(14), rotationEnd: Utils.gradToRad(95),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 4*FRAME_RATE
	},
	{
		posStart: {x: (1533 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (1167 - 1547) / 2, y: (1710 - 842) / 2},
		scaleStart: 0.83, scaleEnd: 1.83,
		rotationStart: Utils.gradToRad(-16), rotationEnd: Utils.gradToRad(1),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 3*FRAME_RATE
	},
	{
		posStart: {x: (1591 - 1547) / 2, y: (870 - 842) / 2}, posEnd: {x: (1801 - 1547) / 2, y: (1752 - 842) / 2},
		scaleStart: 0.79, scaleEnd: 1.86,
		rotationStart: Utils.gradToRad(21), rotationEnd: Utils.gradToRad(-1),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 2*FRAME_RATE
	},
	{
		posStart: {x: (1581 - 1547) / 2, y: (846 - 842) / 2}, posEnd: {x: (1281 - 1547) / 2, y: (1686 - 842) / 2},
		scaleStart: 0.83, scaleEnd: 2,
		rotationStart: Utils.gradToRad(27), rotationEnd: Utils.gradToRad(-38),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 5*FRAME_RATE
	},
	{
		posStart: {x: (1609 - 1547) / 2, y: (860 - 842) / 2}, posEnd: {x: (1167 - 1547) / 2, y: (154 - 842) / 2},
		scaleStart: 0.89, scaleEnd: 1.74,
		rotationStart: Utils.gradToRad(-14), rotationEnd: Utils.gradToRad(20),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 6*FRAME_RATE
	},
	{
		posStart: {x: (1578 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (2001 - 1547) / 2, y: (152 - 842) / 2},
		scaleStart: 0.56, scaleEnd: 1.3,
		rotationStart: Utils.gradToRad(-13), rotationEnd: Utils.gradToRad(-36),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 3*FRAME_RATE
	},
	{
		posStart: {x: (1581 - 1547) / 2, y: (826 - 842) / 2}, posEnd: {x: (1689 - 1547) / 2, y: (1606 - 842) / 2},
		scaleStart: 0.89, scaleEnd: 2,
		rotationStart: Utils.gradToRad(22), rotationEnd: Utils.gradToRad(147),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 6*FRAME_RATE
	},
	{
		posStart: {x: (1610 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (1521 - 1547) / 2, y: (136 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.16,
		rotationStart: Utils.gradToRad(-14), rotationEnd: Utils.gradToRad(-33),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 5*FRAME_RATE
	},
	{
		posStart: {x: (1572 - 1547) / 2, y: (857 - 842) / 2}, posEnd: {x: (1865 - 1547) / 2, y: (108 - 842) / 2},
		scaleStart: 0.93, scaleEnd: 1,
		rotationStart: Utils.gradToRad(14), rotationEnd: Utils.gradToRad(-6),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 3*FRAME_RATE
	},
	{
		posStart: {x: (1579 - 1547) / 2, y: (819 - 842) / 2}, posEnd: {x: (489 - 1547) / 2, y: (210 - 842) / 2},
		scaleStart: 0.58, scaleEnd: 0.9,
		rotationStart: Utils.gradToRad(-17), rotationEnd: Utils.gradToRad(46),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 5*FRAME_RATE
	},
	{
		posStart: {x: (1573 - 1547) / 2, y: (840 - 842) / 2}, posEnd: {x: (2687 - 1547) / 2, y: (242 - 842) / 2},
		scaleStart: 0.88, scaleEnd: 1.49,
		rotationStart: Utils.gradToRad(21), rotationEnd: Utils.gradToRad(46),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 8*FRAME_RATE
	},
	{
		posStart: {x: (1605 - 1547) / 2, y: (860 - 842) / 2}, posEnd: {x: (2267 - 1547) / 2, y: (1794 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.46,
		rotationStart: Utils.gradToRad(18), rotationEnd: Utils.gradToRad(93),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 7*FRAME_RATE
	},
	{
		posStart: {x: (1595 - 1547) / 2, y: (842 - 842) / 2}, posEnd: {x: (781 - 1547) / 2, y: (1748 - 842) / 2},
		scaleStart: 0.8, scaleEnd: 0.8,
		rotationStart: Utils.gradToRad(-13), rotationEnd: Utils.gradToRad(-28),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 4*FRAME_RATE
	},
	{
		posStart: {x: (1574 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (2593 - 1547) / 2, y: (1802 - 842) / 2},
		scaleStart: 0.82, scaleEnd: 0.7,
		rotationStart: Utils.gradToRad(20), rotationEnd: Utils.gradToRad(16),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 7*FRAME_RATE
	},
	{
		posStart: {x: (1621 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (1943 - 1547) / 2, y: (1590 - 842) / 2},
		scaleStart: 0.86, scaleEnd: 1,
		rotationStart: Utils.gradToRad(22), rotationEnd: Utils.gradToRad(53),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 10*FRAME_RATE
	},
	{
		posStart: {x: (1581 - 1547) / 2, y: (834 - 842) / 2}, posEnd: {x: (1113 - 1547) / 2, y: (1564 - 842) / 2},
		scaleStart: 0.85, scaleEnd: 1.78,
		rotationStart: Utils.gradToRad(-18), rotationEnd: Utils.gradToRad(-77),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 9*FRAME_RATE
	},
	{
		posStart: {x: (1568 - 1547) / 2, y: (862 - 842) / 2}, posEnd: {x: (2299 - 1547) / 2, y: (1592 - 842) / 2},
		scaleStart: 0.86, scaleEnd: 2,
		rotationStart: Utils.gradToRad(-14), rotationEnd: Utils.gradToRad(95),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 6*FRAME_RATE
	},
	{
		posStart: {x: (1589 - 1547) / 2, y: (894 - 842) / 2}, posEnd: {x: (2737 - 1547) / 2, y: (888 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1,
		rotationStart: Utils.gradToRad(29), rotationEnd: Utils.gradToRad(-1),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 7*FRAME_RATE
	},
	{
		posStart: {x: (1577 - 1547) / 2, y: (829 - 842) / 2}, posEnd: {x: (711 - 1547) / 2, y: (214 - 842) / 2},
		scaleStart: 0.6, scaleEnd: 0.7,
		rotationStart: Utils.gradToRad(-13), rotationEnd: Utils.gradToRad(20),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 9*FRAME_RATE
	},
	{
		posStart: {x: (1603 - 1547) / 2, y: (868 - 842) / 2}, posEnd: {x: (2737 - 1547) / 2, y: (1174 - 842) / 2},
		scaleStart: 0.5, scaleEnd: 1.24,
		rotationStart: Utils.gradToRad(-12), rotationEnd: Utils.gradToRad(147),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 8*FRAME_RATE
	},
	{
		posStart: {x: (1589 - 1547) / 2, y: (868 - 842) / 2}, posEnd: {x: (469 - 1547) / 2, y: (1568 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.52,
		rotationStart: Utils.gradToRad(27), rotationEnd: Utils.gradToRad(-33),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 6*FRAME_RATE
	},
	{
		posStart: {x: (1564 - 1547) / 2, y: (863 - 842) / 2}, posEnd: {x: (1021 - 1547) / 2, y: (156 - 842) / 2},
		scaleStart: 0.8, scaleEnd: 0.91,
		rotationStart: Utils.gradToRad(-7), rotationEnd: Utils.gradToRad(-6),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 9*FRAME_RATE
	},
	{
		posStart: {x: (1603 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (823 - 1547) / 2, y: (1638 - 842) / 2},
		scaleStart: 0.57, scaleEnd: 1.27,
		rotationStart: Utils.gradToRad(27), rotationEnd: Utils.gradToRad(1),
		curvePoint: {x: 0, y: -150},
		duration: 19*FRAME_RATE, delay: 2*FRAME_RATE
	},
	{
		posStart: {x: (1576 - 1547) / 2, y: (863 - 842) / 2}, posEnd: {x: (405 - 1547) / 2, y: (722 - 842) / 2},
		scaleStart: 0.9, scaleEnd: 0.9,
		rotationStart: Utils.gradToRad(19), rotationEnd: Utils.gradToRad(-38),
		curvePoint: {x: 0, y: -150},
		duration: 19*FRAME_RATE, delay: 2*FRAME_RATE
	},
	{
		posStart: {x: (1606 - 1547) / 2, y: (863 - 842) / 2}, posEnd: {x: (2657 - 1547) / 2, y: (272 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.55,
		rotationStart: Utils.gradToRad(5), rotationEnd: Utils.gradToRad(-36),
		curvePoint: {x: 0, y: -150},
		duration: 19*FRAME_RATE, delay: 4*FRAME_RATE
	},
	{
		posStart: {x: (1591 - 1547) / 2, y: (821 - 842) / 2}, posEnd: {x: (2521 - 1547) / 2, y: (162 - 842) / 2},
		scaleStart: 0.9, scaleEnd: 1.34,
		rotationStart: Utils.gradToRad(29), rotationEnd: Utils.gradToRad(46),
		curvePoint: {x: 0, y: -150},
		duration: 19*FRAME_RATE, delay: 3*FRAME_RATE
	},
	{
		posStart: {x: (1591 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (1447 - 1547) / 2, y: (1738 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.12,
		rotationStart: Utils.gradToRad(-25), rotationEnd: Utils.gradToRad(46),
		curvePoint: {x: 0, y: -150},
		duration: 19*FRAME_RATE, delay: 5*FRAME_RATE
	},
	{
		posStart: {x: (1605 - 1547) / 2, y: (838 - 842) / 2}, posEnd: {x: (1425 - 1547) / 2, y: (1618 - 842) / 2},
		scaleStart: 0.5, scaleEnd: 0.81,
		rotationStart: Utils.gradToRad(21), rotationEnd: Utils.gradToRad(93),
		curvePoint: {x: 0, y: -150},
		duration: 19*FRAME_RATE, delay: 4*FRAME_RATE
	},
	{
		posStart: {x: (1579 - 1547) / 2, y: (813 - 842) / 2}, posEnd: {x: (2721 - 1547) / 2, y: (656 - 842) / 2},
		scaleStart: 0.6, scaleEnd: 1.49,
		rotationStart: Utils.gradToRad(17), rotationEnd: Utils.gradToRad(-28),
		curvePoint: {x: 0, y: -150},
		duration: 19*FRAME_RATE, delay: 5*FRAME_RATE
	},
	{
		posStart: {x: (1589 - 1547) / 2, y: (826 - 842) / 2}, posEnd: {x: (369 - 1547) / 2, y: (582 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.57,
		rotationStart: Utils.gradToRad(-34), rotationEnd: Utils.gradToRad(16),
		curvePoint: {x: 0, y: -150},
		duration: 19*FRAME_RATE, delay: 6*FRAME_RATE
	},
	{
		posStart: {x: (1585 - 1547) / 2, y: (822 - 842) / 2}, posEnd: {x: (861 - 1547) / 2, y: (1508 - 842) / 2},
		scaleStart: 0.8, scaleEnd: 0.91,
		rotationStart: Utils.gradToRad(-7), rotationEnd: Utils.gradToRad(-6),
		curvePoint: {x: 0, y: -150},
		duration: 21*FRAME_RATE, delay: 7*FRAME_RATE
	},
	{
		posStart: {x: (1587 - 1547) / 2, y: (818 - 842) / 2}, posEnd: {x: (2341 - 1547) / 2, y: (1600 - 842) / 2},
		scaleStart: 0.6, scaleEnd: 1.49,
		rotationStart: Utils.gradToRad(17), rotationEnd: Utils.gradToRad(-28),
		curvePoint: {x: 0, y: -150},
		duration: 24*FRAME_RATE, delay: 8*FRAME_RATE
	},
	{
		posStart: {x: (1607 - 1547) / 2, y: (850 - 842) / 2}, posEnd: {x: (745 - 1547) / 2, y: (1582 - 842) / 2},
		scaleStart: 0.7, scaleEnd: 1.57,
		rotationStart: Utils.gradToRad(-34), rotationEnd: Utils.gradToRad(16),
		curvePoint: {x: 0, y: -150},
		duration: 21*FRAME_RATE, delay: 9*FRAME_RATE
	}
];

class CoinsFlyAnimation extends Sprite
{
	static get EVENT_ON_ANIMATION_ENDED()	{return "eventOnCoinsFlyAnimationEnded";}

	startAnimation(aDelay_num)
	{
		this._startAnimation(aDelay_num);
	}

	set coinTextures(aValue_arr)
	{
		this._fCoinTextures_arr = aValue_arr;
	}

	constructor()
	{
		super();

		this._fTimer_t = null;
		this._fDelayTimers_arr = [];
		this._fAnimatedObjects_arr = [];
		this._fGenerateId_num = 0;
		this._fCoinTextures_arr = null;
	}

	_startAnimation(aDelay_num)
	{
		this._fTimer_t = new Timer(()=>{
			this._startExplode();
		}, aDelay_num);
	}

	_startExplode()
	{
		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = null;

		for (let i = 0; i < COINS_CONFIG.length; ++i)
		{
			if (COINS_CONFIG[i].delay)
			{
				let lConfig_obg = COINS_CONFIG[i];
				let lTimer_t = new Timer(()=>{
					this._generateCoin(lConfig_obg);
				}, COINS_CONFIG[i].delay);
				this._fDelayTimers_arr.push(lTimer_t);
			}
			else
			{
				this._generateCoin(COINS_CONFIG[i]);
			}
		}
	}

	_generateCoin(aConfig_obj)
	{
		let lPathContainer_sprt = this.addChild(new Sprite());

		let lCoin_sprt = lPathContainer_sprt.addChild(new Sprite());
		lCoin_sprt.textures = this._fCoinTextures_arr || AtlasSprite.getFrames([APP.library.getAsset("common/coin_spin")], AtlasConfig.WinCoin, "");
		lCoin_sprt.anchor.set(0.5);
		lCoin_sprt.scale.set(aConfig_obj.scaleStart);
		lCoin_sprt.position.set(aConfig_obj.posStart.x, aConfig_obj.posStart.y);
		lCoin_sprt.rotation = aConfig_obj.rotationStart;

		++this._fGenerateId_num;
		if (this._fGenerateId_num >= lCoin_sprt.textures.length) this._fGenerateId_num = 0;
		let lStartFrameIndex_num = ++this._fGenerateId_num;
		lCoin_sprt.gotoAndStop(lStartFrameIndex_num);

		this._fAnimatedObjects_arr.push(lPathContainer_sprt);
		this._fAnimatedObjects_arr.push(lCoin_sprt);

		let lCoinMove_seq = [
			{tweens: [{prop: "position.x", to: aConfig_obj.curvePoint.x}, {prop: "position.y", to: aConfig_obj.curvePoint.y}], duration: aConfig_obj.duration/2, ease: Easing.cubic.easeOut},
			{tweens: [{prop: "position.x", to: 0}, {prop: "position.y", to: 0}], duration: aConfig_obj.duration/2, ease: Easing.cubic.easeIn}
		];

		let lCoinScale_seq = [
			{tweens: [{prop: "scale.x", to: aConfig_obj.scaleEnd}, {prop: "scale.y", to: aConfig_obj.scaleEnd}], duration: aConfig_obj.duration}
		];

		let lCoinRotation_seq = [
			{tweens: [{prop: "rotation", to: aConfig_obj.rotationEnd}], duration: aConfig_obj.duration}
		];

		Sequence.start(lCoin_sprt, lCoinMove_seq);
		Sequence.start(lCoin_sprt, lCoinScale_seq);
		Sequence.start(lCoin_sprt, lCoinRotation_seq);

		let lPathMove_seq = [
			{tweens: [{prop: "position.x", to: aConfig_obj.posEnd.x}, {prop: "position.y", to: aConfig_obj.posEnd.y}], duration: aConfig_obj.duration, onfinish: ()=>{
				if (lCoin_sprt)
				{
					let id = this._fAnimatedObjects_arr.indexOf(lCoin_sprt);
					if (~id)
					{
						this._fAnimatedObjects_arr.splice(id, 1);
					}
					lCoin_sprt && Sequence.destroy(Sequence.findByTarget(lCoin_sprt));
					lCoin_sprt.destroy();
				}

				if (lPathContainer_sprt)
				{
					let id = this._fAnimatedObjects_arr.indexOf(lPathContainer_sprt);
					if (~id)
					{
						this._fAnimatedObjects_arr.splice(id, 1);
					}
					lPathContainer_sprt && Sequence.destroy(Sequence.findByTarget(lPathContainer_sprt));
					lPathContainer_sprt.destroy();
				}

				this._onNextCoinEnded();
			}}
		];

		Sequence.start(lPathContainer_sprt, lPathMove_seq);
	}

	_onNextCoinEnded()
	{
		if (this._fAnimatedObjects_arr.length == 0)
		{
			this.emit(CoinsFlyAnimation.EVENT_ON_ANIMATION_ENDED);
		}
	}

	destroy()
	{
		this._fTimer_t && this._fTimer_t.destructor();

		while (this._fAnimatedObjects_arr && this._fAnimatedObjects_arr.length)
		{
			let lAnim = this._fAnimatedObjects_arr.pop();
			lAnim && Sequence.destroy(Sequence.findByTarget(lAnim));
		}

		for (let timer of this._fDelayTimers_arr)
		{
			timer && timer.destructor();
		}

		super.destroy();

		this._fTimer_t = null;
		this._fGenerateId_num = null;
		this._fAnimatedObjects_arr = null;
		this._fDelayTimers_arr = null;
	}
}

export default CoinsFlyAnimation;