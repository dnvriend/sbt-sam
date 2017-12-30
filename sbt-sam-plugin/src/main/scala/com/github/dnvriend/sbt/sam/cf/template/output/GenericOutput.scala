package com.github.dnvriend.sbt.sam.cf.template.output
import com.github.dnvriend.sbt.sam.cf.template.Output
import play.api.libs.json.{JsValue, Json, Writes}

object GenericOutput {
  implicit val writes: Writes[GenericOutput] = Writes.apply(model => {
    import model._
    // The logical ID must be alphanumeric (a-z, A-Z, 0-9) and unique within the template.
    val logicalId: String = s"${name.capitalize.replace("-", "")}Output"
    Json.obj(
      logicalId -> Json.obj(
        "Description" -> description,
        "Value" -> value,
        "Export" -> Json.obj(
          "Name" -> name
        )
      )
    )
  })
}

case class GenericOutput(
                          /**
                            * A String type that describes the output value. The description can be a maximum of 4 K in length.
                            */
                          description: String,

                          /**
                            * The name of the resource output to be exported for a cross-stack reference.
                            */
                          name: String,
                          /**
                            * The value of the property returned by the aws CloudFormation describe-stacks command.
                            * The value of an output can include literals, parameter references, pseudo-parameters,
                            * a mapping value, or intrinsic functions.
                            */
                          value: JsValue,
                        ) extends Output
