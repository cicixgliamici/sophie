addSbtPlugin("org.mixql" % "sbt-antlr4" % "0.8.5")

// sbt-assembly: permette di creare un fat JAR eseguibile (usato per distribuire la TUI come singolo jar)
// Non lo eseguiamo ora; è solo predisposizione nel progetto.
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.2.0")

// scoverage plugin per generare report di code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.6")
