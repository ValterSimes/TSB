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

    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;


    public TSB_OAHashtable()
    {
        this(11, 0.5f);
    }

    public TSB_OAHashtable(int initial_capacity)
    {
        this(initial_capacity, 0.5f);
    }

    public TSB_OAHashtable(int initial_capacity, float loadFactor)
    {
        if(loadFactor <= 0) { loadFactor = 0.5f; }
        if(initial_capacity <= 0) { initial_capacity = 11; }

        this.vector = new Entry[initial_capacity];
        count = 0;
        this.loadFactor = loadFactor;
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

    public boolean contains(Object value) {
        return containsValue(value);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) return false;
        for (int i = 0; i < vector.length; i++)
        {
            if (vector[i] != null) {
                if (vector[i].getValue().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get(Object key)
    {
        int i = buscar(key);
        if (i != -1)
        {
            return vector[i].getValue();
        }
        else return null;

    }
    private void rehash()
    {
        Entry<K,V> temp[] = this.vector;
        int nuevoTamano = this.siguientePrimo(temp.length * 2 + 1);
        this.vector = new Entry[nuevoTamano]; //nuevo vector (borra lo que tenia)

        this.count = 0;
        for (int i = 0; i < temp.length; i++) //pone de nuevo en vector con nuevo tamaño lo del auxiliar sin null o tumba
        {
            if(temp[i] != null && !temp[i].esTumba())
            {
             this.put(temp[i].getKey(),temp[i].getValue());
            }
        }
    }

    /**
     * Hashcode para el array entero
     */
    @Override
    public int hashCode()
    {
        if(this.isEmpty()) {return 0;}

        int hc = 0;
        for(Map.Entry<K, V> entry : this.entrySet())
        {
            hc += (entry).hashCode();
        }

        return hc;
    }

    /**
     * Copia superficial del vector.
     * Los elementos del arreglo no son objetos nuevos, usan las mismas referencias que el original.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        TSB_OAHashtable<K, V> t = (TSB_OAHashtable<K, V>)super.clone();
        t.vector = new Entry[vector.length];
        for (int i = vector.length; i-- > 0; ) {
            t.vector[i] = vector[i];
        }
        t.keySet = null;
        t.entrySet = null;
        t.values = null;

        return t;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Map)) { return false; }

        Map<K, V> t = (Map<K, V>) obj;
        if(t.size() != this.size()) { return false; }

        try
        {
            Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
            while(i.hasNext())
            {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if(t.get(key) == null) { return false; }
                else
                {
                    if(!value.equals(t.get(key))) { return false; }
                }
            }
        }

        catch (ClassCastException | NullPointerException e)
        {
            return false;
        }

        return true;

    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<K, V> v : vector) {
            if (v != null && !v.esTumba()) {
                sb.append(v.toString() + "\n");
            }
        }
        return sb.toString();
    }

    /**
     * Busca por una key
     * @param key
     * @return el índice del arreglo en donde está el Entry
     */
    private int buscar(Object key) {
        if (key == null) throw new NullPointerException("get(): parámetro null");
        int indexMadre = this.h(key.hashCode());


        for (int j = 0; ; j++) {
            int index = this.h(indexMadre + (j * j));

            if (vector[index] == null) return -1; // si hay una abierta, entonces no lo encontró...
            if (vector[index].key.equals(key) && !vector[index].esTumba()) return index;

            // TODO: que pasa si son todos tumbas? da vueltas infinitas? fijarse si hay q contemplarlo o no.
        }
    }



    @Override
    public V put(K key, V value)
    {
        if(key == null || value == null) throw new NullPointerException("put(): parámetro null");

        if (count + 1 >= vector.length*loadFactor) rehash();

        int indexPrimerTumba = -1;

        int indexMadre = TSB_OAHashtable.this.h(key);

        for (int j = 0;; j++)
        {
            int index = h(indexMadre + (j * j));

            // La casilla no está abierta
            if (vector[index] != null) {

                // La casilla está cerrada...
                if (!vector[index].esTumba()) {

                    // Los key son iguales
                    if (vector[index].key.equals(key)) {
                        // Devuelvo el viejo, y asigno el valor nuevo en esa casilla
                        V temp = vector[index].getValue();
                        vector[index].setValue(value);
                        return temp;
                    }
                }
                else {
                    // Es tumba
                    if (indexPrimerTumba == -1) indexPrimerTumba = index;
                }


            }
            else {
                // Está abierta...

                // TODO: grego
/*
                // Si se encontró una primera tumba, la guardo en ese lugar
                if (indexPrimerTumba != -1) {
                    vector[indexPrimerTumba] = new Entry<>(key, value);
                    count++;
                    return null;
                }
                else {
                    // Si no había tumba anterior, lo guarda en la pos del abierto (ésta)
                    vector[index] = new Entry<>(key, value);
                    count++;
                    return null;
                }*/

                // Lo agrega en la primera tumba o en la abierta encontrada, según corresponda
                vector[indexPrimerTumba != -1 ? indexPrimerTumba : index] = new Entry<>(key, value);
                count++;
                return null;

            }
        }


    }

    private int siguientePrimo(int  n)
    {
        if ( n % 2  == 0)
            n++;
        for ( ; !esPrimo(n); n+=2 ) ;
        return n;
    }

    private boolean esPrimo(int n)
    {
        for (int i = 3; i <= Math.sqrt(n); i++) {
            if (n%i == 0) {return false;}

        }
        return true;
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
    public V remove(Object key) {
        int indice = buscar(key);
        if (-1 == indice) return null;

        // Lo encontro y retorna el value
        V v = vector[indice].getValue();

        // Lo hace tumba
        vector[indice].hacerTumba();
        count--;
        return v;
    }

    /**
     * Pasa un mapa por parámetro y agrega todos sus valores al vector
     */
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
        this.count = 0;
    }

    @Override
    public Set<K> keySet() {
        if(keySet == null)
        {
            keySet = new KeySet();
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        if(values==null)
        {
            values = new ValueCollection();
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        if(this.entrySet == null)
        {
            this.entrySet = new EntrySet();
        }
        return entrySet;
    }


    /*
     * Clase interna que representa los pares de objetos que se almacenan en la
     * tabla hash: son instancias de esta clase las que realmente se guardan en
     * en cada una de las listas del arreglo table que se usa como soporte de
     * la tabla. Lanzará una IllegalArgumentException si alguno de los dos
     * parámetros es null.
     */
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

        private boolean esTumba()
        {
            return (value == null);
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

        /**
         * Convierte una Entry en tumba
         */
        private void hacerTumba() {
            value = null;
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


    /*
     * Clase interna que representa una vista de todas los Claves mapeadas en la
     * tabla: si la vista cambia, cambia también la tabla que le da respaldo, y
     * viceversa. La vista es stateless: no mantiene estado alguno (es decir, no
     * contiene datos ella misma, sino que accede y gestiona directamente datos
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar todo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class KeySet extends AbstractSet<K>
    {
        @Override
        public Iterator<K> iterator()
        {
            return new KeySetIterator();
        }

        @Override
        public int size()
        {
            return TSB_OAHashtable.this.count;
        }

        @Override
        public boolean contains(Object o)
        {
            return TSB_OAHashtable.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o)
        {
            return (TSB_OAHashtable.this.remove(o) != null);
        }

        @Override
        public void clear()
        {
            TSB_OAHashtable.this.clear();
        }

        private class KeySetIterator implements Iterator<K> {

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            private int current_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public KeySetIterator() {

                current_entry = -1;
                next_ok = false;

            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext() {
                // variable auxiliar t para simplificar accesos...
                Entry<K, V> t[] = TSB_OAHashtable.this.vector;

                if (TSB_OAHashtable.this.isEmpty()) {
                    return false;
                }
                if (current_entry >= t.length) {
                    return false;
                }

                // Variable temporal para ver si existe un siguiente
                int tempEntry = current_entry;
                do {
                    tempEntry++;

                } while (tempEntry < t.length && (t[tempEntry] == null || t[tempEntry].esTumba()));

                return (tempEntry < t.length);
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public K next() {

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                Entry<K, V> t[] = TSB_OAHashtable.this.vector;

                do {
                    current_entry++;

                } while (current_entry < t.length && (t[current_entry] == null || t[current_entry].esTumba()));

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la clave del elemento alcanzado...
                K key = t[current_entry].getKey();
                return key;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                Entry<K, V> t[] = TSB_OAHashtable.this.vector;

                // eliminar el objeto que retornó next() la última vez...
                Entry<K, V> eliminado = t[current_entry];
                TSB_OAHashtable.this.remove(eliminado.getKey());

                // quedar apuntando al anterior al que se retornó...
                do {
                    current_entry--;

                } while (current_entry >= 0 && (t[current_entry] == null || t[current_entry].esTumba()));

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSB_OAHashtable.this.count--;

            }
        }
    }

    private class ValueCollection extends AbstractCollection<V> {

        @Override
        public Iterator<V> iterator() {
            return new ValueCollectionIterator();
        }

        @Override
        public int size() {
            return TSB_OAHashtable.this.count;
        }

        @Override
        public boolean contains(Object o) {
            return TSB_OAHashtable.this.containsValue(o);
        }

        @Override
        public void clear() {
            TSB_OAHashtable.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V> {

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            private int current_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public ValueCollectionIterator() {

                current_entry = -1;
                next_ok = false;

            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext() {
                // variable auxiliar t para simplificar accesos...
                Entry<K, V> t[] = TSB_OAHashtable.this.vector;

                if (TSB_OAHashtable.this.isEmpty()) {
                    return false;
                }
                if (current_entry >= t.length) {
                    return false;
                }

                // Variable temporal para ver si existe un siguiente
                int tempEntry = current_entry;
                do {
                    tempEntry++;

                } while (tempEntry < t.length && (t[tempEntry] == null || t[tempEntry].esTumba()));

                return (tempEntry < t.length);
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public V next() {

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                Entry<K, V> t[] = TSB_OAHashtable.this.vector;

                do {
                    current_entry++;

                } while (current_entry < t.length && (t[current_entry] == null || t[current_entry].esTumba()));

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la clave del elemento alcanzado...
                V value = t[current_entry].getValue();
                return value;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                Entry<K, V> t[] = TSB_OAHashtable.this.vector;

                // eliminar el objeto que retornó next() la última vez...
                Entry<K, V> eliminado = t[current_entry];
                TSB_OAHashtable.this.remove(eliminado.getKey());

                // quedar apuntando al anterior al que se retornó...
                do {
                    current_entry--;

                } while (current_entry >= 0 && (t[current_entry] == null || t[current_entry].esTumba() ));

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSB_OAHashtable.this.count--;

            }
        }
    }


    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        @Override
        public int size() {
            return TSB_OAHashtable.this.count;
        }

        @Override
        public void clear() {
            TSB_OAHashtable.this.clear();
        }

        /**
         * Verifica si contiene una Entry pasada por parámetro
         */
        @Override
        public boolean contains(Object o) {
            if(o == null) { return false; }
            if(!(o instanceof Entry)) { return false; }

            Entry<K, V> entry = (Entry<K, V>) o;
            K key = entry.getKey();
            return TSB_OAHashtable.this.containsKey(key);
        }


        @Override
        public boolean remove(Object o) {
            if(o == null) { throw new NullPointerException("remove(): parámetro null");}
            if(!(o instanceof Entry)) { return false; }

            Entry<K, V> entry = (Entry<K, V>) o;
            K key = entry.getKey();
            return TSB_OAHashtable.this.remove(key) != null;
        }

        private class EntrySetIterator implements Iterator<Map.Entry<K, V>> {

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            private int current_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            /*
             * Crea un iterador comenzando en la primera lista.
             */
            public EntrySetIterator() {
                current_entry = -1;
                next_ok = false;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext() {
                // variable auxiliar t para simplificar accesos...
                Entry<K, V> t[] = TSB_OAHashtable.this.vector;

                if (TSB_OAHashtable.this.isEmpty()) {
                    return false;
                }
                if (current_entry >= t.length) {
                    return false;
                }

                // Variable temporal para ver si existe un siguiente
                int tempEntry = current_entry;
                do {
                    tempEntry++;

                } while (tempEntry < t.length && (t[tempEntry] == null || t[tempEntry].esTumba()));

                return (tempEntry < t.length);
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public Entry next() {

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                Entry<K, V> t[] = TSB_OAHashtable.this.vector;

                do {
                    current_entry++;

                } while (current_entry < t.length && (t[current_entry] == null || t[current_entry].esTumba()));

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la entrada del elemento alcanzado...
                return t[current_entry];
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                Entry<K, V> t[] = TSB_OAHashtable.this.vector;

                // eliminar el objeto que retornó next() la última vez...
                Entry<K, V> eliminado = t[current_entry];
                TSB_OAHashtable.this.remove(eliminado.getKey());

                // quedar apuntando al anterior al que se retornó...
                do {
                    current_entry--;

                } while (current_entry >= 0 && (t[current_entry] == null || t[current_entry].esTumba() ));

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSB_OAHashtable.this.count--;

            }
        }
    }
}
