package com.github.dnvriend.sam.schema.plugin.service

import sbt._
import scalaz._
import scalaz.Scalaz._

object ScriptService {
  private final val FILE_NAME_SCRIPT = "Script.scala"
  private final val MaxErrors = 1000

  /**
   * Generates/Creates 'Script.scala'
   */
  def createScriptFile(
    workDir: File,
    schemas: List[String]
  ): File = {
    val schemaGeneratorFile: File = workDir / FILE_NAME_SCRIPT

    val listContent: String = {
      schemas
        .map(fqcn => s""""${fqcn}"""")
        .zip(schemas.map(schemaForContent))
        .map { case (fqcn, schemaFor) => s"($fqcn, $schemaFor)" }
        .intercalate(",")
    }

    IO.write(schemaGeneratorFile, scriptContent(listContent))
    schemaGeneratorFile
  }

  def schemaForContent(fqcn: String): String = {
    s"""SchemaFor[$fqcn]().toString(true)"""
  }

  def scriptContent(listContent: String): String = {
    s"""
       |import com.sksamuel.avro4s._
       |class Script {
       |def run(): Any = {
       |  val xs: List[(String, String)] = List($listContent)
       |  xs.flatMap { case (fqcn, schema) => List(fqcn, schema) }.toArray
       | }
       |}""".stripMargin
  }

  /**
   * Compiles 'Script.scala'
   */
  def compileScript(
    compiler: xsbti.compile.Compilers,
    fileToCompile: File,
    classpath: Seq[File],
    outputDir: File,
    options: Seq[String],
    cache: xsbti.compile.GlobalsCache,
    logger: sbt.util.Logger): Unit = {

    getCompiler(compiler).apply(
      Array(fileToCompile),
      NoChanges,
      classpath.toArray,
      outputDir,
      options.toArray,
      NoopCallback,
      MaxErrors,
      cache,
      getLogger(logger)
    )
  }

  def getCompiler(compiler: xsbti.compile.Compilers): sbt.internal.inc.AnalyzingCompiler = compiler.scalac() match {
    case compiler: sbt.internal.inc.AnalyzingCompiler => compiler
    case _                                            => sys.error("Expected a 'sbt.internal.inc.AnalyzingCompiler' compiler")
  }

  def getLogger(logger: sbt.util.Logger): sbt.internal.util.ManagedLogger = logger match {
    case log: sbt.internal.util.ManagedLogger => log
    case _                                    => sys.error("Expected a 'sbt.internal.util.ManagedLogger' logger")
  }

  private final val NoChanges = new xsbti.compile.DependencyChanges {
    def isEmpty = true
    def modifiedBinaries = Array()
    def modifiedClasses = Array()
  }

  private final val NoopCallback = new xsbti.AnalysisCallback {
    override def startSource(source: File): Unit = {}
    override def mainClass(sourceFile: File, className: String): Unit = {}
    override def apiPhaseCompleted(): Unit = {}
    override def enabled(): Boolean = false
    override def binaryDependency(onBinaryEntry: File, onBinaryClassName: String, fromClassName: String, fromSourceFile: File, context: xsbti.api.DependencyContext): Unit = {}
    override def generatedNonLocalClass(source: File, classFile: File, binaryClassName: String, srcClassName: String): Unit = {}
    override def problem(what: String, pos: xsbti.Position, msg: String, severity: xsbti.Severity, reported: Boolean): Unit = {}
    override def dependencyPhaseCompleted(): Unit = {}
    override def classDependency(onClassName: String, sourceClassName: String, context: xsbti.api.DependencyContext): Unit = {}
    override def generatedLocalClass(source: File, classFile: File): Unit = {}
    override def api(sourceFile: File, classApi: xsbti.api.ClassLike): Unit = {}
    override def usedName(className: String, name: String, useScopes: java.util.EnumSet[xsbti.UseScope]): Unit = {}
  }
}
