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

).jsSettings(
  // Add JS-specific settings here
  libraryDependencies ++= Seq(
    "com.github.chandu0101.scalajs-react-components" %%% "core" % scalajsReactComponentsVersion
  ),

  // React JS itself (Note the filenames, adjust as needed, eg. to remove addons.)
  jsDependencies ++= Seq(

    "org.webjars.bower" % "react" % reactVersion
      /        "react-with-addons.js"
      minified "react-with-addons.min.js"
      commonJSName "React",

    "org.webjars.bower" % "react" % reactVersion
      /         "react-dom.js"
      minified  "react-dom.min.js"
      dependsOn "react-with-addons.js"
      commonJSName "ReactDOM",

    "org.webjars.bower" % "react" % reactVersion
      /         "react-dom-server.js"
      minified  "react-dom-server.min.js"
      dependsOn "react-dom.js"
      commonJSName "ReactDOMServer"
  )
)

lazy val treeMaterialUiJVM = treeMaterialUi.jvm
lazy val treeMaterialUiJS = treeMaterialUi.js