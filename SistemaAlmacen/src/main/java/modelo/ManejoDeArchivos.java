package modelo;

import java.util.*;
import java.io.*;
import java.util.ArrayList;

import modelo.Interface.IManejoDeArchivos;

public class ManejoDeArchivos implements IManejoDeArchivos {

    // Carpeta donde se guardaran los archivos
    private final String RUTA = "src/main/java/datos/";

    public ManejoDeArchivos() {
        File carpeta = new File(RUTA);
        if (!carpeta.exists()) {
            carpeta.mkdirs(); // crea la carpeta si no existe
            System.out.println("Carpeta creada en: " + carpeta.getAbsolutePath());
        }
    }

    @Override
    public boolean leer(String nombreArchivo, String datoBuscado) {
        File archivo = new File(RUTA + nombreArchivo);
        if (!archivo.exists()) return false;

        try (BufferedReader entrada = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                if (linea.contains(datoBuscado)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T> boolean escribir(String nombreArchivo, T objeto, boolean append) {
        try {
            File archivo = new File(RUTA + nombreArchivo);

            if (!archivo.exists()) {
                archivo.createNewFile();
                System.out.println("Archivo creado: " + archivo.getAbsolutePath());
            }


            try (BufferedWriter salida = new BufferedWriter(new FileWriter(RUTA + nombreArchivo, append))) {
                salida.write(objeto.toString());
                salida.newLine();
                System.out.println(" Escrito: " + objeto.toString());
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizarLineaProducto(String nombreArchivo, String idProducto, String lineaNueva,boolean eliminar) {
        try {
            File archivo = new File(RUTA + nombreArchivo);

            if (!archivo.exists()) {
                return false;
            }

            List<String> lineas = leerTodo(nombreArchivo);
            List<String> nuevasLineas = new ArrayList<>();

            for (String linea : lineas) {

                // Suponiendo formato:  ID;nombre;precio;stock
                String[] partes = linea.split(";");
                String idLinea = partes[0];

                if (idLinea.equals(idProducto)) {
                    if(!eliminar) {
                        nuevasLineas.add(lineaNueva);
                    }// reemplaza solo la línea cuyo ID coincide
                } else {
                    nuevasLineas.add(linea);
                }
            }

            try (BufferedWriter salida = new BufferedWriter(new FileWriter(archivo, false))) {
                for (String l : nuevasLineas) {
                    salida.write(l);
                    salida.newLine();
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public String buscarFacturaCliente(String nombreArchivo, String correoCliente) {
        File archivo = new File(RUTA + nombreArchivo);
        if (!archivo.exists()) return null;

        try (BufferedReader entrada = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                if (linea.contains(correoCliente)) {
                    return linea;
                }
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean validarLogin(String nombreArchivo, String correoElectronico, String contrasenia) {
        String linea;
        try {
            BufferedReader entrada = new BufferedReader(new FileReader(RUTA + nombreArchivo));

            while ((linea = entrada.readLine()) != null) {

                String[] l= linea.split(";");
                if (l[2].equals(correoElectronico) && l[3].equals(contrasenia)) {
                    entrada.close();
                    return true;
                }
            }

            entrada.close();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }
    @Override
    public List<String> leerTodo(String nombreArchivo) {
        List<String> lineas = new ArrayList<String>();
        File archivo = new File(RUTA + nombreArchivo);
        if (!archivo.exists()) return lineas;

        try (BufferedReader entrada = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                lineas.add(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lineas;
    }

}
