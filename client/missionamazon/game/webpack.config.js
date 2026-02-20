const path = require('path');
const { resolvePixiPackage } = require('../../common/PIXI/build/pixi-package-resolver');
const PIXI_PACKAGE = resolvePixiPackage(__dirname);
const PROJECT_SRC = path.resolve(__dirname, 'src');
const PIXI_SRC = path.resolve(__dirname, "../../common/PIXI/src");
const SHARED_SRC = path.resolve(__dirname, "../shared/src");
const webpack = require('webpack');

module.exports = {
	entry: [
		'babel-polyfill',
		'./src/index',
		'webpack-dev-server/client?http://localhost:8081'
	],
	output: { filename: 'game.js' },
	devtool: 'source-map',
	module: {
		rules: [
			{
				test: /\.js$/,
				include: [PIXI_SRC, PROJECT_SRC, SHARED_SRC],
				exclude: [/node_modules/],
				loader: 'babel-loader?cacheDirectory',
				query: { presets: ["es2015", "stage-0"], cacheDirectory: true}
			}
		],
		noParse: /.*[\/\\]bin[\/\\].+\.js/
	},
	optimization: {
		minimize: false
	},
	devServer: { contentBase: __dirname },
	plugins: [
		new webpack.ProvidePlugin({
			PIXI: PIXI_PACKAGE
		})
	]};
