/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CardaDeArchivos;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
/**
 *
 * @author Romi
 */
public class FXMLDocumentController implements Initializable {
    
    private Label label;
    @FXML
    private TextField txtPalabra;
    @FXML
    private Label lblResultado;
    @FXML
    private Label lblArchivo;
    @FXML
    private TextArea txtArchivo;
    private Datos.Archivo archPalabras;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        archPalabras = new Datos.Archivo();
    }    
    

    @FXML
    private void btnCargarClick(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo de texto");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento de texto (*.txt)", "*.txt"));
        File file = fc.showOpenDialog(null);
        if (file != null)
        {
            lblArchivo.setText(file.getAbsolutePath());
            archPalabras.setFile(file);
            archPalabras.leerArchivo();
            txtArchivo.setText(archPalabras.toString());            
        }
        
    }

    @FXML
    private void btnBuscarClick(ActionEvent event) {
        if(txtPalabra.getText().isEmpty())
            lblResultado.setText("No ingresó una palabra");
        else
        {
            String x = txtPalabra.getText();
            if(archPalabras.buscarPalabra(x))
                lblResultado.setText("Palabra encontrada");
            else
                lblResultado.setText("Palabra NO encontrada");
        }
        
    }
    
    @FXML
    private void btnLimpiarClick(ActionEvent event) {
    
        txtPalabra.setText("");
        lblResultado.setText("");
    }
    
}
