/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Recursos;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author Marcos Tomas
 */
public class Iconos {
     public static ImageIcon getSolIcon() {
        return crearIcono(Color.YELLOW, "Sol");
    }
    
    public static ImageIcon getLunaIcon() {
        return crearIcono(Color.LIGHT_GRAY, "Luna");
    }
    
    private static ImageIcon crearIcono(Color color, String texto) {
        // Se dibuja un círculo del color indicado (antes se devolvía un ImageIcon vacío)
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(30, 30, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.fillOval(2, 2, 26, 26);
        g.dispose();
        return new ImageIcon(img);
    }
}
