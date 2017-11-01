
package Datos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Archivo {

    private File file;
    private TSBArrayList<String> lista;
    
    public Archivo()
    {
        lista = new TSBArrayList<String>();
    }
    
    public void setFile(File file)
    {
        this.file = file;
    }
    
    public void leerArchivo()
    {
        lista.clear();
        try{
            Scanner sc = new Scanner(file);
            while(sc.hasNext())
            {
                lista.add(sc.nextLine());
            }
        }
        catch(FileNotFoundException e)
        {
            System.err.print("Error de lectura");
                    
        }
    }
    
    public boolean buscarPalabra(String x)
    {
        return lista.contains(x);
    }
    
    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for(String p:lista)
            s.append(p + "\n");
        return s.toString();
    }
  }
    

