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
	// �����߳�
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
	
	// �������ֵ
	int sliderRange=200 ;
	
	// �����Ӧ��ֵ
	int contrastValue = sliderRange/2 ;

	
	// ͼ�����ֵ��Сֵ
	double min, max, org_min, org_max ;
	
	// �˳���־
	boolean	done = false ;
	
	GridBagLayout gridbag ; // ���ֹ�����
	GridBagConstraints c; 	// ���ֹ���������������
	Panel pan1, pan2, pan3 ; // ����
	
	public Plugin_Frame()
	{
		super("B&C");
	}

/*
���Ǹ÷���ʵ�ֳ�ʼ��������
����ؼ��Ĵ����벿��
*/
	public void run(String arg)
	{
		// ���ñ���
		setTitle("Contrast & Brightness");
		
		/*
ע�ᵱǰ�ൽһ���������ֹ��ǰFrame���������ռ����ռ�,
�ϵ�Java�����������������⣬
��ӵ����ڹ��������д��ڵ�ͳһ����
*/
		instance = this;
		IJ.register(Plugin_Frame.class);
		WindowManager.addWindow(this); // ��ӵ����ڹ�������

		// ���ImageJ Frame��ʵ��������
		ij = IJ.getInstance(); 
		
		/* 
        ͬһʹ��GridBagLayout���ֹ������Կؼ����в���
        Grid��x��ʾ���У�y��ʾ�У�y++����ʾ����һ�С�
        �ؼ�ʹ������panel��װ��ʹ�Ŀؼ����Է�����һ�µĴ�С��
 		����ScrollBarʼ�շǳ�С����ǩҲ���Զ��롣
		*/
		gridbag = new GridBagLayout(); // ���ֹ�����
		c = new GridBagConstraints(); // ���ֹ���������������
		setLayout(gridbag); // ���ò��ֹ�����		
		c.gridx = 0;
		int y = 0; // ������ϱ�Ե�����񶥲�֮��ľ���		
		c.gridy = y++;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		
		// ��ǩ1�����ñ�ǩ�Ĳ���
		valNote = new Label ("          �Աȶȹ�����            ", Label.CENTER) ; 

		c.insets = new Insets(20, 10, 10, 10);
		pan1=new Panel();
		pan1.setLayout(new BorderLayout());  
		pan1.add( valNote ) ;
		gridbag.setConstraints(pan1, c);
		add ( pan1 ) ;
		
		// �ԱȶȻ���
		contrastSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange / 2, 1, 0, sliderRange );
		contrastSlider.addAdjustmentListener(this);
		contrastSlider.addKeyListener(ij);
		contrastSlider.setUnitIncrement(1);
		contrastSlider.setFocusable(false);
		c.insets = new Insets(10, 10, 10, 10);
		c.gridy = y++;		
		pan2=new Panel();
		pan2.setLayout(new BorderLayout());  //�������,ScrollBar���ܱ�ýϴ�
		pan2.add(contrastSlider) ;
		gridbag.setConstraints(pan2, c);		
		add (pan2 ) ;	
		
		// ��ǩ2
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
		���߳������run()ʵ�ֶԸı��min��max����Ӧ��
		�����ڶ�ͼ��Ĵ�����
		**/
		thread = new Thread(this, "MyFrame");
		thread.start();

		/**
		��ʼ��ͼ����ֵ����
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
		done = true;  // ���ó�true���Ա��߳��˳�ѭ��
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

	// �߳�ִ�к���
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
	
	// �̶߳�ͼ������úʹ���
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
	
	// ����ͼ��������С���ȡֵ��Χ
	void setMinAndMax(ImagePlus imp, double min, double max)
	{
		imp.setDisplayRange(min, max);
		imp.getProcessor().setSnapshotPixels(null); // disable undo
	}
	
	// �������¼�������
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





