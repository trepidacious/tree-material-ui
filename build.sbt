name := "tree-material-ui root project"

version in ThisBuild := "0.1-SNAPSHOT"

organization in ThisBuild := "org.rebeam"

scalaVersion in ThisBuild := "2.11.8"

scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint"
)

//SLF4J simple logger, y u log to System.err by default, even for info?
javaOptions in ThisBuild := Seq("-Dorg.slf4j.simpleLogger.logFile=System.out")

val scalajsReactVersion = "0.11.2"

val scalajsReactComponentsVersion = "0.5.0"

val reactVersion = "15.3.2"

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
      "org.rebeam"                  %%%  "tree"    % "0.1-SNAPSHOT"
    ),
    //For @Lenses
    addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

  //Settings specific to JVM
  ).jvmSettings(
    libraryDependencies ++= Seq(),
    //We need to assets directory at runtime, so we can serve files in it
    unmanagedClasspath in (Compile, runMain) += baseDirectory.value / "assets"

  //Settings specific to JS
  ).jsSettings(
    //Scalajs dependencies that are used on the client only
    libraryDependencies ++= Seq(
      "com.github.chandu0101.scalajs-react-components"  %%% "core"        % scalajsReactComponentsVersion,
      "com.github.japgolly.scalacss"                    %%% "ext-react"   % "0.5.0"
    ),

    // Output compiled scala-js to assets directory
    artifactPath in (Compile, fastOptJS) :=
      file("assets") / ((moduleName in fastOptJS).value + "-fastopt.js"),

    artifactPath in (Compile, fullOptJS) :=
      file("assets") / ((moduleName in fullOptJS).value + "-opt.js"),

    // We get javascript dependencies from webpack
    jsDependencies ++= Seq()
  )

lazy val treeMaterialUiJVM = treeMaterialUi.jvm
lazy val treeMaterialUiJS = treeMaterialUi.js