package CLASES;

import PRINCIPAL.ClashBros;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Jugador {

    private int x, y;
    private int velX, velY;
    private int width = 32, height = 32;
    private boolean isJumping = false;
    private final int GRAVITY = 1;
    private ClashBros game;

    private EventoTeclado eventoTeclado;
    private boolean isPoweredUp = false;
    private int originalWidth = 32;
    private int originalHeight = 32;

    public Jugador(int x, int y, ClashBros game, EventoTeclado eventoTeclado) {
        this.x = x;
        this.y = y;
        this.velX = 0;
        this.velY = 0;
        this.game = game;
        this.eventoTeclado = eventoTeclado;
    }

    public void update(ArrayList<ClashBros.Plataforma> plataformas, ArrayList<ClashBros.Enemigo> enemigos, ArrayList<BloqueNormal> bloquesNormales, ArrayList<BloqueConHongoOMoneda> bloquesConHongoOMoneda, ArrayList<Moneda> monedas, ArrayList<Hongo> hongos, ArrayList<BloquePiso> bloquesPiso, int cameraX) {
        int moveSpeed = 5;
        velX = 0;
        if (eventoTeclado.isKeyDown(KeyEvent.VK_A)) {
            velX = -moveSpeed;
        }
        if (eventoTeclado.isKeyDown(KeyEvent.VK_D)) {
            velX = moveSpeed;
        }

        if (eventoTeclado.isKeyDown(KeyEvent.VK_SPACE)) {
            jump();
        }

        x += velX;
        y += velY;
        velY += GRAVITY;

        boolean onGround = false;

        // Colisiones con plataformas (permanece igual)
        for (ClashBros.Plataforma p : plataformas) {
            if (getBounds().intersects(p.getBounds())) {
                if (y + height > p.getY() && velY > 0) {
                    y = p.getY() - height;
                    velY = 0;
                    isJumping = false;
                    onGround = true;
                }
            }
        }
        
        // 3. Corregimos las colisiones con los bloques
        for (BloqueNormal b : bloquesNormales) {
            if (getBounds().intersects(b.getBounds())) {
                // Colisión cuando el jugador salta y golpea el bloque con la cabeza
                if (velY < 0 && y < b.getY() + b.getHeight() && y > b.getY()) {
                    y = b.getY() + b.getHeight(); // Ajusta la posición para que no lo atraviese
                    velY = 0; // Detiene el salto
                    // Lógica para romper el bloque si es necesario, aunque no lo pide el usuario, se puede agregar
                }
                
                // Colisión cuando el jugador aterriza sobre el bloque
                if (y + height > b.getY() && velY > 0) {
                    y = b.getY() - height;
                    velY = 0;
                    isJumping = false;
                    onGround = true;
                }
            }
        }

        // Corregimos las colisiones para los bloques de piso
        for (BloquePiso b : bloquesPiso) {
            if (getBounds().intersects(b.getBounds())) {
                // Colisión cuando el jugador salta y golpea el bloque con la cabeza
                if (velY < 0 && y < b.getY() + b.getHeight() && y > b.getY()) {
                    y = b.getY() + b.getHeight();
                    velY = 0;
                }
                
                // Colisión cuando el jugador aterriza sobre el bloque
                if (y + height > b.getY() && velY > 0) {
                    y = b.getY() - height;
                    velY = 0;
                    isJumping = false;
                    onGround = true;
                }
            }
        }
        
        // Corregimos las colisiones para los bloques de hongo/moneda
        for (BloqueConHongoOMoneda b : bloquesConHongoOMoneda) {
            if (getBounds().intersects(b.getBounds())) {
                 if (velY < 0 && y < b.getY() + b.getHeight() && y > b.getY()) {
                    y = b.getY() + b.getHeight(); // **Línea agregada para corregir la posición**
                    velY = 0;
                    if (!b.isHit()) {
                        b.setHit(true);
                        if (b.getContenido() == BloqueConHongoOMoneda.Contenido.MONEDA) {
                            game.addMoneda(new Moneda(b.getX(), b.getY() - b.getHeight()));
                        } else if (b.getContenido() == BloqueConHongoOMoneda.Contenido.HONGO) {
                            game.addHongo(new Hongo(b.getX(), b.getY() - 32));
                        }
                    }
                }
                
                if (y + height > b.getY() && velY > 0) {
                    y = b.getY() - height;
                    velY = 0;
                    isJumping = false;
                    onGround = true;
                }
            }
        }

        // Eliminamos la colisión del jugador con las monedas
        // for (int i = 0; i < monedas.size(); i++) {
        //     Moneda m = monedas.get(i);
        //     if (getBounds().intersects(m.getBounds())) {
        //         game.addCoin();
        //         monedas.remove(i);
        //         i--;
        //     }
        // }

        // Colisiones con hongos (permanece igual)
        for (int i = 0; i < hongos.size(); i++) {
            Hongo h = hongos.get(i);
            if (getBounds().intersects(h.getBounds())) {
                powerUp();
                hongos.remove(i);
                i--;
            }
        }

        // Colisiones con enemigos (permanece igual)
        for (int i = 0; i < enemigos.size(); i++) {
            ClashBros.Enemigo e = enemigos.get(i);
            if (getBounds().intersects(e.getBounds())) {
                if (y + height > e.getY() && y + height < e.getY() + (e.getHeight() / 2) && velY > 10) {
                    enemigos.remove(i);
                    i--;
                    setVelY(-10);
                    game.addScore(100);
                } else {
                    if (isPoweredUp) {
                        revertToNormal();
                    } else {
                        game.setGameOver();
                        break;
                    }
                }
            }
        }
        
        if (!onGround && y > ClashBros.screenHeight) {
            game.setGameOver();
        }
    }

    public void render(Graphics g, int cameraX) {
        if (isPoweredUp) {
            g.setColor(Color.BLUE);
        } else {
            g.setColor(Color.RED);
        }
        g.fillRect(x - cameraX, y, width, height);
    }

    public void jump() {
        if (!isJumping) {
            velY = -15;
            isJumping = true;
        }
    }

    public void powerUp() {
        if (!isPoweredUp) {
            isPoweredUp = true;
            width *= 2;
            height *= 2;
        }
    }

    public void revertToNormal() {
        isPoweredUp = false;
        width = originalWidth;
        height = originalHeight;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getVelX() { return velX; }
    public int getVelY() { return velY; }
    public void setVelX(int velX) { this.velX = velX; }
    public void setVelY(int velY) { this.velY = velY; }
}