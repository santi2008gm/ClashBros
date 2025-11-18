package PRINCIPAL;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import CLASES.EventoTeclado;
import CLASES.Jugador;
import CLASES.BloqueNormal;
import CLASES.BloqueConHongoOMoneda;
import CLASES.Moneda;
import CLASES.Hongo;
import CLASES.BloquePiso;
import java.util.ArrayList;
import java.util.Objects;
import java.awt.Rectangle;
import java.awt.Font;

public class ClashBros extends Canvas implements Runnable {

    private JFrame frame;
    private Thread thread;
    private boolean running = false;
    private Jugador jugador;
    private EventoTeclado eventoTeclado;
    private BufferedImage fondo;
    private BufferedImage coinImage;
    private BufferedImage duendeImage;
    private int coinCount = 0;
    private int score = 0;

    public static int screenWidth;
    public static int screenHeight;
    private int cameraX = 0;

    private ArrayList<Plataforma> plataformas = new ArrayList<>();
    private ArrayList<Enemigo> enemigos = new ArrayList<>();
    private ArrayList<BloqueNormal> bloquesNormales = new ArrayList<>();
    private ArrayList<BloqueConHongoOMoneda> bloquesConHongoOMoneda = new ArrayList<>();
    private ArrayList<Moneda> monedas = new ArrayList<>();
    private ArrayList<Hongo> hongos = new ArrayList<>();
    private ArrayList<BloquePiso> bloquesPiso = new ArrayList<>();

    private boolean gameOver = false;

    public ClashBros() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        frame = new JFrame("Clash Bros.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setUndecorated(true);
        gd.setFullScreenWindow(frame);

        frame.add(this);
        frame.setVisible(true);
        this.setFocusable(true);

        screenWidth = gd.getDisplayMode().getWidth();
        screenHeight = gd.getDisplayMode().getHeight();

        eventoTeclado = new EventoTeclado();
        addKeyListener(eventoTeclado);

        try {
            fondo = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/fondo1.png")));
            coinImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/Moneda.jpeg")));
            duendeImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/Duende.png")));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("No se pudo cargar la imagen de fondo, de moneda o de duende.");
        }
        
        initGame();
    }

    private void initGame() {
        jugador = new Jugador(100, 500, this, eventoTeclado);
        plataformas.clear();
        enemigos.clear();
        bloquesNormales.clear();
        bloquesConHongoOMoneda.clear();
        monedas.clear();
        hongos.clear();
        bloquesPiso.clear();

        // 1. Corregimos el piso para que se generen los bloques uno detrás de otro,
        // excepto en el hueco que queremos para la prueba.
        int yFloor = screenHeight - 64;
        for (int i = 0; i < 20; i++) {
            if (i >= 10 && i < 12) { // Este es el hueco
                continue;
            }
            bloquesPiso.add(new BloquePiso(i * 64, yFloor));
        }

        plataformas.add(new Plataforma(500, 500, 200, 20));
        plataformas.add(new Plataforma(800, 400, 100, 20));
        enemigos.add(new Enemigo(800, 500, duendeImage));
        
        bloquesNormales.add(new BloqueNormal(1000, 490));
        bloquesNormales.add(new BloqueNormal(1064, 490));
        bloquesNormales.add(new BloqueNormal(1128, 490));
        
        bloquesConHongoOMoneda.add(new BloqueConHongoOMoneda(1200, 490, BloqueConHongoOMoneda.Contenido.MONEDA));
        
        bloquesConHongoOMoneda.add(new BloqueConHongoOMoneda(1300, 490, BloqueConHongoOMoneda.Contenido.HONGO));
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int updates = 0;
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                update();
                updates++;
                render();
                frames++;
                delta--;
            }

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames + " Ticks: " + updates);
                updates = 0;
                frames = 0;
            }
        }
        stop();
    }

    private void update() {
        if (!gameOver) {
            // Actualizamos el jugador con las nuevas listas de bloques
            jugador.update(plataformas, enemigos, bloquesNormales, bloquesConHongoOMoneda, monedas, hongos, bloquesPiso, cameraX);

            for (Enemigo e : enemigos) {
                e.update();
            }

            for (Hongo h : hongos) {
                h.update(plataformas, bloquesNormales, bloquesConHongoOMoneda);
            }
            
            // 2. Corregimos la lógica de la moneda para que se elimine después de 1 segundo
            long now = System.currentTimeMillis();
            for (int i = 0; i < monedas.size(); i++) {
                Moneda m = monedas.get(i);
                if (now - m.getCreationTime() > 1000) {
                    monedas.remove(i);
                    i--;
                    // Añadimos la moneda al contador aquí, después de que haya sido visible
                    addCoin();
                }
            }
        } else {
            if (eventoTeclado.isKeyDown(java.awt.event.KeyEvent.VK_R)) {
                restartGame();
            }
        }
    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        cameraX = jugador.getX() - (screenWidth / 2);
        if (cameraX < 0) {
            cameraX = 0;
        }

        if (fondo != null) {
            int fondoWidth = fondo.getWidth();
            int startX = -(cameraX % fondoWidth);
            int currentWidth = startX + screenWidth + fondoWidth;
            for (int x = startX; x < currentWidth; x += fondoWidth) {
                g.drawImage(fondo, x, 0, null);
            }
        } else {
            g.setColor(new Color(135, 206, 235));
            g.fillRect(0, 0, screenWidth, screenHeight);
        }

        for (Plataforma p : plataformas) {
            p.render(g, cameraX);
        }
        
        for (BloqueNormal b : bloquesNormales) {
            b.render(g, cameraX);
        }

        for (BloqueConHongoOMoneda b : bloquesConHongoOMoneda) {
            b.render(g, cameraX);
        }
        
        for (BloquePiso b : bloquesPiso) {
            b.render(g, cameraX);
        }

        for (Moneda m : monedas) {
            m.render(g, cameraX);
        }

        for (Hongo h : hongos) {
            h.render(g, cameraX);
        }
        
        for (Enemigo e : enemigos) {
            e.render(g, cameraX);
        }
        
        jugador.render(g, cameraX);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 20, 45);

        if (coinImage != null) {
            g.drawImage(coinImage, screenWidth - 100, 20, 32, 32, null);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("x " + coinCount, screenWidth - 60, 45);

        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, screenWidth, screenHeight);
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER", screenWidth / 2 - 50, screenHeight / 2);
            g.drawString("Presiona 'R' para reiniciar", screenWidth / 2 - 70, screenHeight / 2 + 20);
        }

        g.dispose();
        bs.show();
    }
    
    public void addMoneda(Moneda moneda) {
        monedas.add(moneda);
    }

    public void addCoin() {
        coinCount++;
    }

    public void addScore(int points) {
        score += points;
    }
    
    public void addHongo(Hongo hongo) {
        hongos.add(hongo);
    }
    
    public void setGameOver() {
        gameOver = true;
    }
    
    public void restartGame() {
        initGame();
        gameOver = false;
        coinCount = 0;
        score = 0;
    }

    public static void main(String[] args) {
        new ClashBros().start();
    }
    
    public static class Plataforma {
        private int x, y, width, height;

        public Plataforma(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public void render(Graphics g, int cameraX) {
            g.setColor(Color.GRAY);
            g.fillRect(x - cameraX, y, width, height);
        }
        
        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }
    
    public static class Enemigo {
        private int x, y;
        private int velX = -1;
        private final int GRAVITY = 1;
        private int velY = 0;
        private int width = 64, height = 64;
        private int groundY = ClashBros.screenHeight - 68;
        private BufferedImage imagen;

        public Enemigo(int x, int y, BufferedImage imagen) {
            this.x = x;
            this.y = y;
            this.imagen = imagen;
        }
        
        public void update() {
            x += velX;
            y += velY;
            velY += GRAVITY;
            
            if (y + height >= groundY && velY > 0) {
                y = groundY - height;
                velY = 0;
            }
        }

        public void render(Graphics g, int cameraX) {
            if (imagen != null) {
                g.drawImage(imagen, x - cameraX, y, width, height, null);
            } else {
                g.setColor(Color.GREEN);
                g.fillRect(x - cameraX, y, width, height);
            }
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        
        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
    }
}