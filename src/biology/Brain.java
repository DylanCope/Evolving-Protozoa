package biology;

import core.Simulation;

public interface Brain 
{
	public double turn(Protozoa p);
	public double speed(Protozoa p);
	public boolean attack(Protozoa p);
	public boolean mate(Protozoa p);
	public double energyConsumption();
	
	public static final Brain RANDOM = new Brain() {
		
		@Override
		public double turn(Protozoa p) {
			double x = 2*Simulation.RANDOM.nextDouble() - 1;
			double t = Math.toRadians(35);
			return t*x;
		}

		@Override
		public double speed(Protozoa p) {
			return Simulation.RANDOM.nextDouble() / 10.0;
		}

		@Override
		public boolean attack(Protozoa p) {
			return Simulation.RANDOM.nextBoolean();
		}

		@Override
		public boolean mate(Protozoa p) {
			return Simulation.RANDOM.nextBoolean();
		}

		@Override
		public double energyConsumption() {
			return 0;
		}
		
	};
}
