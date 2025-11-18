package CLASES;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class BloqueNormal {
    private int x, y;
    private BufferedImage imagen;
    private int width = 64, height = 64;
    private boolean isBroken = false;

    public BloqueNormal(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            // Se corrigió la ruta de la imagen
            imagen = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/BloqueNormal.png")));
        } catch (IOException e) {
            System.err.println("No se pudo cargar la imagen 'BloqueNormal.png'");
            e.printStackTrace();
        }
    }

    public void render(Graphics g, int cameraX) {
        if (!isBroken) {
            if (imagen != null) {
                g.drawImage(imagen, x - cameraX, y, width, height, null);
            } else {
                g.setColor(Color.GRAY);
                g.fillRect(x - cameraX, y, width, height);
            }
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isBroken() {
        return isBroken;
    }

    public void setBroken(boolean broken) {
        this.isBroken = broken;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}