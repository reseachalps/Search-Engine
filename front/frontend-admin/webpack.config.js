var webpack = require('webpack');
var SplitByNamePlugin = require('split-by-name-webpack-plugin');
var autoprefixer = require('autoprefixer');
var precss = require('precss');

var config = {
    context: __dirname,
    entry: './index.ts',
    output: {
        filename: "[name].js",
        chunkFilename: "[name].js"
    },
    plugins: [
        new webpack.DefinePlugin({
            ON_PROD: process.env.NODE_ENV === 'production'
        }),
        new webpack.DefinePlugin({
            ON_TEST: process.env.NODE_ENV === 'test'
        })
    ],
    devtool: 'eval',
    resolve: {
        extensions: ['', '.webpack.js', '.web.js', '.ts', '.js']
    },
    module: {
        loaders: [
            {
                test: /\.ts$/,
                loader: 'ng-annotate!babel!ts',
                exclude: /node_modules/
            },
            {
                test: /\.js$/,
                loader: 'ng-annotate!babel',
                exclude: /node_modules/
            },
            {
                test: /\.html$/,
                loader: 'raw'
            },
            {
                test: /\.json$/,
                loader: 'raw'
            },
            {
                test: /\.svg$/,
                loader: 'raw'
            },
            {
                test: /\.css$/,
                loader: 'style!css!postcss'
            },
            {
                test: /\.styl$/,
                loader: 'style!css!postcss!stylus'
            },
            // font-awesome files
            {
                test: /font-awesome.*\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: "url?limit=10000&mimetype=application/font-woff"
            },
            {
                test: /font-awesome.*\.(png|ico|ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: "file?name=[path][name].[ext]?[hash]"
            }
        ]
    },
    postcss: function() {
        return [autoprefixer, precss];
    },
    devServer: {
        proxy: {
            '/api*': {
                target: 'http://localhost:7227',
                secure: false
            }
        }
    }
};

if (process.env.NODE_ENV !== 'test') {
    // split the production into 2 chunks, the app and the vendor code
    config.plugins.push(
        new SplitByNamePlugin({
            buckets: [{
                name: 'vendor',
                regex: /node_modules/
            }, {
                name: 'app',
                regex: /app/
            }]
        }));
}

if (process.env.NODE_ENV === 'production') {
    // Changing the output path to /dist
    config.output.path = __dirname + '/dist';
    config.devtool = 'source-map';

    // uglify the app code
    config.plugins.push(new webpack.optimize.UglifyJsPlugin({
        compress: {warnings: false},
        sourceMap: false
    }));
}

module.exports = config;