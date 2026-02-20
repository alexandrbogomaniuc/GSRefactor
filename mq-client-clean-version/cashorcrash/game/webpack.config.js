const path = require('path');
const { resolvePixiPackage } = require('../../common/PIXI/build/pixi-package-resolver');
const PIXI_PACKAGE = resolvePixiPackage(__dirname);
const PROJECT_SRC = path.resolve(__dirname, 'src');
const PIXI_SRC = path.resolve(__dirname, "../../common/PIXI/src");
const VueLoaderPlugin = require('vue-loader/lib/plugin');
const webpack = require('webpack');

module.exports = {
	entry: [
		'babel-polyfill',
		'vue',
		'./src/index',
		'webpack-dev-server/client?http://localhost:8080'
	],
	output: { filename: 'game.js' },
	devtool: 'source-map',
	module: {
		rules: [
			{
				test: /\.js$/,
				include: [PIXI_SRC, PROJECT_SRC],
				exclude: [/node_modules/],
				loader: 'babel-loader?cacheDirectory',
				query: { presets: ["es2015", "stage-0"], cacheDirectory: true}
			},
			{
				test: /\.vue$/,
				exclude: [/node_modules/],
				loader: 'vue-loader'
			},
			{
				test: /\.css$/,
				exclude: [/node_modules/],
				use: [
					'vue-style-loader',
					'css-loader'
				]
			}
		],
		noParse: /.*[\/\\]bin[\/\\].+\.js/
	},
	optimization: {
		minimize: false
	},
	devServer: { contentBase: __dirname },
	resolve: {
		alias: {
			'vue$': 'vue/dist/vue.esm.js'
		}
	},
	plugins: [
		new VueLoaderPlugin(),
		new webpack.ProvidePlugin({
			PIXI: PIXI_PACKAGE
		})
	]
};
