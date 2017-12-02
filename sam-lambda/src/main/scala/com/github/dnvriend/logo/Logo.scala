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

package com.github.dnvriend.logo

// http://www.text-image.com/convert/ascii.html
object Logo {
  val samLarge: String = {
    """
      |                                  `..---::---..``
      |                           `-:/ossyyyyyyyyyyyyyysso+/-.
      |                       `-/osyyyyyyyyyyyyyyyyyyyyyyyyysso+:.
      |                    `:+syyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyssso/-
      |                  ./syyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyssssso:`
      |                `+syyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyssssso-
      |               :sysssyyyyyyyysyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyssssss+`
      |              /ssyyhhhhhhhhhhhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyysssssso`
      |             /syhhhhhhhhhhhhhhhhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyssssssss
      |            .shhhhhhhhhhhhhhhhhhhyyyss++++++++ossyyyyyyyyyyyyyssssssh/
      |            +hy+:---/ohhhhhhhhhyyyo+//::::::::///+ssosyyyyyyyysssssshy
      |            --`       .shhhhhs+///////:::::::::///////+oyyyyyysssssyhh.
      |                       `hhhs+/:--/////:::::::::////:--//+ssyyysssssyhh-
      |                        ys+//::///////::::::::://////::://+sysssssyhhh-
      |                       -s+///++//////:++:yo//h+://////++///+sssssshhhh`
      |                      .y+++++/////////ys+h+s+d/:////////+++++sssshhhho
      |                     :yy++++/:::://///os+sos/oo/////::::/++++ssshhhhh.
      |                   .ohhs++++/++++++++++++++++++++++++++++++++syhhhhh/
      |                 `/yhhhy+++++++///////////////////////+++++++yhhhhh+
      |                -shhhhhys+/////////////////////////////////oyhhhhh/
      |              .ohhhhhysyy//////++///////////////////++/////+hhhhy:
      |             :yhhhhysyyyo//////-......--:///:-......--//////hhh+`
      |            +hhhhysyyyyyo///:............:/-...........-:///s+.
      |           ohhhysyyyyyyys///-....:odo.....-....-/hy:....-///`    `.-.------
      |          +hhysyyyyyyyyys///.....sNNd........../mNN/....-///     .-:::::::-
      |         :hhysyyyyyyyyss+///-....-:/-...........:/:.....://:`    `://:-///:
      |        `yhyyyyyyyyyys-.------.........:::::::.........-----.`   .+//-.-//:
      |        :hyyyyyyyyyyy+.`...............-////:-...............``  .://-/-:/:
      |        +yyyyyyyyyyyyy:`.................-:-................``   .:///////:
      |        oyyyyyyyyyyyyys+.............:::-.``-::-...........``   .::-......`
      |        oyyyyyyyyyyyyyyyso/.`.........-::///:--.........```  `.-:::.
      |        +yyyyyyyyyyyyyyyyy-  `:::::::---/---/:--::::::-` ``.--:::::`
      |        -yyyyyyyyyyyyyyyy+  `-//////////oo+os+/////////::::::::::::
      |         oyyyyyyyyyyyyyyy: `::::////::--/ysy+--://///////::::::::.`
      |         `syyyyyyyyyyyyyy+`-:::::::-....-oss-....-://////:::/:-`
      |          .syyyyyyyyyyyyyy/:::::::--....-://+/:....:///////:-`
      |           `oyyyyyyyyyysso//:::::::::--:oo:/+oo+-...://///:`
      |             :syyyyyys+::::::/::----:::dhyys+:+++....//:::::--`
      |               :syyyy/::::::::/:....///h/hyy+-///-...::::::::::`
      |                 ./sy:::::::::::..../oo+/oo+:.---...-/:::::::::.
      |                    .-:::::::::/:..../+oo+/--.......:::::::::::.
      |                     .::::::::::/:....--///--.....-:::::::::::-
      |                      .::::::::::/:--...........-://:::::::::-`
      |                       `-::::::::////::-------::///:::::::::.
      |                         .-::::::://///////////////:::::::-`
      |                           ://////:..---:::::---.-///////.
      |                           ........               .......`
      |
    """.stripMargin
  }

  def main(args: Array[String]): Unit = {
    println(samLarge)
  }
}
