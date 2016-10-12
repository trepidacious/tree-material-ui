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
  settings(
    name := "tree-material-ui",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.rebeam"                  %%%  "tree"    % "0.1-SNAPSHOT"
    ),
    //For @Lenses
    addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

  ).jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
    )
    // Something like this should add js output as resource for server?
    // resources in Compile += (fastOptJS in js).value.data

).jsSettings(
    // Add JS-specific settings here
    libraryDependencies ++= Seq(
      "com.github.chandu0101.scalajs-react-components" %%% "core" % scalajsReactComponentsVersion
    ),

    // Output compiled scala-js to assets directory
    artifactPath in (Compile, fastOptJS) :=
      file("assets") / ((moduleName in fastOptJS).value + "-fastopt.js"),

    artifactPath in (Compile, fullOptJS) :=
      file("assets") / ((moduleName in fullOptJS).value + "-opt.js"),

    // We get dependencies from webpack
    jsDependencies ++= Seq()
  )

lazy val treeMaterialUiJVM = treeMaterialUi.jvm
lazy val treeMaterialUiJS = treeMaterialUi.js