package com.github.dnvriend.sam.serialization.serializer

import com.github.dnvriend.sam.serialization.record.SamRecord
import com.github.dnvriend.sam.serialization.serializer.mock.MockSchemaResolver
import com.github.dnvriend.test.TestSpec
import play.api.libs.json.Json

class SamSerializerTest extends TestSpec {
  val cmkArn = "arn:aws:kms:eu-central-1:123456789:key/5a27910f-7997-41c1-8921-76046e4014d4"

  it should "serialize a value to a sam record" in {
    val value = DefaultInstanceTestDomainPerson()
    val result = SamSerializer.serialize(value, None).value
    result shouldBe SamRecord(
      "com.github.dnvriend.sam.serialization.serializer",
      "DefaultInstanceTestDomainPerson",
      "0EFF14B6C0ABDFD68E194CE74987E1FD61DB8DF2887400D7FBD2AD04DE597806",
      "1F8B080000000000000063600002001DF722C605000000"
    )
  }

  ignore should "serialize a value to a sam record with cmk encryption" in {
    val value = DefaultInstanceTestDomainPerson()
    val result = SamSerializer.serialize(value, Option(cmkArn)).value
    result shouldBe a[SamRecord]
  }

  it should "deserialize a sam record to a value" in {
    val instance = DefaultInstanceTestDomainPerson("dnvriend", 42, DefaultInstanceTestDomainAddress("foostreet", 24), DefaultInstanceTestDomainLuckyNumbers(List(42, 21, 14, 7, 6, 3, 2, 1)))
    val samRecordJson = """{"namespaceName":"com.github.dnvriend.sam.serialization.serializer","schemaName":"DefaultInstanceTestDomainPerson","fingerprint":"0EFF14B6C0ABDFD68E194CE74987E1FD61DB8DF2887400D7FBD2AD04DE597806","payload":"1F8B08000000000000001348C92B2BCA4CCD4B09114ACBCF2F2E294A4D2D311008D192E1E3616361620000955EAA721F000000"}"""
    val samRecord = Json.parse(samRecordJson).as[SamRecord]
    val result = SamSerializer.deserialize[DefaultInstanceTestDomainPerson](samRecord, MockSchemaResolver[DefaultInstanceTestDomainPerson], None)
    result.value shouldBe instance
  }

  it should "deserialize a sam record to a value with invalid cmk payload" in {
    val instance = DefaultInstanceTestDomainPerson()
    val samRecordJson = """{"namespaceName":"com.github.dnvriend.sam.serialization.serializer","schemaName":"DefaultInstanceTestDomainPerson","fingerprint":"0EFF14B6C0ABDFD68E194CE74987E1FD61DB8DF2887400D7FBD2AD04DE597806","payload":"1F8B08000000000000001348C92B2BCA4CCD4B09114ACBCF2F2E294A4D2D311008D192E1E3616361620000955EAA721F000000"}"""
    val samRecord = Json.parse(samRecordJson).as[SamRecord]
    val result = SamSerializer.deserialize[DefaultInstanceTestDomainPerson](samRecord, MockSchemaResolver[DefaultInstanceTestDomainPerson], Option(cmkArn))
    result.value shouldBe instance
  }

  ignore should "deserialize a sam record to a value with valid cmk payload" in {
    val instance = DefaultInstanceTestDomainPerson("dnvriend", 42, DefaultInstanceTestDomainAddress("foostreet", 24), DefaultInstanceTestDomainLuckyNumbers(List(42, 21, 14, 7, 6, 3, 2, 1)))
    val samRecordJson = """{"namespaceName":"com.github.dnvriend.sam.serialization.serializer","schemaName":"DefaultInstanceTestDomainPerson","fingerprint":"0EFF14B6C0ABDFD68E194CE74987E1FD61DB8DF2887400D7FBD2AD04DE597806","payload":"01800378E36A3A0E612069C2D54649D4E0CF9F09005F000100156177732D63727970746F2D7075626C69632D6B657900444167797935537361346363774D504C776B65716E6954756A4E42452B375A656149575756775A65656C654B4F51727645306677755930536A6977514C3668775178773D3D000100076177732D6B6D73004E61726E3A6177733A6B6D733A65752D63656E7472616C2D313A3031353234323237393331343A6B65792F35613237393130662D373939372D343163312D383932312D37363034366534303134643400B801020100786B295698D173E868D743D159529DF2AC4BD7393CB99341EA0033F65CC3D93D26017D5A36F4A47888A0761BAFA54BF92EB60000007E307C06092A864886F70D010706A06F306D020100306806092A864886F70D010701301E060960864801650304012E3011040C1E058FD49493A0CFF9A56701020110803BA93F043AFE4D9F8E3DC4B87C3FF62449F00827845CB32394173C8C1397644B54DC1401B77C3457A9DA4D7EEABDCAE2B1CBBBD84F2C00219039091B02000000000C0000100000000000000000000000000056BAE311A7ADCF87992F902107011B83FFFFFFFF0000000100000000000000000000000100000033F7181E7D13DEF0111B5721AC55CAD49FA526BC763150CF3D52BD0D75337CE2EF4B7BF6508BC472E423EF061BEF643E7D04F5C896B3A59B020A87DBB3BD6A7E909C3D430067306502303A96CEB9E6750472275A0D2B74FA58910B12DEECDFA001AEB50E6B77DF10094BED80F49C30DCCB408B51F4CC47B8C6F4023100F6E998533396FCCA4D4EAEAC7001A581D40471ABF475E142F56BE0C7406E41CFF923D728597A213B060F9CB6DC66F54D"}"""
    val samRecord = Json.parse(samRecordJson).as[SamRecord]
    val result = SamSerializer.deserialize[DefaultInstanceTestDomainPerson](samRecord, MockSchemaResolver[DefaultInstanceTestDomainPerson], Option(cmkArn))
    result.value shouldBe instance
  }
}
