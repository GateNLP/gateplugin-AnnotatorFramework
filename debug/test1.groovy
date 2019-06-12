@Grab('uk.ac.gate:gate-core:8.6') import gate.util.persistence.PersistenceManager
@Grab('uk.ac.gate.plugins:format-json:8.6') 
import gate.*
import gate.creole.*
import java.io.File

Gate.init()
Gate.getCreoleRegister().registerPlugin(
  new Plugin.Maven("uk.ac.gate.plugins","annie","8.6-SNAPSHOT"));
exporter = DocumentExporter.getInstance("gate.corpora.export.GATEJsonExporter")

doc = gate.Factory.newDocument("This is a document")
options = gate.Factory.newFeatureMap()
json = ""
new ByteArrayOutputStream().withOutputStream { stream ->
  exporter.export(doc, dest, options)  
  json = stream.toString("UTF-8")
}

System.out.println("JSON: "+json)
