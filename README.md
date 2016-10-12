# tree-material-ui
Material-UI views and demo using tree

To install JS dependencies, run `npm install`. Then run `npm run build` to build all assets in `assets` directory using webpack.
To run server run sbt then `treeMaterialUiJVM/reStart`, and `treeMaterialUiJVM/reStop` to stop. `~treeMaterialUiJVM/reStart` to keep restarting server on code changes.
To compile client with fast optimisation, run sbt then `treeMaterialUiJS/fastOptJS`.

A quick tour of the important parts:

1. WebPack is used to pull together resources defined as bundles by `.js` files in the `bundles` directory. 
1. The bundle at `bundles/index.js` is loaded by index.html, which gets us all non-scalajs javascript. We have included material-ui in this for convenience.
2. Additional bundles can be loaded later, asynchronously, using WithAsyncScript, see [https://github.com/chandu0101/scalajs-react-components/blob/master/demo/src/main/scala/demo/pages/MuiPage.scala](sjrc demo, material UI page)
2. WebPack gets dependencies via `require('library-name')` in the bundles. Bundles can also include other needed javascript for setup (e.g. the react tap event plugin).
3. The `assets` directory is used for all bundles produced by WebPack.
4. Scalajs compiler is configured to output fastopt and opt js files to `assets` as well.
5. index.html contains minimal styles and html in javascript to display a loading screen, this is replaced by React UI when scalajs script runs (which may take a little while owing to size).
