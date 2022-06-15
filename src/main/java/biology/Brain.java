package biology;

import java.io.Serializable;

import core.Settings;
import core.Simulation;

public interface Brain extends Serializable
{
	void tick(Protozoa p);
	float turn(Protozoa p);
	float speed(Protozoa p);
	boolean wantToMateWith(Protozoa p);
	float energyConsumption();
	
	Brain RANDOM = new Brain()
	{
		private static final long serialVersionUID = 1648484737904226314L;

		@Override
		public void tick(Protozoa p) {}

		@Override
		public float turn(Protozoa p)
		{
			float x = (float) (2*Simulation.RANDOM.nextDouble() - 1);
			float t = (float) Math.toRadians(35);
			return t * x;
		}

		@Override
		public float speed(Protozoa p) {
			return (float) (Simulation.RANDOM.nextDouble() * Settings.maxProtozoaSpeed);
		}

		@Override
		public boolean wantToMateWith(Protozoa p) {
			return false;
		}

		@Override
		public float energyConsumption() {
			return 0;
		}
		
	};
}
