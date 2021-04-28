package jonto.utilities;

import javax.annotation.Nonnull;

public class Pair<T1, T2> implements Comparable<Pair<T1, T2>> {
    protected final T1 m_key;
    protected final T2 m_value;

    public Pair(T1 key, T2 value) {
        m_key = key;
        m_value = value;
    }

    public T1 getKey() {
        return m_key;
    }

    public T2 getValue() {
        return m_value;
    }

    public String toString() {
        return ("Key: " + m_key + "\tValue: " + m_value);
    }

    public int compareTo(@Nonnull Pair<T1, T2> p1) {
        if (p1.equals(this)) {
            return 0;
        } else if (p1.hashCode() > this.hashCode()) {
            return 1;
        } else {
            return -1;
        }
    }

    public boolean equals(Object o) {
        return equals((Pair<T1, T2>) o);
    }

    public boolean equals(Pair<T1, T2> p1) {
        if (null != p1) {
            return p1.m_key.equals(this.m_key) && p1.m_value.equals(this.m_value);
        }
        return (false);
    }

    public int hashCode() {
        return (m_key.hashCode() + (31 * m_value.hashCode()));
    }
}
