// @GrabResolver(name='gate-snapshots', root='http://repo.gate.ac.uk/content/groups/public/')
// @Grab('uk.ac.gate:gate-core:8.6-SNAPSHOT')
import gate.*
import gate.creole.*
Gate.init()
Gate.getCreoleRegister().registerPlugin(
  new Plugin.Maven("uk.ac.gate.plugins","format-json","8.6"));
