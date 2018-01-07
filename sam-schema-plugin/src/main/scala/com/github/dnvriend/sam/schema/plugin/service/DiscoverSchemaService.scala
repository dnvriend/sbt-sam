package com.github.dnvriend.sam.schema.plugin
package service

object DiscoverSchemaService {
  def discoverSchemas(
                       classloader: ClassLoader,
                      schemas: Set[String],
                     ): List[SamSchema] = {
    if (schemas.isEmpty) {
      println("No schema definitions found, not executing schema compile script")
      List.empty[SamSchema]
    } else {
      println("Schema definitions found, executing schema compile script")
      val result: Any = RunScriptService.run(classloader)
      result match {
        case xs: Array[_] if xs.isEmpty => List.empty[SamSchema]
        case xs: Array[_] =>
          val ys: List[String] = xs.toList.map(_.toString)
          ys.sliding(2, 2).map { case List(fqcn, schema) => SamSchema(fqcn, schema) }.toList
      }
    }
  }
}
