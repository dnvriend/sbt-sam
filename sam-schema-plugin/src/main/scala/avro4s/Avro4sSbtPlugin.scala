package avro4s

import java.nio.file.{ Files, Paths }

import org.apache.avro.Protocol
import org.apache.avro.Schema.{ Type => AvroType }
import sbt.Keys._
import sbt._
import sbt.plugins._

import scala.collection.JavaConverters._

object Import {

  lazy val avro2Class = taskKey[Seq[File]]("Generate case classes from avro schema files; is a source generator")
  lazy val avroIdl2Avro = taskKey[Seq[File]]("Generate avro schema files from avro IDL; is a resource generator")

  object Avro4sKeys {
    val avroDirectoryName = SettingKey[String]("Recurrent directory name used for lookup and output")
    val avroFileEnding = SettingKey[String]("File ending of avro schema files, used for lookup and output")
    val avroIdlFileEnding = SettingKey[String]("File ending of avro IDL files, used for lookup and output")
  }

}

/** @author Stephen Samuel, Timo Merlin Zint */
object Avro4sSbtPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = JvmPlugin // avoid override of sourceGenerators

  val autoImport = Import

  import autoImport._
  import Avro4sKeys._

  override def projectSettings = Seq(
    avroDirectoryName := "avro",
    avroFileEnding := "avsc",
    avroIdlFileEnding := "avdl",

    includeFilter in avro2Class := s"*.${avroFileEnding.value}",
    excludeFilter in avro2Class := HiddenFileFilter || FileFilter.globFilter("_*"),
    includeFilter in avroIdl2Avro := s"*.${avroIdlFileEnding.value}",
    excludeFilter in avroIdl2Avro := HiddenFileFilter || FileFilter.globFilter("_*"),

    resourceDirectory in avro2Class := (resourceDirectory in Compile).value / avroDirectoryName.value,
    resourceDirectories in avro2Class := Seq(
      (resourceDirectory in avro2Class).value,
      (resourceManaged in avroIdl2Avro).value),
    resources in avro2Class := (resourceDirectories in avro2Class).value.flatMap(getRecursiveListOfFiles),

    sourceManaged in avro2Class := (sourceManaged in Compile).value / avroDirectoryName.value,

    resourceDirectory in avroIdl2Avro := (resourceDirectory in Compile).value / avroDirectoryName.value,
    resourceManaged in avroIdl2Avro := (resourceManaged in Compile).value / avroDirectoryName.value,
    resources in avroIdl2Avro := getRecursiveListOfFiles((resourceDirectory in avroIdl2Avro).value),

    managedSourceDirectories in Compile += (sourceManaged in avro2Class).value,
    managedResourceDirectories in Compile += (resourceManaged in avroIdl2Avro).value,

    avro2Class := runAvro2Class.value,
    avroIdl2Avro := runAvroIdl2Avro.value,

    sourceGenerators in Compile += avro2Class.taskValue,
    resourceGenerators in Compile += avroIdl2Avro.taskValue)

  private def runAvro2Class: Def.Initialize[Task[Seq[File]]] = Def.task {

    val inc = (includeFilter in avro2Class).value
    val exc = (excludeFilter in avro2Class).value || DirectoryFilter
    val inDir = (resourceDirectories in avro2Class).value
    val outDir = (sourceManaged in avro2Class).value

    streams.value.log.debug(s"[sbt-avro4s] Generating sources from [${inDir}]")
    streams.value.log.debug("--------------------------------------------------------------")

    val combinedFileFilter = inc -- exc
    val allFiles = (resources in avro2Class).value
    val schemaFiles = Option(allFiles.filter(combinedFileFilter.accept))
    streams.value.log.debug(s"[sbt-avro4s] Found ${schemaFiles.fold(0)(_.length)} schemas")
    schemaFiles.map { f =>
      val defs = ModuleGenerator.fromFiles(f)
      streams.value.log.debug(s"[sbt-avro4s] Generated ${defs.length} classes")

      val paths = FileRenderer.render(outDir.toPath, TemplateGenerator.apply(defs))
      streams.value.log.debug(s"[sbt-avro4s] Wrote class files to [${outDir.toPath}]")

      paths
    }.getOrElse(Seq()).map(_.toFile)
  } dependsOn avroIdl2Avro

  private def runAvroIdl2Avro: Def.Initialize[Task[Seq[File]]] = Def.task {
    import org.apache.avro.compiler.idl.Idl
    val inc = (includeFilter in avroIdl2Avro).value
    val exc = (excludeFilter in avroIdl2Avro).value || DirectoryFilter
    val inDir = (resourceDirectory in avroIdl2Avro).value
    val outDir = (resourceManaged in avroIdl2Avro).value
    val outExt = s".${avroFileEnding.value}"

    streams.value.log.debug(s"[sbt-avro4s] Generating sources from [${inDir}]")
    streams.value.log.debug("--------------------------------------------------------------")

    val combinedFileFilter = inc -- exc
    val allFiles = (resources in avroIdl2Avro).value
    val idlFiles = Option(allFiles.filter(combinedFileFilter.accept))
    streams.value.log.debug(s"[sbt-avro4s] Found ${idlFiles.fold(0)(_.length)} IDLs")

    val schemata = idlFiles.map { f =>
      f.flatMap(file => {
        val idl = new Idl(file.getAbsoluteFile)
        val protocol: Protocol = idl.CompilationUnit()
        val protocolSchemata = protocol.getTypes
        idl.close()
        protocolSchemata.asScala
      }).toSeq
    }.getOrElse(Seq())

    val uniqueSchemata = schemata.groupBy(_.getFullName).mapValues { identicalSchemata =>
      val referenceSchema = identicalSchemata.head
      identicalSchemata.foreach { schema =>
        require(referenceSchema.equals(schema), s"Different schemata with name ${referenceSchema.getFullName} found")
      }
      referenceSchema
    }.values

    streams.value.log.debug(s"[sbt-avro4s] Generated ${uniqueSchemata.size} unique schema(-ta)")

    Files.createDirectories(outDir.toPath)
    val schemaFiles = (for (s <- uniqueSchemata if s.getType == AvroType.RECORD) yield {
      val path = Paths.get(outDir.absolutePath, s.getFullName + outExt)
      val writer = Files.newBufferedWriter(path)
      writer.write(s.toString(true))
      writer.close()
      path.toFile
    }).toSeq

    streams.value.log.debug(s"[sbt-avro4s] Wrote schema(-ta) to [${outDir.toPath}]")
    schemaFiles
  }

  def getRecursiveListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    if (these == null)
      Array.empty[File]
    else
      these ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
  }
}
