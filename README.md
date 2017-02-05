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

## TODO

- [X] Direction of transition of pages set by relationship between them. Use a comparison typeclass that yields a direction from two pages. Provide an instance working on pages that implement "back".
- [ ] Look at shadows - can we make them appear underneath (lower z) the thing casting them?
- [ ] Responsive pages - provide a "no effort" implementation for larger screens that displays layers alongside each other, animating them dropping in from above on enter, and down on leave.
- [ ] ServerRootComponent undo/redo. If we do this, also include monitoring of the P in CursorP, so that we can restore the page that was displayed when the undone/redone action occurred.
- [ ] Focus in pages. When going back from a detail page, the master page could remember the displayed detail "index" as a "focus". This could be used to e.g. scroll to that element in a list, so that when navigating backwards, we see the detail we just left in the master list. This is a lighter alternative to just keeping the master view around while displaying detail, and would work well with undo/redo. Focus might or might not update with scrolling.
- [ ] Websocket re-open on close with exponential backoff.
- [ ] Connect server model up to database.