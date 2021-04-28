package jonto.indexing.entities;

import jonto.indexing.labelling.Node;

public abstract class EntityIndex {
    protected int onto_index;

    protected int index;

    protected Node node;

    protected String namespace;

    protected String name4Entitity;
    protected String label4Entity;


    public void setOntologyId(int ontoindex) {
        onto_index = ontoindex;
    }

    public int getOntologyId() {
        return onto_index;
    }

    public boolean equals(String entityname) {
        return name4Entitity.equals(entityname);
    }

    public void setEntityName(String entityName) {
        name4Entitity = entityName;
    }

    public String getEntityName() {
        return name4Entitity;
    }


    public void setLabel(String label) {
        label4Entity = label;
    }

    public String getLabel() {
        return label4Entity;
    }

    public String getIRI(String baseIRI) {
        if (hasDifferentNamespace()) {
            if (namespace.equals(name4Entitity)) {
                return namespace;
            } else {
                if (namespace.endsWith("/")) {
                    return namespace + name4Entitity;
                } else {
                    return namespace + "#" + name4Entitity;
                }
            }
        }
        if (baseIRI.endsWith("/")) {
            return baseIRI + name4Entitity;
        } else {
            return baseIRI + "#" + name4Entitity;
        }
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        if (hasDifferentNamespace()) {
            return namespace;
        } else {
            return "";
        }
    }

    public boolean hasDifferentNamespace() {
        return namespace != null && !namespace.equals("");
    }
}
