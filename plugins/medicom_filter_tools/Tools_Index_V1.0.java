

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
     * 自身线程
     */
    Thread thread;

    // 主窗口
    ImageJ ij;

    // 当前Frame的实例
    static Frame instance;

    // 滚动条
    Scrollbar contrastSlider;

    // 标签
    Label valNote ;
    Label valLabel ;

    // 滑动的最大值
    int sliderRange=200 ;

    /** 滑块对应值 */
    int contrastValue = sliderRange/2 ;


    /**  图像最大值与最小值 */
    double min, max, org_min, org_max ;

    /** 退出标志 */
    boolean	done = false ;

    /** 卡片布局管理器  */
    BorderLayout gridbag ;
    /** 布局管理器常量变量类 */
//    GridBagConstraints c;
    /** 容器 */
    Panel pan1, pan2, pan3 ;
    /** 菜单相关 */
    MenuBar jmb;
    Menu jm1,jm2,jm3,jm4,jm5;
    /** 二级菜单 */
    MenuItem jmt1=null;
    MenuItem jmt2=null;
    /** 二级菜单 滤波算法选择  */
    MenuItem AD;
    MenuItem SRAD1D;
    MenuItem SRAD2D;
    MenuItem SRAD3D;
    MenuItem choiceFile;
    Label label;
    /** 图片显示 */
    ImagePlus impSrc;
    ImagePlus impTag;
    String[] list1;
    /** 文件列表 */
     JList<String> list;
    String dir;
    JScrollPane jScrollPane;
    /** 按钮组 */
    Button startFilter;
    String filterFilePath;
    /** 滤波参数选择 */
    GenericDialog gd;
    int nb_iter=20;
    double nb_lambda=0.1f;
    public Tools_Index()
    {
        super("B&C");
        this.setSize(300,300);
    }

    /**
     * 界面初始化
     */
    private void initView(){
        // 弹出框 选择工作目录
        IJ.showMessage("工作目录选择路径","请选择工作目录");
         dir = IJ.getDir("../");
        IJ.log(dir);
		File sourceFile = new File(dir+"/sourceJpg");
        if (!sourceFile.exists()) {
            sourceFile.mkdirs();
        }
        list1 = new File(dir).list();
        impSrc = new ImagePlus();
        impTag = new ImagePlus();
        startFilter = new Button("开始滤波");
        startFilter.setActionCommand("startFilter");
        startFilter.addActionListener(mAorActionListener);
//        String [] fileList = new String[list1.length];
        int i=0;
        //设置标题
        setTitle("医学图像处理工具箱");
        // 注册当前类到一个向量里，防止当前Frame对象被垃圾回收器收集
        // 老的Java虚拟机可能存在这个问题
        // 添加到窗口管理器进行统一管理
        instance = this;
        IJ.register(Tools_Index.class);
        // 添加到窗口管理器中
        WindowManager.addWindow(this);

        // 获取Image J Frame实例
        ij = IJ.getInstance();

		// 卡片布局管理器
        gridbag = new BorderLayout();
        // 布局管理器产量变量类
//        c = new GridConstraints();
        // 设置布局管理器
        setLayout(gridbag);
//        c.gridx = 0;
        // 组件的上边缘与网格顶部之间的距离
        int y = 0;
        // 设置菜单
        jmb=new MenuBar();
        jm1=new Menu("\t文件");
        jm2=new Menu("\t编辑");
        jm3=new Menu("\t工具");
        jm4=new Menu("\t查看");
        jm5=new Menu("\t帮助");
        //把菜单放入布局
        this.setMenuBar(jmb);
        // 把菜单放入菜单条
        jmb.add(jm1);
        jmb.add(jm2);
        jmb.add(jm3);
        jmb.add(jm4);
        jmb.add(jm5);
        SRAD1D = new MenuItem("      SRAD一维     ");
        SRAD2D = new MenuItem("      SRAD二维      ");
        SRAD3D = new MenuItem("      SRAD三维      ");
        AD = new MenuItem("AD滤波");
        choiceFile = new MenuItem("       工作目录选择      ");
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

        //把二级菜单放入一级菜单
        jm1.add(choiceFile);
//		在工具箱中选择  滤波算法
        jm3.add(SRAD1D);
        jm3.add(SRAD2D);
        jm3.add(SRAD3D);
        jm3.add(AD);
		        // 对比度滑块
        pan2=new Panel();
         label = new Label("滤波算法");
        pan2.add(label,BorderLayout.WEST);
        pan2.add(startFilter,BorderLayout.EAST);
        add(pan2,BorderLayout.NORTH);
        //把菜单条放入面板
        this.setMenuBar(jmb);
        // 标签一 设置标签的布局
//         valNote = new Label ("          对比滚动条         ", Label.CENTER) ;
//        c.insets = new Insets(20, 10, 10, 10);

        // 创建一个JList实例
         list = new JList<String>();
        // 设置一下首选大小
        list.setPreferredSize(new Dimension(600, 600));
        // 允许可间断的多选
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // 设置选项数据（内部将自动封装成 ListModel） 循环在此进行
//         list.setListData(new String[]{"香蕉", "雪梨", "苹果", "荔枝"});
        list.setListData(list1);
        // 添加选项状态被改变的监听器
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // 获取所有被选中的选项索引
                int[] indices = list.getSelectedIndices();
                // 获取选项数据的 ListModel
                ListModel<String> listModel = list.getModel();
                // 输出选中的选项
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
                            IJ.log("医学影像像类型："+dicomInfo);
                            //根据图片类型自动推荐合适的滤波算法
                            if(dicomInfo.equals("US")){
                                label.setText("SRAD 二维滤波");
                                IJ.log("推荐使用SRAD滤波算法");
                            }else if(dicomInfo.equals("CT")){
                                label.setText("AD 二维滤波");
                                IJ.log("推荐使用AD滤波算法");
                            }
                            //impSrc.setStack(listModel.getElementAt(indices[0]),dicom.getImageStack());
                            impSrc.setImage(ImageIO.read(new File(filterFilePath.substring(0,filterFilePath.length()-4)+".jpg")));
                            impSrc.setTitle("原始图像"+listModel.getElementAt(indices[0]));
                            impSrc.show();
                        }else{
                            impSrc.setImage(ImageIO.read(new File(dir+"\\"+listModel.getElementAt(indices[0]))));
                            impSrc.setTitle("原始图像"+listModel.getElementAt(indices[0]));
                            impSrc.show();
                        }
       
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        // 设置默认选项
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
     * 覆盖该方法实现初始化功能
     * 界面控件的创建与部署
     * @param arg
     */
    @Override
    public void run(String arg)
    {
    	// 界面初始化
    	initView();
        show();

        thread = new Thread(this, "MyFrame");
        thread.start();

        /**
         	初始化
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
//                methodArea.setText("SRAD 一维滤波");
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
//                methodArea.setText("SRAD 二维滤波");
//                itemChose = "SRAD2D";
                label.setText("SRAD 二维滤波");
            }
            if (e.getActionCommand().equals("SRAD3D")) {
//                methodArea.setText("SRAD 三维滤波");
//                itemChose = "SRAD3D";
                label.setText("SRAD 三维滤波");
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
            	 
                IJ.log("开始对"+filterFilePath+label.getText()+"滤波");
                IJ.log("迭代次数："+nb_iter);
                IJ.log("Lambda:"+nb_lambda);
                // 弹出滤波参数选择
				                // 弹出滤波参数选择
                try {
                    Object niter =100;
                    Object lamdal = 0.1;
                    String imgSrc = "test1.bmp";
                    //Object imgSrcO = "F:\\test1.bmp";
                    Object imgSrcO = "E:\\data.jpg";
                    impSrc.setImage(ImageIO.read(new File("E:\\data.jpg")));
                    impSrc.setTitle("原始图像");
                    impSrc.show();
                    IJ.log("srad filter……");
                    SRAD srad = new SRAD();
                    srad.SpeckleReducingnisotropicDiffusion( niter,lamdal,imgSrcO);
					
                    IJ.log("end filter……");
                    impTag.setImage(ImageIO.read(new File("F:\\test1.bmp-iter-700.jpg")));
                    impTag.setTitle("原始图像");
                    impTag.show();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
					 IJ.log(e1.getMessage());
                    IJ.log("error");
                }
              
            }
            if(e.getActionCommand().equals("AD")){
                label.setText("AD滤波");
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





