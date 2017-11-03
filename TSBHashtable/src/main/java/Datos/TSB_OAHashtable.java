package Datos;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class TSB_OAHashtable<K,V> implements Map<K,V>, Cloneable, Serializable
{

    private int count;
    private Entry<K,V> vector[];
    private float loadFactor;

    public TSB_OAHashtable()
    {
        this(5, 0.8f);
    }

    public TSB_OAHashtable(int initial_capacity)
    {
        this(initial_capacity, 0.8f);
    }

    public TSB_OAHashtable(int initial_capacity, float loadFactor)
    {
        if(loadFactor <= 0) { loadFactor = 0.8f; }
        if(initial_capacity <= 0) { initial_capacity = 11; }

        this.vector = new Entry[initial_capacity];
        count = 0;
    }

            /*
     * Clase interna que representa los pares de objetos que se almacenan en la
     * tabla hash: son instancias de esta clase las que realmente se guardan en 
     * en cada una de las listas del arreglo table que se usa como soporte de 
     * la tabla. Lanzará una IllegalArgumentException si alguno de los dos 
     * parámetros es null.
     */
    public boolean esTumba(int i)
        {
           return (vector[i].getValue()==null);
        }
    
    
    private class Entry<K, V> implements Map.Entry<K, V>
    {
        private K key;
        private V value;
        
        public Entry(K key, V value) 
        {
            if(key == null || value == null)
            {
                throw new IllegalArgumentException("Entry(): parámetro null...");
            }
            this.key = key;
            this.value = value;
        }
        
        
        @Override
        public K getKey() 
        {
            return key;
        }

        @Override
        public V getValue() 
        {
            return value;
        }

        @Override
        public V setValue(V value) 
        {
            if(value == null) 
            {
                throw new IllegalArgumentException("setValue(): parámetro null...");
            }
                
            V old = this.value;
            this.value = value;
            return old;
        }
       
        @Override
        public int hashCode() 
        {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);            
            return hash;
        }

        @Override
        public boolean equals(Object obj) 
        {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (this.getClass() != obj.getClass()) { return false; }
            
            final Entry other = (Entry) obj;
            if (!Objects.equals(this.key, other.key)) { return false; }
            if (!Objects.equals(this.value, other.value)) { return false; }            
            return true;
        }       
        
        @Override
        public String toString()
        {
            return "(" + key.toString() + ", " + value.toString() + ")";
        }
    }
    @Override
    public int size() {
        return this.count;
    }

    @Override
    public boolean isEmpty() {
        return (this.count == 0);
    }

    @Override
    public boolean containsKey(Object key) {
        return(buscar(key) != -1);
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < vector.length; i++)
        {
            if (vector[i].getValue().equals(value))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key)
    {
        int i =buscar(key);
        if (i != -1)
        {
            return vector[i].getValue();
        }
        else return null;

    }

    private int buscar(Object key)
    {
      if(key == null) throw new NullPointerException("get(): parámetro null");       
       int i = this.h(key.hashCode());       
       while(vector[i]!=null)
       {
           if(vector[i].key.equals(key))
           {
                return i;
           }
           else
           {
               i++;
           }
       }       
       return -1;
    }



    @Override
    public V put(K key, V value)
    {
        if(key == null || value == null) throw new NullPointerException("put(): parámetro null");

        int i = this.h(key);

        while (esTumba(i) || vector[i] == null)
        {
            if ((i + 1) == vector.length)
            {
                i = 0;
            }
            else
            {
                i++;
            }
        }
        vector[i] = new Entry<>(key, value);

        if (count >= vector.length*loadFactor)
        {
            rehash();
        }
        return value;
    }


    /*
     * Función hash. Toma una clave entera k y calcula y retorna un índice
     * válido para esa clave para entrar en la tabla.
     */
    private int h(int k)
    {
        return h(k, this.vector.length);
    }


    /*
     * Función hash. Toma un objeto key que representa una clave y calcula y
     * retorna un índice válido para esa clave para entrar en la tabla.
     */
    private int h(K key)
    {
        return h(key.hashCode(), this.vector.length);
    }

    /*
     * Función hash. Toma un objeto key que representa una clave y un tamaño de
     * tabla t, y calcula y retorna un índice válido para esa clave dedo ese
     * tamaño.
     */
    private int h(K key, int t)
    {
        return h(key.hashCode(), t);
    }

    /*
     * Función hash. Toma una clave entera k y un tamaño de tabla t, y calcula y
     * retorna un índice válido para esa clave dado ese tamaño.
     */
    private int h(int k, int t)
    {
        if(k < 0) k *= -1;
        return k % t;
    }




    @Override
    public V remove(Object key) 
    {
        int indice = buscar(key);
        
         if (-1==indice)
        return null;
         
        V v=vector[indice].getValue();
       
        vector[indice].setValue(null);
        return v;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(Map.Entry<? extends K, ? extends V> e : m.entrySet())
        {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        int cant= vector.length;
        this.vector = new Entry[cant];
        

    }

    @Override
    public Set<K> keySet() {

        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return null;
    }

}
