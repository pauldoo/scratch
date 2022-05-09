const webpack = require('webpack');

module.exports = function override(config) {
     const fallback = config.resolve.fallback || {};

     Object.assign( fallback, {
         "crypto": require.resolve("crypto-browserify")
     });

     config.resolve.fallback = fallback;
     return config;
}
