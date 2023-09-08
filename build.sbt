ThisBuild / scalaVersion     := "2.12.11"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

ThisBuild / resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val root = (project in file("."))
  .settings(
    name := "bill-tracker-api",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
      "org.postgresql" % "postgresql" % "42.6.0",
      "io.scalac" %% "zio-slick-interop" % "0.6.0",
      "dev.zio" %% "zio" % "2.0.16",
      "dev.zio" %% "zio-json" % "0.6.1",
      "dev.zio" %% "zio-http" % "3.0.0-RC2+64-ca6513f7-SNAPSHOT",
      "dev.zio" %% "zio-config" % "4.0.0-RC16",
      "dev.zio" %% "zio-config-typesafe" % "4.0.0-RC16",
      "dev.zio" %% "zio-config-magnolia" % "4.0.0-RC16",
      "dev.zio" %% "zio-logging-slf4j2" % "2.1.12",
      "dev.zio" %% "zio-logging" % "2.1.12",
      "org.slf4j" % "slf4j-simple" % "2.0.9",
      "com.pauldijou" %% "jwt-json4s-jackson" % "5.0.0",
      "dev.zio" %% "zio-streams" % "2.0.9",
      "dev.zio" %% "zio-kafka" % "2.1.1",
      "dev.zio" %% "zio-test" % "2.0.16" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
