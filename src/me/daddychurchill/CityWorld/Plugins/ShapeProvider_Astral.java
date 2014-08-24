package me.daddychurchill.CityWorld.Plugins;

import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Context.DataContext;
import me.daddychurchill.CityWorld.Context.Astral.AstralBaseContext;
import me.daddychurchill.CityWorld.Context.Astral.AstralMushroomContext;
import me.daddychurchill.CityWorld.Context.Astral.AstralNatureContext;
import me.daddychurchill.CityWorld.Context.Astral.AstralRoadContext;
import me.daddychurchill.CityWorld.Plats.PlatLot;
import me.daddychurchill.CityWorld.Support.ByteChunk;
import me.daddychurchill.CityWorld.Support.CachedYs;
import me.daddychurchill.CityWorld.Support.Odds;
import me.daddychurchill.CityWorld.Support.PlatMap;
import me.daddychurchill.CityWorld.Support.RealChunk;
import me.daddychurchill.CityWorld.Support.SegmentedCachedYs;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class ShapeProvider_Astral extends ShapeProvider {

	public SimplexOctaveGenerator landShape1;
	public SimplexOctaveGenerator landShape2;
	public SimplexOctaveGenerator seaShape;
	public SimplexOctaveGenerator noiseShape;
	public SimplexOctaveGenerator featureShape;
//	public SimplexNoiseGenerator caveShape;
	public SimplexNoiseGenerator ecoShape;
	
	protected int height;
	protected int seaLevel;
	protected int landRange;
	protected int seaRange;
	protected int constructMin;
	protected int constructRange;
	
	public final static int landFlattening = 32;
	public final static int seaFlattening = 4;
	public final static int landFactor1to2 = 3;
	public final static int noiseVerticalScale = 3;
	public final static int featureVerticalScale = 10;
	public final static int fudgeVerticalScale = noiseVerticalScale * landFactor1to2 + featureVerticalScale * landFactor1to2;

	public final static double landFrequency1 = 1.50;
	public final static double landAmplitude1 = 20.0;
	public final static double landHorizontalScale1 = 1.0 / 1024;//2048.0;
	public final static double landFrequency2 = 1.0;
	public final static double landAmplitude2 = landAmplitude1 / landFactor1to2;
	public final static double landHorizontalScale2 = landHorizontalScale1 * landFactor1to2;

	public final static double seaFrequency = 1.00;
	public final static double seaAmplitude = 2.00;
	public final static double seaHorizontalScale = 1.0 / 256;//384.0;

	public final static double noiseFrequency = 1.50;
	public final static double noiseAmplitude = 0.70;
	public final static double noiseHorizontalScale = 1.0 / 32.0;
	
	public final static double featureFrequency = 1.50;
	public final static double featureAmplitude = 0.75;
	public final static double featureHorizontalScale = 1.0 / 64.0;
	
//	public final static double caveScale = 1.0 / 64.0;
//	public final static double caveScaleY = caveScale * 2;
//	public final static double caveThreshold = 0.75; // smaller the number the more larger the caves will be
//	
	public final static double ecoScale = 1.0 / 4.0;
	public final static double ecoScaleY = ecoScale;

	public ShapeProvider_Astral(WorldGenerator generator, Odds odds) {
		super(generator, odds);
		
		World world = generator.getWorld();
		long seed = generator.getWorldSeed();
		
		landShape1 = new SimplexOctaveGenerator(seed, 4);
		landShape1.setScale(landHorizontalScale1);
		landShape2 = new SimplexOctaveGenerator(seed, 6);
		landShape2.setScale(landHorizontalScale2);
		seaShape = new SimplexOctaveGenerator(seed + 2, 8);
		seaShape.setScale(seaHorizontalScale);
		noiseShape = new SimplexOctaveGenerator(seed + 3, 16);
		noiseShape.setScale(noiseHorizontalScale);
		featureShape = new SimplexOctaveGenerator(seed + 4, 2);
		featureShape.setScale(featureHorizontalScale);
		
//		caveShape = new SimplexNoiseGenerator(seed);
		ecoShape = new SimplexNoiseGenerator(seed + 5);
		
		// get ranges
		height = world.getMaxHeight();
		seaLevel = world.getSeaLevel();
		landRange = height - seaLevel - fudgeVerticalScale + landFlattening;
		seaRange = seaLevel - fudgeVerticalScale + seaFlattening;
		constructMin = seaLevel;
		constructRange = height - constructMin;
	}
	
	@Override
	public CachedYs getCachedYs(WorldGenerator generator, int chunkX, int chunkZ) {
		return new SegmentedCachedYs(generator, chunkX, chunkZ);
	}
	
	@Override
	protected void validateLots(WorldGenerator generator, PlatMap platmap) {
		// nothing to do in this one
	}
	
	private AstralBaseContext baseContext;
	private AstralMushroomContext mushroomContext;
	
	@Override
	protected void allocateContexts(WorldGenerator generator) {
		if (!contextInitialized) {
			natureContext = new AstralNatureContext(generator);
			roadContext = new AstralRoadContext(generator);
			
			baseContext = new AstralBaseContext(generator); // bunkers on pedestals
			mushroomContext = new AstralMushroomContext(generator); // standard mushrooms and a couple gigantic ones
			// crystalSpiresContext = new AstralCrystalContext(generator); // crystal pokie bits

			// obsidianMineContext = new AstralObsidianContext(generator); // obsidian maze mines
			// citadelContext = new AstralCitadelContext(generator); // dark tower of darkness
			// landingZoneContext = new AstralLandingContext(generator); // spaceship landing zone
			// nexusContext = new AstralNexusContext(generator); // the 0,0 zone
			// wallContext = new AstralWallContext(generator); // the walls going north/south/east/west from the nexus zone
			// punctureContext = new AstralPunctureContext(generator); // hole in the world
			
			contextInitialized = true;
		}
	}
	
	@Override
	protected DataContext getContext(PlatMap platmap) {
		
		// let's keep this one simple
		return mushroomContext;
	}

	@Override
	public String getCollectionName() {
		return "Astral";
	}
	
	@Override
	protected Biome remapBiome(WorldGenerator generator, PlatLot lot, Biome biome) {
		return generator.oreProvider.remapBiome(biome);
	}

	@Override
	public void preGenerateChunk(WorldGenerator generator, PlatLot lot, ByteChunk chunk, BiomeGrid biomes, CachedYs blockYs) {
		Biome biome = lot.getChunkBiome();
		OreProvider ores = generator.oreProvider;
//		boolean surfaceCaves = isSurfaceCaveAt(chunk.chunkX, chunk.chunkZ);
		boolean flattened = blockYs.segmentWidth > 1;
		
		// shape the world
		for (int x = 0; x < chunk.width; x++) {
			for (int z = 0; z < chunk.width; z++) {
				int y = blockYs.getBlockY(x, z);
				
				// roughness
				double noise = noiseShape.noise(chunk.getBlockX(x), chunk.getBlockZ(z), 0, noiseFrequency, noiseAmplitude, true);
				int noiseY = NoiseGenerator.floor(noise * 5) + 5;
				
				// bottom please
				chunk.setBlock(x, 0, z, ores.substratumMaterial);
				
				// Chasm?
				if (y == 0) {
					biome = Biome.OCEAN;
					chunk.setBlock(x, 1, z, ores.fluidMaterial);
					chunk.setBlocks(x, 2, noiseY + 2, z, Material.WEB);
				
				// On the edge?
				} else if (y == seaLevel && blockYs.getSegment(x, z) == 0) {
					chunk.setBlocks(x, 1, y - 1, z, ores.stratumMaterial);
					chunk.setBlocks(x, y - 1, y, z, ores.subsurfaceMaterial);
				
				// Valley? Mountain?
				} else {

					// dented?
					int baseY = Math.min(seaLevel + noiseY, y);
					if (flattened)
						baseY = Math.min(seaLevel, Math.max(16, baseY - blockYs.segmentWidth * 2));
					
					// initial stuff, we will do the rest later
					chunk.setBlocks(x, 1, baseY - 2, z, ores.stratumMaterial);
					chunk.setBlocks(x, baseY - 2, baseY, z, ores.surfaceMaterial);
					
					// we will do the rest later
				}
				
				// set biome for block
				biomes.setBiome(x, z, remapBiome(generator, lot, biome));
			}
		}	
	}
	
	@Override
	public void postGenerateChunk(WorldGenerator generator, PlatLot lot, ByteChunk chunk, CachedYs blockYs) {
		
//		// mines please
//		lot.generateMines(generator, chunk);
	}

	@Override
	public void preGenerateBlocks(WorldGenerator generator, PlatLot lot, RealChunk chunk, CachedYs blockYs) {
		OreProvider ores = generator.oreProvider;
//		boolean surfaceCaves = isSurfaceCaveAt(chunk.chunkX, chunk.chunkZ);
		int originX = chunk.getOriginX();
		int originZ = chunk.getOriginZ();
		boolean flattened = blockYs.segmentWidth > 1;
		
		// shape the world
		for (int x = 0; x < chunk.width; x++) {
			for (int z = 0; z < chunk.width; z++) {
				int blockX = chunk.getBlockX(x);
				int blockZ = chunk.getBlockZ(z);
				int y = blockYs.getBlockY(x, z);
				
				// Chasm?
				if (y == 0) {
					
					// nothing... yet
				
				// On the edge?
				} else if (y == seaLevel && blockYs.getSegment(x, z) == 0) {
					
					// nothing... yet
				
				// Valley? Mountain?
				} else {
					
					// roughness
					double noise = noiseShape.noise(blockX, blockZ, 0, noiseFrequency, noiseAmplitude, true);
					int noiseY = NoiseGenerator.floor(noise * 5) + 5;
					
					// dented?
					int baseY = Math.min(seaLevel + noiseY, y);
					if (flattened)
						baseY = Math.min(seaLevel, Math.max(16, baseY - blockYs.segmentWidth * 2));
					
					// backfill valley
					if (y < seaLevel) {
						
						// little more snow
						chunk.setBlocks(x, baseY, y, z, ores.surfaceMaterial);
						double perciseY = blockYs.getPerciseY(x, z);
						chunk.setSnowCover(x, y, z, (byte) NoiseGenerator.floor((perciseY - Math.floor(perciseY)) * 8.0));
						
					// backfill mountain
					} else if (y > seaLevel) {
				
						// now the pretty colors
						if (y > baseY) {
							int segmentX = x / blockYs.segmentWidth * blockYs.segmentWidth + originX;
							int segmentZ = z / blockYs.segmentWidth * blockYs.segmentWidth + originZ;
							double colorD = noiseShape.noise(segmentX, segmentZ, blockYs.getSegment(x, z), noiseFrequency, noiseAmplitude, true);
							chunk.setGlass(x, x + 1, baseY, y, z, z + 1, DyeColor.values()[Math.min(15, Math.max(0, NoiseGenerator.floor(colorD * 8) + 8))]);
						
						// sprinkle a little bit more snow?
						} else {
							
							// little more snow
							chunk.setBlocks(x, baseY, y, z, ores.surfaceMaterial);
							double perciseY = blockYs.getPerciseY(x, z);
							chunk.setSnowCover(x, y, z, (byte) NoiseGenerator.floor((perciseY - Math.floor(perciseY)) * 8.0));
						}
					}
				}
			}
		}	
	}

	@Override
	public void postGenerateBlocks(WorldGenerator generator, PlatLot lot, RealChunk chunk, CachedYs blockYs) {
		
//		// put ores in?
//		lot.generateOres(generator, chunk);
//
//		// do we do it or not?
//		lot.generateMines(generator, chunk);
	}

	@Override
	public int getWorldHeight() {
		return height;
	}

	@Override
	public int getStreetLevel() {
		return seaLevel + 1;
	}

	@Override
	public int getSeaLevel() {
		return seaLevel;
	}

	@Override
	public int getLandRange() {
		return landRange;
	}

	@Override
	public int getSeaRange() {
		return seaRange;
	}

	@Override
	public int getConstuctMin() {
		return constructMin;
	}

	@Override
	public int getConstuctRange() {
		return constructRange;
	}
	
	@Override
	public double findPerciseY(WorldGenerator generator, int blockX, int blockZ) {
		double y = 0;
		
		// shape the noise
		double noise = noiseShape.noise(blockX, blockZ, noiseFrequency, noiseAmplitude, true);
		double feature = featureShape.noise(blockX, blockZ, featureFrequency, featureAmplitude, true);

		double land1 = seaLevel + (landShape1.noise(blockX, blockZ, landFrequency1, landAmplitude1, true) * landRange) + 
				(noise * noiseVerticalScale * landFactor1to2 + feature * featureVerticalScale * landFactor1to2) - landFlattening;
		double land2 = seaLevel + (landShape2.noise(blockX, blockZ, landFrequency2, landAmplitude2, true) * (landRange / (double) landFactor1to2)) + 
				(noise * noiseVerticalScale + feature * featureVerticalScale) - landFlattening;
		double landY = Math.max(land1, land2);
		
		double sea = seaShape.noise(blockX, blockZ, seaFrequency, seaAmplitude, true);
		double seaY = seaLevel + (sea * seaRange) + (noise * noiseVerticalScale) + seaFlattening;

		// Mountain?
		if (landY > seaLevel) {
			y = landY;	
		} else {
			
			// Chasm?
			if (seaY < seaLevel) {
				y = 0;
				
			// Cliff?
			} else if (seaY == seaLevel) {
				y = seaLevel;
			
			// Something in between?
			} else {
				
				// invert the sea
//				seaY = Math.max(0, seaLevel - Math.max(seaLevel, (sea * seaRange * 3) + (noise * noiseVerticalScale * 2)));
//				seaY = Math.max(0, seaLevel - (Math.abs(sea) * seaRange * 3) + (noise * noiseVerticalScale * 2));
				seaY = Math.min(seaLevel, 3 + Math.max(0, seaLevel - ((seaY - seaLevel) * 3)));
				
				// who is on top?
				y = Math.max(landY, seaY);
			}
		}
		
		// range validation
		return Math.min(height - 3, Math.max(y, 0));
	}
	
	@Override
	public boolean isHorizontalNSShaft(int chunkX, int chunkY, int chunkZ) {
		return false;
//		return mineShape.noise(chunkX * mineScale, chunkY * mineScale, chunkZ * mineScale + 0.5) > 0.0;
	}

	@Override
	public boolean isHorizontalWEShaft(int chunkX, int chunkY, int chunkZ) {
		return false;
//		return mineShape.noise(chunkX * mineScale + 0.5, chunkY * mineScale, chunkZ * mineScale) > 0.0;
	}

	@Override
	public boolean isVerticalShaft(int chunkX, int chunkY, int chunkZ) {
		return false;
//		return mineShape.noise(chunkX * mineScale, chunkY * mineScale + 0.5, chunkZ * mineScale) > 0.0;
	}

	@Override
	public boolean notACave(WorldGenerator generator, int blockX, int blockY, int blockZ) {
		return true;
//		if (generator.settings.includeCaves) {
//			double cave = caveShape.noise(blockX * caveScale, blockY * caveScaleY, blockZ * caveScale);
//			return !(cave > caveThreshold || cave < -caveThreshold);
//		} else
//			return true;
	}
		
}