name := "tree-material-ui root project"

version in ThisBuild := "0.1-SNAPSHOT"

organization in ThisBuild := "org.rebeam"

scalaVersion in ThisBuild := "2.12.4"

scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Xfatal-warnings",
//  "-Xlint",
  "-Xcheckinit"
)

resolvers += Resolver.sonatypeRepo("snapshots")

//SLF4J simple logger, y u log to System.err by default, even for info?
javaOptions in ThisBuild := Seq("-Dorg.slf4j.simpleLogger.logFile=System.out")

val scalajsReactComponentsVersion = "0.8.0"
val scalacssVersion = "0.5.3"
val pathsScalaJsVersion = "0.4.5"

val assetsDir = file("assets")

lazy val root = project.in(file(".")).
  aggregate(treeMaterialUiJS, treeMaterialUiJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val treeMaterialUi = crossProject.in(file(".")).

  //Settings for all projects
  settings(
    name := "tree-material-ui",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.rebeam"                  %%%  "tree"    % "0.1-SNAPSHOT",

      "org.log4s"   %% "log4s"                % "1.3.3",
      "org.slf4j"   % "slf4j-simple"          % "1.7.21"
    ),
    //For @Lenses
    addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

  //Settings specific to JVM
  ).jvmSettings(
    libraryDependencies ++= Seq()//,
    //We need to assets directory at runtime, so we can serve files in it
//    unmanagedClasspath in (Compile, runMain) += baseDirectory.value / "assets"

  //Settings specific to JS
  ).jsSettings(
    //Scalajs dependencies that are used on the client only
    libraryDependencies ++= Seq(
      "com.olvind"                                      %%% "scalajs-react-components"  % scalajsReactComponentsVersion,
      "com.github.japgolly.scalacss"                    %%% "ext-react"                 % scalacssVersion,
      "org.rebeam"                                      %%% "paths-scala-js"            % pathsScalaJsVersion
    ),

    crossTarget in (Compile, fullOptJS) := assetsDir,
    crossTarget in (Compile, fastOptJS) := assetsDir,
    crossTarget in (Compile, packageJSDependencies) := assetsDir,
//    crossTarget in (Compile, packageLauncher) := assetsDir,

    // We get javascript dependencies from webpack
    jsDependencies ++= Seq()
  )

lazy val treeMaterialUiJVM = treeMaterialUi.jvm
lazy val treeMaterialUiJS = treeMaterialUi.js