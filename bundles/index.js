// rebeam: This is the js required for JUST the front page.
//The other bundles add more javascript, and can be loaded with
//e.g. WithAsyncScript("assets/material_ui-bundle.js") to allow
//for the main page to start up without needing all bundles
//loaded.

// rebeam: We can require any of the dependencies in package.json
window.ReactDOM = require('react-dom');
window.React    = require('react');

// rebeam: Improves click/tap performance, see
//   https://github.com/callemall/material-ui#react-tap-event-plugin
//   http://stackoverflow.com/a/34015469/988941
var injectTapEventPlugin = require('react-tap-event-plugin');
injectTapEventPlugin();

// rebeam: Syntax highlighting. Not needed, left as example
//window.hljs = require("highlight.js");
//require("highlight.js/styles/github.css");

// rebeam: Images that can be encoded into script by webpack. Not needed,
// one left as an example. Seems like we get these via scala.scalajs.js.Dynamic.global,
// looking at webpack configuration googleMapImage should end up as a jString with the
// URL for the image, which might be a "Data URL" with the actual image encoded in the URL,
// to save HTTP requests.
//window.googleMapImage      = require("../images/googleMap.png");

// rebeam: Original index.js bundle did not include material, but for now
// it's easier to just require immediately rather than loading async. However
// we could load async if we start up a simple UI without this.
window.mui          = require("material-ui");
window.mui.Styles   = require("material-ui/styles");
window.mui.SvgIcons = require('material-ui/svg-icons/index');

// rebeam: react-grid-layout in normal and responsive variants, plus WidthProvider
window.ReactGridLayout = require('react-grid-layout');
window.ResponsiveReactGridLayout = require('react-grid-layout').Responsive;
window.WidthProvider = require('react-grid-layout').WidthProvider;

// rebeam: react-sortable-hoc
window.Sortable = require('react-sortable-hoc');
window.SortableContainer = window.Sortable.SortableContainer;
window.SortableElement = window.Sortable.SortableElement;
window.SortableHandle = window.Sortable.SortableHandle;

// rebeam: react-infinite
var Infinite = require('react-infinite');
window.Infinite = Infinite;