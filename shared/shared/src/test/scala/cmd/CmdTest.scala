package crashbox.ci


object CmdTest extends App {

  val cbx = cmd.Command(
    "cbx",
    cmd.Option("server", Some('s'), Some(cmd.Parameter("name"))),
    cmd.Command("version", cmd.Option("verbose", Some('v'))),
    cmd.Command("login", cmd.Parameter("server_url"), cmd.Parameter("username", false), cmd.Parameter("password", false)),
    cmd.Command("apply",
      cmd.Parameter("pipeline", false),
      cmd.Option("file", Some('f'), Some(cmd.Parameter("file"))),
      cmd.Option("force", None, Some(cmd.Parameter("b", false)))
    ))

  def run(line: cmd.CommandLine) = line.subcommand match {
    case Some(cmd.CommandLine("version", args, _)) =>
      println("0.1.2")
      if (args.contains("verbose")) {
        println("curl ")
      }
    case Some(cmd.CommandLine("login", args, _)) =>
      println("Logging in to server " + args("server_url"))
    case Some(cmd.CommandLine("create", args, _)) =>
      println("creating file " + args("file"))
  }

  try {
    val in = "cbx create -f foo.yml a c"
    val args = in.split(" ").drop(1)
    println(in)
    run(cmd.parse(cbx, args))
  } catch {
    case ex: cmd.ParseException =>
      println("parse error: " + ex.getMessage)
      println(cbx.usage)
  }


}
