package com.github.dnvriend.sam.schema.plugin.service

import javax.script.ScriptEngineManager

object RunScriptService {
  def run(classLoader: ClassLoader): Any = {
    val engine = new ScriptEngineManager().getEngineByName("nashorn")
    engine.put("script", classLoader.loadClass("Script").newInstance())
    engine.eval("script.run();")
  }
}
