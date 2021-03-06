/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import Comunications.BotCommunicator;
import Graficas.GraficaEncoders;
import Graficas.GraficaPID;
import Graficas.GraficaPWM;
import Graficas.GraficaPosicion;
import Graficas.GraficaUltrasonido;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;
/**
 *
 * @author jesus
 */
public class VentanaP extends javax.swing.JFrame{
    BotCommunicator communicator;
    private float P,I,D;
    private int PWM_MIN,PWM_MAX,PWM_ABS,PWM_BASE;
    
    protected int PWMRight, PWMLeft, position, cycleTime;
    protected double proporcionalConst, derivativeConst, integralConst;
    protected double proportionalFRobot, derivativeFRobot, integralFRobot, plusValuesFRobot;
    protected double encoderA1, encoderA2, encoderW1, encoderW2;
    
    protected ArrayList<Integer> PWMRList, PWMLList, positionList, cycleTimeList, ultrasonidoList;
    protected ArrayList<Double> proportionalFRobotList, derivativeFRobotList, integralFRobotList, plusValuesFRobotList, encoderA1List,encoderA2List,encoderW1List,encoderW2List;
    
    protected ArrayList<String> comandosTecleados;
    int nComando = 0;
    float tolerancia_curva;
    int FRENO_CURVA, delaycurva1, delaycurva2, delay_o1, delay_o2, delay_o3, delay_o4, giro_loco;;
    
    protected String commandLineText = "";
    
    private BotCommunicatorTimer readerTimer;
    
    private BotCommunicatorTimer writerTimer;
    private int consoleLineCounter;
    
    private static GraficaPWM graficaPWM = null;
    private static GraficaPID graficaPID = null;
    private static GraficaPosicion graficaPos = null;
    private static GraficaUltrasonido graficaUltra = null;
    private static GraficaEncoders graficaEnc = null;
    
    public Timer timer;
    
    private Random rnd = new Random();
    
    public static final String[] comandos = {"comandos","help","stop","run","status","actSensor","desactSensor","key","clear","echo","change_pwm","freno_c","c_tolerancia","c_delay"};
    public static final String[] comandosSignificado = {"Muestra los comandos existentes.","Muestra la ayuda de los comandos.","Detiene al robot.","Pone en marcha al robot.","Verifica el estado de todo el robot.","Activa el sensor especificado.","Desactiva el sensor especificado.","Muestra/Cambia la clave","Borra la consola.","Envía un mensaje al arduino para poder comprobar la conexión.","Cambia los limites pwm del arduino. OPCIONES [pwmMinima,pwmMaxima,pwmFrenoABS,PWM_BASE].","Numero de veces que se ejecuta el freno en las curvas.","Cambia la tolerancia en las curvas.","Cambia el delay obstaculo."};
    private String clave;
    
    /**
     * Creates new form VentanaP
     */
    public VentanaP() {
        initComponents();
        
        JesusDesordenado.setVisible(false);
        
        iniciar();
        this.setTitle("Telemetría seguidor LabPrototipos UNET");
        
        //this.setResizable(false);
        
        communicator=new BotCommunicator();
        //comm.start();
        ultrasonidoList=new ArrayList();
        encoderA2List=new ArrayList();
        encoderA1List=new ArrayList();
        encoderW1List=new ArrayList();
        encoderW2List=new ArrayList();
        this.PWMLList=new ArrayList();
        this.PWMRList=new ArrayList();
        this.positionList=new ArrayList();
        this.cycleTimeList=new ArrayList();
        this.comandosTecleados=new ArrayList();
        this.proportionalFRobotList=new ArrayList();
        this.derivativeFRobotList=new ArrayList();
        this.integralFRobotList=new ArrayList();
        this.plusValuesFRobotList=new ArrayList();
        readerTimer = new BotCommunicatorTimer(10, new ReadFromBotCommunicatorTimerActionListener());
        //readerTimer.start();
        writerTimer = new BotCommunicatorTimer(5000, new WriteToBotCommunicatorTimerActionListener());
        consoleLineCounter = 0;
        
        PWMLeft=PWMRight=position=cycleTime=0;
        proporcionalConst=derivativeConst=integralConst=proportionalFRobot=derivativeFRobot=integralFRobot=plusValuesFRobot=0;
        encoderA1=encoderA2=encoderW1=encoderW2=0;
        
        graficaPID = new GraficaPID(proportionalFRobotList,integralFRobotList,derivativeFRobotList,plusValuesFRobotList);
        graficaPWM = new GraficaPWM(PWMLList,PWMRList,cycleTimeList);
        graficaUltra = new GraficaUltrasonido(ultrasonidoList);
        graficaEnc = new GraficaEncoders(encoderA1List, encoderA2List, encoderW1List, encoderW2List);       
        graficaPos = new GraficaPosicion(positionList);
        
        timer = new Timer(30, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setPWMLeft(((getPWMLeft() <= 100)?1:-1)*rnd.nextInt(60)+getPWMLeft());
                setPWMRight(((getPWMRight()<= 100)?1:-1)*rnd.nextInt(60)+getPWMRight());
                /*setPWMLeft(rnd.nextInt(256));
                setPWMRight(rnd.nextInt(256));**/
                setCycleTime(rnd.nextInt(40));
                
                setPosition(rnd.nextInt(1000));
                
                setDerivativeFRobot(rnd.nextDouble()+rnd.nextInt(1000));
                setIntegralFRobot(rnd.nextDouble()+rnd.nextInt(1000));
                setProportionalFRobot(rnd.nextDouble()+rnd.nextInt(1000));
                setPlusValuesFRobot(getProportionalFRobot()+getDerivativeFRobot()+getIntegralFRobot());
                
                setEncoderA1(rnd.nextDouble());
                setEncoderA2(rnd.nextDouble());
                setEncoderW1(rnd.nextDouble());
                setEncoderW2(rnd.nextDouble());
                
                processingDataDistanceSensor(rnd.nextInt(2)+"");
                
                if(graficaPWM != null)
                    graficaPWM.agregarASeries(getPWMLeft(), getPWMRight(),getCycleTime());
                
                if(graficaPID != null)
                    graficaPID.agregar(getProportionalFRobot(), getIntegralFRobot(), getDerivativeFRobot(),getPlusValuesFRobot());
                
                if(graficaPos != null)
                    graficaPos.agregar(getPosition());
                
                if(graficaEnc != null)
                    graficaEnc.agregarASeries(encoderA1, encoderA2, encoderW1, encoderW2);
                
            }
        });

    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jInternalFrame1 = new javax.swing.JInternalFrame();
        panelConstantesPID = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        Barra_P = new javax.swing.JSlider();
        Label_P = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        Barra_I = new javax.swing.JSlider();
        Label_I = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        Barra_D = new javax.swing.JSlider();
        Label_D = new javax.swing.JLabel();
        JesusDesordenado = new javax.swing.JPanel();
        textFieldConstantesPID_P = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        textFieldConstantesPID_I = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        textFieldConstantesPID_D = new javax.swing.JTextField();
        buttonCPID_Cambiar = new javax.swing.JButton();
        panelPWM = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        textFieldPWMI = new javax.swing.JTextField();
        textFieldPWMD = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        textFieldTiempoCiclo = new javax.swing.JTextField();
        progressBarPWMI = new javax.swing.JProgressBar();
        progressBarPWMD = new javax.swing.JProgressBar();
        buttonGraficaPWM = new javax.swing.JButton();
        sliderPosicion = new javax.swing.JSlider();
        panelEncoders = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        textFieldEncodersA1 = new javax.swing.JTextField();
        textFieldEncodersB1 = new javax.swing.JTextField();
        textFieldEncodersA2 = new javax.swing.JTextField();
        textFieldEncodersB2 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        buttonGraficaEncoders = new javax.swing.JButton();
        panelProcesoPID = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        textFieldPPID_P = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        textFieldPPID_I = new javax.swing.JTextField();
        textFieldPPID_D = new javax.swing.JTextField();
        textFieldPPID_Suma = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        buttonGraficaPID = new javax.swing.JButton();
        panelConexion = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        buttonConectar = new javax.swing.JButton();
        buttonDesconectar = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        buttonOpciones = new javax.swing.JButton();
        panelConsola = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textAreaCommandLine = new javax.swing.JTextArea();
        textFieldComando = new javax.swing.JTextField();
        buttonEnviar = new javax.swing.JButton();
        buttonRun = new javax.swing.JButton();
        buttonStop = new javax.swing.JButton();
        panelUltrasonido = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        textFieldUltrasonido = new javax.swing.JTextField();
        buttonGraficaUltrasonido = new javax.swing.JButton();
        panelPWMConstante = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        sliderPWM_MIN = new javax.swing.JSlider();
        labelpwm_min = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel10 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        sliderPWM_MAX = new javax.swing.JSlider();
        labelpwm_max = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jPanel11 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        sliderPWM_ABS = new javax.swing.JSlider();
        labelpwm_abs = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jPanel12 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        sliderPWM_BASE = new javax.swing.JSlider();
        labelpwm_base = new javax.swing.JLabel();

        jInternalFrame1.setVisible(true);

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(139, 208, 249));

        panelConstantesPID.setBackground(new java.awt.Color(175, 221, 248));
        panelConstantesPID.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelConstantesPID.setName("Variables PID"); // NOI18N
        panelConstantesPID.setLayout(new javax.swing.BoxLayout(panelConstantesPID, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel2.setBackground(new java.awt.Color(175, 221, 248));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.PAGE_AXIS));

        jLabel1.setFont(new java.awt.Font("Droid Sans", 0, 14)); // NOI18N
        jLabel1.setText("Constantes PID");
        jPanel2.add(jLabel1);

        jPanel4.setBackground(new java.awt.Color(244, 240, 240));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.X_AXIS));

        jLabel24.setLabelFor(textFieldConstantesPID_P);
        jLabel24.setText("P");
        jPanel4.add(jLabel24);

        Barra_P.setMaximum(100000);
        Barra_P.setValue(50000);
        Barra_P.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                Barra_PStateChanged(evt);
            }
        });
        Barra_P.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Barra_PMouseReleased(evt);
            }
        });
        Barra_P.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Barra_DKeyReleased(evt);
            }
        });
        jPanel4.add(Barra_P);

        Label_P.setText("10.00");
        jPanel4.add(Label_P);

        jPanel2.add(jPanel4);
        jPanel2.add(jSeparator2);

        jPanel3.setBackground(new java.awt.Color(244, 240, 240));
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.X_AXIS));

        jLabel22.setLabelFor(textFieldConstantesPID_P);
        jLabel22.setText("I");
        jPanel3.add(jLabel22);

        Barra_I.setMaximum(100000);
        Barra_I.setValue(50000);
        Barra_I.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                Barra_IStateChanged(evt);
            }
        });
        Barra_I.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Barra_IMouseReleased(evt);
            }
        });
        Barra_I.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Barra_DKeyReleased(evt);
            }
        });
        jPanel3.add(Barra_I);

        Label_I.setText("10.00");
        jPanel3.add(Label_I);

        jPanel2.add(jPanel3);
        jPanel2.add(jSeparator1);

        jPanel5.setBackground(new java.awt.Color(244, 240, 240));
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.X_AXIS));

        jLabel26.setLabelFor(textFieldConstantesPID_P);
        jLabel26.setText("D");
        jPanel5.add(jLabel26);

        Barra_D.setMaximum(100000);
        Barra_D.setValue(50000);
        Barra_D.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                Barra_DStateChanged(evt);
            }
        });
        Barra_D.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Barra_DMouseReleased(evt);
            }
        });
        Barra_D.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Barra_DKeyReleased(evt);
            }
        });
        jPanel5.add(Barra_D);

        Label_D.setText("10.00");
        jPanel5.add(Label_D);

        jPanel2.add(jPanel5);

        panelConstantesPID.add(jPanel2);

        JesusDesordenado.setDoubleBuffered(false);

        textFieldConstantesPID_P.setText("2");

        jLabel2.setLabelFor(textFieldConstantesPID_P);
        jLabel2.setText("P");

        jLabel3.setLabelFor(textFieldConstantesPID_I);
        jLabel3.setText("I");

        textFieldConstantesPID_I.setText("4");

        jLabel4.setLabelFor(textFieldConstantesPID_D);
        jLabel4.setText("D");

        textFieldConstantesPID_D.setText("6");

        buttonCPID_Cambiar.setText("Cambiar");
        buttonCPID_Cambiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCPID_CambiarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout JesusDesordenadoLayout = new javax.swing.GroupLayout(JesusDesordenado);
        JesusDesordenado.setLayout(JesusDesordenadoLayout);
        JesusDesordenadoLayout.setHorizontalGroup(
            JesusDesordenadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JesusDesordenadoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(JesusDesordenadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(JesusDesordenadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textFieldConstantesPID_D)
                    .addComponent(textFieldConstantesPID_I)
                    .addComponent(textFieldConstantesPID_P))
                .addContainerGap())
            .addGroup(JesusDesordenadoLayout.createSequentialGroup()
                .addGap(112, 112, 112)
                .addComponent(buttonCPID_Cambiar)
                .addContainerGap(181, Short.MAX_VALUE))
        );
        JesusDesordenadoLayout.setVerticalGroup(
            JesusDesordenadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JesusDesordenadoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonCPID_Cambiar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(JesusDesordenadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldConstantesPID_P, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(JesusDesordenadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(textFieldConstantesPID_I, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(JesusDesordenadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(textFieldConstantesPID_D, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        panelConstantesPID.add(JesusDesordenado);

        panelPWM.setBackground(new java.awt.Color(175, 221, 248));
        panelPWM.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel5.setFont(new java.awt.Font("Droid Sans", 0, 14)); // NOI18N
        jLabel5.setText("PWM");

        jLabel6.setLabelFor(jLabel2);
        jLabel6.setText("Izquierdo");

        jLabel7.setLabelFor(textFieldPWMD);
        jLabel7.setText("Derecho");

        textFieldPWMI.setText("255");
        textFieldPWMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldPWMIActionPerformed(evt);
            }
        });

        textFieldPWMD.setText("255");

        jLabel13.setText("Tiempo de Ciclo");

        textFieldTiempoCiclo.setText("jTextField10");

        progressBarPWMI.setMaximum(255);

        progressBarPWMD.setMaximum(255);

        buttonGraficaPWM.setText("Grafica");
        buttonGraficaPWM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGraficaPWMActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelPWMLayout = new javax.swing.GroupLayout(panelPWM);
        panelPWM.setLayout(panelPWMLayout);
        panelPWMLayout.setHorizontalGroup(
            panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPWMLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPWMLayout.createSequentialGroup()
                        .addGroup(panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPWMLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(textFieldTiempoCiclo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelPWMLayout.createSequentialGroup()
                                .addGroup(panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addGroup(panelPWMLayout.createSequentialGroup()
                                        .addGroup(panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(panelPWMLayout.createSequentialGroup()
                                                .addComponent(jLabel7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(progressBarPWMD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(panelPWMLayout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(progressBarPWMI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(textFieldPWMI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(textFieldPWMD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(10, 10, 10))
                    .addGroup(panelPWMLayout.createSequentialGroup()
                        .addComponent(buttonGraficaPWM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(220, 220, 220))))
        );
        panelPWMLayout.setVerticalGroup(
            panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPWMLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addGap(24, 24, 24)
                .addGroup(panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelPWMLayout.createSequentialGroup()
                        .addGroup(panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(textFieldPWMI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel6)
                                .addComponent(progressBarPWMI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelPWMLayout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(textFieldPWMD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelPWMLayout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addComponent(jLabel7))))
                    .addComponent(progressBarPWMD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(panelPWMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldTiempoCiclo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addGap(18, 18, 18)
                .addComponent(buttonGraficaPWM)
                .addContainerGap())
        );

        sliderPosicion.setMaximum(5000);
        sliderPosicion.setValue(2500);
        sliderPosicion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderPosicionMouseClicked(evt);
            }
        });

        panelEncoders.setBackground(new java.awt.Color(175, 221, 248));
        panelEncoders.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel8.setText("Encoders");

        textFieldEncodersA1.setText("a_angular1");

        textFieldEncodersB1.setText("v_angular1");

        textFieldEncodersA2.setText("a_angular2");

        textFieldEncodersB2.setText("v_angular2");

        jLabel9.setText("a1");

        jLabel10.setText("a2");

        jLabel11.setText("w2");

        jLabel12.setText("w1");

        buttonGraficaEncoders.setText("Grafica");
        buttonGraficaEncoders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGraficaEncodersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelEncodersLayout = new javax.swing.GroupLayout(panelEncoders);
        panelEncoders.setLayout(panelEncodersLayout);
        panelEncodersLayout.setHorizontalGroup(
            panelEncodersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncodersLayout.createSequentialGroup()
                .addGroup(panelEncodersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelEncodersLayout.createSequentialGroup()
                        .addGroup(panelEncodersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panelEncodersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(panelEncodersLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(jLabel8))
                                .addGroup(panelEncodersLayout.createSequentialGroup()
                                    .addGap(14, 14, 14)
                                    .addComponent(jLabel9)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(textFieldEncodersA1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(panelEncodersLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldEncodersB1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelEncodersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addGap(18, 18, 18)
                        .addGroup(panelEncodersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textFieldEncodersA2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textFieldEncodersB2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelEncodersLayout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(buttonGraficaEncoders, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelEncodersLayout.setVerticalGroup(
            panelEncodersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncodersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEncodersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldEncodersA1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textFieldEncodersA2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addGap(18, 18, 18)
                .addGroup(panelEncodersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldEncodersB1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textFieldEncodersB2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonGraficaEncoders)
                .addContainerGap())
        );

        panelProcesoPID.setBackground(new java.awt.Color(175, 221, 248));
        panelProcesoPID.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelProcesoPID.setName("Variables PID"); // NOI18N

        jLabel14.setFont(new java.awt.Font("Droid Sans", 0, 14)); // NOI18N
        jLabel14.setText("Proceso PID");

        textFieldPPID_P.setText("Proporcional");

        jLabel15.setLabelFor(textFieldConstantesPID_P);
        jLabel15.setText("P");

        jLabel16.setLabelFor(textFieldConstantesPID_I);
        jLabel16.setText("I");

        jLabel17.setLabelFor(textFieldConstantesPID_D);
        jLabel17.setText("D");

        textFieldPPID_I.setText("Integral");

        textFieldPPID_D.setText("Derivativo");

        textFieldPPID_Suma.setText("Juntos");
        textFieldPPID_Suma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldPPID_SumaActionPerformed(evt);
            }
        });

        jLabel18.setLabelFor(textFieldConstantesPID_D);
        jLabel18.setText("Suma");

        buttonGraficaPID.setText("Grafica");
        buttonGraficaPID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGraficaPIDActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelProcesoPIDLayout = new javax.swing.GroupLayout(panelProcesoPID);
        panelProcesoPID.setLayout(panelProcesoPIDLayout);
        panelProcesoPIDLayout.setHorizontalGroup(
            panelProcesoPIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProcesoPIDLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelProcesoPIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonGraficaPID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel14)
                    .addGroup(panelProcesoPIDLayout.createSequentialGroup()
                        .addGroup(panelProcesoPIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelProcesoPIDLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(panelProcesoPIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel17)))
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelProcesoPIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textFieldPPID_Suma)
                            .addComponent(textFieldPPID_D)
                            .addComponent(textFieldPPID_I)
                            .addComponent(textFieldPPID_P))))
                .addContainerGap())
        );
        panelProcesoPIDLayout.setVerticalGroup(
            panelProcesoPIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProcesoPIDLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelProcesoPIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldPPID_P, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(18, 18, 18)
                .addGroup(panelProcesoPIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(textFieldPPID_I, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelProcesoPIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(textFieldPPID_D, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelProcesoPIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(textFieldPPID_Suma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonGraficaPID)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelConexion.setBackground(new java.awt.Color(175, 221, 248));
        panelConexion.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel19.setText("Conexión Bluetooth");

        buttonConectar.setText("Conectar");
        buttonConectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConectarActionPerformed(evt);
            }
        });

        buttonDesconectar.setText("Desconectar");
        buttonDesconectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDesconectarActionPerformed(evt);
            }
        });

        jLabel20.setText("No se encuentra conectado");

        buttonOpciones.setText("Opciones de datos");
        buttonOpciones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOpcionesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelConexionLayout = new javax.swing.GroupLayout(panelConexion);
        panelConexion.setLayout(panelConexionLayout);
        panelConexionLayout.setHorizontalGroup(
            panelConexionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConexionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelConexionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonOpciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelConexionLayout.createSequentialGroup()
                        .addComponent(buttonConectar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonDesconectar))
                    .addGroup(panelConexionLayout.createSequentialGroup()
                        .addGroup(panelConexionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelConexionLayout.setVerticalGroup(
            panelConexionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConexionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelConexionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonConectar)
                    .addComponent(buttonDesconectar))
                .addGap(12, 12, 12)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonOpciones)
                .addContainerGap())
        );

        panelConsola.setBackground(new java.awt.Color(175, 221, 248));
        panelConsola.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        textAreaCommandLine.setEditable(false);
        textAreaCommandLine.setColumns(20);
        textAreaCommandLine.setRows(5);
        jScrollPane1.setViewportView(textAreaCommandLine);

        textFieldComando.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFieldComandoKeyReleased(evt);
            }
        });

        buttonEnviar.setText("Enviar");
        buttonEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEnviarActionPerformed(evt);
            }
        });

        buttonRun.setBackground(new java.awt.Color(101, 227, 18));
        buttonRun.setText("Run Forest!!! Ruuuun!!!!");
        buttonRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRunActionPerformed(evt);
            }
        });

        buttonStop.setBackground(new java.awt.Color(242, 17, 17));
        buttonStop.setForeground(new java.awt.Color(255, 255, 255));
        buttonStop.setText("Bájale a tu ki");
        buttonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelConsolaLayout = new javax.swing.GroupLayout(panelConsola);
        panelConsola.setLayout(panelConsolaLayout);
        panelConsolaLayout.setHorizontalGroup(
            panelConsolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConsolaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelConsolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelConsolaLayout.createSequentialGroup()
                        .addComponent(textFieldComando, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonEnviar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelConsolaLayout.createSequentialGroup()
                        .addComponent(buttonRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonStop, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(panelConsolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelConsolaLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelConsolaLayout.setVerticalGroup(
            panelConsolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelConsolaLayout.createSequentialGroup()
                .addContainerGap(112, Short.MAX_VALUE)
                .addGroup(panelConsolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldComando, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonEnviar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelConsolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonRun)
                    .addComponent(buttonStop))
                .addGap(22, 22, 22))
            .addGroup(panelConsolaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelConsolaLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(95, Short.MAX_VALUE)))
        );

        panelUltrasonido.setBackground(new java.awt.Color(175, 221, 248));
        panelUltrasonido.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelUltrasonido.setLayout(new javax.swing.BoxLayout(panelUltrasonido, javax.swing.BoxLayout.Y_AXIS));

        jLabel21.setText("Sensor ultrasónico");
        panelUltrasonido.add(jLabel21);

        textFieldUltrasonido.setText("Why?");
        panelUltrasonido.add(textFieldUltrasonido);

        buttonGraficaUltrasonido.setText("Grafica");
        buttonGraficaUltrasonido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGraficaUltrasonidoActionPerformed(evt);
            }
        });
        panelUltrasonido.add(buttonGraficaUltrasonido);

        panelPWMConstante.setBackground(new java.awt.Color(175, 221, 248));
        panelPWMConstante.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelPWMConstante.setLayout(new javax.swing.BoxLayout(panelPWMConstante, javax.swing.BoxLayout.Y_AXIS));

        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.LINE_AXIS));

        jLabel25.setText("PWM_MIN");
        jPanel9.add(jLabel25);

        sliderPWM_MIN.setBackground(new java.awt.Color(175, 221, 248));
        sliderPWM_MIN.setMaximum(255);
        sliderPWM_MIN.setValue(100);
        sliderPWM_MIN.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderPWM_MINStateChanged(evt);
            }
        });
        sliderPWM_MIN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                eleased(evt);
            }
        });
        sliderPWM_MIN.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sliderPWM_MINKeyReleased(evt);
            }
        });
        jPanel9.add(sliderPWM_MIN);

        labelpwm_min.setText("100");
        jPanel9.add(labelpwm_min);

        panelPWMConstante.add(jPanel9);
        panelPWMConstante.add(jSeparator3);

        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.LINE_AXIS));

        jLabel27.setText("PWM_MAX");
        jPanel10.add(jLabel27);

        sliderPWM_MAX.setBackground(new java.awt.Color(175, 221, 248));
        sliderPWM_MAX.setMaximum(255);
        sliderPWM_MAX.setValue(100);
        sliderPWM_MAX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderPWM_MAXStateChanged(evt);
            }
        });
        sliderPWM_MAX.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                sliderPWM_MAXMouseReleased(evt);
            }
        });
        sliderPWM_MAX.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sliderPWM_MAXKeyReleased(evt);
            }
        });
        jPanel10.add(sliderPWM_MAX);

        labelpwm_max.setText("100");
        jPanel10.add(labelpwm_max);

        panelPWMConstante.add(jPanel10);
        panelPWMConstante.add(jSeparator5);

        jPanel11.setLayout(new javax.swing.BoxLayout(jPanel11, javax.swing.BoxLayout.LINE_AXIS));

        jLabel28.setText("PWM_ABS");
        jPanel11.add(jLabel28);

        sliderPWM_ABS.setBackground(new java.awt.Color(175, 221, 248));
        sliderPWM_ABS.setMaximum(255);
        sliderPWM_ABS.setValue(100);
        sliderPWM_ABS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderPWM_ABSStateChanged(evt);
            }
        });
        sliderPWM_ABS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                sliderPWM_ABSMouseReleased(evt);
            }
        });
        sliderPWM_ABS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sliderPWM_ABSKeyReleased(evt);
            }
        });
        jPanel11.add(sliderPWM_ABS);

        labelpwm_abs.setText("100");
        jPanel11.add(labelpwm_abs);

        panelPWMConstante.add(jPanel11);
        panelPWMConstante.add(jSeparator4);

        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.LINE_AXIS));

        jLabel29.setText("PWM_BASE");
        jPanel12.add(jLabel29);

        sliderPWM_BASE.setBackground(new java.awt.Color(175, 221, 248));
        sliderPWM_BASE.setMaximum(255);
        sliderPWM_BASE.setValue(100);
        sliderPWM_BASE.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderPWM_BASEStateChanged(evt);
            }
        });
        sliderPWM_BASE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                sliderPWM_BASEMouseReleased(evt);
            }
        });
        sliderPWM_BASE.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sliderPWM_BASEKeyReleased(evt);
            }
        });
        jPanel12.add(sliderPWM_BASE);

        labelpwm_base.setText("100");
        jPanel12.add(labelpwm_base);

        panelPWMConstante.add(jPanel12);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sliderPosicion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(panelEncoders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(panelUltrasonido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(panelPWMConstante, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(panelConstantesPID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(panelPWM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(panelProcesoPID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(panelConexion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(1, 1, 1))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addComponent(panelConsola, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sliderPosicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelConstantesPID, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelProcesoPID, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelPWM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelEncoders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelUltrasonido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelConexion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelPWMConstante, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelConsola, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(331, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void textFieldPWMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldPWMIActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textFieldPWMIActionPerformed

    private void buttonCPID_CambiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCPID_CambiarActionPerformed
        try{
            float p = Float.parseFloat(textFieldConstantesPID_P.getText()), i = Float.parseFloat(textFieldConstantesPID_I.getText()), d = Float.parseFloat(textFieldConstantesPID_D.getText());
            
            setCommandLineText("Proporcional="+p+"/Derivatio="+d+"/Integral="+i);
            writeToDataStream("v/"+p+"/"+i+"/"+d);
        }catch(Exception e){
            JOptionPane.showMessageDialog(this, "ERROR!!"+ e.getMessage()+" No es un valor válido. (Error en actualizacion de PID)", "Error en actualización de PID", JOptionPane.ERROR_MESSAGE);
            setCommandLineText("ERROR!!"+ e.getMessage()+" No es un valor válido. (Error en actualizacion de PID)");
        }
    }//GEN-LAST:event_buttonCPID_CambiarActionPerformed

    private void buttonConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConectarActionPerformed
       boolean conectado = communicator.iniciar();
        
        if(conectado){
            readerTimer.start();
            buttonConectar.setBackground(Color.GREEN);
            buttonDesconectar.setBackground(Color.GREEN);
            
            jLabel20.setText(communicator.getPuertoSeleccionado());
            setCommandLineText("Message: Conectado satisfactoriamente al puerto serial.");
            this.getContentPane().setBackground(Color.cyan);
            putComando("echo Estoy vivo");
        }else{
            setCommandLineText("ERROR: Ocurrió un error al tratar de conectarse al puerto serial.");
        } 
    }//GEN-LAST:event_buttonConectarActionPerformed

    private void buttonDesconectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDesconectarActionPerformed
        boolean desconectado = communicator.parar();
        
        guardarEnArchivos();
        if(desconectado){
            readerTimer.stop();
            buttonConectar.setBackground(Color.BLUE);
            buttonDesconectar.setBackground(Color.BLUE);
            jLabel20.setText("No se encuentra conectado.");
            setCommandLineText("Message: Desconectado satisfactoriamente del puerto serial.");
            this.getContentPane().setBackground(Color.GRAY);
        }else{
            setCommandLineText("ERROR: No se pudo desconectar del puerto serial.");
        }  
    }//GEN-LAST:event_buttonDesconectarActionPerformed

    private void buttonGraficaPWMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGraficaPWMActionPerformed
        if(graficaPWM == null)
            graficaPWM = new GraficaPWM(PWMLList,PWMRList,cycleTimeList);
        else
            graficaPWM.mostrarGrafica();
    }//GEN-LAST:event_buttonGraficaPWMActionPerformed

    private void buttonGraficaPIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGraficaPIDActionPerformed
        if(graficaPID == null)
            graficaPID = new GraficaPID(proportionalFRobotList,integralFRobotList,derivativeFRobotList,plusValuesFRobotList);
        else
            graficaPID.mostrarGrafica();
    }//GEN-LAST:event_buttonGraficaPIDActionPerformed

    private void buttonEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEnviarActionPerformed
        putComando(textFieldComando.getText());
    }//GEN-LAST:event_buttonEnviarActionPerformed

    private void textFieldComandoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldComandoKeyReleased
        boolean band = false;
        if(evt.getKeyCode() == KeyEvent.VK_ENTER)
            putComando(textFieldComando.getText());
        else{
            if(comandosTecleados.size() > 0){
                if(evt.getKeyCode() == KeyEvent.VK_UP){
                    if(nComando == 0){
                        nComando = comandosTecleados.size() - 1;
                    }else{
                          nComando--;  
                    }
                    band = true;
                }
                else{
                    if(evt.getKeyCode() == KeyEvent.VK_DOWN){
                        if(nComando == comandosTecleados.size() - 1)
                            nComando = 0;
                        else
                            nComando++;

                        band = true;
                    }
                }

            }
        }
        
        if(band == true){
            textFieldComando.setText(comandosTecleados.get(nComando));
        }
    }//GEN-LAST:event_textFieldComandoKeyReleased

    private void sliderPosicionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sliderPosicionMouseClicked
        setCommandLineText("Wiiii!!");
        
        if(graficaPos == null)
            graficaPos = new GraficaPosicion(positionList);
        else
            graficaPos.mostrarGrafica();
    }//GEN-LAST:event_sliderPosicionMouseClicked

    private void buttonGraficaUltrasonidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGraficaUltrasonidoActionPerformed
        if(graficaUltra == null)
            graficaUltra = new GraficaUltrasonido(ultrasonidoList);
        else
            graficaUltra.mostrarGrafica();
    }//GEN-LAST:event_buttonGraficaUltrasonidoActionPerformed

    private void buttonGraficaEncodersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGraficaEncodersActionPerformed
        if(graficaEnc == null)
            graficaEnc = new GraficaEncoders(encoderA1List, encoderA2List, encoderW1List, encoderW2List);
        else
            graficaEnc.mostrarGrafica();
    }//GEN-LAST:event_buttonGraficaEncodersActionPerformed

    private void textFieldPPID_SumaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldPPID_SumaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textFieldPPID_SumaActionPerformed

    private void buttonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRunActionPerformed
        putComando("run");
        //timer.start();
    }//GEN-LAST:event_buttonRunActionPerformed

    private void buttonStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStopActionPerformed
        putComando("stop");
        //timer.stop();
    }//GEN-LAST:event_buttonStopActionPerformed

    private void buttonOpcionesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOpcionesActionPerformed
        int showOptionDialog = JOptionPane.showOptionDialog(this, "¿Qué desea hacer?",  "Opciones de Guardado/Recate", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {"Guardar","Abrir","Ver","Reiniciar todo"}, "Guardar");
        System.out.println("Value: "+showOptionDialog);
        
        switch(showOptionDialog){
            case 0:
                guardarEnArchivos();
                break;
            case 1:
                String name = JOptionPane.showInputDialog(null, "Ingrese el nombre del archivo.");
        
                try {
                    BufferedReader buffer = new BufferedReader(new FileReader("DataCollection/GraficasGuardadas/data_texto/"+name));
                    
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "Se chingó al abrir el archivo de configuracion.");
                }
                
                
        
                break;
            case 2:break;
            case 3:
                
                ultrasonidoList.clear();
                encoderA2List.clear();
                encoderA1List.clear();
                encoderW1List.clear();
                encoderW2List.clear();
                this.PWMLList.clear();
                this.PWMRList.clear();
                this.positionList.clear();
                this.cycleTimeList.clear();
                this.comandosTecleados.clear();
                this.proportionalFRobotList.clear();
                this.derivativeFRobotList.clear();
                this.integralFRobotList.clear();
                this.plusValuesFRobotList.clear();
                graficaEnc = null;
                graficaPID = null;
                graficaPWM = null;
                graficaPos = null;
                graficaUltra = null;
                
                break;
        }
    }//GEN-LAST:event_buttonOpcionesActionPerformed

    private void Barra_PStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_Barra_PStateChanged
        textFieldConstantesPID_P.setText(String.valueOf(P));
        P = control_de_slider(Label_P, Barra_P.getValue(), 10000);
    }//GEN-LAST:event_Barra_PStateChanged

    private void Barra_PMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Barra_PMouseReleased
        enviarPIDParametros();
    }//GEN-LAST:event_Barra_PMouseReleased

    private void Barra_IStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_Barra_IStateChanged
        textFieldConstantesPID_I.setText(String.valueOf(I));
        I = control_de_slider(Label_I, Barra_I.getValue(), 10000);
    }//GEN-LAST:event_Barra_IStateChanged

    private void Barra_IMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Barra_IMouseReleased
        enviarPIDParametros();
    }//GEN-LAST:event_Barra_IMouseReleased

    private void Barra_DMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Barra_DMouseReleased
        enviarPIDParametros();
    }//GEN-LAST:event_Barra_DMouseReleased

    private void Barra_DStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_Barra_DStateChanged
        textFieldConstantesPID_D.setText(String.valueOf(D));
        D = control_de_slider(Label_D, Barra_D.getValue(), 10000);
    }//GEN-LAST:event_Barra_DStateChanged

    private void Barra_DKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Barra_DKeyReleased
        enviarPIDParametros();
    }//GEN-LAST:event_Barra_DKeyReleased

    private void eleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_eleased
        enviarPWMPArametros();
    }//GEN-LAST:event_eleased

    private void sliderPWM_MINStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderPWM_MINStateChanged
        PWM_MIN = (int)control_de_slider(labelpwm_min, sliderPWM_MIN.getValue(), 1);
        //System.out.println(""+PWM_MIN);
    }//GEN-LAST:event_sliderPWM_MINStateChanged

    private void sliderPWM_MINKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sliderPWM_MINKeyReleased
       enviarPWMPArametros();
    }//GEN-LAST:event_sliderPWM_MINKeyReleased

    private void sliderPWM_MAXKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sliderPWM_MAXKeyReleased
        enviarPWMPArametros();
    }//GEN-LAST:event_sliderPWM_MAXKeyReleased

    private void sliderPWM_MAXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderPWM_MAXStateChanged
        PWM_MAX = (int)control_de_slider(labelpwm_max, sliderPWM_MAX.getValue(), 1);
    }//GEN-LAST:event_sliderPWM_MAXStateChanged

    private void sliderPWM_ABSKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sliderPWM_ABSKeyReleased
        enviarPWMPArametros();
    }//GEN-LAST:event_sliderPWM_ABSKeyReleased

    private void sliderPWM_ABSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderPWM_ABSStateChanged
        PWM_ABS = (int)control_de_slider(labelpwm_abs, sliderPWM_ABS.getValue(), 1);
    }//GEN-LAST:event_sliderPWM_ABSStateChanged

    private void sliderPWM_BASEStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderPWM_BASEStateChanged
        PWM_BASE = (int)control_de_slider(labelpwm_base, sliderPWM_BASE.getValue(), 1);
    }//GEN-LAST:event_sliderPWM_BASEStateChanged

    private void sliderPWM_MAXMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sliderPWM_MAXMouseReleased
        enviarPWMPArametros();
    }//GEN-LAST:event_sliderPWM_MAXMouseReleased

    private void sliderPWM_ABSMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sliderPWM_ABSMouseReleased
        enviarPWMPArametros();
    }//GEN-LAST:event_sliderPWM_ABSMouseReleased

    private void sliderPWM_BASEMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sliderPWM_BASEMouseReleased
        enviarPWMPArametros();
    }//GEN-LAST:event_sliderPWM_BASEMouseReleased

    private void sliderPWM_BASEKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sliderPWM_BASEKeyReleased
        enviarPWMPArametros();
    }//GEN-LAST:event_sliderPWM_BASEKeyReleased
    
    private void guardarEnArchivos(){
            Date fecha = new Date();
        new Archivos.ArchivoEscrituraConfiguracion("Configuracion-"+fecha.toLocaleString()+"-", Double.parseDouble(this.textFieldConstantesPID_P.getText()), Double.parseDouble(this.textFieldConstantesPID_I.getText()), Double.parseDouble(this.textFieldConstantesPID_D.getText()), 1, 0,PWM_MIN,PWM_MAX,PWM_ABS,PWM_BASE,tolerancia_curva,FRENO_CURVA,delaycurva1,delaycurva2,delay_o1,delay_o2,delay_o3,delay_o4,giro_loco);
        new Archivos.ArchivoEscrituraData("Data-"+fecha.toLocaleString()+"-", this.proportionalFRobotList, this.integralFRobotList, this.derivativeFRobotList, this.plusValuesFRobotList, this.encoderW1List, this.encoderW2List, this.encoderA1List, this.encoderA2List, this.PWMLList, this.PWMRList, this.positionList, null, cycleTimeList);
    }
    
    private void putComando(String comando){
        
        comandosTecleados.add(comando);
        setCommandLineText("**COMANDO**: " + comando);
        
        String partes[] = comando.split(" ");
        
        
        int idComando = verificarComando(partes[0]);
        String sensores = "";
        int cValidos = 0;
        
        if(idComando == 5 || idComando == 6){
            
                for (int i = 1; i < partes.length; i++) {
                    if(partes[i].compareToIgnoreCase("infra") == 0){
                        sensores += "/1";
                        cValidos++;
                    }else{
                        if(partes[i].compareToIgnoreCase("ultra") == 0){
                            sensores += "/2";
                            cValidos++;
                        }else{
                            setCommandLineText("\tLa opcion "+partes[i]+" no fue encontrada dentro del comando.");
                        }
                    }
                }
        }
        
        switch(idComando){
            case 0:
                String total = "COMANDOS DISPONIBLES:\n\n";
                for(String ca: comandos){
                    total += ("*    "+ca+"\n");
                }
                setCommandLineText(total);
                break;
            case 1:
                for (int cActual = 1; cActual < partes.length; cActual++) {
                    int posComando = verificarComando(partes[cActual]);
                    
                    if(posComando == -1){
                        setCommandLineText("\tEl comando "+partes[cActual]+" no fue encontrado.");
                    }else{
                        setCommandLineText("\t"+comandos[posComando]+": "+comandosSignificado[posComando]);
                    }
                }
                    
                break;
                
            case 2: 
                writeToDataStream("p");
                this.getContentPane().setBackground(Color.red);
                break;
            case 3: 
                writeToDataStream("r");
                this.getContentPane().setBackground(Color.green);
                break;
            case 4: 
                writeToDataStream("s");
                this.getContentPane().setBackground(Color.yellow);
                break;
            case 5:
                if(cValidos > 0){
                    writeToDataStream(clave+"/a/"+(cValidos)+sensores);
                    setCommandLineText("\t**Activando sensores: "+sensores);
                }else
                    setCommandLineText("\t**Error de sintaxis.");

                break;
            case 6: 
                if(cValidos > 0){
                    writeToDataStream(clave+"/d/"+(cValidos)+sensores);
                    setCommandLineText("\t**Desactivando sensores: "+sensores);
                }else
                    setCommandLineText("\t**Error de sintaxis.");
                
                break;
                
            case 7: 
                if(partes.length > 1){
                    if(partes[1].compareToIgnoreCase("show")==0)
                        setCommandLineText("\tCLAVE: "+clave);
                    else
                        if(partes[1].compareToIgnoreCase("cambiar") == 0 && partes.length > 2){
                            clave = partes[2];
                            setCommandLineText("\tClave cambiada a \""+clave+"\".");
                        }else
                            setCommandLineText("\t**Error de sintaxis.");

                            

                }else
                    setCommandLineText("\t**Error de sintaxis.");

                break;
            case 8:
                this.textAreaCommandLine.setText("");
                this.commandLineText="";
                break;
            case 9:
                writeToDataStream("e/"+comando.substring(5));
                break;
            case 10:
                writeToDataStream("C/"+partes[1]);
                setCommandLineText("PWM cambiado");
                break;
            case 11:
                writeToDataStream("f/"+partes[1]);
                setCommandLineText("Freno de la curva y delays= "+partes[1]);
                String[] frag = partes[1].split(",");
                FRENO_CURVA = Integer.parseInt(frag[0]);
                delaycurva1 = Integer.parseInt(frag[1]);
                delaycurva2 = Integer.parseInt(frag[2]);
                break;
            case 12:
                writeToDataStream("t/"+partes[1]);
                tolerancia_curva = Float.parseFloat(partes[1]);
                setCommandLineText("Tolerancia cambiada a "+partes[1]);
                break;
            case 13:
                writeToDataStream("o/"+partes[1]);
                setCommandLineText("Delay del obstáculo cambiado a "+partes[1]);
                String[] frag1 = partes[1].split(",");
                delay_o1 = Integer.parseInt(frag1[0]);
                delay_o2 = Integer.parseInt(frag1[1]);
                delay_o3 = Integer.parseInt(frag1[2]);
                delay_o4 = Integer.parseInt(frag1[3]);
                giro_loco = Integer.parseInt(frag1[4]);
                break;
            default:
                setCommandLineText("El comando \""+comando+"\" no fue encontrado.");
                break;
        }

        textFieldComando.setText("");
    }
    
    private int verificarComando(String s){
        for (int i = 0; i < comandos.length; i++) {
            if(s.compareToIgnoreCase(comandos[i]) == 0)
                return i;
        }

        return -1;
    }
        
    private void iniciar(){
        //this.jTextField1.setText("            ");
        this.textFieldConstantesPID_P.setEditable(true);
        
        //this.jTextField2.setText("           ");
        this.textFieldPWMI.setEditable(false);
        
        //this.jTextField3.setText("      ");
        this.textFieldPWMD.setEditable(false);
        
        //this.jTextField4.setText("              ");
        this.textFieldConstantesPID_I.setEditable(true);
        
        //this.jTextField5.setText("              ");
        this.textFieldConstantesPID_D.setEditable(true);
        
        this.textFieldEncodersA1.setEditable(false);
        
        this.textFieldEncodersB1.setEditable(false);
        this.textFieldEncodersA2.setEditable(false);
        this.textFieldEncodersB2.setEditable(false);
        this.textFieldTiempoCiclo.setEditable(false);
        this.textFieldPPID_P.setEditable(false);
        this.textFieldPPID_I.setEditable(false);
        this.textFieldPPID_Suma.setEditable(false);
        this.textFieldPPID_D.setEditable(false);
        
        sliderPosicion.setEnabled(false);
        
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VentanaP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VentanaP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VentanaP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VentanaP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VentanaP().setVisible(true);
                
            }
        });
    }

    public int getPWMRight() {
        return PWMRight;
    }

    public void setPWMRight(int PWMRight) {
        this.PWMRight = PWMRight;
        this.progressBarPWMD.setValue(PWMRight);
        this.textFieldPWMD.setText(""+PWMRight);
        this.PWMRList.add(PWMRight);
    }

    public int getPWMLeft() {
        return PWMLeft;
    }

    public void setPWMLeft(int PWMLeft) {
        this.PWMLeft = PWMLeft;
        this.progressBarPWMI.setValue(PWMLeft);
        this.textFieldPWMI.setText(""+PWMLeft);
        this.PWMLList.add(PWMLeft);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
        this.sliderPosicion.setValue(position);
        this.positionList.add(position);
    }

    public int getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(int cycleTime) {
        this.cycleTime = cycleTime;
        this.textFieldTiempoCiclo.setText(cycleTime+"");
        this.cycleTimeList.add(cycleTime);
    }

    public double getProporcionalConst() {
        return proporcionalConst;
    }

    public void setProporcionalConst(double proporcionalConst) {
        this.proporcionalConst = proporcionalConst;
        this.textFieldConstantesPID_P.setText(proporcionalConst+"");
                
    }

    public double getDerivativeConst() {
        return derivativeConst;
    }

    public void setDerivativeConst(double derivativeConst) {
        this.derivativeConst = derivativeConst;
        this.textFieldConstantesPID_D.setText(derivativeConst+"");
    }

    public double getIntegralConst() {
        return integralConst;
    }

    public void setIntegralConst(double integralConst) {
        this.integralConst = integralConst;
        this.textFieldConstantesPID_I.setText(integralConst+"");
    }

    public double getProportionalFRobot() {
        return proportionalFRobot;
    }

    public void setProportionalFRobot(double proportionalFRobot) {
        this.proportionalFRobot = proportionalFRobot;
        this.textFieldPPID_P.setText(proportionalFRobot+"");
        this.proportionalFRobotList.add(proportionalFRobot);
    }

    public double getDerivativeFRobot() {
        return derivativeFRobot;
    }

    public void setDerivativeFRobot(double derivativeFRobot) {
        this.derivativeFRobot = derivativeFRobot;
        this.textFieldPPID_D.setText(derivativeFRobot+"");
        this.derivativeFRobotList.add(derivativeFRobot);
    }

    public double getIntegralFRobot() {
        return integralFRobot;
    }

    public void setIntegralFRobot(double integralFRobot) {
        this.integralFRobot = integralFRobot;
        this.textFieldPPID_I.setText(integralFRobot+"");
        this.integralFRobotList.add(integralFRobot);
    }

    public double getPlusValuesFRobot() {
        return plusValuesFRobot;
    }

    public void setPlusValuesFRobot(double plusValuesFRobot) {
        this.plusValuesFRobot = plusValuesFRobot;
        this.textFieldPPID_Suma.setText(plusValuesFRobot+"");
        this.plusValuesFRobotList.add(plusValuesFRobot);
////        
    }

    public double getEncoderA1() {
        return encoderA1;
    }

    public void setEncoderA1(double encoderA1) {
        this.encoderA1 = encoderA1;
        this.textFieldEncodersA1.setText(encoderA1+"");
        encoderA1List.add(encoderA1);
    }

    public double getEncoderA2() {
        return encoderA2;
    }

    public void setEncoderA2(double encoderA2) {
        this.encoderA2 = encoderA2;
        this.textFieldEncodersA2.setText(encoderA2+"");
        encoderA2List.add(encoderA2);
    }

    public double getEncoderW1() {
        return encoderW1;
    }

    public void setEncoderW1(double encoderB1) {
        this.encoderW1 = encoderB1;
        this.textFieldEncodersB1.setText(encoderB1+"");
        encoderW1List.add(encoderB1);
    }

    public double getEncoderW2() {
        return encoderW2;
    }

    public void setEncoderW2(double encoderB2) {
        this.encoderW2 = encoderB2;
        this.textFieldEncodersB2.setText(encoderB2+"");
        encoderW2List.add(encoderB2);
    }
    
    protected void readFromDataStream(){
        String incoming = null;
        
        if(this.communicator.isIsConectado()){
            if(this.communicator.getReader().isAvailable()){

                incoming = this.communicator.getReader().getEntrada();

                if(incoming != null){
                    String[] cadenas = incoming.split("/");
                    
                    if(cadenas[0].compareToIgnoreCase("message") == 0)
                        setCommandLineText("MENSAJE:  "+cadenas[1]);
                    else
                        if(cadenas[0].compareToIgnoreCase("data") == 0)
                            try{
                                setProportionalFRobot(Double.parseDouble(cadenas[1]));
                                setDerivativeFRobot(Double.parseDouble(cadenas[3]));
                                setIntegralFRobot(Double.parseDouble(cadenas[2]));
                                setPlusValuesFRobot(Double.parseDouble(cadenas[4]));
                                setPWMLeft(Integer.parseInt(cadenas[5]));
                                setPWMRight(Integer.parseInt(cadenas[6]));
                                setCycleTime(Integer.parseInt(cadenas[7]));
                                setPosition(Integer.parseInt(cadenas[8]));
                                processingDataDistanceSensor(cadenas[9]);
                                setEncoderW1(Double.parseDouble(cadenas[10]));
                                setEncoderW2(Double.parseDouble(cadenas[11]));
                                setEncoderA1(Double.parseDouble(cadenas[12]));
                                setEncoderA2(Double.parseDouble(cadenas[13]));

                                if(graficaPWM != null)
                                    graficaPWM.agregarASeries(this.getPWMLeft(), this.getPWMRight(), this.getCycleTime());
                                

                                if(graficaPID != null)
                                    graficaPID.agregar(this.getProportionalFRobot(), this.getIntegralFRobot(), this.getDerivativeFRobot(), this.getPlusValuesFRobot());

                                if(graficaPos != null)
                                    graficaPos.agregar(getPosition());
                                
                                if(graficaEnc != null)
                                    graficaEnc.agregarASeries(encoderA1, encoderA2, encoderW1, encoderW2);
                                
                                
                            }catch(Exception e){
                                setCommandLineText("\nERROR! Problema al convertir data entrante desde el InputStream en valores numéricos.\n");
                            }
                        else
                            if(cadenas[0].compareToIgnoreCase("status") == 0){
                                System.out.println("status");
                                String mensaje = "STATUS\n\n* Sensores infrarrojos ";
                                
                                if(Integer.parseInt(cadenas[1]) == 1){
                                    mensaje += "activos";
                                }else
                                    mensaje += "inactivos";
                                
                                mensaje += ".\n* Sensor de distancia ";
                                if(Integer.parseInt(cadenas[2]) == 1)
                                    mensaje += "activo";
                                else
                                    mensaje += "inactivo";
                                
                                mensaje += "\nP:"+cadenas[3]+"\nI: "+cadenas[4]+"\nD:"+cadenas[5]+"\nPWM_MIN="+cadenas[6]+"\nPWM_MAX="+cadenas[7]+"\nPWM_ABS="+cadenas[8]+"\nPWM_BASE="+cadenas[9]+"\nTolerancia dinamica="+cadenas[10]+"\nFreno curva:"+cadenas[11]+"\nDelay1: "+cadenas[12]+"\nDelay2: "+cadenas[13]+"\nDelay_o1: "+cadenas[14]+"\nDelay_o2: "+cadenas[15]+"\nDelay_o3: "+cadenas[16]+"\nDelay_o4: "+cadenas[17]+"\nGiro_loco_porc="+cadenas[18];                                
                                            
                                JOptionPane.showMessageDialog(this, mensaje, "STATUS DEL ROBOT!!!", JOptionPane.INFORMATION_MESSAGE);
                            }
                            else
                                if(cadenas[0].compareToIgnoreCase("message2") == 0)
                                    JOptionPane.showMessageDialog(null, "Curva hacia: "+ cadenas[1]);
                    
                    setCommandLineText(consoleLineCounter+")"+incoming);
                    consoleLineCounter++;
                }
            }
        }
    }
    
    private void processingDataDistanceSensor(String s){
        int valor =Integer.parseInt(s);
        
        if(valor == 1){
            textFieldUltrasonido.setText("Obstáculo!!!!");
            panelUltrasonido.setBackground(Color.RED);
            ultrasonidoList.add(1);
                
        }else{
            textFieldUltrasonido.setText("Nada");
            panelUltrasonido.setBackground(Color.GREEN);
            ultrasonidoList.add(0);
        }
        
        if(graficaUltra!=null)
            graficaUltra.agregar((valor == 1)?1:0);
    }
    
    protected void writeToDataStream(String s){
        if(this.communicator.isIsConectado())
            this.communicator.getSender().writeData(s);
        else
            setCommandLineText("La operación no pudo realizarse porque no se encuentra conectado al robot.");
    }
    
    protected void setCommandLineText(String s){
        this.commandLineText += (s+"\n");
        //writeToDataStream("Recibido:"+s+" --//\n");
        textAreaCommandLine.setText(commandLineText);
    }
    
    class BotCommunicatorTimer extends Timer{

        public BotCommunicatorTimer(int delay, ActionListener listener) {
            super(delay, listener);
        }
    } 
    
    class ReadFromBotCommunicatorTimerActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Listener.");
            readFromDataStream();
        }
        
    }
    
    class WriteToBotCommunicatorTimerActionListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            System.out.println("Writer");
            writeToDataStream("asd");
        }
        
    }
    private float control_de_slider(JLabel l,int valor,int decimales)
    {
        float r = (float)valor/decimales;
        l.setText(String.valueOf(r));

        return r;
    }
    private void enviarPIDParametros()
    {
         try{
            setCommandLineText("Proporcional="+P+"/Derivatio="+D+"/Integral="+I);
            writeToDataStream("v/"+P*10000+"/"+I*10000+"/"+D*10000);
        }catch(Exception e){
            JOptionPane.showMessageDialog(this, "ERROR!!"+ e.getMessage()+" No es un valor válido. (Error en actualizacion de PID)", "Error en actualización de PID", JOptionPane.ERROR_MESSAGE);
            setCommandLineText("ERROR!!"+ e.getMessage()+" No es un valor válido. (Error en actualizacion de PID)");
        }
    }
    
    private void enviarPWMPArametros(){
        putComando("change_pwm "+PWM_MIN+","+PWM_MAX+","+PWM_ABS+","+PWM_BASE);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider Barra_D;
    private javax.swing.JSlider Barra_I;
    private javax.swing.JSlider Barra_P;
    private javax.swing.JPanel JesusDesordenado;
    private javax.swing.JLabel Label_D;
    private javax.swing.JLabel Label_I;
    private javax.swing.JLabel Label_P;
    private javax.swing.JButton buttonCPID_Cambiar;
    private javax.swing.JButton buttonConectar;
    private javax.swing.JButton buttonDesconectar;
    private javax.swing.JButton buttonEnviar;
    private javax.swing.JButton buttonGraficaEncoders;
    private javax.swing.JButton buttonGraficaPID;
    private javax.swing.JButton buttonGraficaPWM;
    private javax.swing.JButton buttonGraficaUltrasonido;
    private javax.swing.JButton buttonOpciones;
    private javax.swing.JButton buttonRun;
    private javax.swing.JButton buttonStop;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JLabel labelpwm_abs;
    private javax.swing.JLabel labelpwm_base;
    private javax.swing.JLabel labelpwm_max;
    private javax.swing.JLabel labelpwm_min;
    private javax.swing.JPanel panelConexion;
    private javax.swing.JPanel panelConsola;
    private javax.swing.JPanel panelConstantesPID;
    private javax.swing.JPanel panelEncoders;
    private javax.swing.JPanel panelPWM;
    private javax.swing.JPanel panelPWMConstante;
    private javax.swing.JPanel panelProcesoPID;
    private javax.swing.JPanel panelUltrasonido;
    private javax.swing.JProgressBar progressBarPWMD;
    private javax.swing.JProgressBar progressBarPWMI;
    private javax.swing.JSlider sliderPWM_ABS;
    private javax.swing.JSlider sliderPWM_BASE;
    private javax.swing.JSlider sliderPWM_MAX;
    private javax.swing.JSlider sliderPWM_MIN;
    private javax.swing.JSlider sliderPosicion;
    private javax.swing.JTextArea textAreaCommandLine;
    private javax.swing.JTextField textFieldComando;
    private javax.swing.JTextField textFieldConstantesPID_D;
    private javax.swing.JTextField textFieldConstantesPID_I;
    private javax.swing.JTextField textFieldConstantesPID_P;
    private javax.swing.JTextField textFieldEncodersA1;
    private javax.swing.JTextField textFieldEncodersA2;
    private javax.swing.JTextField textFieldEncodersB1;
    private javax.swing.JTextField textFieldEncodersB2;
    private javax.swing.JTextField textFieldPPID_D;
    private javax.swing.JTextField textFieldPPID_I;
    private javax.swing.JTextField textFieldPPID_P;
    private javax.swing.JTextField textFieldPPID_Suma;
    private javax.swing.JTextField textFieldPWMD;
    private javax.swing.JTextField textFieldPWMI;
    private javax.swing.JTextField textFieldTiempoCiclo;
    private javax.swing.JTextField textFieldUltrasonido;
    // End of variables declaration//GEN-END:variables

}
