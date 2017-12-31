package com.github.dnvriend.sbt.sam.cf.template.output

import com.github.dnvriend.sbt.sam.cf.template.Output
import com.github.dnvriend.sbt.sam.cf.{ CloudFormation, PseudoParameters }
import play.api.libs.json.{ Json, Writes }

object ServerlessApiOutput {
  implicit val writes: Writes[ServerlessApiOutput] = Writes.apply(model => {
    import model._
    val stageLogicalName = s"ServerlessRestApi${stage}Stage"
    Json.obj(
      "ServiceEndpoint" -> Json.obj(
        "Description" -> "URL of the service endpoint",
        "Value" -> Json.obj(
          "Fn::Join" -> Json.arr(
            "",
            Json.arr(
              "https://",
              CloudFormation.ref("ServerlessRestApi"),
              ".execute-api.",
              PseudoParameters.ref(PseudoParameters.Region),
              ".",
              PseudoParameters.ref(PseudoParameters.URLSuffix),
              "/",
              CloudFormation.ref(stageLogicalName)
            )
          )
        )
      )
    )
  })
}
case class ServerlessApiOutput(stage: String) extends Output