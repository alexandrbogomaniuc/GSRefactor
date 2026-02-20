const PACKAGE = require('./package.json');
const path = require('path');
const { resolvePixiPackage } = require('../../common/PIXI/build/pixi-package-resolver');
const PIXI_PACKAGE = resolvePixiPackage(__dirname);
const webpack = require('webpack');

const CopyWebpackPlugin = require('copy-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const WebpackObfuscator = require('webpack-obfuscator');

const PROJECT_SRC = path.resolve(__dirname, 'src');
const PIXI_SRC = path.resolve(__dirname, "../../common/PIXI");
const BUILD_OUTPUT = path.resolve(__dirname, "dist/build");
const VueLoaderPlugin = require('vue-loader/lib/plugin')

module.exports = {
	context: __dirname,
	entry: {
		game: [
			'babel-polyfill',
			'vue',
			path.resolve(PROJECT_SRC, 'index.js')
		]
	},
	output: {
		path: __dirname + "/dist/build",
		filename: '[name].js'
	},
	module: {
		rules: [
			{
				test: /\.js$/,
				include: [PIXI_SRC, PROJECT_SRC],
				exclude: [],
				loader: 'babel-loader',
				query: { presets: ["es2015", "stage-0"] ,compact: false},
				
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
		noParse: [
			/.*[\/\\]bin[\/\\].+\.js/,
			/.*[\/\\]api_src[\/\\].+\.js/,
			/game\.js/
		]
	},
	resolve: {
		extensions: ['.js', '.json'],
		alias: {
			"P2M": PIXI_SRC,
			'vue$': 'vue/dist/vue.esm.js'
		}
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
		new CopyWebpackPlugin([
			{ from: 'index.html', to: path.resolve(BUILD_OUTPUT, 'index.html') },
			{ from: 'validator.js', to: path.resolve(BUILD_OUTPUT, 'validator.js') },
			{ from: 'common_ue.js', to: path.resolve(BUILD_OUTPUT, 'common_ue.js') },
			{ from: 'version.json', to: path.resolve(BUILD_OUTPUT, 'version.json') },
			{ from: 'assets/**/*', to: BUILD_OUTPUT },
			{ from: 'favicon.ico', to: path.resolve(BUILD_OUTPUT, 'favicon.ico') }
		], { copyUnmodified: true, debug: "warning" }),
		new VueLoaderPlugin(),
		new webpack.DefinePlugin({
			'process.env.NODE_ENV': JSON.stringify('production')
		}),
		new webpack.ProvidePlugin({
			PIXI: PIXI_PACKAGE
		}),
		new WebpackObfuscator({
			compact: true,
			controlFlowFlattening: false,
			deadCodeInjection: false,
			debugProtection: false,
			debugProtectionInterval: false,
			disableConsoleOutput: false,
			identifierNamesGenerator: 'hexadecimal',
			log: false,
			numbersToExpressions: false,
			renameGlobals: false,
			rotateStringArray: true,
			selfDefending: true,
			shuffleStringArray: true,
			simplify: true,
			splitStrings: false,
			stringArray: true,
			stringArrayEncoding: [],
			stringArrayWrappersCount: 1,
			stringArrayWrappersChainedCalls: true,
			stringArrayWrappersType: 'variable',
			stringArrayThreshold: 0.75,
			unicodeEscapeSequence: false
		})
	],
	performance: {
		hints: false
	}
};
