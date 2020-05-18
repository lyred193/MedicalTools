import ij.IJ;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * @author lyred
 */
public class MySARD {

   public static float[] startSRAD(ImageProcessor ip, int niter, float lambda, int r1, int r2, int c1, int c2){
         /*=====================================================================
      VARIABLE DECLARATION
    =====================================================================*/

       // inputs
       float I[];                   // input image
       //int niter;                  // nbr of iterations
       //float lambda;               // update step size
        //int r1,r2,c1,c2;   // row/col coordinates of uniform ROI

       // outputs
       float J[];               // output image ("single")

       // sizes
        int Nr,Nc,Ne;  // image nbr of rows/cols/elements
        int NeROI;     // ROI   nbr of elements

       // ROI statistics
       double meanROI, varROI, q0sqr ; //local region statistics

       // surrounding pixel indicies
        int iN[],iS[],jE[],jW[];

       // center pixel value & directional derivatives
       double Jc, dN[],dS[],dW[],dE[];

       // calculation variables
       double tmp,sum,sum2;
       double G2,L,num,den,qsqr,D;

       // diffusion coefficient
       double c[], cN,cS,cW,cE;

       // counters
       int iter;   // primary loop
       int i,j;    // image row/col
       int k;      // image single index



    /*=====================================================================
      SETUP
    =====================================================================*/

       // load input variables
       I      = (float[]) ip.convertToFloat().getPixels(); ;
       //niter  = (int)mxGetScalar(prhs[1]) ;
       //lambda = (float)mxGetScalar(prhs[2]) ;
       //r1     = (int)mxGetScalar(prhs[3]) - 1;
       //r2     = (int)mxGetScalar(prhs[4]) - 1;
       //c1     = (int)mxGetScalar(prhs[5]) - 1;
       //c2     = (int)mxGetScalar(prhs[6]) - 1;

       // input image size
       Nr = ip.getWidth();
       Nc = ip.getHeight();
       Ne = Nr*Nc;

       // ROI image size
       NeROI = (r2-r1+1)*(c2-c1+1);

       // allocate local variables
       iN = new int[Nr] ;
       iS = new int[Nr] ;
       jW = new int[Nc] ;
       jE = new int[Nc] ;
       dN = new double[Ne] ;
       dS = new double[Ne] ;
       dW = new double[Ne] ;
       dE = new double[Ne] ;
       c  = new double[Ne];

       // N/S/W/E indices & boundary conditions
       for (i=0; i<Nr; i++) {
           iN[i] = i-1;
           iS[i] = i+1;
       }
       for (j=0; j<Nc; j++) {
           jW[j] = j-1;
           jE[j] = j+1;
       }
       iN[0]    = 0;
       iS[Nr-1] = Nr-1;
       jW[0]    = 0;
       jE[Nc-1] = Nc-1;

       // allocate output variables
        FloatProcessor cFP = (FloatProcessor) ip.convertToFloat().duplicate();
        J = (float[]) cFP.getPixels();


    /*=====================================================================
      SRAD
    =====================================================================*/

       // copy input to output & log uncompress
       for (k=0; k<Ne; k++) {
           J[k] = (float)Math.exp(I[k]) ;
       }

       // primary loop
       for (iter=0; iter<niter; iter++){

           // ROI statistics
           sum=0; sum2=0;
           for (i=r1; i<=r2; i++) {
               for (j=c1; j<=c2; j++) {
                   tmp   = J[i + Nr*j];
                   sum  += tmp ;
                   sum2 += tmp*tmp;
               }
           }
           meanROI = sum / NeROI;
           varROI  = (sum2 / NeROI) - meanROI*meanROI;
           q0sqr   = varROI / (meanROI*meanROI);    // 原始程序
           //  q0sqr = pow(exp(-0.05/6),2);   // 吴俊 ：pow(x,2) = x的平方；exp()还是自然对数
           //  q0sqr = pow(exp(-1/6),2);        // 吴俊

           // directional derivatives, ICOV, diffusion coefficent
           for (j=0; j<Nc; j++) {
               for (i=0; i<Nr; i++) {

                   // current index/pixel
                   k = i + Nr*j;
                   Jc = J[k];

                   // directional derivates
                   dN[k] = J[iN[i] + Nr*j] - Jc;
                   dS[k] = J[iS[i] + Nr*j] - Jc;
                   dW[k] = J[i + Nr*jW[j]] - Jc;
                   dE[k] = J[i + Nr*jE[j]] - Jc;

                   // normalized discrete gradient mag squared (equ 52,53)
                   G2 = (dN[k]*dN[k] + dS[k]*dS[k]
                           + dW[k]*dW[k] + dE[k]*dE[k]) / (Jc*Jc);

                   // normalized discrete laplacian (equ 54)
                   L = (dN[k] + dS[k] + dW[k] + dE[k]) / Jc;

                   // ICOV (equ 31/35)
                   num  =  ((0.5*G2) - ((1.0/16.0)*(L*L)));
                   den  =  (1 + (.25*L));
                   qsqr = num/(den*den);

                   // diffusion coefficent (equ 33)
                   den = (qsqr-q0sqr) / (q0sqr * (1+q0sqr)) ;
                   c[k] =  (1.0 / (1.0+den));

                   // saturate diffusion coefficent
                   if (c[k] < 0) {c[k] = 0;}
                   else if (c[k] > 1) {c[k] = 1;}

               }
           }

           // divergence & image update
           for (j=0; j<Nc; j++) {
               for (i=0; i<Nr; i++) {

                   // current index
                   k = i + Nr*j;

                   // diffusion coefficent
                   cN = c[k];
                   cS = c[iS[i] + Nr*j];
                   cW = c[k];
                   cE = c[i + Nr*jE[j]];

                   // divergence (equ 58)
                   D = cN*dN[k] + cS*dS[k] + cW*dW[k] + cE*dE[k];

                   // image update (equ 61)
                   J[k] = (float) (J[k] + 0.25*lambda*D);
               }
           }
       } // end primary loop.

       //log compress
       for (k=0; k<Ne; k++) {
           J[k] = (float) Math.log(J[k]);
       }
       IJ.log(String.valueOf(I[Ne/10]));
       IJ.log(String.valueOf(Ne));
       return J;
    }
}
