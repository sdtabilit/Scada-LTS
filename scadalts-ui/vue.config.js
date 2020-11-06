var webpack = require('webpack');
const fs = require('fs')
const packageJson = JSON.parse(fs.readFileSync('./package.json'))
const tag = packageJson.tag || 0;
const version = packageJson.version || 0
const milestone = packageJson.milestone || 0
const build = packageJson.build || 0
const branch = packageJson.branch || 'local'
module.exports = {
  filenameHashing: false,

  configureWebpack: {
    devtool: 'source-map',
    plugins: [
      new webpack.DefinePlugin({
        'process.env': {
          PACKAGE_VERSION: '"' + version + '"',
          PACKAGE_TAG: '"' + tag + '"',
          SCADA_LTS_MILESTONE: '"' + milestone + '"',
          SCADA_LTS_BUILD: '"' + build + '"',
          SCADA_LTS_BRANCH: '"' + branch + '"'
        }
      })
    ]
  },

  pluginOptions: {
    i18n: {
      locale: 'en',
      fallbackLocale: 'en',
      localeDir: 'locales',
      enableInSFC: true
    }
  }
}
