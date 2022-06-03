package biology;

import java.io.Serializable;

import core.Settings;
import core.Simulation;

public interface Brain extends Serializable
{
	void tick(Protozoa p);
	double turn(Protozoa p);
	double speed(Protozoa p);
	boolean wantToAttack(Protozoa p);
	boolean wantToMateWith(Protozoa p);
	double energyConsumption();
	
	Brain RANDOM = new Brain()
	{
		private static final long serialVersionUID = 1648484737904226314L;

		@Override
		public void tick(Protozoa p) {}

		@Override
		public double turn(Protozoa p)
		{
			double x = 2*Simulation.RANDOM.nextDouble() - 1;
			double t = Math.toRadians(35);
			return t * x;
		}

		@Override
		public double speed(Protozoa p) {
			return Simulation.RANDOM.nextDouble() * Settings.maxVel;
		}

		@Override
		public boolean wantToAttack(Protozoa p)
		{
			return Simulation.RANDOM.nextBoolean();
		}

		@Override
		public boolean wantToMateWith(Protozoa p) {
			return false;
		}

		@Override
		public double energyConsumption() {
			return 0;
		}
		
	};
}
