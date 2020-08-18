package com.volmit.iris.object;

import com.volmit.iris.gen.atomics.AtomicCache;
import com.volmit.iris.noise.CNG;
import com.volmit.iris.util.Desc;
import com.volmit.iris.util.DontObfuscate;
import com.volmit.iris.util.MaxNumber;
import com.volmit.iris.util.MinNumber;
import com.volmit.iris.util.RNG;
import com.volmit.iris.util.Required;

import lombok.Data;

@Desc("A gen style")
@Data
public class IrisGeneratorStyle {

	@Required
	@DontObfuscate
	@Desc("The chance is 1 in CHANCE per interval")
	private NoiseStyle style = NoiseStyle.IRIS;

	@DontObfuscate
	@MinNumber(0.00001)
	@Desc("The zoom of this style")
	private double zoom = 1;

	@DontObfuscate
	@MinNumber(0.00001)
	@Desc("The Output multiplier. Only used if parent is fracture.")
	private double multiplier = 1;

	@DontObfuscate
	@Desc("Apply a generator to the coordinate field fed into this parent generator. I.e. Distort your generator with another generator.")
	private IrisGeneratorStyle fracture = null;

	@DontObfuscate
	@MinNumber(0.01562)
	@MaxNumber(64)
	@Desc("The exponent")
	private double exponent = 1;

	private final transient AtomicCache<CNG> cng = new AtomicCache<CNG>();

	public IrisGeneratorStyle() {

	}

	public IrisGeneratorStyle(NoiseStyle s) {
		this.style = s;
	}

	public CNG create(RNG rng) {
		return cng.aquire(() -> {
			CNG cng = style.create(rng).bake().scale(1D / zoom).pow(exponent).bake();

			if (fracture != null) {
				cng.fractureWith(fracture.create(rng.nextParallelRNG(2934)), fracture.getMultiplier());
			}

			return cng;
		});
	}
}