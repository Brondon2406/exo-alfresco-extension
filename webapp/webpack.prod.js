const path = require('path');
const ESLintPlugin = require('eslint-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader')
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

const app = 'alfresco-extension-webapp';

const config = {
  mode: 'production',
  context: path.resolve(__dirname, '.'),
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          'babel-loader',
        ]
      },
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
        ]
      },
      {
        test: /\.css$/,
        use: [
          MiniCssExtractPlugin.loader,
          'css-loader'
        ]
      }
    ]
  },
  plugins: [
    new ESLintPlugin({
      files: [
        './src/main/webapp/vue-app/*.js',
        './src/main/webapp/vue-app/*.vue',
        './src/main/webapp/vue-app/**/*.js',
        './src/main/webapp/vue-app/**/*.vue',
      ],
      failOnError: false,
      failOnWarning: false,
    }),
    new VueLoaderPlugin(),
    new MiniCssExtractPlugin({
      filename: 'css/alfrescoApp.css',
    }),
  ],
  entry: {
    alfrescoApp: './src/main/webapp/vue-app/main.js'
  },
  output: {
    path: path.join(__dirname, './src/main/webapp'),
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  externals: {
    vuetify: 'Vuetify',
    jquery: '$',
  },
};

module.exports = config;
