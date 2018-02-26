package crashbox.ci
package dal

import slick.jdbc.JdbcProfile

trait DatabaseConfig {
  val profile: JdbcProfile
  val database: JdbcProfile#Backend#Database
}
