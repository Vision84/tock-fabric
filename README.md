# Tock: The Tick-Aware Orchestrator of Chunk Kinetics

A server-side-only Fabric mod that brings adaptive, low-overhead tick scheduling, dynamic chunk prioritization, and CPU-budget-aware execution to the Minecraft server tick loop.

## Features

### 🧠 NeuroTick – Tick Budget Governor
- Prevents lag spikes by distributing heavy workloads across multiple ticks
- Maintains a cost profile per tick task type
- Implements a low-cost PID-style controller for adapting to server load

### 🔳 ChunkFuse – Adaptive Chunk Activity Graph
- Only ticks what players actually see, use, or influence
- Builds a lightweight visibility + usage graph of loaded chunks
- Marks chunks as "cold" when inactive

### 🐌 SnailSpawn – Controlled Entity Spawning
- Fine-tuned, lag-free mob spawning
- Per-entity-type spawn queues with load-aware delay injection
- Area-specific spawn pacing

### 🧱 BlockCache – High-Speed Read-Only Block Data
- Safe API for plugins to inspect chunks without loading them
- Creates an async, tick-updated cache of blockstates
- No chunk loads, no memory explosions

## Requirements

- Minecraft 1.21.5
- Fabric Loader 0.15.10+
- Fabric API 0.95.0+
- Java 21+

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download the latest release from the [releases page](https://github.com/tockmod/tock/releases)
3. Place the jar file in your server's `mods` folder
4. Start your server

## Configuration

Tock can be configured through the `config/tock.json5` file. The following options are available:

```json5
{
  "neurotick": {
    "enabled": true,
    "maxTickTime": 40
  },
  "chunkfuse": {
    "enabled": true,
    "chunkColdTimeout": 30
  },
  "snailspawn": {
    "enabled": true,
    "maxSpawnsPerTick": 10
  },
  "scheduler": {
    "enabled": true,
    "enablePreemptiveCancellation": true
  }
}
```

## Commands

- `/tock debug` - Shows current tick statistics
- `/tock profile` - Shows performance profiling data
- `/tock heatmap` - Shows chunk activity heatmap

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Fabric Team for the amazing modding platform
- The Minecraft community for inspiration and feedback 