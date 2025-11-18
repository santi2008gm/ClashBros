package CLASES;

import PRINCIPAL.ClashBros; // Importamos ClashBros para acceder a las clases internas
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Hongo {
    private int x, y;
    private int velX = 2; // Velocidad del hongo
    private final int GRAVITY = 1; // Gravedad para el hongo
    private int velY = 0;
    private BufferedImage imagen;
    private int width = 32, height = 32;
    private boolean isVisible = true;

    public Hongo(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            imagen = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/Hongo.jpeg")));
        } catch (IOException e) {
            System.err.println("No se pudo cargar la imagen 'Hongo.jpeg'");
            e.printStackTrace();
        }
    }

    public void update(ArrayList<ClashBros.Plataforma> plataformas, ArrayList<BloqueNormal> bloquesNormales, ArrayList<BloqueConHongoOMoneda> bloquesConHongoOMoneda) {
        x += velX;
        y += velY;
        velY += GRAVITY;

        // Verificar colisión con plataformas y bloques
        for (ClashBros.Plataforma p : plataformas) {
            if (getBounds().intersects(p.getBounds())) {
                if (y + height > p.getY() && velY > 0) {
                    y = p.getY() - height;
                    velY = 0;
                }
            }
        }
        
        for (BloqueNormal b : bloquesNormales) {
            if (getBounds().intersects(b.getBounds())) {
                if (y + height > b.getY() && velY > 0) {
                    y = b.getY() - height;
                    velY = 0;
                }
            }
        }

        for (BloqueConHongoOMoneda b : bloquesConHongoOMoneda) {
            if (getBounds().intersects(b.getBounds())) {
                if (y + height > b.getY() && velY > 0) {
                    y = b.getY() - height;
                    velY = 0;
                }
            }
        }
    }
    
    // Método para cambiar de dirección
    public void changeDirection() {
        velX *= -1;
    }

    public void render(Graphics g, int cameraX) {
        if (isVisible) {
            if (imagen != null) {
                g.drawImage(imagen, x - cameraX, y, width, height, null);
            } else {
                g.setColor(Color.MAGENTA);
                g.fillRect(x - cameraX, y, width, height);
            }
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
}