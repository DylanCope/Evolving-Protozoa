# Evolving-Protozoa

## Aim

Create an environment where protozoa-like entities can evolve their controllers.

![png](/screenshots/tank.png)

## Notes and TODOs

* Thinking time should be proportional to the complexity of the brain. ✔

* Neurons should change their state according to a Hebbian update rule, where the 
  learning rate is either zero or it is supplied by another neuron.

* Genes should be able to regulate senescence. Deterioration rate should be a 
  bounded evolutionary variable; i.e. a species can increase deterioration to encourage 
  faster evolutionary change, but individual organisms will have less time to adapt and
  will be less complex (which may impact survival).

* Energy consumption should determine how quickly an protozoa dies. 
	- Larger brains increase energy consumption.
	- Larger bodies increase energy consumption.
	- Moving faster increases energy consumption.
	- More retina cells increases energy consumption.

* Eating shouldn't be a instantaneous process, a protozoa should absorb health from it's food
  at some determined rate. ✔

* A fighting mechanism between protozoa should be implemented ✔
* When two protozoa fight, how much of each other they absorb should be a result of the differential
  between each of their respective fighting abilities.
* A protozoa's fighting ability should be a result of it's size and health (plus a random element).

* Command line interface: ✔
	- startsim ?pelletNumber ?protozoaNumber ?parentGenomes
		  starts new simulation
		  pelletNumber is the number of pellets in each generation (default = 100)
		  protozoaNumber is the number of protozoa in each generation (default = 60)
		  parentGenomes is a file containing a set of parent genomes for generation 0 (if left blank random genomes are used)
