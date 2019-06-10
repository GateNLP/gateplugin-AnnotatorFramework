#!/usr/bin/env python
"""
Simple demonstration for a program that tokenises the text it gets from the annotator framework
and returns the annotations as JSON.
"""
import spacy
import json
import sys

nlp = spacy.load("en_core_web_sm")

def token2ann(t):
    ret = {
        "features":{"string":t.text, "lemma":t.lemma_}, 
        "from":t.idx, 
        "to":t.idx+len(t.text), 
        "type":"Token"}
    return ret

def tokenise(text):
    doc = nlp(text)
    toks = [token2ann(t) for t in doc]
    return toks

def send(ret):
    response = json.dumps(ret)
    print(response)
    sys.stdout.flush()

if __name__ == "__main__":
    for line in sys.stdin:
        if line == "STOP":
            break
        try:
            data = json.loads(line)
        except Exception as ex:
            ret = {"status":"error", "error":"Could not parse data as JSON"}
            send(ret)
            break
        tokens = tokenise(data.get("text",""))
        ret = {}
        ret["status"] = "ok"
        ret["anns"] = tokens
        send(ret)


        
