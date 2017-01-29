package org.rebeam.tree

import japgolly.scalajs.react._

package object view {
  type RCP[P] = ReactComponentU[P, Unit, Unit, TopNode]
}
