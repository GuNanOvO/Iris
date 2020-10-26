package com.volmit.iris.gen.v2.scaffold.hunk;

import java.util.concurrent.atomic.AtomicLongArray;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AtomicLongHunk extends StorageHunk<Long> implements Hunk<Long>
{
	private final AtomicLongArray data;

	protected AtomicLongHunk(int w, int h, int d)
	{
		super(w, h, d);
		data = new AtomicLongArray(w * h * d);
	}

	@Override
	public void setRaw(int x, int y, int z, Long t)
	{
		data.set(index(x, y, z), t);
	}

	@Override
	public Long getRaw(int x, int y, int z)
	{
		return data.get(index(x, y, z));
	}

	private int index(int x, int y, int z)
	{
		return (z * getWidth() * getHeight()) + (y * getWidth()) + x;
	}
}
