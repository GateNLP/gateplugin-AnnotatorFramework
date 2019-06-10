# GATE Annotator Framework Plugin 

This plugin provides processing resources for integrating external annotators into GATE:
* `AF_Program` is a processing resources that allows you to run a program which will receive 
  GATE document data as JSON on standard input and is expected to return annotation information
  as JSON to standard output. 
* `AF_Server` is a processing resource that allows you to connect to a HTTP server and send
  JSON via GET or PUT requests and receive back JSON annotation information.

The basic idea is that in some cases it is probably easiest to wrap the external annotator
(e.g. using Python) to adapt to the JSON used by these PRs, rather than implementing a different
plugin for each external annotator.

* [User Documentation](https://gatenlp.github.io/gateplugin-AnnotatorFramework/)
* [Developer Documentation/Notes](https://github.com/GateNLP/gateplugin-AnnotatorFramework/wiki)


