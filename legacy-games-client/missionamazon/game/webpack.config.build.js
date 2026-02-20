const PACKAGE = require('./package.json');
const path = require('path');
const { resolvePixiPackage } = require('../../common/PIXI/build/pixi-package-resolver');
const PIXI_PACKAGE = resolvePixiPackage(__dirname);
const webpack = require('webpack');

const CopyWebpackPlugin = require('copy-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');

const PROJECT_SRC = path.resolve(__dirname, 'src');
const PIXI_SRC = path.resolve(__dirname, "../../common/PIXI");
const SHARED_SRC = path.resolve(__dirname, "../shared/src");
const BUILD_OUTPUT = path.resolve(__dirname, "dist/build");

module.exports = {
	context: __dirname,
	entry: {
		game: [
			'babel-polyfill',
			path.resolve(PROJECT_SRC, 'index.js')
		]
	},
	devtool: 'source-map',
	output: {
		path: __dirname + "/dist/build",
		filename: '[name].js'
	},
	module: {
		rules: [
			{
				test: /\.js$/,
				include: [PIXI_SRC, PROJECT_SRC, SHARED_SRC],
				exclude: [],
				loader: 'babel-loader',
				query: { presets: ["es2015", "stage-0"], compact: false }
			}
		],
		noParse: [
			/.*[\/\\]bin[\/\\].+\.js/,
			/.*[\/\\]api_src[\/\\].+\.js/,
			/game\.js/
		]
	},
	resolve: {
		extensions: ['.js', '.json'],
		alias: {
			"P2M": PIXI_SRC
		}
	},
	optimization: {
		minimizer: [new UglifyJsPlugin({sourceMap: true})],
	},
	plugins: [
		new HtmlWebpackPlugin({
			title: PACKAGE.description,
			filename: 'index.html',
			template: 'index.html',
			inject: false,
			minify: {
				removeComments: true,
				collapseWhitespace: true,
				removeAttributeQuotes: true
			},
		}),
		new webpack.ProvidePlugin({
			PIXI: PIXI_PACKAGE
		}),
		new CopyWebpackPlugin([
			{ from: 'index.html', to: path.resolve(BUILD_OUTPUT, 'index.html') },
			{ from: 'validator.js', to: path.resolve(BUILD_OUTPUT, 'validator.js') },
			{ from: 'version.json', to: path.resolve(BUILD_OUTPUT, 'version.json') },
			{ from: 'assets/**/*', to: BUILD_OUTPUT },
			{ from: 'favicon.ico', to: path.resolve(BUILD_OUTPUT, 'favicon.ico') },
			{
				from: 'assets/spine/**/**/*.json',
				transform: function (content) {
					return JSON.stringify(JSON.parse(content.toString()));
				},
				to: BUILD_OUTPUT
			}
		], { copyUnmodified: true, debug: "warning" })
	],
	performance: {
		hints: false
	}
};
