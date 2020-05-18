

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

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

import SpeckleReducingnisotropicDiffusion.SRAD;
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
    MenuItem AD;
    MenuItem SRAD1D;
    MenuItem SRAD2D;
    MenuItem SRAD3D;
    MenuItem choiceFile;
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
        jm4=new Menu("\t�鿴");
        jm5=new Menu("\t����");
        //�Ѳ˵����벼��
        this.setMenuBar(jmb);
        // �Ѳ˵�����˵���
        jmb.add(jm1);
        jmb.add(jm2);
        jmb.add(jm3);
        jmb.add(jm4);
        jmb.add(jm5);
        SRAD1D = new MenuItem("      SRADһά     ");
        SRAD2D = new MenuItem("      SRAD��ά      ");
        SRAD3D = new MenuItem("      SRAD��ά      ");
        AD = new MenuItem("AD�˲�");
        choiceFile = new MenuItem("       ����Ŀ¼ѡ��      ");
        SRAD1D.setActionCommand("SRAD1D");
        SRAD2D.setActionCommand("SRAD2D");
        SRAD3D.setActionCommand("SRAD3D");
        AD.setActionCommand("AD");
        choiceFile.setActionCommand("choiceFile");

        SRAD1D.addActionListener(mAorActionListener);
        SRAD2D.addActionListener(mAorActionListener);
        SRAD3D.addActionListener(mAorActionListener);
        AD.addActionListener(mAorActionListener);
        choiceFile.addActionListener(mAorActionListener);

        //�Ѷ����˵�����һ���˵�
        jm1.add(choiceFile);
//		�ڹ�������ѡ��  �˲��㷨
        jm3.add(SRAD1D);
        jm3.add(SRAD2D);
        jm3.add(SRAD3D);
        jm3.add(AD);
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
                ){
                    try {
                        IJ.log( listModel.getElementAt(indices[0]));
                        filterFilePath = dir+"\\"+listModel.getElementAt(indices[0]);
                        if(listModel.getElementAt(indices[0]).endsWith(".dcm")){
                            DICOM dicom = new DICOM();
                            dicom.run(filterFilePath);
                            filterFilePath = dir+"\\sourceJpg\\"+listModel.getElementAt(indices[0]);
                            BufferedImage bi = (BufferedImage) dicom.getImage();
                            ImageIO.write(bi, "jpg", new File(filterFilePath.substring(0,filterFilePath.length()-4)+".jpg"));
                            String dicomInfo = dicom.getProperty("Info").toString().split("Modality:")[1].substring(0,4).trim();
                            IJ.log("ҽѧӰ�������ͣ�"+dicomInfo);
                            //����ͼƬ�����Զ��Ƽ����ʵ��˲��㷨
                            if(dicomInfo.equals("US")){
                                label.setText("SRAD ��ά�˲�");
                                IJ.log("�Ƽ�ʹ��SRAD�˲��㷨");
                            }else if(dicomInfo.equals("CT")){
                                label.setText("AD ��ά�˲�");
                                IJ.log("�Ƽ�ʹ��AD�˲��㷨");
                            }
                            //impSrc.setStack(listModel.getElementAt(indices[0]),dicom.getImageStack());
                            impSrc.setImage(ImageIO.read(new File(filterFilePath.substring(0,filterFilePath.length()-4)+".jpg")));
                            impSrc.setTitle("ԭʼͼ��"+listModel.getElementAt(indices[0]));
                            impSrc.show();
                        }else{
                            impSrc.setImage(ImageIO.read(new File(dir+"\\"+listModel.getElementAt(indices[0]))));
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
                label.setText("SRAD ��ά�˲�");
            }
            if (e.getActionCommand().equals("SRAD3D")) {
//                methodArea.setText("SRAD ��ά�˲�");
//                itemChose = "SRAD3D";
                label.setText("SRAD ��ά�˲�");
            }
            if(e.getActionCommand().equals("choiceFile")){
                dir = IJ.getDir("D:\\");
                IJ.log(dir);
                list1 = new File(dir).list();
                list.updateUI();
                jScrollPane.updateUI();
            }
            if(e.getActionCommand().equals("startFilter")){
            	  dialogGUI();
            	 
                IJ.log("��ʼ��"+filterFilePath+label.getText()+"�˲�");
                IJ.log("����������"+nb_iter);
                IJ.log("Lambda:"+nb_lambda);
                // �����˲�����ѡ��
				                // �����˲�����ѡ��
                try {
                    Object niter =100;
                    Object lamdal = 0.1;
                    String imgSrc = "test1.bmp";
                    //Object imgSrcO = "F:\\test1.bmp";
                    Object imgSrcO = "E:\\data.jpg";
                    impSrc.setImage(ImageIO.read(new File("E:\\data.jpg")));
                    impSrc.setTitle("ԭʼͼ��");
                    impSrc.show();
                    IJ.log("srad filter����");
                    SRAD srad = new SRAD();
                    srad.SpeckleReducingnisotropicDiffusion( niter,lamdal,imgSrcO);
					
                    IJ.log("end filter����");
                    impTag.setImage(ImageIO.read(new File("F:\\test1.bmp-iter-700.jpg")));
                    impTag.setTitle("ԭʼͼ��");
                    impTag.show();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
					 IJ.log(e1.getMessage());
                    IJ.log("error");
                }
              
            }
            if(e.getActionCommand().equals("AD")){
                label.setText("AD�˲�");
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
}





