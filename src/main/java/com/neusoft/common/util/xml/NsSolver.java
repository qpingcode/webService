package com.neusoft.common.util.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class NsSolver implements NamespaceContext {

    private Map<String,String> prefix2uri;
    private Map<String,String> uri2prefix;

    public NsSolver(){
        if(prefix2uri == null){
            prefix2uri = Collections.synchronizedMap(new HashMap<String,String>());
        }
        if(uri2prefix == null){
            uri2prefix = Collections.synchronizedMap(new HashMap<String,String>());
        }
    }


    public NsSolver(Map<String,String> prefix2uri,Map<String,String> uri2prefix){
        this.prefix2uri = prefix2uri == null ? Collections.synchronizedMap(new HashMap<String,String>()) : Collections.synchronizedMap(prefix2uri);
        this.uri2prefix = uri2prefix == null ? Collections.synchronizedMap(new HashMap<String,String>()) : Collections.synchronizedMap(uri2prefix);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        // TODO Auto-generated method stub
        return prefix2uri.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        // TODO Auto-generated method stub
        return uri2prefix.get(namespaceURI);
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        // TODO Auto-generated method stub
        return prefix2uri.keySet().iterator();
    }

}