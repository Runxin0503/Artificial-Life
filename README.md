# 🧬 Artificial Life

Welcome to **Artificial Life** — a 2D virtual evolution simulator where artificial creatures evolve by interacting with plants, eggs, other creatures, and their environment. You can run simulations over long periods, watch species emerge and adapt, and explore the dynamics of artificial ecosystems.

---

## 🌟 Key Features

- **🧠 Real-Time Evolution**  
  Creatures evolve using TWEANN (Topology and Weight Evolving Artificial Neural Networks), powered by a NEAT (NeuroEvolution of Augmenting Topologies) genetic algorithm. This allows complex behaviors to emerge organically without external programming.

- **♻️ Continuous Simulation**  
  Unlike traditional simulations with fixed endpoints, this one runs indefinitely. Expect millions of births and deaths, leading to emergent species, survival strategies, and even extinction events.

- **🦎 Evolving Creatures**  
  Each creature evolves both mentally and physically: attributes like digestion, vision, strength, armor, and size all change. Creatures may evolve toward herbivory, carnivory, or hybrid diets.

- **⚡ High-Performance Engine**  
  Optimized collision detection and spatial partitioning enable extremely fast simulations (hundreds of thousands of ticks/second), with multi-threading for long evolutionary runs.

---

## 🖼️ Screenshots

<details>
  <summary>🌍 World snapshot (12 ticks/sec)</summary>
  <img src="/image3.png" alt="World Snapshot" width="2367">
</details>

<details>
  <summary>🔍 Creature Info Tab (WIP)</summary>
  <img src="/image4.png" alt="Creature Info" width="3068">
</details>

<details>
  <summary>👁️ Debug mode (vision cone + grid)</summary>
  <img src="/image5.png" alt="Debug Mode" width="3071">
</details>

<details>
  <summary>🧬 Brain viewer + forced mutation</summary>
  <img src="/image6.png" alt="Brain Viewer" width="3071">
</details>

<details>
  <summary>⚙️ Simulation settings menu</summary>
  <img src="/image1.png" alt="Settings Panel" width="2368">
</details>

<details>
  <summary>📈 Graph of evolving traits</summary>
  <img src="/image7.png" alt="Statistics Graph" width="3071">
</details>

---

## 🧩 Implementation Details

### 🧠 Neural Network (TWEANN) & NEAT

The neural architecture was implemented from scratch over ~1.5 months. RT-NEAT (Real-Time NEAT) was particularly challenging due to sparse documentation. Extensive parameter tuning ensures adaptive, realistic behavior in creatures.

### ⚙️ Simulation Engine

Built for performance, the engine handles thousands of dynamically-sized entities using optimized spatial partitioning and collision detection. Designed to support billion-tick evolutionary cycles.

### 🧬 Creature Evolution

Creatures evolve both structurally and functionally: vision, diet, strength, and size all co-evolve with their neural networks. This allows for rich emergent diversity — predators, grazers, tanks, and everything in between.

### 🖥️ GUI & Tools

The GUI is feature-rich and built for deep exploration:

- Real-time neural network visualization
- Graphs showing population stats & trait trends
- Creature inspector and dynamic mutation tools
- Toggle views for vision cones, hitboxes, and partitioning grids
- Save/load simulation states for long-term evolution

---

## 🛠 Installation & Running the Simulation

This project uses standard Java and requires no external dependencies.

### ✅ Prerequisites

- [Java JDK 17+](https://adoptium.net/) installed
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (recommended) or another Java-compatible IDE

### 🚀 How to Run

1. **Clone this repository**

   ```bash
   git clone https://github.com/your-username/artificial-life.git
   cd artificial-life

2. **Open in IntelliJ IDEA**

  * Go to **File > Open**, and select the project folder.

  * Wait for IntelliJ to index the files.

3. **Run the simulation**

  * Open the main class (e.g., `Main.java`)

  * Right-click and choose **Run**.


> 📝 No build tools like Maven or Gradle are needed for pre-release versions. Just open, build, and run.

---

🧭 Roadmap
----------

* [x] Add support for **player-controlled creatures**

* [ ] Refactor simulation into a proper **MVC architecture**

* [ ] Improve **world saving/loading**

* [ ] Eliminate all **multi-threading bugs**

* [ ] Integrate **reinforcement learning protocols**

* [ ] Improve **UI appearance and interactivity**

* [ ] Optimize **collision detection & memory usage**

* [ ] Add **biomes, temperature, terrain height, etc**. (Low Priority)

---

🙏 Acknowledgments
------------------

This project was inspired by many researchers and creators exploring artificial life, NEAT, and digital evolution:

### 🎥 YouTube Inspirations

* [The Bibites: Digital Life](https://www.youtube.com/@TheBibitesDigitalLife)

* [Simulating an Evolving Microcosmos – Dylan Cope](https://youtu.be/fEDqdvKO5Y0?si=MBvfZUhXTDg_klVG)

* [Evolving AIs – Pezzza’s Work](https://youtu.be/qwrp3lB-jkQ?si=Oa_vXSVZQ5LDtpeR)

* [Evolution Simulated Series – The Sapling](https://youtu.be/x7Kc40SryVc)


### 📚 NEAT Resources

* Stanley, Kenneth O., & Miikkulainen, R. (2002).  
  _Evolving Neural Networks through Augmenting Topologies_.  
  [Read the paper](https://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf)

* [AI NEAT (YouTube Playlist) – Finn Eggers](https://www.youtube.com/playlist?list=PLgomWLYGNl1fcL0o4exBShNeCC5tc6s9C)

* [NEAT Algorithm Visually Explained – David Schäfer](https://youtu.be/yVtdp1kF0I4)


## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


Thank you for exploring **Artificial Life**. This project is continually evolving, so check back often for updates as new features and optimizations are added.
