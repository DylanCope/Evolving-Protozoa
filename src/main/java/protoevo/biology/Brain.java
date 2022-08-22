package protoevo.biology;

import java.io.Serializable;

import protoevo.biology.genes.ProtozoaGenome;
import protoevo.core.Settings;
import protoevo.core.Simulation;

public interface Brain extends Serializable
{
	void tick(Protozoan p);
	float turn(Protozoan p);
	float speed(Protozoan p);
	float attack(Protozoan p);
	boolean wantToMateWith(Protozoan p);
	float energyConsumption();
	
	Brain RANDOM = new Brain()
	{
		private static final long serialVersionUID = 1648484737904226314L;

		@Override
		public void tick(Protozoan p) {}

		@Override
		public float turn(Protozoan p)
		{
			float x = (float) (2* Simulation.RANDOM.nextDouble() - 1);
			float t = (float) Math.toRadians(35);
			return t * x;
		}

		@Override
		public float speed(Protozoan p) {
			return (float) (Simulation.RANDOM.nextDouble() * Settings.maxProtozoaSpeed);
		}

		@Override
		public float attack(Protozoan p) {
			return Simulation.RANDOM.nextFloat();
		}

		@Override
		public boolean wantToMateWith(Protozoan p) {
			return false;
		}

		@Override
		public float energyConsumption() {
			return 0;
		}
		
	};

	Brain EMPTY = new Brain() {
		@Override
		public void tick(Protozoan p) {}

		@Override
		public float turn(Protozoan p) {
			return 0;
		}

		@Override
		public float speed(Protozoan p) {
			return 0;
		}

		@Override
		public float attack(Protozoan p) {
			return 0;
		}

		@Override
		public boolean wantToMateWith(Protozoan p) {
			return false;
		}

		@Override
		public float energyConsumption() {
			return 0;
		}
	};
}
