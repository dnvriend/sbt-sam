package com.github.dnvriend.sam.serialization.serializer

case class DefaultInstanceTestDomainPerson(
                                      name: String = "",

                                      age: Int = 0,

                                      address: DefaultInstanceTestDomainAddress = DefaultInstanceTestDomainAddress(),

                                      luckyNumbers: DefaultInstanceTestDomainLuckyNumbers = DefaultInstanceTestDomainLuckyNumbers(),
                                    )

case class DefaultInstanceTestDomainAddress(

                                           name: String = "",

                                           houseNr: Int = 0,
                                           )

case class DefaultInstanceTestDomainLuckyNumbers(

                                                luckyNumbers: List[Int] = List.empty[Int],

                                                )
