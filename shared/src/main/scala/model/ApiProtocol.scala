package crashbox.ci
package model

import spray.json.{DefaultJsonProtocol, DerivedFormats}

trait ApiProtocol extends DefaultJsonProtocol with DerivedFormats {
  // add custom protocols here
}
