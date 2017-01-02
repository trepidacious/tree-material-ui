package org.rebeam.tree.view.pages

import japgolly.scalajs.react.Callback

case class Pages[P](current: P, set: P => Callback) {
}
