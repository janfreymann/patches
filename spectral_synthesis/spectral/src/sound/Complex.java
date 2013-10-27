package sound;

public class Complex {
	  private double re;
	  private double im;
	  
	  public Complex () {
		    this.re = 0;
		    this.im = 0;
	  }
		  
	  public Complex(double re, double im) {
		  this.re = re;
		  this.im = im;
	  }

	  public Complex(Complex input) {
		  this.re = input.getRe();
		  this.im = input.getIm();
	  }
	  public void setRe(double re) {
		  this.re = re;
	  }

	  public void setIm(double im) {
		  this.im = im;
	  }   
	  public double getRe() {
		  return this.re;
	  }

	  public double getIm() {
		  return this.im;
	  }
	  public Complex getConjugate() {
		  return new Complex(this.re, this.im * (-1));
	  }
	  public double getMagnitude() {
		  return Math.sqrt(re*re + im*im);
	  }
	  
	  public Complex add(Complex op) {
		  Complex result = new Complex();
		  result.setRe(this.re + op.getRe());
		  result.setIm(this.im + op.getIm());
		  return result;
	  }

	  public Complex sub(Complex op) {
		  Complex result = new Complex();
		  result.setRe(this.re - op.getRe());
		  result.setIm(this.im - op.getIm());
		  return result;
	  }

	  public Complex mul(Complex op) {
		  Complex result = new Complex();
		  result.setRe(this.re * op.getRe() - this.im * op.getIm());
		  result.setIm(this.re * op.getIm() + this.im * op.getRe());
		  return result;
	  }

	  public Complex div(Complex op) {
		  Complex result = new Complex(this);
		  result = result.mul(op.getConjugate());
		  double opNormSq = op.getRe()*op.getRe()+op.getIm()*op.getIm();
		  result.setRe(result.getRe() / opNormSq);
		  result.setIm(result.getIm() / opNormSq);
		  return result;
	  }
	  public String toString() {
		  if (this.re == 0) {
			  if (this.im == 0) {
				  return "0";
			  } else {
				  return (this.im + "i");
			  }
		  } else {
			  if (this.im == 0) {
				  return String.valueOf(this.re);
			  } else if (this.im < 0) {
				  return(this.re + " " + this.im + "i");
			  } else {
				  return(this.re + " +" + this.im + "i");
			  }
		  }
	  }
	  public Complex ffthelper(int n, int k, Complex u) {
		  double kth = -2.0 * (double) k * Math.PI / (double) n;
		  //System.out.println(u.getIm() + " " + u.getRe());   //imaginary part is always 0?
		  //System.out.println((new Complex(Math.cos(kth), Math.sin(kth)).toString()));
		  return (new Complex(Math.cos(kth), Math.sin(kth)).mul(u));
	  }
}
