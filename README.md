# Evolving Artificial Life

Welcome to **Evolving Artificial Life**! This project is a simulator of evolution in a 2D virtual environment, where artificial creatures evolve through interactions with plants, eggs, other creatures, and the environment itself. This simulator lets you create new simulations, run them over extended periods, and observe the complex behaviors that emerge as creatures evolve over time.

## Key Features

- **Real-Time Evolution**: Creatures evolve through a topology and weight evolving artificial neural network (TWEANN), allowing for diverse and intricate topologies. This enables complex behavior patterns to emerge without external guidance, driven by a NEAT (NeuroEvolution of Augmenting Topologies) genetic algorithm.
  
- **Continuous Simulation**: Unlike other simulators that end after a short period and retain only a handful of the best organisms, this simulation is designed to run indefinitely with potentially millions of births and deaths, creating a realistic evolutionary environment.
  
- **Sophisticated Creature Design**: Each creature features evolving body attributes like vision, digestion, armor, strength, and size. Creatures may develop tendencies to be herbivorous or carnivorous, allowing for a dynamic predator-prey ecosystem.

- **High Performance Simulation Engine**: Leveraging optimized algorithms for collision detection and spatial partitioning, the engine is built for speed, enabling hundreds of thousands of ticks per second. Multi-threading ensures the simulation can run at an acceptable speed, supporting long, complex evolutionary runs.

## Implementation

### Neural Network (TWEANN) & NEAT
The neural network system took about 1.5 months to develop, starting from scratch. Implementing RT-NEAT (Real-time NEAT) presented its own challenges due to limited resources on NEAT specifics. This work involved extensive parameter tuning to ensure realistic, adaptive creature behavior.

### Simulation Engine
The engine was developed with spatial partitioning and collision detection algorithms tailored to handle a large population of dynamically sized entities. With an emphasis on speed, the simulation can run for billions of ticks, making it possible to observe the emergence of stable species and ecological dynamics dozens of times in a short period.

### Creature Anatomy and Evolution
The creatures feature an evolving physiology, with a functioning digestive system that adjusts for herbivory or carnivory, and various physical attributes that adapt over time. This multi-faceted evolution allows for rich diversity, including creatures with differing diets, strengths, and survival strategies.

### Graphical User Interface (GUI)
The GUI is the most sophisticated interface in this project, providing real-time visualizations of each creature’s vital stats, dynamic graphs, and a neural network visualizer. Key features include:
- Real-time neural network visualization
- Graphs tracking species traits over time
- Detailed creature inspection and adjustable settings for simulation speed
- Toggleable views for creature hitboxes, spatial partitioning grids, and vision cones.

The simulation setup allows you to save and load simulation states, so you can pick up exactly where you left off.

## Future Plans

- **Player Control**: Currently working on enabling player control of specific creatures to directly interact with the evolving ecosystem.
- **Collision System Optimization**: Ongoing work on a more memory-efficient collision detection system to improve performance on resource-constrained systems.

## Acknowledgments

This project was heavily influenced by research and resources that helped guide my understanding and implementation of NEAT, TWEANN, the creature themselves, and more:

- **Inspiration**: Various creators have inspired me to create this project, whether they be the source of inspiration that made me start this project or the instructor that led me to add more elements to the simulation. 
    - [The Bibites: Digital Life (Youtube Channel)](https://www.youtube.com/@TheBibitesDigitalLife).
    - [Simulating an Evolving Microcosmos | The Path to Multicellularity ](https://youtu.be/fEDqdvKO5Y0?si=MBvfZUhXTDg_klVG) - [Dylan Cope](https://www.youtube.com/@dylancope)
    - [Evolving AIs - Predator vs Prey, who will win?](https://youtu.be/qwrp3lB-jkQ?si=Oa_vXSVZQ5LDtpeR) - [Pezzza's Work](https://www.youtube.com/@PezzzasWork)
- **Resources for NEAT** - applications, visual examples, and implementations:
    - Stanley, Kenneth O., and Risto Miikkulainen. "Evolving Neural Networks through Augmenting Topologies." *MIT Press* (2002). [Read the paper here](https://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf).
    - [AI NEAT (PlayList) - Finn Eggers ](https://www.youtube.com/watch?v=VMQOa4-rVxE&list=PLgomWLYGNl1fcL0o4exBShNeCC5tc6s9C&ab_channel=FinnEggers)
    - [NEAT Algorithm Visually Explained - David Schäfer](https://youtu.be/yVtdp1kF0I4?si=SfH0ouURtsrmutv2)

  

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


Thank you for exploring **Evolving Artificial Life**. This project is continually evolving, so check back often for updates as new features and optimizations are added.
