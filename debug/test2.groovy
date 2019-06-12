import gate.*
import gate.creole.*
import java.io.*

Gate.init()
Gate.getCreoleRegister().registerPlugin(
  new Plugin.Maven("uk.ac.gate.plugins","format-json","8.6"));
exporter = DocumentExporter.getInstance("gate.corpora.export.GATEJsonExporter")

doc = gate.Factory.newDocument("This is a documenti. It contains some text and some annotations.")
defaultSet = doc.getAnnotations()
mySet = doc.getAnnotations("MySet")
gate.Utils.addAnn(defaultSet, 0, 4, "Token", gate.Utils.featureMap("type", "xxx"))
gate.Utils.addAnn(mySet, 5, 7, "MyAnn", gate.Utils.featureMap("key", 12))
System.out.println("Doc: "+doc)
options = gate.Factory.newFeatureMap()
json = ""
new ByteArrayOutputStream().withCloseable { stream ->
  exporter.export(doc, stream, options)  
  json = stream.toString("UTF-8")
}

System.out.println("JSON: "+json)
