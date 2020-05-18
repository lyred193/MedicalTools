import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.frame.*;
import javax.swing.*;

public class Plugin_Frame extends PlugInFrame implements Runnable,
		ActionListener, AdjustmentListener, ItemListener
{
	// 本身线程
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
	
	// 滑动最大值
	int sliderRange=200 ;
	
	// 滑块对应的值
	int contrastValue = sliderRange/2 ;

	
	// 图象最大值最小值
	double min, max, org_min, org_max ;
	
	// 退出标志
	boolean	done = false ;
	
	GridBagLayout gridbag ; // 布局管理器
	GridBagConstraints c; 	// 布局管理器常量变量类
	Panel pan1, pan2, pan3 ; // 容器
	
	public Plugin_Frame()
	{
		super("B&C");
	}

/*
覆盖该方法实现初始化操作。
界面控件的创建与部署。
*/
	public void run(String arg)
	{
		// 设置标题
		setTitle("Contrast & Brightness");
		
		/*
注册当前类到一个向量里，防止当前Frame对象被垃圾收集器收集,
老的Java虚拟机可能有这个问题，
添加到窗口管理器进行窗口的统一管理。
*/
		instance = this;
		IJ.register(Plugin_Frame.class);
		WindowManager.addWindow(this); // 添加到窗口管理器中

		// 获得ImageJ Frame的实例的引用
		ij = IJ.getInstance(); 
		
		/* 
        同一使用GridBagLayout布局管理器对控件进行部署。
        Grid下x表示在列，y表示行，y++，表示在下一行。
        控件使用容器panel包装，使的控件可以放缩到一致的大小，
 		否则ScrollBar始终非常小，标签也难以对齐。
		*/
		gridbag = new GridBagLayout(); // 布局管理器
		c = new GridBagConstraints(); // 布局管理器常量变量类
		setLayout(gridbag); // 设置布局管理器		
		c.gridx = 0;
		int y = 0; // 组件的上边缘与网格顶部之间的距离		
		c.gridy = y++;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		
		// 标签1，设置标签的布局
		valNote = new Label ("          对比度滚动条            ", Label.CENTER) ; 

		c.insets = new Insets(20, 10, 10, 10);
		pan1=new Panel();
		pan1.setLayout(new BorderLayout());  
		pan1.add( valNote ) ;
		gridbag.setConstraints(pan1, c);
		add ( pan1 ) ;
		
		// 对比度滑块
		contrastSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange / 2, 1, 0, sliderRange );
		contrastSlider.addAdjustmentListener(this);
		contrastSlider.addKeyListener(ij);
		contrastSlider.setUnitIncrement(1);
		contrastSlider.setFocusable(false);
		c.insets = new Insets(10, 10, 10, 10);
		c.gridy = y++;		
		pan2=new Panel();
		pan2.setLayout(new BorderLayout());  //设置这个,ScrollBar才能变得较大
		pan2.add(contrastSlider) ;
		gridbag.setConstraints(pan2, c);		
		add (pan2 ) ;	
		
		// 标签2
		valLabel = new Label ("1.00", Label.CENTER ) ;
		c.insets = new Insets(2, 10, 20, 10);
		c.gridy = y++;		
		pan3=new Panel();
		pan3.setLayout(new BorderLayout());  
		pan3.add( valLabel) ;
		gridbag.setConstraints(pan3, c);
		add ( pan3 ) ;

		pack();
		GUI.center(this);
		if (IJ.isMacOSX())
			setResizable(false);
		show();

		/**
		在线程里调用run()实现对改变的min和max的响应，
		体现在对图象的处理上
		**/
		thread = new Thread(this, "MyFrame");
		thread.start();

		/**
		初始的图象及数值设置
		**/
		setup();
	}

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
	public void close()
	{
		super.close();
		instance = null;
		done = true;  // 设置成true，以便线程退出循环
		synchronized (this)
		{
			notify();
		}
	}

	ImageProcessor setup(ImagePlus imp)
	{
		Roi roi = imp.getRoi();
		if (roi != null)
			roi.endPaste();
		ImageProcessor ip = imp.getProcessor();
		org_min = ip.getMin() ;
		org_max = ip.getMax() ;
		min = org_min ;
		max = org_max ;
	
		return ip;
	}

	// 线程执行函数
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
	
	// 线程对图象的设置和处理
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
	
	// 设置图象像素最小最大取值范围
	void setMinAndMax(ImagePlus imp, double min, double max)
	{
		imp.setDisplayRange(min, max);
		imp.getProcessor().setSnapshotPixels(null); // disable undo
	}
	
	// 滑动条事件处理函数
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
	
	public synchronized void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
	}

	public void itemStateChanged(ItemEvent arg0)
	{
		// TODO Auto-generated method stub		
	}
}





