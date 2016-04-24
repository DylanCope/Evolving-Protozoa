package biology;

import java.util.Random;


public interface Brain 
{
	public double turn(Protozoa p);
	public double speed(Protozoa p);
	
	static final Random r = new Random();
	public static final Brain RANDOM = new Brain() {

		@Override
		public double turn(Protozoa p) {
			double x = 2*r.nextDouble() - 1;
			double t = Math.toRadians(35);
			return t*x;
		}

		@Override
		public double speed(Protozoa p) {
			return r.nextDouble();
		}
		
	};
}
