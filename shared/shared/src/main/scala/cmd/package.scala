package crashbox.ci
package cmd

object `package` {
  def parse(command: Command, arguments: Seq[String]) =
    Parser.parse(command, arguments)
}
