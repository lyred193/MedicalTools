

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.NoSuchElementException;

import MedicalTools.MedicalTools;
import com.mathworks.toolbox.javabuilder.MWException;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.org.apache.xml.internal.security.Init;

import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.frame.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import ij.IJ;
import ij.ImagePlus;

/**
 * @author lyred
 */
public class Tools_Index extends PlugInFrame implements Runnable,
        ActionListener, AdjustmentListener, ItemListener
{
    /**
     * �����߳�
     */
    Thread thread;

    // ������
    ImageJ ij;

    // ��ǰFrame��ʵ��
    static Frame instance;

    // ������
    Scrollbar contrastSlider;

    // ��ǩ
    Label valNote ;
    Label valLabel ;

    // ���������ֵ
    int sliderRange=200 ;

    /** �����Ӧֵ */
    int contrastValue = sliderRange/2 ;


    /**  ͼ�����ֵ����Сֵ */
    double min, max, org_min, org_max ;

    /** �˳���־ */
    boolean	done = false ;

    /** ��Ƭ���ֹ�����  */
    BorderLayout gridbag ;
    /** ���ֹ��������������� */
//    GridBagConstraints c;
    /** ���� */
    Panel pan1, pan2, pan3 ;
    /** �˵���� */
    MenuBar jmb;
    Menu jm1,jm2,jm3,jm4,jm5;
    /** �����˵� */
    MenuItem jmt1=null;
    MenuItem jmt2=null;
    /** �����˵� �˲��㷨ѡ��  */
    MenuItem AD2D;
    MenuItem AD3D;
    MenuItem SRAD2D;
    MenuItem SRAD3D;
    MenuItem choiceFile;
    MenuItem choiceSetting;
    MenuItem aboutTools;
    MenuItem instructions;
    String INSTRUCTIONS_URL = "https://1938494248.gitbook.io/-1/shi-yong-shuo-ming";
    String ABOUTTOOLS_URL = "https://1938494248.gitbook.io/-1/";
    String ABOUTTOOLS_CONTENT = "2020��4�±�д��ɣ����аߵ㽵�����������ɢ������������ɢ��ά��ά�˲���";
    Label label;
    /** ͼƬ��ʾ */
    ImagePlus impSrc;
    ImagePlus impTag;
    String[] list1;
    /** �ļ��б� */
    JList<String> list;
    String dir;
    JScrollPane jScrollPane;
    /** ��ť�� */
    Button startFilter;
    String filterFilePath;
    /** �˲�����ѡ�� */
    GenericDialog gd;
    int nb_iter=20;
    double nb_lambda=0.1f;
    /** SRAD���� */

    /* ���ڷ�dicom ��ʽ���ļ����Ƿ񵯳�ѡ��� */
    boolean isSelectForNomalImage = true;
    public Tools_Index()
    {
        super("B&C");
        this.setSize(300,300);
    }

    /**
     * �����ʼ��
     */
    private void initView(){
        // ������ ѡ����Ŀ¼
        IJ.showMessage("����Ŀ¼ѡ��·��","��ѡ����Ŀ¼");
        dir = IJ.getDir("../");
        IJ.log(dir);
        File sourceFile = new File(dir+"/sourceJpg");
        if (!sourceFile.exists()) {
            sourceFile.mkdirs();
        }
        list1 = new File(dir).list();
        impSrc = new ImagePlus();
        impTag = new ImagePlus();
        startFilter = new Button("��ʼ�˲�");
        startFilter.setActionCommand("startFilter");
        startFilter.addActionListener(mAorActionListener);
//        String [] fileList = new String[list1.length];
        int i=0;
        //���ñ���
        setTitle("ҽѧͼ��������");
        // ע�ᵱǰ�ൽһ���������ֹ��ǰFrame���������������ռ�
        // �ϵ�Java��������ܴ����������
        // ��ӵ����ڹ���������ͳһ����
        instance = this;
        IJ.register(Tools_Index.class);
        // ��ӵ����ڹ�������
        WindowManager.addWindow(this);

        // ��ȡImage J Frameʵ��
        ij = IJ.getInstance();

        // ��Ƭ���ֹ�����
        gridbag = new BorderLayout();
        // ���ֹ���������������
//        c = new GridConstraints();
        // ���ò��ֹ�����
        setLayout(gridbag);
//        c.gridx = 0;
        // ������ϱ�Ե�����񶥲�֮��ľ���
        int y = 0;
        // ���ò˵�
        jmb=new MenuBar();
        jm1=new Menu("\t�ļ�");
        jm2=new Menu("\t�༭");
        jm3=new Menu("\t����");
        jm4=new Menu("\t����");
        jm5=new Menu("\t����");
        jm4.setActionCommand("setting");
        //�Ѳ˵����벼��
        this.setMenuBar(jmb);
        // �Ѳ˵�����˵���
        jmb.add(jm1);
        jmb.add(jm2);
        jmb.add(jm3);
        jmb.add(jm4);
        jmb.add(jm5);
        //SRAD1D = new MenuItem("      SRADһά     ");
        SRAD2D = new MenuItem("      SRAD��ά      ");
        SRAD3D = new MenuItem("      SRAD��ά      ");
        AD2D = new MenuItem("      AD��ά      ");
        AD3D = new MenuItem("      AD��ά      ");
        choiceFile = new MenuItem("       ����Ŀ¼ѡ��      ");
        choiceSetting = new MenuItem("��ͨͼ���Ƽ�����");

        aboutTools = new MenuItem("�������");
        instructions = new MenuItem("����˵��");

        //SRAD1D.setActionCommand("SRAD1D");
        SRAD2D.setActionCommand("SRAD2D");
        SRAD3D.setActionCommand("SRAD3D");
        AD2D.setActionCommand("AD2D");
        AD3D.setActionCommand("AD3D");
        choiceFile.setActionCommand("choiceFile");
        choiceSetting.setActionCommand("choiceSetting");

        instructions.setActionCommand("instructions");
        aboutTools.setActionCommand("aboutTools");


        //SRAD1D.addActionListener(mAorActionListener);
        SRAD2D.addActionListener(mAorActionListener);
        SRAD3D.addActionListener(mAorActionListener);
        AD2D.addActionListener(mAorActionListener);
        AD3D.addActionListener(mAorActionListener);
        choiceFile.addActionListener(mAorActionListener);
        choiceSetting.addActionListener(mAorActionListener);
        aboutTools.addActionListener(mAorActionListener);
        instructions.addActionListener(mAorActionListener);

        //�Ѷ����˵�����һ���˵�
        jm1.add(choiceFile);
//		�ڹ�������ѡ��  �˲��㷨
        //jm3.add(SRAD1D);
        jm3.add(SRAD2D);
        jm3.add(SRAD3D);
        jm3.add(AD2D);
        jm3.add(AD3D);

        jm4.add(choiceSetting);

        jm5.add(instructions);
        jm5.add(aboutTools);

        // �ԱȶȻ���
        pan2=new Panel();
        label = new Label("�˲��㷨");
        pan2.add(label,BorderLayout.WEST);
        pan2.add(startFilter,BorderLayout.EAST);
        add(pan2,BorderLayout.NORTH);
        //�Ѳ˵����������
        this.setMenuBar(jmb);
        // ��ǩһ ���ñ�ǩ�Ĳ���
//         valNote = new Label ("          �Աȹ�����         ", Label.CENTER) ;
//        c.insets = new Insets(20, 10, 10, 10);

        // ����һ��JListʵ��
        list = new JList<String>();
        // ����һ����ѡ��С
        list.setPreferredSize(new Dimension(600, 600));
        // ����ɼ�ϵĶ�ѡ
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // ����ѡ�����ݣ��ڲ����Զ���װ�� ListModel�� ѭ���ڴ˽���
//         list.setListData(new String[]{"�㽶", "ѩ��", "ƻ��", "��֦"});
        list.setListData(list1);
        // ���ѡ��״̬���ı�ļ�����
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // ��ȡ���б�ѡ�е�ѡ������
                int[] indices = list.getSelectedIndices();
                // ��ȡѡ�����ݵ� ListModel
                ListModel<String> listModel = list.getModel();
                // ���ѡ�е�ѡ��
                if(listModel.getElementAt(indices[0]).endsWith(".png")
                        || listModel.getElementAt(indices[0]).endsWith(".jpg")
                        || listModel.getElementAt(indices[0]).endsWith(".bmp")
                        || listModel.getElementAt(indices[0]).endsWith(".dcm")
                        || listModel.getElementAt(indices[0]).endsWith(".mat")
                ){
                    try {
                        IJ.log( listModel.getElementAt(indices[0]));
                        filterFilePath = dir+listModel.getElementAt(indices[0]);
                        if(listModel.getElementAt(indices[0]).endsWith(".dcm")){
                            DICOM dicom = new DICOM();
                            dicom.run(filterFilePath);
                            filterFilePath = dir+"sourceJpg\\"+listModel.getElementAt(indices[0]);
                            filterFilePath = filterFilePath.substring(0,filterFilePath.length()-4)+".jpg";
                            BufferedImage bi = (BufferedImage) dicom.getImage();
                            ImageIO.write(bi, "jpg", new File(filterFilePath));
                            String dicomInfo = dicom.getProperty("Info").toString().split("Modality:")[1].substring(0,4).trim();
                            IJ.log("ҽѧӰ�������ͣ�"+dicomInfo);
                            //����ͼƬ�����Զ��Ƽ����ʵ��˲��㷨
                            if(dicomInfo.equals("US")){
                                label.setText("SRAD2D�˲�");
                                IJ.log("�Ƽ�ʹ��SRAD�˲��㷨");
                            }else if(dicomInfo.equals("CT")){
                                label.setText("AD2D�˲�");
                                IJ.log("�Ƽ�ʹ��AD�˲��㷨");
                            }
                            //impSrc.setStack(listModel.getElementAt(indices[0]),dicom.getImageStack());
                            impSrc.setImage(ImageIO.read(new File(filterFilePath)));
                            impSrc.setTitle("ԭʼͼ��"+listModel.getElementAt(indices[0]));
                            impSrc.show();
                        }else if(listModel.getElementAt(indices[0]).endsWith(".mat")){
                            filterFilePath = filterFilePath;
                            if(isSelectForNomalImage){
                                choiceImageTypeForNomal();
                            }
                        } else{
                            if(isSelectForNomalImage){
                                choiceImageTypeForNomal();
                            }
                            impSrc.setImage(ImageIO.read(new File(filterFilePath)));
                            impSrc.setTitle("ԭʼͼ��"+listModel.getElementAt(indices[0]));
                            impSrc.show();
                        }

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        // ����Ĭ��ѡ��
        list.setSelectedIndex(1);
        list.updateUI();
        pan1=new Panel();
        pan1.setLayout(new GridLayout(1,1));
        pan1.add(list);
        jScrollPane = new JScrollPane(pan1);
        add ( jScrollPane,BorderLayout.WEST) ;
        pack();
        GUI.center(this);
        if (IJ.isMacOSX()) {
            setResizable(false);
        }
    }
    /**
     * ���Ǹ÷���ʵ�ֳ�ʼ������
     * ����ؼ��Ĵ����벿��
     * @param arg
     */
    @Override
    public void run(String arg)
    {
        // �����ʼ��
        initView();
        show();

        thread = new Thread(this, "MyFrame");
        thread.start();

        /**
         ��ʼ��
         **/
        setup();
    }

    /**
     * �Ƿ񵯳�ѡ���
     */
    private void choiceImageTypeForNomal(){
        gd = new GenericDialog("��Dicom��ʽҽѧӰ�������ѡ��");
        gd.addChoice("��ѡ��ҽѧͼ�����ͣ�",new String[]{"US","CT","MRI"},"US");
        gd.showDialog();
        if(gd.wasCanceled()){
            return;
        }
        String nextChoice = gd.getNextChoice();
        if("US".equals(nextChoice) || "MRI".equals(nextChoice)){
            IJ.log("�Ƽ�ʹ��SRAD�㷨�˲�");
            label.setText("SRAD2D�˲�");
        }else if("CT".equals(nextChoice)){
            IJ.log("�Ƽ�ʹ��AD�㷨�˲�");
            label.setText("AD2D�˲�");
        }
    }
    private void dialogGUI()
    {
        gd = new GenericDialog(label.getText()+"v1.0");
        gd.addNumericField("Number of iterations", nb_iter, 0);
        gd.addNumericField("Lambda per iteration", nb_lambda, 0);
        gd.addMessage("Incorrect values will be replaced by defaults.\nLabels are drawn in the foreground color.\nPress Esc to stop processing.");
        gd.showDialog();
        nb_iter = (int) gd.getNextNumber();
        nb_lambda = gd.getNextNumber();
    }
    private ActionListener mAorActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            if(e.getActionCommand().equals("SRAD1D")){
//                methodArea.setText("SRAD һά�˲�");
//                itemChose = "SRAD1D";
                try {
//					test1d = new Test1d();
                    Object arg1 = "filename";
                    Object arg2 = 10;
                    Object arg3 = "1dtest";
                    Object arg4 = 10;
                    Object arg5 = 10;

//					test1d.test1d(arg1,arg2,arg3,arg4);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
            if(e.getActionCommand().equals("SRAD2D")){
//                methodArea.setText("SRAD ��ά�˲�");
//                itemChose = "SRAD2D";
                label.setText("SRAD2D�˲�");
            }
            if (e.getActionCommand().equals("SRAD3D")) {
//                methodArea.setText("SRAD ��ά�˲�");
//                itemChose = "SRAD3D";
                label.setText("SRAD3D�˲�");
            }
            if(e.getActionCommand().equals("choiceFile")){
                dir = IJ.getDir("D:\\");
                IJ.log(dir);
                list1 = new File(dir).list();
                list.updateUI();
                jScrollPane.updateUI();
            }
            if(e.getActionCommand().equals("startFilter")){
                //dialogGUI();
                startFilter();
            }
            if(e.getActionCommand().equals("AD2D")){
                label.setText("AD2D�˲�");
            }
            if(e.getActionCommand().equals("AD3D")){
                label.setText("AD3D�˲�");
            }
            if(e.getActionCommand().equals("instructions")){
                openURL(INSTRUCTIONS_URL);
            }
            if(e.getActionCommand().equals("aboutTools")){
                //openURL(ABOUTTOOLS_URL);
                gd = new GenericDialog("ҽѧӰ���˲�������v1.0");
                gd.addMessage("���ߣ����޺�");
                gd.addMessage("��λ�����ϴ�ѧ��ϢѧԺ");
                gd.addMessage("������ҳ��https://about.lcurious.cn:8888");
                gd.addHelp("https://about.lcurious.cn:8888");
                gd.addMessage(ABOUTTOOLS_CONTENT);
                gd.showDialog();
                if(gd.wasCanceled()){
                    return;
                }
            }
            if(e.getActionCommand().equals("choiceSetting")){
                gd = new GenericDialog("��Dicom��ʽ�ļ��Ƿ�����Ƽ�");
                gd.addCheckbox("���ڷ�dicom ��ʽ���ļ����Ƿ񵯳���ʽѡ���",isSelectForNomalImage);
                gd.showDialog();
                if(gd.wasCanceled()){
                    return;
                }
                isSelectForNomalImage = gd.getNextBoolean();
            }
        }
    };
    void setup()
    {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null)
        {
            setup(imp);
            this.setMinAndMax(imp, min, max) ;
            imp.updateAndDraw();
        }
    }
    public void startFilter(){
        try {
             //filterFilePath = filterFilePath.replace('\\', '/');
            Object niter =100;
            Object lamdal = 0.1;
            String imgSrc = "test1.bmp";
            //Object imgSrcO = "F:\\test1.bmp";
            Object imgSrcO = "E:\\data.jpg";
            String choseTag = label.getText().substring(0,label.getText().length()-2).trim();
            IJ.log(choseTag);
            if("SRAD2D".equals(choseTag)){
                impSrc.setImage(ImageIO.read(new File(filterFilePath)));
                impSrc.setTitle("ԭʼͼ��");
                impSrc.show();
                IJ.log("srad filter����");
                /** ��ʼ����SRAD�˲� */
                final GenericDialog dialog = new GenericDialog("��������ѡ��");
                //dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
                //dialog.addNumericField("num_iter",10, 4, 8, "");
                dialog.addChoice("��������:",new String[]{"java","matlab"},"java");
                dialog.showDialog();
                String choice = dialog.getNextChoice();
                IJ.log("ѡ��"+choice+"���Խ����˲�");
                if("java".equals(choice)){
                    startSRAD(impSrc.getProcessor());
                }else if("matlab".equals(choice)){

                    startSRADUseMatlab();
                }

//                int[] rect = new int[]{129, 1, 63, 63};
//                float[] floats = MySARD.startSRAD(impSrc.getProcessor(), 20, (float) 0.1, 1, 63, 128, (128 + 63));
//                new ImagePlus("SRAD�˲����", new FloatProcessor(impSrc.getWidth(),impSrc.getHeight(), (float[]) ((FloatProcessor)impSrc.getProcessor().convertToFloat()).getPixels())).show();
//                new ImagePlus("SRAD�˲����", new FloatProcessor(impSrc.getWidth(),impSrc.getHeight(), floats)).show();
                IJ.log("end filter����");
            }else if("AD2D".equals(choseTag)){
                impSrc.setImage(ImageIO.read(new File(filterFilePath)));
                impSrc.setTitle("ԭʼͼ��");
                impSrc.show();
                IJ.log("ad filter����");
                /** ��ʼ����AD�˲� */
                new AnisotropicDiffusion().startAD(impSrc.getProcessor(),impSrc);
                IJ.log("end filter����");
            }else if("SRAD3D".equals(choseTag)){
                IJ.log(filterFilePath);
                if(filterFilePath.endsWith("mat")){
                    startSRAD3D();
                }else{
                    IJ.error("��άͼ���ܽ�����ά�˲������ǲ���Ҫ���ж�άSRAD�˲���");
                }
            }else if("AD3D".equals(choseTag)){
                if(filterFilePath.endsWith("mat")){
                    startAD3D();
                }else{
                    IJ.error("��άͼ���ܽ�����ά�˲������ǲ���Ҫ���ж�άAD�˲���");
                }
            }
//                    impTag.setImage(ImageIO.read(new File("F:\\test1.bmp-iter-700.jpg")));
//                    impTag.setTitle("ԭʼͼ��");
//                    impTag.show();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            IJ.log(e1.getMessage());
            IJ.log("error----"+e1.getCause().getMessage());
        }
    }

    /**
     * ʹ��matlab�����˲�
     */
    private void startSRADUseMatlab() {
        final GenericDialog dialog = new GenericDialog("SRAD2D based matalb����ѡ��");
        //dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        dialog.addNumericField("niter",20, 4, 8, "");
        dialog.addNumericField("lambda", 0.1, 4, 8, "");
        dialog.addNumericField("imageSize of center", 20, 4, 8, "");
        //dialog.addNumericField("is use Thumb to filter", 1, 4, 8, "");
        dialog.addCheckbox("is use Thumb to filter",true);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }
        IJ.log("start filter");
        Object niter= (int)dialog.getNextNumber();
        Object lambda= (double)dialog.getNextNumber();
        Object centerSize= (int)dialog.getNextNumber();
        Object isTumb = 1;
        IJ.log(String.valueOf(niter));
        if(dialog.getNextBoolean()){
            isTumb = 1;
        }else{
            isTumb = 0;
        }
        IJ.log("���ڶ�"+filterFilePath+"����SRAD�˲�");
        try {
            MedicalTools medicalTools = new MedicalTools();
            IJ.log("���ڶ�"+filterFilePath+"����SRAD�˲�");
            medicalTools.SpeckleReducingnisotropicDiffusion(niter,lambda,filterFilePath,isTumb,centerSize);
            impTag.setImage(ImageIO.read(new File(filterFilePath+"-iter-"+niter+".jpg")));
            impTag.setTitle("�˲���ͼ��");
            impTag.show();
        } catch (Exception e) {
            e.printStackTrace();
            IJ.error(e.getMessage());
        }
    }

    private void startAD3D() {
        IJ.log("start ad 3D filter");
        String file = "";
        // Show options dialog
        final GenericDialog dialog = new GenericDialog("AD3D����ѡ��");
        //dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        dialog.addNumericField("num_iter",10, 4, 8, "");
        dialog.addNumericField("delta_t", 0.1429, 4, 8, "");
        dialog.addNumericField("kappa", 3, 0, 8, "");
        dialog.addNumericField("imageSize of center", 20, 4, 8, "");
        //dialog.addNumericField("is use Thumb to filter", 1, 4, 8, "");
        dialog.addCheckbox("is use Thumb to filter",true);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }
        Object num_iter= (int)dialog.getNextNumber();
        Object delta_t= (double)dialog.getNextNumber();
        Object kappa= (double)dialog.getNextNumber();
        Object imageSize= (int)dialog.getNextNumber();
        Object isTumb = 1;
        if(dialog.getNextBoolean()){
            isTumb = 1;
        }else{
            isTumb = 0;
        }

        MedicalTools medicalTools = null;
        try {
            medicalTools = new MedicalTools();
            IJ.log("��ʼ��"+filterFilePath+"�����˲�");
            medicalTools.AnisotrpicDiffusion3D(0,num_iter,delta_t,kappa,imageSize,isTumb,filterFilePath);
        } catch (MWException e) {
            e.printStackTrace();
        }
        IJ.log("end ad 3d filter");

    }

    /**
     * SRAD 3D Filter
     */
    private void startSRAD3D() {
        IJ.log("start srad 3D filter");
        final GenericDialog dialog = new GenericDialog("SRAD3D����ѡ��");
        //dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        dialog.addNumericField("niter",10, 4, 8, "");
        dialog.addNumericField("imageSize of center", 20, 4, 8, "");
        dialog.addCheckbox("is use Thumb to filter",true);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }
        Object niter = (int)dialog.getNextNumber();
        Object imageSize = (int)dialog.getNextNumber();
        Object isTumb = 1;
        if(dialog.getNextBoolean()){
             isTumb = 1;
        }else{
            isTumb = 0;
        }

        //Object imgSrcO = "F:\\test1.bmp";
        Object imgSrcO = "E:\\data.jpg";
        MedicalTools medicalTools = null;
        try {
            medicalTools = new MedicalTools();
            IJ.log("��ʼ��"+filterFilePath+"�����˲�");
            medicalTools.SpeckleReducingnisotropicDiffusion3D(0,niter,isTumb,imageSize,filterFilePath);
        } catch (MWException e) {
            e.printStackTrace();
        }
        IJ.log("end srad 3d filter");
    }


    /** Overrides close() in PlugInFrame. */
    @Override
    public void close()
    {
        super.close();
        instance = null;
        done = true;  // ???true?????????
        synchronized (this)
        {
            notify();
        }
    }

    ImageProcessor setup(ImagePlus imp)
    {
        Roi roi = imp.getRoi();
        if (roi != null) {
            roi.endPaste();
        }
        ImageProcessor ip = imp.getProcessor();
        org_min = ip.getMin() ;
        org_max = ip.getMax() ;
        min = org_min ;
        max = org_max ;

        return ip;
    }
    public void startSRAD(ImageProcessor ip){
        final FloatProcessor src = (FloatProcessor) ip.convertToFloat();
        final SRAD filter = new SRAD();
        //final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        //filter.addProgressListener(progressBarAdapter);

        // Show options dialog
        final GenericDialog dialog = new GenericDialog("SRAD����ѡ��");
        //dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        dialog.addNumericField("Diffusion coefficient threshold", filter.getCThreshold(), 4, 8, "");
        dialog.addNumericField("Mean square error limit", filter.getMeanSquareError(), 4, 8, "");
        dialog.addNumericField("Max iterations", filter.getNumberOfIterations(), 0, 8, "");
        dialog.addNumericField("Initial coefficient of variation", filter.getQ0(), 4, 8, "");
        dialog.addNumericField("Coefficient of variation decay rate", filter.getRo(), 4, 8, "");
        dialog.addNumericField("Time step", filter.getTimeStep(), 4, 8, "");
        //dialog.addHelp(HELP_URL);

        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        filter.setCThreshold(dialog.getNextNumber());
        filter.setMeanSquareError(dialog.getNextNumber());
        filter.setNumberOfIterations((int) Math.round(dialog.getNextNumber()));
        filter.setQ0(dialog.getNextNumber());
        filter.setRo(dialog.getNextNumber());
        filter.setTimeStep(dialog.getNextNumber());

        // Filter
        try {
            final FloatProcessor dest = filter.process(src);
            new ImagePlus("SRAD�˲����", dest).show();
        } finally {
            //filter.removeProgressListener(progressBarAdapter);
        }
    }
    // ??????
    @Override
    public void run()
    {

        while (!done)
        {
            synchronized (this)
            {
                try
                {
                    wait();
                }
                catch (InterruptedException e)
                {
                }
            }
            doUpdate();
        }
    }

    /**
     * ???????????
     */
    void doUpdate()
    {
        ImagePlus imp;

        imp = WindowManager.getCurrentImage();
        if (imp == null)
        {
            IJ.beep();
            IJ.showStatus("No image");
            return;
        }
        setMinAndMax(imp, min,  max) ;
        imp.updateAndDraw();
    }

    // ??????????????
    void setMinAndMax(ImagePlus imp, double min, double max)
    {
        imp.setDisplayRange(min, max);
        // disable undo
        imp.getProcessor().setSnapshotPixels(null);
    }

    // ?????????
    @Override
    public synchronized void adjustmentValueChanged(AdjustmentEvent e)
    {
        Object source = e.getSource();
        if (source == contrastSlider)
        {
            contrastValue = contrastSlider.getValue();
        }
        min = org_min*(contrastValue/100.0 );
        max = org_max*(contrastValue/100.0 );
        valLabel.setText( IJ.d2s(contrastValue/100.0, 2)) ;

        notify();
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void itemStateChanged(ItemEvent arg0)
    {
        // TODO Auto-generated method stub
    }

    public  void openURL(String url) {
        try {
            browse(url);
        } catch (Exception e) {
        }
    }

    private  void browse(String url) throws Exception {
        //��ȡ����ϵͳ������
        String osName = System.getProperty("os.name", "");
        if (osName.startsWith("Mac OS")) {
            //ƻ���Ĵ򿪷�ʽ
            Class fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
            openURL.invoke(null, new Object[] { url });
        } else if (osName.startsWith("Windows")) {
            //windows�Ĵ򿪷�ʽ��
            System.out.println("111");
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
        } else {
            // Unix or Linux�Ĵ򿪷�ʽ
            String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++)
                //ִ�д��룬��brower��ֵ��������
                //������������̴����ɹ��ˣ�==0�Ǳ�ʾ����������
                if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
                    browser = browsers[count];
            if (browser == null)
                throw new Exception("Could not find web browser");
            else
                //���ֵ�������Ѿ��ɹ��ĵõ���һ�����̡�
                Runtime.getRuntime().exec(new String[] { browser, url });
        }
    }



}

/** SRAD��������
 *  */











