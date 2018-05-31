/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

const path = require('path');
const webpack = require('webpack');
var CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
    context: path.resolve(__dirname, './src'),
    devtool: "source-map",
    entry: {
        index: './EmotionsAnalysis.jsx'
    },
    output: {
        path: path.resolve(__dirname, './dist/EmotionsAnalysis/'),
        filename: 'EmotionsAnalysis.js'
    },
    module: {
        loaders: [
            {
                test: /\.html$/,
                use: [{loader: 'html-loader'}]
            },
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: [
                    {
                        loader: 'babel-loader',
                        query: {
                            presets: ['es2015', 'react']
                        }
                    }
                ]
            },
            {
                test: /\.(png|jpg|svg|cur|gif|eot|svg|ttf|woff|woff2)$/,
                use: ['url-loader']
            },
            {
                test: /\.jsx?$/,
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {
                    presets: ['es2015', 'react']
                }
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.scss$/,
                use: [{loader: 'style-loader'}, {loader: 'css-loader'}, {loader: 'sass-loader'}]
            }

        ]
    },
    plugins: [
        new CopyWebpackPlugin([
            {from: path.resolve(__dirname, './src/resources/')}
        ])
    ],
    resolve: {
        extensions: ['.js', '.json', '.jsx', '.scss']
    },
    devServer: {
        contentBase: path.join(__dirname, 'public'),
        publicPath: '/dist/'
    },
    externals: {
        react: 'React'
    }
};
