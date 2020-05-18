

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
    String ABOUTTOOLS_CONTENT = "2020年4月编写完成，含有斑点降噪各向异性扩散、各向异性扩散二维三维滤波。";
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
    /** SRAD参数 */

    /* 对于非dicom 格式的文件，是否弹出选择框 */
    boolean isSelectForNomalImage = true;
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
        jm4=new Menu("\t设置");
        jm5=new Menu("\t帮助");
        jm4.setActionCommand("setting");
        //把菜单放入布局
        this.setMenuBar(jmb);
        // 把菜单放入菜单条
        jmb.add(jm1);
        jmb.add(jm2);
        jmb.add(jm3);
        jmb.add(jm4);
        jmb.add(jm5);
        //SRAD1D = new MenuItem("      SRAD一维     ");
        SRAD2D = new MenuItem("      SRAD二维      ");
        SRAD3D = new MenuItem("      SRAD三维      ");
        AD2D = new MenuItem("      AD二维      ");
        AD3D = new MenuItem("      AD三维      ");
        choiceFile = new MenuItem("       工作目录选择      ");
        choiceSetting = new MenuItem("普通图像推荐配置");

        aboutTools = new MenuItem("关于软件");
        instructions = new MenuItem("操作说明");

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

        //把二级菜单放入一级菜单
        jm1.add(choiceFile);
//		在工具箱中选择  滤波算法
        //jm3.add(SRAD1D);
        jm3.add(SRAD2D);
        jm3.add(SRAD3D);
        jm3.add(AD2D);
        jm3.add(AD3D);

        jm4.add(choiceSetting);

        jm5.add(instructions);
        jm5.add(aboutTools);

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
                            IJ.log("医学影像像类型："+dicomInfo);
                            //根据图片类型自动推荐合适的滤波算法
                            if(dicomInfo.equals("US")){
                                label.setText("SRAD2D滤波");
                                IJ.log("推荐使用SRAD滤波算法");
                            }else if(dicomInfo.equals("CT")){
                                label.setText("AD2D滤波");
                                IJ.log("推荐使用AD滤波算法");
                            }
                            //impSrc.setStack(listModel.getElementAt(indices[0]),dicom.getImageStack());
                            impSrc.setImage(ImageIO.read(new File(filterFilePath)));
                            impSrc.setTitle("原始图像"+listModel.getElementAt(indices[0]));
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

    /**
     * 是否弹出选择框
     */
    private void choiceImageTypeForNomal(){
        gd = new GenericDialog("非Dicom格式医学影像的类型选择");
        gd.addChoice("请选择医学图像类型：",new String[]{"US","CT","MRI"},"US");
        gd.showDialog();
        if(gd.wasCanceled()){
            return;
        }
        String nextChoice = gd.getNextChoice();
        if("US".equals(nextChoice) || "MRI".equals(nextChoice)){
            IJ.log("推荐使用SRAD算法滤波");
            label.setText("SRAD2D滤波");
        }else if("CT".equals(nextChoice)){
            IJ.log("推荐使用AD算法滤波");
            label.setText("AD2D滤波");
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
                label.setText("SRAD2D滤波");
            }
            if (e.getActionCommand().equals("SRAD3D")) {
//                methodArea.setText("SRAD 三维滤波");
//                itemChose = "SRAD3D";
                label.setText("SRAD3D滤波");
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
                label.setText("AD2D滤波");
            }
            if(e.getActionCommand().equals("AD3D")){
                label.setText("AD3D滤波");
            }
            if(e.getActionCommand().equals("instructions")){
                openURL(INSTRUCTIONS_URL);
            }
            if(e.getActionCommand().equals("aboutTools")){
                //openURL(ABOUTTOOLS_URL);
                gd = new GenericDialog("医学影像滤波工具箱v1.0");
                gd.addMessage("作者：李艳红");
                gd.addMessage("单位：云南大学信息学院");
                gd.addMessage("个人主页：https://about.lcurious.cn:8888");
                gd.addHelp("https://about.lcurious.cn:8888");
                gd.addMessage(ABOUTTOOLS_CONTENT);
                gd.showDialog();
                if(gd.wasCanceled()){
                    return;
                }
            }
            if(e.getActionCommand().equals("choiceSetting")){
                gd = new GenericDialog("非Dicom格式文件是否进行推荐");
                gd.addCheckbox("对于非dicom 格式的文件，是否弹出格式选择框",isSelectForNomalImage);
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
                impSrc.setTitle("原始图像");
                impSrc.show();
                IJ.log("srad filter……");
                /** 开始进行SRAD滤波 */
                final GenericDialog dialog = new GenericDialog("运行语言选择");
                //dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
                //dialog.addNumericField("num_iter",10, 4, 8, "");
                dialog.addChoice("运行语言:",new String[]{"java","matlab"},"java");
                dialog.showDialog();
                String choice = dialog.getNextChoice();
                IJ.log("选择"+choice+"语言进行滤波");
                if("java".equals(choice)){
                    startSRAD(impSrc.getProcessor());
                }else if("matlab".equals(choice)){

                    startSRADUseMatlab();
                }

//                int[] rect = new int[]{129, 1, 63, 63};
//                float[] floats = MySARD.startSRAD(impSrc.getProcessor(), 20, (float) 0.1, 1, 63, 128, (128 + 63));
//                new ImagePlus("SRAD滤波结果", new FloatProcessor(impSrc.getWidth(),impSrc.getHeight(), (float[]) ((FloatProcessor)impSrc.getProcessor().convertToFloat()).getPixels())).show();
//                new ImagePlus("SRAD滤波结果", new FloatProcessor(impSrc.getWidth(),impSrc.getHeight(), floats)).show();
                IJ.log("end filter……");
            }else if("AD2D".equals(choseTag)){
                impSrc.setImage(ImageIO.read(new File(filterFilePath)));
                impSrc.setTitle("原始图像");
                impSrc.show();
                IJ.log("ad filter……");
                /** 开始进行AD滤波 */
                new AnisotropicDiffusion().startAD(impSrc.getProcessor(),impSrc);
                IJ.log("end filter……");
            }else if("SRAD3D".equals(choseTag)){
                IJ.log(filterFilePath);
                if(filterFilePath.endsWith("mat")){
                    startSRAD3D();
                }else{
                    IJ.error("二维图像不能进行三维滤波，你是不是要进行二维SRAD滤波？");
                }
            }else if("AD3D".equals(choseTag)){
                if(filterFilePath.endsWith("mat")){
                    startAD3D();
                }else{
                    IJ.error("二维图像不能进行三维滤波，你是不是要进行二维AD滤波？");
                }
            }
//                    impTag.setImage(ImageIO.read(new File("F:\\test1.bmp-iter-700.jpg")));
//                    impTag.setTitle("原始图像");
//                    impTag.show();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            IJ.log(e1.getMessage());
            IJ.log("error----"+e1.getCause().getMessage());
        }
    }

    /**
     * 使用matlab进行滤波
     */
    private void startSRADUseMatlab() {
        final GenericDialog dialog = new GenericDialog("SRAD2D based matalb参数选择");
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
        IJ.log("正在对"+filterFilePath+"进行SRAD滤波");
        try {
            MedicalTools medicalTools = new MedicalTools();
            IJ.log("正在对"+filterFilePath+"进行SRAD滤波");
            medicalTools.SpeckleReducingnisotropicDiffusion(niter,lambda,filterFilePath,isTumb,centerSize);
            impTag.setImage(ImageIO.read(new File(filterFilePath+"-iter-"+niter+".jpg")));
            impTag.setTitle("滤波后图像");
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
        final GenericDialog dialog = new GenericDialog("AD3D参数选择");
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
            IJ.log("开始对"+filterFilePath+"进行滤波");
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
        final GenericDialog dialog = new GenericDialog("SRAD3D参数选择");
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
            IJ.log("开始对"+filterFilePath+"进行滤波");
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
        final GenericDialog dialog = new GenericDialog("SRAD参数选择");
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
            new ImagePlus("SRAD滤波结果", dest).show();
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
        //获取操作系统的名字
        String osName = System.getProperty("os.name", "");
        if (osName.startsWith("Mac OS")) {
            //苹果的打开方式
            Class fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
            openURL.invoke(null, new Object[] { url });
        } else if (osName.startsWith("Windows")) {
            //windows的打开方式。
            System.out.println("111");
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
        } else {
            // Unix or Linux的打开方式
            String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++)
                //执行代码，在brower有值后跳出，
                //这里是如果进程创建成功了，==0是表示正常结束。
                if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
                    browser = browsers[count];
            if (browser == null)
                throw new Exception("Could not find web browser");
            else
                //这个值在上面已经成功的得到了一个进程。
                Runtime.getRuntime().exec(new String[] { browser, url });
        }
    }



}

/** SRAD处理主类
 *  */











