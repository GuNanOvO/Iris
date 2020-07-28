package com.volmit.iris.object;

import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.World.Environment;
import org.bukkit.block.data.BlockData;

import com.volmit.iris.util.BlockDataTools;
import com.volmit.iris.util.CNG;
import com.volmit.iris.util.Desc;
import com.volmit.iris.util.DontObfuscate;
import com.volmit.iris.util.KList;
import com.volmit.iris.util.RNG;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Desc("Represents a dimension")
@Data
@EqualsAndHashCode(callSuper = false)
public class IrisDimension extends IrisRegistrant
{
	@DontObfuscate
	@Desc("The human readable name of this dimension")
	private String name = "A Dimension";

	@DontObfuscate
	@Desc("The interpolation function for splicing noise maxes together")
	private InterpolationMethod interpolationFunction = InterpolationMethod.BICUBIC;

	@DontObfuscate
	@Desc("The interpolation distance scale. Increase = more smooth, less detail")
	private double interpolationScale = 63;

	@DontObfuscate
	@Desc("The Thickness scale of cave veins")
	private double caveThickness = 1D;

	@DontObfuscate
	@Desc("The cave web scale. Smaller values means scaled up vein networks.")
	private double caveScale = 1D;

	@DontObfuscate
	@Desc("Shift the Y value of the cave networks up or down.")
	private double caveShift = 0D;

	@DontObfuscate
	@Desc("Generate caves or not.")
	private boolean caves = true;

	@DontObfuscate
	@Desc("Carve terrain or not")
	private boolean carving = true;

	@DontObfuscate
	@Desc("Generate decorations or not")
	private boolean decorate = true;

	@DontObfuscate
	@Desc("Use post processing features. Usually for production only as there is a gen speed cost.")
	private boolean postProcess = true;

	@DontObfuscate
	@Desc("The ceiling dimension. Leave blank for normal sky.")
	private String ceiling = "";

	@DontObfuscate
	@Desc("Mirrors the generator floor into the ceiling. Think nether but worse...")
	private boolean mirrorCeiling = false;

	@DontObfuscate
	@Desc("The world environment")
	private Environment environment = Environment.NORMAL;

	@DontObfuscate
	@Desc("Define all of the regions to include in this dimension. Dimensions -> Regions -> Biomes -> Objects etc")
	private KList<String> regions = new KList<>();

	@DontObfuscate
	@Desc("The fluid height for this dimension")
	private int fluidHeight = 63;

	@DontObfuscate
	@Desc("Keep this either undefined or empty. Setting any biome name into this will force iris to only generate the specified biome. Great for testing.")
	private String focus = "";

	@DontObfuscate
	@Desc("Zoom in or out the biome size. Higher = bigger biomes")
	private double biomeZoom = 5D;

	@DontObfuscate
	@Desc("Zoom in or out the terrain. This stretches the terrain. Due to performance improvements, Higher than 2.0 may cause weird rounding artifacts. Lower = more terrain changes per block. Its a true zoom-out.")
	private double terrainZoom = 2D;

	@DontObfuscate
	@Desc("You can rotate the input coordinates by an angle. This can make terrain appear more natural (less sharp corners and lines). This literally rotates the entire dimension by an angle. Hint: Try 12 degrees or something not on a 90 or 45 degree angle.")
	private double dimensionAngleDeg = 0;

	@DontObfuscate
	@Desc("Iris adds a few roughness filters to noise. Increasing this smooths it out. Decreasing this makes it bumpier/scratchy")
	private double roughnessZoom = 2D;

	@DontObfuscate
	@Desc("The height of the roughness filters")
	private int roughnessHeight = 3;

	@DontObfuscate
	@Desc("Coordinate fracturing applies noise to the input coordinates. This creates the 'iris swirls' and wavy features. The distance pushes these waves further into places they shouldnt be. This is a block value multiplier.")
	private double coordFractureDistance = 20;

	@DontObfuscate
	@Desc("Coordinate fracturing zoom. Higher = less frequent warping, Lower = more frequent and rapid warping / swirls.")
	private double coordFractureZoom = 8;

	@DontObfuscate
	@Desc("This zooms in the land space")
	private double landZoom = 1;

	@DontObfuscate
	@Desc("This zooms in the cave biome space")
	private double caveBiomeZoom = 1;

	@DontObfuscate
	@Desc("This can zoom the shores")
	private double shoreZoom = 1;

	@DontObfuscate
	@Desc("This zooms oceanic biomes")
	private double seaZoom = 1;

	@DontObfuscate
	@Desc("Zoom in continents")
	private double continentZoom = 1;

	@DontObfuscate
	@Desc("Change the size of regions")
	private double regionZoom = 1;

	@DontObfuscate
	@Desc("Disable this to stop placing schematics in biomes")
	private boolean placeObjects = true;

	@DontObfuscate
	@Desc("Prevent Leaf decay as if placed in creative mode")
	private boolean preventLeafDecay = false;

	@DontObfuscate
	@Desc("Define global deposit generators")
	private KList<IrisDepositGenerator> deposits = new KList<>();

	@DontObfuscate
	@Desc("The dispersion of materials for the rock palette")
	private Dispersion dispersion = Dispersion.SCATTER;

	@DontObfuscate
	@Desc("The rock zoom mostly for zooming in on a wispy palette")
	private double rockZoom = 5;

	@DontObfuscate
	@Desc("The palette of blocks for 'stone'")
	private KList<String> rockPalette = new KList<String>().qadd("STONE");

	@DontObfuscate
	@Desc("The palette of blocks for 'water'")
	private KList<String> fluidPalette = new KList<String>().qadd("WATER");

	private transient ReentrantLock rockLock = new ReentrantLock();
	private transient ReentrantLock fluidLock = new ReentrantLock();
	private transient KList<BlockData> rockData;
	private transient KList<BlockData> fluidData;
	private transient CNG rockLayerGenerator;
	private transient CNG fluidLayerGenerator;
	private transient CNG coordFracture;
	private transient Double sinr;
	private transient Double cosr;
	private transient Double rad;
	private transient boolean inverted;

	public CNG getCoordFracture(RNG rng, int signature)
	{
		if(coordFracture == null)
		{
			coordFracture = CNG.signature(rng.nextParallelRNG(signature));
			coordFracture.scale(0.012 / coordFractureZoom);
		}

		return coordFracture;
	}

	public BlockData getRock(RNG rng, double x, double y, double z)
	{
		if(getRockData().size() == 1)
		{
			return getRockData().get(0);
		}

		if(rockLayerGenerator == null)
		{
			cacheRockGenerator(rng);
		}

		if(rockLayerGenerator != null)
		{
			if(dispersion.equals(Dispersion.SCATTER))
			{
				return getRockData().get(rockLayerGenerator.fit(0, 30000000, x, y, z) % getRockData().size());
			}

			else
			{
				return getRockData().get(rockLayerGenerator.fit(0, getRockData().size() - 1, x, y, z));
			}
		}

		return getRockData().get(0);
	}

	public void cacheRockGenerator(RNG rng)
	{
		RNG rngx = rng.nextParallelRNG(getRockData().size() * hashCode());

		switch(dispersion)
		{
			case SCATTER:
				rockLayerGenerator = CNG.signature(rngx).freq(1000000);
				break;
			case WISPY:
				rockLayerGenerator = CNG.signature(rngx);
				break;
		}
	}

	public KList<BlockData> getRockData()
	{
		rockLock.lock();

		if(rockData == null)
		{
			rockData = new KList<>();
			for(String ix : rockPalette)
			{
				BlockData bx = BlockDataTools.getBlockData(ix);
				if(bx != null)
				{
					rockData.add(bx);
				}
			}
		}

		rockLock.unlock();

		return rockData;
	}

	public BlockData getFluid(RNG rng, double x, double y, double z)
	{
		if(getFluidData().size() == 1)
		{
			return getFluidData().get(0);
		}

		if(fluidLayerGenerator == null)
		{
			cacheFluidGenerator(rng);
		}

		if(fluidLayerGenerator != null)
		{
			if(dispersion.equals(Dispersion.SCATTER))
			{
				return getFluidData().get(fluidLayerGenerator.fit(0, 30000000, x, y, z) % getFluidData().size());
			}

			else
			{
				return getFluidData().get(fluidLayerGenerator.fit(0, getFluidData().size() - 1, x, y, z));
			}
		}

		return getFluidData().get(0);
	}

	public void cacheFluidGenerator(RNG rng)
	{
		RNG rngx = rng.nextParallelRNG(getFluidData().size() * hashCode());

		switch(dispersion)
		{
			case SCATTER:
				fluidLayerGenerator = CNG.signature(rngx).freq(1000000);
				break;
			case WISPY:
				fluidLayerGenerator = CNG.signature(rngx);
				break;
		}
	}

	public KList<BlockData> getFluidData()
	{
		fluidLock.lock();

		if(fluidData == null)
		{
			fluidData = new KList<>();
			for(String ix : fluidPalette)
			{
				BlockData bx = BlockDataTools.getBlockData(ix);
				if(bx != null)
				{
					fluidData.add(bx);
				}
			}
		}

		fluidLock.unlock();

		return fluidData;
	}

	public double getDimensionAngle()
	{
		if(rad == null)
		{
			rad = Math.toRadians(dimensionAngleDeg);
		}

		return rad;
	}

	public double sinRotate()
	{
		if(sinr == null)
		{
			sinr = Math.sin(getDimensionAngle());
		}

		return sinr;
	}

	public double cosRotate()
	{
		if(cosr == null)
		{
			cosr = Math.cos(getDimensionAngle());
		}

		return cosr;
	}
}