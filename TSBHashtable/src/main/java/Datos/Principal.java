package Datos;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Principal {
    public static void main(String[] args) {
        TSB_OAHashtable<Integer, String> hashtable = new TSB_OAHashtable<>();
        hashtable.put(5,"you");
        hashtable.put(52,"say");
        hashtable.put(55,"hi");
        hashtable.put(15,"i");
        hashtable.put(65,"say");
        hashtable.put(85,"low");
        hashtable.put(85,"you");
        hashtable.put(55,"say");
        hashtable.put(54,"stop");
        hashtable.put(67,"and");
        hashtable.put(23,"i");
        hashtable.put(75,"say");
        hashtable.put(80,"go");
        hashtable.put(16,"go");
        hashtable.put(28,"go");

        System.out.println(hashtable.size());
        Set<Map.Entry<Integer, String>> vistaKeys = hashtable.entrySet();
        Iterator<Map.Entry<Integer, String>> it = vistaKeys.iterator();
        while (it.hasNext()){
            System.out.println(it.next());
        }
        System.out.println(hashtable.size());
        System.out.println(hashtable.size());
    }
}
