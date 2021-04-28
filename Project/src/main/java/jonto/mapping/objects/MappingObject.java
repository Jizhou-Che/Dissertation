package jonto.mapping.objects;


public abstract class MappingObject {
    protected int ident_onto1;
    protected int ident_onto2;


    public int getIdentifierOnto1() {
        return ident_onto1;
    }

    public int getIdentifierOnto2() {
        return ident_onto2;
    }


    public void setIdentifierOnto1(int ident) {
        this.ident_onto1 = ident;
    }

    public void setIdentifierOnto2(int ident) {
        this.ident_onto2 = ident;
    }


    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof MappingObject i)) {
            return false;
        }

        return equals(i);
    }


    public boolean equals(MappingObject m) {
        return ident_onto1 == m.getIdentifierOnto1() && ident_onto2 == m.getIdentifierOnto2();
    }

    public String toString() {
        return "<" + ident_onto1 + "==" + ident_onto2 + ">";
    }

    public int hashCode() {
        int code = 10;
        code = 40 * code + ident_onto1;
        code = 40 * code + ident_onto2;
        return code;
    }
}
