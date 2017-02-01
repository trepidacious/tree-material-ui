package org.rebeam.tree

import japgolly.scalajs.react._

package object view {
  type RCP[P] = ReactComponentU[P, Unit, Unit, TopNode]
  type RCPS[P, S] = ReactComponentU[P, S, Unit, TopNode]
}
