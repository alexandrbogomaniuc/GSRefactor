import { Vector } from './Vector';

/**
 * @class
 * @classdesc Rectangle
 */
class Rectangle {

	/**
	 * @constructor
	 * @param {Number} x - x coordinate of the center
	 * @param {Number} y - y coordinate of the center
	 * @param {Number} w - width
	 * @param {Number} h - height
	 * @param {Number} angle
	 */
	constructor(x, y, w, h, angle) {
		/**
		 * @member {Vector} center
		 */
		this.center = new Vector(x, y);

		/**
		 * @member {Number} width
		 */
		this.width = w;

		/**
		 * @member {Number} height
		 */
		this.height = h;

		/**
		 * @member {Number} angle - rotation angle
		 */
		this.angle = angle;

		/**
		 * @member {Array} vertices - array of verticies
		 */
		this.vertices = [];

		/**
		 * @type Array
		 * @member {Array} AABB - Coordinates of top-left and bottom-right angle
		 */
		this.AABB = [];

		this.refreshVertices();
	}

	/**
	 * Clone rectangle
	 * @returns {Rectangle} a copy of rectangle
	 */
	clone() {
		return new Rectangle(this.center.x, this.center.y, this.width, this.height, this.angle);
	}

	/**
	 * recalculate verticies
	 */
	refreshVertices() {
		var w = this.width / 2;
		var h = this.height / 2;
		this.vertices = [];
		this.vertices.push(new Vector(-w, h));
		this.vertices.push(new Vector(w, h));
		this.vertices.push(new Vector(w, -h));
		this.vertices.push(new Vector(-w, -h));

		this.AABB = [this.center.clone(), this.center.clone()];

		for (var i = 0; i < 4; i++) {
			this.vertices[i].rotate(-this.angle, this.center);
			if (this.vertices[i].x < this.AABB[0].x) this.AABB[0].x = this.vertices[i].x;
			if (this.vertices[i].x > this.AABB[1].x) this.AABB[1].x = this.vertices[i].x;
			if (this.vertices[i].y < this.AABB[0].y) this.AABB[0].y = this.vertices[i].y;
			if (this.vertices[i].y > this.AABB[1].y) this.AABB[1].y = this.vertices[i].y;
		}
	}

	/**
	 * Rectangle offset
	 * @param {Number} x - X-axis offset
	 * @param {Number} y - Y-axis offset
	 */
	move(x, y) {
		this.center.add(new Vector(x, y));
		this.refreshVertices();
	}

	/**
	 * Rectangke rotation
	 * @param {Number} angle - angle offset
	 */
	rotate(angle) {
		this.angle += angle;
		this.refreshVertices();
	}

	/**
	 * Checking if a point is in a rectangle
	 * @param {Vector} point
	 * @returns {Boolean}
	 */
	hitTestPoint(point) {
		var p = point.clone();
		p.normalize(-this.angle, this.center);
		return ((Math.abs(p.x) <= (this.width / 2)) && (Math.abs(p.y) <= (this.height / 2)));
	}

	/**
	 * Checking for an intersection with another rectangle
	 * @param {Rectangle} rect
	 * @returns {Boolean}
	 */
	hitTestRectangle(rect) {
		var r1 = this.clone();
		var r2 = rect.clone();
		var len, len1, len2;

		r1.move(-this.center.x, -this.center.y);
		r2.move(-this.center.x, -this.center.y);
		r2.center.rotate(this.angle);
		r1.rotate(-this.angle);
		r2.rotate(-this.angle);
		len = Math.max(r1.AABB[0].x, r1.AABB[1].x, r2.AABB[0].x, r2.AABB[1].x) - Math.min(r1.AABB[0].x, r1.AABB[1].x, r2.AABB[0].x, r2.AABB[1].x);
		len1 = r1.AABB[1].x - r1.AABB[0].x;
		len2 = r2.AABB[1].x - r2.AABB[0].x;
		if (len > len1 + len2) return false;

		len = Math.max(r1.AABB[0].y, r1.AABB[1].y, r2.AABB[0].y, r2.AABB[1].y) - Math.min(r1.AABB[0].y, r1.AABB[1].y, r2.AABB[0].y, r2.AABB[1].y);
		len1 = r1.AABB[1].y - r1.AABB[0].y;
		len2 = r2.AABB[1].y - r2.AABB[0].y;
		if (len > len1 + len2) return false;

		r1.move(-r2.center.x, -r2.center.y);
		r2.move(-r2.center.x, -r2.center.y);
		r1.center.rotate(r2.angle);
		r1.refreshVertices();
		r1.rotate(-r2.angle);
		r2.rotate(-r2.angle);

		len = Math.max(r1.AABB[0].x, r1.AABB[1].x, r2.AABB[0].x, r2.AABB[1].x) - Math.min(r1.AABB[0].x, r1.AABB[1].x, r2.AABB[0].x, r2.AABB[1].x);
		len1 = r1.AABB[1].x - r1.AABB[0].x;
		len2 = r2.AABB[1].x - r2.AABB[0].x;
		if (len > len1 + len2) return false;

		len = Math.max(r1.AABB[0].y, r1.AABB[1].y, r2.AABB[0].y, r2.AABB[1].y) - Math.min(r1.AABB[0].y, r1.AABB[1].y, r2.AABB[0].y, r2.AABB[1].y);
		len1 = r1.AABB[1].y - r1.AABB[0].y;
		len2 = r2.AABB[1].y - r2.AABB[0].y;

		return (len <= len1 + len2);
	}

}

export {Rectangle};