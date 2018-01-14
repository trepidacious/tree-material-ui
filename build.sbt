name := "tree-material-ui root project"

version in ThisBuild := "0.1-SNAPSHOT"

organization in ThisBuild := "org.rebeam"

// Plain Scala
//scalaVersion in ThisBuild := "2.12.4"

//Typelevel Scala, also see .jsSettings below
scalaOrganization in ThisBuild := "org.typelevel"
scalaVersion in ThisBuild := "2.12.4-bin-typelevel-4"

scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Xcheckinit",
  "-Xlint:-unused",
  "-Ywarn-unused:imports",
  "-Ypartial-unification",
  "-language:existentials",
  "-language:higherKinds",
  "-Yno-adapted-args",
  //"-Ywarn-dead-code", - can't enable for JS, generates dead code warnings on js.native, so added to jvmSettings below
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
  //"-Yno-predef" ?
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
    addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.patch)

  //Settings specific to JVM
  ).jvmSettings(
    scalacOptions += "-Ywarn-dead-code",

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

    //Typelevel scala, see https://github.com/scala-js/scala-js/pull/2954#issuecomment-302743801
    // Remove the dependency on the scalajs-compiler
    libraryDependencies := libraryDependencies.value.filterNot(_.name == "scalajs-compiler"),
    // And add a custom one
    addCompilerPlugin("org.scala-js" % "scalajs-compiler" % scalaJSVersion cross CrossVersion.patch),

    // We get javascript dependencies from webpack
    jsDependencies ++= Seq()
  )

lazy val treeMaterialUiJVM = treeMaterialUi.jvm
lazy val treeMaterialUiJS = treeMaterialUi.js