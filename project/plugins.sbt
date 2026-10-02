addSbtPlugin("org.mixql" % "sbt-antlr4" % "0.8.5")

// Package the TUI and its runtime dependencies as one executable fat JAR.
// Packaging is explicit: the assembly task does not run during ordinary compilation.
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.2.0")

// Generate coverage reports so reviewers can inspect which behavior is exercised.
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.6")
