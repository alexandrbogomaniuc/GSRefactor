let A = document.createElement('a');
function normalizeURL(url) {
	A.href = url;
	let auth = A.username;
	if (auth) {
		auth += (A.password && `:${A.password}`) + '@';
	}
	return `${A.protocol}//${auth}${A.host}${A.pathname}${A.search}${A.hash}`;
}

function parseQueryString(str) {
	let query = new Map();
	if (str.length) {
		if (str.substr(0, 1) == '?') str = str.substr(1);
		str = str.split('&').map(param => param.split('='));
		for (let param of str) {
			query.set(
				decodeURIComponent(param.shift()),
				decodeURIComponent(param.join('='))
			);
		}
	}
	return query;
}

function createQueryString(query) {
	let queryStr = [];
	for (let [param, value] of query) {
		queryStr.push(`${encodeURIComponent(param)}${encodeURIComponent(value)}`);
	}
	return queryStr.join('&');
}

function parseURL(url) {
	A.href = normalizeURL(url);

	let path = (A.pathname || '/').split('/');
	let basename = path.length > 1 && path.pop() || '';
	path = path.join('/');

	let ext = basename.length && basename.indexOf('.') && basename.split('.').pop();
	let params = parseQueryString(A.search);

	return {
		href: A.href,

		protocol: A.protocol,
		username: A.username,
		password: A.password,
		host: A.host,
		hostname: A.hostname,
		port: A.port,
		pathname: A.pathname,
		search: A.search,
		hash: A.hash,

		path: path,
		basename: basename,
		extension: ext,
		params: params
	}
}

export {
	normalizeURL,
	parseURL,
	parseQueryString,
	createQueryString
}