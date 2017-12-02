// Copyright 2017 Dennis Vriend
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.dnvriend.ops

import com.sksamuel.avro4s._
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType
import org.apache.avro.file.SeekableByteArrayInput
import org.apache.avro.{ Schema, SchemaCompatibility, SchemaNormalization, SchemaValidatorBuilder }

import scala.util.Try
import scalaz._
import scalaz.Scalaz._

object AvroOps extends AvroOps

trait AvroOps {
  implicit def ToAvroSerializeOps[A <: Product: SchemaFor: ToRecord](that: A): ToAvroSerializeOps[A] = new ToAvroSerializeOps(that)
  implicit def ToAvroDeserializeOps[A <: Product](that: Array[Byte] @@ AvroBinary): ToAvroDeSerializeOps = new ToAvroDeSerializeOps(that)
  implicit def ToAvroDeserializeStringOps(that: String): ToAvroDeSerializeStringOps = new ToAvroDeSerializeStringOps(that)
  implicit def ToAvroSchemaOps(that: Schema): ToAvroSchemaOps = new ToAvroSchemaOps(that)
  implicit def ToAvroStringOps(that: String): ToAvroStringOps = new ToAvroStringOps(that)
  implicit def ToAvroBase64StringOps(that: String @@ Base64): ToAvroBase64StringOps = new ToAvroBase64StringOps(that)
  implicit def ToAvroHexStringOps(that: String @@ Hex): ToAvroHexStringOps = new ToAvroHexStringOps(that)
  implicit def ToAvroBinaryUnwrapOps(that: Array[Byte] @@ AvroBinary): ToAvroBinaryUnwrapOps = new ToAvroBinaryUnwrapOps(that)
  implicit def ToAvroJsonUnwrapOps(that: Array[Byte] @@ AvroJson): ToAvroJsonUnwrapOps = new ToAvroJsonUnwrapOps(that)
  implicit def ToAvroTagOps(that: Array[Byte]): ToAvroTagOps = new ToAvroTagOps(that)

  /**
   * Returns a SHA-256 fingerprint for the schema generated from the product type
   */
  def fingerPrintFor[A <: Product](implicit schemaFor: SchemaFor[A]): Array[Byte] = {
    SchemaNormalization.parsingFingerprint("SHA-256", schemaFor())
  }

  /**
   * Returns the [[org.apache.avro.Schema]] for the given product type
   */
  def schemaFor[A <: Product](implicit schemaFor: SchemaFor[A]): Schema = schemaFor()

  /**
   * Checks whether the product types are full compatible
   */
  def checkCanReadWith[R <: Product, W <: Product](implicit readerSchema: SchemaFor[R], writerSchema: SchemaFor[W]): Validation[Throwable, Schema] = {
    val result = SchemaCompatibility.checkReaderWriterCompatibility(readerSchema(), writerSchema())
    Validation.lift(readerSchema())(_ => result.getType != SchemaCompatibilityType.COMPATIBLE, new Error(result.getDescription))
  }

  /**
   *
   */
  def checkFullCompatibility[R <: Product](existingSchemas: Schema*)(implicit readerSchema: SchemaFor[R]): Disjunction[Throwable, Schema] = Disjunction.fromTryCatchNonFatal {
    import scala.collection.JavaConverters._
    new SchemaValidatorBuilder()
      .mutualReadStrategy()
      .validateAll()
      .validate(readerSchema(), List(existingSchemas: _*).asJava)

    readerSchema()
  }
}

class ToAvroSerializeOps[A <: Product: ToRecord](that: A)(implicit schemaForA: SchemaFor[A]) {
  def toAvroBinary(implicit encoder: Converter[A, Array[Byte] @@ AvroBinary]): Array[Byte] @@ AvroBinary = {
    encoder(that)
  }

  def toAvroJson(implicit encoder: Converter[A, Array[Byte] @@ AvroJson]): Array[Byte] @@ AvroJson = {
    encoder(that)
  }

  def to[B <: Product: SchemaFor: FromRecord](implicit converter: Converter[A, Try[B]]): Disjunction[Throwable, B] = {
    converter(that).toDisjunction
  }
}

class ToAvroDeSerializeStringOps(that: String) extends StringOps with AvroOps {
  //  def parseAvroBinary[R <: Product: SchemaFor: FromRecord, W <: Product: SchemaFor]: Disjunction[Throwable, R] = {
  //    that.parseHex.parseAvroBinary[R, W]
  //  }
  //
  //  def parseAvroBinary[R <: Product: SchemaFor: FromRecord](writerSchema: Schema): Disjunction[Throwable, R] = {
  //    that.parseHex.parseAvroBinary[R](writerSchema)
  //  }
  //
  //  def parseAvroJson[R <: Product: SchemaFor: FromRecord, W <: Product: SchemaFor]: Disjunction[Throwable, R] = {
  //    that.parseHex.parseAvroJson[R, W]
  //  }
  //
  //  def parseAvroJson[R <: Product: FromRecord: SchemaFor](writerSchema: Schema): Disjunction[Throwable, R] = {
  //    that.parseHex.parseAvroJson[R](writerSchema)
  //  }
}

class ToAvroDeSerializeOps(bytes: Array[Byte] @@ AvroBinary) {
  def parseAvro[R <: Product: SchemaFor: FromRecord, W <: Product](implicit writerSchemaFor: SchemaFor[W]): Disjunction[Throwable, R] = {
    //    parseAvro[R](writerSchemaFor())
    ???
  }

  def parseAvro[R <: Product: FromRecord: SchemaFor](writerSchema: Schema)(decoder: Converter2[Schema, Array[Byte] @@ AvroBinary, Try[R]]): Disjunction[Throwable, R] = {
    decoder(writerSchema, bytes).toDisjunction
  }

  def parseAvroJson[R <: Product: SchemaFor: FromRecord, W <: Product](implicit writerSchemaFor: SchemaFor[W]): Disjunction[Throwable, R] = {
    parseAvroJson[R](writerSchemaFor())
  }

  def parseAvroJson[R <: Product: FromRecord](writerSchema: Schema)(implicit readerSchemaFor: SchemaFor[R]): Disjunction[Throwable, R] = {
    AvroJsonInputStream[R](new SeekableByteArrayInput(Tag.unwrap(bytes)), Option(writerSchema), Option(readerSchemaFor())).tryIterator().next().toDisjunction
  }
}

class ToAvroStringOps(that: String) {
  def parseAvroSchemaFromString: Schema = {
    new Schema.Parser().parse(that)
  }
}
class ToAvroBase64StringOps(that: String @@ Base64) extends StringOps with ByteArrayOps {
  def parseAvroSchemaFromBase64: Schema = {
    new Schema.Parser().parse(that.parseBase64.toInputStream)
  }
}

class ToAvroHexStringOps(that: String @@ Hex) extends StringOps with ByteArrayOps {
  def parseAvroSchemaFromHex: Schema = {
    new Schema.Parser().parse(that.parseHex.toInputStream)
  }
}

class ToAvroSchemaOps(that: Schema) extends StringOps {
  def fingerprint: Array[Byte] @@ SHA256 = {
    Tag(SchemaNormalization.parsingFingerprint("SHA-256", that))
  }
  def toUtf8Array: Array[Byte] @@ UTF8 = {
    that.toString(false).toUtf8Array
  }
}

class ToAvroBinaryUnwrapOps(that: Array[Byte] @@ AvroBinary) {
  def unwrap: Array[Byte] = Tag.unwrap(that)
}
class ToAvroJsonUnwrapOps(that: Array[Byte] @@ AvroJson) {
  def unwrap: Array[Byte] = Tag.unwrap(that)
}
class ToAvroTagOps(that: Array[Byte]) {
  def tagAvroBinary: Array[Byte] @@ AvroBinary = Tag(that)
  def tagAvroJson: Array[Byte] @@ AvroJson = Tag(that)
}