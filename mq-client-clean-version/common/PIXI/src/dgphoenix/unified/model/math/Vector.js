/**
 * Point
 * @class
 */
class Vector {

	/**
	 * @constructor
	 * @param {Number} x coordinate
	 * @param {Number} y coordinate
	 */
	constructor(x = 0, y = 0) {
		
		this.x = x;
		this.y = y;
	}

	/**
	 * Checking for 0 x and y values
	 */
	isZero() {
		return this.x == 0 && this.y == 0;
	}

	/**
	 * Clone Vector
	 * @returns {Vector} Copy of vector
	 */
	clone() {
		return new Vector(this.x, this.y);
	}

	/**
	 * Add points
	 * @param {Vector} p
	 */
	add(p) {
		this.x += p.x;
		this.y += p.y;
		return this;
	}

	/**
	 * Subtract points
	 * @param {Vector} p
	 */
	subtract(p) {
		this.x -= p.x;
		this.y -= p.y;
		return this;
	}

	/**
	 * Scalar multiplication
	 * @param {Number} n
	 */
	mult(n) {
		this.x *= n;
		this.y *= n;
		return this;
	}

	/**
	 * Invert point
	 */
	invert() {
		this.mult(-1);
		return this;
	}

	/**
	 * Rotation
	 * @param {Number} angle
	 * @param {Vector} offset
	 */
	rotate(angle, offset = new Vector(0, 0)) {
		var r = this.clone();
		r.subtract(offset);
		r.x = this.x * Math.cos(angle) + this.y * Math.sin(angle);
		r.y = this.x * -Math.sin(angle) + this.y * Math.cos(angle);
		r.add(offset);
		this.x = r.x;
		this.y = r.y;
		return this;
	}

	/**
	 * Point normalization
	 * @param {Number} angle
	 * @param {Vector} offset
	 */
	normalize(angle, offset = new Vector(0, 0)) {
		this.subtract(offset);
		this.rotate(-angle);
		return this;
	}

	/**
	 * Returns the scalar length of the vector
	 * @returns {Number} length of the vector
	 */
	getLength() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}

	/**
	 * Calc distance to another point
	 * @param {Vector} p
	 */
	distanceTo(p) {
		var p2 = this.clone();
		p2.subtract(p);
		return p2.getLength();
	}
}

export {Vector};